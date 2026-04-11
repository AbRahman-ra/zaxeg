package sa.abrahman.zaxeg.core.shared.contract;

import org.jspecify.annotations.NonNull;

import sa.abrahman.zaxeg.core.shared.dto.FailableResult;

/**
 * @param <D> the input data needed by the job
 * @param <R> the response returned by the job
 */
public interface FailableJob<D, R> {
    @NonNull FailableResult<R> run(D data);
}
