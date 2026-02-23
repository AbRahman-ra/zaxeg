/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;
import net.sf.saxon.Configuration;
import net.sf.saxon.event.Outputter;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.PendingUpdateList;
import net.sf.saxon.expr.StringLiteral;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.PathMap;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.functions.hof.FunctionLiteral;
import net.sf.saxon.ma.arrays.ArrayItem;
import net.sf.saxon.ma.map.KeyValuePair;
import net.sf.saxon.ma.map.MapItem;
import net.sf.saxon.om.AtomicSequence;
import net.sf.saxon.om.Function;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NameOfNode;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.SequenceTool;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.pattern.NodeTestPattern;
import net.sf.saxon.pattern.Pattern;
import net.sf.saxon.query.QueryResult;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.UnfailingIterator;
import net.sf.saxon.type.AlphaCode;
import net.sf.saxon.type.AtomicType;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.ErrorType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.TypeHierarchy;
import net.sf.saxon.type.UType;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.BooleanValue;
import net.sf.saxon.value.EmptySequence;
import net.sf.saxon.value.ExternalObject;
import net.sf.saxon.value.Int64Value;
import net.sf.saxon.value.IntegerRange;
import net.sf.saxon.value.IntegerValue;
import net.sf.saxon.value.QualifiedNameValue;
import net.sf.saxon.value.SequenceExtent;
import net.sf.saxon.value.StringValue;

