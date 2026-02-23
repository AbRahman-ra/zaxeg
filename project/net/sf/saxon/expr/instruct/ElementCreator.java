/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.instruct;

import net.sf.saxon.event.ComplexContentOutputter;
import net.sf.saxon.event.Outputter;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.Operand;
import net.sf.saxon.expr.StaticContext;
import net.sf.saxon.expr.StringLiteral;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.instruct.Block;
import net.sf.saxon.expr.instruct.ParentNodeConstructor;
import net.sf.saxon.expr.instruct.TailCall;
import net.sf.saxon.expr.instruct.ValueOf;
import net.sf.saxon.lib.ParseOptions;
import net.sf.saxon.lib.Validation;
import net.sf.saxon.ma.arrays.ArrayItemType;
import net.sf.saxon.ma.map.MapType;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.pattern.NodeKindTest;
import net.sf.saxon.pattern.NodeTest;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.AnyType;
import net.sf.saxon.type.FunctionItemType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.type.UType;
import net.sf.saxon.type.Untyped;
import net.sf.saxon.value.Cardinality;

public abstract class ElementCreator
extends ParentNodeConstructor {
    boolean bequeathNamespacesToChildren = true;
    boolean inheritNamespacesFromParent = true;

    @Override
    public ItemType getItemType() {
        return NodeKindTest.ELEMENT;
    }

    @Override
    public int getCardinality() {
        return 16384;
    }

    public void setBequeathNamespacesToChildren(boolean inherit) {
        this.bequeathNamespacesToChildren = inherit;
    }

    public boolean isBequeathNamespacesToChildren() {
        return this.bequeathNamespacesToChildren;
    }

    public void setInheritNamespacesFromParent(boolean inherit) {
        this.inheritNamespacesFromParent = inherit;
    }

    public boolean isInheritNamespacesFromParent() {
        return this.inheritNamespacesFromParent;
    }

    @Override
    public int computeSpecialProperties() {
        int p = super.computeSpecialProperties() | 0x1000000;
        if (this.getValidationAction() == 4) {
            p |= 0x8000000;
        }
        return p;
    }

    @Override
    public void suppressValidation(int parentValidationMode) {
        if (this.getValidationAction() == parentValidationMode && this.getSchemaType() == null) {
            this.setValidationAction(3, null);
        }
    }

    @Override
    protected void checkContentSequence(StaticContext env) throws XPathException {
        Operand[] components = this.getContentExpression() instanceof Block ? ((Block)this.getContentExpression()).getOperanda() : new Operand[]{this.contentOp};
        boolean foundChild = false;
        boolean foundPossibleChild = false;
        for (Operand o : components) {
            XPathException de;
            Expression component = o.getChildExpression();
            ItemType it = component.getItemType();
            if (it.isAtomicType()) {
                foundChild = true;
                continue;
            }
            if (it instanceof FunctionItemType && !(it instanceof ArrayItemType)) {
                String which = it instanceof MapType ? "map" : "function";
                XPathException de2 = new XPathException("Cannot add a " + which + " as a child of a constructed element");
                de2.setErrorCode(this.isXSLT() ? "XTDE0450" : "XQTY0105");
                de2.setLocator(component.getLocation());
                de2.setIsTypeError(true);
                throw de2;
            }
            if (!(it instanceof NodeTest)) continue;
            boolean maybeEmpty = Cardinality.allowsZero(component.getCardinality());
            UType possibleNodeKinds = it.getUType();
            if (possibleNodeKinds.overlaps(UType.TEXT)) {
                if (component instanceof ValueOf && ((ValueOf)component).getSelect() instanceof StringLiteral) {
                    String value = ((StringLiteral)((ValueOf)component).getSelect()).getStringValue();
                    if (value.isEmpty()) continue;
                    foundChild = true;
                    continue;
                }
                foundPossibleChild = true;
                continue;
            }
            if (!possibleNodeKinds.overlaps(UType.CHILD_NODE_KINDS)) {
                if (maybeEmpty) {
                    foundPossibleChild = true;
                    continue;
                }
                foundChild = true;
                continue;
            }
            if (foundChild && possibleNodeKinds == UType.ATTRIBUTE && !maybeEmpty) {
                de = new XPathException("Cannot create an attribute node after creating a child of the containing element");
                de.setErrorCode(this.isXSLT() ? "XTDE0410" : "XQTY0024");
                de.setLocator(component.getLocation());
                throw de;
            }
            if (foundChild && possibleNodeKinds == UType.NAMESPACE && !maybeEmpty) {
                de = new XPathException("Cannot create a namespace node after creating a child of the containing element");
                de.setErrorCode(this.isXSLT() ? "XTDE0410" : "XQTY0024");
                de.setLocator(component.getLocation());
                throw de;
            }
            if ((foundChild || foundPossibleChild) && possibleNodeKinds == UType.ATTRIBUTE) {
                env.issueWarning("Creating an attribute here will fail if previous instructions create any children", component.getLocation());
                continue;
            }
            if (!foundChild && !foundPossibleChild || possibleNodeKinds != UType.NAMESPACE) continue;
            env.issueWarning("Creating a namespace node here will fail if previous instructions create any children", component.getLocation());
        }
    }

    public abstract NodeName getElementName(XPathContext var1, NodeInfo var2) throws XPathException;

    public abstract String getNewBaseURI(XPathContext var1, NodeInfo var2);

    public abstract void outputNamespaceNodes(Outputter var1, NodeName var2, NodeInfo var3) throws XPathException;

    @Override
    public int getImplementationMethod() {
        return 4;
    }

    @Override
    public TailCall processLeavingTail(Outputter output, XPathContext context) throws XPathException {
        return this.processLeavingTail(output, context, null);
    }

    public final TailCall processLeavingTail(Outputter out, XPathContext context, NodeInfo copiedNode) throws XPathException {
        try {
            NodeName elemName = this.getElementName(context, copiedNode);
            Enum typeCode = this.getValidationAction() == 3 ? AnyType.getInstance() : Untyped.getInstance();
            Outputter elemOut = out;
            if (!this.preservingTypes) {
                ParseOptions options = new ParseOptions(this.getValidationOptions());
                options.setTopLevelElement(elemName.getStructuredQName());
                context.getConfiguration().prepareValidationReporting(context, options);
                Receiver validator = context.getConfiguration().getElementValidator(elemOut, options, this.getLocation());
                if (validator != elemOut) {
                    out = new ComplexContentOutputter(validator);
                }
            }
            if (out.getSystemId() == null) {
                out.setSystemId(this.getNewBaseURI(context, copiedNode));
            }
            int properties = 0;
            if (!this.bequeathNamespacesToChildren) {
                properties |= 0x80;
            }
            if (!this.inheritNamespacesFromParent) {
                properties |= 0x10000;
            }
            out.startElement(elemName, (SchemaType)((Object)typeCode), this.getLocation(), properties |= 0x80000);
            this.outputNamespaceNodes(out, elemName, copiedNode);
            this.getContentExpression().process(out, context);
            out.endElement();
            return null;
        } catch (XPathException e) {
            e.maybeSetLocation(this.getLocation());
            e.maybeSetContext(context);
            throw e;
        }
    }

    void exportValidationAndType(ExpressionPresenter out) {
        SchemaType type;
        if (this.getValidationAction() != 4 && this.getValidationAction() != 8) {
            out.emitAttribute("validation", Validation.toString(this.getValidationAction()));
        }
        if (this.getValidationAction() == 8 && (type = this.getSchemaType()) != null) {
            out.emitAttribute("type", type.getStructuredQName());
        }
    }

    String getInheritanceFlags() {
        String flags = "";
        if (!this.inheritNamespacesFromParent) {
            flags = flags + "P";
        }
        if (!this.bequeathNamespacesToChildren) {
            flags = flags + "C";
        }
        return flags;
    }

    public void setInheritanceFlags(String flags) {
        this.inheritNamespacesFromParent = !flags.contains("P");
        this.bequeathNamespacesToChildren = !flags.contains("C");
    }

    @Override
    public String getStreamerName() {
        return "ElementCreator";
    }
}

