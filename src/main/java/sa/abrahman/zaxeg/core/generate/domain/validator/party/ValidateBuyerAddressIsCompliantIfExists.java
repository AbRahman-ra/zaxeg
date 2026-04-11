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
@Order(ValidatorsOrderRegistry.PARTY_BUYER_ADDRESS_COMPLIANT_IF_EXISTS)
public class ValidateBuyerAddressIsCompliantIfExists implements InvoiceRuleValidator {
    private static final String PATH = "parties.buyer.address.*";
    private static final String RULE = KsaRules.BR_KSA_10;

    @Override
    public @NonNull FailableResult<Entry<String, String>> run(InvoiceGenerationPayload data) {
        var address = Optional.ofNullable(data.getParties().getBuyer()).map(PartiesPayload.Party::getAddress).orElse(null);
        boolean isInvalidAddress = address == null
                || address.getStreet() == null || address.getStreet().isBlank()
                || address.getCity() == null || address.getCity().isBlank()
                || address.getCountry() == null;

        if (isInvalidAddress) {
            return FailableResult.failed(Map.entry(PATH, RULE));
        }

        return FailableResult.okay();
    }
}
