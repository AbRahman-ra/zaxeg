package sa.abrahman.zaxeg.core.generate.domain.validator.component;

import java.util.function.Function;
import java.util.function.Predicate;

import org.springframework.stereotype.Service;

import sa.abrahman.zaxeg.core.generate.domain.constant.ValidatorBeansRegistry;
import sa.abrahman.zaxeg.core.generate.domain.constant.rule.KsaRules;
import sa.abrahman.zaxeg.core.generate.domain.constant.rule.UblRules;
import sa.abrahman.zaxeg.core.generate.domain.exception.InvoiceRuleViolationException;
import sa.abrahman.zaxeg.core.generate.port.in.payload.InvoiceGenerationPayload;
import sa.abrahman.zaxeg.core.generate.port.in.payload.PartiesPayload;
import sa.abrahman.zaxeg.core.generate.domain.contract.InvoiceValidator;
import sa.abrahman.zaxeg.shared.helper.*;

@Service(ValidatorBeansRegistry.PARTIES_VALIDATOR)
public class PartiesValidator implements InvoiceValidator {
    @Override
    public void validate(InvoiceGenerationPayload payload) {
        // initialization & sanity check
        PartiesPayload parties = payload.getParties();
        Function<String, InvoiceRuleViolationException> f = InvoiceRuleViolationException::new;

        // helpers
        // seller info
        PartiesPayload.Party seller = parties.getSeller();
        PartiesPayload.Address sellerAddress = seller.getAddress();

        // buyer info
        PartiesPayload.Party buyer = parties.getBuyer();

        // predicates & booleans
        Predicate<PartiesPayload.Address> isCompliantSaudiPartyAddress = a -> !a.getStreet().isBlank()
                && !a.getBuildingNumber().isBlank() && !a.getPostalCode().isBlank() && !a.getCity().isBlank()
                && !a.getDistrict().isBlank() && a.getCountry() != null;

        Predicate<PartiesPayload.Address> isCompliantBuyerAddress = a -> !a.getStreet().isBlank()
                && !a.getCity().isBlank() && a.getCountry() != null;

        // RULES
        // Seller Validations
        ObjectValueValidator.check(seller, f).exists(UblRules.BR_06);
        CollectionValueValidator.check(seller.getOtherIds(), f).notEmpty(KsaRules.BR_KSA_08);
        ObjectValueValidator.check(sellerAddress, f).exists(UblRules.BR_08).matches(isCompliantSaudiPartyAddress,
                KsaRules.BR_KSA_09);
        StringValueValidator.check(sellerAddress.getBuildingNumber(), f).hasLength(4, KsaRules.BR_KSA_37)
                .numeric(KsaRules.BR_KSA_37);

        if (!sellerAddress.getAdditionalNumber().isBlank()) {
            StringValueValidator.check(sellerAddress.getAdditionalNumber(), f).hasLength(4, KsaRules.BR_KSA_64)
                    .numeric(KsaRules.BR_KSA_64);
        }

        StringValueValidator.check(sellerAddress.getPostalCode(), f).hasLength(5, KsaRules.BR_KSA_66)
                .numeric(KsaRules.BR_KSA_66);
        ObjectValueValidator.check(seller.getIdentification(), f).exists(KsaRules.BR_KSA_39);
        StringValueValidator.check(seller.getIdentification().getCompanyId(), f).exists(KsaRules.BR_KSA_39)
                .hasLength(15, KsaRules.BR_KSA_40).startsAndEndsWith("3", KsaRules.BR_KSA_40);
        StringValueValidator.check(seller.getName(), f).exists(UblRules.BR_06);

        if (buyer != null) {
            PartiesPayload.Address buyerAddress = buyer.getAddress();
            ObjectValueValidator.check(buyerAddress, f).exists(UblRules.BR_10).matches(isCompliantBuyerAddress,
                    KsaRules.BR_KSA_10);
            if ("sa".equalsIgnoreCase(buyerAddress.getCountry().getCountry())) {
                ObjectValueValidator.check(buyerAddress, f).exists(UblRules.BR_10).matches(isCompliantSaudiPartyAddress,
                        KsaRules.BR_KSA_63);
                StringValueValidator.check(buyerAddress.getPostalCode(), f).hasLength(5, KsaRules.BR_KSA_67)
                        .numeric(KsaRules.BR_KSA_67);
            }

            if (!buyerAddress.getAdditionalNumber().isBlank()) {
                StringValueValidator.check(buyerAddress.getAdditionalNumber(), f).exists(KsaRules.BR_KSA_65);
            }

            if (buyer.getIdentification() == null) {
                CollectionValueValidator.check(buyer.getOtherIds(), f).notEmpty(KsaRules.BR_KSA_14);
            }
        }
    }
}
