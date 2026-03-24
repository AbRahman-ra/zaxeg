package sa.abrahman.zaxeg.core.model.invoice.wrapper;

import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import sa.abrahman.zaxeg.core.model.invoice.line.InvoiceLine;

@Getter
@RequiredArgsConstructor
public class InvoiceLines {
    private final List<InvoiceLine> lines;

    public static InvoiceLines of(List<InvoiceLine> lines) {
        return new InvoiceLines(lines);
    }
}
