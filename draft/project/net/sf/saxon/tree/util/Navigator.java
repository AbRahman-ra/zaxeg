/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.tree.util;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;
import net.sf.saxon.Controller;
import net.sf.saxon.event.Outputter;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.om.AbsolutePath;
import net.sf.saxon.om.AttributeInfo;
import net.sf.saxon.om.CopyOptions;
import net.sf.saxon.om.NameOfNode;
import net.sf.saxon.om.NamespaceBinding;
import net.sf.saxon.om.NamespaceMap;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.om.TreeInfo;
import net.sf.saxon.pattern.AnyNodeTest;
import net.sf.saxon.pattern.NodeKindTest;
import net.sf.saxon.pattern.NodeTest;
import net.sf.saxon.pattern.NodeTestPattern;
import net.sf.saxon.pattern.Pattern;
import net.sf.saxon.pattern.SameNameTest;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.AxisIterator;
import net.sf.saxon.tree.iter.EmptyIterator;
import net.sf.saxon.tree.iter.ListIterator;
import net.sf.saxon.tree.iter.ReversibleIterator;
import net.sf.saxon.tree.iter.SingleNodeIterator;
import net.sf.saxon.tree.tiny.TinyElementImpl;
import net.sf.saxon.tree.tiny.TinyNodeImpl;
import net.sf.saxon.tree.tiny.TinyTextualElement;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.tree.wrapper.SiblingCountingNode;
import net.sf.saxon.tree.wrapper.VirtualCopy;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.ErrorType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.type.SimpleType;
import net.sf.saxon.type.UType;
import net.sf.saxon.type.Untyped;

public final class Navigator {
    private static int[] nodeCategories = new int[]{-1, 3, 2, 3, -1, -1, -1, 3, 3, 0, -1, -1, -1, 1};

    private Navigator() {
    }

    public static String getAttributeValue(NodeInfo element, String uri, String localName) {
        return element.getAttributeValue(uri, localName);
    }

    public static String getInheritedAttributeValue(NodeInfo element, String uri, String localName) {
        for (NodeInfo node = element; node != null; node = node.getParent()) {
            String value = node.getAttributeValue(uri, localName);
            if (value == null) {
                continue;
            }
            return value;
        }
        return null;
    }

    public static StructuredQName getNodeName(NodeInfo node) {
        if (node.getLocalPart() != null) {
            return new StructuredQName(node.getPrefix(), node.getURI(), node.getLocalPart());
        }
        return null;
    }

    public static NodeInfo getOutermostElement(TreeInfo doc) {
        return doc.getRootNode().iterateAxis(3, NodeKindTest.ELEMENT).next();
    }

    public static String getBaseURI(NodeInfo node) {
        return Navigator.getBaseURI(node, n -> {
            NodeInfo parent = n.getParent();
            return parent == null || !parent.getSystemId().equals(n.getSystemId());
        });
    }

    public static String getBaseURI(NodeInfo node, Predicate<NodeInfo> isTopElementWithinEntity) {
        String xmlBase;
        String string = xmlBase = node instanceof TinyElementImpl ? ((TinyElementImpl)node).getAttributeValue(385) : node.getAttributeValue("http://www.w3.org/XML/1998/namespace", "base");
        if (xmlBase != null) {
            URI baseURI;
            try {
                baseURI = new URI(xmlBase);
                if (!baseURI.isAbsolute()) {
                    NodeInfo parent = node.getParent();
                    if (parent == null) {
                        URI base = new URI(node.getSystemId());
                        URI resolved = xmlBase.isEmpty() ? base : base.resolve(baseURI);
                        return resolved.toString();
                    }
                    String startSystemId = node.getSystemId();
                    if (startSystemId == null) {
                        return null;
                    }
                    String parentSystemId = parent.getSystemId();
                    boolean isTopWithinEntity = false;
                    isTopWithinEntity = node instanceof TinyElementImpl ? ((TinyElementImpl)node).getTree().isTopWithinEntity(((TinyElementImpl)node).getNodeNumber()) : !startSystemId.equals(parentSystemId);
                    URI base = new URI(isTopElementWithinEntity.test(node) ? startSystemId : parent.getBaseURI());
                    baseURI = xmlBase.isEmpty() ? base : base.resolve(baseURI);
                }
            } catch (URISyntaxException e) {
                return xmlBase;
            }
            return baseURI.toString();
        }
        String startSystemId = node.getSystemId();
        if (startSystemId == null) {
            return null;
        }
        NodeInfo parent = node.getParent();
        if (parent == null) {
            return startSystemId;
        }
        String parentSystemId = parent.getSystemId();
        if (startSystemId.equals(parentSystemId) || parentSystemId.isEmpty()) {
            return parent.getBaseURI();
        }
        return startSystemId;
    }

