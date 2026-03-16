package sa.abrahman.zaxeg.core.helper;

import java.util.Collection;
import java.util.function.Function;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CollectionValueValidator<E extends RuntimeException> {
    private final Collection<?> subject;
    private final Function<String, E> exceptionFactory;

    public static <E extends RuntimeException> CollectionValueValidator<E> check(Collection<?> subject,
            Function<String, E> exceptionFactory) {
        if (subject == null) throw new NullPointerException("Subject must be NonNull Collection");
        return new CollectionValueValidator<>(subject, exceptionFactory);
    }

    public CollectionValueValidator<E> hasAtleast(int length, String errMsg) {
        if (this.subject.size() < length) {
            throw exceptionFactory.apply(errMsg);
        }
        return this;
    }
}
