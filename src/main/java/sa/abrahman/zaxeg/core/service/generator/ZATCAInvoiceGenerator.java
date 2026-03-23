package sa.abrahman.zaxeg.core.service.generator;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import sa.abrahman.zaxeg.core.model.invoice.old.Invoice;
import sa.abrahman.zaxeg.core.port.in.InvoiceGenerationPayload;
import sa.abrahman.zaxeg.core.port.in.InvoiceGenerationPayload.FinancialsPayload;
import sa.abrahman.zaxeg.core.port.in.InvoiceGenerator;
import sa.abrahman.zaxeg.core.port.out.InvoiceFormatter;
import sa.abrahman.zaxeg.core.service.contract.InvoiceValidator;
import sa.abrahman.zaxeg.core.service.processor.FinancialsPayloadCalculator;
import sa.abrahman.zaxeg.core.service.validator.InvoiceValidatorBeanNameResolver;

@Service
public class ZATCAInvoiceGenerator implements InvoiceGenerator {
    private final InvoiceValidator validator;
    private final InvoiceFormatter formatter;
    private final FinancialsPayloadCalculator calculator;

    public ZATCAInvoiceGenerator(
            @Qualifier(InvoiceValidatorBeanNameResolver.FULL_INVOICE_VALIDATOR) InvoiceValidator validator,
            InvoiceFormatter formatter,
            FinancialsPayloadCalculator calculator) {
        this.formatter = formatter;
        this.validator = validator;
        this.calculator = calculator;
    }

    @Override
    public String handle(InvoiceGenerationPayload payload) {
        if (payload.getFinancials() == null) {
            FinancialsPayload financials = calculator.calculate(payload.getLines(),
                    payload.getDocumentAllowancesAndOrCharges(),
                    payload.getPrepaidAmount());
            payload.setFinancials(financials);
        }

        for (InvoiceGenerationPayload.LinePayload l : payload.getLines()) {
            if (l.getNetAmount() == null){
                BigDecimal netAmount = calculator.calculateNetAmountForLine(l);
                l.setNetAmount(netAmount);
            }
        }

        validator.validate(payload);
        Invoice invoice = Invoice.from(payload);
        return formatter.format(invoice);
    }
}