    public static String getPath(NodeInfo node) {
        return Navigator.getPath(node, null);
    }

    public static String getPath(NodeInfo node, XPathContext context) {
        if (node == null) {
            return "";
        }
        boolean streamed = node.getConfiguration().isStreamedNode(node);
        NodeInfo parent = node.getParent();
        switch (node.getNodeKind()) {
            case 9: {
                return "/";
            }
            case 1: {
                if (parent == null) {
                    return node.getDisplayName();
                }
                String pre = Navigator.getPath(parent, context);
                if (pre.equals("/")) {
                    return '/' + node.getDisplayName();
                }
                return pre + '/' + node.getDisplayName() + (streamed ? "" : "[" + Navigator.getNumberSimple(node, context) + "]");
            }
            case 2: {
                return Navigator.getPath(parent, context) + "/@" + node.getDisplayName();
            }
            case 3: {
                String pre = Navigator.getPath(parent, context);
                return (pre.equals("/") ? "" : pre) + "/text()" + (streamed ? "" : "[" + Navigator.getNumberSimple(node, context) + "]");
            }
            case 8: {
                String pre = Navigator.getPath(parent, context);
                return (pre.equals("/") ? "" : pre) + "/comment()" + (streamed ? "" : "[" + Navigator.getNumberSimple(node, context) + "]");
            }
            case 7: {
                String pre = Navigator.getPath(parent, context);
                return (pre.equals("/") ? "" : pre) + "/processing-instruction()" + (streamed ? "" : "[" + Navigator.getNumberSimple(node, context) + "]");
            }
            case 13: {
                String test = node.getLocalPart();
                if (test.isEmpty()) {
                    test = "*[not(local-name()]";
                }
                return Navigator.getPath(parent, context) + "/namespace::" + test;
            }
        }
        return "";
    }

    public static AbsolutePath getAbsolutePath(NodeInfo node) {
        boolean streamed = node.getConfiguration().isStreamedNode(node);
        LinkedList<AbsolutePath.PathElement> path = new LinkedList<AbsolutePath.PathElement>();
        String sysId = node.getSystemId();
        while (node != null && node.getNodeKind() != 9) {
            path.add(0, new AbsolutePath.PathElement(node.getNodeKind(), NameOfNode.makeName(node), streamed ? -1 : Navigator.getNumberSimple(node, null)));
            node = node.getParent();
        }
        AbsolutePath a = new AbsolutePath(path);
        a.setSystemId(sysId);
        return a;
    }

    public static boolean haveSameName(NodeInfo n1, NodeInfo n2) {
        if (n1.hasFingerprint() && n2.hasFingerprint()) {
            return n1.getFingerprint() == n2.getFingerprint();
        }
        return n1.getLocalPart().equals(n2.getLocalPart()) && n1.getURI().equals(n2.getURI());
    }

    public static int getNumberSimple(NodeInfo node, XPathContext context) {
        NodeInfo prev;
        NodeTest same = node.getLocalPart().isEmpty() ? NodeKindTest.makeNodeKindTest(node.getNodeKind()) : new SameNameTest(node);
        Controller controller = context == null ? null : context.getController();
        AxisIterator preceding = node.iterateAxis(11, same);
        int i = 1;
        while ((prev = preceding.next()) != null) {
            int memo;
            if (controller != null && (memo = controller.getRememberedNumber(prev)) > 0) {
                controller.setRememberedNumber(node, memo += i);
                return memo;
            }
            ++i;
        }
        if (controller != null) {
            controller.setRememberedNumber(node, i);
        }
        return i;
    }

