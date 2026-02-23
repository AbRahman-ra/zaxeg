/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.instruct;

import java.util.ArrayList;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.Operand;
import net.sf.saxon.expr.OperandRole;
import net.sf.saxon.expr.OperandUsage;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.pattern.Pattern;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.ListIterator;
import net.sf.saxon.tree.util.Navigator;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.value.Int64Value;
import net.sf.saxon.value.SequenceType;

public class NumberInstruction
extends Expression {
    public static final int SINGLE = 0;
    public static final int MULTI = 1;
    public static final int ANY = 2;
    public static final int SIMPLE = 3;
    public static final String[] LEVEL_NAMES = new String[]{"single", "multi", "any", "simple"};
    private Operand selectOp;
    private int level;
    private Operand countOp;
    private Operand fromOp;
    private boolean hasVariablesInPatterns = false;

    public NumberInstruction(Expression select, int level, Pattern count, Pattern from) {
        assert (select != null);
        this.selectOp = new Operand(this, select, new OperandRole(0, OperandUsage.NAVIGATION, SequenceType.SINGLE_NODE));
        this.level = level;
        if (count != null) {
            this.countOp = new Operand(this, count, OperandRole.INSPECT);
        }
        if (from != null) {
            this.fromOp = new Operand(this, from, OperandRole.INSPECT);
        }
        this.hasVariablesInPatterns = Pattern.patternContainsVariable(count) || Pattern.patternContainsVariable(from);
    }

    @Override
    public boolean isInstruction() {
        return true;
    }

    @Override
    public Iterable<Operand> operands() {
        return this.operandSparseList(this.selectOp, this.countOp, this.fromOp);
    }

    public int getLevel() {
        return this.level;
    }

    public Pattern getCount() {
        return this.countOp == null ? null : (Pattern)this.countOp.getChildExpression();
    }

    public Pattern getFrom() {
        return this.fromOp == null ? null : (Pattern)this.fromOp.getChildExpression();
    }

    public Expression getSelect() {
        return this.selectOp.getChildExpression();
    }

    @Override
    public Expression copy(RebindingMap rebindings) {
        NumberInstruction exp = new NumberInstruction(this.copy(this.selectOp, rebindings), this.level, this.copy(this.getCount(), rebindings), this.copy(this.getFrom(), rebindings));
        ExpressionTool.copyLocationInfo(this, exp);
        return exp;
    }

    private Expression copy(Operand op, RebindingMap rebindings) {
        return op == null ? null : op.getChildExpression().copy(rebindings);
    }

    private Pattern copy(Pattern op, RebindingMap rebindings) {
        return op == null ? null : op.copy(rebindings);
    }

    @Override
    public ItemType getItemType() {
        return BuiltInAtomicType.INTEGER;
    }

    @Override
    public int computeCardinality() {
        switch (this.level) {
            case 0: 
            case 2: 
            case 3: {
                return 24576;
            }
        }
        return 57344;
    }

    @Override
    public int getImplementationMethod() {
        return 2;
    }

    @Override
    public Expression optimize(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        Expression e = super.optimize(visitor, contextInfo);
        if (e != this) {
            return e;
        }
        if ("EE".equals(this.getPackageData().getTargetEdition()) && (e = visitor.obtainOptimizer().optimizeNumberInstruction(this, contextInfo)) != null) {
            return e;
        }
        return this;
    }

    @Override
    public SequenceIterator iterate(XPathContext context) throws XPathException {
        ArrayList<Int64Value> vec = new ArrayList<Int64Value>(1);
        NodeInfo source = (NodeInfo)this.selectOp.getChildExpression().evaluateItem(context);
        switch (this.level) {
            case 3: {
                long value = Navigator.getNumberSimple(source, context);
                if (value == 0L) break;
                vec.add(Int64Value.makeIntegerValue(value));
                break;
            }
            case 0: {
                long value = Navigator.getNumberSingle(source, this.getCount(), this.getFrom(), context);
                if (value == 0L) break;
                vec.add(Int64Value.makeIntegerValue(value));
                break;
            }
            case 2: {
                long value = Navigator.getNumberAny(this, source, this.getCount(), this.getFrom(), context, this.hasVariablesInPatterns);
                if (value == 0L) break;
                vec.add(Int64Value.makeIntegerValue(value));
                break;
            }
            case 1: {
                for (long n : Navigator.getNumberMulti(source, this.getCount(), this.getFrom(), context)) {
                    vec.add(Int64Value.makeIntegerValue(n));
                }
                break;
            }
        }
        return new ListIterator(vec);
    }

    @Override
    public String getExpressionName() {
        return "xsl:number";
    }

    @Override
    public void export(ExpressionPresenter out) throws XPathException {
        out.startElement("nodeNum", this);
        out.emitAttribute("level", LEVEL_NAMES[this.level]);
        out.setChildRole("select");
        this.selectOp.getChildExpression().export(out);
        if (this.countOp != null) {
            out.setChildRole("count");
            this.getCount().export(out);
        }
        if (this.fromOp != null) {
            out.setChildRole("from");
            this.getFrom().export(out);
        }
        out.endElement();
    }
}

