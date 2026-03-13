/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr;

import java.util.Collections;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.Operand;
import net.sf.saxon.expr.OperandRole;
import net.sf.saxon.expr.StaticContext;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.oper.OperandArray;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.Optimizer;
import net.sf.saxon.expr.parser.PathMap;
import net.sf.saxon.expr.parser.RoleDiagnostic;
import net.sf.saxon.expr.parser.TypeChecker;
import net.sf.saxon.om.Function;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.NoDynamicContextException;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.type.JavaExternalObjectType;
import net.sf.saxon.value.SequenceType;

public abstract class FunctionCall
extends Expression {
    private OperandArray operanda;

    protected void setOperanda(OperandArray operanda) {
        this.operanda = operanda;
    }

    public OperandArray getOperanda() {
        return this.operanda;
    }

    @Override
    public Iterable<Operand> operands() {
        if (this.operanda != null) {
            return this.operanda.operands();
        }
        return Collections.emptyList();
    }

    public abstract Function getTargetFunction(XPathContext var1) throws XPathException;

    public abstract StructuredQName getFunctionName();

    public final int getArity() {
        return this.getOperanda().getNumberOfOperands();
    }

    public void setArguments(Expression[] args) {
        this.setOperanda(new OperandArray(this, args));
    }

    protected void setOperanda(Expression[] args, OperandRole[] roles) {
        this.setOperanda(new OperandArray((Expression)this, args, roles));
    }

    public Expression[] getArguments() {
        Expression[] result = new Expression[this.getArity()];
        int i = 0;
        for (Operand o : this.operands()) {
            result[i++] = o.getChildExpression();
        }
        return result;
    }

    public Expression getArg(int n) {
        return this.getOperanda().getOperandExpression(n);
    }

    public void setArg(int n, Expression child) {
        this.getOperanda().setOperand(n, child);
        this.adoptChildExpression(child);
    }

    protected final Expression simplifyArguments(StaticContext env) throws XPathException {
        for (int i = 0; i < this.getArguments().length; ++i) {
            Expression exp = this.getArg(i).simplify();
            if (exp == this.getArg(i)) continue;
            this.adoptChildExpression(exp);
            this.setArg(i, exp);
        }
        return this;
    }

    @Override
    public Expression typeCheck(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        this.typeCheckChildren(visitor, contextInfo);
        this.checkArguments(visitor);
        return this.preEvaluateIfConstant(visitor);
    }

    protected Expression preEvaluateIfConstant(ExpressionVisitor visitor) throws XPathException {
        Optimizer opt = visitor.obtainOptimizer();
        if (opt.isOptionSet(32768)) {
            boolean fixed = true;
            for (Operand o : this.operands()) {
                if (o.getChildExpression() instanceof Literal) continue;
                fixed = false;
            }
            if (fixed) {
                try {
                    return this.preEvaluate(visitor);
                } catch (NoDynamicContextException err) {
                    return this;
                }
            }
        }
        return this;
    }

    public void checkFunctionCall(Function target, ExpressionVisitor visitor) throws XPathException {
        TypeChecker tc = visitor.getConfiguration().getTypeChecker(visitor.getStaticContext().isInBackwardsCompatibleMode());
        SequenceType[] argTypes = target.getFunctionItemType().getArgumentTypes();
        int n = target.getArity();
        for (int i = 0; i < n; ++i) {
            String name = this.getFunctionName() == null ? "" : this.getFunctionName().getDisplayName();
            RoleDiagnostic role = new RoleDiagnostic(0, name, i);
            this.setArg(i, tc.staticTypeCheck(this.getArg(i), argTypes[i], role, visitor));
        }
    }

    @Override
    public Expression optimize(ExpressionVisitor visitor, ContextItemStaticInfo contextItemType) throws XPathException {
        this.optimizeChildren(visitor, contextItemType);
        Optimizer opt = visitor.obtainOptimizer();
        if (opt.isOptionSet(32768)) {
            boolean fixed = true;
            for (Operand o : this.operands()) {
                if (o.getChildExpression() instanceof Literal) continue;
                fixed = false;
                break;
            }
            if (fixed) {
                return this.preEvaluate(visitor);
            }
        }
        return this;
    }

    @Override
    public int getNetCost() {
        return 5;
    }

    public Expression preEvaluate(ExpressionVisitor visitor) throws XPathException {
        if ((this.getIntrinsicDependencies() & 0xFFFFF7FF) != 0) {
            return this;
        }
        try {
            Literal lit = Literal.makeLiteral(this.iterate(visitor.getStaticContext().makeEarlyEvaluationContext()).materialize(), this);
            Optimizer.trace(visitor.getConfiguration(), "Pre-evaluated function call " + this.toShortString(), lit);
            return lit;
        } catch (NoDynamicContextException e) {
            return this;
        } catch (UnsupportedOperationException e) {
            if (e.getCause() instanceof NoDynamicContextException) {
                return this;
            }
            throw e;
        }
    }

    protected void checkArguments(ExpressionVisitor visitor) throws XPathException {
    }

    protected int checkArgumentCount(int min, int max) throws XPathException {
        int numArgs = this.getArity();
        String msg = null;
        if (min == max && numArgs != min) {
            msg = "Function " + this.getDisplayName() + " must have " + FunctionCall.pluralArguments(min);
        } else if (numArgs < min) {
            msg = "Function " + this.getDisplayName() + " must have at least " + FunctionCall.pluralArguments(min);
        } else if (numArgs > max) {
            msg = "Function " + this.getDisplayName() + " must have no more than " + FunctionCall.pluralArguments(max);
        }
        if (msg != null) {
            XPathException err = new XPathException(msg, "XPST0017");
            err.setIsStaticError(true);
            err.setLocation(this.getLocation());
            throw err;
        }
        return numArgs;
    }

    @Override
    public int getImplementationMethod() {
        return 2;
    }

    public static String pluralArguments(int num) {
        return num == 1 ? "one argument" : num + " arguments";
    }

    public PathMap.PathMapNodeSet addExternalFunctionCallToPathMap(PathMap pathMap, PathMap.PathMapNodeSet pathMapNodes) {
        PathMap.PathMapNodeSet result = new PathMap.PathMapNodeSet();
        for (Operand o : this.operands()) {
            result.addNodeSet(o.getChildExpression().addToPathMap(pathMap, pathMapNodes));
        }
        result.setHasUnknownDependencies();
        return result;
    }

    @Override
    public String getExpressionName() {
        return "functionCall";
    }

    public final String getDisplayName() {
        StructuredQName fName = this.getFunctionName();
        return fName == null ? "(anonymous)" : fName.getDisplayName();
    }

    @Override
    public String toString() {
        FastStringBuffer buff = new FastStringBuffer(64);
        StructuredQName fName = this.getFunctionName();
        String f = fName == null ? "$anonymousFunction" : (fName.hasURI("http://www.w3.org/2005/xpath-functions") ? fName.getLocalPart() : fName.getEQName());
        buff.append(f);
        boolean first = true;
        for (Operand o : this.operands()) {
            buff.append(first ? "(" : ", ");
            buff.append(o.getChildExpression().toString());
            first = false;
        }
        buff.append(first ? "()" : ")");
        return buff.toString();
    }

    @Override
    public String toShortString() {
        StructuredQName fName = this.getFunctionName();
        return (fName == null ? "$anonFn" : fName.getDisplayName()) + "(" + (this.getArity() == 0 ? "" : "...") + ")";
    }

    @Override
    public void export(ExpressionPresenter out) throws XPathException {
        out.startElement("functionCall", this);
        if (this.getFunctionName() == null) {
            throw new AssertionError((Object)"Exporting call to anonymous function");
        }
        out.emitAttribute("name", this.getFunctionName().getDisplayName());
        for (Operand o : this.operands()) {
            o.getChildExpression().export(out);
        }
        out.endElement();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof FunctionCall)) {
            return false;
        }
        if (this.getFunctionName() == null) {
            return this == o;
        }
        FunctionCall f = (FunctionCall)o;
        if (!this.getFunctionName().equals(f.getFunctionName())) {
            return false;
        }
        if (this.getArity() != f.getArity()) {
            return false;
        }
        for (int i = 0; i < this.getArity(); ++i) {
            if (this.getArg(i).isEqual(f.getArg(i))) continue;
            return false;
        }
        return true;
    }

    @Override
    public int computeHashCode() {
        if (this.getFunctionName() == null) {
            return super.computeHashCode();
        }
        int h = this.getFunctionName().hashCode();
        for (int i = 0; i < this.getArity(); ++i) {
            h ^= this.getArg(i).hashCode();
        }
        return h;
    }

    @Override
    public SequenceIterator iterate(XPathContext context) throws XPathException {
        Function target = this.getTargetFunction(context);
        Sequence[] actualArgs = this.evaluateArguments(context);
        try {
            return target.call(context, actualArgs).iterate();
        } catch (XPathException e) {
            e.maybeSetLocation(this.getLocation());
            e.maybeSetContext(context);
            e.maybeSetFailingExpression(this);
            throw e;
        }
    }

    public Sequence[] evaluateArguments(XPathContext context) throws XPathException {
        int numArgs = this.getArity();
        Sequence[] actualArgs = new Sequence[numArgs];
        for (int i = 0; i < numArgs; ++i) {
            actualArgs[i] = ExpressionTool.lazyEvaluate(this.getArg(i), context, false);
        }
        return actualArgs;
    }

    public boolean adjustRequiredType(JavaExternalObjectType requiredType) throws XPathException {
        return false;
    }
}