    public static int getNumberSingle(NodeInfo node, Pattern count, Pattern from, XPathContext context) throws XPathException {
        NodeInfo target;
        block9: {
            if (count == null && from == null) {
                return Navigator.getNumberSimple(node, context);
            }
            boolean knownToMatch = false;
            if (count == null) {
                count = node.getLocalPart().isEmpty() ? new NodeTestPattern(NodeKindTest.makeNodeKindTest(node.getNodeKind())) : new NodeTestPattern(new SameNameTest(node));
                knownToMatch = true;
            }
            target = node;
            if (!knownToMatch) {
                do {
                    if (count.matches(target, context)) {
                        if (from != null) {
                            NodeInfo anc = target;
                            while (!from.matches(anc, context)) {
                                if ((anc = anc.getParent()) != null) continue;
                                return 0;
                            }
                        }
                        break block9;
                    }
                    if (from == null || !from.matches(target, context)) continue;
                    return 0;
                } while ((target = target.getParent()) != null);
                return 0;
            }
        }
        AxisIterator preceding = target.iterateAxis(11, Navigator.getNodeTestForPattern(count));
        boolean alreadyChecked = count instanceof NodeTestPattern;
        int i = 1;
        NodeInfo p;
        while ((p = (NodeInfo)preceding.next()) != null) {
            if (!alreadyChecked && !count.matches(p, context)) continue;
            ++i;
        }
        return i;
    }

    public static int getNumberAny(Expression inst, NodeInfo node, Pattern count, Pattern from, XPathContext context, boolean hasVariablesInPatterns) throws XPathException {
        NodeInfo prev;
        Object[] memo;
        boolean memoise;
        NodeInfo memoNode = null;
        int memoNumber = 0;
        Controller controller = context.getController();
        assert (controller != null);
        boolean bl = memoise = !hasVariablesInPatterns && from == null;
        if (memoise && (memo = (Object[])controller.getUserData(inst.getLocation(), "xsl:number")) != null) {
            memoNode = (NodeInfo)memo[0];
            memoNumber = (Integer)memo[1];
        }
        int num = 0;
        if (count == null) {
            count = node.getLocalPart().isEmpty() ? new NodeTestPattern(NodeKindTest.makeNodeKindTest(node.getNodeKind())) : new NodeTestPattern(new SameNameTest(node));
            num = 1;
        } else if (count.matches(node, context)) {
            num = 1;
        }
        NodeTest filter = from == null ? Navigator.getNodeTestForPattern(count) : (from.getUType() == UType.ELEMENT && count.getUType() == UType.ELEMENT ? NodeKindTest.ELEMENT : AnyNodeTest.getInstance());
        if (from != null && from.matches(node, context)) {
            return num;
        }
        AxisIterator preceding = node.iterateAxis(13, filter);
        while ((prev = (NodeInfo)preceding.next()) != null) {
            if (count.matches(prev, context)) {
                if (num == 1 && prev.equals(memoNode)) {
                    num = memoNumber + 1;
                    break;
                }
                ++num;
            }
            if (from == null || !from.matches(prev, context)) continue;
            break;
        }
        if (memoise) {
            Object[] memo2 = new Object[]{node, num};
            controller.setUserData(inst.getLocation(), "xsl:number", memo2);
        }
        return num;
    }

    public static List<Long> getNumberMulti(NodeInfo node, Pattern count, Pattern from, XPathContext context) throws XPathException {
        ArrayList<Long> v = new ArrayList<Long>(5);
        if (count == null) {
            count = node.getLocalPart().isEmpty() ? new NodeTestPattern(NodeKindTest.makeNodeKindTest(node.getNodeKind())) : new NodeTestPattern(new SameNameTest(node));
        }
        NodeInfo curr = node;
        do {
            if (!count.matches(curr, context)) continue;
            int num = Navigator.getNumberSingle(curr, count, null, context);
            v.add(0, Long.valueOf(num));
        } while ((from == null || !from.matches(curr, context)) && (curr = curr.getParent()) != null);
        return v;
    }

    private static NodeTest getNodeTestForPattern(Pattern pattern) {
        ItemType type = pattern.getItemType();
        if (type instanceof NodeTest) {
            return (NodeTest)type;
        }
        if (pattern.getUType().overlaps(UType.ANY_NODE)) {
            return AnyNodeTest.getInstance();
        }
        return ErrorType.getInstance();
    }

