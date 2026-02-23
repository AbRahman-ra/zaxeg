/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.oper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.function.Predicate;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.Operand;
import net.sf.saxon.expr.OperandRole;

public class OperandArray
implements Iterable<Operand> {
    private Operand[] operandArray;

    public OperandArray(Expression parent, Expression[] args) {
        this.operandArray = new Operand[args.length];
        for (int i = 0; i < args.length; ++i) {
            this.operandArray[i] = new Operand(parent, args[i], OperandRole.NAVIGATE);
        }
    }

    public OperandArray(Expression parent, Expression[] args, OperandRole[] roles) {
        this.operandArray = new Operand[args.length];
        for (int i = 0; i < args.length; ++i) {
            this.operandArray[i] = new Operand(parent, args[i], roles[i]);
        }
    }

    public OperandArray(Expression parent, Expression[] args, OperandRole role) {
        this.operandArray = new Operand[args.length];
        for (int i = 0; i < args.length; ++i) {
            this.operandArray[i] = new Operand(parent, args[i], role);
        }
    }

    private OperandArray(Operand[] operands) {
        this.operandArray = operands;
    }

    @Override
    public Iterator<Operand> iterator() {
        return Arrays.asList(this.operandArray).iterator();
    }

    public Operand[] copy() {
        return Arrays.copyOf(this.operandArray, this.operandArray.length);
    }

    public OperandRole[] getRoles() {
        OperandRole[] or = new OperandRole[this.operandArray.length];
        for (int i = 0; i < or.length; ++i) {
            or[i] = this.operandArray[i].getOperandRole();
        }
        return or;
    }

    public Operand getOperand(int n) {
        try {
            return this.operandArray[n];
        } catch (ArrayIndexOutOfBoundsException a) {
            throw new IllegalArgumentException();
        }
    }

    public Expression getOperandExpression(int n) {
        try {
            return this.operandArray[n].getChildExpression();
        } catch (ArrayIndexOutOfBoundsException a) {
            throw new IllegalArgumentException(a);
        }
    }

    public Iterable<Operand> operands() {
        return Arrays.asList(this.operandArray);
    }

    public Iterable<Expression> operandExpressions() {
        ArrayList<Expression> list = new ArrayList<Expression>(this.operandArray.length);
        for (Operand o : this.operands()) {
            list.add(o.getChildExpression());
        }
        return list;
    }

    public void setOperand(int n, Expression child) {
        try {
            if (this.operandArray[n].getChildExpression() != child) {
                this.operandArray[n].setChildExpression(child);
            }
        } catch (ArrayIndexOutOfBoundsException a) {
            throw new IllegalArgumentException();
        }
    }

    public int getNumberOfOperands() {
        return this.operandArray.length;
    }

    public static <T> boolean every(T[] args, Predicate<T> condition) {
        for (T arg : args) {
            if (condition.test(arg)) continue;
            return false;
        }
        return true;
    }

    public static <T> boolean some(T[] args, Predicate<T> condition) {
        for (T arg : args) {
            if (!condition.test(arg)) continue;
            return true;
        }
        return false;
    }
}

