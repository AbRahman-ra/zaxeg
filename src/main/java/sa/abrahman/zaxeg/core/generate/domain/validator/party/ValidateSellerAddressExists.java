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
import sa.abrahman.zaxeg.shared.constant.rule.UblRules;

@Component
@Order(ValidatorsOrderRegistry.PARTY_SELLER_ADDRESS_EXISTS)
public class ValidateSellerAddressExists implements InvoiceRuleValidator {
    private static final String PATH = "parties.seller.address";
    private static final String RULE = UblRules.BR_08;

    @Override
    public @NonNull FailableResult<Entry<String, String>> run(InvoiceGenerationPayload data) {
        if (data.getParties().getSeller().getAddress() == null) {
            return FailableResult.failed(Map.entry(PATH, RULE));
        }

        return FailableResult.okay();
    }
}
