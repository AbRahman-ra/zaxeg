package sa.abrahman.zaxeg.core.service.validator;

import java.time.LocalDate;

import org.springframework.stereotype.Component;

import sa.abrahman.zaxeg.core.exception.InvoiceRuleViolationException;
import sa.abrahman.zaxeg.core.model.Invoice;

@Component("STANDARD_INVOICE_VALIDATOR")
public class StandardInvoiceValidator implements InvoiceValidator {

    @Override
    public void validate(Invoice invoice) {
        // BR-KSA-25: Issue Date cannot be in the future
        if (invoice.getIssueDate().isAfter(LocalDate.now())) {
            throw new InvoiceRuleViolationException("BR-KSA-25: Invoice Issue Date cannot be in the future.");
        }

        // BR-KSA-14: Standard invoices require a Buyer
        if (invoice.getBuyer() == null) {
            throw new InvoiceRuleViolationException("BR-KSA-14: Standard Invoices (0100000) must include Buyer Information.");
        }

        // BR-KSA-08: Buyer Address is mandatory for Standard Invoices
        if (invoice.getBuyer().getAddress() == null) {
            throw new InvoiceRuleViolationException("BR-KSA-08: Standard Invoices require a complete Buyer Address.");
        }

        // KSA rules strictly require VAT number or CRN for B2B buyers
        boolean hasVat = invoice.getBuyer().getVatNumber() != null && !invoice.getBuyer().getVatNumber().isBlank();
        boolean hasCrn = invoice.getBuyer().getCommercialRegistrationNumber() != null && !invoice.getBuyer().getCommercialRegistrationNumber().isBlank();
        
        if (!hasVat && !hasCrn) {
            throw new InvoiceRuleViolationException("Standard Invoices require either a Buyer VAT Number or a Commercial Registration Number (CRN).");
        }
    }

}
