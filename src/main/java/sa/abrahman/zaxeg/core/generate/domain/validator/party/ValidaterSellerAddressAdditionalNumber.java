package sa.abrahman.zaxeg.core.generate.domain.validator.party;

import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;

import org.jspecify.annotations.NonNull;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import sa.abrahman.zaxeg.core.generate.domain.config.ValidatorsOrderRegistry;
import sa.abrahman.zaxeg.core.generate.domain.contract.InvoiceRuleValidator;
import sa.abrahman.zaxeg.core.generate.port.in.payload.InvoiceGenerationPayload;
import sa.abrahman.zaxeg.core.generate.port.in.payload.PartiesPayload;
import sa.abrahman.zaxeg.core.shared.dto.FailableResult;
import sa.abrahman.zaxeg.shared.constant.rule.KsaRules;

@Component
@Order(ValidatorsOrderRegistry.PARTY_SELLER_ADDRESS_ADD_NUMBER)
public class ValidaterSellerAddressAdditionalNumber implements InvoiceRuleValidator {
    private static final String PATH = "parties.seller.address.additionalNumber";
    private static final String RULE = KsaRules.BR_KSA_64;
    private static final String MATCHER = "\\d{4}";

    @Override
    public @NonNull FailableResult<Entry<String, String>> run(InvoiceGenerationPayload data) {
        String bldg = Optional.ofNullable(data.getParties().getSeller().getAddress()).map(PartiesPayload.Address::getAdditionalNumber).orElse(null);

        if (bldg != null && (bldg.isBlank() || !bldg.matches(MATCHER))) {
            return FailableResult.failed(Map.entry(PATH, RULE));
        }

        return FailableResult.okay();
    }
}
