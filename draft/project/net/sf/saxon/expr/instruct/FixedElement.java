/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.instruct;

import java.util.function.BiConsumer;
import net.sf.saxon.Configuration;
import net.sf.saxon.event.Outputter;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.Operand;
import net.sf.saxon.expr.StaticContext;
import net.sf.saxon.expr.StringLiteral;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.instruct.Block;
import net.sf.saxon.expr.instruct.ElementCreator;
import net.sf.saxon.expr.instruct.FixedAttribute;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.om.NameChecker;
import net.sf.saxon.om.NamespaceBinding;
import net.sf.saxon.om.NamespaceMap;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.om.QNameException;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.pattern.CombinedNodeTest;
import net.sf.saxon.pattern.ContentTypeTest;
import net.sf.saxon.pattern.NameTest;
import net.sf.saxon.pattern.NodeKindTest;
import net.sf.saxon.pattern.NodeTest;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.type.Affinity;
import net.sf.saxon.type.AnyType;
import net.sf.saxon.type.ComplexType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.MissingComponentException;
import net.sf.saxon.type.SchemaDeclaration;
import net.sf.saxon.type.SchemaException;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.type.SimpleType;
import net.sf.saxon.type.TypeHierarchy;
import net.sf.saxon.type.Untyped;
import net.sf.saxon.type.ValidationFailure;