    public static void copy(NodeInfo node, Receiver out, int copyOptions, Location locationId) throws XPathException {
        switch (node.getNodeKind()) {
            case 9: {
                out.startDocument(CopyOptions.getStartDocumentProperties(copyOptions));
                for (NodeInfo nodeInfo : node.children()) {
                    nodeInfo.copy(out, copyOptions, locationId);
                }
                out.endDocument();
                break;
            }
            case 1: {
                SchemaType annotation = (copyOptions & 4) != 0 ? node.getSchemaType() : Untyped.getInstance();
                NamespaceMap namespaceMap = CopyOptions.includes(copyOptions, 2) ? node.getAllNamespaces() : NamespaceMap.emptyMap();
                out.startElement(NameOfNode.makeName(node), annotation, node.attributes(), namespaceMap, locationId, 131136);
                for (NodeInfo nodeInfo : node.children()) {
                    nodeInfo.copy(out, copyOptions, locationId);
                }
                out.endElement();
                return;
            }
            case 2: {
                throw new IllegalArgumentException("Cannot copy attribute to Receiver");
            }
            case 3: {
                CharSequence value = node.getStringValueCS();
                if (value.length() != 0) {
                    out.characters(value, locationId, 0);
                }
                return;
            }
            case 8: {
                out.comment(node.getStringValueCS(), locationId, 0);
                return;
            }
            case 7: {
                out.processingInstruction(node.getLocalPart(), node.getStringValueCS(), locationId, 0);
                return;
            }
            case 13: {
                throw new IllegalArgumentException("Cannot copy namespace to Receiver");
            }
        }
    }

    public static void copy(NodeInfo node, Outputter out, int copyOptions, Location locationId) throws XPathException {
        boolean keepTypes = (copyOptions & 4) != 0;
        switch (node.getNodeKind()) {
            case 9: {
                out.startDocument(CopyOptions.getStartDocumentProperties(copyOptions));
                for (NodeInfo nodeInfo : node.children()) {
                    Navigator.copy(nodeInfo, out, copyOptions, locationId);
                }
                out.endDocument();
                break;
            }
            case 1: {
                SchemaType annotation = keepTypes ? node.getSchemaType() : Untyped.getInstance();
                out.startElement(NameOfNode.makeName(node), annotation, locationId, 192);
                if ((copyOptions & 2) != 0) {
                    for (NamespaceBinding namespaceBinding : node.getAllNamespaces()) {
                        out.namespace(namespaceBinding.getPrefix(), namespaceBinding.getURI(), 0);
                    }
                }
                for (AttributeInfo attributeInfo : node.attributes()) {
                    SimpleType attType = keepTypes ? attributeInfo.getType() : BuiltInAtomicType.UNTYPED_ATOMIC;
                    out.attribute(attributeInfo.getNodeName(), attType, attributeInfo.getValue(), attributeInfo.getLocation(), attributeInfo.getProperties());
                }
                for (NodeInfo nodeInfo : node.children()) {
                    Navigator.copy(nodeInfo, out, copyOptions, locationId);
                }
                out.endElement();
                return;
            }
            case 2: {
                BuiltInAtomicType attType = keepTypes ? (SimpleType)node.getSchemaType() : BuiltInAtomicType.UNTYPED_ATOMIC;
                out.attribute(NameOfNode.makeName(node), attType, node.getStringValueCS(), locationId, 0);
                return;
            }
            case 3: {
                CharSequence value = node.getStringValueCS();
                if (value.length() != 0) {
                    out.characters(value, locationId, 0);
                }
                return;
            }
            case 8: {
                out.comment(node.getStringValueCS(), locationId, 0);
                return;
            }
            case 7: {
                out.processingInstruction(node.getLocalPart(), node.getStringValueCS(), locationId, 0);
                return;
            }
            case 13: {
                out.namespace(node.getLocalPart(), node.getStringValue(), 0);
                return;
            }
        }
    }

