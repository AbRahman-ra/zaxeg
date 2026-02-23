/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.pattern;

import net.sf.saxon.Configuration;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.ItemMappingFunction;
import net.sf.saxon.expr.ItemMappingIterator;
import net.sf.saxon.expr.LocalBinding;
import net.sf.saxon.expr.LocalVariableReference;
import net.sf.saxon.expr.MappingFunction;
import net.sf.saxon.expr.MappingIterator;
import net.sf.saxon.expr.Operand;
import net.sf.saxon.expr.PackageData;
import net.sf.saxon.expr.PseudoExpression;
import net.sf.saxon.expr.StaticContext;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.instruct.SlotManager;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.expr.parser.XPathParser;
import net.sf.saxon.functions.Current;
import net.sf.saxon.lib.Feature;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.TreeInfo;
import net.sf.saxon.pattern.MultipleNodeKindTest;
import net.sf.saxon.pattern.NodeKindTest;
import net.sf.saxon.pattern.NodeTest;
import net.sf.saxon.pattern.PatternParser;
import net.sf.saxon.s9api.HostLanguage;
import net.sf.saxon.style.ExpressionContext;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.AxisIterator;
import net.sf.saxon.tree.iter.ConcatenatingAxisIterator;
import net.sf.saxon.tree.iter.EmptyIterator;
import net.sf.saxon.tree.iter.SingleNodeIterator;
import net.sf.saxon.tree.iter.SingletonIterator;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.UType;

