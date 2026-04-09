package sa.abrahman.zaxeg.core.generate.domain.contract;

import java.util.Collection;
import java.util.Map;

import sa.abrahman.zaxeg.core.generate.port.in.payload.InvoiceGenerationPayload;
import sa.abrahman.zaxeg.core.shared.contract.FailableStrategy;

public interface InvoiceValidationFailStrategy
        extends FailableStrategy<InvoiceGenerationPayload, Collection<InvoiceRuleValidator>, Map<String, String>> {}
