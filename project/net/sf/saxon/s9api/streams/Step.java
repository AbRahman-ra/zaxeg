/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.s9api.streams;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.sf.saxon.s9api.XdmItem;

public abstract class Step<T extends XdmItem>
implements Function<XdmItem, Stream<? extends T>> {
    public Step<T> where(final Predicate<? super T> predicate) {
        final Step base = this;
        return new Step<T>(){

            @Override
            public Stream<? extends T> apply(XdmItem item) {
                return ((Stream)base.apply(item)).filter(predicate);
            }
        };
    }

    public Step<T> cat(final Step<T> other) {
        final Step base = this;
        return new Step<T>(){

            @Override
            public Stream<T> apply(XdmItem item) {
                return Stream.concat((Stream)base.apply(item), (Stream)other.apply(item));
            }
        };
    }

    public Step<T> first() {
        final Step base = this;
        return new Step<T>(){

            @Override
            public Stream<? extends T> apply(XdmItem item) {
                return ((Stream)base.apply(item)).limit(1L);
            }
        };
    }

    public Step<T> last() {
        final Step base = this;
        return new Step<T>(){

            @Override
            public Stream<? extends T> apply(XdmItem item) {
                return ((Stream)base.apply(item)).reduce((first, second) -> second).map(Stream::of).orElseGet(Stream::empty);
            }
        };
    }

    public Step<T> at(final long index) {
        final Step base = this;
        return new Step<T>(){

            @Override
            public Stream<? extends T> apply(XdmItem item) {
                return ((Stream)base.apply(item)).skip(index).limit(1L);
            }
        };
    }

    public <U extends XdmItem> Step<U> then(final Step<U> next) {
        final Step me = this;
        return new Step<U>(){

            @Override
            public Stream<? extends U> apply(XdmItem item) {
                return ((Stream)me.apply(item)).flatMap(next);
            }
        };
    }
}

