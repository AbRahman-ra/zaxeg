package sa.abrahman.zaxeg.core.service.generator;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import sa.abrahman.zaxeg.core.model.invoice.Invoice;
import sa.abrahman.zaxeg.core.port.in.InvoiceGenerationCommand;
import sa.abrahman.zaxeg.core.port.in.InvoiceGenerationCommand.FinancialsCommand;
import sa.abrahman.zaxeg.core.port.in.InvoiceGenerator;
import sa.abrahman.zaxeg.core.port.out.InvoiceFormatter;
import sa.abrahman.zaxeg.core.service.processor.FinancialsCommandCalculator;
import sa.abrahman.zaxeg.core.service.validator.InvoiceValidator;
import sa.abrahman.zaxeg.core.service.validator.InvoiceValidatorBeanNameResolver;

@Service
public class ZATCAInvoiceGenerator implements InvoiceGenerator {
    private final InvoiceValidator validator;
    private final InvoiceFormatter formatter;
    private final FinancialsCommandCalculator calculator;

    public ZATCAInvoiceGenerator(
        @Qualifier(InvoiceValidatorBeanNameResolver.FULL_INVOICE_VALIDATOR) InvoiceValidator validator,
        InvoiceFormatter formatter,
        FinancialsCommandCalculator calculator
    ) {
        this.formatter = formatter;
        this.validator = validator;
        this.calculator = calculator;
    }

    @Override
    public String handle(InvoiceGenerationCommand payload) {
        if (payload.getFinancials() == null) {
            FinancialsCommand financialsCommand = calculator.calculate(payload.getLines(),
                    payload.getDocumentAllowancesAndOrCharges(),
                    payload.getPrepaidAmount());
            payload.setFinancials(financialsCommand);
        }

        validator.validate(payload);
        Invoice invoice = Invoice.from(payload);
        return formatter.format(invoice);
    }
}
