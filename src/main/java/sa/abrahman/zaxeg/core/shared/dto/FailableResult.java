package sa.abrahman.zaxeg.core.shared.dto;
public record FailableResult<D>(boolean ok, D payload) {
    public static <D> FailableResult<D> of(boolean ok, D payload) {
        return new FailableResult<>(ok, payload);
    }

    public static <D> FailableResult<D> failed(D payload) {
        return of(false, payload);
    }

    public static <D> FailableResult<D> okay() {
        return of(true, null);
    }
}
