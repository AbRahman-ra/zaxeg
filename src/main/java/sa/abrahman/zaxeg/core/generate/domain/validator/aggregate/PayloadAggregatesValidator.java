package sa.abrahman.zaxeg.core.generate.domain.validator.aggregate;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import org.springframework.stereotype.Service;

import sa.abrahman.zaxeg.core.generate.domain.constant.field.InvoiceSubtype;
import sa.abrahman.zaxeg.core.generate.domain.constant.field.Scheme;
import sa.abrahman.zaxeg.core.generate.domain.constant.field.TaxExemptionCode;
import sa.abrahman.zaxeg.core.generate.domain.constant.ValidatorBeansRegistry;
import sa.abrahman.zaxeg.core.generate.domain.constant.rule.KsaRules;
import sa.abrahman.zaxeg.core.generate.domain.exception.InvoiceRuleViolationException;
import sa.abrahman.zaxeg.core.generate.port.in.payload.InvoiceGenerationPayload;
import sa.abrahman.zaxeg.core.generate.port.in.payload.LinesPayload;
import sa.abrahman.zaxeg.core.generate.port.in.payload.MetadataPayload;
import sa.abrahman.zaxeg.core.generate.port.in.payload.PartiesPayload;
import sa.abrahman.zaxeg.core.generate.port.in.payload.PayloadCommons.TaxSubtotal;
import sa.abrahman.zaxeg.core.generate.domain.contract.InvoiceValidator;
import sa.abrahman.zaxeg.shared.helper.CollectionValueValidator;
import sa.abrahman.zaxeg.shared.helper.ObjectValueValidator;
import sa.abrahman.zaxeg.shared.helper.StringValueValidator;

/**
 * For doing validations accross subvalidators
 */
@Service(ValidatorBeansRegistry.AGGREGATES_VALIDATOR)
public class PayloadAggregatesValidator implements InvoiceValidator {
    @Override
    public void validate(InvoiceGenerationPayload payload) {
        metadataAndPartiesValidations(payload);
        metadataAndLinesValidations(payload);
        partiesAndCheckoutDetailsValidations(payload);
    }

    private void metadataAndPartiesValidations(InvoiceGenerationPayload payload) {
        // initialization & sanity check
        PartiesPayload parties = payload.getParties();
        Function<String, InvoiceRuleViolationException> f = InvoiceRuleViolationException::new;

        // helpers
        // buyer info
        PartiesPayload.Party buyer = parties.getBuyer();
        String buyerName = buyer == null ? null : buyer.getName();
        PartiesPayload.PartyTaxScheme buyerVatInfo = buyer == null ? null : buyer.getIdentification();

        InvoiceSubtype invoiceSubtype = payload.getMetadata().getInvoiceTypeTransactions().getSubtype();
        boolean isExportInvoice = payload.getMetadata().getInvoiceTypeTransactions().isExports();
        boolean isSummaryInvoice = payload.getMetadata().getInvoiceTypeTransactions().isSummary();

        // Buyer Validations
        if (invoiceSubtype != InvoiceSubtype.SIMPLIFIED) {
            ObjectValueValidator.check(buyer, f).exists(KsaRules.BR_KSA_42);
            StringValueValidator.check(buyerName, f).exists(KsaRules.BR_KSA_42);
        }

        if (invoiceSubtype == InvoiceSubtype.SIMPLIFIED && isSummaryInvoice) {
            ObjectValueValidator.check(buyer, f).exists(KsaRules.BR_KSA_71);
            StringValueValidator.check(buyerName, f).exists(KsaRules.BR_KSA_71);
        }

        if (isExportInvoice) {
            ObjectValueValidator.check(buyer, f).exists(KsaRules.BR_KSA_46);
            ObjectValueValidator.check(buyerVatInfo, f).matches(
                    i -> i == null || i.getCompanyId() == null || i.getCompanyId().isBlank(), KsaRules.BR_KSA_46);
        }

        if (buyer != null && buyer.getIdentification() != null && !isExportInvoice) {
            StringValueValidator.check(buyer.getIdentification().getCompanyId(), f).exists(KsaRules.BR_KSA_44)
                    .hasLength(15, KsaRules.BR_KSA_44).startsAndEndsWith("3", KsaRules.BR_KSA_44);
        }
    }

    private void metadataAndLinesValidations(InvoiceGenerationPayload payload) {
        // initialization
        MetadataPayload metadata = payload.getMetadata();
        List<LinesPayload.InvoiceLine> lines = payload.getLines().getInvoiceLines();
        Function<String, InvoiceRuleViolationException> f = InvoiceRuleViolationException::new;

        // rules
        CollectionValueValidator.check(lines, f).allMatch(
                l -> l.getNetAmount().getCurrency().equals(metadata.getInvoiceCurrency()), KsaRules.BR_KSA_CL_02);
        CollectionValueValidator.check(lines, f).allMatch(
                l -> l.getAllowanceCharges().stream()
                        .allMatch(ac -> ac.getAmount() != null
                                && ac.getAmount().getCurrency().equals(metadata.getInvoiceCurrency())),
                KsaRules.BR_KSA_CL_02);
    }

    private void partiesAndCheckoutDetailsValidations(InvoiceGenerationPayload payload) {
        // initialization
        InvoiceSubtype invoiceSubtype = payload.getMetadata().getInvoiceTypeTransactions().getSubtype();
        Function<String, InvoiceRuleViolationException> f = InvoiceRuleViolationException::new;

        // buyer info
        PartiesPayload.Party buyer = payload.getParties().getBuyer();
        String buyerName = buyer == null ? null : buyer.getName();
        List<PartiesPayload.PartyIdentification> buyerIds = buyer == null ? List.of() : buyer.getOtherIds();

        // lines info TODO: REMOVE CONDITION AFTER IMPLEMENTATING CHECKOUT DETAILS
        if (payload.getCheckout() != null) {
            boolean hasEducationalOrHealthExemption = payload.getCheckout().getInvoiceTaxTotals().getTaxSubtotal()
                    .stream().map(TaxSubtotal::getTaxCategory).filter(Objects::nonNull)
                    .anyMatch(tax -> tax.getTaxExemptionReasonCode() == TaxExemptionCode.PRIVATE_EDUCATION_TO_CITIZEN
                            || tax.getTaxExemptionReasonCode() == TaxExemptionCode.PRIVATE_HEALTHCARE_TO_CITIZEN);

            if (invoiceSubtype == InvoiceSubtype.SIMPLIFIED && hasEducationalOrHealthExemption) {
                ObjectValueValidator.check(buyer, f).exists(KsaRules.BR_KSA_25);
                StringValueValidator.check(buyerName, f).exists(KsaRules.BR_KSA_25);
            }

            if (hasEducationalOrHealthExemption) {
                ObjectValueValidator.check(buyer, f).exists(KsaRules.BR_KSA_49);
                CollectionValueValidator.check(buyerIds, f).exists(KsaRules.BR_KSA_49).notEmpty(KsaRules.BR_KSA_49)
                        .anyMatch(id -> id.getSchemeId() == Scheme.NATIONAL_ID, KsaRules.BR_KSA_49);
            }
        }

    }
}
