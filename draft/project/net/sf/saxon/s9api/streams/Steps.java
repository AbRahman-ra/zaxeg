/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.s9api.streams;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.sf.saxon.lib.ConversionRules;
import net.sf.saxon.ma.arrays.ArrayItem;
import net.sf.saxon.om.AtomicSequence;
import net.sf.saxon.om.NameChecker;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.pattern.NodeTest;
import net.sf.saxon.s9api.Axis;
import net.sf.saxon.s9api.ItemType;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.SaxonApiUncheckedException;
import net.sf.saxon.s9api.XdmArray;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmNodeKind;
import net.sf.saxon.s9api.XdmSequenceIterator;
import net.sf.saxon.s9api.XdmValue;
import net.sf.saxon.s9api.streams.AxisStep;
import net.sf.saxon.s9api.streams.Predicates;
import net.sf.saxon.s9api.streams.Step;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.AtomicType;
import net.sf.saxon.type.Converter;
import net.sf.saxon.type.ValidationException;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.Whitespace;

public class Steps {
    private static final Step<XdmNode> ANCESTOR = new AxisStep(Axis.ANCESTOR);
    private static final Step<XdmNode> ANCESTOR_OR_SELF = new AxisStep(Axis.ANCESTOR_OR_SELF);
    private static final Step<XdmNode> ATTRIBUTE = new AxisStep(Axis.ATTRIBUTE);
    private static final Step<XdmNode> CHILD = new AxisStep(Axis.CHILD);
    private static final Step<XdmNode> DESCENDANT = new AxisStep(Axis.DESCENDANT);
    private static final Step<XdmNode> DESCENDANT_OR_SELF = new AxisStep(Axis.DESCENDANT_OR_SELF);
    private static final Step<XdmNode> FOLLOWING = new AxisStep(Axis.FOLLOWING);
    private static final Step<XdmNode> FOLLOWING_SIBLING = new AxisStep(Axis.FOLLOWING_SIBLING);
    private static final Step<XdmNode> NAMESPACE = new AxisStep(Axis.NAMESPACE);
    private static final Step<XdmNode> PARENT = new AxisStep(Axis.PARENT);
    private static final Step<XdmNode> PRECEDING_SIBLING = new AxisStep(Axis.PRECEDING_SIBLING);
    private static final Step<XdmNode> PRECEDING = new AxisStep(Axis.PRECEDING);
    private static final Step<XdmNode> SELF = new AxisStep(Axis.SELF);

    public static Step<XdmNode> root() {
        return new Step<XdmNode>(){

            @Override
            public Stream<XdmNode> apply(XdmItem origin) {
                return origin instanceof XdmNode ? Stream.of(((XdmNode)origin).getRoot()) : Stream.empty();
            }
        };
    }

    public static Step<XdmAtomicValue> atomize() {
        return new Step<XdmAtomicValue>(){

            @Override
            public Stream<? extends XdmAtomicValue> apply(XdmItem item) {
                if (item instanceof XdmAtomicValue) {
                    return Stream.of((XdmAtomicValue)item);
                }
                if (item instanceof XdmNode) {
                    try {
                        return ((XdmNode)item).getTypedValue().stream();
                    } catch (SaxonApiException e) {
                        throw new SaxonApiUncheckedException(e);
                    }
                }
                if (item instanceof XdmArray) {
                    try {
                        ArrayItem arrayItem = ((XdmArray)item).getUnderlyingValue();
                        AtomicSequence data = arrayItem.atomize();
                        return XdmValue.wrap(data).stream();
                    } catch (XPathException e) {
                        throw new SaxonApiUncheckedException(new SaxonApiException(e));
                    }
                }
                throw new SaxonApiUncheckedException(new SaxonApiException("Cannot atomize supplied value"));
            }
        };
    }

    public static Step<XdmAtomicValue> castAs(ItemType type) {
        if (!ItemType.ANY_ATOMIC_VALUE.subsumes(type)) {
            throw new IllegalArgumentException("Target of castAs must be an atomic type");
        }
        final net.sf.saxon.type.ItemType tType = type.getUnderlyingItemType().getPrimitiveItemType();
        final ConversionRules rules = type.getConversionRules();
        return Steps.atomize().then(new Step<XdmAtomicValue>(){

            @Override
            public Stream<? extends XdmAtomicValue> apply(XdmItem xdmItem) {
                try {
                    AtomicValue source = ((XdmAtomicValue)xdmItem).getUnderlyingValue();
                    Converter converter = rules.getConverter(source.getItemType(), (AtomicType)tType);
                    AtomicValue result = converter.convert(source).asAtomic();
                    return Stream.of((XdmAtomicValue)XdmValue.wrap(result));
                } catch (ValidationException e) {
                    throw new SaxonApiUncheckedException(new SaxonApiException(e));
                }
            }
        });
    }

    public static <U extends XdmItem> Step<U> nothing() {
        return new Step<U>(){

            @Override
            public Stream<U> apply(XdmItem xdmItem) {
                return Stream.empty();
            }
        };
    }

