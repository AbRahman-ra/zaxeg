package sa.abrahman.zaxeg.core.service.validator;

import java.time.LocalDate;

import org.springframework.stereotype.Component;

import sa.abrahman.zaxeg.core.exception.InvoiceRuleViolationException;
import sa.abrahman.zaxeg.core.model.Invoice;

@Component("SIMPLIFIED_INVOICE_VALIDATOR")
public class SimplifiedInvoiceValidator implements InvoiceValidator {

    @Override
    public void validate(Invoice invoice) {
        // BR-KSA-25: Issue Date cannot be in the future
        if (invoice.getIssueDate().isAfter(LocalDate.now())) {
            throw new InvoiceRuleViolationException("BR-KSA-25: Invoice Issue Date cannot be in the future.");
        }

        // Buyer info is optional in simplified notes
        boolean buyerHasVat = invoice.getBuyer() != null && invoice.getBuyer().getVatNumber() != null;
        if (buyerHasVat) {
            String vat = invoice.getBuyer().getVatNumber();
            if (vat.length() != 15 || !vat.startsWith("3") || !vat.endsWith("3")) {
                throw new InvoiceRuleViolationException("BR-KSA-14: If provided, VAT number must be exactly 15 digits, starting and ending with '3'.");
            }
        }
    }
    
}
