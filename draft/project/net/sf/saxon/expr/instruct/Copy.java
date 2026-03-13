/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.instruct;

import java.util.Iterator;
import net.sf.saxon.Configuration;
import net.sf.saxon.Controller;
import net.sf.saxon.event.ComplexContentOutputter;
import net.sf.saxon.event.NoOpenStartTagException;
import net.sf.saxon.event.Outputter;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.event.SequenceCollector;
import net.sf.saxon.expr.ContextItemExpression;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.InstanceOfExpression;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.Operand;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.instruct.Choose;
import net.sf.saxon.expr.instruct.CopyOf;
import net.sf.saxon.expr.instruct.ElementCreator;
import net.sf.saxon.expr.instruct.TailCall;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.lib.ParseOptions;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NameOfNode;
import net.sf.saxon.om.NamespaceBinding;
import net.sf.saxon.om.NoElementsSpaceStrippingRule;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.pattern.AnyNodeTest;
import net.sf.saxon.pattern.ContentTypeTest;
import net.sf.saxon.pattern.MultipleNodeKindTest;
import net.sf.saxon.pattern.NodeKindTest;
import net.sf.saxon.pattern.NodeTest;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.Affinity;
import net.sf.saxon.type.AnyItemType;
import net.sf.saxon.type.AnySimpleType;
import net.sf.saxon.type.AnyType;
import net.sf.saxon.type.AtomicType;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.ComplexType;
import net.sf.saxon.type.ErrorType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.MissingComponentException;
import net.sf.saxon.type.SchemaDeclaration;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.type.SimpleType;
import net.sf.saxon.type.TypeHierarchy;
import net.sf.saxon.type.UType;
import net.sf.saxon.type.Untyped;
import net.sf.saxon.value.BooleanValue;
import net.sf.saxon.value.SequenceType;

