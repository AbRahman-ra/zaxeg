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
import net.sf.saxon.expr.StringLiteral;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.instruct.SimpleNodeConstructor;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.expr.parser.RoleDiagnostic;
import net.sf.saxon.om.NameChecker;
import net.sf.saxon.om.NoNamespaceName;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.pattern.NodeKindTest;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.Err;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.value.AnyURIValue;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;
import net.sf.saxon.value.Whitespace;

public class ProcessingInstruction
extends SimpleNodeConstructor {
    private Operand nameOp;

    public ProcessingInstruction(Expression name) {
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
        return 192;
    }

    @Override
    public ItemType getItemType() {
        return NodeKindTest.PROCESSING_INSTRUCTION;
    }

    @Override
    public int getCardinality() {
        return 16384;
    }

    @Override
    public Expression copy(RebindingMap rebindings) {
        ProcessingInstruction exp = new ProcessingInstruction(this.getNameExp().copy(rebindings));
        ExpressionTool.copyLocationInfo(this, exp);
        exp.setSelect(this.getSelect().copy(rebindings));
        return exp;
    }

    @Override
    public void localTypeCheck(ExpressionVisitor visitor, ContextItemStaticInfo contextItemType) throws XPathException {
        String s;
        String s2;
        StaticContext env = visitor.getStaticContext();
        this.nameOp.typeCheck(visitor, contextItemType);
        RoleDiagnostic role = new RoleDiagnostic(4, "processing-instruction/name", 0);
        this.setNameExp(visitor.getConfiguration().getTypeChecker(false).staticTypeCheck(this.getNameExp(), SequenceType.SINGLE_ATOMIC, role, visitor));
        Expression nameExp = this.getNameExp();
        this.adoptChildExpression(nameExp);
        if (nameExp instanceof Literal && ((Literal)nameExp).getValue() instanceof AtomicValue) {
            AtomicValue val = (AtomicValue)((Literal)nameExp).getValue();
            this.checkName(val, env.makeEarlyEvaluationContext());
        }
        if (this.getSelect() instanceof Literal && !(s2 = this.checkContent(s = ((Literal)this.getSelect()).getValue().getStringValue(), env.makeEarlyEvaluationContext())).equals(s)) {
            this.setSelect(new StringLiteral(s2));
        }
    }

    @Override
    public int getDependencies() {
        return this.getNameExp().getDependencies() | super.getDependencies();
    }

    @Override
    public void processValue(CharSequence value, Outputter output, XPathContext context) throws XPathException {
        String expandedName = this.evaluateName(context);
        if (expandedName != null) {
            String data = this.checkContent(value.toString(), context);
            output.processingInstruction(expandedName, data, this.getLocation(), 0);
        }
    }

    @Override
    protected String checkContent(String data, XPathContext context) throws XPathException {
        if (this.isXSLT()) {
            return ProcessingInstruction.checkContentXSLT(data);
        }
        try {
            return ProcessingInstruction.checkContentXQuery(data);
        } catch (XPathException err) {
            err.setXPathContext(context);
            err.setLocation(this.getLocation());
            throw err;
        }
    }

    public static String checkContentXSLT(String data) {
        int hh;
        while ((hh = data.indexOf("?>")) >= 0) {
            data = data.substring(0, hh + 1) + ' ' + data.substring(hh + 1);
        }
        return Whitespace.removeLeadingWhitespace(data).toString();
    }

    public static String checkContentXQuery(String data) throws XPathException {
        if (data.contains("?>")) {
            throw new XPathException("Invalid characters (?>) in processing instruction", "XQDY0026");
        }
        return Whitespace.removeLeadingWhitespace(data).toString();
    }

    @Override
    public NodeName evaluateNodeName(XPathContext context) throws XPathException {
        String expandedName = this.evaluateName(context);
        return new NoNamespaceName(expandedName);
    }

    private String evaluateName(XPathContext context) throws XPathException {
        AtomicValue av = (AtomicValue)this.getNameExp().evaluateItem(context);
        if (av instanceof StringValue && !(av instanceof AnyURIValue)) {
            return this.checkName(av, context);
        }
        XPathException e = new XPathException("Processing instruction name is not a string");
        e.setXPathContext(context);
        e.setErrorCode("XPTY0004");
        throw ProcessingInstruction.dynamicError(this.getLocation(), e, context);
    }

    private String checkName(AtomicValue name, XPathContext context) throws XPathException {
        if (name instanceof StringValue && !(name instanceof AnyURIValue)) {
            String expandedName = Whitespace.trim(name.getStringValue());
            if (!NameChecker.isValidNCName(expandedName)) {
                XPathException e = new XPathException("Processing instruction name " + Err.wrap(expandedName) + " is not a valid NCName");
                e.setXPathContext(context);
                e.setErrorCode(this.isXSLT() ? "XTDE0890" : "XQDY0041");
                throw ProcessingInstruction.dynamicError(this.getLocation(), e, context);
            }
            if (expandedName.equalsIgnoreCase("xml")) {
                XPathException e = new XPathException("Processing instructions cannot be named 'xml' in any combination of upper/lower case");
                e.setXPathContext(context);
                e.setErrorCode(this.isXSLT() ? "XTDE0890" : "XQDY0064");
                throw ProcessingInstruction.dynamicError(this.getLocation(), e, context);
            }
            return expandedName;
        }
        XPathException e = new XPathException("Processing instruction name " + Err.wrap(name.getStringValue()) + " is not of type xs:string or xs:untypedAtomic");
        e.setXPathContext(context);
        e.setErrorCode("XPTY0004");
        e.setIsTypeError(true);
        throw ProcessingInstruction.dynamicError(this.getLocation(), e, context);
    }

    @Override
    public void export(ExpressionPresenter out) throws XPathException {
        out.startElement("procInst", this);
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

