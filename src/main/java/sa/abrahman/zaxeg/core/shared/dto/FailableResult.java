package sa.abrahman.zaxeg.core.shared.dto;
public record FailableResult<D>(boolean ok, D payload) {
    public static <D> FailableResult<D> of(boolean ok, D payload) {
        return new FailableResult<>(ok, payload);
    }
}
