package sa.abrahman.zaxeg.shared.contract;

/**
 * A generic interface representing a class can be mapped to another type {@code C} using additional data {@code D}
 *
 * @param <C> the target type
 * @param <D> additional data needed to map to {@Code C}, if no data is needed, use {@ling Void}
 */
public interface Mapable<C, D> {
    C mapped(D data);

    default C mapped() {
        return mapped(null);
    }
}
