package sa.abrahman.zaxeg.core.service.generator;

import java.util.Optional;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import sa.abrahman.zaxeg.core.model.BusinessParty;
import sa.abrahman.zaxeg.core.model.Invoice;
import sa.abrahman.zaxeg.core.port.in.InvoiceGenerator;

@Service
@Profile("dev")
public class DummyInvoiceGenerator implements InvoiceGenerator {

    @Override
    public String toXML(Invoice invoice) {
        String template = """
                <Invoice>
                    <ID>%s</ID>
                    <Buyer>%s</Buyer>
                    <Total>%s</Total>
                </Invoice>
                """;
        String buyer = Optional.ofNullable(invoice.getBuyer())
                .map(BusinessParty::getRegistrationName)
                .orElse("N/A");
        String totalAmount = Optional.ofNullable(invoice.getFinancials())
                .map(f -> f.getTotalAmountInclusive().toString())
                .orElse("0.00");

        return template.formatted(invoice.getInvoiceNumber(), buyer, totalAmount);
    }

}
