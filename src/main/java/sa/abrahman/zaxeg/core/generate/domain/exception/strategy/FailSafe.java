package sa.abrahman.zaxeg.core.generate.domain.exception.strategy;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.stereotype.Component;

import sa.abrahman.zaxeg.core.generate.domain.contract.InvoiceRuleValidator;
import sa.abrahman.zaxeg.core.generate.domain.contract.InvoiceValidationFailStrategy;
import sa.abrahman.zaxeg.core.generate.port.in.payload.InvoiceGenerationPayload;
import sa.abrahman.zaxeg.core.shared.dto.FailableResult;

@Component
public class FailSafe implements InvoiceValidationFailStrategy {
    @Override
    public Map<String, String> execute(Collection<InvoiceRuleValidator> failables, InvoiceGenerationPayload data) {
        Map<String, String> result = new HashMap<>();
        for (InvoiceRuleValidator f : failables) {
            FailableResult<Entry<String, String>> r = f.run(data);
            if (!r.ok()) {
                result.put(r.payload().getKey(), r.payload().getValue());
            }
        }
        return result;
    }
}
