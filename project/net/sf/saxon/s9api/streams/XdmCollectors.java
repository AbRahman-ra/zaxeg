/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.s9api.streams;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmValue;

public class XdmCollectors {
    public static XdmCollector<XdmValue, XdmItem> asXdmValue() {
        return new XdmCollector<XdmValue, XdmItem>(){

            @Override
            protected XdmValue makeResult(List<XdmItem> list) {
                return new XdmValue(list);
            }
        };
    }

    public static XdmCollector<XdmNode, XdmNode> asNode() {
        return new XdmCollector<XdmNode, XdmNode>(){

            @Override
            protected void onEmpty() {
                throw new NoSuchElementException();
            }

            @Override
            protected void onMultiple() {
                throw new MultipleItemException();
            }

            @Override
            protected XdmNode convert(XdmItem item) {
                return (XdmNode)item;
            }

            @Override
            protected XdmNode makeResult(List<XdmNode> list) {
                return list.get(0);
            }
        };
    }

    public static XdmCollector<Optional<XdmNode>, XdmNode> asOptionalNode() {
        return new XdmCollector<Optional<XdmNode>, XdmNode>(){

            @Override
            protected void onEmpty() {
            }

            @Override
            protected void onMultiple() {
                throw new MultipleItemException();
            }

            @Override
            protected XdmNode convert(XdmItem item) {
                return (XdmNode)item;
            }

            @Override
            protected Optional<XdmNode> makeResult(List<XdmNode> list) {
                return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
            }
        };
    }

    public static XdmCollector<List<XdmNode>, XdmNode> asListOfNodes() {
        return new XdmCollector<List<XdmNode>, XdmNode>(){

            @Override
            protected void onEmpty() {
            }

            @Override
            protected void onMultiple() {
            }

            @Override
            protected XdmNode convert(XdmItem item) {
                return (XdmNode)item;
            }

            @Override
            protected List<XdmNode> makeResult(List<XdmNode> list) {
                return list;
            }
        };
    }

    public static XdmCollector<List<XdmAtomicValue>, XdmAtomicValue> asListOfAtomic() {
        return new XdmCollector<List<XdmAtomicValue>, XdmAtomicValue>(){

            @Override
            protected void onEmpty() {
            }

            @Override
            protected void onMultiple() {
            }

            @Override
            protected XdmAtomicValue convert(XdmItem item) {
                return (XdmAtomicValue)item;
            }

            @Override
            protected List<XdmAtomicValue> makeResult(List<XdmAtomicValue> list) {
                return list;
            }
        };
    }

    public static XdmCollector<Optional<XdmAtomicValue>, XdmAtomicValue> asOptionalAtomic() {
        return new XdmCollector<Optional<XdmAtomicValue>, XdmAtomicValue>(){

            @Override
            protected void onEmpty() {
            }

            @Override
            protected void onMultiple() {
                throw new MultipleItemException();
            }

            @Override
            protected XdmAtomicValue convert(XdmItem item) {
                return (XdmAtomicValue)item;
            }

            @Override
            protected Optional<XdmAtomicValue> makeResult(List<XdmAtomicValue> list) {
                return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
            }
        };
    }

    public static XdmCollector<XdmAtomicValue, XdmAtomicValue> asAtomic() {
        return new XdmCollector<XdmAtomicValue, XdmAtomicValue>(){

            @Override
            protected void onEmpty() {
                throw new NoSuchElementException();
            }

            @Override
            protected void onMultiple() {
                throw new MultipleItemException();
            }

            @Override
            protected XdmAtomicValue convert(XdmItem item) {
                return (XdmAtomicValue)item;
            }

            @Override
            protected XdmAtomicValue makeResult(List<XdmAtomicValue> list) {
                return list.get(0);
            }
        };
    }

    public static XdmCollector<Optional<String>, XdmItem> asOptionalString() {
        return new XdmCollector<Optional<String>, XdmItem>(){

            @Override
            protected void onMultiple() {
                throw new MultipleItemException();
            }

            @Override
            protected Optional<String> makeResult(List<XdmItem> list) {
                return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0).getStringValue());
            }
        };
    }

    public static XdmCollector<String, XdmItem> asString() {
        return new XdmCollector<String, XdmItem>(){

            @Override
            protected void onEmpty() {
                throw new NoSuchElementException();
            }

            @Override
            protected void onMultiple() {
                throw new MultipleItemException();
            }

            @Override
            protected String makeResult(List<XdmItem> list) {
                return list.get(0).getStringValue();
            }
        };
    }

    private static abstract class XdmCollector<R, I extends XdmItem>
    implements Collector<XdmItem, List<I>, R> {
        private XdmCollector() {
        }

        protected void onEmpty() {
        }

        protected void onMultiple() {
        }

        protected I convert(XdmItem item) {
            return (I)item;
        }

        protected abstract R makeResult(List<I> var1);

        @Override
        public Supplier<List<I>> supplier() {
            return ArrayList::new;
        }

        @Override
        public BiConsumer<List<I>, XdmItem> accumulator() {
            return (list, next) -> {
                I item = this.convert((XdmItem)next);
                if (!list.isEmpty()) {
                    this.onMultiple();
                }
                list.add(item);
            };
        }

        @Override
        public BinaryOperator<List<I>> combiner() {
            return (list1, list2) -> {
                list1.addAll(list2);
                if (list1.size() > 1) {
                    this.onMultiple();
                }
                return list1;
            };
        }

        @Override
        public Function<List<I>, R> finisher() {
            return list -> {
                if (list.isEmpty()) {
                    this.onEmpty();
                }
                return this.makeResult((List<I>)list);
            };
        }

        @Override
        public Set<Collector.Characteristics> characteristics() {
            return Collections.emptySet();
        }
    }

    public static class MultipleItemException
    extends RuntimeException {
    }
}

