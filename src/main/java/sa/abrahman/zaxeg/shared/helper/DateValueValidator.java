package sa.abrahman.zaxeg.shared.helper;

import java.time.LocalDate;
import java.util.function.Function;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Deprecated
public class DateValueValidator<E extends RuntimeException> {
    private final LocalDate subject;
    private final Function<String, E> exceptionFactory;

    @Deprecated public static <E extends RuntimeException> DateValueValidator<E> check(LocalDate subject,
            Function<String, E> exceptionFactory) {
        return new DateValueValidator<>(subject, exceptionFactory);
    }

    @Deprecated public DateValueValidator<E> notInFuture(String errMsg) {
        if (this.subject.isAfter(LocalDate.now())) {
            throw exceptionFactory.apply(errMsg);
        }
        return this;
    }

    @Deprecated public DateValueValidator<E> before(LocalDate future, String errMsg) {
        if (this.subject.isAfter(future)) throw exceptionFactory.apply(errMsg);
        return this;
    }

    @Deprecated public DateValueValidator<E> exists(String errMsg) {
        if (this.subject == null) {
            throw exceptionFactory.apply(errMsg);
        }
        return this;
    }
}
