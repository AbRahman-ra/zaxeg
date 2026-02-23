/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions;

import java.util.ArrayList;
import java.util.HashSet;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.sort.AtomicComparer;
import net.sf.saxon.expr.sort.GenericAtomicComparer;
import net.sf.saxon.functions.CollatingFunctionFixed;
import net.sf.saxon.lib.ErrorReporter;
import net.sf.saxon.om.Function;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NameOfNode;
import net.sf.saxon.om.NamespaceBinding;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.SequenceTool;
import net.sf.saxon.pattern.SameNameTest;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.trans.XmlProcessingIncident;
import net.sf.saxon.tree.iter.AtomicIterator;
import net.sf.saxon.tree.iter.AxisIterator;
import net.sf.saxon.tree.tiny.WhitespaceTextImpl;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.tree.util.Navigator;
import net.sf.saxon.tree.util.Orphan;
import net.sf.saxon.type.ComplexType;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.type.Type;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.BooleanValue;
import net.sf.saxon.value.ObjectValue;
import net.sf.saxon.value.SequenceExtent;
import net.sf.saxon.value.StringValue;
import net.sf.saxon.value.Whitespace;

public class DeepEqual
extends CollatingFunctionFixed {
    public static final int INCLUDE_NAMESPACES = 1;
    public static final int INCLUDE_PREFIXES = 2;
    public static final int INCLUDE_COMMENTS = 4;
    public static final int INCLUDE_PROCESSING_INSTRUCTIONS = 8;
    public static final int EXCLUDE_WHITESPACE_TEXT_NODES = 16;
    public static final int COMPARE_STRING_VALUES = 32;
    public static final int COMPARE_ANNOTATIONS = 64;
    public static final int WARNING_IF_FALSE = 128;
    public static final int JOIN_ADJACENT_TEXT_NODES = 256;
    public static final int COMPARE_ID_FLAGS = 512;
    public static final int EXCLUDE_VARIETY = 1024;

    public static boolean deepEqual(SequenceIterator op1, SequenceIterator op2, AtomicComparer comparer, XPathContext context, int flags) throws XPathException {
        ErrorReporter reporter;
        String reason;
        boolean result;
        block14: {
            result = true;
            reason = null;
            reporter = context.getErrorReporter();
            try {
                if ((flags & 0x100) != 0) {
                    op1 = DeepEqual.mergeAdjacentTextNodes(op1);
                    op2 = DeepEqual.mergeAdjacentTextNodes(op2);
                }
                int pos1 = 0;
                int pos2 = 0;
                while (true) {
                    Item item1 = op1.next();
                    Item item2 = op2.next();
                    if (item1 == null && item2 == null) break block14;
                    ++pos1;
                    ++pos2;
                    if (item1 == null || item2 == null) {
                        result = false;
                        reason = item1 == null ? "Second sequence is longer (first sequence length = " + pos2 + ")" : "First sequence is longer (second sequence length = " + pos1 + ")";
                        if (item1 instanceof WhitespaceTextImpl || item2 instanceof WhitespaceTextImpl) {
                            reason = reason + " (the first extra node is whitespace text)";
                        }
                        break block14;
                    }
                    if (item1 instanceof Function || item2 instanceof Function) {
                        if (!(item1 instanceof Function) || !(item2 instanceof Function)) {
                            reason = "if one item is a function then both must be functions (position " + pos1 + ")";
                            return false;
                        }
                        boolean fe = ((Function)item1).deepEquals((Function)item2, context, comparer, flags);
                        if (fe) continue;
                        result = false;
                        reason = "functions at position " + pos1 + " differ";
                        break block14;
                    }
                    if (item1 instanceof ObjectValue || item2 instanceof ObjectValue) {
                        if (item1.equals(item2)) continue;
                        return false;
                    }
                    if (item1 instanceof NodeInfo) {
                        if (item2 instanceof NodeInfo) {
                            if (DeepEqual.deepEquals((NodeInfo)item1, (NodeInfo)item2, comparer, context, flags)) continue;
                            result = false;
                            reason = "nodes at position " + pos1 + " differ";
                        } else {
                            result = false;
                            reason = "comparing a node to an atomic value at position " + pos1;
                        }
                        break block14;
                    }
                    if (item2 instanceof NodeInfo) {
                        result = false;
                        reason = "comparing an atomic value to a node at position " + pos1;
                        break block14;
                    }
                    AtomicValue av1 = (AtomicValue)item1;
                    AtomicValue av2 = (AtomicValue)item2;
                    if (!(av1.isNaN() && av2.isNaN() || comparer.comparesEqual(av1, av2))) break;
                }
                result = false;
                reason = "atomic values at position " + pos1 + " differ";
            } catch (ClassCastException err) {
                result = false;
                reason = "sequences contain non-comparable values";
            }
        }
        if (!result) {
            DeepEqual.explain(reporter, reason, flags, null, null);
        }
        return result;
    }

    public static boolean deepEquals(NodeInfo n1, NodeInfo n2, AtomicComparer comparer, XPathContext context, int flags) throws XPathException {
        if (n1.equals(n2)) {
            return true;
        }
        ErrorReporter reporter = context.getErrorReporter();
        if (n1.getNodeKind() != n2.getNodeKind()) {
            DeepEqual.explain(reporter, "node kinds differ: comparing " + DeepEqual.showKind(n1) + " to " + DeepEqual.showKind(n2), flags, n1, n2);
            return false;
        }
        switch (n1.getNodeKind()) {
            case 1: {
                NodeInfo att1;
                AxisIterator a2;
                if (!Navigator.haveSameName(n1, n2)) {
                    DeepEqual.explain(reporter, "element names differ: " + NameOfNode.makeName(n1).getStructuredQName().getEQName() + " != " + NameOfNode.makeName(n2).getStructuredQName().getEQName(), flags, n1, n2);
                    return false;
                }
                if ((flags & 2) != 0 && !n1.getPrefix().equals(n2.getPrefix())) {
                    DeepEqual.explain(reporter, "element prefixes differ: " + n1.getPrefix() + " != " + n2.getPrefix(), flags, n1, n2);
                    return false;
                }
                AxisIterator a1 = n1.iterateAxis(2);
                if (!SequenceTool.sameLength(a1, a2 = n2.iterateAxis(2))) {
                    DeepEqual.explain(reporter, "elements have different number of attributes", flags, n1, n2);
                    return false;
                }
                a1 = n1.iterateAxis(2);
                while ((att1 = a1.next()) != null) {
                    AxisIterator a2iter = n2.iterateAxis(2, new SameNameTest(att1));
                    NodeInfo att2 = a2iter.next();
                    if (att2 == null) {
                        DeepEqual.explain(reporter, "one element has an attribute " + NameOfNode.makeName(att1).getStructuredQName().getEQName() + ", the other does not", flags, n1, n2);
                        return false;
                    }
                    if (DeepEqual.deepEquals(att1, att2, comparer, context, flags)) continue;
                    DeepEqual.deepEquals(att1, att2, comparer, context, flags);
                    DeepEqual.explain(reporter, "elements have different values for the attribute " + NameOfNode.makeName(att1).getStructuredQName().getEQName(), flags, n1, n2);
                    return false;
                }
                if ((flags & 1) != 0) {
                    NodeInfo nn2;
                    NodeInfo nn1;
                    HashSet<NamespaceBinding> ns1 = new HashSet<NamespaceBinding>(10);
                    HashSet<NamespaceBinding> ns2 = new HashSet<NamespaceBinding>(10);
                    AxisIterator it1 = n1.iterateAxis(8);
                    while ((nn1 = it1.next()) != null) {
                        NamespaceBinding nscode1 = new NamespaceBinding(nn1.getLocalPart(), nn1.getStringValue());
                        ns1.add(nscode1);
                    }
                    AxisIterator it2 = n2.iterateAxis(8);
                    while ((nn2 = it2.next()) != null) {
                        NamespaceBinding nscode2 = new NamespaceBinding(nn2.getLocalPart(), nn2.getStringValue());
                        ns2.add(nscode2);
                    }
                    if (!ns1.equals(ns2)) {
                        DeepEqual.explain(reporter, "elements have different in-scope namespaces: " + DeepEqual.showNamespaces(ns1) + " versus " + DeepEqual.showNamespaces(ns2), flags, n1, n2);
                        return false;
                    }
                }
                if ((flags & 0x40) != 0 && !n1.getSchemaType().equals(n2.getSchemaType())) {
                    DeepEqual.explain(reporter, "elements have different type annotation", flags, n1, n2);
                    return false;
                }
                if ((flags & 0x400) == 0) {
                    int variety2;
                    int variety1;
                    if (n1.getSchemaType().isComplexType() != n2.getSchemaType().isComplexType()) {
                        DeepEqual.explain(reporter, "one element has complex type, the other simple", flags, n1, n2);
                        return false;
                    }
                    if (n1.getSchemaType().isComplexType() && (variety1 = ((ComplexType)n1.getSchemaType()).getVariety()) != (variety2 = ((ComplexType)n2.getSchemaType()).getVariety())) {
                        DeepEqual.explain(reporter, "both elements have complex type, but a different variety", flags, n1, n2);
                        return false;
                    }
                }
                if ((flags & 0x20) == 0) {
                    boolean isSimple2;
                    SchemaType type1 = n1.getSchemaType();
                    SchemaType type2 = n2.getSchemaType();
                    boolean isSimple1 = type1.isSimpleType() || ((ComplexType)type1).isSimpleContent();
                    boolean bl = isSimple2 = type2.isSimpleType() || ((ComplexType)type2).isSimpleContent();
                    if (isSimple1 != isSimple2) {
                        DeepEqual.explain(reporter, "one element has a simple type, the other does not", flags, n1, n2);
                        return false;
                    }
                    if (isSimple1) {
                        assert (isSimple2);
                        AtomicIterator v1 = n1.atomize().iterate();
                        AtomicIterator v2 = n2.atomize().iterate();
                        return DeepEqual.deepEqual(v1, v2, comparer, context, flags);
                    }
                }
                if ((flags & 0x200) != 0) {
                    if (n1.isId() != n2.isId()) {
                        DeepEqual.explain(reporter, "one element is an ID, the other is not", flags, n1, n2);
                        return false;
                    }
                    if (n1.isIdref() != n2.isIdref()) {
                        DeepEqual.explain(reporter, "one element is an IDREF, the other is not", flags, n1, n2);
                        return false;
                    }
                }
            }
            case 9: {
                NodeInfo d2;
                NodeInfo d1;
                AxisIterator c1 = n1.iterateAxis(3);
                AxisIterator c2 = n2.iterateAxis(3);
                do {
                    boolean r;
                    d1 = c1.next();
                    while (d1 != null && DeepEqual.isIgnorable(d1, flags)) {
                        d1 = c1.next();
                    }
                    d2 = c2.next();
                    while (d2 != null && DeepEqual.isIgnorable(d2, flags)) {
                        d2 = c2.next();
                    }
                    if (d1 != null && d2 != null) continue;
                    boolean bl = r = d1 == d2;
                    if (!r) {
                        String message = "the first operand contains a node with " + (d1 == null ? "fewer" : "more") + " children than the second";
                        if (d1 instanceof WhitespaceTextImpl || d2 instanceof WhitespaceTextImpl) {
                            message = message + " (the first extra child is whitespace text)";
                        }
                        DeepEqual.explain(reporter, message, flags, n1, n2);
                    }
                    return r;
                } while (DeepEqual.deepEquals(d1, d2, comparer, context, flags));
                return false;
            }
            case 2: {
                if (!Navigator.haveSameName(n1, n2)) {
                    DeepEqual.explain(reporter, "attribute names differ: " + NameOfNode.makeName(n1).getStructuredQName().getEQName() + " != " + NameOfNode.makeName(n1).getStructuredQName().getEQName(), flags, n1, n2);
                    return false;
                }
                if ((flags & 2) != 0 && !n1.getPrefix().equals(n2.getPrefix())) {
                    DeepEqual.explain(reporter, "attribute prefixes differ: " + n1.getPrefix() + " != " + n2.getPrefix(), flags, n1, n2);
                    return false;
                }
                if ((flags & 0x40) != 0 && !n1.getSchemaType().equals(n2.getSchemaType())) {
                    DeepEqual.explain(reporter, "attributes have different type annotations", flags, n1, n2);
                    return false;
                }
                boolean ar = (flags & 0x20) == 0 ? DeepEqual.deepEqual(n1.atomize().iterate(), n2.atomize().iterate(), comparer, context, 0) : comparer.comparesEqual(new StringValue(n1.getStringValueCS()), new StringValue(n2.getStringValueCS()));
                if (!ar) {
                    DeepEqual.explain(reporter, "attribute values differ", flags, n1, n2);
                    return false;
                }
                if ((flags & 0x200) != 0) {
                    if (n1.isId() != n2.isId()) {
                        DeepEqual.explain(reporter, "one attribute is an ID, the other is not", flags, n1, n2);
                        return false;
                    }
                    if (n1.isIdref() != n2.isIdref()) {
                        DeepEqual.explain(reporter, "one attribute is an IDREF, the other is not", flags, n1, n2);
                        return false;
                    }
                }
                return true;
            }
            case 7: 
            case 13: {
                if (!n1.getLocalPart().equals(n2.getLocalPart())) {
                    DeepEqual.explain(reporter, Type.displayTypeName(n1) + " names differ", flags, n1, n2);
                    return false;
                }
            }
            case 3: 
            case 8: {
                boolean vr = comparer.comparesEqual((AtomicValue)n1.atomize(), (AtomicValue)n2.atomize());
                if (!vr && (flags & 0x80) != 0) {
                    String v1 = n1.atomize().getStringValue();
                    String v2 = n2.atomize().getStringValue();
                    String message = "";
                    if (v1.length() != v2.length()) {
                        message = "lengths (" + v1.length() + "," + v2.length() + ")";
                    }
                    if (v1.length() < 10 && v2.length() < 10) {
                        message = " (\"" + v1 + "\" vs \"" + v2 + "\")";
                    } else {
                        int min = Math.min(v1.length(), v2.length());
                        if (v1.substring(0, min).equals(v2.substring(0, min))) {
                            message = message + " different at char " + min + "(\"" + StringValue.diagnosticDisplay((v1.length() > v2.length() ? v1 : v2).substring(min)) + "\")";
                        } else if (v1.charAt(0) != v2.charAt(0)) {
                            message = message + " different at start (\"" + v1.substring(0, Math.min(v1.length(), 10)) + "\", \"" + v2.substring(0, Math.min(v2.length(), 10)) + "\")";
                        } else {
                            for (int i = 1; i < min; ++i) {
                                if (v1.substring(0, i).equals(v2.substring(0, i))) continue;
                                message = message + " different at char " + (i - 1) + "(\"" + v1.substring(i - 1, Math.min(v1.length(), i + 10)) + "\", \"" + v2.substring(i - 1, Math.min(v2.length(), i + 10)) + "\")";
                                break;
                            }
                        }
                    }
                    DeepEqual.explain(reporter, Type.displayTypeName(n1) + " values differ (" + Navigator.getPath(n1) + ", " + Navigator.getPath(n2) + "): " + message, flags, n1, n2);
                }
                return vr;
            }
        }
        throw new IllegalArgumentException("Unknown node type");
    }

    private static boolean isIgnorable(NodeInfo node, int flags) {
        int kind = node.getNodeKind();
        if (kind == 8) {
            return (flags & 4) == 0;
        }
        if (kind == 7) {
            return (flags & 8) == 0;
        }
        if (kind == 3) {
            return (flags & 0x10) != 0 && Whitespace.isWhite(node.getStringValueCS());
        }
        return false;
    }

    private static void explain(ErrorReporter reporter, String message, int flags, NodeInfo n1, NodeInfo n2) {
        if ((flags & 0x80) != 0) {
            reporter.report(new XmlProcessingIncident("deep-equal() " + (n1 != null && n2 != null ? "comparing " + Navigator.getPath(n1) + " to " + Navigator.getPath(n2) + ": " : ": ") + message).asWarning());
        }
    }

    private static String showKind(Item item) {
        if (item instanceof NodeInfo && ((NodeInfo)item).getNodeKind() == 3 && Whitespace.isWhite(item.getStringValueCS())) {
            return "whitespace text() node";
        }
        return Type.displayTypeName(item);
    }

    private static String showNamespaces(HashSet<NamespaceBinding> bindings) {
        FastStringBuffer sb = new FastStringBuffer(256);
        for (NamespaceBinding binding : bindings) {
            sb.append(binding.getPrefix());
            sb.append("=");
            sb.append(binding.getURI());
            sb.append(" ");
        }
        sb.setLength(sb.length() - 1);
        return sb.toString();
    }

    private static SequenceIterator mergeAdjacentTextNodes(SequenceIterator in) throws XPathException {
        Item next;
        ArrayList<Item> items = new ArrayList<Item>(20);
        boolean prevIsText = false;
        FastStringBuffer textBuffer = new FastStringBuffer(64);
        while ((next = in.next()) != null) {
            if (next instanceof NodeInfo && ((NodeInfo)next).getNodeKind() == 3) {
                textBuffer.cat(next.getStringValueCS());
                prevIsText = true;
                continue;
            }
            if (prevIsText) {
                Orphan textNode = new Orphan(null);
                textNode.setNodeKind((short)3);
                textNode.setStringValue(textBuffer.toString());
                items.add(textNode);
                textBuffer.setLength(0);
            }
            prevIsText = false;
            items.add(next);
        }
        if (prevIsText) {
            Orphan textNode = new Orphan(null);
            textNode.setNodeKind((short)3);
            textNode.setStringValue(textBuffer.toString());
            items.add(textNode);
        }
        SequenceExtent extent = new SequenceExtent(items);
        return extent.iterate();
    }

    @Override
    public BooleanValue call(XPathContext context, Sequence[] arguments) throws XPathException {
        GenericAtomicComparer comparer = new GenericAtomicComparer(this.getStringCollator(), context);
        boolean b = DeepEqual.deepEqual(arguments[0].iterate(), arguments[1].iterate(), comparer, context, 0);
        return BooleanValue.get(b);
    }

    @Override
    public String getStreamerName() {
        return "DeepEqual";
    }
}

