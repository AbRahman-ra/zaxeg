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
@Order(ValidatorsOrderRegistry.PARTY_SELLER_ID)
public class ValidateSellerIdentificationExists implements InvoiceRuleValidator {
    private static final String PATH = "parties.seller.otherIds";
    private static final String RULE = KsaRules.BR_KSA_08;

    @Override
    public @NonNull FailableResult<Entry<String, String>> run(InvoiceGenerationPayload data) {
        if (data.getParties().getSeller().getOtherIds() == null
                || data.getParties().getSeller().getOtherIds().isEmpty()) {
            return FailableResult.failed(Map.entry(PATH, RULE));
        }

        return FailableResult.okay();
    }
}
