/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.instruct;

import net.sf.saxon.Configuration;
import net.sf.saxon.Controller;
import net.sf.saxon.event.Outputter;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.Operand;
import net.sf.saxon.expr.OperandRole;
import net.sf.saxon.expr.StringLiteral;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.instruct.ElementCreator;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.expr.parser.RoleDiagnostic;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.lib.StandardURIChecker;
import net.sf.saxon.om.FingerprintedQName;
import net.sf.saxon.om.NameChecker;
import net.sf.saxon.om.NamespaceResolver;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.om.QNameException;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.pattern.ContentTypeTest;
import net.sf.saxon.pattern.NodeKindTest;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.Affinity;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.ComplexType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.type.SimpleType;
import net.sf.saxon.type.TypeHierarchy;
import net.sf.saxon.type.Untyped;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.QNameValue;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;
import net.sf.saxon.value.Whitespace;

public class ComputedElement
extends ElementCreator {
    private Operand nameOp;
    private Operand namespaceOp;
    private boolean allowNameAsQName;
    private ItemType itemType;

    public ComputedElement(Expression elementName, Expression namespace, SchemaType schemaType, int validation, boolean inheritNamespaces, boolean allowQName) {
        this.nameOp = new Operand(this, elementName, OperandRole.SINGLE_ATOMIC);
        if (namespace != null) {
            this.namespaceOp = new Operand(this, namespace, OperandRole.SINGLE_ATOMIC);
        }
        this.setValidationAction(validation, schemaType);
        this.preservingTypes = schemaType == null && validation == 3;
        this.bequeathNamespacesToChildren = inheritNamespaces;
        this.allowNameAsQName = allowQName;
    }

    public Expression getNameExp() {
        return this.nameOp.getChildExpression();
    }

    public Expression getNamespaceExp() {
        return this.namespaceOp == null ? null : this.namespaceOp.getChildExpression();
    }

    protected void setNameExp(Expression elementName) {
        this.nameOp.setChildExpression(elementName);
    }

    protected void setNamespaceExp(Expression namespace) {
        if (this.namespaceOp == null) {
            this.namespaceOp = new Operand(this, namespace, OperandRole.SINGLE_ATOMIC);
        } else {
            this.namespaceOp.setChildExpression(namespace);
        }
    }

    @Override
    public Iterable<Operand> operands() {
        return this.operandSparseList(this.contentOp, this.nameOp, this.namespaceOp);
    }

    public NamespaceResolver getNamespaceResolver() {
        return this.getRetainedStaticContext();
    }

    @Override
    public Expression simplify() throws XPathException {
        this.setNameExp(this.getNameExp().simplify());
        if (this.getNamespaceExp() != null) {
            this.setNamespaceExp(this.getNamespaceExp().simplify());
        }
        Configuration config = this.getConfiguration();
        boolean schemaAware = this.getPackageData().isSchemaAware();
        this.preservingTypes |= !schemaAware;
        SchemaType schemaType = this.getSchemaType();
        if (schemaType != null) {
            this.itemType = new ContentTypeTest(1, schemaType, config, false);
            schemaType.analyzeContentExpression(this.getContentExpression(), 1);
        } else {
            this.itemType = this.getValidationAction() == 4 || !schemaAware ? new ContentTypeTest(1, Untyped.getInstance(), config, false) : NodeKindTest.ELEMENT;
        }
        return super.simplify();
    }

    @Override
    public Expression typeCheck(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        super.typeCheck(visitor, contextInfo);
        Configuration config = visitor.getConfiguration();
        TypeHierarchy th = config.getTypeHierarchy();
        RoleDiagnostic role = new RoleDiagnostic(4, "element/name", 0);
        if (this.allowNameAsQName) {
            this.setNameExp(config.getTypeChecker(false).staticTypeCheck(this.getNameExp(), SequenceType.SINGLE_ATOMIC, role, visitor));
            ItemType supplied = this.getNameExp().getItemType();
            if (th.relationship(supplied, BuiltInAtomicType.STRING) == Affinity.DISJOINT && th.relationship(supplied, BuiltInAtomicType.UNTYPED_ATOMIC) == Affinity.DISJOINT && th.relationship(supplied, BuiltInAtomicType.QNAME) == Affinity.DISJOINT) {
                XPathException de = new XPathException("The name of a constructed element must be a string, QName, or untypedAtomic");
                de.setErrorCode("XPTY0004");
                de.setIsTypeError(true);
                de.setLocation(this.getLocation());
                throw de;
            }
        } else if (!th.isSubType(this.getNameExp().getItemType(), BuiltInAtomicType.STRING)) {
            this.setNameExp(SystemFunction.makeCall("string", this.getRetainedStaticContext(), this.getNameExp()));
        }
        if (Literal.isAtomic(this.getNameExp())) {
            try {
                AtomicValue val = (AtomicValue)((Literal)this.getNameExp()).getValue();
                if (val instanceof StringValue) {
                    String[] parts = NameChecker.checkQNameParts(val.getStringValueCS());
                    if (this.getNamespaceExp() == null) {
                        String prefix = parts[0];
                        String uri = this.getNamespaceResolver().getURIForPrefix(prefix, true);
                        if (uri == null) {
                            XPathException se = new XPathException("Prefix " + prefix + " has not been declared");
                            se.setErrorCode("XPST0081");
                            se.setIsStaticError(true);
                            throw se;
                        }
                        this.setNamespaceExp(new StringLiteral(uri));
                    }
                }
            } catch (XPathException e) {
                String code = e.getErrorCodeLocalPart();
                if (code == null || code.equals("FORG0001")) {
                    e.setErrorCode(this.isXSLT() ? "XTDE0820" : "XQDY0074");
                } else if (code.equals("XPST0081")) {
                    e.setErrorCode(this.isXSLT() ? "XTDE0830" : "XQDY0074");
                }
                e.maybeSetLocation(this.getLocation());
                e.setIsStaticError(true);
                throw e;
            }
        }
        return super.typeCheck(visitor, contextInfo);
    }

    @Override
    public Expression copy(RebindingMap rebindings) {
        ComputedElement ce = new ComputedElement(this.getNameExp().copy(rebindings), this.getNamespaceExp() == null ? null : this.getNamespaceExp().copy(rebindings), this.getSchemaType(), this.getValidationAction(), this.bequeathNamespacesToChildren, this.allowNameAsQName);
        ExpressionTool.copyLocationInfo(this, ce);
        ce.setContentExpression(this.getContentExpression().copy(rebindings));
        return ce;
    }

    @Override
    public ItemType getItemType() {
        if (this.itemType == null) {
            return super.getItemType();
        }
        return this.itemType;
    }

    @Override
    public void checkPermittedContents(SchemaType parentType, boolean whole) throws XPathException {
        if (parentType instanceof SimpleType || ((ComplexType)parentType).isSimpleContent()) {
            String msg = "Elements are not permitted here: the containing element ";
            msg = parentType instanceof SimpleType ? (parentType.isAnonymousType() ? msg + "is defined to have a simple type" : msg + "is of simple type " + parentType.getDescription()) : msg + "has a complex type with simple content";
            XPathException err = new XPathException(msg);
            err.setIsTypeError(true);
            err.setLocation(this.getLocation());
            throw err;
        }
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    @Override
    public NodeName getElementName(XPathContext context, NodeInfo copiedNode) throws XPathException {
        XPathException err;
        String errorCode;
        String localName;
        String prefix;
        String uri;
        block25: {
            AtomicValue nameValue;
            block24: {
                Controller controller = context.getController();
                assert (controller != null);
                uri = null;
                nameValue = (AtomicValue)this.getNameExp().evaluateItem(context);
                if (nameValue == null) {
                    String errorCode2 = this.isXSLT() ? "XTDE0820" : "XPTY0004";
                    XPathException err1 = new XPathException("Invalid element name (empty sequence)", errorCode2, this.getLocation());
                    throw ComputedElement.dynamicError(this.getLocation(), err1, context);
                }
                if (!(nameValue instanceof StringValue)) break block24;
                String rawName = nameValue.getStringValue();
                if ((rawName = Whitespace.trimWhitespace(rawName).toString()).startsWith("Q{") && this.allowNameAsQName) {
                    try {
                        StructuredQName qn = StructuredQName.fromEQName(rawName);
                        prefix = "";
                        localName = qn.getLocalPart();
                        uri = qn.getURI();
                    } catch (IllegalArgumentException e) {
                        throw new XPathException("Invalid EQName in computed element constructor: " + e.getMessage(), "XQDY0074");
                    }
                    if (!NameChecker.isValidNCName(localName)) {
                        throw new XPathException("Local part of EQName in computed element constructor is invalid", "XQDY0074");
                    }
                    break block25;
                } else {
                    try {
                        String[] parts = NameChecker.getQNameParts(rawName);
                        prefix = parts[0];
                        localName = parts[1];
                    } catch (QNameException err2) {
                        String message = "Invalid element name. " + err2.getMessage();
                        if (rawName.length() == 0) {
                            message = "Supplied element name is a zero-length string";
                        }
                        String errorCode3 = this.isXSLT() ? "XTDE0820" : "XQDY0074";
                        XPathException err1 = new XPathException(message, errorCode3, this.getLocation());
                        throw ComputedElement.dynamicError(this.getLocation(), err1, context);
                    }
                }
            }
            if (nameValue instanceof QNameValue && this.allowNameAsQName) {
                localName = ((QNameValue)nameValue).getLocalName();
                uri = ((QNameValue)nameValue).getNamespaceURI();
                prefix = ((QNameValue)nameValue).getPrefix();
                if (prefix.equals("xmlns")) {
                    XPathException err3 = new XPathException("Computed element name has prefix xmlns", "XQDY0096", this.getLocation());
                    throw ComputedElement.dynamicError(this.getLocation(), err3, context);
                }
            } else {
                String errorCode4 = this.isXSLT() ? "XTDE0820" : "XPTY0004";
                XPathException err4 = new XPathException("Computed element name has incorrect type", errorCode4, this.getLocation());
                err4.setIsTypeError(true);
                throw ComputedElement.dynamicError(this.getLocation(), err4, context);
            }
        }
        if (this.getNamespaceExp() == null && uri == null) {
            uri = this.getRetainedStaticContext().getURIForPrefix(prefix, true);
            if (uri == null) {
                errorCode = this.isXSLT() ? "XTDE0830" : (prefix.equals("xmlns") ? "XQDY0096" : "XQDY0074");
                err = new XPathException("Undeclared prefix in element name: " + prefix, errorCode, this.getLocation());
                throw ComputedElement.dynamicError(this.getLocation(), err, context);
            }
        } else {
            if (uri == null) {
                if (this.getNamespaceExp() instanceof StringLiteral) {
                    uri = ((StringLiteral)this.getNamespaceExp()).getStringValue();
                } else {
                    uri = this.getNamespaceExp().evaluateAsString(context).toString();
                    if (!StandardURIChecker.getInstance().isValidURI(uri)) {
                        XPathException de = new XPathException("The value of the namespace attribute must be a valid URI", "XTDE0835", this.getLocation());
                        throw ComputedElement.dynamicError(this.getLocation(), de, context);
                    }
                }
            }
            if (uri.isEmpty()) {
                prefix = "";
            }
            if (prefix.equals("xmlns")) {
                prefix = "x-xmlns";
            }
        }
        if (uri.equals("http://www.w3.org/2000/xmlns/")) {
            errorCode = this.isXSLT() ? "XTDE0835" : "XQDY0096";
            err = new XPathException("Cannot create element in namespace " + uri, errorCode, this.getLocation());
            throw ComputedElement.dynamicError(this.getLocation(), err, context);
        }
        if (uri.equals("http://www.w3.org/XML/1998/namespace") == prefix.equals("xml")) {
            return new FingerprintedQName(prefix, uri, localName);
        }
        String message = prefix.equals("xml") ? "When the prefix is 'xml', the namespace URI must be http://www.w3.org/XML/1998/namespace" : "When the namespace URI is http://www.w3.org/XML/1998/namespace, the prefix must be 'xml'";
        String errorCode5 = this.isXSLT() ? "XTDE0835" : "XQDY0096";
        XPathException err5 = new XPathException(message, errorCode5, this.getLocation());
        throw ComputedElement.dynamicError(this.getLocation(), err5, context);
    }

    public boolean isAllowNameAsQName() {
        return this.allowNameAsQName;
    }

    @Override
    public String getNewBaseURI(XPathContext context, NodeInfo copiedNode) {
        return this.getStaticBaseURIString();
    }

    @Override
    public void outputNamespaceNodes(Outputter out, NodeName nodeName, NodeInfo copiedNode) throws XPathException {
    }

    @Override
    public int getInstructionNameCode() {
        return 151;
    }

    @Override
    public void export(ExpressionPresenter out) throws XPathException {
        out.startElement("compElem", this);
        String flags = this.getInheritanceFlags();
        if (this.isLocal()) {
            flags = flags + "l";
        }
        if (!flags.isEmpty()) {
            out.emitAttribute("flags", flags);
        }
        this.exportValidationAndType(out);
        out.setChildRole("name");
        this.getNameExp().export(out);
        if (this.getNamespaceExp() != null) {
            out.setChildRole("namespace");
            this.getNamespaceExp().export(out);
        }
        out.setChildRole("content");
        this.getContentExpression().export(out);
        out.endElement();
    }
}