    public static int compareOrder(SiblingCountingNode first, SiblingCountingNode second) {
        NodeInfo p1;
        if (first.equals(second)) {
            return 0;
        }
        NodeInfo firstParent = first.getParent();
        if (firstParent == null) {
            return -1;
        }
        NodeInfo secondParent = second.getParent();
        if (secondParent == null) {
            return 1;
        }
        if (firstParent.equals(secondParent)) {
            int cat2;
            int cat1 = nodeCategories[first.getNodeKind()];
            if (cat1 == (cat2 = nodeCategories[second.getNodeKind()])) {
                return first.getSiblingPosition() - second.getSiblingPosition();
            }
            return cat1 - cat2;
        }
        int depth1 = 0;
        int depth2 = 0;
        NodeInfo p2 = second;
        for (p1 = first; p1 != null; p1 = p1.getParent()) {
            ++depth1;
        }
        while (p2 != null) {
            ++depth2;
            p2 = p2.getParent();
        }
        p1 = first;
        while (depth1 > depth2) {
            p1 = p1.getParent();
            assert (p1 != null);
            if (p1.equals(second)) {
                return 1;
            }
            --depth1;
        }
        p2 = second;
        while (depth2 > depth1) {
            p2 = p2.getParent();
            assert (p2 != null);
            if (p2.equals(first)) {
                return -1;
            }
            --depth2;
        }
        while (true) {
            NodeInfo par1 = p1.getParent();
            NodeInfo par2 = p2.getParent();
            if (par1 == null || par2 == null) {
                throw new NullPointerException("Node order comparison - internal error");
            }
            if (par1.equals(par2)) {
                if (p1.getNodeKind() == 2 && p2.getNodeKind() != 2) {
                    return -1;
                }
                if (p1.getNodeKind() != 2 && p2.getNodeKind() == 2) {
                    return 1;
                }
                return ((SiblingCountingNode)p1).getSiblingPosition() - ((SiblingCountingNode)p2).getSiblingPosition();
            }
            p1 = par1;
            p2 = par2;
        }
    }

    public static int comparePosition(NodeInfo first, NodeInfo second) {
        NodeInfo p1;
        if (first.getNodeKind() == 2 || first.getNodeKind() == 13 || second.getNodeKind() == 2 || second.getNodeKind() == 13) {
            throw new UnsupportedOperationException();
        }
        if (first.equals(second)) {
            return 12;
        }
        NodeInfo firstParent = first.getParent();
        if (firstParent == null) {
            return 0;
        }
        NodeInfo secondParent = second.getParent();
        if (secondParent == null) {
            return 4;
        }
        if (firstParent.equals(secondParent)) {
            if (first.compareOrder(second) < 0) {
                return 10;
            }
            return 6;
        }
        int depth1 = 0;
        int depth2 = 0;
        NodeInfo p2 = second;
        for (p1 = first; p1 != null; p1 = p1.getParent()) {
            ++depth1;
        }
        while (p2 != null) {
            ++depth2;
            p2 = p2.getParent();
        }
        p1 = first;
        while (depth1 > depth2) {
            p1 = p1.getParent();
            assert (p1 != null);
            if (p1.equals(second)) {
                return 4;
            }
            --depth1;
        }
        p2 = second;
        while (depth2 > depth1) {
            p2 = p2.getParent();
            assert (p2 != null);
            if (p2.equals(first)) {
                return 0;
            }
            --depth2;
        }
        if (first.compareOrder(second) < 0) {
            return 10;
        }
        return 6;
    }

    public static void appendSequentialKey(SiblingCountingNode node, FastStringBuffer sb, boolean addDocNr) {
        if (addDocNr) {
            sb.cat('w');
            sb.append(Long.toString(node.getTreeInfo().getDocumentNumber()));
        }
        if (node.getNodeKind() != 9) {
            NodeInfo parent = node.getParent();
            if (parent != null) {
                Navigator.appendSequentialKey((SiblingCountingNode)parent, sb, false);
            }
            if (node.getNodeKind() == 2) {
                sb.cat('A');
            }
        }
        sb.append(Navigator.alphaKey(node.getSiblingPosition()));
    }

    public static String alphaKey(int value) {
        if (value < 1) {
            return "a";
        }
        if (value < 10) {
            return "b" + value;
        }
        if (value < 100) {
            return "c" + value;
        }
        if (value < 1000) {
            return "d" + value;
        }
        if (value < 10000) {
            return "e" + value;
        }
        if (value < 100000) {
            return "f" + value;
        }
        if (value < 1000000) {
            return "g" + value;
        }
        if (value < 10000000) {
            return "h" + value;
        }
        if (value < 100000000) {
            return "i" + value;
        }
        if (value < 1000000000) {
            return "j" + value;
        }
        return "k" + value;
    }

