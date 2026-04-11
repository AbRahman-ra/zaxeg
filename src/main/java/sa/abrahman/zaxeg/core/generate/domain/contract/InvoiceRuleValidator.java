package sa.abrahman.zaxeg.core.generate.domain.contract;

import java.util.Map.Entry;

import sa.abrahman.zaxeg.core.generate.port.in.payload.InvoiceGenerationPayload;
import sa.abrahman.zaxeg.core.shared.contract.FailableJob;

public interface InvoiceRuleValidator extends FailableJob<InvoiceGenerationPayload, Entry<String, String>> {
    //
}
