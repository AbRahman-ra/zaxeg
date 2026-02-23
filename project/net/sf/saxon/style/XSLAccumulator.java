/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.style;

import net.sf.saxon.Configuration;
import net.sf.saxon.expr.Component;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.StringLiteral;
import net.sf.saxon.expr.accum.Accumulator;
import net.sf.saxon.expr.accum.AccumulatorRegistry;
import net.sf.saxon.expr.accum.AccumulatorRule;
import net.sf.saxon.expr.instruct.Actor;
import net.sf.saxon.expr.instruct.SlotManager;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.Optimizer;
import net.sf.saxon.expr.parser.RoleDiagnostic;
import net.sf.saxon.lib.Feature;
import net.sf.saxon.om.AttributeInfo;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.pattern.NodeTest;
import net.sf.saxon.pattern.Pattern;
import net.sf.saxon.style.Compilation;
import net.sf.saxon.style.ComponentDeclaration;
import net.sf.saxon.style.PrincipalStylesheetModule;
import net.sf.saxon.style.StyleElement;
import net.sf.saxon.style.StyleNodeFactory;
import net.sf.saxon.style.StylesheetComponent;
import net.sf.saxon.style.XSLAccumulatorRule;
import net.sf.saxon.trans.SimpleMode;
import net.sf.saxon.trans.SymbolicName;
import net.sf.saxon.trans.Visibility;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.AtomicType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.UType;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;
import net.sf.saxon.value.Whitespace;