public class Copy
extends ElementCreator {
    private boolean copyNamespaces;
    private ItemType selectItemType = AnyItemType.getInstance();
    private ItemType resultItemType;

    public Copy(boolean copyNamespaces, boolean inheritNamespaces, SchemaType schemaType, int validation) {
        this.copyNamespaces = copyNamespaces;
        this.bequeathNamespacesToChildren = inheritNamespaces;
        this.setValidationAction(validation, schemaType);
        this.preservingTypes = schemaType == null && validation == 3;
    }

    public void setCopyNamespaces(boolean copy) {
        this.copyNamespaces = copy;
    }

    public boolean isCopyNamespaces() {
        return this.copyNamespaces;
    }

    @Override
    public Expression simplify() throws XPathException {
        this.preservingTypes |= !this.getPackageData().isSchemaAware();
        return super.simplify();
    }

    @Override
    public Expression typeCheck(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        this.typeCheckChildren(visitor, contextInfo);
        this.selectItemType = contextInfo.getItemType();
        ItemType selectItemType = contextInfo.getItemType();
        if (selectItemType == ErrorType.getInstance()) {
            XPathException err = new XPathException("No context item supplied for xsl:copy", "XTTE0945");
            err.setIsTypeError(true);
            err.setLocation(this.getLocation());
            throw err;
        }
        if (selectItemType instanceof NodeTest) {
            switch (selectItemType.getPrimitiveType()) {
                case 1: {
                    this.resultItemType = NodeKindTest.ELEMENT;
                    break;
                }
                case 9: {
                    this.resultItemType = NodeKindTest.DOCUMENT;
                    break;
                }
                case 2: 
                case 3: 
                case 7: 
                case 8: 
                case 13: {
                    ContextItemExpression dot = new ContextItemExpression();
                    ExpressionTool.copyLocationInfo(this, dot);
                    CopyOf c = new CopyOf(dot, this.copyNamespaces, this.getValidationAction(), this.getSchemaType(), false);
                    ExpressionTool.copyLocationInfo(this, c);
                    return c.typeCheck(visitor, contextInfo);
                }
                default: {
                    this.resultItemType = selectItemType;
                    break;
                }
            }
        } else {
            this.resultItemType = selectItemType;
        }
        this.checkContentSequence(visitor.getStaticContext());
        return this;
    }

    @Override
    public Expression copy(RebindingMap rebindings) {
        Copy copy = new Copy(this.copyNamespaces, this.bequeathNamespacesToChildren, this.getSchemaType(), this.getValidationAction());
        ExpressionTool.copyLocationInfo(this, copy);
        copy.setContentExpression(this.getContentExpression().copy(rebindings));
        copy.resultItemType = this.resultItemType;
        return copy;
    }

    public void setSelectItemType(ItemType type) {
        this.selectItemType = type;
    }

    @Override
    public int getIntrinsicDependencies() {
        return 2;
    }

    @Override
    public int getInstructionNameCode() {
        return 145;
    }

    @Override
    public Iterable<Operand> operands() {
        return this.contentOp;
    }

    @Override
    public ItemType getItemType() {
        if (this.resultItemType != null) {
            return this.resultItemType;
        }
        TypeHierarchy th = this.getConfiguration().getTypeHierarchy();
        this.resultItemType = this.computeItemType(th);
        return this.resultItemType;
    }

    private ItemType computeItemType(TypeHierarchy th) {
        ItemType selectItemType = this.selectItemType;
        if (!this.getPackageData().isSchemaAware()) {
            return selectItemType;
        }
        if (selectItemType.getUType().overlaps(UType.ANY_ATOMIC.union(UType.FUNCTION))) {
            return selectItemType;
        }
        Configuration config = th.getConfiguration();
        if (this.getSchemaType() != null) {
            Affinity e = th.relationship(selectItemType, NodeKindTest.ELEMENT);
            if (e == Affinity.SAME_TYPE || e == Affinity.SUBSUMED_BY) {
                return new ContentTypeTest(1, this.getSchemaType(), config, false);
            }
            Affinity a = th.relationship(selectItemType, NodeKindTest.ATTRIBUTE);
            if (a == Affinity.SAME_TYPE || a == Affinity.SUBSUMED_BY) {
                return new ContentTypeTest(2, this.getSchemaType(), config, false);
            }
            return AnyNodeTest.getInstance();
        }
        switch (this.getValidationAction()) {
            case 3: {
                return selectItemType;
            }
            case 4: {
                Affinity e = th.relationship(selectItemType, NodeKindTest.ELEMENT);
                if (e == Affinity.SAME_TYPE || e == Affinity.SUBSUMED_BY) {
                    return new ContentTypeTest(1, Untyped.getInstance(), config, false);
                }
                Affinity a = th.relationship(selectItemType, NodeKindTest.ATTRIBUTE);
                if (a == Affinity.SAME_TYPE || a == Affinity.SUBSUMED_BY) {
                    return new ContentTypeTest(2, BuiltInAtomicType.UNTYPED_ATOMIC, config, false);
                }
                if (e != Affinity.DISJOINT || a != Affinity.DISJOINT) {
                    return AnyNodeTest.getInstance();
                }
                return selectItemType;
            }
            case 1: 
            case 2: {
                if (selectItemType instanceof NodeTest) {
                    int fp = ((NodeTest)selectItemType).getFingerprint();
                    if (fp != -1) {
                        Affinity e = th.relationship(selectItemType, NodeKindTest.ELEMENT);
                        if (e == Affinity.SAME_TYPE || e == Affinity.SUBSUMED_BY) {
                            SchemaDeclaration elem = config.getElementDeclaration(fp);
                            if (elem != null) {
                                try {
                                    return new ContentTypeTest(1, elem.getType(), config, false);
                                } catch (MissingComponentException e1) {
                                    return new ContentTypeTest(1, AnyType.getInstance(), config, false);
                                }
                            }
                            return new ContentTypeTest(1, AnyType.getInstance(), config, false);
                        }
                        Affinity a = th.relationship(selectItemType, NodeKindTest.ATTRIBUTE);
                        if (a == Affinity.SAME_TYPE || a == Affinity.SUBSUMED_BY) {
                            SchemaDeclaration attr = config.getElementDeclaration(fp);
                            if (attr != null) {
                                try {
                                    return new ContentTypeTest(2, attr.getType(), config, false);
                                } catch (MissingComponentException e1) {
                                    return new ContentTypeTest(2, AnySimpleType.getInstance(), config, false);
                                }
                            }
                            return new ContentTypeTest(2, AnySimpleType.getInstance(), config, false);
                        }
                    } else {
                        Affinity e = th.relationship(selectItemType, NodeKindTest.ELEMENT);
                        if (e == Affinity.SAME_TYPE || e == Affinity.SUBSUMED_BY) {
                            return NodeKindTest.ELEMENT;
                        }
                        Affinity a = th.relationship(selectItemType, NodeKindTest.ATTRIBUTE);
                        if (a == Affinity.SAME_TYPE || a == Affinity.SUBSUMED_BY) {
                            return NodeKindTest.ATTRIBUTE;
                        }
                    }
                    return AnyNodeTest.getInstance();
                }
                if (selectItemType instanceof AtomicType) {
                    return selectItemType;
                }
                return AnyItemType.getInstance();
            }
        }
        throw new IllegalStateException();
    }

    @Override
    public Expression optimize(ExpressionVisitor visitor, ContextItemStaticInfo contextItemType) throws XPathException {
        Expression exp = super.optimize(visitor, contextItemType);
        if (exp == this) {
            UType type;
            if (this.resultItemType == null) {
                this.resultItemType = this.computeItemType(visitor.getConfiguration().getTypeHierarchy());
            }
            if (visitor.isOptimizeForStreaming() && !(type = contextItemType.getItemType().getUType()).intersection(MultipleNodeKindTest.LEAF.getUType()).equals(UType.VOID)) {
                Expression p = this.getParentExpression();
                if (p instanceof Choose && ((Choose)p).size() == 2 && ((Choose)p).getAction(1) == this && ((Choose)p).getAction(0) instanceof CopyOf) {
                    return exp;
                }
                CopyOf copyOf = new CopyOf(new ContextItemExpression(), false, this.getValidationAction(), this.getSchemaType(), false);
                MultipleNodeKindTest leafTest = new MultipleNodeKindTest(type.intersection(MultipleNodeKindTest.LEAF.getUType()));
                Expression[] conditions = new Expression[]{new InstanceOfExpression(new ContextItemExpression(), SequenceType.makeSequenceType(leafTest, 16384)), Literal.makeLiteral(BooleanValue.TRUE, this)};
                Expression[] actions = new Expression[]{copyOf, this};
                Choose choose = new Choose(conditions, actions);
                ExpressionTool.copyLocationInfo(this, choose);
                return choose;
            }
        }
        return exp;
    }

    @Override
    public NodeName getElementName(XPathContext context, NodeInfo copiedNode) {
        return NameOfNode.makeName(copiedNode);
    }

    @Override
    public String getNewBaseURI(XPathContext context, NodeInfo copiedNode) {
        return copiedNode.getBaseURI();
    }

    @Override
    public void outputNamespaceNodes(Outputter receiver, NodeName nodeName, NodeInfo copiedNode) throws XPathException {
        if (this.copyNamespaces) {
            receiver.namespaces(copiedNode.getAllNamespaces(), 64);
        } else {
            NamespaceBinding ns = nodeName.getNamespaceBinding();
            if (!ns.isDefaultUndeclaration()) {
                receiver.namespace(ns.getPrefix(), ns.getURI(), 0);
            }
        }
    }

    @Override
    public TailCall processLeavingTail(Outputter out, XPathContext context) throws XPathException {
        Controller controller = context.getController();
        Item item = context.getContextItem();
        if (item == null) {
            return null;
        }
        if (!(item instanceof NodeInfo)) {
            out.append(item, this.getLocation(), 524288);
            return null;
        }
        NodeInfo source = (NodeInfo)item;
        switch (source.getNodeKind()) {
            case 1: {
                return super.processLeavingTail(out, context, (NodeInfo)item);
            }
            case 2: {
                if (this.getSchemaType() instanceof ComplexType) {
                    this.dynamicError("Cannot copy an attribute when the type requested for validation is a complex type", "XTTE1535", context);
                }
                try {
                    CopyOf.copyAttribute(source, (SimpleType)this.getSchemaType(), this.getValidationAction(), this, out, context, false);
                    break;
                } catch (NoOpenStartTagException err) {
                    err.setXPathContext(context);
                    throw Copy.dynamicError(this.getLocation(), err, context);
                }
            }
            case 3: {
                CharSequence tval = source.getStringValueCS();
                out.characters(tval, this.getLocation(), 0);
                break;
            }
            case 7: {
                CharSequence pval = source.getStringValueCS();
                out.processingInstruction(source.getDisplayName(), pval, this.getLocation(), 0);
                break;
            }
            case 8: {
                CharSequence cval = source.getStringValueCS();
                out.comment(cval, this.getLocation(), 0);
                break;
            }
            case 13: {
                out.namespace(((NodeInfo)item).getLocalPart(), item.getStringValue(), 0);
                break;
            }
            case 9: {
                if (!this.preservingTypes) {
                    ParseOptions options = new ParseOptions(this.getValidationOptions());
                    options.setSpaceStrippingRule(NoElementsSpaceStrippingRule.getInstance());
                    controller.getConfiguration().prepareValidationReporting(context, options);
                    Receiver val = controller.getConfiguration().getDocumentValidator(out, source.getBaseURI(), options, this.getLocation());
                    out = new ComplexContentOutputter(val);
                }
                if (out.getSystemId() == null) {
                    out.setSystemId(source.getBaseURI());
                }
                out.startDocument(0);
                Copy.copyUnparsedEntities(source, out);
                this.getContentExpression().process(out, context);
                out.endDocument();
                break;
            }
            default: {
                throw new IllegalArgumentException("Unknown node kind " + source.getNodeKind());
            }
        }
        return null;
    }

    public static void copyUnparsedEntities(NodeInfo source, Outputter out) throws XPathException {
        Iterator<String> unparsedEntities = source.getTreeInfo().getUnparsedEntityNames();
        while (unparsedEntities.hasNext()) {
            String n = unparsedEntities.next();
            String[] details = source.getTreeInfo().getUnparsedEntity(n);
            out.setUnparsedEntity(n, details[0], details[1]);
        }
    }

    @Override
    public Item evaluateItem(XPathContext context) throws XPathException {
        Controller controller = context.getController();
        SequenceCollector seq = controller.allocateSequenceOutputter(1);
        seq.getPipelineConfiguration().setHostLanguage(this.getPackageData().getHostLanguage());
        this.process(new ComplexContentOutputter(seq), context);
        seq.close();
        Item item = seq.getFirstItem();
        seq.reset();
        return item;
    }

    @Override
    public String getStreamerName() {
        return "Copy";
    }

    @Override
    public void export(ExpressionPresenter out) throws XPathException {
        out.startElement("copy", this);
        this.exportValidationAndType(out);
        String flags = "";
        if (this.copyNamespaces) {
            flags = "c";
        }
        if (this.bequeathNamespacesToChildren) {
            flags = flags + "i";
        }
        if (this.inheritNamespacesFromParent) {
            flags = flags + "n";
        }
        if (this.isLocal()) {
            flags = flags + "l";
        }
        out.emitAttribute("flags", flags);
        String sType = SequenceType.makeSequenceType(this.selectItemType, this.getCardinality()).toAlphaCode();
        if (sType != null) {
            out.emitAttribute("sit", sType);
        }
        out.setChildRole("content");
        this.getContentExpression().export(out);
        out.endElement();
    }
}

