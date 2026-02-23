/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.s9api.streams;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Spliterator;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmValue;
import net.sf.saxon.s9api.streams.Step;
import net.sf.saxon.s9api.streams.XdmCollectors;

public class XdmStream<T extends XdmItem>
implements Stream<T> {
    Stream<T> base;

    public XdmStream(Stream<T> base) {
        this.base = base;
    }

    public XdmStream(Optional<T> input) {
        this.base = input.map(Stream::of).orElseGet(Stream::empty);
    }

    @Override
    public XdmStream<T> filter(Predicate<? super T> predicate) {
        return new XdmStream<T>(this.base.filter(predicate));
    }

    @Override
    public <R> Stream<R> map(Function<? super T, ? extends R> mapper) {
        return this.base.map(mapper);
    }

    @Override
    public IntStream mapToInt(ToIntFunction<? super T> mapper) {
        return this.base.mapToInt(mapper);
    }

    @Override
    public LongStream mapToLong(ToLongFunction<? super T> mapper) {
        return this.base.mapToLong(mapper);
    }

    @Override
    public DoubleStream mapToDouble(ToDoubleFunction<? super T> mapper) {
        return this.base.mapToDouble(mapper);
    }

    @Override
    public <R> Stream<R> flatMap(Function<? super T, ? extends Stream<? extends R>> mapper) {
        return this.base.flatMap(mapper);
    }

    public <U extends XdmItem> XdmStream<U> flatMapToXdm(Step<U> mapper) {
        return new XdmStream(this.base.flatMap(mapper));
    }

    @Override
    public IntStream flatMapToInt(Function<? super T, ? extends IntStream> mapper) {
        return this.base.flatMapToInt(mapper);
    }

    @Override
    public LongStream flatMapToLong(Function<? super T, ? extends LongStream> mapper) {
        return this.base.flatMapToLong(mapper);
    }

    @Override
    public DoubleStream flatMapToDouble(Function<? super T, ? extends DoubleStream> mapper) {
        return this.base.flatMapToDouble(mapper);
    }

    @Override
    public XdmStream<T> distinct() {
        return new XdmStream<T>(this.base.distinct());
    }

    @Override
    public XdmStream<T> sorted() {
        return new XdmStream<T>(this.base.sorted());
    }

    @Override
    public XdmStream<T> sorted(Comparator<? super T> comparator) {
        return new XdmStream<T>(this.base.sorted(comparator));
    }

    @Override
    public XdmStream<T> peek(Consumer<? super T> action) {
        return new XdmStream<T>(this.base.peek(action));
    }

    @Override
    public XdmStream<T> limit(long maxSize) {
        return new XdmStream<T>(this.base.limit(maxSize));
    }

    @Override
    public XdmStream<T> skip(long n) {
        return new XdmStream<T>(this.base.skip(n));
    }

    @Override
    public void forEach(Consumer<? super T> action) {
        this.base.forEach(action);
    }

    @Override
    public void forEachOrdered(Consumer<? super T> action) {
        this.base.forEachOrdered(action);
    }

    @Override
    public Object[] toArray() {
        return this.base.toArray();
    }

    @Override
    public <A> A[] toArray(IntFunction<A[]> generator) {
        return this.base.toArray(generator);
    }

    @Override
    public T reduce(T identity, BinaryOperator<T> accumulator) {
        return (T)((XdmItem)this.base.reduce(identity, accumulator));
    }

    @Override
    public Optional<T> reduce(BinaryOperator<T> accumulator) {
        return this.base.reduce(accumulator);
    }

    @Override
    public <U> U reduce(U identity, BiFunction<U, ? super T, U> accumulator, BinaryOperator<U> combiner) {
        return this.base.reduce(identity, accumulator, combiner);
    }

    @Override
    public <R> R collect(Supplier<R> supplier, BiConsumer<R, ? super T> accumulator, BiConsumer<R, R> combiner) {
        return this.base.collect(supplier, accumulator, combiner);
    }

    @Override
    public <R, A> R collect(Collector<? super T, A, R> collector) {
        return this.base.collect(collector);
    }

    @Override
    public Optional<T> min(Comparator<? super T> comparator) {
        return this.base.min(comparator);
    }

    @Override
    public Optional<T> max(Comparator<? super T> comparator) {
        return this.base.max(comparator);
    }

    @Override
    public long count() {
        return this.base.count();
    }

    @Override
    public boolean anyMatch(Predicate<? super T> predicate) {
        return this.base.anyMatch(predicate);
    }

    @Override
    public boolean allMatch(Predicate<? super T> predicate) {
        return this.base.allMatch(predicate);
    }

    @Override
    public boolean noneMatch(Predicate<? super T> predicate) {
        return this.base.noneMatch(predicate);
    }

    @Override
    public Optional<T> findFirst() {
        return this.base.findFirst();
    }

    @Override
    public Optional<T> findAny() {
        return this.base.findAny();
    }

    @Override
    public Iterator<T> iterator() {
        return this.base.iterator();
    }

    @Override
    public Spliterator<T> spliterator() {
        return this.base.spliterator();
    }

    @Override
    public boolean isParallel() {
        return this.base.isParallel();
    }

    @Override
    public Stream<T> sequential() {
        return new XdmStream<T>((Stream)this.base.sequential());
    }

    @Override
    public Stream<T> parallel() {
        return new XdmStream<T>((Stream)this.base.parallel());
    }

    @Override
    public Stream<T> unordered() {
        return new XdmStream<T>((Stream)this.base.unordered());
    }

    @Override
    public Stream<T> onClose(Runnable closeHandler) {
        return new XdmStream<T>((Stream)this.base.onClose(closeHandler));
    }

    @Override
    public void close() {
        this.base.close();
    }

    public XdmValue asXdmValue() {
        return (XdmValue)this.base.collect(XdmCollectors.asXdmValue());
    }

    public List<T> asList() {
        return this.base.collect(Collectors.toList());
    }

    public List<XdmNode> asListOfNodes() {
        return (List)this.base.collect(XdmCollectors.asListOfNodes());
    }

    public Optional<XdmNode> asOptionalNode() {
        return (Optional)this.base.collect(XdmCollectors.asOptionalNode());
    }

    public XdmNode asNode() {
        return (XdmNode)this.base.collect(XdmCollectors.asNode());
    }

    public List<XdmAtomicValue> asListOfAtomic() {
        return (List)this.base.collect(XdmCollectors.asListOfAtomic());
    }

    public Optional<XdmAtomicValue> asOptionalAtomic() {
        return (Optional)this.base.collect(XdmCollectors.asOptionalAtomic());
    }

    public XdmAtomicValue asAtomic() {
        return (XdmAtomicValue)this.base.collect(XdmCollectors.asAtomic());
    }

    public Optional<String> asOptionalString() {
        return (Optional)this.base.collect(XdmCollectors.asOptionalString());
    }

    public String asString() {
        return (String)this.base.collect(XdmCollectors.asString());
    }

    public XdmStream<T> first() {
        Optional<T> result = this.base.findFirst();
        return new XdmStream<T>(result);
    }

    public boolean exists() {
        Optional<T> result = this.base.findFirst();
        return result.isPresent();
    }

    public XdmStream<T> last() {
        Optional<T> result = this.base.reduce((first, second) -> second);
        return new XdmStream<T>(result);
    }

    public Optional<T> at(int position) {
        return this.base.skip(position).findFirst();
    }

    public XdmStream<T> subStream(int start, int end) {
        if (start < 0) {
            start = 0;
        }
        if (end <= start) {
            return new XdmStream(Stream.empty());
        }
        return new XdmStream<T>(this.base.skip(start).limit(end - start));
    }

    public XdmStream<T> untilFirstInclusive(Predicate<? super XdmItem> predicate) {
        Stream<XdmItem> stoppable = this.base.peek((? super T item) -> {
            if (predicate.test((XdmItem)item)) {
                this.base.close();
            }
        });
        return new XdmStream<XdmItem>(stoppable);
    }

    public XdmStream<T> untilFirstExclusive(Predicate<? super XdmItem> predicate) {
        Stream<XdmItem> stoppable = this.base.peek((? super T item) -> {
            if (predicate.test((XdmItem)item)) {
                this.base.close();
            }
        });
        return new XdmStream<XdmItem>(stoppable.filter(predicate.negate()));
    }
}

