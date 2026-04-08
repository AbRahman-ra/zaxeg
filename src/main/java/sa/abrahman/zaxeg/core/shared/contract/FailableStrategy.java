package sa.abrahman.zaxeg.core.shared.contract;

public interface FailableStrategy<J, D> {
    void execute(J job, D data);
}
