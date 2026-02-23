/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import net.sf.saxon.Configuration;
import net.sf.saxon.expr.Atomizer;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.Operand;
import net.sf.saxon.expr.OperandRole;
import net.sf.saxon.expr.StringLiteral;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.number.NumberFormatter;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.functions.Number_1;
import net.sf.saxon.lib.ConversionRules;
import net.sf.saxon.lib.Numberer;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.Converter;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.StringConverter;
import net.sf.saxon.type.ValidationFailure;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.BigIntegerValue;
import net.sf.saxon.value.Int64Value;
import net.sf.saxon.value.NumericValue;
import net.sf.saxon.value.StringValue;

public class NumberSequenceFormatter
extends Expression {
    private Operand valueOp;
    private Operand formatOp;
    private Operand groupSizeOp;
    private Operand groupSeparatorOp;
    private Operand letterValueOp;
    private Operand ordinalOp;
    private Operand startAtOp;
    private Operand langOp;
    private NumberFormatter formatter = null;
    private Numberer numberer = null;
    private boolean backwardsCompatible;

    public NumberSequenceFormatter(Expression value, Expression format, Expression groupSize, Expression groupSeparator, Expression letterValue, Expression ordinal, Expression startAt, Expression lang, NumberFormatter formatter, boolean backwardsCompatible) {
        if (value != null) {
            this.valueOp = new Operand(this, value, OperandRole.SINGLE_ATOMIC);
        }
        if (format != null) {
            this.formatOp = new Operand(this, format, OperandRole.SINGLE_ATOMIC);
        }
        if (groupSize != null) {
            this.groupSizeOp = new Operand(this, groupSize, OperandRole.SINGLE_ATOMIC);
        }
        if (groupSeparator != null) {
            this.groupSeparatorOp = new Operand(this, groupSeparator, OperandRole.SINGLE_ATOMIC);
        }
        if (letterValue != null) {
            this.letterValueOp = new Operand(this, letterValue, OperandRole.SINGLE_ATOMIC);
        }
        if (ordinal != null) {
            this.ordinalOp = new Operand(this, ordinal, OperandRole.SINGLE_ATOMIC);
        }
        this.startAtOp = new Operand(this, startAt, OperandRole.SINGLE_ATOMIC);
        if (lang != null) {
            this.langOp = new Operand(this, lang, OperandRole.SINGLE_ATOMIC);
        }
        this.formatter = formatter;
        this.backwardsCompatible = backwardsCompatible;
        if (formatter == null && format instanceof StringLiteral) {
            this.formatter = new NumberFormatter();
            this.formatter.prepare(((StringLiteral)format).getStringValue());
        }
    }

    @Override
    public Expression simplify() throws XPathException {
        if (this.valueOp != null && !this.valueOp.getChildExpression().getItemType().isPlainType()) {
            this.valueOp.setChildExpression(Atomizer.makeAtomizer(this.valueOp.getChildExpression(), null));
        }
        this.preallocateNumberer(this.getConfiguration());
        return super.simplify();
    }

    public void preallocateNumberer(Configuration config) throws XPathException {
        if (this.langOp == null) {
            this.numberer = config.makeNumberer(null, null);
        } else if (this.langOp.getChildExpression() instanceof StringLiteral) {
            ValidationFailure vf;
            String language = ((StringLiteral)this.langOp.getChildExpression()).getStringValue();
            if (!language.isEmpty() && (vf = StringConverter.StringToLanguage.INSTANCE.validate(language)) != null) {
                this.langOp.setChildExpression(new StringLiteral(StringValue.EMPTY_STRING));
                throw new XPathException("The lang attribute must be a valid language code", "XTDE0030");
            }
            this.numberer = config.makeNumberer(language, null);
        }
    }

    @Override
    public Iterable<Operand> operands() {
        return this.operandSparseList(this.valueOp, this.formatOp, this.groupSizeOp, this.groupSeparatorOp, this.letterValueOp, this.ordinalOp, this.startAtOp, this.langOp);
    }

    private boolean isFixed(Operand op) {
        return op == null || op.getChildExpression() instanceof Literal;
    }

    private boolean hasFixedOperands() {
        for (Operand o : this.operands()) {
            if (this.isFixed(o)) continue;
            return false;
        }
        return true;
    }

    @Override
    public Expression optimize(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        this.optimizeChildren(visitor, contextInfo);
        if (this.hasFixedOperands()) {
            StringValue val = this.evaluateItem(visitor.makeDynamicContext());
            StringLiteral literal = new StringLiteral(val);
            ExpressionTool.copyLocationInfo(this, literal);
            return literal;
        }
        return this;
    }

    @Override
    public Expression copy(RebindingMap rebindings) {
        NumberSequenceFormatter exp = new NumberSequenceFormatter(this.copy(this.valueOp, rebindings), this.copy(this.formatOp, rebindings), this.copy(this.groupSizeOp, rebindings), this.copy(this.groupSeparatorOp, rebindings), this.copy(this.letterValueOp, rebindings), this.copy(this.ordinalOp, rebindings), this.copy(this.startAtOp, rebindings), this.copy(this.langOp, rebindings), this.formatter, this.backwardsCompatible);
        ExpressionTool.copyLocationInfo(this, exp);
        return exp;
    }

    private Expression copy(Operand op, RebindingMap rebindings) {
        return op == null ? null : op.getChildExpression().copy(rebindings);
    }

    @Override
    public ItemType getItemType() {
        return BuiltInAtomicType.STRING;
    }

    @Override
    public int computeCardinality() {
        return 16384;
    }

    @Override
    public int getImplementationMethod() {
        return 1;
    }

    @Override
    public StringValue evaluateItem(XPathContext context) throws XPathException {
        NumberFormatter nf;
        String letterVal;
        Numberer numb;
        AtomicValue val;
        long value = -1L;
        ArrayList<Object> vec = new ArrayList<Object>(4);
        ConversionRules rules = context.getConfiguration().getConversionRules();
        String startAv = this.startAtOp.getChildExpression().evaluateAsString(context).toString();
        List<Integer> startValues = this.parseStartAtValue(startAv);
        SequenceIterator iter = this.valueOp.getChildExpression().iterate(context);
        int pos = 0;
        while ((val = (AtomicValue)iter.next()) != null && (!this.backwardsCompatible || vec.isEmpty())) {
            int startValue = startValues.size() > pos ? startValues.get(pos).intValue() : startValues.get(startValues.size() - 1).intValue();
            ++pos;
            try {
                NumericValue num = val instanceof NumericValue ? (NumericValue)val : Number_1.convert(val, context.getConfiguration());
                if (num.isNaN()) {
                    throw new XPathException("NaN");
                }
                if ((num = num.round(0)).compareTo(Int64Value.MAX_LONG) > 0) {
                    BigInteger bi = ((BigIntegerValue)Converter.convert(num, BuiltInAtomicType.INTEGER, rules).asAtomic()).asBigInteger();
                    if (startValue != 1) {
                        bi = bi.add(BigInteger.valueOf(startValue - 1));
                    }
                    vec.add(bi);
                    continue;
                }
                if (num.compareTo(Int64Value.ZERO) < 0) {
                    throw new XPathException("The numbers to be formatted must not be negative");
                }
                long i = ((NumericValue)Converter.convert(num, BuiltInAtomicType.INTEGER, rules).asAtomic()).longValue();
                vec.add(i += (long)(startValue - 1));
            } catch (XPathException err) {
                if (this.backwardsCompatible) {
                    vec.add("NaN");
                    continue;
                }
                vec.add(val.getStringValue());
                XPathException e = new XPathException("Cannot convert supplied value to an integer. " + err.getMessage());
                e.setErrorCode("XTDE0980");
                e.setLocation(this.getLocation());
                e.setXPathContext(context);
                throw e;
            }
        }
        if (this.backwardsCompatible && vec.isEmpty()) {
            vec.add("NaN");
        }
        int gpsize = 0;
        String gpseparator = "";
        String ordinalVal = null;
        if (this.groupSizeOp != null) {
            String g = this.groupSizeOp.getChildExpression().evaluateAsString(context).toString();
            try {
                gpsize = Integer.parseInt(g);
            } catch (NumberFormatException err) {
                XPathException e = new XPathException("grouping-size must be numeric");
                e.setXPathContext(context);
                e.setErrorCode("XTDE0030");
                e.setLocation(this.getLocation());
                throw e;
            }
        }
        if (this.groupSeparatorOp != null) {
            gpseparator = this.groupSeparatorOp.getChildExpression().evaluateAsString(context).toString();
        }
        if (this.ordinalOp != null) {
            ordinalVal = this.ordinalOp.getChildExpression().evaluateAsString(context).toString();
        }
        if ((numb = this.numberer) == null) {
            if (this.langOp == null) {
                numb = context.getConfiguration().makeNumberer(null, null);
            } else {
                String language = this.langOp.getChildExpression().evaluateAsString(context).toString();
                ValidationFailure vf = StringConverter.StringToLanguage.INSTANCE.validate(language);
                if (vf != null) {
                    throw new XPathException("The lang attribute of xsl:number must be a valid language code", "XTDE0030");
                }
                numb = context.getConfiguration().makeNumberer(language, null);
            }
        }
        if (this.letterValueOp == null) {
            letterVal = "";
        } else {
            letterVal = this.letterValueOp.getChildExpression().evaluateAsString(context).toString();
            if (!"alphabetic".equals(letterVal) && !"traditional".equals(letterVal)) {
                XPathException e = new XPathException("letter-value must be \"traditional\" or \"alphabetic\"");
                e.setXPathContext(context);
                e.setErrorCode("XTDE0030");
                e.setLocation(this.getLocation());
                throw e;
            }
        }
        if (this.formatter == null) {
            nf = new NumberFormatter();
            nf.prepare(this.formatOp.getChildExpression().evaluateAsString(context).toString());
        } else {
            nf = this.formatter;
        }
        CharSequence s = nf.format(vec, gpsize, gpseparator, letterVal, ordinalVal, numb);
        return new StringValue(s);
    }

    public List<Integer> parseStartAtValue(String value) throws XPathException {
        String[] tokens;
        ArrayList<Integer> list = new ArrayList<Integer>();
        for (String tok : tokens = value.split("\\s+")) {
            try {
                int n = Integer.parseInt(tok);
                list.add(n);
            } catch (NumberFormatException err) {
                XPathException e = new XPathException("Invalid start-at value: non-integer component {" + tok + "}");
                e.setErrorCode("XTDE0030");
                e.setLocation(this.getLocation());
                throw e;
            }
        }
        if (list.isEmpty()) {
            XPathException e = new XPathException("Invalid start-at value: no numeric components found");
            e.setErrorCode("XTDE0030");
            e.setLocation(this.getLocation());
            throw e;
        }
        return list;
    }

    @Override
    public void export(ExpressionPresenter out) throws XPathException {
        out.startElement("numSeqFmt", this);
        String flags = "";
        if (this.backwardsCompatible) {
            flags = flags + "1";
        }
        if (!flags.isEmpty()) {
            out.emitAttribute("flags", flags);
        }
        if (this.valueOp != null) {
            out.setChildRole("value");
            this.valueOp.getChildExpression().export(out);
        }
        if (this.formatOp != null) {
            out.setChildRole("format");
            this.formatOp.getChildExpression().export(out);
        }
        if (this.startAtOp != null) {
            out.setChildRole("startAt");
            this.startAtOp.getChildExpression().export(out);
        }
        if (this.langOp != null) {
            out.setChildRole("lang");
            this.langOp.getChildExpression().export(out);
        }
        if (this.ordinalOp != null) {
            out.setChildRole("ordinal");
            this.ordinalOp.getChildExpression().export(out);
        }
        if (this.groupSeparatorOp != null) {
            out.setChildRole("gpSep");
            this.groupSeparatorOp.getChildExpression().export(out);
        }
        if (this.groupSizeOp != null) {
            out.setChildRole("gpSize");
            this.groupSizeOp.getChildExpression().export(out);
        }
        out.endElement();
    }
}

