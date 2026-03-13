/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import net.sf.saxon.Configuration;
import net.sf.saxon.expr.Atomizer;
import net.sf.saxon.expr.ContextItemExpression;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.FirstItemExpression;
import net.sf.saxon.expr.ItemChecker;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.SimpleStepExpression;
import net.sf.saxon.expr.StaticContext;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.PathMap;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.expr.parser.RoleDiagnostic;
import net.sf.saxon.om.AxisInfo;
import net.sf.saxon.om.Genre;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NamePool;
import net.sf.saxon.om.NamespaceResolver;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.pattern.AnyNodeTest;
import net.sf.saxon.pattern.CombinedNodeTest;
import net.sf.saxon.pattern.ContentTypeTest;
import net.sf.saxon.pattern.DocumentNodeTest;
import net.sf.saxon.pattern.MultipleNodeKindTest;
import net.sf.saxon.pattern.NameTest;
import net.sf.saxon.pattern.NodeKindTest;
import net.sf.saxon.pattern.NodeTest;
import net.sf.saxon.pattern.NodeTestPattern;
import net.sf.saxon.pattern.Pattern;
import net.sf.saxon.pattern.SchemaNodeTest;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.AxisIterator;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.type.Affinity;
import net.sf.saxon.type.AlphaCode;
import net.sf.saxon.type.AnyItemType;
import net.sf.saxon.type.AnySimpleType;
import net.sf.saxon.type.AnyType;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.BuiltInListType;
import net.sf.saxon.type.ComplexType;
import net.sf.saxon.type.ErrorType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.SchemaDeclaration;
import net.sf.saxon.type.SchemaException;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.type.SimpleType;
import net.sf.saxon.type.TypeHierarchy;
import net.sf.saxon.type.UType;
import net.sf.saxon.type.Untyped;
import net.sf.saxon.value.Cardinality;
import net.sf.saxon.z.IntHashSet;
import net.sf.saxon.z.IntIterator;
import net.sf.saxon.z.IntSet;

