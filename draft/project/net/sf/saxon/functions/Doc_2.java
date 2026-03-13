/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Map;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import net.sf.saxon.Configuration;
import net.sf.saxon.Controller;
import net.sf.saxon.event.Builder;
import net.sf.saxon.event.Sender;
import net.sf.saxon.expr.Callable;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.PackageData;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.accum.Accumulator;
import net.sf.saxon.expr.accum.AccumulatorRegistry;
import net.sf.saxon.expr.parser.RetainedStaticContext;
import net.sf.saxon.functions.OptionsParameter;
import net.sf.saxon.functions.ResolveURI;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.lib.ParseOptions;
import net.sf.saxon.lib.Validation;
import net.sf.saxon.ma.map.MapItem;
import net.sf.saxon.om.AllElementsSpaceStrippingRule;
import net.sf.saxon.om.IgnorableSpaceStrippingRule;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NoElementsSpaceStrippingRule;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.TreeInfo;
import net.sf.saxon.om.ZeroOrOne;
import net.sf.saxon.style.StylesheetPackage;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.trans.XsltController;
import net.sf.saxon.tree.tiny.TinyBuilder;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.BooleanValue;
import net.sf.saxon.value.QNameValue;
import net.sf.saxon.value.SequenceType;

public class Doc_2
extends SystemFunction
implements Callable {
    public static OptionsParameter makeOptionsParameter() {
        SequenceType listOfQNames = SequenceType.makeSequenceType(BuiltInAtomicType.QNAME, 57344);
        OptionsParameter op = new OptionsParameter();
        op.addAllowedOption("base-uri", SequenceType.SINGLE_STRING);
        op.addAllowedOption("validation", SequenceType.SINGLE_STRING);
        op.setAllowedValues("validation", "SXZZ0001", "strict", "lax", "preserve", "strip", "skip");
        op.addAllowedOption("type", SequenceType.SINGLE_QNAME);
        op.addAllowedOption("strip-space", SequenceType.SINGLE_STRING);
        op.setAllowedValues("strip-space", "SXZZ0001", "none", "all", "ignorable", "package-defined", "default");
        op.addAllowedOption("stable", SequenceType.SINGLE_BOOLEAN);
        op.addAllowedOption("dtd-validation", SequenceType.SINGLE_BOOLEAN);
        op.addAllowedOption("accumulators", listOfQNames);
        op.addAllowedOption("use-xsi-schema-location", SequenceType.SINGLE_BOOLEAN);
        return op;
    }

    public static ParseOptions setParseOptions(RetainedStaticContext rsc, Map<String, Sequence> checkedOptions, XPathContext context) throws XPathException {
        ParseOptions result = new ParseOptions(context.getConfiguration().getParseOptions());
        Sequence value = checkedOptions.get("validation");
        if (value != null) {
            int v;
            String valStr = value.head().getStringValue();
            if ("skip".equals(valStr)) {
                valStr = "strip";
            }
            if ((v = Validation.getCode(valStr)) == -1) {
                throw new XPathException("Invalid validation value " + valStr, "SXZZ0002");
            }
            result.setSchemaValidationMode(v);
        }
        if ((value = checkedOptions.get("type")) != null) {
            QNameValue qval = (QNameValue)value.head();
            result.setTopLevelType(context.getConfiguration().getSchemaType(qval.getStructuredQName()));
            result.setSchemaValidationMode(8);
        }
        if ((value = checkedOptions.get("strip-space")) != null) {
            String s;
            switch (s = value.head().getStringValue()) {
                case "all": {
                    result.setSpaceStrippingRule(AllElementsSpaceStrippingRule.getInstance());
                    break;
                }
                case "none": {
                    result.setSpaceStrippingRule(NoElementsSpaceStrippingRule.getInstance());
                    break;
                }
                case "ignorable": {
                    result.setSpaceStrippingRule(IgnorableSpaceStrippingRule.getInstance());
                    break;
                }
                case "package-defined": 
                case "default": {
                    PackageData data = rsc.getPackageData();
                    if (!(data instanceof StylesheetPackage)) break;
                    result.setSpaceStrippingRule(((StylesheetPackage)data).getSpaceStrippingRule());
                }
            }
        }
        if ((value = checkedOptions.get("dtd-validation")) != null) {
            result.setDTDValidationMode(((BooleanValue)value.head()).getBooleanValue() ? 1 : 4);
        }
        if ((value = checkedOptions.get("accumulators")) != null) {
            Item it;
            AccumulatorRegistry reg = rsc.getPackageData().getAccumulatorRegistry();
            HashSet<Accumulator> accumulators = new HashSet<Accumulator>();
            SequenceIterator iter = value.iterate();
            while ((it = iter.next()) != null) {
                QNameValue name = (QNameValue)it;
                Accumulator acc = reg.getAccumulator(name.getStructuredQName());
                accumulators.add(acc);
            }
            result.setApplicableAccumulators(accumulators);
        }
        if ((value = checkedOptions.get("use-xsi-schema-location")) != null) {
            result.setUseXsiSchemaLocation(((BooleanValue)value.head()).getBooleanValue());
        }
        return result;
    }

    @Override
    public ZeroOrOne<NodeInfo> call(XPathContext context, Sequence[] arguments) throws XPathException {
        AtomicValue hrefVal = (AtomicValue)arguments[0].head();
        if (hrefVal == null) {
            return ZeroOrOne.empty();
        }
        String href = hrefVal.getStringValue();
        Item param = arguments[1].head();
        Map<String, Sequence> checkedOptions = this.getDetails().optionDetails.processSuppliedOptions((MapItem)param, context);
        ParseOptions parseOptions = Doc_2.setParseOptions(this.getRetainedStaticContext(), checkedOptions, context);
        NodeInfo item = this.fetch(href, parseOptions, context).getRootNode();
        if (item == null) {
            throw new XPathException("Failed to load document " + href, "FODC0002", context);
        }
        Controller controller = context.getController();
        if (controller instanceof XsltController) {
            ((XsltController)controller).getAccumulatorManager().setApplicableAccumulators(item.getTreeInfo(), parseOptions.getApplicableAccumulators());
        }
        return new ZeroOrOne<NodeInfo>(item);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private TreeInfo fetch(String href, ParseOptions options, XPathContext context) throws XPathException {
        TreeInfo newdoc;
        URI abs;
        Configuration config = context.getConfiguration();
        Controller controller = context.getController();
        try {
            abs = ResolveURI.makeAbsolute(href, this.getStaticBaseUriString());
        } catch (URISyntaxException e) {
            throw new XPathException("Invalid URI supplied to saxon:doc - " + e.getMessage(), "FODC0002");
        }
        Source source = config.getSourceResolver().resolveSource(new StreamSource(abs.toASCIIString()), config);
        if (source instanceof NodeInfo || source instanceof DOMSource) {
            NodeInfo startNode = controller.prepareInputTree(source);
            newdoc = startNode.getTreeInfo();
        } else {
            Builder b = controller.makeBuilder();
            if (b instanceof TinyBuilder) {
                ((TinyBuilder)b).setStatistics(config.getTreeStatistics().SOURCE_DOCUMENT_STATISTICS);
            }
            b.setPipelineConfiguration(b.getPipelineConfiguration());
            try {
                Sender.send(source, b, options);
                newdoc = b.getCurrentRoot().getTreeInfo();
                b.reset();
            } finally {
                if (options.isPleaseCloseAfterUse()) {
                    ParseOptions.close(source);
                }
            }
        }
        return newdoc;
    }

    @Override
    public int getSpecialProperties(Expression[] arguments) {
        return 25821184;
    }
}

