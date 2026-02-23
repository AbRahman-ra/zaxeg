/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.instruct;

import net.sf.saxon.Configuration;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.Operand;
import net.sf.saxon.expr.OperandRole;
import net.sf.saxon.expr.StringLiteral;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.instruct.AttributeCreator;
import net.sf.saxon.expr.instruct.FixedAttribute;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.expr.parser.RoleDiagnostic;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.lib.StandardURIChecker;
import net.sf.saxon.lib.Validation;
import net.sf.saxon.om.FingerprintedQName;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NameChecker;
import net.sf.saxon.om.NamePool;
import net.sf.saxon.om.NamespaceResolver;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.om.QNameException;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.pattern.NodeKindTest;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.util.Orphan;
import net.sf.saxon.type.Affinity;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.type.SimpleType;
import net.sf.saxon.type.TypeHierarchy;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.QNameValue;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;
import net.sf.saxon.value.Whitespace;

public final class ComputedAttribute
extends AttributeCreator {
    private Operand nameOp;
    private Operand namespaceOp;
    private boolean allowNameAsQName;

    public ComputedAttribute(Expression attributeName, Expression namespace, NamespaceResolver nsContext, int validationAction, SimpleType schemaType, boolean allowNameAsQName) {
        this.nameOp = new Operand(this, attributeName, OperandRole.SINGLE_ATOMIC);
        if (namespace != null) {
            this.namespaceOp = new Operand(this, namespace, OperandRole.SINGLE_ATOMIC);
        }
        this.setSchemaType(schemaType);
        this.setValidationAction(validationAction);
        this.setOptions(0);
        this.allowNameAsQName = allowNameAsQName;
    }

    @Override
    public void setRejectDuplicates() {
        this.setOptions(this.getOptions() | 0x20);
    }

    @Override
    public int getInstructionNameCode() {
        return 135;
    }

    public Expression getNameExp() {
        return this.nameOp.getChildExpression();
    }

    public Expression getNamespaceExp() {
        return this.namespaceOp == null ? null : this.namespaceOp.getChildExpression();
    }

    public void setNameExp(Expression attributeName) {
        this.nameOp.setChildExpression(attributeName);
    }

    public void setNamespace(Expression namespace) {
        if (namespace != null) {
            if (this.namespaceOp == null) {
                this.namespaceOp = new Operand(this, namespace, OperandRole.SINGLE_ATOMIC);
            } else {
                this.namespaceOp.setChildExpression(namespace);
            }
        }
    }

    @Override
    public Iterable<Operand> operands() {
        return this.operandSparseList(this.selectOp, this.nameOp, this.namespaceOp);
    }

    public NamespaceResolver getNamespaceResolver() {
        return this.getRetainedStaticContext();
    }

    @Override
    public ItemType getItemType() {
        return NodeKindTest.ATTRIBUTE;
    }

    @Override
    public int getCardinality() {
        return 24576;
    }

    public boolean isAllowNameAsQName() {
        return this.allowNameAsQName;
    }

    @Override
    public int computeSpecialProperties() {
        return super.computeSpecialProperties() | 0x1000000;
    }

    @Override
    public void localTypeCheck(ExpressionVisitor visitor, ContextItemStaticInfo contextItemType) throws XPathException {
        this.nameOp.typeCheck(visitor, contextItemType);
        RoleDiagnostic role = new RoleDiagnostic(4, "attribute/name", 0);
        Configuration config = visitor.getConfiguration();
        TypeHierarchy th = config.getTypeHierarchy();
        if (this.allowNameAsQName) {
            boolean maybeQName;
            this.setNameExp(config.getTypeChecker(false).staticTypeCheck(this.getNameExp(), SequenceType.SINGLE_ATOMIC, role, visitor));
            ItemType nameItemType = this.getNameExp().getItemType();
            boolean maybeString = th.relationship(nameItemType, BuiltInAtomicType.STRING) != Affinity.DISJOINT || th.relationship(nameItemType, BuiltInAtomicType.UNTYPED_ATOMIC) != Affinity.DISJOINT;
            boolean bl = maybeQName = th.relationship(nameItemType, BuiltInAtomicType.QNAME) != Affinity.DISJOINT;
            if (!maybeString && !maybeQName) {
                XPathException err = new XPathException("The attribute name must be either an xs:string, an xs:QName, or untyped atomic");
                err.setErrorCode("XPTY0004");
                err.setIsTypeError(true);
                err.setLocation(this.getLocation());
                throw err;
            }
        } else if (!th.isSubType(this.getNameExp().getItemType(), BuiltInAtomicType.STRING)) {
            this.setNameExp(SystemFunction.makeCall("string", this.getRetainedStaticContext(), this.getNameExp()));
        }
        if (this.getNamespaceExp() != null) {
            this.namespaceOp.typeCheck(visitor, contextItemType);
        }
        if (Literal.isAtomic(this.getNameExp())) {
            try {
                AtomicValue val = (AtomicValue)((Literal)this.getNameExp()).getValue();
                if (val instanceof StringValue) {
                    String[] parts = NameChecker.checkQNameParts(val.getStringValueCS());
                    if (this.getNamespaceExp() == null) {
                        String uri = this.getNamespaceResolver().getURIForPrefix(parts[0], false);
                        if (uri == null) {
                            XPathException se = new XPathException("Prefix " + parts[0] + " has not been declared");
                            if (this.isXSLT()) {
                                se.setErrorCode("XTDE0860");
                                se.setIsStaticError(true);
                                throw se;
                            }
                            se.setErrorCode("XQDY0074");
                            se.setIsStaticError(false);
                            throw se;
                        }
                        this.setNamespace(new StringLiteral(uri));
                    }
                }
            } catch (XPathException e) {
                if (e.getErrorCodeQName() == null || e.getErrorCodeLocalPart().equals("FORG0001")) {
                    e.setErrorCode(this.isXSLT() ? "XTDE0850" : "XQDY0074");
                }
                e.maybeSetLocation(this.getLocation());
                e.setIsStaticError(true);
                throw e;
            }
        }
    }

    @Override
    public Expression optimize(ExpressionVisitor visitor, ContextItemStaticInfo contextItemType) throws XPathException {
        Expression exp = super.optimize(visitor, contextItemType);
        if (exp != this) {
            return exp;
        }
        if (this.getNameExp() instanceof Literal && (this.getNamespaceExp() == null || this.getNamespaceExp() instanceof Literal)) {
            XPathContext context = visitor.getStaticContext().makeEarlyEvaluationContext();
            NodeName nc = this.evaluateNodeName(context);
            FixedAttribute fa = new FixedAttribute(nc, this.getValidationAction(), this.getSchemaType());
            fa.setSelect(this.getSelect());
            return fa;
        }
        return this;
    }

    @Override
    public Expression copy(RebindingMap rebindings) {
        ComputedAttribute exp = new ComputedAttribute(this.getNameExp() == null ? null : this.getNameExp().copy(rebindings), this.getNamespaceExp() == null ? null : this.getNamespaceExp().copy(rebindings), this.getRetainedStaticContext(), this.getValidationAction(), this.getSchemaType(), this.allowNameAsQName);
        ExpressionTool.copyLocationInfo(this, exp);
        exp.setSelect(this.getSelect().copy(rebindings));
        exp.setInstruction(this.isInstruction());
        return exp;
    }

    @Override
    public void checkPermittedContents(SchemaType parentType, boolean whole) throws XPathException {
        if (parentType instanceof SimpleType) {
            String msg = "Attributes are not permitted here: ";
            msg = parentType.isAnonymousType() ? msg + "the containing element is defined to have a simple type" : msg + "the containing element is of simple type " + parentType.getDescription();
            XPathException err = new XPathException(msg);
            err.setIsTypeError(true);
            err.setLocation(this.getLocation());
            throw err;
        }
    }

    @Override
    public NodeName evaluateNodeName(XPathContext context) throws XPathException {
        XPathException err;
        String errorCode;
        String localName;
        String prefix;
        NamePool pool = context.getNamePool();
        Item nameValue = this.getNameExp().evaluateItem(context);
        String uri = null;
        if (nameValue instanceof StringValue) {
            String errorCode2;
            String rawName = nameValue.getStringValue();
            if ((rawName = Whitespace.trimWhitespace(rawName).toString()).startsWith("Q{") && this.allowNameAsQName) {
                try {
                    StructuredQName qn = StructuredQName.fromEQName(rawName);
                    prefix = "";
                    localName = qn.getLocalPart();
                    uri = qn.getURI();
                } catch (IllegalArgumentException e) {
                    throw new XPathException("Invalid EQName in computed attribute constructor: " + e.getMessage(), "XQDY0074");
                }
                if (!NameChecker.isValidNCName(localName)) {
                    throw new XPathException("Local part of EQName in computed attribute constructor is invalid", "XQDY0074");
                }
            } else {
                try {
                    String[] parts = NameChecker.getQNameParts(rawName);
                    prefix = parts[0];
                    localName = parts[1];
                } catch (QNameException err2) {
                    String errorCode3 = this.isXSLT() ? "XTDE0850" : "XQDY0074";
                    XPathException err1 = new XPathException("Invalid attribute name: " + rawName, errorCode3, this.getLocation());
                    throw ComputedAttribute.dynamicError(this.getLocation(), err1, context);
                }
                if (rawName.toString().equals("xmlns") && this.getNamespaceExp() == null) {
                    errorCode2 = this.isXSLT() ? "XTDE0855" : "XQDY0044";
                    XPathException err3 = new XPathException("Invalid attribute name: " + rawName, errorCode2, this.getLocation());
                    throw ComputedAttribute.dynamicError(this.getLocation(), err3, context);
                }
            }
            if (prefix.equals("xmlns")) {
                if (this.getNamespaceExp() == null) {
                    errorCode2 = this.isXSLT() ? "XTDE0860" : "XQDY0044";
                    XPathException err4 = new XPathException("Invalid attribute name: " + rawName, errorCode2, this.getLocation());
                    throw ComputedAttribute.dynamicError(this.getLocation(), err4, context);
                }
                prefix = "";
            }
        } else if (nameValue instanceof QNameValue && this.allowNameAsQName) {
            localName = ((QNameValue)nameValue).getLocalName();
            uri = ((QNameValue)nameValue).getNamespaceURI();
            if (localName.equals("xmlns") && uri.isEmpty()) {
                XPathException err5 = new XPathException("Invalid attribute name: xmlns", "XQDY0044", this.getLocation());
                throw ComputedAttribute.dynamicError(this.getLocation(), err5, context);
            }
            if (uri.isEmpty()) {
                prefix = "";
            } else {
                prefix = ((QNameValue)nameValue).getPrefix();
                if (prefix.isEmpty() && (prefix = pool.suggestPrefixForURI(uri)) == null) {
                    prefix = "ns0";
                }
                if (uri.equals("http://www.w3.org/XML/1998/namespace") != "xml".equals(prefix)) {
                    String message = "xml".equals(prefix) ? "When the prefix is 'xml', the namespace URI must be http://www.w3.org/XML/1998/namespace" : "When the namespace URI is http://www.w3.org/XML/1998/namespace, the prefix must be 'xml'";
                    String errorCode4 = this.isXSLT() ? "XTDE0835" : "XQDY0044";
                    XPathException err6 = new XPathException(message, errorCode4, this.getLocation());
                    throw ComputedAttribute.dynamicError(this.getLocation(), err6, context);
                }
            }
            if ("xmlns".equals(prefix)) {
                XPathException err7 = new XPathException("Invalid attribute namespace: http://www.w3.org/2000/xmlns/", "XQDY0044", this.getLocation());
                throw ComputedAttribute.dynamicError(this.getLocation(), err7, context);
            }
        } else {
            XPathException err8 = new XPathException("Attribute name must be either a string or a QName", "XPTY0004", this.getLocation());
            err8.setIsTypeError(true);
            throw ComputedAttribute.dynamicError(this.getLocation(), err8, context);
        }
        if (this.getNamespaceExp() == null && uri == null) {
            if (prefix.isEmpty()) {
                uri = "";
            } else {
                uri = this.getRetainedStaticContext().getURIForPrefix(prefix, false);
                if (uri == null) {
                    errorCode = this.isXSLT() ? "XTDE0860" : "XQDY0074";
                    err = new XPathException("Undeclared prefix in attribute name: " + prefix, errorCode, this.getLocation());
                    throw ComputedAttribute.dynamicError(this.getLocation(), err, context);
                }
            }
        } else {
            if (uri == null) {
                if (this.getNamespaceExp() instanceof StringLiteral) {
                    uri = ((StringLiteral)this.getNamespaceExp()).getStringValue();
                } else {
                    uri = this.getNamespaceExp().evaluateAsString(context).toString();
                    if (!StandardURIChecker.getInstance().isValidURI(uri)) {
                        XPathException de = new XPathException("The value of the namespace attribute must be a valid URI", "XTDE0865", this.getLocation());
                        throw ComputedAttribute.dynamicError(this.getLocation(), de, context);
                    }
                }
            }
            if (uri.isEmpty()) {
                prefix = "";
            } else if (prefix.isEmpty() && (prefix = pool.suggestPrefixForURI(uri)) == null) {
                prefix = "ns0";
            }
        }
        if (uri.equals("http://www.w3.org/2000/xmlns/")) {
            errorCode = this.isXSLT() ? "XTDE0865" : "XQDY0044";
            err = new XPathException("Cannot create attribute in namespace " + uri, errorCode, this.getLocation());
            throw ComputedAttribute.dynamicError(this.getLocation(), err, context);
        }
        return new FingerprintedQName(prefix, uri, localName);
    }

    @Override
    public Item evaluateItem(XPathContext context) throws XPathException {
        Item node = super.evaluateItem(context);
        this.validateOrphanAttribute((Orphan)node, context);
        return node;
    }

    @Override
    public void export(ExpressionPresenter out) throws XPathException {
        SimpleType type;
        out.startElement("compAtt", this);
        if (this.getValidationAction() != 4) {
            out.emitAttribute("validation", Validation.toString(this.getValidationAction()));
        }
        if ((type = this.getSchemaType()) != null) {
            out.emitAttribute("type", type.getStructuredQName());
        }
        String flags = "";
        if (this.isLocal()) {
            flags = flags + "l";
        }
        if (!flags.isEmpty()) {
            out.emitAttribute("flags", flags);
        }
        out.setChildRole("name");
        this.getNameExp().export(out);
        if (this.getNamespaceExp() != null) {
            out.setChildRole("namespace");
            this.getNamespaceExp().export(out);
        }
        out.setChildRole("select");
        this.getSelect().export(out);
        out.endElement();
    }
}