public final class AxisExpression
extends Expression {
    private int axis;
    private NodeTest test;
    private ItemType itemType = null;
    private ContextItemStaticInfo staticInfo = ContextItemStaticInfo.DEFAULT;
    private boolean doneTypeCheck = false;
    private boolean doneOptimize = false;

    public AxisExpression(int axis, NodeTest nodeTest) {
        this.axis = axis;
        this.test = nodeTest;
    }

    public void setAxis(int axis) {
        this.axis = axis;
    }

    @Override
    public String getExpressionName() {
        return "axisStep";
    }

    @Override
    public Expression simplify() throws XPathException {
        Expression e2 = super.simplify();
        if (e2 != this) {
            return e2;
        }
        if (!(this.test != null && this.test != AnyNodeTest.getInstance() || this.axis != 9 && this.axis != 0)) {
            this.test = MultipleNodeKindTest.PARENT_NODE;
        }
        return this;
    }

    @Override
    public Expression typeCheck(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        ItemType contextItemType = contextInfo.getItemType();
        boolean noWarnings = this.doneOptimize || this.doneTypeCheck && this.staticInfo.getItemType().equals(contextItemType);
        this.doneTypeCheck = true;
        if (contextItemType == ErrorType.getInstance()) {
            XPathException err = new XPathException("Axis step " + this + " cannot be used here: the context item is absent");
            err.setErrorCode("XPDY0002");
            err.setLocation(this.getLocation());
            throw err;
        }
        this.staticInfo = contextInfo;
        Configuration config = visitor.getConfiguration();
        if (contextItemType.getGenre() != Genre.NODE) {
            TypeHierarchy th = config.getTypeHierarchy();
            Affinity relation = th.relationship(contextItemType, AnyNodeTest.getInstance());
            if (relation == Affinity.DISJOINT) {
                XPathException err = new XPathException("Axis step " + this + " cannot be used here: the context item is not a node");
                err.setIsTypeError(true);
                err.setErrorCode("XPTY0020");
                err.setLocation(this.getLocation());
                throw err;
            }
            if (relation == Affinity.OVERLAPS || relation == Affinity.SUBSUMES) {
                Expression thisExp = this.checkPlausibility(visitor, contextInfo, !noWarnings);
                if (Literal.isEmptySequence(thisExp)) {
                    return thisExp;
                }
                ContextItemExpression exp = new ContextItemExpression();
                ExpressionTool.copyLocationInfo(this, exp);
                RoleDiagnostic role = new RoleDiagnostic(14, "", this.axis);
                role.setErrorCode("XPTY0020");
                ItemChecker checker = new ItemChecker(exp, AnyNodeTest.getInstance(), role);
                ExpressionTool.copyLocationInfo(this, checker);
                SimpleStepExpression step = new SimpleStepExpression(checker, thisExp);
                ExpressionTool.copyLocationInfo(this, step);
                return step;
            }
        }
        if (visitor.getStaticContext().getOptimizerOptions().isSet(8192)) {
            return this.checkPlausibility(visitor, contextInfo, !noWarnings);
        }
        return this;
    }

    private Expression checkPlausibility(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo, boolean warnings) throws XPathException {
        block86: {
            UType nonSelfTarget;
            UType kind;
            UType testUType;
            StaticContext env = visitor.getStaticContext();
            Configuration config = env.getConfiguration();
            ItemType contextType = contextInfo.getItemType();
            if (!(contextType instanceof NodeTest)) {
                contextType = AnyNodeTest.getInstance();
            }
            if (this.test != null && !AxisInfo.getTargetUType(UType.ANY_NODE, this.axis).overlaps(this.test.getUType())) {
                if (warnings) {
                    visitor.issueWarning("The " + AxisInfo.axisName[this.axis] + " axis will never select " + this.test.getUType().toStringWithIndefiniteArticle(), this.getLocation());
                }
                return Literal.makeEmptySequence();
            }
            if (this.test instanceof NameTest && this.axis == 8 && !((NameTest)this.test).getNamespaceURI().isEmpty()) {
                if (warnings) {
                    visitor.issueWarning("The names of namespace nodes are never prefixed, so this axis step will never select anything", this.getLocation());
                }
                return Literal.makeEmptySequence();
            }
            UType originUType = contextType.getUType();
            UType targetUType = AxisInfo.getTargetUType(originUType, this.axis);
            UType uType = testUType = this.test == null ? UType.ANY_NODE : this.test.getUType();
            if (targetUType.equals(UType.VOID)) {
                if (warnings) {
                    visitor.issueWarning("The " + AxisInfo.axisName[this.axis] + " axis starting at " + originUType.toStringWithIndefiniteArticle() + " will never select anything", this.getLocation());
                }
                return Literal.makeEmptySequence();
            }
            if (contextInfo.isParentless() && (this.axis == 9 || this.axis == 0)) {
                if (warnings) {
                    visitor.issueWarning("The " + AxisInfo.axisName[this.axis] + " axis will never select anything because the context item is parentless", this.getLocation());
                }
                return Literal.makeEmptySequence();
            }
            if (!targetUType.overlaps(testUType)) {
                if (warnings) {
                    visitor.issueWarning("The " + AxisInfo.axisName[this.axis] + " axis starting at " + originUType.toStringWithIndefiniteArticle() + " will never select " + this.test.getUType().toStringWithIndefiniteArticle(), this.getLocation());
                }
                return Literal.makeEmptySequence();
            }
            int nonSelf = AxisInfo.excludeSelfAxis[this.axis];
            UType uType2 = kind = this.test == null ? UType.ANY_NODE : this.test.getUType();
            if (this.axis != nonSelf && !(nonSelfTarget = AxisInfo.getTargetUType(originUType, nonSelf)).overlaps(testUType)) {
                this.axis = 12;
                targetUType = AxisInfo.getTargetUType(originUType, this.axis);
            }
            ItemType target = targetUType.toItemType();
            this.itemType = this.test == null || this.test instanceof AnyNodeTest ? target : (target instanceof AnyNodeTest || targetUType.subsumes(this.test.getUType()) ? this.test : new CombinedNodeTest((NodeTest)target, 23, this.test));
            int origin = contextType.getPrimitiveType();
            if (this.test != null) {
                SchemaType ct;
                SchemaType contentType;
                Optional<IntSet> selectedElementNames;
                NodeTest elementTest;
                Optional<IntSet> outermostElementNames;
                if (contextType instanceof DocumentNodeTest && kind.equals(UType.ELEMENT) && (outermostElementNames = (elementTest = ((DocumentNodeTest)contextType).getElementTest()).getRequiredNodeNames()).isPresent() && (selectedElementNames = this.test.getRequiredNodeNames()).isPresent()) {
                    if (this.axis == 3) {
                        if (selectedElementNames.get().intersect(outermostElementNames.get()).isEmpty()) {
                            if (warnings) {
                                visitor.issueWarning("Starting at a document node, the step is selecting an element whose name is not among the names of child elements permitted for this document node type", this.getLocation());
                            }
                            return Literal.makeEmptySequence();
                        }
                        if (env.getPackageData().isSchemaAware() && elementTest instanceof SchemaNodeTest && outermostElementNames.get().size() == 1) {
                            IntIterator oeni = outermostElementNames.get().iterator();
                            int outermostElementName = oeni.hasNext() ? oeni.next() : -1;
                            SchemaDeclaration decl = config.getElementDeclaration(outermostElementName);
                            if (decl == null) {
                                if (warnings) {
                                    visitor.issueWarning("Element " + config.getNamePool().getEQName(outermostElementName) + " is not declared in the schema", this.getLocation());
                                }
                                this.itemType = elementTest;
                            } else {
                                SchemaType contentType2 = decl.getType();
                                this.itemType = new CombinedNodeTest(elementTest, 23, new ContentTypeTest(1, contentType2, config, true));
                            }
                        } else {
                            this.itemType = elementTest;
                        }
                        return this;
                    }
                    if (this.axis == 4) {
                        boolean canMatchOutermost;
                        boolean bl = canMatchOutermost = !selectedElementNames.get().intersect(outermostElementNames.get()).isEmpty();
                        if (!canMatchOutermost) {
                            Expression path = ExpressionTool.makePathExpression(new AxisExpression(3, elementTest), new AxisExpression(4, this.test));
                            ExpressionTool.copyLocationInfo(this, path);
                            return path.typeCheck(visitor, contextInfo);
                        }
                    }
                }
                if ((contentType = ((NodeTest)contextType).getContentType()) == AnyType.getInstance()) {
                    return this;
                }
                if (!env.getPackageData().isSchemaAware() && (ct = this.test.getContentType()) != AnyType.getInstance() && ct != Untyped.getInstance() && ct != AnySimpleType.getInstance() && ct != BuiltInAtomicType.ANY_ATOMIC && ct != BuiltInAtomicType.UNTYPED_ATOMIC && ct != BuiltInAtomicType.STRING) {
                    if (warnings) {
                        visitor.issueWarning("The " + AxisInfo.axisName[this.axis] + " axis will never select any typed nodes, because the expression is being compiled in an environment that is not schema-aware", this.getLocation());
                    }
                    return Literal.makeEmptySequence();
                }
                int targetfp = this.test.getFingerprint();
                StructuredQName targetName = this.test.getMatchingNodeName();
                if (contentType.isSimpleType()) {
                    if (warnings) {
                        if ((this.axis == 3 || this.axis == 4 || this.axis == 5) && UType.PARENT_NODE_KINDS.union(UType.ATTRIBUTE).subsumes(kind)) {
                            visitor.issueWarning("The " + AxisInfo.axisName[this.axis] + " axis will never select any " + kind + " nodes when starting at " + (origin == 2 ? "an attribute node" : AxisExpression.getStartingNodeDescription(contentType)), this.getLocation());
                        } else if (this.axis == 3 && kind.equals(UType.TEXT) && this.getParentExpression() instanceof Atomizer) {
                            visitor.issueWarning("Selecting the text nodes of an element with simple content may give the wrong answer in the presence of comments or processing instructions. It is usually better to omit the '/text()' step", this.getLocation());
                        } else if (this.axis == 2) {
                            Iterator<? extends SchemaType> extensions = config.getExtensionsOfType(contentType);
                            boolean found = false;
                            if (targetfp == -1) {
                                while (extensions.hasNext()) {
                                    ComplexType extension = (ComplexType)extensions.next();
                                    if (!extension.allowsAttributes()) continue;
                                    found = true;
                                    break;
                                }
                            } else {
                                while (extensions.hasNext()) {
                                    ComplexType extension = (ComplexType)extensions.next();
                                    try {
                                        if (extension.getAttributeUseType(targetName) == null) continue;
                                        found = true;
                                        break;
                                    } catch (SchemaException contentType2) {
                                    }
                                }
                            }
                            if (!found) {
                                visitor.issueWarning("The " + AxisInfo.axisName[this.axis] + " axis will never select " + (targetName == null ? "any attribute nodes" : "an attribute node named " + AxisExpression.getDiagnosticName(targetName, env)) + " when starting at " + AxisExpression.getStartingNodeDescription(contentType), this.getLocation());
                            }
                        }
                    }
                } else {
                    if (((ComplexType)contentType).isSimpleContent() && (this.axis == 3 || this.axis == 4 || this.axis == 5) && UType.PARENT_NODE_KINDS.subsumes(kind)) {
                        if (warnings) {
                            visitor.issueWarning("The " + AxisInfo.axisName[this.axis] + " axis will never select any " + kind + " nodes when starting at " + AxisExpression.getStartingNodeDescription(contentType) + ", as this type requires simple content", this.getLocation());
                        }
                        return Literal.makeEmptySequence();
                    }
                    if (((ComplexType)contentType).isEmptyContent() && (this.axis == 3 || this.axis == 4 || this.axis == 5)) {
                        Iterator<? extends SchemaType> iter = config.getExtensionsOfType(contentType);
                        while (iter.hasNext()) {
                            ComplexType extension = (ComplexType)iter.next();
                            if (extension.isEmptyContent()) continue;
                            return this;
                        }
                        if (warnings) {
                            visitor.issueWarning("The " + AxisInfo.axisName[this.axis] + " axis will never select any nodes when starting at " + AxisExpression.getStartingNodeDescription(contentType) + ", as this type requires empty content", this.getLocation());
                        }
                        return Literal.makeEmptySequence();
                    }
                    if (this.axis == 2) {
                        if (targetfp == -1) {
                            if (warnings && !((ComplexType)contentType).allowsAttributes()) {
                                visitor.issueWarning("The complex type " + contentType.getDescription() + " allows no attributes other than the standard attributes in the xsi namespace", this.getLocation());
                            }
                        } else {
                            try {
                                SimpleType schemaType = targetfp == 641 ? BuiltInAtomicType.QNAME : (targetfp == 643 ? BuiltInListType.ANY_URIS : (targetfp == 644 ? BuiltInAtomicType.ANY_URI : (targetfp == 642 ? BuiltInAtomicType.BOOLEAN : ((ComplexType)contentType).getAttributeUseType(targetName))));
                                if (schemaType == null) {
                                    if (warnings) {
                                        visitor.issueWarning("The complex type " + contentType.getDescription() + " does not allow an attribute named " + AxisExpression.getDiagnosticName(targetName, env), this.getLocation());
                                        return Literal.makeEmptySequence();
                                    }
                                    break block86;
                                }
                                this.itemType = new CombinedNodeTest(this.test, 23, new ContentTypeTest(2, schemaType, config, false));
                            } catch (SchemaException schemaType) {}
                        }
                    } else if (this.axis == 3 && kind.equals(UType.ELEMENT)) {
                        try {
                            SchemaType schemaType;
                            int childfp = targetfp;
                            if (targetName == null) {
                                if (((ComplexType)contentType).containsElementWildcard()) {
                                    return this;
                                }
                                IntHashSet children = new IntHashSet();
                                ((ComplexType)contentType).gatherAllPermittedChildren(children, false);
                                if (children.isEmpty()) {
                                    if (warnings) {
                                        visitor.issueWarning("The complex type " + contentType.getDescription() + " does not allow children", this.getLocation());
                                    }
                                    return Literal.makeEmptySequence();
                                }
                                if (children.size() == 1) {
                                    IntIterator iter = children.iterator();
                                    if (iter.hasNext()) {
                                        childfp = iter.next();
                                    }
                                } else {
                                    return this;
                                }
                            }
                            if ((schemaType = ((ComplexType)contentType).getElementParticleType(childfp, true)) == null) {
                                if (warnings) {
                                    StructuredQName childElement = this.getConfiguration().getNamePool().getStructuredQName(childfp);
                                    String message = "The complex type " + contentType.getDescription() + " does not allow a child element named " + AxisExpression.getDiagnosticName(childElement, env);
                                    IntHashSet permitted = new IntHashSet();
                                    ((ComplexType)contentType).gatherAllPermittedChildren(permitted, false);
                                    if (!permitted.contains(-1)) {
                                        IntIterator kids = permitted.iterator();
                                        while (kids.hasNext()) {
                                            int kid = kids.next();
                                            StructuredQName sq = this.getConfiguration().getNamePool().getStructuredQName(kid);
                                            if (!sq.getLocalPart().equals(childElement.getLocalPart()) || kid == childfp) continue;
                                            message = message + ". Perhaps the namespace is " + (childElement.hasURI("") ? "missing" : "wrong") + ", and " + sq.getEQName() + " was intended?";
                                            break;
                                        }
                                    }
                                    visitor.issueWarning(message, this.getLocation());
                                }
                                return Literal.makeEmptySequence();
                            }
                            this.itemType = new CombinedNodeTest(this.test, 23, new ContentTypeTest(1, schemaType, config, true));
                            int computedCardinality = ((ComplexType)contentType).getElementParticleCardinality(childfp, true);
                            ExpressionTool.resetStaticProperties(this);
                            if (computedCardinality == 8192) {
                                StructuredQName childElement = this.getConfiguration().getNamePool().getStructuredQName(childfp);
                                visitor.issueWarning("The complex type " + contentType.getDescription() + " appears not to allow a child element named " + AxisExpression.getDiagnosticName(childElement, env), this.getLocation());
                                return Literal.makeEmptySequence();
                            }
                            if (!(Cardinality.allowsMany(computedCardinality) || this.getParentExpression() instanceof FirstItemExpression || visitor.isOptimizeForPatternMatching())) {
                                return FirstItemExpression.makeFirstItemExpression(this);
                            }
                        } catch (SchemaException childfp) {}
                    } else if (this.axis == 4 && kind.equals(UType.ELEMENT) && targetfp != -1) {
                        try {
                            IntHashSet descendants = new IntHashSet();
                            ((ComplexType)contentType).gatherAllPermittedDescendants(descendants);
                            if (descendants.contains(-1)) {
                                return this;
                            }
                            if (descendants.contains(targetfp)) {
                                SchemaType st;
                                IntHashSet children = new IntHashSet();
                                ((ComplexType)contentType).gatherAllPermittedChildren(children, false);
                                IntHashSet usefulChildren = new IntHashSet();
                                boolean considerSelf = false;
                                boolean considerDescendants = false;
                                IntIterator kids = children.iterator();
                                while (kids.hasNext()) {
                                    SchemaType st2;
                                    int c = kids.next();
                                    if (c == targetfp) {
                                        usefulChildren.add(c);
                                        considerSelf = true;
                                    }
                                    if ((st2 = ((ComplexType)contentType).getElementParticleType(c, true)) == null) {
                                        throw new AssertionError((Object)("Can't find type for child element " + c));
                                    }
                                    if (!(st2 instanceof ComplexType)) continue;
                                    IntHashSet subDescendants = new IntHashSet();
                                    ((ComplexType)st2).gatherAllPermittedDescendants(subDescendants);
                                    if (!subDescendants.contains(targetfp)) continue;
                                    usefulChildren.add(c);
                                    considerDescendants = true;
                                }
                                this.itemType = this.test;
                                if (considerDescendants && (st = ((ComplexType)contentType).getDescendantElementType(targetfp)) != AnyType.getInstance()) {
                                    this.itemType = new CombinedNodeTest(this.test, 23, new ContentTypeTest(1, st, config, true));
                                }
                                if (usefulChildren.size() < children.size()) {
                                    NodeTest childTest = this.makeUnionNodeTest(usefulChildren, config.getNamePool());
                                    AxisExpression first = new AxisExpression(3, childTest);
                                    ExpressionTool.copyLocationInfo(this, first);
                                    int nextAxis = considerSelf ? (considerDescendants ? 5 : 12) : 4;
                                    AxisExpression next = new AxisExpression(nextAxis, (NodeTest)this.itemType);
                                    ExpressionTool.copyLocationInfo(this, next);
                                    Expression path = ExpressionTool.makePathExpression(first, next);
                                    ExpressionTool.copyLocationInfo(this, path);
                                    return path.typeCheck(visitor, contextInfo);
                                }
                            } else if (warnings) {
                                visitor.issueWarning("The complex type " + contentType.getDescription() + " does not allow a descendant element named " + AxisExpression.getDiagnosticName(targetName, env), this.getLocation());
                            }
                        } catch (SchemaException e) {
                            throw new AssertionError((Object)e);
                        }
                    }
                }
            }
        }
        return this;
    }

    private static String getDiagnosticName(StructuredQName name, StaticContext env) {
        String uri = name.getURI();
        if (uri.equals("")) {
            return name.getLocalPart();
        }
        NamespaceResolver resolver = env.getNamespaceResolver();
        Iterator<String> it = resolver.iteratePrefixes();
        while (it.hasNext()) {
            String prefix = it.next();
            if (!uri.equals(resolver.getURIForPrefix(prefix, true))) continue;
            if (prefix.isEmpty()) {
                return "Q{" + uri + "}" + name.getLocalPart();
            }
            return prefix + ":" + name.getLocalPart();
        }
        return "Q{" + uri + "}" + name.getLocalPart();
    }

    private static String getStartingNodeDescription(SchemaType type) {
        String s = type.getDescription();
        if (s.startsWith("of element")) {
            return "a valid element named" + s.substring("of element".length());
        }
        if (s.startsWith("of attribute")) {
            return "a valid attribute named" + s.substring("of attribute".length());
        }
        return "a node with " + (type.isSimpleType() ? "simple" : "complex") + " type " + s;
    }

    private NodeTest makeUnionNodeTest(IntHashSet elements, NamePool pool) {
        NodeTest test = null;
        IntIterator iter = elements.iterator();
        while (iter.hasNext()) {
            int fp = iter.next();
            NameTest nextTest = new NameTest(1, fp, pool);
            if (test == null) {
                test = nextTest;
                continue;
            }
            test = new CombinedNodeTest(test, 1, nextTest);
        }
        return test;
    }

    public ItemType getContextItemType() {
        return this.staticInfo.getItemType();
    }

    @Override
    public Expression optimize(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) {
        this.doneOptimize = true;
        this.staticInfo = contextInfo;
        return this;
    }

    @Override
    public double getCost() {
        switch (this.axis) {
            case 2: 
            case 9: 
            case 12: {
                return 1.0;
            }
            case 0: 
            case 1: 
            case 3: 
            case 7: 
            case 11: {
                return 5.0;
            }
        }
        return 20.0;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof AxisExpression && this.axis == ((AxisExpression)other).axis && Objects.equals(this.test, ((AxisExpression)other).test);
    }

    @Override
    public int computeHashCode() {
        int h = 9375162 + this.axis << 20;
        if (this.test != null) {
            h ^= this.test.getPrimitiveType() << 16;
            h ^= this.test.getFingerprint();
        }
        return h;
    }

    @Override
    public Expression copy(RebindingMap rebindings) {
        AxisExpression a2 = new AxisExpression(this.axis, this.test);
        a2.itemType = this.itemType;
        a2.staticInfo = this.staticInfo;
        a2.doneTypeCheck = this.doneTypeCheck;
        a2.doneOptimize = this.doneOptimize;
        ExpressionTool.copyLocationInfo(this, a2);
        return a2;
    }

    @Override
    public int computeSpecialProperties() {
        return 0x1810000 | (AxisInfo.isForwards[this.axis] ? 131072 : 262144) | (AxisInfo.isPeerAxis[this.axis] || AxisExpression.isPeerNodeTest(this.test) ? 524288 : 0) | (AxisInfo.isSubtreeAxis[this.axis] ? 0x100000 : 0) | (this.axis == 2 || this.axis == 8 ? 0x200000 : 0);
    }

    private static boolean isPeerNodeTest(NodeTest test) {
        if (test == null) {
            return false;
        }
        UType uType = test.getUType();
        if (uType.overlaps(UType.ELEMENT)) {
            return false;
        }
        if (uType.overlaps(UType.DOCUMENT)) {
            return uType.equals(UType.DOCUMENT);
        }
        return true;
    }

    @Override
    public final ItemType getItemType() {
        if (this.itemType != null) {
            return this.itemType;
        }
        short p = AxisInfo.principalNodeType[this.axis];
        switch (p) {
            case 2: 
            case 13: {
                return NodeKindTest.makeNodeKindTest(p);
            }
        }
        if (this.test == null) {
            return AnyNodeTest.getInstance();
        }
        return this.test;
    }

    @Override
    public UType getStaticUType(UType contextItemType) {
        UType reachable = AxisInfo.getTargetUType(contextItemType, this.axis);
        if (this.test == null) {
            return reachable;
        }
        return reachable.intersection(this.test.getUType());
    }

    @Override
    public int getIntrinsicDependencies() {
        return 2;
    }

    @Override
    public final int computeCardinality() {
        NodeTest originNodeType;
        NodeTest nodeTest = this.test;
        ItemType contextItemType = this.staticInfo.getItemType();
        if (contextItemType instanceof NodeTest) {
            originNodeType = (NodeTest)contextItemType;
        } else if (contextItemType instanceof AnyItemType) {
            originNodeType = AnyNodeTest.getInstance();
        } else {
            return 57344;
        }
        if (this.axis == 2 && nodeTest instanceof NameTest) {
            SchemaType contentType = originNodeType.getContentType();
            if (contentType instanceof ComplexType) {
                try {
                    return ((ComplexType)contentType).getAttributeUseCardinality(nodeTest.getMatchingNodeName());
                } catch (SchemaException err) {
                    return 24576;
                }
            }
            if (contentType instanceof SimpleType) {
                return 8192;
            }
            return 24576;
        }
        if (this.axis == 4 && nodeTest instanceof NameTest && nodeTest.getPrimitiveType() == 1) {
            SchemaType contentType = originNodeType.getContentType();
            if (contentType instanceof ComplexType) {
                try {
                    return ((ComplexType)contentType).getDescendantElementCardinality(nodeTest.getFingerprint());
                } catch (SchemaException err) {
                    return 57344;
                }
            }
            return 8192;
        }
        if (this.axis == 12) {
            return 24576;
        }
        return 57344;
    }

    @Override
    public boolean isSubtreeExpression() {
        return AxisInfo.isSubtreeAxis[this.axis];
    }

    public int getAxis() {
        return this.axis;
    }

    public NodeTest getNodeTest() {
        return this.test;
    }

    @Override
    public PathMap.PathMapNodeSet addToPathMap(PathMap pathMap, PathMap.PathMapNodeSet pathMapNodeSet) {
        if (pathMapNodeSet == null) {
            ContextItemExpression cie = new ContextItemExpression();
            pathMapNodeSet = new PathMap.PathMapNodeSet(pathMap.makeNewRoot(cie));
        }
        return pathMapNodeSet.createArc(this.axis, this.test == null ? AnyNodeTest.getInstance() : this.test);
    }

    public boolean isContextPossiblyUndefined() {
        return this.staticInfo.isPossiblyAbsent();
    }

    public ContextItemStaticInfo getContextItemStaticInfo() {
        return this.staticInfo;
    }

    @Override
    public Pattern toPattern(Configuration config) throws XPathException {
        NodeTestPattern pat;
        NodeTest test = this.getNodeTest();
        if (test == null) {
            test = AnyNodeTest.getInstance();
        }
        if (test instanceof AnyNodeTest && (this.axis == 3 || this.axis == 4 || this.axis == 12)) {
            test = MultipleNodeKindTest.CHILD_NODE;
        }
        int kind = test.getPrimitiveType();
        if (this.axis == 12) {
            pat = new NodeTestPattern(test);
        } else if (this.axis == 2) {
            pat = kind == 0 ? new NodeTestPattern(NodeKindTest.ATTRIBUTE) : (!AxisInfo.containsNodeKind(this.axis, kind) ? new NodeTestPattern(ErrorType.getInstance()) : new NodeTestPattern(test));
        } else if (this.axis == 3 || this.axis == 4 || this.axis == 5) {
            pat = kind != 0 && !AxisInfo.containsNodeKind(this.axis, kind) ? new NodeTestPattern(ErrorType.getInstance()) : new NodeTestPattern(test);
        } else if (this.axis == 8) {
            pat = kind == 0 ? new NodeTestPattern(NodeKindTest.NAMESPACE) : (!AxisInfo.containsNodeKind(this.axis, kind) ? new NodeTestPattern(ErrorType.getInstance()) : new NodeTestPattern(test));
        } else {
            throw new XPathException("Only downwards axes are allowed in a pattern", "XTSE0340");
        }
        ExpressionTool.copyLocationInfo(this, pat);
        return pat;
    }

    @Override
    public int getImplementationMethod() {
        return 2;
    }

    @Override
    public SequenceIterator iterate(XPathContext context) throws XPathException {
        Item item = context.getContextItem();
        if (item == null) {
            XPathException err = new XPathException("The context item for axis step " + this + " is absent");
            err.setErrorCode("XPDY0002");
            err.setXPathContext(context);
            err.setLocation(this.getLocation());
            err.setIsTypeError(true);
            throw err;
        }
        try {
            if (this.test == null) {
                return ((NodeInfo)item).iterateAxis(this.axis);
            }
            return ((NodeInfo)item).iterateAxis(this.axis, this.test);
        } catch (ClassCastException cce) {
            XPathException err = new XPathException("The context item for axis step " + this + " is not a node");
            err.setErrorCode("XPTY0020");
            err.setXPathContext(context);
            err.setLocation(this.getLocation());
            err.setIsTypeError(true);
            throw err;
        } catch (UnsupportedOperationException err) {
            if (err.getCause() instanceof XPathException) {
                XPathException ec = (XPathException)err.getCause();
                ec.maybeSetLocation(this.getLocation());
                ec.maybeSetContext(context);
                throw ec;
            }
            this.dynamicError(err.getMessage(), "XPST0010", context);
            return null;
        }
    }

    public AxisIterator iterate(NodeInfo origin) {
        if (this.test == null) {
            return origin.iterateAxis(this.axis);
        }
        return origin.iterateAxis(this.axis, this.test);
    }

    @Override
    public void export(ExpressionPresenter destination) throws XPathException {
        destination.startElement("axis", this);
        destination.emitAttribute("name", AxisInfo.axisName[this.axis]);
        destination.emitAttribute("nodeTest", AlphaCode.fromItemType(this.test == null ? AnyNodeTest.getInstance() : this.test));
        destination.endElement();
    }

    @Override
    public String toString() {
        FastStringBuffer fsb = new FastStringBuffer(16);
        fsb.append(AxisInfo.axisName[this.axis]);
        fsb.append("::");
        fsb.append(this.test == null ? "node()" : this.test.toString());
        return fsb.toString();
    }

    @Override
    public String toShortString() {
        FastStringBuffer fsb = new FastStringBuffer(16);
        if (this.axis != 3) {
            if (this.axis == 2) {
                fsb.append("@");
            } else {
                fsb.append(AxisInfo.axisName[this.axis]);
                fsb.append("::");
            }
        }
        if (this.test == null) {
            fsb.append("node()");
        } else if (this.test instanceof NameTest) {
            if (((NameTest)this.test).getNodeKind() != AxisInfo.principalNodeType[this.axis]) {
                fsb.append(this.test.toString());
            } else {
                fsb.append(this.test.getMatchingNodeName().getDisplayName());
            }
        } else {
            fsb.append(this.test.toString());
        }
        return fsb.toString();
    }

    @Override
    public String getStreamerName() {
        return "AxisExpression";
    }

    public Set<Expression> getPreconditions() {
        HashSet<Expression> pre = new HashSet<Expression>(1);
        Expression a = this.copy(new RebindingMap());
        a.setRetainedStaticContext(this.getRetainedStaticContext());
        pre.add(a);
        return pre;
    }
}

