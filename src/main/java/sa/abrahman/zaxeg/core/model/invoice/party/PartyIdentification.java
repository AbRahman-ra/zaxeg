package sa.abrahman.zaxeg.core.model.invoice.party;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import sa.abrahman.zaxeg.core.model.invoice.predefined.Scheme;

@Getter
@RequiredArgsConstructor
public class PartyIdentification {
    private final Scheme schemeId;
    private final String value;

    public static PartyIdentification of(Scheme schemeId, String value) {
        return new PartyIdentification(schemeId, value);
    }
}
