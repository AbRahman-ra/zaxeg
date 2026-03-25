package sa.abrahman.zaxeg.core.service.generator;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import sa.abrahman.zaxeg.core.port.in.InvoiceGenerator;
import sa.abrahman.zaxeg.core.port.in.payload.InvoiceGenerationPayload;

@Service
@Profile("dev")
public class DummyInvoiceGenerator implements InvoiceGenerator {

    @Override
    public String handle(InvoiceGenerationPayload payload) {
        String template = """
                <Invoice>
                    <ID>%s</ID>
                    <Buyer>%s</Buyer>
                    <Total>%s</Total>
                </Invoice>
                """;
        String buyer = payload.getParties().getBuyer().getName();
        String totalAmount = "0.00";

        return template.formatted(payload.getMetadata().getInvoiceUuid(), buyer, totalAmount);
    }

}
