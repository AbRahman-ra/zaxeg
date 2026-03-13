/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.instruct;

import java.util.ArrayList;
import java.util.List;
import net.sf.saxon.Configuration;
import net.sf.saxon.event.Outputter;
import net.sf.saxon.event.PipelineConfiguration;
import net.sf.saxon.expr.AnalyzeMappingFunction;
import net.sf.saxon.expr.ContextMappingIterator;
import net.sf.saxon.expr.ContextOriginator;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.Operand;
import net.sf.saxon.expr.OperandRole;
import net.sf.saxon.expr.OperandUsage;
import net.sf.saxon.expr.StringLiteral;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.XPathContextMajor;
import net.sf.saxon.expr.instruct.Instruction;
import net.sf.saxon.expr.instruct.TailCall;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.expr.parser.RoleDiagnostic;
import net.sf.saxon.expr.parser.TypeChecker;
import net.sf.saxon.om.FocusIterator;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.regex.RegexIterator;
import net.sf.saxon.regex.RegularExpression;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.ErrorType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.type.Type;
import net.sf.saxon.type.TypeHierarchy;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;

public class AnalyzeString
extends Instruction
implements ContextOriginator {
    private Operand selectOp;
    private Operand regexOp;
    private Operand flagsOp;
    private Operand matchingOp;
    private Operand nonMatchingOp;
    private static final OperandRole ACTION = new OperandRole(6, OperandUsage.NAVIGATION);
    private static final OperandRole SELECT = new OperandRole(1, OperandUsage.ABSORPTION, SequenceType.SINGLE_STRING);
    private RegularExpression pattern;

    public AnalyzeString(Expression select, Expression regex, Expression flags, Expression matching, Expression nonMatching, RegularExpression pattern) {
        this.selectOp = new Operand(this, select, SELECT);
        this.regexOp = new Operand(this, regex, OperandRole.SINGLE_ATOMIC);
        this.flagsOp = new Operand(this, flags, OperandRole.SINGLE_ATOMIC);
        if (matching != null) {
            this.matchingOp = new Operand(this, matching, ACTION);
        }
        if (nonMatching != null) {
            this.nonMatchingOp = new Operand(this, nonMatching, ACTION);
        }
        this.pattern = pattern;
    }

    public Expression getSelect() {
        return this.selectOp.getChildExpression();
    }

    public void setSelect(Expression select) {
        this.selectOp.setChildExpression(select);
    }

    public Expression getRegex() {
        return this.regexOp.getChildExpression();
    }

    public void setRegex(Expression regex) {
        this.regexOp.setChildExpression(regex);
    }

    public Expression getFlags() {
        return this.flagsOp.getChildExpression();
    }

    public void setFlags(Expression flags) {
        this.flagsOp.setChildExpression(flags);
    }

    public Expression getMatching() {
        return this.matchingOp == null ? null : this.matchingOp.getChildExpression();
    }

    public void setMatching(Expression matching) {
        if (this.matchingOp != null) {
            this.matchingOp.setChildExpression(matching);
        } else {
            this.matchingOp = new Operand(this, matching, ACTION);
        }
    }

    public Expression getNonMatching() {
        return this.nonMatchingOp == null ? null : this.nonMatchingOp.getChildExpression();
    }

    public void setNonMatching(Expression nonMatching) {
        if (this.nonMatchingOp != null) {
            this.nonMatchingOp.setChildExpression(nonMatching);
        } else {
            this.nonMatchingOp = new Operand(this, nonMatching, ACTION);
        }
    }

    @Override
    public int getInstructionNameCode() {
        return 131;
    }

    @Override
    public Iterable<Operand> operands() {
        return this.operandSparseList(this.selectOp, this.regexOp, this.flagsOp, this.matchingOp, this.nonMatchingOp);
    }

    @Override
    public int getImplementationMethod() {
        return 6;
    }

    public RegularExpression getPatternExpression() {
        return this.pattern;
    }

    @Override
    public boolean allowExtractingCommonSubexpressions() {
        return false;
    }

    @Override
    public Expression typeCheck(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        Configuration config = visitor.getConfiguration();
        this.selectOp.typeCheck(visitor, contextInfo);
        this.regexOp.typeCheck(visitor, contextInfo);
        this.flagsOp.typeCheck(visitor, contextInfo);
        if (this.matchingOp != null) {
            this.matchingOp.typeCheck(visitor, config.makeContextItemStaticInfo(BuiltInAtomicType.STRING, false));
        }
        if (this.nonMatchingOp != null) {
            this.nonMatchingOp.typeCheck(visitor, config.makeContextItemStaticInfo(BuiltInAtomicType.STRING, false));
        }
        TypeChecker tc = visitor.getConfiguration().getTypeChecker(false);
        RoleDiagnostic role = new RoleDiagnostic(4, "analyze-string/select", 0);
        SequenceType required = SequenceType.OPTIONAL_STRING;
        this.setSelect(tc.staticTypeCheck(this.getSelect(), required, role, visitor));
        role = new RoleDiagnostic(4, "analyze-string/regex", 0);
        this.setRegex(tc.staticTypeCheck(this.getRegex(), SequenceType.SINGLE_STRING, role, visitor));
        role = new RoleDiagnostic(4, "analyze-string/flags", 0);
        this.setFlags(tc.staticTypeCheck(this.getFlags(), SequenceType.SINGLE_STRING, role, visitor));
        return this;
    }

    @Override
    public Expression optimize(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        Configuration config = visitor.getConfiguration();
        this.selectOp.optimize(visitor, contextInfo);
        this.regexOp.optimize(visitor, contextInfo);
        this.flagsOp.optimize(visitor, contextInfo);
        if (this.matchingOp != null) {
            this.matchingOp.optimize(visitor, config.makeContextItemStaticInfo(BuiltInAtomicType.STRING, false));
        }
        if (this.nonMatchingOp != null) {
            this.nonMatchingOp.optimize(visitor, config.makeContextItemStaticInfo(BuiltInAtomicType.STRING, false));
        }
        ArrayList<String> warnings = new ArrayList<String>();
        this.precomputeRegex(config, warnings);
        for (String w : warnings) {
            visitor.getStaticContext().issueWarning(w, this.getLocation());
        }
        return this;
    }

    public void precomputeRegex(Configuration config, List<String> warnings) throws XPathException {
        if (this.pattern == null && this.getRegex() instanceof StringLiteral && this.getFlags() instanceof StringLiteral) {
            try {
                String regex = ((StringLiteral)this.getRegex()).getStringValue();
                String flagstr = ((StringLiteral)this.getFlags()).getStringValue();
                String hostLang = "XP30";
                this.pattern = config.compileRegularExpression(regex, flagstr.toString(), hostLang, warnings);
            } catch (XPathException err) {
                if ("XTDE1150".equals(err.getErrorCodeLocalPart())) {
                    throw err;
                }
                if ("FORX0001".equals(err.getErrorCodeLocalPart())) {
                    this.invalidRegex("Error in regular expression flags: " + err, "FORX0001");
                }
                this.invalidRegex("Error in regular expression: " + err, err.getErrorCodeLocalPart());
            }
        }
    }

    private void invalidRegex(String message, String errorCode) throws XPathException {
        this.pattern = null;
        XPathException err = new XPathException(message, errorCode);
        err.setLocation(this.getLocation());
        throw err;
    }

    @Override
    public Expression copy(RebindingMap rm) {
        AnalyzeString a2 = new AnalyzeString(this.copy(this.getSelect(), rm), this.copy(this.getRegex(), rm), this.copy(this.getFlags(), rm), this.copy(this.getMatching(), rm), this.copy(this.getNonMatching(), rm), this.pattern);
        ExpressionTool.copyLocationInfo(this, a2);
        return a2;
    }

    private Expression copy(Expression exp, RebindingMap rebindings) {
        return exp == null ? null : exp.copy(rebindings);
    }

    @Override
    public void checkPermittedContents(SchemaType parentType, boolean whole) throws XPathException {
        if (this.getMatching() != null) {
            this.getMatching().checkPermittedContents(parentType, false);
        }
        if (this.getNonMatching() != null) {
            this.getNonMatching().checkPermittedContents(parentType, false);
        }
    }

    @Override
    public ItemType getItemType() {
        if (this.getMatching() != null) {
            if (this.getNonMatching() != null) {
                TypeHierarchy th = this.getConfiguration().getTypeHierarchy();
                return Type.getCommonSuperType(this.getMatching().getItemType(), this.getNonMatching().getItemType(), th);
            }
            return this.getMatching().getItemType();
        }
        if (this.getNonMatching() != null) {
            return this.getNonMatching().getItemType();
        }
        return ErrorType.getInstance();
    }

    @Override
    public int computeDependencies() {
        int dependencies = 0;
        dependencies |= this.getSelect().getDependencies();
        dependencies |= this.getRegex().getDependencies();
        dependencies |= this.getFlags().getDependencies();
        if (this.getMatching() != null) {
            dependencies |= this.getMatching().getDependencies() & 0xFFFFFFA1;
        }
        if (this.getNonMatching() != null) {
            dependencies |= this.getNonMatching().getDependencies() & 0xFFFFFFA1;
        }
        return dependencies;
    }

    @Override
    public TailCall processLeavingTail(Outputter output, XPathContext context) throws XPathException {
        Item it;
        RegexIterator iter = this.getRegexIterator(context);
        XPathContextMajor c2 = context.newContext();
        c2.setOrigin(this);
        FocusIterator focusIter = c2.trackFocus(iter);
        c2.setCurrentRegexIterator(iter);
        PipelineConfiguration pipe = output.getPipelineConfiguration();
        pipe.setXPathContext(c2);
        while ((it = focusIter.next()) != null) {
            if (iter.isMatching()) {
                if (this.getMatching() == null) continue;
                this.getMatching().process(output, c2);
                continue;
            }
            if (this.getNonMatching() == null) continue;
            this.getNonMatching().process(output, c2);
        }
        pipe.setXPathContext(context);
        return null;
    }

    private RegexIterator getRegexIterator(XPathContext context) throws XPathException {
        CharSequence input = this.getSelect().evaluateAsString(context);
        RegularExpression re = this.pattern;
        if (re == null) {
            String flagstr = this.getFlags().evaluateAsString(context).toString();
            StringValue regexString = (StringValue)this.getRegex().evaluateItem(context);
            re = context.getConfiguration().compileRegularExpression(this.getRegex().evaluateAsString(context), flagstr, "XP30", null);
        }
        return re.analyze(input);
    }

    @Override
    public SequenceIterator iterate(XPathContext context) throws XPathException {
        RegexIterator iter = this.getRegexIterator(context);
        XPathContextMajor c2 = context.newContext();
        c2.setOrigin(this);
        c2.trackFocus(iter);
        c2.setCurrentRegexIterator(iter);
        AnalyzeMappingFunction fn = new AnalyzeMappingFunction(iter, c2, this.getNonMatching(), this.getMatching());
        return new ContextMappingIterator(fn, c2);
    }

    @Override
    public String getExpressionName() {
        return "analyzeString";
    }

    @Override
    public void export(ExpressionPresenter out) throws XPathException {
        out.startElement("analyzeString", this);
        out.setChildRole("select");
        this.getSelect().export(out);
        out.setChildRole("regex");
        this.getRegex().export(out);
        out.setChildRole("flags");
        this.getFlags().export(out);
        if (this.getMatching() != null) {
            out.setChildRole("matching");
            this.getMatching().export(out);
        }
        if (this.getNonMatching() != null) {
            out.setChildRole("nonMatching");
            this.getNonMatching().export(out);
        }
        out.endElement();
    }
}

