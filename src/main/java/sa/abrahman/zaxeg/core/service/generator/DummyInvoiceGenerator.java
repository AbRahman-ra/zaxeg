package sa.abrahman.zaxeg.core.service.generator;

import java.util.Optional;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import sa.abrahman.zaxeg.core.port.in.InvoiceGenerationPayload;
import sa.abrahman.zaxeg.core.port.in.InvoiceGenerator;

@Service
@Profile("dev")
public class DummyInvoiceGenerator implements InvoiceGenerator {

    @Override
    public String handle(InvoiceGenerationPayload command) {
        String template = """
                <Invoice>
                    <ID>%s</ID>
                    <Buyer>%s</Buyer>
                    <Total>%s</Total>
                </Invoice>
                """;
        String buyer = Optional.ofNullable(command.getBuyer())
                .map(InvoiceGenerationPayload.PartyPayload::getRegistrationName)
                .orElse("N/A");
        String totalAmount = "0.00";

        return template.formatted(command.getInvoiceNumber(), buyer, totalAmount);
    }

}
