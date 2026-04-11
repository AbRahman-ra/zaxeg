package sa.abrahman.zaxeg.core.generate.domain.model.invoice.metadata;

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
