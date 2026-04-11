package sa.abrahman.zaxeg.core.generate.domain.validator.metadata;

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
@Order(ValidatorsOrderRegistry.METADATA_DATE_EXISTS)
public class ValidateInvoiceDateExists implements InvoiceRuleValidator {
    private static final String PATH = "metadata.issueDate[exists]";
    private static final String RULE = UblRules.BR_02;

    @Override
    public @NonNull FailableResult<Entry<String, String>> run(InvoiceGenerationPayload data) {
        if (data.getMetadata().getIssueDate() == null) {
            return FailableResult.failed(Map.entry(PATH, RULE));
        }

        return FailableResult.okay();
    }
}
