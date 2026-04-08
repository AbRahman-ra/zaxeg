package sa.abrahman.zaxeg.core.generate.domain.validator.component.metadata;

import java.util.Map;
import java.util.Map.Entry;

import sa.abrahman.zaxeg.core.generate.domain.constant.rule.UblRules;
import sa.abrahman.zaxeg.core.generate.port.in.payload.InvoiceGenerationPayload;
import sa.abrahman.zaxeg.core.shared.contract.FailableJob;
import sa.abrahman.zaxeg.core.shared.dto.FailableResult;

public class InvoiceNumberExistanceValidator implements FailableJob<InvoiceGenerationPayload> {
    private static final String PATH = "metadata.invoiceNumber";
    private static final String RULE = UblRules.BR_02;

    @Override
    public FailableResult<Entry<String, String>> run(InvoiceGenerationPayload data) {
        if (!data.getMetadata().getInvoiceNumber().isBlank()) {
            return FailableResult.of(true, null);
        }
        return FailableResult.of(false, Map.entry(PATH, RULE));
    }
}
