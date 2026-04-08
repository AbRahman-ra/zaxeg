package sa.abrahman.zaxeg.core.generate.domain.validator;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import sa.abrahman.zaxeg.core.generate.domain.constant.ValidatorBeansRegistry;
import sa.abrahman.zaxeg.core.generate.port.in.payload.InvoiceGenerationPayload;
import sa.abrahman.zaxeg.core.generate.domain.contract.InvoiceValidator;

@Service(ValidatorBeansRegistry.FULL_INVOICE_VALIDATOR)
public class ZatcaInvoiceValidator implements InvoiceValidator {
    private final InvoiceValidator metadata;
    private final InvoiceValidator parties;
    private final InvoiceValidator lines;
    private final InvoiceValidator checkout;
    private final InvoiceValidator aggregates;

    public ZatcaInvoiceValidator(@Qualifier(ValidatorBeansRegistry.METADATA_VALIDATOR) InvoiceValidator metadata,
            @Qualifier(ValidatorBeansRegistry.PARTIES_VALIDATOR) InvoiceValidator parties,
            @Qualifier(ValidatorBeansRegistry.LINES_VALIDATOR) InvoiceValidator lines,
            @Qualifier(ValidatorBeansRegistry.CHECKOUT_VALIDATOR) InvoiceValidator checkout,
            @Qualifier(ValidatorBeansRegistry.AGGREGATES_VALIDATOR) InvoiceValidator aggregates) {

        this.metadata = metadata;
        this.parties = parties;
        this.lines = lines;
        this.checkout = checkout;
        this.aggregates = aggregates;
    }

    @Override
    public void validate(InvoiceGenerationPayload payload) {
        metadata.validate(payload);
        parties.validate(payload);
        lines.validate(payload);
        checkout.validate(payload);
        aggregates.validate(payload);
    }
}
