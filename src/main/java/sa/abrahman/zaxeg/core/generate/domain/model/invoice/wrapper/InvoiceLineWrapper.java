package sa.abrahman.zaxeg.core.generate.domain.model.invoice.wrapper;

import java.util.List;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullMarked;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import sa.abrahman.zaxeg.core.generate.domain.model.invoice.line.InvoiceLine;

@Getter
@RequiredArgsConstructor
@NullMarked
public class InvoiceLineWrapper {
    private final List<InvoiceLine> lines;

    public static InvoiceLineWrapper of(@NonNull List<InvoiceLine> lines) {
        return new InvoiceLineWrapper(lines);
    }
}
