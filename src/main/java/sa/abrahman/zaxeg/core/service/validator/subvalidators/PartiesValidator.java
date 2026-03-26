package sa.abrahman.zaxeg.core.service.validator.subvalidators;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import org.springframework.stereotype.Service;

import sa.abrahman.zaxeg.core.exception.InvoiceRuleViolationException;
import sa.abrahman.zaxeg.core.helper.*;
import sa.abrahman.zaxeg.core.model.invoice.predefined.InvoiceSubtype;
import sa.abrahman.zaxeg.core.model.invoice.predefined.Scheme;
import sa.abrahman.zaxeg.core.model.invoice.predefined.TaxExemptionCode;
import sa.abrahman.zaxeg.core.model.invoice.predefined.TaxScheme;
import sa.abrahman.zaxeg.core.port.in.payload.InvoiceGenerationPayload;
import sa.abrahman.zaxeg.core.port.in.payload.LinesPayload;
import sa.abrahman.zaxeg.core.port.in.payload.MetadataPayload;
import sa.abrahman.zaxeg.core.port.in.payload.PartiesPayload;
import sa.abrahman.zaxeg.core.port.in.payload.PayloadCommons;
import sa.abrahman.zaxeg.core.service.contract.InvoiceValidator;
import sa.abrahman.zaxeg.core.service.validator.InvoiceValidationRule;
import sa.abrahman.zaxeg.core.service.validator.InvoiceValidatorBeanNameResolver;

