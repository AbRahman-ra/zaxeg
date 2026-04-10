package sa.abrahman.zaxeg.core.generate.domain.validator.metadata;

import java.util.Map;
import java.util.Map.Entry;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import sa.abrahman.zaxeg.core.generate.config.ValidatorsOrderRegistry;
import sa.abrahman.zaxeg.core.generate.domain.contract.InvoiceRuleValidator;
import sa.abrahman.zaxeg.core.generate.port.in.payload.InvoiceGenerationPayload;
import sa.abrahman.zaxeg.core.shared.dto.FailableResult;
import sa.abrahman.zaxeg.shared.constant.rule.UblRules;

@Component
@Order(ValidatorsOrderRegistry.METADATA_DOCUMENT_TYPE)
public class ValidateDocumentTypeExists implements InvoiceRuleValidator {
    private static final String PATH = "metadata.invoiceDocumentType";
    private static final String RULE = UblRules.BR_04;

    @Override
    public FailableResult<Entry<String, String>> run(InvoiceGenerationPayload data) {
        if (data.getMetadata().getInvoiceDocumentType() == null) {
            return FailableResult.failed(Map.entry(PATH, RULE));
        }

        return FailableResult.okay();
    }
}