    public static boolean isAncestorOrSelf(NodeInfo a, NodeInfo d) {
        int k = a.getNodeKind();
        if (k != 1 && k != 9) {
            return a.equals(d);
        }
        if (a instanceof TinyNodeImpl) {
            if (d instanceof TinyNodeImpl) {
                return ((TinyNodeImpl)a).isAncestorOrSelf((TinyNodeImpl)d);
            }
            if (d instanceof TinyTextualElement.TinyTextualElementText) {
                return a.equals(d) || Navigator.isAncestorOrSelf(a, d.getParent());
            }
            if (d.getNodeKind() != 13 && !(d instanceof VirtualCopy)) {
                return false;
            }
        }
        for (NodeInfo p = d; p != null; p = p.getParent()) {
            if (!a.equals(p)) continue;
            return true;
        }
        return false;
    }

    public static AxisIterator filteredSingleton(NodeInfo node, Predicate<? super NodeInfo> nodeTest) {
        if (node != null && nodeTest.test(node)) {
            return SingleNodeIterator.makeIterator(node);
        }
        return EmptyIterator.ofNodes();
    }

    public static int getSiblingPosition(NodeInfo node, NodeTest nodeTest, int max) {
        if (node instanceof SiblingCountingNode && nodeTest instanceof AnyNodeTest) {
            return ((SiblingCountingNode)node).getSiblingPosition();
        }
        AxisIterator prev = node.iterateAxis(11, nodeTest);
        int count = 1;
        while (prev.next() != null) {
            if (++count <= max) continue;
            return count;
        }
        return count;
    }

    public static final class PrecedingEnumeration
    implements AxisIterator {
        private AxisIterator ancestorEnum;
        private AxisIterator siblingEnum;
        private AxisIterator descendEnum = null;
        private boolean includeAncestors;

        public PrecedingEnumeration(NodeInfo start, boolean includeAncestors) {
            this.includeAncestors = includeAncestors;
            this.ancestorEnum = new AncestorEnumeration(start, false);
            switch (start.getNodeKind()) {
                case 1: 
                case 3: 
                case 7: 
                case 8: {
                    this.siblingEnum = start.iterateAxis(11);
                    break;
                }
                default: {
                    this.siblingEnum = EmptyIterator.ofNodes();
                }
            }
        }

        @Override
        public final NodeInfo next() {
            NodeInfo nexta;
            if (this.descendEnum != null) {
                NodeInfo nextd = this.descendEnum.next();
                if (nextd != null) {
                    return nextd;
                }
                this.descendEnum = null;
            }
            if (this.siblingEnum != null) {
                NodeInfo nexts = this.siblingEnum.next();
                if (nexts != null) {
                    if (nexts.hasChildNodes()) {
                        this.descendEnum = new DescendantEnumeration(nexts, true, false);
                        return this.next();
                    }
                    this.descendEnum = null;
                    return nexts;
                }
                this.descendEnum = null;
                this.siblingEnum = null;
            }
            if ((nexta = this.ancestorEnum.next()) != null) {
                this.siblingEnum = nexta.getNodeKind() == 9 ? EmptyIterator.ofNodes() : nexta.iterateAxis(11);
                if (!this.includeAncestors) {
                    return this.next();
                }
                return nexta;
            }
            return null;
        }
    }

    public static final class FollowingEnumeration
    implements AxisIterator {
        private AxisIterator ancestorEnum;
        private AxisIterator siblingEnum;
        private AxisIterator descendEnum = null;

        public FollowingEnumeration(NodeInfo start) {
            this.ancestorEnum = new AncestorEnumeration(start, false);
            switch (start.getNodeKind()) {
                case 1: 
                case 3: 
                case 7: 
                case 8: {
                    this.siblingEnum = start.iterateAxis(7);
                    break;
                }
                case 2: 
                case 13: {
                    NodeInfo parent = start.getParent();
                    if (parent == null) {
                        this.siblingEnum = EmptyIterator.ofNodes();
                        break;
                    }
                    this.siblingEnum = parent.iterateAxis(3);
                    break;
                }
                default: {
                    this.siblingEnum = EmptyIterator.ofNodes();
                }
            }
        }

        @Override
        public final NodeInfo next() {
            NodeInfo nexta;
            if (this.descendEnum != null) {
                NodeInfo nextd = this.descendEnum.next();
                if (nextd != null) {
                    return nextd;
                }
                this.descendEnum = null;
            }
            if (this.siblingEnum != null) {
                NodeInfo nexts = this.siblingEnum.next();
                if (nexts != null) {
                    this.descendEnum = nexts.hasChildNodes() ? new DescendantEnumeration(nexts, false, true) : null;
                    return nexts;
                }
                this.descendEnum = null;
                this.siblingEnum = null;
            }
            if ((nexta = this.ancestorEnum.next()) != null) {
                this.siblingEnum = nexta.getNodeKind() == 9 ? EmptyIterator.ofNodes() : nexta.iterateAxis(7);
                return this.next();
            }
            return null;
        }
    }

