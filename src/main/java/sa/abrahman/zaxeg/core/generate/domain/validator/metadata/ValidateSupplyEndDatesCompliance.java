package sa.abrahman.zaxeg.core.generate.domain.validator.metadata;

import java.time.LocalDate;
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
@Order(ValidatorsOrderRegistry.METADATA_SUPPLY_AND_END_DATES_02)
public class ValidateSupplyEndDatesCompliance implements InvoiceRuleValidator {
    private static final String PATH = "metadata.supplyDate,supplyEndDate[supplyEndDate >= supplyDate]";
    private static final String RULE = KsaRules.BR_KSA_36;

    @Override
    public @NonNull FailableResult<Entry<String, String>> run(InvoiceGenerationPayload data) {
        LocalDate supplyDate = data.getMetadata().getSupplyDate();
        LocalDate supplyEndDate = data.getMetadata().getSupplyEndDate();

        if (supplyDate != null && supplyEndDate != null && supplyEndDate.isBefore(supplyDate)) {
            return FailableResult.failed(Map.entry(PATH, RULE));
        }

        return FailableResult.okay();
    }
}
