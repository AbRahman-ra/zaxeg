package sa.abrahman.zaxeg.infrastructure.in.contract;

/**
 * Intentionally created in the infrastructure, the purpose of this interface to
 * handle the possibility of changing the request body overtime, since it's
 * concerned with the request dto, it's kept in the infrastructure
 */
public interface Payloadable<C, D> {
    C toPayload(D additionalData);

    default C toPayload() {
        return toPayload(null);
    }
}
