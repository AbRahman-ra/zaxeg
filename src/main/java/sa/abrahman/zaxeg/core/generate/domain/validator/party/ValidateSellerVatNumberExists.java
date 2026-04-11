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
@Order(ValidatorsOrderRegistry.PARTY_SELLER_VAT_EXISTS)
public class ValidateSellerVatNumberExists implements InvoiceRuleValidator {
    private static final String PATH = "parties.seller.identification.companyId";
    private static final String RULE = KsaRules.BR_KSA_39;

    @Override
    public @NonNull FailableResult<Entry<String, String>> run(InvoiceGenerationPayload data) {
        if (data.getParties().getSeller().getIdentification() == null
                || data.getParties().getSeller().getIdentification().getCompanyId() == null
                || data.getParties().getSeller().getIdentification().getCompanyId().isBlank()) {
            return FailableResult.failed(Map.entry(PATH, RULE));
        }

        return FailableResult.okay();
    }
}
