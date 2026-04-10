package sa.abrahman.zaxeg.core.generate.domain.validator.metadata;

import java.util.Map;
import java.util.Map.Entry;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import sa.abrahman.zaxeg.core.generate.config.ValidatorsOrderRegistry;
import sa.abrahman.zaxeg.core.generate.domain.contract.InvoiceRuleValidator;
import sa.abrahman.zaxeg.core.generate.port.in.payload.InvoiceGenerationPayload;
import sa.abrahman.zaxeg.core.shared.dto.FailableResult;
import sa.abrahman.zaxeg.shared.constant.rule.KsaRules;

@Component
@Order(ValidatorsOrderRegistry.METADATA_UUID)
public class ValidateInvoiceUuidExists implements InvoiceRuleValidator {
    private static final String PATH = "metadata.invoiceUuid";
    private static final String RULE = KsaRules.BR_KSA_03;

    @Override
    public FailableResult<Entry<String, String>> run(InvoiceGenerationPayload data) {
        if (data.getMetadata().getInvoiceUuid() != null) {
            return FailableResult.okay();
        }
        return FailableResult.failed(Map.entry(PATH, RULE));
    }
}
