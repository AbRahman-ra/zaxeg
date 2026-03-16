package sa.abrahman.zaxeg.core.helper;

import java.time.LocalDate;
import java.util.function.Function;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DateValueValidator<E extends RuntimeException> {
    private final LocalDate subject;
    private final Function<String, E> exceptionFactory;

    public static <E extends RuntimeException> DateValueValidator<E> check(LocalDate subject, Function<String, E> exceptionFactory) {
        if (subject == null) throw new NullPointerException("Subject must be NonNull Date");
        return new DateValueValidator<>(subject, exceptionFactory);
    }

    public DateValueValidator<E> notInFuture(String errMsg) {
        if (this.subject.isAfter(LocalDate.now())) {
            throw exceptionFactory.apply(errMsg);
        }
        return this;
    }

    public DateValueValidator<E> exists(String errMsg) {
        if (this.subject == null) {
            throw exceptionFactory.apply(errMsg);
        }
        return this;
    }
}
