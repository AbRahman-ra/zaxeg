package sa.abrahman.zaxeg.shared.contract;

public interface Port<I, O> {
    O handle(I payload);
}
