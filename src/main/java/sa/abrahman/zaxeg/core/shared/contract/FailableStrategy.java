package sa.abrahman.zaxeg.core.shared.contract;

/**
 * @param <D> the data needed by the failable
 * @param <F> the failable(s) to apply the strategy based on its result (example: {@code List<FailableJob<?,?>>})
 * @param <R> the return type of the strategy
 */
public interface FailableStrategy<D, F, R> {
    R execute(F failables, D data);
}
