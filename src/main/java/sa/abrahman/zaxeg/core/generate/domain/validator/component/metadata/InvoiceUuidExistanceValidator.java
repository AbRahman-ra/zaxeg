package sa.abrahman.zaxeg.core.generate.domain.validator.component.metadata;

import java.util.Map;
import java.util.Map.Entry;

import sa.abrahman.zaxeg.core.generate.domain.contract.InvoiceRuleValidator;
import sa.abrahman.zaxeg.core.generate.port.in.payload.InvoiceGenerationPayload;
import sa.abrahman.zaxeg.core.shared.dto.FailableResult;
import sa.abrahman.zaxeg.shared.constant.rule.KsaRules;

public class InvoiceUuidExistanceValidator implements InvoiceRuleValidator {
    private static final String PATH = "metadata.invoiceUuid";
    private static final String RULE = KsaRules.BR_KSA_03;

    @Override
    public FailableResult<Entry<String, String>> run(InvoiceGenerationPayload data) {
        if (data.getMetadata().getInvoiceUuid() != null) {
            return FailableResult.of(true, null);
        }
        return FailableResult.of(false, Map.entry(PATH, RULE));
    }
}
