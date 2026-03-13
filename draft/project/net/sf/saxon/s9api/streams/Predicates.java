/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.s9api.streams;

import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import net.sf.saxon.expr.sort.AtomicMatchKey;
import net.sf.saxon.s9api.ItemType;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmArray;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmFunctionItem;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmMap;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmNodeKind;
import net.sf.saxon.s9api.streams.Step;

public class Predicates {
    public static Predicate<XdmItem> isNode() {
        return item -> item instanceof XdmNode;
    }

    public static Predicate<XdmItem> isElement() {
        return Predicates.nodeKindPredicate(XdmNodeKind.ELEMENT);
    }

    public static Predicate<XdmItem> isAttribute() {
        return Predicates.nodeKindPredicate(XdmNodeKind.ATTRIBUTE);
    }

    public static Predicate<XdmItem> isText() {
        return Predicates.nodeKindPredicate(XdmNodeKind.TEXT);
    }

    public static Predicate<XdmItem> isComment() {
        return Predicates.nodeKindPredicate(XdmNodeKind.COMMENT);
    }

    public static Predicate<XdmItem> isProcessingInstruction() {
        return Predicates.nodeKindPredicate(XdmNodeKind.PROCESSING_INSTRUCTION);
    }

    public static Predicate<XdmItem> isDocument() {
        return Predicates.nodeKindPredicate(XdmNodeKind.DOCUMENT);
    }

    public static Predicate<XdmItem> isNamespace() {
        return Predicates.nodeKindPredicate(XdmNodeKind.NAMESPACE);
    }

    public static Predicate<XdmItem> isAtomic() {
        return item -> item instanceof XdmAtomicValue;
    }

    public static Predicate<XdmItem> isFunction() {
        return item -> item instanceof XdmFunctionItem;
    }

    public static Predicate<XdmItem> isMap() {
        return item -> item instanceof XdmMap;
    }

    public static Predicate<XdmItem> isArray() {
        return item -> item instanceof XdmArray;
    }

    public static <T extends XdmItem> Predicate<XdmItem> empty(Step<T> step) {
        return item -> !((Stream)step.apply(item)).findFirst().isPresent();
    }

    public static <T> Predicate<T> not(Predicate<T> condition) {
        return condition.negate();
    }

    public static <T extends XdmItem> Predicate<XdmItem> exists(Step<T> step) {
        return item -> ((Stream)step.apply(item)).findFirst().isPresent();
    }

    public static Predicate<? super XdmNode> hasName(String uri, String localName) {
        return item -> {
            QName name = item.getNodeName();
            return name != null && name.getLocalName().equals(localName) && name.getNamespaceURI().equals(uri);
        };
    }

    public static Predicate<XdmNode> hasLocalName(String localName) {
        return item -> {
            QName name = item.getNodeName();
            return name != null && name.getLocalName().equals(localName);
        };
    }

    public static Predicate<XdmNode> hasNamespace(String uri) {
        return item -> {
            QName name = item.getNodeName();
            return name != null && name.getNamespaceURI().equals(uri);
        };
    }

    public static Predicate<XdmNode> hasAttribute(String local) {
        return item -> item.attribute(local) != null;
    }

    public static Predicate<XdmNode> attributeEq(String local, String value) {
        return item -> value.equals(item.attribute(local));
    }

    public static Predicate<XdmItem> hasType(ItemType type) {
        return type::matches;
    }

    public static <T extends XdmItem> Predicate<XdmItem> some(Step<T> step, Predicate<? super T> condition) {
        return item -> ((Stream)step.apply(item)).anyMatch(condition);
    }

    public static <T extends XdmItem> Predicate<XdmItem> every(Step<T> step, Predicate<? super XdmItem> condition) {
        return item -> ((Stream)step.apply(item)).allMatch(condition);
    }

    public static Predicate<XdmAtomicValue> eq(XdmAtomicValue value) {
        AtomicMatchKey k2 = value.getUnderlyingValue().asMapKey();
        return item -> item.getUnderlyingValue().asMapKey().equals(k2);
    }

    public static Predicate<XdmItem> eq(String value) {
        return item -> item.getStringValue().equals(value);
    }

    public static Predicate<XdmItem> matchesRegex(String regex) {
        Pattern re = Pattern.compile(regex);
        return item -> re.matcher(item.getStringValue()).find();
    }

    public static <T extends XdmItem> Predicate<XdmItem> eq(Step<T> step, String value) {
        return Predicates.some(step, Predicates.eq(value));
    }

    private static Predicate<XdmItem> nodeKindPredicate(XdmNodeKind kind) {
        return item -> item instanceof XdmNode && ((XdmNode)item).getNodeKind() == kind;
    }
}

