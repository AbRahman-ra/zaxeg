package sa.abrahman.zaxeg.core.generate.domain.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import sa.abrahman.zaxeg.core.generate.domain.factory.InvoiceFactory;
import sa.abrahman.zaxeg.core.generate.domain.model.invoice.Invoice;
import sa.abrahman.zaxeg.core.generate.port.in.InvoiceGenerator;
import sa.abrahman.zaxeg.core.generate.port.in.payload.InvoiceGenerationPayload;
import sa.abrahman.zaxeg.core.generate.port.out.InvoiceFormatter;
import sa.abrahman.zaxeg.shared.constant.StatusCode;
import sa.abrahman.zaxeg.shared.dto.ApiResponse;
import sa.abrahman.zaxeg.core.generate.domain.contract.InvoiceRuleValidator;
import sa.abrahman.zaxeg.core.generate.domain.contract.InvoiceValidationFailStrategy;

@Service
public class GenerateInvoiceService implements InvoiceGenerator {
    private final Collection<InvoiceRuleValidator> validationRules;
    private final InvoiceValidationFailStrategy strategy;
    private final InvoiceFormatter formatter;

    public GenerateInvoiceService(List<InvoiceRuleValidator> validationRules,
            @Qualifier("failSafe") InvoiceValidationFailStrategy strategy, InvoiceFormatter formatter) {
        this.validationRules = validationRules;
        this.strategy = strategy;
        this.formatter = formatter;
    }

    @Override
    public ApiResponse<?> handle(InvoiceGenerationPayload payload) {
        Map<String, String> violations = strategy.execute(validationRules, payload);
        if (!violations.isEmpty()) {
            return ApiResponse.from(StatusCode.UNPROCESSABLE_CONTENT.code(), "Invalid Invoice", violations);
        }

        Invoice invoice = InvoiceFactory.from(payload);
        return ApiResponse.from(StatusCode.CREATED.code(), "Invoice Generated Successfully", formatter.format(invoice));
    }
}
