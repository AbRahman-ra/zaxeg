package sa.abrahman.zaxeg.core.shared.contract;

import sa.abrahman.zaxeg.core.shared.dto.FailableResult;

public interface FailableJob<D> {
    <R  extends Object> FailableResult<R> run(D data);
}