public class FixedElement
extends ElementCreator {
    private NodeName elementName;
    protected NamespaceMap namespaceBindings;
    private ItemType itemType;

    public FixedElement(NodeName elementName, NamespaceMap namespaceBindings, boolean inheritNamespacesToChildren, boolean inheritNamespacesFromParent, SchemaType schemaType, int validation) {
        this.elementName = elementName;
        this.namespaceBindings = namespaceBindings;
        this.bequeathNamespacesToChildren = inheritNamespacesToChildren;
        this.inheritNamespacesFromParent = inheritNamespacesFromParent;
        this.setValidationAction(validation, schemaType);
        this.preservingTypes = schemaType == null && validation == 3;
    }

    @Override
    public Iterable<Operand> operands() {
        return this.contentOp;
    }

    @Override
    public Expression simplify() throws XPathException {
        this.preservingTypes |= !this.getPackageData().isSchemaAware();
        return super.simplify();
    }

    @Override
    protected void checkContentSequence(StaticContext env) throws XPathException {
        super.checkContentSequence(env);
        this.itemType = this.computeFixedElementItemType(this, env, this.getValidationAction(), this.getSchemaType(), this.elementName, this.getContentExpression());
    }

    @Override
    public Expression optimize(ExpressionVisitor visitor, ContextItemStaticInfo contextItemType) throws XPathException {
        Expression e = super.optimize(visitor, contextItemType);
        if (e != this) {
            return e;
        }
        if (!this.bequeathNamespacesToChildren) {
            return this;
        }
        return this;
    }

    private void removeRedundantNamespaces(ExpressionVisitor visitor, NamespaceMap parentNamespaces) {
        ItemType contentType;
        boolean ok;
        if (this.namespaceBindings.isEmpty()) {
            return;
        }
        TypeHierarchy th = visitor.getConfiguration().getTypeHierarchy();
        boolean bl = ok = th.relationship(contentType = this.getContentExpression().getItemType(), NodeKindTest.ATTRIBUTE) == Affinity.DISJOINT;
        if (!ok && this.getContentExpression() instanceof Block) {
            ok = true;
            for (Operand o : this.getContentExpression().operands()) {
                Expression exp = o.getChildExpression();
                if (exp instanceof FixedAttribute) {
                    if (((FixedAttribute)exp).getAttributeName().hasURI("")) continue;
                    ok = false;
                    break;
                }
                ItemType childType = exp.getItemType();
                if (th.relationship(childType, NodeKindTest.ATTRIBUTE) == Affinity.DISJOINT) continue;
                ok = false;
                break;
            }
        }
        if (ok) {
            NamespaceMap reduced = this.namespaceBindings;
            for (NamespaceBinding childNamespace : this.namespaceBindings) {
                if (!childNamespace.getURI().equals(parentNamespaces.getURI(childNamespace.getPrefix()))) continue;
                reduced = reduced.remove(childNamespace.getPrefix());
            }
            this.namespaceBindings = reduced;
        }
    }

    @Override
    public Expression copy(RebindingMap rebindings) {
        FixedElement fe = new FixedElement(this.elementName, this.namespaceBindings, this.bequeathNamespacesToChildren, this.inheritNamespacesFromParent, this.getSchemaType(), this.getValidationAction());
        fe.setContentExpression(this.getContentExpression().copy(rebindings));
        ExpressionTool.copyLocationInfo(this, fe);
        return fe;
    }

    private ItemType computeFixedElementItemType(FixedElement instr, StaticContext env, int validation, SchemaType schemaType, NodeName elementName, Expression content) throws XPathException {
        NodeTest itemType;
        Configuration config = env.getConfiguration();
        int fp = elementName.obtainFingerprint(config.getNamePool());
        if (schemaType == null) {
            if (validation == 1) {
                SchemaDeclaration decl = config.getElementDeclaration(fp);
                if (decl == null) {
                    XPathException err = new XPathException("There is no global element declaration for " + elementName.getStructuredQName().getEQName() + ", so strict validation will fail");
                    err.setErrorCode(instr.isXSLT() ? "XTTE1512" : "XQDY0084");
                    err.setIsTypeError(true);
                    err.setLocation(instr.getLocation());
                    throw err;
                }
                if (decl.isAbstract()) {
                    XPathException err = new XPathException("The element declaration for " + elementName.getStructuredQName().getEQName() + " is abstract, so strict validation will fail");
                    err.setErrorCode(instr.isXSLT() ? "XTTE1512" : "XQDY0027");
                    err.setIsTypeError(true);
                    err.setLocation(instr.getLocation());
                    throw err;
                }
                SchemaType declaredType = decl.getType();
                SchemaType xsiType = instr.getXSIType(env);
                schemaType = xsiType != null ? xsiType : declaredType;
                itemType = new CombinedNodeTest(new NameTest(1, fp, env.getConfiguration().getNamePool()), 23, new ContentTypeTest(1, schemaType, config, false));
                if (xsiType != null || !decl.hasTypeAlternatives()) {
                    instr.getValidationOptions().setTopLevelType(schemaType);
                    try {
                        schemaType.analyzeContentExpression(content, 1);
                    } catch (XPathException e) {
                        e.setErrorCode(instr.isXSLT() ? "XTTE1510" : "XQDY0027");
                        e.setLocation(instr.getLocation());
                        throw e;
                    }
                    if (xsiType != null) {
                        try {
                            config.checkTypeDerivationIsOK(xsiType, declaredType, 0);
                        } catch (SchemaException e) {
                            ValidationFailure ve = new ValidationFailure("The specified xsi:type " + xsiType.getDescription() + " is not validly derived from the required type " + declaredType.getDescription());
                            ve.setConstraintReference(1, "cvc-elt", "4.3");
                            ve.setErrorCode(instr.isXSLT() ? "XTTE1515" : "XQDY0027");
                            ve.setLocator(instr.getLocation());
                            throw ve.makeException();
                        }
                    }
                }
            } else if (validation == 2) {
                SchemaDeclaration decl = config.getElementDeclaration(fp);
                if (decl == null) {
                    env.issueWarning("There is no global element declaration for " + elementName.getDisplayName(), instr.getLocation());
                    itemType = new NameTest(1, fp, config.getNamePool());
                } else {
                    schemaType = decl.getType();
                    instr.getValidationOptions().setTopLevelType(schemaType);
                    itemType = new CombinedNodeTest(new NameTest(1, fp, config.getNamePool()), 23, new ContentTypeTest(1, instr.getSchemaType(), config, false));
                    try {
                        schemaType.analyzeContentExpression(content, 1);
                    } catch (XPathException e) {
                        e.setErrorCode(instr.isXSLT() ? "XTTE1515" : "XQDY0027");
                        e.setLocation(instr.getLocation());
                        throw e;
                    }
                }
            } else {
                itemType = validation == 3 ? new CombinedNodeTest(new NameTest(1, fp, config.getNamePool()), 23, new ContentTypeTest(1, AnyType.getInstance(), config, false)) : new CombinedNodeTest(new NameTest(1, fp, config.getNamePool()), 23, new ContentTypeTest(1, Untyped.getInstance(), config, false));
            }
        } else {
            itemType = new CombinedNodeTest(new NameTest(1, fp, config.getNamePool()), 23, new ContentTypeTest(1, schemaType, config, false));
            try {
                schemaType.analyzeContentExpression(content, 1);
            } catch (XPathException e) {
                e.setErrorCode(instr.isXSLT() ? "XTTE1540" : "XQDY0027");
                e.setLocation(instr.getLocation());
                throw e;
            }
        }
        return itemType;
    }

    @Override
    public ItemType getItemType() {
        if (this.itemType == null) {
            return super.getItemType();
        }
        return this.itemType;
    }

    @Override
    public NodeName getElementName(XPathContext context, NodeInfo copiedNode) {
        return this.elementName;
    }

    public NodeName getElementName() {
        return this.elementName;
    }

    @Override
    public void gatherProperties(BiConsumer<String, Object> consumer) {
        consumer.accept("name", this.getElementName());
    }

    @Override
    public String getNewBaseURI(XPathContext context, NodeInfo copiedNode) {
        return this.getStaticBaseURIString();
    }

    private SchemaType getXSIType(StaticContext env) throws XPathException {
        if (this.getContentExpression() instanceof FixedAttribute) {
            return this.testForXSIType((FixedAttribute)this.getContentExpression(), env);
        }
        if (this.getContentExpression() instanceof Block) {
            for (Operand o : this.getContentExpression().operands()) {
                SchemaType type;
                Expression exp = o.getChildExpression();
                if (!(exp instanceof FixedAttribute) || (type = this.testForXSIType((FixedAttribute)exp, env)) == null) continue;
                return type;
            }
            return null;
        }
        return null;
    }

    private SchemaType testForXSIType(FixedAttribute fat, StaticContext env) throws XPathException {
        Expression attValue;
        int att = fat.getAttributeFingerprint();
        if (att == 641 && (attValue = fat.getSelect()) instanceof StringLiteral) {
            try {
                String[] parts = NameChecker.getQNameParts(((StringLiteral)attValue).getStringValue());
                String uri = this.namespaceBindings.getURI(parts[0]);
                if (uri == null) {
                    return null;
                }
                return env.getConfiguration().getSchemaType(new StructuredQName("", uri, parts[1]));
            } catch (QNameException e) {
                throw new XPathException(e.getMessage());
            }
        }
        return null;
    }

    @Override
    public void checkPermittedContents(SchemaType parentType, boolean whole) throws XPathException {
        SchemaType type;
        if (parentType instanceof SimpleType) {
            XPathException err = new XPathException("Element " + this.elementName.getDisplayName() + " is not permitted here: the containing element is of simple type " + parentType.getDescription());
            err.setIsTypeError(true);
            err.setLocation(this.getLocation());
            throw err;
        }
        if (((ComplexType)parentType).isSimpleContent()) {
            XPathException err = new XPathException("Element " + this.elementName.getDisplayName() + " is not permitted here: the containing element has a complex type with simple content");
            err.setIsTypeError(true);
            err.setLocation(this.getLocation());
            throw err;
        }
        if (whole) {
            Expression parent = this.getParentExpression();
            Block block = new Block(new Expression[]{this});
            parentType.analyzeContentExpression(block, 1);
            this.setParentExpression(parent);
        }
        try {
            int fp = this.elementName.obtainFingerprint(this.getConfiguration().getNamePool());
            type = ((ComplexType)parentType).getElementParticleType(fp, true);
        } catch (MissingComponentException e) {
            throw new XPathException(e);
        }
        if (type == null) {
            XPathException err = new XPathException("Element " + this.elementName.getDisplayName() + " is not permitted in the content model of the complex type " + parentType.getDescription());
            err.setIsTypeError(true);
            err.setLocation(this.getLocation());
            err.setErrorCode(this.isXSLT() ? "XTTE1510" : "XQDY0027");
            throw err;
        }
        if (type instanceof AnyType) {
            return;
        }
        try {
            this.getContentExpression().checkPermittedContents(type, true);
        } catch (XPathException e) {
            e.maybeSetLocation(this.getLocation());
            throw e;
        }
    }

    @Override
    public void outputNamespaceNodes(Outputter out, NodeName nodeName, NodeInfo copiedNode) throws XPathException {
        for (NamespaceBinding ns : this.namespaceBindings) {
            out.namespace(ns.getPrefix(), ns.getURI(), 0);
        }
    }

    public NamespaceMap getActiveNamespaces() {
        return this.namespaceBindings;
    }

    @Override
    public void export(ExpressionPresenter out) throws XPathException {
        out.startElement("elem", this);
        out.emitAttribute("name", this.elementName.getDisplayName());
        out.emitAttribute("nsuri", this.elementName.getURI());
        String flags = this.getInheritanceFlags();
        if (!this.elementName.getURI().isEmpty() && this.elementName.getPrefix().isEmpty()) {
            flags = flags + "d";
        }
        if (this.isLocal()) {
            flags = flags + "l";
        }
        if (!flags.isEmpty()) {
            out.emitAttribute("flags", flags);
        }
        FastStringBuffer fsb = new FastStringBuffer(256);
        if (!this.namespaceBindings.isEmpty()) {
            for (NamespaceBinding ns : this.namespaceBindings) {
                String prefix = ns.getPrefix();
                if (prefix.equals("xml")) continue;
                fsb.append(prefix.isEmpty() ? "#" : prefix);
                if (!ns.getURI().equals(this.getRetainedStaticContext().getURIForPrefix(prefix, true))) {
                    fsb.cat('=');
                    fsb.append(ns.getURI());
                }
                fsb.cat(' ');
            }
            fsb.setLength(fsb.length() - 1);
            out.emitAttribute("namespaces", fsb.toString());
        }
        this.exportValidationAndType(out);
        this.getContentExpression().export(out);
        out.endElement();
    }

    @Override
    public String toString() {
        return "<" + this.elementName.getStructuredQName().getDisplayName() + " {" + this.getContentExpression().toString() + "}/>";
    }

    @Override
    public String toShortString() {
        return "<" + this.elementName.getStructuredQName().getDisplayName() + " {" + this.getContentExpression().toShortString() + "}/>";
    }

    @Override
    public String getExpressionName() {
        return "element";
    }
}