    public static final class DescendantEnumeration
    implements AxisIterator {
        private AxisIterator children = null;
        private AxisIterator descendants = null;
        private NodeInfo start;
        private boolean includeSelf;
        private boolean forwards;
        private boolean atEnd = false;

        public DescendantEnumeration(NodeInfo start, boolean includeSelf, boolean forwards) {
            this.start = start;
            this.includeSelf = includeSelf;
            this.forwards = forwards;
        }

        @Override
        public final NodeInfo next() {
            if (this.descendants != null) {
                NodeInfo nextd = this.descendants.next();
                if (nextd != null) {
                    return nextd;
                }
                this.descendants = null;
            }
            if (this.children != null) {
                NodeInfo n = this.children.next();
                if (n != null) {
                    if (n.hasChildNodes()) {
                        if (this.forwards) {
                            this.descendants = new DescendantEnumeration(n, false, true);
                            return n;
                        }
                        this.descendants = new DescendantEnumeration(n, true, false);
                        return this.next();
                    }
                    return n;
                }
                if (this.forwards || !this.includeSelf) {
                    return null;
                }
                this.atEnd = true;
                this.children = null;
                return this.start;
            }
            if (this.atEnd) {
                return null;
            }
            if (this.start.hasChildNodes()) {
                this.children = this.start.iterateAxis(3);
                if (!this.forwards) {
                    if (this.children instanceof ReversibleIterator) {
                        this.children = (AxisIterator)((ReversibleIterator)((Object)this.children)).getReverseIterator();
                    } else {
                        NodeInfo n;
                        LinkedList<NodeInfo> list = new LinkedList<NodeInfo>();
                        AxisIterator forwards = this.start.iterateAxis(3);
                        while ((n = forwards.next()) != null) {
                            list.addFirst(n);
                        }
                        this.children = new ListIterator.OfNodes((List<NodeInfo>)list);
                    }
                }
            } else {
                this.children = EmptyIterator.ofNodes();
            }
            if (this.forwards && this.includeSelf) {
                return this.start;
            }
            return this.next();
        }

        public void advance() {
        }
    }

    public static final class AncestorEnumeration
    implements AxisIterator {
        private boolean includeSelf;
        private boolean atStart;
        private NodeInfo current;

        public AncestorEnumeration(NodeInfo start, boolean includeSelf) {
            this.includeSelf = includeSelf;
            this.current = start;
            this.atStart = true;
        }

        @Override
        public final NodeInfo next() {
            if (this.atStart) {
                this.atStart = false;
                if (this.includeSelf) {
                    return this.current;
                }
            }
            this.current = this.current == null ? null : this.current.getParent();
            return this.current;
        }
    }

    public static class EmptyTextFilter
    implements AxisIterator {
        private AxisIterator base;

        public EmptyTextFilter(AxisIterator base) {
            this.base = base;
        }

        @Override
        public NodeInfo next() {
            NodeInfo next;
            do {
                if ((next = this.base.next()) != null) continue;
                return null;
            } while (next.getNodeKind() == 3 && next.getStringValueCS().length() == 0);
            return next;
        }
    }

    public static class AxisFilter
    implements AxisIterator {
        private AxisIterator base;
        private Predicate<? super NodeInfo> nodeTest;

        public AxisFilter(AxisIterator base, Predicate<? super NodeInfo> test) {
            this.base = base;
            this.nodeTest = test;
        }

        @Override
        public NodeInfo next() {
            NodeInfo next;
            do {
                if ((next = this.base.next()) != null) continue;
                return null;
            } while (!this.nodeTest.test(next));
            return next;
        }
    }
}

