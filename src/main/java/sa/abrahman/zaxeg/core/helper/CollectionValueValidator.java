package sa.abrahman.zaxeg.core.helper;

import java.util.Collection;
import java.util.function.Function;
import java.util.function.Predicate;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CollectionValueValidator<C, E extends RuntimeException> {
    private final Collection<C> subject;
    private final Function<String, E> exceptionFactory;

    public static <C, E extends RuntimeException> CollectionValueValidator<C, E> check(Collection<C> subject,
            Function<String, E> exceptionFactory) {
        return new CollectionValueValidator<>(subject, exceptionFactory);
    }

    public CollectionValueValidator<C, E> exists(String errMsg) {
        if (subject == null)
            throw exceptionFactory.apply(errMsg);
        return this;
    }

    public CollectionValueValidator<C, E> hasAtleast(int length, String errMsg) {
        if (subject.size() < length) {
            throw exceptionFactory.apply(errMsg);
        }
        return this;
    }

    public CollectionValueValidator<C, E> allMatch(Predicate<C> p, String errMsg) {
        if (subject.stream().filter(p).count() != subject.size()) {
            throw exceptionFactory.apply(errMsg);
        }
        return this;
    }
}
