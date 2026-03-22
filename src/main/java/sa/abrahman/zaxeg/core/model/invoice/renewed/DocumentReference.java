package sa.abrahman.zaxeg.core.model.invoice.renewed;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class DocumentReference {
    private final String id;

    public static DocumentReference of(String id) {
        return new DocumentReference(id);
    }
}