public abstract class Pattern
extends PseudoExpression {
    private double priority = 0.5;
    private boolean recoverable = true;
    private String originalText;

    public static Pattern make(String pattern, StaticContext env, PackageData packageData) throws XPathException {
        int languageLevel = env.getConfiguration().getConfigurationProperty(Feature.XPATH_VERSION_FOR_XSLT);
        if (languageLevel == 30) {
            languageLevel = 305;
        }
        int lineNumber = env instanceof ExpressionContext ? ((ExpressionContext)env).getStyleElement().getLineNumber() : -1;
        PatternParser parser = (PatternParser)((Object)env.getConfiguration().newExpressionParser("PATTERN", false, languageLevel));
        ((XPathParser)((Object)parser)).setLanguage(XPathParser.ParsedLanguage.XSLT_PATTERN, 30);
        Pattern pat = parser.parsePattern(pattern, env);
        pat.setRetainedStaticContext(env.makeRetainedStaticContext());
        pat = pat.simplify();
        return pat;
    }

    protected static void replaceCurrent(Expression exp, LocalBinding binding) {
        for (Operand o : exp.operands()) {
            Expression child = o.getChildExpression();
            if (child.isCallOn(Current.class)) {
                LocalVariableReference ref = new LocalVariableReference(binding);
                o.setChildExpression(ref);
                continue;
            }
            Pattern.replaceCurrent(child, binding);
        }
    }

    public static boolean patternContainsVariable(Pattern pattern) {
        return pattern != null && (pattern.getDependencies() & 0x80) != 0;
    }

    @Override
    public boolean isLiftable(boolean forStreaming) {
        return false;
    }

    public void bindCurrent(LocalBinding binding) {
    }

    public boolean matchesCurrentGroup() {
        return false;
    }

    public void setOriginalText(String text) {
        this.originalText = text;
    }

    public boolean isRecoverable() {
        return this.recoverable;
    }

    public void setRecoverable(boolean recoverable) {
        this.recoverable = recoverable;
    }

    protected void handleDynamicError(XPathException ex, XPathContext context) throws XPathException {
        if ("XTDE0640".equals(ex.getErrorCodeLocalPart())) {
            throw ex;
        }
        if (!this.isRecoverable()) {
            throw ex;
        }
        context.getController().warning("An error occurred matching pattern {" + this + "}: " + ex.getMessage(), ex.getErrorCodeQName().getEQName(), this.getLocation());
    }

    @Override
    public Pattern simplify() throws XPathException {
        return this;
    }

    @Override
    public Pattern typeCheck(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        this.typeCheckChildren(visitor, contextInfo);
        return this;
    }

    @Override
    public int getDependencies() {
        return 0;
    }

    public int allocateSlots(SlotManager slotManager, int nextFree) {
        return nextFree;
    }

    public boolean isMotionless() {
        return true;
    }

    @Override
    public final boolean effectiveBooleanValue(XPathContext context) throws XPathException {
        return this.matches(context.getContextItem(), context);
    }

    public abstract boolean matches(Item var1, XPathContext var2) throws XPathException;

    public boolean matchesBeneathAnchor(NodeInfo node, NodeInfo anchor, XPathContext context) throws XPathException {
        return this.matches(node, context);
    }

    public SequenceIterator selectNodes(TreeInfo document, XPathContext context) throws XPathException {
        NodeInfo doc = document.getRootNode();
        UType uType = this.getUType();
        if (UType.DOCUMENT.subsumes(uType)) {
            if (this.matches(doc, context)) {
                return SingletonIterator.makeIterator(doc);
            }
            return EmptyIterator.ofNodes();
        }
        if (UType.ATTRIBUTE.subsumes(uType)) {
            AxisIterator allElements = doc.iterateAxis(4, NodeKindTest.ELEMENT);
            MappingFunction atts = item -> ((NodeInfo)item).iterateAxis(2);
            MappingIterator allAttributes = new MappingIterator(allElements, atts);
            ItemMappingFunction selection = item -> this.matches(item, context) ? (NodeInfo)item : null;
            return new ItemMappingIterator(allAttributes, selection);
        }
        if (UType.NAMESPACE.subsumes(uType)) {
            AxisIterator allElements = doc.iterateAxis(4, NodeKindTest.ELEMENT);
            MappingFunction atts = item -> ((NodeInfo)item).iterateAxis(8);
            MappingIterator allNamespaces = new MappingIterator(allElements, atts);
            ItemMappingFunction selection = item -> this.matches(item, context) ? (NodeInfo)item : null;
            return new ItemMappingIterator(allNamespaces, selection);
        }
        if (UType.CHILD_NODE_KINDS.subsumes(uType)) {
            NodeTest nodeTest = uType.equals(UType.ELEMENT) ? NodeKindTest.ELEMENT : new MultipleNodeKindTest(uType);
            AxisIterator allChildren = doc.iterateAxis(4, nodeTest);
            ItemMappingFunction selection = item -> this.matches(item, context) ? (NodeInfo)item : null;
            return new ItemMappingIterator(allChildren, selection);
        }
        int axis = uType.subsumes(UType.DOCUMENT) ? 5 : 4;
        AxisIterator allChildren = doc.iterateAxis(axis);
        MappingFunction processElement = item -> {
            AxisIterator mapper = SingleNodeIterator.makeIterator((NodeInfo)item);
            if (uType.subsumes(UType.NAMESPACE)) {
                mapper = new ConcatenatingAxisIterator(mapper, ((NodeInfo)item).iterateAxis(8));
            }
            if (uType.subsumes(UType.ATTRIBUTE)) {
                mapper = new ConcatenatingAxisIterator(mapper, ((NodeInfo)item).iterateAxis(2));
            }
            return mapper;
        };
        MappingIterator attributesOrSelf = new MappingIterator(allChildren, processElement);
        ItemMappingFunction test = item -> {
            if (this.matches(item, context)) {
                return (NodeInfo)item;
            }
            return null;
        };
        return new ItemMappingIterator(attributesOrSelf, test);
    }

    public abstract UType getUType();

    public int getFingerprint() {
        return -1;
    }

    @Override
    public abstract ItemType getItemType();

    public void setPriority(double priority) {
        this.priority = priority;
    }

    public double getDefaultPriority() {
        return this.priority;
    }

    public String getOriginalText() {
        return this.originalText;
    }

    @Override
    public String toString() {
        if (this.originalText != null) {
            return this.originalText;
        }
        return this.reconstruct();
    }

    public String reconstruct() {
        return "pattern matching " + this.getItemType();
    }

    public HostLanguage getHostLanguage() {
        return HostLanguage.XSLT;
    }

    public Pattern convertToTypedPattern(String val) throws XPathException {
        return null;
    }

    @Override
    public Pattern toPattern(Configuration config) {
        return this;
    }

    @Override
    public abstract void export(ExpressionPresenter var1) throws XPathException;

    @Override
    public abstract Pattern copy(RebindingMap var1);

    @Override
    public Pattern optimize(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        return this;
    }

    @Override
    public String toShortString() {
        return this.toString();
    }
}

