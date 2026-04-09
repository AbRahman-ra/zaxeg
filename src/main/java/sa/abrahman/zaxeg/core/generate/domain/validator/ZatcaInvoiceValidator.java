package sa.abrahman.zaxeg.core.generate.domain.validator;

import java.util.List;

import org.springframework.stereotype.Service;

import sa.abrahman.zaxeg.core.generate.domain.constant.ValidatorBeansRegistry;
import sa.abrahman.zaxeg.core.generate.port.in.payload.InvoiceGenerationPayload;
import sa.abrahman.zaxeg.core.generate.domain.contract.InvoiceRuleValidator;
import sa.abrahman.zaxeg.core.generate.domain.contract.InvoiceValidationFailStrategy;
import sa.abrahman.zaxeg.core.generate.domain.contract.InvoiceValidator;

@Service(ValidatorBeansRegistry.FULL_INVOICE_VALIDATOR)
public class ZatcaInvoiceValidator implements InvoiceValidator {
    private final List<InvoiceRuleValidator> validators;
    private final InvoiceValidationFailStrategy strategy;

    public ZatcaInvoiceValidator(List<InvoiceRuleValidator> validators, InvoiceValidationFailStrategy strategy) {
        this.validators = validators;
        this.strategy = strategy;
    }

    @Override
    public void validate(InvoiceGenerationPayload payload) {
        strategy.execute(validators, payload);
    }
}
