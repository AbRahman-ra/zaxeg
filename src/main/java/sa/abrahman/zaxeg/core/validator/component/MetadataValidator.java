package sa.abrahman.zaxeg.core.validator.component;

import java.util.function.Function;

import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Service;

import sa.abrahman.zaxeg.core.exception.InvoiceRuleViolationException;
import sa.abrahman.zaxeg.core.factory.bean.ValidatorBeansRegistry;
import sa.abrahman.zaxeg.core.helper.*;
import sa.abrahman.zaxeg.core.model.invoice.predefined.InvoiceDocumentType;
import sa.abrahman.zaxeg.core.model.invoice.predefined.InvoiceSubtype;
import sa.abrahman.zaxeg.core.port.in.payload.InvoiceGenerationPayload;
import sa.abrahman.zaxeg.core.port.in.payload.MetadataPayload;
import sa.abrahman.zaxeg.core.service.contract.InvoiceValidator;
import sa.abrahman.zaxeg.core.validator.rule.KsaRules;
import sa.abrahman.zaxeg.core.validator.rule.UblRules;

@Service(ValidatorBeansRegistry.METADATA_VALIDATOR)
@NullMarked
public class MetadataValidator implements InvoiceValidator {
    @Override
    public void validate(InvoiceGenerationPayload payload) {
        // initialization & sanity check
        Function<String, InvoiceRuleViolationException> f = InvoiceRuleViolationException::new;
        MetadataPayload metadata = payload.getMetadata();

        // helpers
        boolean isStandardInvoice = metadata.getInvoiceTypeTransactions().getSubtype() == InvoiceSubtype.STANDARD;
        boolean isSimplifiedInvoice = metadata.getInvoiceTypeTransactions().getSubtype() == InvoiceSubtype.SIMPLIFIED;
        boolean isCreditNote = metadata.getInvoiceDocumentType() == InvoiceDocumentType.CREDIT_NOTE;
        boolean isDebitNote = metadata.getInvoiceDocumentType() == InvoiceDocumentType.DEBIT_NOTE;
        boolean isTaxInvoice = metadata.getInvoiceDocumentType() == InvoiceDocumentType.TAX_INVOICE;

        // rules
        StringValueValidator.check(metadata.getInvoiceNumber(), f).exists(UblRules.BR_02);
        ObjectValueValidator.check(metadata.getInvoiceUuid(), f).exists(KsaRules.BR_KSA_03);
        DateValueValidator.check(metadata.getIssueDate(), f).exists(UblRules.BR_03)
                .notInFuture(KsaRules.BR_KSA_04);
        ObjectValueValidator.check(metadata.getIssueTime(), f).exists(KsaRules.BR_KSA_70);
        ObjectValueValidator.check(metadata.getInvoiceDocumentType(), f).exists(UblRules.BR_04);
        ObjectValueValidator.check(metadata.getInvoiceTypeTransactions(), f).exists(KsaRules.BR_KSA_06)
                .matches(t -> !(t.isSelfBilled() && t.isExports()), KsaRules.BR_KSA_07);
        ObjectValueValidator.check(metadata.getInvoiceCurrency(), f).exists(UblRules.BR_05);
        ObjectValueValidator.check(metadata.getTaxCurrency(), f).exists(KsaRules.BR_KSA_68)
                .matches(c -> "sar".equalsIgnoreCase(c.getCurrencyCode()), KsaRules.BR_KSA_EN16391_02);
        if (isCreditNote || isDebitNote) {
            ObjectValueValidator.check(metadata.getBillingReference(), f).exists(KsaRules.BR_KSA_56)
                    .matches(ref -> ref.getId() != null, KsaRules.BR_KSA_56);
            CollectionValueValidator.check(metadata.getCreditOrDebitNoteIssuanceReasons(), f)
                    .notEmpty(KsaRules.BR_KSA_17);
        }

        if (isStandardInvoice && isTaxInvoice) {
            DateValueValidator.check(metadata.getSupplyDate(), f).exists(KsaRules.BR_KSA_15);
        }

        if (isSimplifiedInvoice && metadata.getInvoiceTypeTransactions().isSummary()) {
            DateValueValidator.check(metadata.getSupplyDate(), f).exists(KsaRules.BR_KSA_72);
            DateValueValidator.check(metadata.getSupplyEndDate(), f).exists(KsaRules.BR_KSA_72);
        }

        if (metadata.getSupplyEndDate() != null) {
            DateValueValidator.check(metadata.getSupplyDate(), f).exists(KsaRules.BR_KSA_35)
                    .before(metadata.getSupplyEndDate(), KsaRules.BR_KSA_36);
        }
    }
}
