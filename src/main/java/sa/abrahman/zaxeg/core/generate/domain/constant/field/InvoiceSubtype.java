package sa.abrahman.zaxeg.core.generate.domain.constant.field;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum InvoiceSubtype {
    STANDARD("01"), SIMPLIFIED("02");

    private final String code;

    public String code() {
        return code;
    }
}
