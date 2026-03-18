package sa.abrahman.zaxeg.core.helper;

import java.util.function.Function;
import java.util.function.Predicate;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ObjectValueValidator<C, E extends RuntimeException> {
    private final C subject;
    private final Function<String, E> exceptionFactory;

    public static <C, E extends RuntimeException> ObjectValueValidator<C, E> check(C subject,
            Function<String, E> exceptionFactory) {
        if (subject == null)
            throw new NullPointerException("Subject must be NonNull Collection");
        return new ObjectValueValidator<>(subject, exceptionFactory);
    }

    public ObjectValueValidator<C, E> exists(String errMsg) {
        if (subject == null)
            exceptionFactory.apply(errMsg);
        return this;
    }

    public ObjectValueValidator<C, E> matches(Predicate<Object> predicate, String errMsg) {
        if (!predicate.test(this))
            exceptionFactory.apply(errMsg);
        return this;
    }
}
