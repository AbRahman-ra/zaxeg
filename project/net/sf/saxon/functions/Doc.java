/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions;

import net.sf.saxon.Configuration;
import net.sf.saxon.Controller;
import net.sf.saxon.expr.Callable;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.PackageData;
import net.sf.saxon.expr.StringLiteral;
import net.sf.saxon.expr.SystemFunctionCall;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.functions.DocumentFn;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.lib.Feature;
import net.sf.saxon.lib.ParseOptions;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.ZeroOrOne;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.trans.XsltController;
import net.sf.saxon.value.AtomicValue;

public class Doc
extends SystemFunction
implements Callable {
    private ParseOptions parseOptions;

    public ParseOptions getParseOptions() {
        return this.parseOptions;
    }

    public void setParseOptions(ParseOptions parseOptions) {
        this.parseOptions = parseOptions;
    }

    @Override
    public int getCardinality(Expression[] arguments) {
        return arguments[0].getCardinality() & 0xFFFF7FFF;
    }

    @Override
    public Expression makeFunctionCall(Expression ... arguments) {
        Expression expr = Doc.maybePreEvaluate(this, arguments);
        return expr == null ? super.makeFunctionCall(arguments) : expr;
    }

    public static Expression maybePreEvaluate(final SystemFunction sf, Expression[] arguments) {
        if (arguments.length > 1 || !sf.getRetainedStaticContext().getConfiguration().getBooleanProperty(Feature.PRE_EVALUATE_DOC_FUNCTION)) {
            sf.getDetails().properties |= 0x200;
            return null;
        }
        return new SystemFunctionCall(sf, arguments){

            @Override
            public Expression preEvaluate(ExpressionVisitor visitor) {
                Configuration config = visitor.getConfiguration();
                try {
                    GroundedValue firstArg = ((Literal)this.getArg(0)).getValue();
                    if (firstArg.getLength() == 0) {
                        return null;
                    }
                    if (firstArg.getLength() > 1) {
                        return this;
                    }
                    String href = firstArg.head().getStringValue();
                    if (href.indexOf(35) >= 0) {
                        return this;
                    }
                    NodeInfo item = DocumentFn.preLoadDoc(href, sf.getStaticBaseUriString(), config, this.getLocation());
                    if (item != null) {
                        Literal constant = Literal.makeLiteral(item);
                        ExpressionTool.copyLocationInfo(this.getArg(0), constant);
                        return constant;
                    }
                } catch (Exception err) {
                    return this;
                }
                return this;
            }

            @Override
            public Expression optimize(ExpressionVisitor visitor, ContextItemStaticInfo contextItemType) throws XPathException {
                this.optimizeChildren(visitor, contextItemType);
                if (this.getArg(0) instanceof StringLiteral) {
                    return this.preEvaluate(visitor);
                }
                return this;
            }
        };
    }

    @Override
    public ZeroOrOne call(XPathContext context, Sequence[] arguments) throws XPathException {
        AtomicValue hrefVal = (AtomicValue)arguments[0].head();
        if (hrefVal == null) {
            return ZeroOrOne.empty();
        }
        String href = hrefVal.getStringValue();
        PackageData packageData = this.getRetainedStaticContext().getPackageData();
        NodeInfo item = DocumentFn.makeDoc(href, this.getRetainedStaticContext().getStaticBaseUriString(), packageData, this.getParseOptions(), context, null, false);
        if (item == null) {
            throw new XPathException("Failed to load document " + href, "FODC0002", context);
        }
        Controller controller = context.getController();
        if (this.parseOptions != null && controller instanceof XsltController) {
            ((XsltController)controller).getAccumulatorManager().setApplicableAccumulators(item.getTreeInfo(), this.parseOptions.getApplicableAccumulators());
        }
        return new ZeroOrOne<NodeInfo>(item);
    }

    @Override
    public int getSpecialProperties(Expression[] arguments) {
        return 25821184;
    }
}

