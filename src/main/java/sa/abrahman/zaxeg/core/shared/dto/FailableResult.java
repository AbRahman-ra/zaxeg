package sa.abrahman.zaxeg.core.shared.dto;
public record FailableResult<D>(boolean ok, D data) {
    public static <D> FailableResult<D> of(boolean ok, D data) {
        return new FailableResult<>(ok, data);
    }
}
