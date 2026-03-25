package sa.abrahman.zaxeg.core.helper;

import java.util.Set;
import java.util.function.Function;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class StringValueValidator<E extends RuntimeException> {
    private final String subject;
    private final Function<String, E> exceptionFactory;

    /**
     * Entry point for the fluent chain.
     *
     * @param subject          The string to validate
     * @param exceptionFactory A method reference to the exception you want to throw
     *                         (e.g., MyCustomException::new)
     * @param <E>              Unchecked Exception
     */
    public static <E extends RuntimeException> StringValueValidator<E> check(String subject,
            Function<String, E> exceptionFactory) {
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

    public StringValueValidator<E> exists(String errMsg) {
        if (this.subject == null || this.subject.isBlank()) {
            throw exceptionFactory.apply(errMsg);
        }
        return this;
    }

    public StringValueValidator<E> numeric(String errMsg) {
        Set<Character> numbers = Set.of('0', '1', '2', '3', '4', '5', '6', '7', '8', '9');
        for (int i = 0; i < this.subject.length(); i++) {
            if (!numbers.contains(this.subject.charAt(i))) {
                throw exceptionFactory.apply(errMsg);
            }
        }
        return this;
    }
}