@Service(InvoiceValidatorBeanNameResolver.PARTIES_VALIDATOR)
public class PartiesValidator implements InvoiceValidator {
    @Override
    public void validate(InvoiceGenerationPayload payload) {
        // initialization & sanity check
        Function<String, InvoiceRuleViolationException> f = InvoiceRuleViolationException::new;
        PartiesPayload parties = Optional.ofNullable(payload).map(InvoiceGenerationPayload::getParties).orElse(null);
        LinesPayload lines = Optional.ofNullable(payload).map(InvoiceGenerationPayload::getLines).orElse(null);
        if (parties == null)
            throw f.apply("Error Parsing Invoice Parties");

        // helpers
        // seller info
        PartiesPayload.Party seller = parties.getSeller();
        PartiesPayload.Address sellerAddress = seller.getAddress();

        // buyer info
        PartiesPayload.Party buyer = parties.getBuyer();
        PartiesPayload.Address buyerAddress = Optional.ofNullable(buyer).map(PartiesPayload.Party::getAddress).orElse(null);
        String buyerName = Optional.ofNullable(buyer).map(PartiesPayload.Party::getName).orElse("");
        String buyerCountry = Optional.ofNullable(buyerAddress).map(PartiesPayload.Address::getCountry).map(Locale::getCountry).orElse("");
        List<PartiesPayload.PartyIdentification> otherBuyerIds = Optional.ofNullable(buyer).map(PartiesPayload.Party::getOtherIds).orElse(List.of());
        String buyerVat = Optional.ofNullable(buyer).map(PartiesPayload.Party::getIdentification).map(id -> id.getCompanyId()).orElse("");

        // metada
        MetadataPayload.InvoiceTypeTransactions invoiceTypeTransactions = Optional.ofNullable(payload.getMetadata().getInvoiceTypeTransactions()).orElse(null);
        InvoiceSubtype invoiceSubtype = Optional.ofNullable(invoiceTypeTransactions).map(MetadataPayload.InvoiceTypeTransactions::getSubtype).orElse(null);

        // predicates & booleans
        Predicate<PartiesPayload.Address> addressChecker = a -> {
            StringValueValidator.check(a.getStreet(), f).exists(InvoiceValidationRule.BR_KSA_09);
            StringValueValidator.check(a.getBuildingNumber(), f).exists(InvoiceValidationRule.BR_KSA_09);
            StringValueValidator.check(a.getPostalCode(), f).exists(InvoiceValidationRule.BR_KSA_09);
            StringValueValidator.check(a.getDistrict(), f).exists(InvoiceValidationRule.BR_KSA_09);
            ObjectValueValidator.check(a.getCountry(), f).exists(InvoiceValidationRule.BR_KSA_09);
            return true;
        };
        Predicate<PayloadCommons.TaxCategory> isHealthCareOrEducationToCitizen = t -> t
                .getTaxExemptionReasonCode() == TaxExemptionCode.PRIVATE_EDUCATION_TO_CITIZEN
                || t.getTaxExemptionReasonCode() == TaxExemptionCode.PRIVATE_HEALTHCARE_TO_CITIZEN;

        boolean anyItemHasHealthcareOrEducationExemption = Optional.ofNullable(lines.getInvoiceLines())
                .orElse(List.of())
                .stream()
                .map(LinesPayload.InvoiceLine::getItem)
                .filter(Objects::nonNull)
                .map(LinesPayload.InvoiceLineItem::getClassifiedTaxCategory)
                .anyMatch(isHealthCareOrEducationToCitizen);
        Predicate<PartiesPayload.PartyIdentification> hasNationalId = pid -> pid.getSchemeId() == Scheme.NATIONAL_ID;

        // rules
        ObjectValueValidator.check(seller, f).exists(InvoiceValidationRule.BR_06);
        CollectionValueValidator.check(seller.getOtherIds(), f).notEmpty(InvoiceValidationRule.BR_KSA_08);
        ObjectValueValidator.check(sellerAddress, f).exists(InvoiceValidationRule.BR_08).matches(addressChecker, InvoiceValidationRule.BR_KSA_09);
        StringValueValidator.check(sellerAddress.getBuildingNumber(), f).hasLength(4, InvoiceValidationRule.BR_KSA_37).numeric(InvoiceValidationRule.BR_KSA_37);

        if (sellerAddress.getAdditionalNumber() != null) {
            StringValueValidator.check(sellerAddress.getAdditionalNumber(), f)
                    .hasLength(4, InvoiceValidationRule.BR_KSA_64).numeric(InvoiceValidationRule.BR_KSA_64);
        }

        StringValueValidator.check(sellerAddress.getPostalCode(), f).hasLength(5, InvoiceValidationRule.BR_KSA_66)
                .numeric(InvoiceValidationRule.BR_KSA_66);
        ObjectValueValidator.check(seller.getIdentification(), f).exists(InvoiceValidationRule.BR_KSA_39);
        StringValueValidator.check(seller.getIdentification().getCompanyId(), f).exists(InvoiceValidationRule.BR_KSA_39)
                .hasLength(15, InvoiceValidationRule.BR_KSA_40).startsAndEndsWith("3", InvoiceValidationRule.BR_KSA_40);
        StringValueValidator.check(seller.getName(), f).exists(InvoiceValidationRule.BR_06);

        boolean isVatRegistered = Optional.ofNullable(buyer)
                .map(PartiesPayload.Party::getIdentification)
                .map(id -> id.getTaxScheme())
                .map(ts -> ts == TaxScheme.VAT)
                .orElse(false);

        if (buyer != null && !isVatRegistered) {
            CollectionValueValidator.check(buyer.getOtherIds(), f).notEmpty(InvoiceValidationRule.BR_KSA_14);
        }

        if (invoiceSubtype == InvoiceSubtype.STANDARD) {
            ObjectValueValidator.check(buyerAddress, f).exists(InvoiceValidationRule.BR_10);
            StringValueValidator.check(buyerName, f).exists(InvoiceValidationRule.BR_KSA_42);
        }

        if (invoiceSubtype == InvoiceSubtype.SIMPLIFIED && invoiceTypeTransactions != null && invoiceTypeTransactions.isSummary()) {
            StringValueValidator.check(buyerName, f).exists(InvoiceValidationRule.BR_KSA_71);
        }

        if ("sa".equalsIgnoreCase(buyerCountry)) {
            ObjectValueValidator.check(buyerAddress, f).exists(InvoiceValidationRule.BR_KSA_63).matches(addressChecker, InvoiceValidationRule.BR_KSA_63);
            StringValueValidator.check(buyerAddress.getPostalCode(), f).hasLength(5, InvoiceValidationRule.BR_KSA_67).numeric(InvoiceValidationRule.BR_KSA_67);
        }

        if (invoiceTypeTransactions != null && !invoiceTypeTransactions.isExports()) {
            StringValueValidator.check(buyerVat, f).hasLength(15, InvoiceValidationRule.BR_KSA_44).startsAndEndsWith("3", InvoiceValidationRule.BR_KSA_44);
        }

        if (invoiceTypeTransactions != null && invoiceTypeTransactions.isExports()) {
            ObjectValueValidator.check(buyerVat, f).matches(String::isBlank, InvoiceValidationRule.BR_KSA_46);
        }

        if (invoiceSubtype == InvoiceSubtype.SIMPLIFIED && anyItemHasHealthcareOrEducationExemption) {
            StringValueValidator.check(buyerName, f).exists(InvoiceValidationRule.BR_KSA_25);
            CollectionValueValidator.check(otherBuyerIds, f).notEmpty(InvoiceValidationRule.BR_KSA_49).anyMatch(hasNationalId, InvoiceValidationRule.BR_KSA_49);
        }
    }
}
