package sa.abrahman.zaxeg.core.shared.dto;
public record FailableResult<D>(boolean ok, D payload) {
    public static <D> FailableResult<D> of(boolean ok, D payload) {
        return new FailableResult<>(ok, payload);
    }

    public static <D> FailableResult<D> failed(D payload) {
        return new FailableResult<>(false, payload);
    }

    public static <D> FailableResult<D> okay() {
        return new FailableResult<>(true, null);
    }
}