public class Literal
extends Expression {
    private GroundedValue value;

    protected Literal(GroundedValue value) {
        this.value = value.reduce();
    }

    public static Literal makeStringsLiteral(List<String> strings) {
        ArrayList<StringValue> values = new ArrayList<StringValue>();
        for (String s : strings) {
            values.add(new StringValue(s));
        }
        GroundedValue gv = SequenceExtent.makeSequenceExtent(values);
        return Literal.makeLiteral(gv);
    }

    public GroundedValue getValue() {
        return this.value;
    }

    @Override
    public Expression typeCheck(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        return this;
    }

    @Override
    public Expression optimize(ExpressionVisitor visitor, ContextItemStaticInfo contextItemType) throws XPathException {
        return this;
    }

    @Override
    public int getNetCost() {
        return 0;
    }

    @Override
    public ItemType getItemType() {
        if (this.value instanceof AtomicValue) {
            return ((AtomicValue)this.value).getItemType();
        }
        if (this.value.getLength() == 0) {
            return ErrorType.getInstance();
        }
        TypeHierarchy th = this.getConfiguration().getTypeHierarchy();
        return SequenceTool.getItemType(this.value, th);
    }

    @Override
    public UType getStaticUType(UType contextItemType) {
        if (this.value.getLength() == 0) {
            return UType.VOID;
        }
        if (this.value instanceof AtomicValue) {
            return ((AtomicValue)this.value).getUType();
        }
        if (this.value instanceof Function) {
            return UType.FUNCTION;
        }
        return super.getStaticUType(contextItemType);
    }

    @Override
    public int computeCardinality() {
        if (this.value.getLength() == 0) {
            return 8192;
        }
        if (this.value instanceof AtomicValue) {
            return 16384;
        }
        try {
            UnfailingIterator iter = this.value.iterate();
            Item next = iter.next();
            if (next == null) {
                return 8192;
            }
            if (iter.next() != null) {
                return 32768;
            }
            return 16384;
        } catch (XPathException err) {
            return 57344;
        }
    }

    @Override
    public int computeSpecialProperties() {
        if (this.value.getLength() == 0) {
            return 0xDFF0000;
        }
        return 0x800000;
    }

    @Override
    public IntegerValue[] getIntegerBounds() {
        if (this.value instanceof IntegerValue) {
            return new IntegerValue[]{(IntegerValue)this.value, (IntegerValue)this.value};
        }
        if (this.value instanceof IntegerRange) {
            return new IntegerValue[]{Int64Value.makeIntegerValue(((IntegerRange)this.value).getStart()), Int64Value.makeIntegerValue(((IntegerRange)this.value).getEnd())};
        }
        return null;
    }

    @Override
    public boolean isVacuousExpression() {
        return this.value.getLength() == 0;
    }

    @Override
    public Expression copy(RebindingMap rebindings) {
        Literal l2 = new Literal(this.value);
        ExpressionTool.copyLocationInfo(this, l2);
        return l2;
    }

    @Override
    public Pattern toPattern(Configuration config) throws XPathException {
        if (Literal.isEmptySequence(this)) {
            return new NodeTestPattern(ErrorType.getInstance());
        }
        return super.toPattern(config);
    }

    @Override
    public PathMap.PathMapNodeSet addToPathMap(PathMap pathMap, PathMap.PathMapNodeSet pathMapNodeSet) {
        return pathMapNodeSet;
    }

    @Override
    public final int getDependencies() {
        return 0;
    }

    @Override
    public SequenceIterator iterate(XPathContext context) throws XPathException {
        return this.value.iterate();
    }

    public SequenceIterator iterate() throws XPathException {
        return this.value.iterate();
    }

    @Override
    public Item evaluateItem(XPathContext context) throws XPathException {
        return this.value.head();
    }

    @Override
    public void process(Outputter output, XPathContext context) throws XPathException {
        if (this.value instanceof Item) {
            output.append((Item)this.value, this.getLocation(), 524288);
        } else {
            this.value.iterate().forEachOrFail(it -> output.append(it, this.getLocation(), 524288));
        }
    }

    @Override
    public int getImplementationMethod() {
        return 7;
    }

    @Override
    public CharSequence evaluateAsString(XPathContext context) throws XPathException {
        AtomicValue value = (AtomicValue)this.evaluateItem(context);
        if (value == null) {
            return "";
        }
        return value.getStringValueCS();
    }

    @Override
    public boolean effectiveBooleanValue(XPathContext context) throws XPathException {
        return this.value.effectiveBooleanValue();
    }

    @Override
    public void evaluatePendingUpdates(XPathContext context, PendingUpdateList pul) throws XPathException {
        if (this.value.getLength() != 0) {
            super.evaluatePendingUpdates(context, pul);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Literal)) {
            return false;
        }
        GroundedValue v0 = this.value;
        GroundedValue v1 = ((Literal)obj).value;
        try {
            block9: {
                UnfailingIterator i0 = v0.iterate();
                UnfailingIterator i1 = v1.iterate();
                while (true) {
                    Item m0 = i0.next();
                    Item m1 = i1.next();
                    if (m0 == null && m1 == null) {
                        return true;
                    }
                    if (m0 == null || m1 == null) {
                        return false;
                    }
                    if (m0 == m1) continue;
                    boolean n0 = m0 instanceof NodeInfo;
                    boolean n1 = m1 instanceof NodeInfo;
                    if (n0 != n1) {
                        return false;
                    }
                    if (n0) {
                        if (m0.equals(m1)) continue;
                        return false;
                    }
                    boolean a0 = m0 instanceof AtomicValue;
                    boolean a1 = m1 instanceof AtomicValue;
                    if (a0 != a1) {
                        return false;
                    }
                    if (!a0) break block9;
                    if (!((AtomicValue)m0).isIdentical((AtomicValue)m1) || ((AtomicValue)m0).getItemType() != ((AtomicValue)m1).getItemType()) break;
                }
                return false;
            }
            return false;
        } catch (XPathException err) {
            return false;
        }
    }

    @Override
    public int computeHashCode() {
        if (this.value instanceof AtomicSequence) {
            return ((AtomicSequence)this.value).getSchemaComparable().hashCode();
        }
        return super.computeHashCode();
    }

    @Override
    public String toString() {
        return this.value.toString();
    }

    @Override
    public void export(ExpressionPresenter out) throws XPathException {
        Literal.exportValue(this.value, out);
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public static void exportValue(Sequence value, ExpressionPresenter out) throws XPathException {
        block20: {
            block22: {
                int nodeKind;
                block21: {
                    if (value.head() == null) {
                        out.startElement("empty");
                        out.endElement();
                        return;
                    }
                    if (value instanceof AtomicValue) {
                        Literal.exportAtomicValue((AtomicValue)value, out);
                        return;
                    }
                    if (value instanceof IntegerRange) {
                        out.startElement("range");
                        out.emitAttribute("from", "" + ((IntegerRange)value).getStart());
                        out.emitAttribute("to", "" + ((IntegerRange)value).getEnd());
                        out.endElement();
                        return;
                    }
                    if (!(value instanceof NodeInfo)) break block20;
                    out.startElement("node");
                    nodeKind = ((NodeInfo)value).getNodeKind();
                    out.emitAttribute("kind", nodeKind + "");
                    if (!((ExpressionPresenter.ExportOptions)out.getOptions()).explaining) break block21;
                    String name = ((NodeInfo)value).getDisplayName();
                    if (name.isEmpty()) break block22;
                    out.emitAttribute("name", name);
                    break block22;
                }
                switch (nodeKind) {
                    case 1: 
                    case 9: {
                        StringWriter sw = new StringWriter();
                        Properties props = new Properties();
                        props.setProperty("method", "xml");
                        props.setProperty("indent", "no");
                        props.setProperty("omit-xml-declaration", "yes");
                        QueryResult.serialize((NodeInfo)value, (Result)new StreamResult(sw), props);
                        out.emitAttribute("content", sw.toString());
                        out.emitAttribute("baseUri", ((NodeInfo)value).getBaseURI());
                        break;
                    }
                    case 3: 
                    case 8: {
                        out.emitAttribute("content", ((NodeInfo)value).getStringValue());
                        break;
                    }
                    case 2: 
                    case 7: 
                    case 13: {
                        StructuredQName name = NameOfNode.makeName((NodeInfo)value).getStructuredQName();
                        if (!name.getLocalPart().isEmpty()) {
                            out.emitAttribute("localName", name.getLocalPart());
                        }
                        if (!name.getPrefix().isEmpty()) {
                            out.emitAttribute("prefix", name.getPrefix());
                        }
                        if (!name.getURI().isEmpty()) {
                            out.emitAttribute("ns", name.getURI());
                        }
                        out.emitAttribute("content", ((NodeInfo)value).getStringValue());
                        break;
                    }
                    default: {
                        assert (false);
                        break;
                    }
                }
            }
            out.endElement();
            return;
        }
        if (value instanceof MapItem) {
            out.startElement("map");
            out.emitAttribute("size", "" + ((MapItem)value).size());
            for (KeyValuePair kvp : ((MapItem)value).keyValuePairs()) {
                Literal.exportAtomicValue(kvp.key, out);
                Literal.exportValue(kvp.value, out);
            }
            out.endElement();
            return;
        } else if (value instanceof Function) {
            ((Function)value).export(out);
            return;
        } else if (value instanceof ExternalObject) {
            if (!((ExpressionPresenter.ExportOptions)out.getOptions()).explaining) throw new XPathException("Cannot export a stylesheet containing literal values bound to external Java objects");
            out.startElement("externalObject");
            out.emitAttribute("class", ((ExternalObject)value).getObject().getClass().getName());
            out.endElement();
            return;
        } else {
            out.startElement("literal");
            if (value instanceof GroundedValue) {
                out.emitAttribute("count", ((GroundedValue)value).getLength() + "");
            }
            value.iterate().forEachOrFail(it -> Literal.exportValue(it, out));
            out.endElement();
        }
    }

    @Override
    public String getExpressionName() {
        return "literal";
    }

    public static void exportAtomicValue(AtomicValue value, ExpressionPresenter out) throws XPathException {
        if ("JS".equals(((ExpressionPresenter.ExportOptions)out.getOptions()).target)) {
            value.checkValidInJavascript();
        }
        AtomicType type = value.getItemType();
        String val = value.getStringValue();
        if (type.equals(BuiltInAtomicType.STRING)) {
            out.startElement("str");
            out.emitAttribute("val", val);
            out.endElement();
        } else if (type.equals(BuiltInAtomicType.INTEGER)) {
            out.startElement("int");
            out.emitAttribute("val", val);
            out.endElement();
        } else if (type.equals(BuiltInAtomicType.DECIMAL)) {
            out.startElement("dec");
            out.emitAttribute("val", val);
            out.endElement();
        } else if (type.equals(BuiltInAtomicType.DOUBLE)) {
            out.startElement("dbl");
            out.emitAttribute("val", val);
            out.endElement();
        } else if (type.equals(BuiltInAtomicType.BOOLEAN)) {
            out.startElement(((BooleanValue)value).effectiveBooleanValue() ? "true" : "false");
            out.endElement();
        } else if (value instanceof QualifiedNameValue) {
            out.startElement("qName");
            out.emitAttribute("pre", ((QualifiedNameValue)value).getPrefix());
            out.emitAttribute("uri", ((QualifiedNameValue)value).getNamespaceURI());
            out.emitAttribute("loc", ((QualifiedNameValue)value).getLocalName());
            if (!type.equals(BuiltInAtomicType.QNAME)) {
                out.emitAttribute("type", type.getEQName());
            }
            out.endElement();
        } else {
            out.startElement("atomic");
            out.emitAttribute("val", val);
            out.emitAttribute("type", AlphaCode.fromItemType(type));
            out.endElement();
        }
    }

    @Override
    public String toShortString() {
        if (this.value.getLength() == 0) {
            return "()";
        }
        if (this.value.getLength() == 1) {
            return this.value.toShortString();
        }
        return "(" + this.value.head().toShortString() + ", ...{" + this.value.getLength() + "})";
    }

    public static boolean isAtomic(Expression exp) {
        return exp instanceof Literal && ((Literal)exp).getValue() instanceof AtomicValue;
    }

    public static boolean isEmptySequence(Expression exp) {
        return exp instanceof Literal && ((Literal)exp).getValue().getLength() == 0;
    }

    public static boolean isConstantBoolean(Expression exp, boolean value) {
        if (exp instanceof Literal) {
            GroundedValue b = ((Literal)exp).getValue();
            return b instanceof BooleanValue && ((BooleanValue)b).getBooleanValue() == value;
        }
        return false;
    }

    public static boolean hasEffectiveBooleanValue(Expression exp, boolean value) {
        if (exp instanceof Literal) {
            try {
                return value == ((Literal)exp).getValue().effectiveBooleanValue();
            } catch (XPathException err) {
                return false;
            }
        }
        return false;
    }

    public static boolean isConstantOne(Expression exp) {
        if (exp instanceof Literal) {
            GroundedValue v = ((Literal)exp).getValue();
            return v instanceof Int64Value && ((Int64Value)v).longValue() == 1L;
        }
        return false;
    }

    @Override
    public boolean isSubtreeExpression() {
        return true;
    }

    public static Literal makeEmptySequence() {
        return new Literal(EmptySequence.getInstance());
    }

    public static <T extends Item> Literal makeLiteral(GroundedValue value) {
        if ((value = value.reduce()) instanceof StringValue) {
            return new StringLiteral((StringValue)value);
        }
        if (value instanceof Function && !(value instanceof MapItem) && !(value instanceof ArrayItem)) {
            return new FunctionLiteral((Function)value);
        }
        return new Literal(value);
    }

    public static Literal makeLiteral(GroundedValue value, Expression origin) {
        Literal lit = Literal.makeLiteral(value);
        ExpressionTool.copyLocationInfo(origin, lit);
        return lit;
    }

    @Override
    public String getStreamerName() {
        return "Literal";
    }
}