public class XSLAccumulator
extends StyleElement
implements StylesheetComponent {
    private Accumulator accumulator = new Accumulator();
    private SlotManager slotManager;

    @Override
    public Actor getActor() {
        if (this.accumulator.getDeclaringComponent() == null) {
            this.accumulator.makeDeclaringComponent(Visibility.PRIVATE, this.getContainingPackage());
        }
        return this.accumulator;
    }

    @Override
    public SymbolicName getSymbolicName() {
        StructuredQName qname = this.accumulator.getAccumulatorName();
        return qname == null ? null : new SymbolicName(129, null);
    }

    @Override
    public void checkCompatibility(Component component) {
    }

    @Override
    public boolean isDeclaration() {
        return true;
    }

    private void prepareSimpleAttributes() {
        for (AttributeInfo att : this.attributes()) {
            NodeName attName = att.getNodeName();
            String value = att.getValue();
            String f = attName.getDisplayName();
            if (f.equals("name")) {
                String name = Whitespace.trim(value);
                this.accumulator.setAccumulatorName(this.makeQName(name, null, "name"));
                continue;
            }
            if (f.equals("streamable")) {
                this.accumulator.setDeclaredStreamable(false);
                boolean streamable = this.processStreamableAtt(value);
                this.accumulator.setDeclaredStreamable(streamable);
                continue;
            }
            if (!attName.hasURI("http://saxon.sf.net/") || !attName.getLocalPart().equals("trace") || !this.isExtensionAttributeAllowed(attName.getDisplayName())) continue;
            this.accumulator.setTracing(this.processBooleanAttribute("saxon:trace", value));
        }
        if (this.accumulator.getAccumulatorName() == null) {
            this.reportAbsence("name");
        }
    }

    @Override
    public void prepareAttributes() {
        for (AttributeInfo att : this.attributes()) {
            NodeName attName = att.getNodeName();
            String f = attName.getDisplayName();
            String value = att.getValue();
            if (f.equals("name") || f.equals("streamable")) continue;
            if (f.equals("initial-value")) {
                this.accumulator.setInitialValueExpression(this.makeExpression(value, att));
                continue;
            }
            if (f.equals("as")) {
                try {
                    SequenceType requiredType = this.makeSequenceType(value);
                    this.accumulator.setType(requiredType);
                } catch (XPathException e) {
                    this.compileErrorInAttribute(e.getMessage(), e.getErrorCodeLocalPart(), "as");
                }
                continue;
            }
            if (attName.hasURI("http://saxon.sf.net/") && attName.getLocalPart().equals("trace")) {
                if (!this.isExtensionAttributeAllowed(attName.getDisplayName())) continue;
                this.accumulator.setTracing(this.processBooleanAttribute("saxon:trace", value));
                continue;
            }
            this.checkUnknownAttribute(attName);
        }
        if (this.accumulator.getType() == null) {
            this.accumulator.setType(SequenceType.ANY_SEQUENCE);
        }
        if (this.accumulator.getInitialValueExpression() == null) {
            this.reportAbsence("initial-value");
            StringLiteral zls = new StringLiteral(StringValue.EMPTY_STRING);
            zls.setRetainedStaticContext(this.makeRetainedStaticContext());
            this.accumulator.setInitialValueExpression(zls);
        }
    }

    @Override
    public void compileDeclaration(Compilation compilation, ComponentDeclaration decl) throws XPathException {
        Configuration config = compilation.getConfiguration();
        this.accumulator.setPackageData(compilation.getPackageData());
        this.accumulator.obtainDeclaringComponent(decl.getSourceElement());
        Expression init = this.accumulator.getInitialValueExpression();
        ExpressionVisitor visitor = ExpressionVisitor.make(this.getStaticContext());
        init = init.typeCheck(visitor, config.getDefaultContextItemStaticInfo());
        RoleDiagnostic roleDiagnostic = new RoleDiagnostic(4, "xsl:accumulator-rule/select", 0);
        init = config.getTypeChecker(false).staticTypeCheck(init, this.accumulator.getType(), roleDiagnostic, visitor);
        init = init.optimize(visitor, config.getDefaultContextItemStaticInfo());
        SlotManager stackFrameMap = this.slotManager;
        ExpressionTool.allocateSlots(init, 0, stackFrameMap);
        this.accumulator.setSlotManagerForInitialValueExpression(stackFrameMap);
        this.checkInitialStreamability(init);
        this.accumulator.setInitialValueExpression(init);
        this.accumulator.addChildExpression(init);
        int position = 0;
        for (NodeInfo nodeInfo : this.children(XSLAccumulatorRule.class::isInstance)) {
            ItemType itemType;
            XSLAccumulatorRule rule = (XSLAccumulatorRule)nodeInfo;
            Pattern pattern = rule.getMatch();
            Expression newValueExp = rule.getNewValueExpression(compilation, decl);
            ExpressionVisitor visitor2 = ExpressionVisitor.make(this.getStaticContext());
            newValueExp = newValueExp.typeCheck(visitor2, config.makeContextItemStaticInfo(pattern.getItemType(), false));
            RoleDiagnostic role2 = new RoleDiagnostic(4, "xsl:accumulator-rule/select", 0);
            newValueExp = config.getTypeChecker(false).staticTypeCheck(newValueExp, this.accumulator.getType(), role2, visitor2);
            newValueExp = newValueExp.optimize(visitor2, this.getConfiguration().makeContextItemStaticInfo(pattern.getItemType(), false));
            SlotManager stackFrameMap2 = this.getConfiguration().makeSlotManager();
            stackFrameMap2.allocateSlotNumber(new StructuredQName("", "", "value"));
            ExpressionTool.allocateSlots(newValueExp, 1, stackFrameMap2);
            boolean isPreDescent = !rule.isPostDescent();
            SimpleMode mode = isPreDescent ? this.accumulator.getPreDescentRules() : this.accumulator.getPostDescentRules();
            AccumulatorRule action = new AccumulatorRule(newValueExp, stackFrameMap2, rule.isPostDescent());
            mode.addRule(pattern, action, decl.getModule(), decl.getModule().getPrecedence(), 1.0, position++, 0);
            this.checkRuleStreamability(rule, pattern, newValueExp);
            if (this.accumulator.isDeclaredStreamable() && rule.isPostDescent() && rule.isCapture()) {
                action.setCapturing(true);
            }
            if ((itemType = pattern.getItemType()) instanceof NodeTest) {
                if (!itemType.getUType().overlaps(UType.DOCUMENT.union(UType.CHILD_NODE_KINDS))) {
                    rule.compileWarning("An accumulator rule that matches attribute or namespace nodes has no effect", "SXWN9999");
                }
            } else if (itemType instanceof AtomicType) {
                rule.compileWarning("An accumulator rule that matches atomic values has no effect", "SXWN9999");
            }
            this.accumulator.addChildExpression(newValueExp);
            this.accumulator.addChildExpression(pattern);
        }
        this.accumulator.getPreDescentRules().allocateAllPatternSlots();
        this.accumulator.getPostDescentRules().allocateAllPatternSlots();
    }

    @Override
    public StructuredQName getObjectName() {
        StructuredQName qn = super.getObjectName();
        if (qn == null) {
            String nameAtt = Whitespace.trim(this.getAttributeValue("", "name"));
            if (nameAtt == null) {
                return new StructuredQName("saxon", "http://saxon.sf.net/", "badly-named-accumulator" + this.generateId());
            }
            qn = this.makeQName(nameAtt, null, "name");
            this.setObjectName(qn);
        }
        return qn;
    }

    @Override
    public void index(ComponentDeclaration decl, PrincipalStylesheetModule top) {
        AccumulatorRegistry mgr;
        Accumulator existing;
        if (this.accumulator.getAccumulatorName() == null) {
            this.prepareSimpleAttributes();
        }
        this.accumulator.setImportPrecedence(decl.getPrecedence());
        if (top.getAccumulatorManager() == null) {
            StyleNodeFactory styleNodeFactory = this.getCompilation().getStyleNodeFactory(true);
            AccumulatorRegistry manager = styleNodeFactory.makeAccumulatorManager();
            top.setAccumulatorManager(manager);
            this.getCompilation().getPackageData().setAccumulatorRegistry(manager);
        }
        if ((existing = (mgr = top.getAccumulatorManager()).getAccumulator(this.accumulator.getAccumulatorName())) != null) {
            int existingPrec = existing.getImportPrecedence();
            if (existingPrec == decl.getPrecedence()) {
                this.compileError("There are two accumulators with the same name (" + this.accumulator.getAccumulatorName().getDisplayName() + ") and the same import precedence", "XTSE3350");
            }
            if (existingPrec > decl.getPrecedence()) {
                return;
            }
        }
        mgr.addAccumulator(this.accumulator);
    }

    @Override
    public void validate(ComponentDeclaration decl) throws XPathException {
        this.slotManager = this.getConfiguration().makeSlotManager();
        this.checkTopLevel("XTSE0010", true);
        boolean foundRule = false;
        for (NodeInfo nodeInfo : this.children()) {
            if (nodeInfo instanceof XSLAccumulatorRule) {
                foundRule = true;
                continue;
            }
            this.compileError("Only xsl:accumulator-rule is allowed here", "XTSE0010");
        }
        if (!foundRule) {
            this.compileError("xsl:accumulator must contain at least one xsl:accumulator-rule", "XTSE0010");
        }
    }

    @Override
    public SlotManager getSlotManager() {
        return this.slotManager;
    }

    @Override
    public void optimize(ComponentDeclaration declaration) throws XPathException {
    }

    public SequenceType getResultType() {
        return this.accumulator.getType();
    }

    @Override
    public void generateByteCode(Optimizer opt) {
    }

    private void checkInitialStreamability(Expression init) throws XPathException {
    }

    private void checkRuleStreamability(XSLAccumulatorRule rule, Pattern pattern, Expression newValueExp) throws XPathException {
    }

    private void notStreamable(StyleElement rule, String message) {
        boolean fallback = this.getConfiguration().getBooleanProperty(Feature.STREAMING_FALLBACK);
        if (fallback) {
            message = message + ". Falling back to non-streaming implementation";
            rule.compileWarning(message, "XTSE3430");
            rule.getCompilation().setFallbackToNonStreaming(true);
        } else {
            rule.compileError(message, "XTSE3430");
        }
    }
}

