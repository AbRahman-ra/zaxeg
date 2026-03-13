/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.instruct;

import java.util.HashSet;
import java.util.Set;
import net.sf.saxon.Controller;
import net.sf.saxon.event.Outputter;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.Operand;
import net.sf.saxon.expr.OperandRole;
import net.sf.saxon.expr.OperandUsage;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.XPathContextMinor;
import net.sf.saxon.expr.accum.Accumulator;
import net.sf.saxon.expr.instruct.Instruction;
import net.sf.saxon.expr.instruct.TailCall;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.expr.parser.RoleDiagnostic;
import net.sf.saxon.expr.parser.TypeChecker;
import net.sf.saxon.functions.DocumentFn;
import net.sf.saxon.lib.ParseOptions;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.SpaceStrippingRule;
import net.sf.saxon.pattern.NodeKindTest;
import net.sf.saxon.style.StylesheetPackage;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.QuitParsingException;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.trans.XsltController;
import net.sf.saxon.tree.iter.ManualIterator;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.value.SequenceType;

public class SourceDocument
extends Instruction {
    protected Operand hrefOp;
    protected Operand bodyOp;
    protected ParseOptions parseOptions;
    protected Set<? extends Accumulator> accumulators = new HashSet<Accumulator>();

    public SourceDocument(Expression hrefExp, Expression body, ParseOptions options) {
        this.hrefOp = new Operand(this, hrefExp, OperandRole.SINGLE_ATOMIC);
        this.bodyOp = new Operand(this, body, new OperandRole(64, OperandUsage.TRANSMISSION));
        this.parseOptions = options;
        this.accumulators = options.getApplicableAccumulators();
    }

    @Override
    public String getExpressionName() {
        return "xsl:source-document";
    }

    public String getExportTag() {
        return "sourceDoc";
    }

    public Expression getHref() {
        return this.hrefOp.getChildExpression();
    }

    public void setHref(Expression href) {
        this.hrefOp.setChildExpression(href);
    }

    public Expression getBody() {
        return this.bodyOp.getChildExpression();
    }

    public void setBody(Expression body) {
        this.bodyOp.setChildExpression(body);
    }

    public void setUsedAccumulators(Set<? extends Accumulator> used) {
        this.accumulators = used;
    }

    @Override
    public Iterable<Operand> operands() {
        return this.operandList(this.hrefOp, this.bodyOp);
    }

    @Override
    public boolean allowExtractingCommonSubexpressions() {
        return false;
    }

    @Override
    public Expression typeCheck(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        this.hrefOp.typeCheck(visitor, contextInfo);
        RoleDiagnostic role = new RoleDiagnostic(4, "xsl:stream/href", 0);
        TypeChecker tc = visitor.getConfiguration().getTypeChecker(false);
        this.hrefOp.setChildExpression(tc.staticTypeCheck(this.hrefOp.getChildExpression(), SequenceType.SINGLE_STRING, role, visitor));
        ContextItemStaticInfo newType = this.getConfiguration().makeContextItemStaticInfo(NodeKindTest.DOCUMENT, false);
        newType.setContextPostureStriding();
        this.bodyOp.typeCheck(visitor, newType);
        return this;
    }

    @Override
    public Expression optimize(ExpressionVisitor visitor, ContextItemStaticInfo contextItemType) throws XPathException {
        ContextItemStaticInfo newType = this.getConfiguration().makeContextItemStaticInfo(NodeKindTest.DOCUMENT, false);
        newType.setContextPostureStriding();
        this.hrefOp.optimize(visitor, contextItemType);
        this.bodyOp.optimize(visitor, newType);
        return this;
    }

    @Override
    public boolean mayCreateNewNodes() {
        return !this.getBody().hasSpecialProperty(0x800000);
    }

    @Override
    public int computeDependencies() {
        int dependencies = 0;
        dependencies |= this.getHref().getDependencies();
        return dependencies |= this.getBody().getDependencies() & 0xFFFFFFE1;
    }

    @Override
    public int computeSpecialProperties() {
        Expression body = this.getBody();
        if ((body.getSpecialProperties() & 0x400000) != 0) {
            return 655360;
        }
        return super.computeSpecialProperties();
    }

    @Override
    public void export(ExpressionPresenter out) throws XPathException {
        SchemaType schemaType;
        ExpressionPresenter.ExportOptions options = (ExpressionPresenter.ExportOptions)out.getOptions();
        if ("JS".equals(options.target) && options.targetVersion == 1) {
            throw new XPathException("xsl:source-document is not supported in Saxon-JS 1.*", "SXJS0001");
        }
        out.startElement(this.getExportTag(), this);
        int validation = this.parseOptions.getSchemaValidationMode();
        if (validation != 4 && validation != 8) {
            out.emitAttribute("validation", validation + "");
        }
        if ((schemaType = this.parseOptions.getTopLevelType()) != null) {
            out.emitAttribute("schemaType", schemaType.getStructuredQName());
        }
        SpaceStrippingRule xsltStripSpace = this.getPackageData() instanceof StylesheetPackage ? ((StylesheetPackage)this.getPackageData()).getSpaceStrippingRule() : null;
        String flags = "";
        if (this.parseOptions.getSpaceStrippingRule() == xsltStripSpace) {
            flags = flags + "s";
        }
        if (this.parseOptions.isLineNumbering()) {
            flags = flags + "l";
        }
        if (this.parseOptions.isExpandAttributeDefaults()) {
            flags = flags + "a";
        }
        if (this.parseOptions.getDTDValidationMode() == 1) {
            flags = flags + "d";
        }
        if (this.parseOptions.isXIncludeAware()) {
            flags = flags + "i";
        }
        out.emitAttribute("flags", flags);
        if (this.accumulators != null && !this.accumulators.isEmpty()) {
            FastStringBuffer fsb = new FastStringBuffer(256);
            for (Accumulator accumulator : this.accumulators) {
                if (!fsb.isEmpty()) {
                    fsb.append(" ");
                }
                fsb.append(accumulator.getAccumulatorName().getEQName());
            }
            out.emitAttribute("accum", fsb.toString());
        }
        out.setChildRole("href");
        this.getHref().export(out);
        out.setChildRole("body");
        this.getBody().export(out);
        out.endElement();
    }

    @Override
    public Expression copy(RebindingMap rebindings) {
        SourceDocument exp = new SourceDocument(this.getHref().copy(rebindings), this.getBody().copy(rebindings), this.parseOptions);
        exp.setRetainedStaticContext(this.getRetainedStaticContext());
        ExpressionTool.copyLocationInfo(this, exp);
        return exp;
    }

    @Override
    public TailCall processLeavingTail(Outputter output, XPathContext context) throws XPathException {
        try {
            this.push(output, context);
        } catch (QuitParsingException quitParsingException) {
        } catch (XPathException e) {
            e.maybeSetLocation(this.getLocation());
            if (e.getErrorCodeQName() == null) {
                e.setErrorCode("FODC0002");
            }
            throw e;
        }
        return null;
    }

    public void push(Outputter output, XPathContext context) throws XPathException {
        String href = this.hrefOp.getChildExpression().evaluateAsString(context).toString();
        NodeInfo doc = DocumentFn.makeDoc(href, this.getStaticBaseURIString(), this.getPackageData(), this.parseOptions, context, this.getLocation(), false);
        if (doc != null) {
            Controller controller = context.getController();
            if (this.accumulators != null && controller instanceof XsltController) {
                ((XsltController)controller).getAccumulatorManager().setApplicableAccumulators(doc.getTreeInfo(), this.accumulators);
            }
            XPathContextMinor c2 = context.newMinorContext();
            c2.setCurrentIterator(new ManualIterator(doc));
            this.bodyOp.getChildExpression().process(output, c2);
        }
    }
}

