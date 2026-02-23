/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.OperandRole;
import net.sf.saxon.expr.UnaryExpression;
import net.sf.saxon.expr.parser.XPathParser;
import net.sf.saxon.om.NamespaceResolver;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.AtomicType;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.Converter;
import net.sf.saxon.value.SequenceType;

public abstract class CastingExpression
extends UnaryExpression {
    private AtomicType targetType;
    private AtomicType targetPrimitiveType;
    private boolean allowEmpty = false;
    protected Converter converter;
    private boolean operandIsStringLiteral = false;

    public CastingExpression(Expression source, AtomicType target, boolean allowEmpty) {
        super(source);
        this.allowEmpty = allowEmpty;
        this.targetType = target;
        this.targetPrimitiveType = target.getPrimitiveItemType();
    }

    public AtomicType getTargetPrimitiveType() {
        return this.targetPrimitiveType;
    }

    public void setTargetType(AtomicType type) {
        this.targetType = type;
    }

    public AtomicType getTargetType() {
        return this.targetType;
    }

    @Override
    protected OperandRole getOperandRole() {
        return OperandRole.SINGLE_ATOMIC;
    }

    public void setAllowEmpty(boolean allow) {
        this.allowEmpty = allow;
    }

    public boolean allowsEmpty() {
        return this.allowEmpty;
    }

    public void setOperandIsStringLiteral(boolean option) {
        this.operandIsStringLiteral = option;
    }

    public boolean isOperandIsStringLiteral() {
        return this.operandIsStringLiteral;
    }

    public Converter getConverter() {
        return this.converter;
    }

    public NamespaceResolver getNamespaceResolver() {
        return this.getRetainedStaticContext();
    }

    @Override
    public int getIntrinsicDependencies() {
        return this.getTargetType().isNamespaceSensitive() ? 2048 : 0;
    }

    @Override
    public Expression simplify() throws XPathException {
        String s;
        if (this.targetType instanceof BuiltInAtomicType && (s = XPathParser.whyDisallowedType(this.getPackageData(), (BuiltInAtomicType)this.targetType)) != null) {
            XPathException err = new XPathException(s, "XPST0080", this.getLocation());
            err.setIsStaticError(true);
            throw err;
        }
        this.setBaseExpression(this.getBaseExpression().simplify());
        return this;
    }

    @Override
    public int computeSpecialProperties() {
        int p = super.computeSpecialProperties();
        return p | 0x800000;
    }

    protected void export(ExpressionPresenter out, String elemName) throws XPathException {
        out.startElement(elemName, this);
        int card = this.allowsEmpty() ? 24576 : 16384;
        SequenceType st = SequenceType.makeSequenceType(this.getTargetType(), card);
        out.emitAttribute("flags", "a" + (this.allowsEmpty() ? "e" : ""));
        out.emitAttribute("as", st.toAlphaCode());
        this.getBaseExpression().export(out);
        out.endElement();
    }
}

