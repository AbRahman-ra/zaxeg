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
@Order(ValidatorsOrderRegistry.PARTY_SELLER_ADDRESS_POSTAL_CODE)
public class ValidaterSellerPostalCode implements InvoiceRuleValidator {
    private static final String PATH = "parties.seller.address.postalCode";
    private static final String RULE = KsaRules.BR_KSA_66;
    private static final String MATCHER = "\\d{5}";

    @Override
    public @NonNull FailableResult<Entry<String, String>> run(InvoiceGenerationPayload data) {
        String postalcode = data.getParties().getSeller().getAddress().getPostalCode();

        if (postalcode.isBlank() || !postalcode.matches(MATCHER)) {
            return FailableResult.failed(Map.entry(PATH, RULE));
        }

        return FailableResult.okay();
    }
}
