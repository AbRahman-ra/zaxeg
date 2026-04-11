package sa.abrahman.zaxeg.core.generate.domain.validator.party;

import java.util.Map;
import java.util.Map.Entry;

import org.jspecify.annotations.NonNull;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import sa.abrahman.zaxeg.core.generate.domain.config.ValidatorsOrderRegistry;
import sa.abrahman.zaxeg.core.generate.domain.contract.InvoiceRuleValidator;
import sa.abrahman.zaxeg.core.generate.port.in.payload.InvoiceGenerationPayload;
import sa.abrahman.zaxeg.core.shared.dto.FailableResult;
import sa.abrahman.zaxeg.shared.constant.rule.KsaRules;

@Component
@Order(ValidatorsOrderRegistry.PARTY_SELLER_ADDRESS_COMPLIANT)
public class ValidateSellerAddressIsCompliant implements InvoiceRuleValidator {
    private static final String PATH = "parties.seller.address.*";
    private static final String RULE = KsaRules.BR_KSA_09;

    @Override
    public @NonNull FailableResult<Entry<String, String>> run(InvoiceGenerationPayload data) {
        var address = data.getParties().getSeller().getAddress();
        boolean isInvalidAddress = address.getStreet() == null || address.getStreet().isBlank()
                || address.getBuildingNumber() == null || address.getBuildingNumber().isBlank()
                || address.getPostalCode() == null || address.getPostalCode().isBlank()
                || address.getCity() == null || address.getCity().isBlank()
                || address.getDistrict() == null || address.getDistrict().isBlank()
                || address.getCountry() == null;

        if (isInvalidAddress) {
            return FailableResult.failed(Map.entry(PATH, RULE));
        }

        return FailableResult.okay();
    }
}
