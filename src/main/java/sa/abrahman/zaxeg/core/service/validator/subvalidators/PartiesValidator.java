package sa.abrahman.zaxeg.core.service.validator.subvalidators;

import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import org.springframework.stereotype.Service;

import sa.abrahman.zaxeg.core.exception.InvoiceRuleViolationException;
import sa.abrahman.zaxeg.core.helper.*;
import sa.abrahman.zaxeg.core.model.invoice.predefined.InvoiceSubtype;
import sa.abrahman.zaxeg.core.model.invoice.predefined.TaxScheme;
import sa.abrahman.zaxeg.core.port.in.payload.InvoiceGenerationPayload;
import sa.abrahman.zaxeg.core.port.in.payload.MetadataPayload;
import sa.abrahman.zaxeg.core.port.in.payload.PartiesPayload;
import sa.abrahman.zaxeg.core.service.contract.InvoiceValidator;
import sa.abrahman.zaxeg.core.service.validator.InvoiceValidationRule;
import sa.abrahman.zaxeg.core.service.validator.InvoiceValidatorBeanNameResolver;

@Service(InvoiceValidatorBeanNameResolver.PARTIES_VALIDATOR)
public class PartiesValidator implements InvoiceValidator {
    @Override
    public void validate(InvoiceGenerationPayload payload) {
        // initialization & sanity check
        Function<String, InvoiceRuleViolationException> f = InvoiceRuleViolationException::new;
        PartiesPayload parties = Optional.ofNullable(payload).map(p -> p.getParties()).orElse(null);
        if (parties == null)
            throw f.apply("Error Parsing Invoice Parties");

        // helpers
        PartiesPayload.Party seller = parties.getSeller();
        PartiesPayload.Address sellerAddress = seller.getAddress();
        PartiesPayload.Party buyer = parties.getBuyer();
        PartiesPayload.Address buyerAddress = Optional.ofNullable(buyer).map(PartiesPayload.Party::getAddress).orElse(null);
        String buyerName = Optional.ofNullable(buyer).map(PartiesPayload.Party::getName).orElse(null);
        String buyerCountry = Optional.ofNullable(buyerAddress).map(PartiesPayload.Address::getCountry).map(Locale::getCountry).orElse(null);
        MetadataPayload.InvoiceTypeTransactions invoiceTypeTransactions = Optional.ofNullable(payload.getMetadata().getInvoiceTypeTransactions()).orElse(null);
        InvoiceSubtype invoiceSubtype = Optional.ofNullable(invoiceTypeTransactions).map(MetadataPayload.InvoiceTypeTransactions::getSubtype).orElse(null);
        String buyerVat = Optional.ofNullable(buyer).map(PartiesPayload.Party::getIdentification).map(id -> id.getCompanyId()).orElse(null);
        Predicate<PartiesPayload.Address> addressChecker = a -> {
            StringValueValidator.check(a.getStreet(), f).exists(InvoiceValidationRule.BR_KSA_09);
            StringValueValidator.check(a.getBuildingNumber(), f).exists(InvoiceValidationRule.BR_KSA_09);
            StringValueValidator.check(a.getPostalCode(), f).exists(InvoiceValidationRule.BR_KSA_09);
            StringValueValidator.check(a.getDistrict(), f).exists(InvoiceValidationRule.BR_KSA_09);
            ObjectValueValidator.check(a.getCountry(), f).exists(InvoiceValidationRule.BR_KSA_09);
            return true;
        };

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

        if (buyer != null && "sa".equalsIgnoreCase(buyerCountry)) {
            ObjectValueValidator.check(buyerAddress, f).exists(InvoiceValidationRule.BR_KSA_63).matches(addressChecker, InvoiceValidationRule.BR_KSA_63);
            // exists will eliminate possibilities for NullPointerException, so if IDE is shouting because of buyerAddress.getPostalCode() just don't listen to it
            StringValueValidator.check(buyerAddress.getPostalCode(), f).hasLength(5, InvoiceValidationRule.BR_KSA_67).numeric(InvoiceValidationRule.BR_KSA_67);
        }

        if (buyerVat != null && invoiceTypeTransactions != null && !invoiceTypeTransactions.isExports()) {
            StringValueValidator.check(buyerVat, f).hasLength(15, InvoiceValidationRule.BR_KSA_44).startsAndEndsWith("3", InvoiceValidationRule.BR_KSA_44);
        }

        if (invoiceTypeTransactions != null && invoiceTypeTransactions.isExports()) {
            ObjectValueValidator.check(buyerVat, f).matches(v -> v == null || v.isBlank(), InvoiceValidationRule.BR_KSA_46);
        }
    }
}
