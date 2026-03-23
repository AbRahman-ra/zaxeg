package sa.abrahman.zaxeg.core.model.invoice.party;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class PartyIdentification {
    private Scheme schemeId;
    private String value;
}
