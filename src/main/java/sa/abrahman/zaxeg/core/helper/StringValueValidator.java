package sa.abrahman.zaxeg.core.helper;

import java.util.function.Function;

public class StringValueValidator<E extends RuntimeException> {
    private final String subject;
    private final Function<String, E> exceptionFactory;

    private StringValueValidator(String subject, Function<String, E> exceptionFactory) {
        this.subject = subject;
        this.exceptionFactory = exceptionFactory;
    }

    /**
     * Entry point for the fluent chain.
     * 
     * @param subject          The string to validate
     * @param exceptionFactory A method reference to the exception you want to throw
     *                         (e.g., MyCustomException::new)
     */
    public static <E extends RuntimeException> StringValueValidator<E> check(String subject,
            Function<String, E> exceptionFactory) {
        if (subject == null) throw new NullPointerException("Subject must be NonNull String");
        return new StringValueValidator<>(subject, exceptionFactory);
    }

    public StringValueValidator<E> hasLength(int length, String errMsg) {
        if (this.subject.length() != length) {
            throw exceptionFactory.apply(errMsg);
        }
        return this;
    }

    public StringValueValidator<E> startsWith(String prefix, String errMsg) {
        if (!this.subject.startsWith(prefix)) {
            throw exceptionFactory.apply(errMsg);
        }
        return this;
    }

    public StringValueValidator<E> endsWith(String suffix, String errMsg) {
        if (!this.subject.endsWith(suffix)) {
            throw exceptionFactory.apply(errMsg);
        }
        return this;
    }

    public StringValueValidator<E> startsAndEndsWith(String substring, String errMsg) {
        if (!this.subject.startsWith(substring) || !this.subject.endsWith(substring)) {
            throw exceptionFactory.apply(errMsg);
        }
        return this;
    }
}
