package sa.abrahman.zaxeg.core.model.invoice.metadata;

import java.util.function.Function;

import lombok.Builder;
import lombok.Getter;
import sa.abrahman.zaxeg.core.model.invoice.predefined.InvoiceSubtype;

@Getter
@Builder
public class InvoiceTypeTransactions {
    private InvoiceSubtype subtype;

    @Builder.Default
    private boolean thirdParty = false;

    @Builder.Default
    private boolean nominal = false;

    @Builder.Default
    private boolean exports = false;

    @Builder.Default
    private boolean summary = false;

    @Builder.Default
    private boolean selfBilled = false;

    /**
     * Generating invoice transaction code based on BR-KSA-06 rule
     *
     * @return
     */
    public String code() {
        Function<Boolean, String> toIntString = b -> Boolean.TRUE.equals(b) ? "1" : "0";

        return subtype.code()
                + toIntString.apply(thirdParty)
                + toIntString.apply(nominal)
                + toIntString.apply(exports)
                + toIntString.apply(summary)
                + toIntString.apply(selfBilled);
    }
}
