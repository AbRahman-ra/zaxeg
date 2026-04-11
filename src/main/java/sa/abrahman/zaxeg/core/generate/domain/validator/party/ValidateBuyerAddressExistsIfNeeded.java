package sa.abrahman.zaxeg.core.generate.domain.validator.party;

import java.util.Map;
import java.util.Map.Entry;

import org.jspecify.annotations.NonNull;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import sa.abrahman.zaxeg.core.generate.domain.config.ValidatorsOrderRegistry;
import sa.abrahman.zaxeg.core.generate.domain.constant.InvoiceSubtype;
import sa.abrahman.zaxeg.core.generate.domain.contract.InvoiceRuleValidator;
import sa.abrahman.zaxeg.core.generate.port.in.payload.InvoiceGenerationPayload;
import sa.abrahman.zaxeg.core.shared.dto.FailableResult;
import sa.abrahman.zaxeg.shared.constant.rule.UblRules;

@Component
@Order(ValidatorsOrderRegistry.PARTY_BUYER_ADDRESS_EXISTS_IF_NEEDED)
public class ValidateBuyerAddressExistsIfNeeded implements InvoiceRuleValidator {
    private static final String PATH = "parties.buyer.address";
    private static final String RULE = UblRules.BR_10;

    @Override
    public @NonNull FailableResult<Entry<String, String>> run(InvoiceGenerationPayload data) {
        boolean isSimplifiedInvoice = data.getMetadata().getInvoiceTypeTransactions()
                .getSubtype() == InvoiceSubtype.SIMPLIFIED;
        boolean hasNoAddress = data.getParties().getBuyer() == null
                || data.getParties().getBuyer().getAddress() == null;
        if (!isSimplifiedInvoice && hasNoAddress) {
            return FailableResult.failed(Map.entry(PATH, RULE));
        }

        return FailableResult.okay();
    }
}
