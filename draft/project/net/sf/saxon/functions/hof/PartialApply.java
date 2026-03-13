/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions.hof;

import java.util.ArrayList;
import java.util.Arrays;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.Operand;
import net.sf.saxon.expr.OperandRole;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.expr.parser.RoleDiagnostic;
import net.sf.saxon.expr.parser.TypeChecker;
import net.sf.saxon.functions.hof.CurriedFunction;
import net.sf.saxon.om.Function;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.type.AnyFunctionType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.SpecificFunctionType;
import net.sf.saxon.value.SequenceType;

public class PartialApply
extends Expression {
    private Operand baseOp;
    private Operand[] boundArgumentsOp;

    public PartialApply(Expression base, Expression[] boundArguments) {
        this.baseOp = new Operand(this, base, OperandRole.INSPECT);
        this.adoptChildExpression(base);
        this.boundArgumentsOp = new Operand[boundArguments.length];
        for (int i = 0; i < boundArguments.length; ++i) {
            if (boundArguments[i] == null) continue;
            this.boundArgumentsOp[i] = new Operand(this, boundArguments[i], OperandRole.NAVIGATE);
            this.adoptChildExpression(boundArguments[i]);
        }
    }

    public Expression getBaseExpression() {
        return this.baseOp.getChildExpression();
    }

    public void setBaseExpression(Expression base) {
        this.baseOp.setChildExpression(base);
    }

    public int getNumberOfPlaceHolders() {
        int n = 0;
        for (Operand o : this.boundArgumentsOp) {
            if (o != null) continue;
            ++n;
        }
        return n;
    }

    @Override
    public Expression typeCheck(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        this.typeCheckChildren(visitor, contextInfo);
        ItemType baseType = this.getBaseExpression().getItemType();
        Object[] argTypes = new SequenceType[this.boundArgumentsOp.length];
        Arrays.fill(argTypes, SequenceType.ANY_SEQUENCE);
        TypeChecker tc = visitor.getConfiguration().getTypeChecker(false);
        for (int i = 0; i < this.boundArgumentsOp.length; ++i) {
            Operand op = this.boundArgumentsOp[i];
            if (op == null) continue;
            Expression arg = op.getChildExpression();
            if (!(baseType instanceof SpecificFunctionType) || i >= ((SpecificFunctionType)baseType).getArity()) continue;
            RoleDiagnostic role = new RoleDiagnostic(0, "saxon:call", i);
            SequenceType requiredArgType = ((SpecificFunctionType)baseType).getArgumentTypes()[i];
            argTypes[i] = requiredArgType;
            Expression a3 = tc.staticTypeCheck(arg, requiredArgType, role, visitor);
            if (a3 == arg) continue;
            op.setChildExpression(a3);
        }
        SequenceType requiredFunctionType = SequenceType.makeSequenceType(new SpecificFunctionType((SequenceType[])argTypes, baseType instanceof AnyFunctionType ? ((AnyFunctionType)baseType).getResultType() : SequenceType.ANY_SEQUENCE), 16384);
        RoleDiagnostic role = new RoleDiagnostic(0, "saxon:call", 0);
        this.setBaseExpression(tc.staticTypeCheck(this.getBaseExpression(), requiredFunctionType, role, visitor));
        return this;
    }

    @Override
    public ItemType getItemType() {
        ItemType baseItemType = this.getBaseExpression().getItemType();
        SequenceType resultType = SequenceType.ANY_SEQUENCE;
        if (baseItemType instanceof SpecificFunctionType) {
            resultType = ((SpecificFunctionType)baseItemType).getResultType();
        }
        int placeholders = this.getNumberOfPlaceHolders();
        Object[] argTypes = new SequenceType[placeholders];
        if (baseItemType instanceof SpecificFunctionType) {
            int j = 0;
            for (int i = 0; i < this.boundArgumentsOp.length; ++i) {
                if (this.boundArgumentsOp[i] != null) continue;
                argTypes[j++] = ((SpecificFunctionType)baseItemType).getArgumentTypes()[i];
            }
        } else {
            Arrays.fill(argTypes, SequenceType.ANY_SEQUENCE);
        }
        return new SpecificFunctionType((SequenceType[])argTypes, resultType);
    }

    @Override
    public Iterable<Operand> operands() {
        ArrayList<Operand> operanda = new ArrayList<Operand>(this.boundArgumentsOp.length + 1);
        operanda.add(this.baseOp);
        for (Operand o : this.boundArgumentsOp) {
            if (o == null) continue;
            operanda.add(o);
        }
        return operanda;
    }

    public int getNumberOfArguments() {
        return this.boundArgumentsOp.length;
    }

    public Expression getArgument(int n) {
        Operand o = this.boundArgumentsOp[n];
        return o == null ? null : o.getChildExpression();
    }

    @Override
    public int getImplementationMethod() {
        return 1;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof PartialApply)) {
            return false;
        }
        PartialApply pa2 = (PartialApply)other;
        if (!this.getBaseExpression().isEqual(pa2.getBaseExpression())) {
            return false;
        }
        if (this.boundArgumentsOp.length != pa2.boundArgumentsOp.length) {
            return false;
        }
        for (int i = 0; i < this.boundArgumentsOp.length; ++i) {
            if (this.boundArgumentsOp[i] == null != (pa2.boundArgumentsOp[i] == null)) {
                return false;
            }
            if (this.boundArgumentsOp[i] == null || this.boundArgumentsOp[i].equals(pa2.boundArgumentsOp[i])) continue;
            return false;
        }
        return true;
    }

    @Override
    public int computeHashCode() {
        int h = -2090102112;
        int i = 0;
        for (Operand o : this.operands()) {
            h ^= o == null ? i++ : o.getChildExpression().hashCode();
        }
        return h;
    }

    @Override
    public void export(ExpressionPresenter out) throws XPathException {
        out.startElement("partialApply", this);
        this.getBaseExpression().export(out);
        for (Operand o : this.boundArgumentsOp) {
            if (o == null) {
                out.startElement("null", this);
                out.endElement();
                continue;
            }
            o.getChildExpression().export(out);
        }
        out.endElement();
    }

    @Override
    protected int computeCardinality() {
        return 16384;
    }

    @Override
    public Expression copy(RebindingMap rebindings) {
        Expression[] boundArgumentsCopy = new Expression[this.boundArgumentsOp.length];
        for (int i = 0; i < this.boundArgumentsOp.length; ++i) {
            boundArgumentsCopy[i] = this.boundArgumentsOp[i] == null ? null : this.boundArgumentsOp[i].getChildExpression().copy(rebindings);
        }
        PartialApply exp = new PartialApply(this.getBaseExpression().copy(rebindings), boundArgumentsCopy);
        ExpressionTool.copyLocationInfo(this, exp);
        return exp;
    }

    @Override
    public String toString() {
        FastStringBuffer buff = new FastStringBuffer(64);
        boolean par = this.getBaseExpression().operands().iterator().hasNext();
        if (par) {
            buff.append("(" + this.getBaseExpression().toString() + ")");
        } else {
            buff.append(this.getBaseExpression().toString());
        }
        buff.append("(");
        for (int i = 0; i < this.boundArgumentsOp.length; ++i) {
            if (this.boundArgumentsOp[i] == null) {
                buff.append("?");
            } else {
                buff.append(this.boundArgumentsOp[i].getChildExpression().toString());
            }
            if (i == this.boundArgumentsOp.length - 1) continue;
            buff.append(", ");
        }
        buff.append(")");
        return buff.toString();
    }

    @Override
    public Function evaluateItem(XPathContext context) throws XPathException {
        Function f = (Function)this.getBaseExpression().evaluateItem(context);
        assert (f != null);
        Sequence[] values = new Sequence[this.boundArgumentsOp.length];
        for (int i = 0; i < this.boundArgumentsOp.length; ++i) {
            values[i] = this.boundArgumentsOp[i] == null ? null : this.boundArgumentsOp[i].getChildExpression().iterate(context).materialize();
        }
        return new CurriedFunction(f, values);
    }

    @Override
    public String getExpressionName() {
        return "partialApply";
    }
}

