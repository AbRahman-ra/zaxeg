package sa.abrahman.zaxeg.core.helper;

import java.util.function.Function;
import java.util.function.Predicate;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ObjectValueValidator<C, E extends RuntimeException> {
    private final C subject;
    private final Function<String, E> exceptionFactory;

    /**
     * Entry point for the fluent chain.
     *
     * @param <C>              Instance Type
     * @param <E>              Unchecked Exception
     *
     * @param subject          The object to validate
     * @param exceptionFactory A method reference to the exception you want to throw
     *                         (e.g., MyCustomException::new)
     */
    public static <C, E extends RuntimeException> ObjectValueValidator<C, E> check(C subject,
            Function<String, E> exceptionFactory) {
        return new ObjectValueValidator<>(subject, exceptionFactory);
    }

    public ObjectValueValidator<C, E> exists(String errMsg) {
        if (subject == null)
            throw exceptionFactory.apply(errMsg);
        return this;
    }

    /**
     * Throws {@code errMsg} if the predicate yields to false
     * @param predicate
     * @param errMsg
     * @return
     */
    public ObjectValueValidator<C, E> matches(Predicate<C> predicate, String errMsg) {
        if (!predicate.test(this.subject))
            throw exceptionFactory.apply(errMsg);
        return this;
    }
}
