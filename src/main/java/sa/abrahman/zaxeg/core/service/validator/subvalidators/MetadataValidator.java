package sa.abrahman.zaxeg.core.service.validator.subvalidators;

import java.util.Optional;
import java.util.function.Function;

import org.springframework.stereotype.Service;

import sa.abrahman.zaxeg.core.exception.InvoiceRuleViolationException;
import sa.abrahman.zaxeg.core.helper.*;
import sa.abrahman.zaxeg.core.model.invoice.predefined.InvoiceDocumentType;
import sa.abrahman.zaxeg.core.model.invoice.predefined.InvoiceSubtype;
import sa.abrahman.zaxeg.core.port.in.payload.InvoiceGenerationPayload;
import sa.abrahman.zaxeg.core.port.in.payload.MetadataPayload;
import sa.abrahman.zaxeg.core.service.contract.InvoiceValidator;
import sa.abrahman.zaxeg.core.service.validator.InvoiceValidationRule;
import sa.abrahman.zaxeg.core.service.validator.InvoiceValidatorBeanNameResolver;

@Service(InvoiceValidatorBeanNameResolver.METADATA_VALIDATOR)
public class MetadataValidator implements InvoiceValidator {
    @Override
    public void validate(InvoiceGenerationPayload payload) {
        // initialization & sanity check
        Function<String, InvoiceRuleViolationException> f = InvoiceRuleViolationException::new;
        MetadataPayload metadata = Optional.ofNullable(payload).map(p -> p.getMetadata()).orElse(null);
        if (metadata == null)
            throw f.apply("Error Parsing Invoice Metadata");

        // helpers
        boolean isStandardInvoice = metadata.getInvoiceTypeTransactions().getSubtype() == InvoiceSubtype.STANDARD;
        boolean isSimplifiedInvoice = metadata.getInvoiceTypeTransactions().getSubtype() == InvoiceSubtype.SIMPLIFIED;
        boolean isCreditNote = metadata.getInvoiceDocumentType() == InvoiceDocumentType.CREDIT_NOTE;
        boolean isDebitNote = metadata.getInvoiceDocumentType() == InvoiceDocumentType.DEBIT_NOTE;
        boolean isTaxInvoice = metadata.getInvoiceDocumentType() == InvoiceDocumentType.TAX_INVOICE;

        // rules
        StringValueValidator.check(metadata.getInvoiceNumber(), f).exists(InvoiceValidationRule.BR_02);
        ObjectValueValidator.check(metadata.getInvoiceUuid(), f).exists(InvoiceValidationRule.BR_KSA_03);
        DateValueValidator.check(metadata.getIssueDate(), f).exists(InvoiceValidationRule.BR_03)
                .notInFuture(InvoiceValidationRule.BR_KSA_04);
        ObjectValueValidator.check(metadata.getIssueTime(), f).exists(InvoiceValidationRule.BR_KSA_70);
        ObjectValueValidator.check(metadata.getInvoiceDocumentType(), f).exists(InvoiceValidationRule.BR_04);
        ObjectValueValidator.check(metadata.getInvoiceTypeTransactions(), f).exists(InvoiceValidationRule.BR_KSA_06)
                .matches(t -> !(t.isSelfBilled() && t.isExports()), InvoiceValidationRule.BR_KSA_07);
        ObjectValueValidator.check(metadata.getInvoiceCurrency(), f).exists(InvoiceValidationRule.BR_05);
        ObjectValueValidator.check(metadata.getTaxCurrency(), f).exists(InvoiceValidationRule.BR_KSA_68)
                .matches(c -> "sar".equalsIgnoreCase(c.getCurrencyCode()), InvoiceValidationRule.BR_KSA_EN16391_03);
        if (isCreditNote || isDebitNote) {
            ObjectValueValidator.check(metadata.getBillingReference(), f).exists(InvoiceValidationRule.BR_KSA_56)
                    .matches(ref -> ref.getId() != null, InvoiceValidationRule.BR_KSA_56);
            CollectionValueValidator.check(metadata.getCreditOrDebitNoteIssuanceReasons(), f)
                    .notEmpty(InvoiceValidationRule.BR_KSA_17);
        }

        if (isStandardInvoice && isTaxInvoice) {
            DateValueValidator.check(metadata.getSupplyDate(), f).exists(InvoiceValidationRule.BR_KSA_15);
        }

        if (isSimplifiedInvoice && metadata.getInvoiceTypeTransactions().isSummary()) {
            DateValueValidator.check(metadata.getSupplyDate(), f).exists(InvoiceValidationRule.BR_KSA_72);
            DateValueValidator.check(metadata.getSupplyEndDate(), f).exists(InvoiceValidationRule.BR_KSA_72);
        }

        if (metadata.getSupplyEndDate() != null) {
            DateValueValidator.check(metadata.getSupplyDate(), f).exists(InvoiceValidationRule.BR_KSA_35)
                    .before(metadata.getSupplyEndDate(), InvoiceValidationRule.BR_KSA_36);
        }
    }
}
