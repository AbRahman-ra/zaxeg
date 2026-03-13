/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.instruct;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import net.sf.saxon.event.Outputter;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.ExpressionOwner;
import net.sf.saxon.expr.Operand;
import net.sf.saxon.expr.PackageData;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.XPathContextMajor;
import net.sf.saxon.expr.instruct.LocalParam;
import net.sf.saxon.expr.instruct.SlotManager;
import net.sf.saxon.expr.instruct.TailCall;
import net.sf.saxon.expr.instruct.TailCallReturner;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.expr.parser.RoleDiagnostic;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.pattern.Pattern;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.style.Compilation;
import net.sf.saxon.style.ComponentDeclaration;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trace.TraceableComponent;
import net.sf.saxon.trans.UncheckedXPathException;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.trans.rules.Rule;
import net.sf.saxon.trans.rules.RuleTarget;
import net.sf.saxon.type.AnyItemType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.TypeHierarchy;
import net.sf.saxon.value.SequenceType;

public class TemplateRule
implements RuleTarget,
Location,
ExpressionOwner,
TraceableComponent {
    protected Expression body;
    protected Pattern matchPattern;
    private boolean hasRequiredParams;
    private boolean bodyIsTailCallReturner;
    private SequenceType requiredType;
    private boolean declaredStreamable;
    private ItemType requiredContextItemType = AnyItemType.getInstance();
    private boolean absentFocus;
    private SlotManager stackFrameMap;
    private PackageData packageData;
    private String systemId;
    private int lineNumber;
    private List<Rule> rules = new ArrayList<Rule>();
    protected List<TemplateRule> slaveCopies = new ArrayList<TemplateRule>();

    public void setMatchPattern(Pattern pattern) {
        this.matchPattern = pattern;
    }

    @Override
    public Expression getBody() {
        return this.body;
    }

    @Override
    public Expression getChildExpression() {
        return this.body;
    }

    @Override
    public Location getLocation() {
        return this;
    }

    @Override
    public void gatherProperties(BiConsumer<String, Object> consumer) {
        consumer.accept("match", this.getMatchPattern().toShortString());
    }

    public void setContextItemRequirements(ItemType type, boolean absentFocus) {
        this.requiredContextItemType = type;
        this.absentFocus = absentFocus;
    }

    public int getComponentKind() {
        return 200;
    }

    public Pattern getMatchPattern() {
        return this.matchPattern;
    }

    @Override
    public void setBody(Expression body) {
        this.body = body;
        this.bodyIsTailCallReturner = body instanceof TailCallReturner;
    }

    public void setStackFrameMap(SlotManager map) {
        this.stackFrameMap = map;
    }

    public SlotManager getStackFrameMap() {
        return this.stackFrameMap;
    }

    public void setHasRequiredParams(boolean has) {
        this.hasRequiredParams = has;
    }

    public boolean hasRequiredParams() {
        return this.hasRequiredParams;
    }

    public void setRequiredType(SequenceType type) {
        this.requiredType = type;
    }

    public SequenceType getRequiredType() {
        if (this.requiredType == null) {
            return SequenceType.ANY_SEQUENCE;
        }
        return this.requiredType;
    }

    @Override
    public void registerRule(Rule rule) {
        this.rules.add(rule);
    }

    public List<Rule> getRules() {
        return this.rules;
    }

    public int getContainerGranularity() {
        return 0;
    }

    public PackageData getPackageData() {
        return this.packageData;
    }

    public void setPackageData(PackageData data) {
        this.packageData = data;
    }

    @Override
    public String getPublicId() {
        return null;
    }

    @Override
    public String getSystemId() {
        return this.systemId;
    }

    public void setSystemId(String id) {
        this.systemId = id;
    }

    @Override
    public int getLineNumber() {
        return this.lineNumber;
    }

    public void setLineNumber(int line) {
        this.lineNumber = line;
    }

    @Override
    public int getColumnNumber() {
        return -1;
    }

    @Override
    public Location saveLocation() {
        return this;
    }

    public ItemType getRequiredContextItemType() {
        return this.requiredContextItemType;
    }

    public boolean isAbsentFocus() {
        return this.absentFocus;
    }

    public List<LocalParam> getLocalParams() {
        ArrayList<LocalParam> result = new ArrayList<LocalParam>();
        TemplateRule.gatherLocalParams(this.getInterpretedBody(), result);
        return result;
    }

    private static void gatherLocalParams(Expression exp, List<LocalParam> result) {
        if (exp instanceof LocalParam) {
            result.add((LocalParam)exp);
        } else {
            for (Operand o : exp.operands()) {
                TemplateRule.gatherLocalParams(o.getChildExpression(), result);
            }
        }
    }

    public void prepareInitializer(Compilation compilation, ComponentDeclaration decl, StructuredQName modeName) {
    }

    public void initialize() throws XPathException {
    }

    public void apply(Outputter output, XPathContextMajor context) throws XPathException {
        for (TailCall tc = this.applyLeavingTail(output, context); tc != null; tc = tc.processLeavingTail()) {
        }
    }

    public TailCall applyLeavingTail(Outputter output, XPathContext context) throws XPathException {
        TypeHierarchy th = context.getConfiguration().getTypeHierarchy();
        if (this.requiredContextItemType != AnyItemType.getInstance() && !this.requiredContextItemType.matches(context.getContextItem(), th)) {
            RoleDiagnostic role = new RoleDiagnostic(20, "context item for the template rule", 0);
            String message = role.composeErrorMessage(this.requiredContextItemType, context.getContextItem(), th);
            XPathException err = new XPathException(message, "XTTE0590");
            err.setLocation(this);
            err.setIsTypeError(true);
            throw err;
        }
        if (this.absentFocus) {
            context = context.newMinorContext();
            context.setCurrentIterator(null);
        }
        try {
            if (this.bodyIsTailCallReturner) {
                return ((TailCallReturner)((Object)this.body)).processLeavingTail(output, context);
            }
            this.body.process(output, context);
            return null;
        } catch (UncheckedXPathException e) {
            XPathException xe = e.getXPathException();
            xe.maybeSetLocation(this);
            xe.maybeSetContext(context);
            throw xe;
        } catch (XPathException e) {
            e.maybeSetLocation(this);
            e.maybeSetContext(context);
            throw e;
        } catch (Exception e2) {
            String message = "Internal error evaluating template rule " + (this.getLineNumber() > 0 ? " at line " + this.getLineNumber() : "") + (this.getSystemId() != null ? " in module " + this.getSystemId() : "");
            e2.printStackTrace();
            throw new RuntimeException(message, e2);
        }
    }

    @Override
    public void export(ExpressionPresenter presenter) throws XPathException {
        throw new UnsupportedOperationException();
    }

    public void setDeclaredStreamable(boolean streamable) {
    }

    public boolean isDeclaredStreamable() {
        return false;
    }

    public void explainProperties(ExpressionPresenter presenter) throws XPathException {
        if (this.getRequiredContextItemType() != AnyItemType.getInstance()) {
            SequenceType st = SequenceType.makeSequenceType(this.getRequiredContextItemType(), 16384);
            presenter.emitAttribute("cxt", st.toAlphaCode());
        }
        String flags = "";
        if (!this.absentFocus) {
            flags = flags + "s";
        }
        presenter.emitAttribute("flags", flags);
        if (this.getRequiredType() != SequenceType.ANY_SEQUENCE) {
            presenter.emitAttribute("as", this.getRequiredType().toAlphaCode());
        }
        presenter.emitAttribute("line", this.getLineNumber() + "");
        presenter.emitAttribute("module", this.getSystemId());
        if (this.isDeclaredStreamable()) {
            presenter.emitAttribute("streamable", "1");
        }
    }

    public Expression getInterpretedBody() {
        return this.body.getInterpretedExpression();
    }

    public TemplateRule copy() {
        TemplateRule tr = new TemplateRule();
        if (this.body == null || this.matchPattern == null) {
            this.slaveCopies.add(tr);
        } else {
            this.copyTo(tr);
        }
        return tr;
    }

    public void updateSlaveCopies() {
        for (TemplateRule tr : this.slaveCopies) {
            this.copyTo(tr);
        }
    }

    protected void copyTo(TemplateRule tr) {
        if (this.body != null) {
            tr.body = this.body.copy(new RebindingMap());
        }
        if (this.matchPattern != null) {
            tr.matchPattern = this.matchPattern.copy(new RebindingMap());
        }
        tr.hasRequiredParams = this.hasRequiredParams;
        tr.bodyIsTailCallReturner = this.bodyIsTailCallReturner;
        tr.requiredType = this.requiredType;
        tr.declaredStreamable = this.declaredStreamable;
        tr.requiredContextItemType = this.requiredContextItemType;
        tr.absentFocus = this.absentFocus;
        tr.stackFrameMap = this.stackFrameMap;
        tr.packageData = this.packageData;
        tr.systemId = this.systemId;
        tr.lineNumber = this.lineNumber;
    }

    @Override
    public void setChildExpression(Expression expr) {
        this.setBody(expr);
    }

    @Override
    public StructuredQName getObjectName() {
        return null;
    }

    @Override
    public String getTracingTag() {
        return "xsl:template";
    }
}

