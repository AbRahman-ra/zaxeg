/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.om;

import java.util.Collections;
import java.util.function.Predicate;
import javax.xml.transform.Source;
import net.sf.saxon.Configuration;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.expr.parser.Loc;
import net.sf.saxon.om.AtomicSequence;
import net.sf.saxon.om.AttributeInfo;
import net.sf.saxon.om.AttributeMap;
import net.sf.saxon.om.EmptyAttributeMap;
import net.sf.saxon.om.Genre;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NameOfNode;
import net.sf.saxon.om.NamespaceBinding;
import net.sf.saxon.om.NamespaceMap;
import net.sf.saxon.om.TreeInfo;
import net.sf.saxon.pattern.AnyNodeTest;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.trans.Err;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.AxisIterator;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.tree.util.Navigator;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.type.SimpleType;
import net.sf.saxon.type.Untyped;

public interface NodeInfo
extends Source,
Item,
Location {
    public static final int IS_DTD_TYPE = 0x40000000;
    public static final int IS_NILLED = 0x20000000;

    public TreeInfo getTreeInfo();

    default public Configuration getConfiguration() {
        return this.getTreeInfo().getConfiguration();
    }

    public int getNodeKind();

    default public boolean isSameNodeInfo(NodeInfo other) {
        return this.equals(other);
    }

    public boolean equals(Object var1);

    public int hashCode();

    @Override
    public String getSystemId();

    @Override
    default public String getPublicId() {
        return null;
    }

    public String getBaseURI();

    @Override
    default public int getLineNumber() {
        return -1;
    }

    @Override
    default public int getColumnNumber() {
        return -1;
    }

    public int compareOrder(NodeInfo var1);

    @Override
    public String getStringValue();

    public boolean hasFingerprint();

    public int getFingerprint();

    public String getLocalPart();

    public String getURI();

    public String getDisplayName();

    public String getPrefix();

    default public SchemaType getSchemaType() {
        switch (this.getNodeKind()) {
            case 2: {
                return BuiltInAtomicType.UNTYPED_ATOMIC;
            }
            case 1: 
            case 9: {
                return Untyped.getInstance();
            }
        }
        return null;
    }

    @Override
    public AtomicSequence atomize() throws XPathException;

    public NodeInfo getParent();

    default public AxisIterator iterateAxis(int axisNumber) {
        return this.iterateAxis(axisNumber, AnyNodeTest.getInstance());
    }

    public AxisIterator iterateAxis(int var1, Predicate<? super NodeInfo> var2);

    public String getAttributeValue(String var1, String var2);

    public NodeInfo getRoot();

    public boolean hasChildNodes();

    default public Iterable<? extends NodeInfo> children() {
        if (this.hasChildNodes()) {
            NodeInfo parent = this;
            return () -> parent.iterateAxis(3).asIterator();
        }
        return Collections.emptyList();
    }

    default public Iterable<? extends NodeInfo> children(Predicate<? super NodeInfo> filter) {
        if (this.hasChildNodes()) {
            NodeInfo parent = this;
            return () -> parent.iterateAxis(3, filter).asIterator();
        }
        return Collections.emptyList();
    }

    default public AttributeMap attributes() {
        AttributeMap atts = EmptyAttributeMap.getInstance();
        if (this.getNodeKind() == 1) {
            NodeInfo attr;
            AxisIterator iter = this.iterateAxis(2);
            while ((attr = iter.next()) != null) {
                atts = atts.put(new AttributeInfo(NameOfNode.makeName(attr), (SimpleType)attr.getSchemaType(), attr.getStringValue(), Loc.NONE, 0));
            }
        }
        return atts;
    }

    public void generateId(FastStringBuffer var1);

    default public void copy(Receiver out, int copyOptions, Location locationId) throws XPathException {
        Navigator.copy(this, out, copyOptions, locationId);
    }

    public NamespaceBinding[] getDeclaredNamespaces(NamespaceBinding[] var1);

    public NamespaceMap getAllNamespaces();

    default public boolean isId() {
        return false;
    }

    default public boolean isIdref() {
        return false;
    }

    default public boolean isNilled() {
        return false;
    }

    @Override
    default public boolean isStreamed() {
        return false;
    }

    @Override
    default public String toShortString() {
        switch (this.getNodeKind()) {
            case 9: {
                return "document-node()";
            }
            case 1: {
                return "<" + this.getDisplayName() + "/>";
            }
            case 2: {
                return "@" + this.getDisplayName();
            }
            case 3: {
                return "text(\"" + Err.truncate30(this.getStringValue()) + "\")";
            }
            case 8: {
                return "<!--" + Err.truncate30(this.getStringValue()) + "-->";
            }
            case 7: {
                return "<?" + this.getDisplayName() + "?>";
            }
            case 13: {
                String prefix = this.getLocalPart();
                return "xmlns" + (prefix.equals("") ? "" : ":" + prefix) + "=\"" + this.getStringValue() + '\"';
            }
        }
        return "";
    }

    @Override
    default public Genre getGenre() {
        return Genre.NODE;
    }
}

