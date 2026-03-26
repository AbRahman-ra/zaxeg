package sa.abrahman.zaxeg.core.helper;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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
        if (subject == null || subject.size() < length) {
            throw exceptionFactory.apply(errMsg);
        }
        return this;
    }

    public CollectionValueValidator<C, E> notEmpty(String errMsg) {
        return hasAtleast(1, errMsg);
    }

    /**
     * Check if all non-null elements match a predicate
     *
     * @implNote this method filters out any null values in the collection before validating the predicate
     * @param p
     * @param errMsg
     * @return
     */
    public CollectionValueValidator<C, E> allMatch(Predicate<C> p, String errMsg) {
        boolean notNullAndSatisfies = Optional.ofNullable(this.subject).orElse(List.of()).stream().filter(Objects::nonNull).allMatch(p);
        if (!notNullAndSatisfies) {
            throw exceptionFactory.apply(errMsg);
        }
        return this;
    }

    public CollectionValueValidator<C, E> anyMatch(Predicate<C> p, String errMsg) {
        boolean notNullAndSatisfies = Optional.ofNullable(this.subject).orElse(List.of()).stream().filter(Objects::nonNull).anyMatch(p);
        if (!notNullAndSatisfies) {
            throw exceptionFactory.apply(errMsg);
        }
        return this;
    }
}