    private static Predicate<XdmNode> nodeTestPredicate(NodeTest test) {
        return item -> test.test(item.getUnderlyingNode());
    }

    private static Predicate<? super XdmNode> localNamePredicate(String given) {
        if ("*".equals(given)) {
            return Predicates.isElement();
        }
        return item -> {
            NodeInfo node = item.getUnderlyingNode();
            return node.getNodeKind() == 1 && node.getLocalPart().equals(given);
        };
    }

    private static Predicate<? super XdmNode> expandedNamePredicate(String ns, String local) {
        return item -> {
            NodeInfo node = item.getUnderlyingNode();
            return node.getNodeKind() == 1 && node.getLocalPart().equals(local) && node.getURI().equals(ns);
        };
    }

    public static Step<XdmNode> ancestor() {
        return ANCESTOR;
    }

    public static Step<XdmNode> ancestor(String localName) {
        return Steps.ancestor().where(Steps.localNamePredicate(localName));
    }

    public static Step<XdmNode> ancestor(String uri, String localName) {
        return Steps.ancestor().where(Steps.expandedNamePredicate(uri, localName));
    }

    public static Step<XdmNode> ancestor(Predicate<? super XdmNode> filter) {
        return Steps.ancestor().where(filter);
    }

    public static Step<XdmNode> ancestorOrSelf() {
        return ANCESTOR_OR_SELF;
    }

    public static Step<XdmNode> ancestorOrSelf(String localName) {
        return Steps.ancestorOrSelf().where(Steps.localNamePredicate(localName));
    }

    public static Step<XdmNode> ancestorOrSelf(String uri, String localName) {
        return Steps.ancestorOrSelf().where(Steps.expandedNamePredicate(uri, localName));
    }

    public static Step<XdmNode> ancestorOrSelf(Predicate<? super XdmNode> filter) {
        return Steps.ancestorOrSelf().where(filter);
    }

    public static Step<XdmNode> attribute() {
        return ATTRIBUTE;
    }

    public static Step<XdmNode> attribute(String localName) {
        return "*".equals(localName) ? Steps.attribute() : Steps.attribute().where(Predicates.hasLocalName(localName));
    }

    public static Step<XdmNode> attribute(String uri, String localName) {
        return Steps.attribute().where(Predicates.hasName(uri, localName));
    }

    public static Step<XdmNode> attribute(Predicate<? super XdmNode> filter) {
        return Steps.attribute().where(filter);
    }

    public static Step<XdmNode> child() {
        return CHILD;
    }

    public static Step<XdmNode> child(String localName) {
        return Steps.child().where(Steps.localNamePredicate(localName));
    }

    public static Step<XdmNode> child(String uri, String localName) {
        return Steps.child().where(Steps.expandedNamePredicate(uri, localName));
    }

    public static Step<XdmNode> child(Predicate<? super XdmNode> filter) {
        return Steps.child().where(filter);
    }

    public static Step<XdmNode> descendant() {
        return DESCENDANT;
    }

    public static Step<XdmNode> descendant(String localName) {
        return Steps.descendant().where(Steps.localNamePredicate(localName));
    }

    public static Step<XdmNode> descendant(String uri, String localName) {
        return Steps.descendant().where(Steps.expandedNamePredicate(uri, localName));
    }

    public static Step<XdmNode> descendantOrSelf() {
        return DESCENDANT_OR_SELF;
    }

    public static Step<XdmNode> descendant(Predicate<? super XdmNode> filter) {
        return Steps.descendant().where(filter);
    }

    public static Step<XdmNode> descendantOrSelf(String localName) {
        return Steps.descendantOrSelf().where(Steps.localNamePredicate(localName));
    }

    public static Step<XdmNode> descendantOrSelf(String uri, String localName) {
        return Steps.descendantOrSelf().where(Steps.expandedNamePredicate(uri, localName));
    }

    public static Step<XdmNode> descendantOrSelf(Predicate<? super XdmNode> filter) {
        return Steps.descendantOrSelf().where(filter);
    }

    public static Step<XdmNode> following() {
        return FOLLOWING;
    }

    public static Step<XdmNode> following(String localName) {
        return Steps.following().where(Steps.localNamePredicate(localName));
    }

    public static Step<XdmNode> following(String uri, String localName) {
        return Steps.following().where(Steps.expandedNamePredicate(uri, localName));
    }

    public static Step<XdmNode> following(Predicate<? super XdmNode> filter) {
        return Steps.following().where(filter);
    }

    public static Step<XdmNode> followingSibling() {
        return FOLLOWING_SIBLING;
    }

    public static Step<XdmNode> followingSibling(String localName) {
        return Steps.followingSibling().where(Steps.localNamePredicate(localName));
    }

    public static Step<XdmNode> followingSibling(String uri, String localName) {
        return Steps.followingSibling().where(Steps.expandedNamePredicate(uri, localName));
    }

    public static Step<XdmNode> followingSibling(Predicate<? super XdmNode> filter) {
        return Steps.followingSibling().where(filter);
    }

    public static Step<XdmNode> namespace() {
        return NAMESPACE;
    }

