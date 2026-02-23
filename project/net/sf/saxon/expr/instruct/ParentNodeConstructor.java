/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.instruct;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.Operand;
import net.sf.saxon.expr.OperandRole;
import net.sf.saxon.expr.OperandUsage;
import net.sf.saxon.expr.StaticContext;
import net.sf.saxon.expr.instruct.Block;
import net.sf.saxon.expr.instruct.Instruction;
import net.sf.saxon.expr.instruct.InstructionWithComplexContent;
import net.sf.saxon.expr.instruct.ValidatingInstruction;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.PathMap;
import net.sf.saxon.lib.ParseOptions;
import net.sf.saxon.pattern.MultipleNodeKindTest;
import net.sf.saxon.pattern.NodeKindTest;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.Affinity;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.type.TypeHierarchy;
import net.sf.saxon.type.Untyped;
import net.sf.saxon.value.SequenceType;

public abstract class ParentNodeConstructor
extends Instruction
implements ValidatingInstruction,
InstructionWithComplexContent {
    private static final OperandRole SAME_FOCUS_CONTENT = new OperandRole(0, OperandUsage.ABSORPTION, SequenceType.ANY_SEQUENCE);
    protected Operand contentOp;
    private ParseOptions validationOptions = null;
    protected boolean preservingTypes = true;

    @Override
    public SchemaType getSchemaType() {
        return this.validationOptions == null ? null : this.validationOptions.getTopLevelType();
    }

    public ParseOptions getValidationOptions() {
        return this.validationOptions;
    }

    public void setValidationAction(int mode, SchemaType schemaType) {
        boolean bl = this.preservingTypes = mode == 3 && schemaType == null;
        if (!this.preservingTypes) {
            if (this.validationOptions == null) {
                this.validationOptions = new ParseOptions();
            }
            if (schemaType == Untyped.getInstance()) {
                this.validationOptions.setSchemaValidationMode(4);
            } else {
                this.validationOptions.setSchemaValidationMode(mode);
                this.validationOptions.setTopLevelType(schemaType);
            }
        }
    }

    @Override
    public int getValidationAction() {
        return this.validationOptions == null ? 3 : this.validationOptions.getSchemaValidationMode();
    }

    public void setNoNeedToStrip() {
        this.preservingTypes = true;
    }

    public void setContentExpression(Expression content) {
        if (this.contentOp == null) {
            this.contentOp = new Operand(this, content, SAME_FOCUS_CONTENT);
        } else {
            this.contentOp.setChildExpression(content);
        }
    }

    @Override
    public Expression getContentExpression() {
        return this.contentOp == null ? null : this.contentOp.getChildExpression();
    }

    public Operand getContentOperand() {
        return this.contentOp;
    }

    @Override
    public int computeCardinality() {
        return 16384;
    }

    @Override
    public Expression typeCheck(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        this.typeCheckChildren(visitor, contextInfo);
        this.checkContentSequence(visitor.getStaticContext());
        return this;
    }

    @Override
    public boolean allowExtractingCommonSubexpressions() {
        return false;
    }

    protected abstract void checkContentSequence(StaticContext var1) throws XPathException;

    @Override
    public Expression optimize(ExpressionVisitor visitor, ContextItemStaticInfo contextItemType) throws XPathException {
        this.optimizeChildren(visitor, contextItemType);
        if (!Literal.isEmptySequence(this.getContentExpression())) {
            if (this.getContentExpression() instanceof Block) {
                this.setContentExpression(((Block)this.getContentExpression()).mergeAdjacentTextInstructions());
            }
            if (visitor.isOptimizeForStreaming()) {
                visitor.obtainOptimizer().makeCopyOperationsExplicit(this, this.contentOp);
            }
        }
        if (visitor.getStaticContext().getPackageData().isSchemaAware()) {
            TypeHierarchy th = visitor.getConfiguration().getTypeHierarchy();
            if (this.getValidationAction() == 4 && (this.getContentExpression().hasSpecialProperty(0x8000000) || th.relationship(this.getContentExpression().getItemType(), MultipleNodeKindTest.DOC_ELEM_ATTR) == Affinity.DISJOINT)) {
                this.setNoNeedToStrip();
            }
        } else {
            this.setValidationAction(4, null);
            this.setNoNeedToStrip();
        }
        return this;
    }

    @Override
    public final boolean mayCreateNewNodes() {
        return true;
    }

    @Override
    public boolean alwaysCreatesNewNodes() {
        return true;
    }

    @Override
    public int getCardinality() {
        return 16384;
    }

    @Override
    public PathMap.PathMapNodeSet addToPathMap(PathMap pathMap, PathMap.PathMapNodeSet pathMapNodeSet) {
        PathMap.PathMapNodeSet result = super.addToPathMap(pathMap, pathMapNodeSet);
        result.setReturnable(false);
        TypeHierarchy th = this.getConfiguration().getTypeHierarchy();
        ItemType type = this.getItemType();
        if (th.relationship(type, NodeKindTest.ELEMENT) != Affinity.DISJOINT || th.relationship(type, NodeKindTest.DOCUMENT) != Affinity.DISJOINT) {
            result.addDescendants();
        }
        return new PathMap.PathMapNodeSet(pathMap.makeNewRoot(this));
    }

    public boolean isPreservingTypes() {
        return this.preservingTypes;
    }

    public boolean isLocal() {
        return ExpressionTool.isLocalConstructor(this);
    }
}

