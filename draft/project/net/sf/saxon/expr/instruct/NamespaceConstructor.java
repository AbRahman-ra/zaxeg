/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.instruct;

import net.sf.saxon.event.Outputter;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.Operand;
import net.sf.saxon.expr.OperandRole;
import net.sf.saxon.expr.StaticContext;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.instruct.SimpleNodeConstructor;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.expr.parser.RoleDiagnostic;
import net.sf.saxon.lib.StandardURIChecker;
import net.sf.saxon.om.NameChecker;
import net.sf.saxon.om.NoNamespaceName;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.pattern.NodeKindTest;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.value.AnyURIValue;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;
import net.sf.saxon.value.Whitespace;

public class NamespaceConstructor
extends SimpleNodeConstructor {
    private Operand nameOp;

    public NamespaceConstructor(Expression name) {
        this.nameOp = new Operand(this, name, OperandRole.SINGLE_ATOMIC);
    }

    public Expression getNameExp() {
        return this.nameOp.getChildExpression();
    }

    public void setNameExp(Expression nameExp) {
        this.nameOp.setChildExpression(nameExp);
    }

    @Override
    public Iterable<Operand> operands() {
        return this.operandList(this.selectOp, this.nameOp);
    }

    @Override
    public int getInstructionNameCode() {
        return 175;
    }

    @Override
    public ItemType getItemType() {
        return NodeKindTest.NAMESPACE;
    }

    @Override
    public int getCardinality() {
        return 16384;
    }

    @Override
    public void localTypeCheck(ExpressionVisitor visitor, ContextItemStaticInfo contextItemType) throws XPathException {
        StaticContext env = visitor.getStaticContext();
        this.nameOp.typeCheck(visitor, contextItemType);
        RoleDiagnostic role = new RoleDiagnostic(4, "namespace/name", 0);
        this.setNameExp(env.getConfiguration().getTypeChecker(false).staticTypeCheck(this.getNameExp(), SequenceType.OPTIONAL_ATOMIC, role, visitor));
        this.adoptChildExpression(this.getNameExp());
        if (this.getNameExp() instanceof Literal) {
            this.evaluatePrefix(env.makeEarlyEvaluationContext());
        }
    }

    @Override
    public Expression copy(RebindingMap rebindings) {
        NamespaceConstructor exp = new NamespaceConstructor(this.getNameExp().copy(rebindings));
        exp.setSelect(this.getSelect().copy(rebindings));
        ExpressionTool.copyLocationInfo(this, exp);
        return exp;
    }

    @Override
    public NodeName evaluateNodeName(XPathContext context) throws XPathException {
        String prefix = this.evaluatePrefix(context);
        return new NoNamespaceName(prefix);
    }

    private String evaluatePrefix(XPathContext context) throws XPathException {
        AtomicValue value = (AtomicValue)this.getNameExp().evaluateItem(context);
        if (value == null) {
            return "";
        }
        if (!(value instanceof StringValue) || value instanceof AnyURIValue) {
            XPathException err = new XPathException("Namespace prefix is not an xs:string or xs:untypedAtomic", "XPTY0004", this.getLocation());
            err.setIsTypeError(true);
            throw NamespaceConstructor.dynamicError(this.getLocation(), err, context);
        }
        String prefix = Whitespace.trim(value.getStringValueCS());
        if (!prefix.isEmpty() && !NameChecker.isValidNCName(prefix)) {
            String errorCode = this.isXSLT() ? "XTDE0920" : "XQDY0074";
            XPathException err = new XPathException("Namespace prefix is invalid: " + prefix, errorCode, this.getLocation());
            throw NamespaceConstructor.dynamicError(this.getLocation(), err, context);
        }
        if (prefix.equals("xmlns")) {
            String errorCode = this.isXSLT() ? "XTDE0920" : "XQDY0101";
            XPathException err = new XPathException("Namespace prefix 'xmlns' is not allowed", errorCode, this.getLocation());
            throw NamespaceConstructor.dynamicError(this.getLocation(), err, context);
        }
        return prefix;
    }

    @Override
    public void processValue(CharSequence value, Outputter output, XPathContext context) throws XPathException {
        String prefix = this.evaluatePrefix(context);
        String uri = value.toString();
        this.checkPrefixAndUri(prefix, uri, context);
        output.namespace(prefix, uri, 32);
    }

    @Override
    public NodeInfo evaluateItem(XPathContext context) throws XPathException {
        NodeInfo node = (NodeInfo)super.evaluateItem(context);
        assert (node != null);
        String prefix = node.getLocalPart();
        String uri = node.getStringValue();
        this.checkPrefixAndUri(prefix, uri, context);
        return node;
    }

    private void checkPrefixAndUri(String prefix, String uri, XPathContext context) throws XPathException {
        if (prefix.equals("xml") != uri.equals("http://www.w3.org/XML/1998/namespace")) {
            String errorCode = this.isXSLT() ? "XTDE0925" : "XQDY0101";
            XPathException err = new XPathException("Namespace prefix 'xml' and namespace uri http://www.w3.org/XML/1998/namespace must only be used together", errorCode, this.getLocation());
            throw NamespaceConstructor.dynamicError(this.getLocation(), err, context);
        }
        if (uri.isEmpty()) {
            String errorCode = this.isXSLT() ? "XTDE0930" : "XQDY0101";
            XPathException err = new XPathException("Namespace URI is an empty string", errorCode, this.getLocation());
            throw NamespaceConstructor.dynamicError(this.getLocation(), err, context);
        }
        if (uri.equals("http://www.w3.org/2000/xmlns/")) {
            String errorCode = this.isXSLT() ? "XTDE0905" : "XQDY0101";
            XPathException err = new XPathException("A namespace node cannot have the reserved namespace http://www.w3.org/2000/xmlns/", errorCode, this.getLocation());
            throw NamespaceConstructor.dynamicError(this.getLocation(), err, context);
        }
        if (context.getConfiguration().getXsdVersion() == 10 && !StandardURIChecker.getInstance().isValidURI(uri)) {
            XPathException de = new XPathException("The string value of the constructed namespace node must be a valid URI", "XTDE0905", this.getLocation());
            throw NamespaceConstructor.dynamicError(this.getLocation(), de, context);
        }
    }

    @Override
    public void export(ExpressionPresenter out) throws XPathException {
        out.startElement("namespace", this);
        String flags = "";
        if (this.isLocal()) {
            flags = flags + "l";
        }
        if (!flags.isEmpty()) {
            out.emitAttribute("flags", flags);
        }
        out.setChildRole("name");
        this.getNameExp().export(out);
        out.setChildRole("select");
        this.getSelect().export(out);
        out.endElement();
    }
}