    public static Step<XdmNode> namespace(String localName) {
        return "*".equals(localName) ? Steps.namespace() : Steps.namespace().where(Predicates.hasLocalName(localName));
    }

    public static Step<XdmNode> namespace(Predicate<? super XdmNode> filter) {
        return Steps.namespace().where(filter);
    }

    public static Step<XdmNode> parent() {
        return PARENT;
    }

    public static Step<XdmNode> parent(String localName) {
        return Steps.parent().where(Predicates.isElement()).where(Steps.localNamePredicate(localName));
    }

    public static Step<XdmNode> parent(String uri, String localName) {
        return Steps.parent().where(Steps.expandedNamePredicate(uri, localName));
    }

    public static Step<XdmNode> parent(Predicate<? super XdmNode> filter) {
        return Steps.parent().where(filter);
    }

    public static Step<XdmNode> precedingSibling() {
        return PRECEDING_SIBLING;
    }

    public static Step<XdmNode> precedingSibling(String localName) {
        return Steps.precedingSibling().where(Steps.localNamePredicate(localName));
    }

    public static Step<XdmNode> precedingSibling(String uri, String localName) {
        return Steps.precedingSibling().where(Steps.expandedNamePredicate(uri, localName));
    }

    public static Step<XdmNode> precedingSibling(Predicate<? super XdmNode> filter) {
        return Steps.precedingSibling().where(filter);
    }

    public static Step<XdmNode> preceding() {
        return PRECEDING;
    }

    public static Step<XdmNode> preceding(String localName) {
        return Steps.preceding().where(Steps.localNamePredicate(localName));
    }

    public static Step<XdmNode> preceding(String uri, String localName) {
        return Steps.preceding().where(Steps.expandedNamePredicate(uri, localName));
    }

    public static Step<XdmNode> preceding(Predicate<? super XdmNode> filter) {
        return Steps.preceding().where(filter);
    }

    public static Step<XdmNode> self() {
        return SELF;
    }

    public static Step<XdmNode> self(String localName) {
        return Steps.self().where(Steps.localNamePredicate(localName));
    }

    public static Step<XdmNode> self(String uri, String localName) {
        return Steps.self().where(Steps.expandedNamePredicate(uri, localName));
    }

    public static Step<XdmNode> self(Predicate<? super XdmNode> filter) {
        return Steps.self().where(filter);
    }

    public static Step<XdmNode> text() {
        return Steps.child().where(Predicates.isText());
    }

    @SafeVarargs
    public static Step<? extends XdmNode> path(Step<? extends XdmNode> ... steps) {
        return Steps.pathFromList(Arrays.asList(steps));
    }

    private static Step<? extends XdmNode> pathFromList(List<Step<? extends XdmNode>> steps) {
        if (steps.isEmpty()) {
            return Steps.nothing();
        }
        if (steps.size() == 1) {
            return steps.get(0);
        }
        return steps.get(0).then(Steps.pathFromList(steps.subList(1, steps.size())));
    }

    public static Step<? extends XdmNode> path(String ... steps) {
        ArrayList<Step<? extends XdmNode>> pathSteps = new ArrayList<Step<? extends XdmNode>>();
        for (String step : steps) {
            if (step.equals("/")) {
                pathSteps.add(Steps.root().where(Predicates.isDocument()));
                continue;
            }
            if (step.equals("..")) {
                pathSteps.add(Steps.parent());
                continue;
            }
            if (step.equals("*")) {
                pathSteps.add(Steps.child(Predicates.isElement()));
                continue;
            }
            if (step.equals("//")) {
                pathSteps.add(Steps.descendantOrSelf());
                continue;
            }
            if (step.startsWith("@")) {
                String name = step.substring(1);
                if (!NameChecker.isValidNCName(name)) {
                    throw new IllegalArgumentException("Invalid attribute name " + name);
                }
                pathSteps.add(Steps.attribute(name));
                continue;
            }
            if (!NameChecker.isValidNCName(step)) {
                throw new IllegalArgumentException("Invalid element name " + step);
            }
            pathSteps.add(Steps.child(step));
        }
        return Steps.pathFromList(pathSteps);
    }

    public static Step<XdmAtomicValue> tokenize() {
        return new Step<XdmAtomicValue>(){

            @Override
            public Stream<XdmAtomicValue> apply(XdmItem item) {
                Whitespace.Tokenizer iter = new Whitespace.Tokenizer(item.getStringValue());
                return XdmSequenceIterator.ofAtomicValues(iter).stream();
            }
        };
    }

    public static Step<XdmNode> id(final XdmNode doc) {
        return new Step<XdmNode>(){

            @Override
            public Stream<XdmNode> apply(XdmItem item) {
                if (doc.getNodeKind() != XdmNodeKind.DOCUMENT) {
                    throw new IllegalArgumentException("id() - argument is not a document node");
                }
                NodeInfo target = doc.getUnderlyingNode().getTreeInfo().selectID(item.getStringValue(), true);
                return target == null ? Stream.empty() : Stream.of((XdmNode)XdmNode.wrap(target));
            }
        };
    }
}

