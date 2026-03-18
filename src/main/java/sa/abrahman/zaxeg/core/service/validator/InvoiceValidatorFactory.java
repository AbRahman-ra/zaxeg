package sa.abrahman.zaxeg.core.service.validator;

import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import sa.abrahman.zaxeg.core.model.invoice.meta.InvoiceSubtype;

@Component
@RequiredArgsConstructor
public class InvoiceValidatorFactory {
    private final Map<String, InvoiceValidator> map;
    private static final String suffix = "_INVOICE_VALIDATOR";

    public InvoiceValidator getValidatorInstanceFor(InvoiceSubtype subtype) {
        String bean = subtype.toString() + suffix;
        return Optional.ofNullable(map.get(bean))
                .orElseThrow(() -> new IllegalArgumentException(
                        "No registered invoice validator bean with the name " + bean));
    }
}
