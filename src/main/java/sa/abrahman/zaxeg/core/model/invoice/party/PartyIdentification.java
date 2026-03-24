package sa.abrahman.zaxeg.core.model.invoice.party;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import sa.abrahman.zaxeg.core.model.invoice.predefined.Scheme;

@Getter
@RequiredArgsConstructor
public class PartyIdentification {
    private Scheme schemeId;
    private String value;
}
