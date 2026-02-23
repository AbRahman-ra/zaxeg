/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.ma.json;

import java.util.Map;
import net.sf.saxon.event.DocumentValidator;
import net.sf.saxon.event.Outputter;
import net.sf.saxon.event.PipelineConfiguration;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.parser.Loc;
import net.sf.saxon.functions.OptionsParameter;
import net.sf.saxon.functions.PushableFunction;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.ma.json.JsonReceiver;
import net.sf.saxon.ma.map.MapItem;
import net.sf.saxon.om.Function;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.util.CharSequenceConsumer;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.type.FunctionItemType;
import net.sf.saxon.type.SpecificFunctionType;
import net.sf.saxon.value.BooleanValue;
import net.sf.saxon.value.EmptySequence;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;

public class XMLToJsonFn
extends SystemFunction
implements PushableFunction {
    private static final FunctionItemType formatterFunctionType = new SpecificFunctionType(new SequenceType[]{SequenceType.SINGLE_STRING}, SequenceType.SINGLE_STRING);

    public static OptionsParameter makeOptionsParameter() {
        OptionsParameter xmlToJsonOptions = new OptionsParameter();
        xmlToJsonOptions.addAllowedOption("indent", SequenceType.SINGLE_BOOLEAN, BooleanValue.FALSE);
        xmlToJsonOptions.addAllowedOption("number-formatter", SequenceType.makeSequenceType(formatterFunctionType, 24576), EmptySequence.getInstance());
        return xmlToJsonOptions;
    }

    @Override
    public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
        NodeInfo xml = (NodeInfo)arguments[0].head();
        if (xml == null) {
            return EmptySequence.getInstance();
        }
        Options options = this.getOptions(context, arguments);
        PipelineConfiguration pipe = context.getController().makePipelineConfiguration();
        pipe.setXPathContext(context);
        FastStringBuffer stringBuffer = new FastStringBuffer(2048);
        this.convertToJson(xml, stringBuffer, options, context);
        return new StringValue(stringBuffer.condense());
    }

    private Options getOptions(XPathContext context, Sequence[] arguments) throws XPathException {
        if (this.getArity() > 1) {
            MapItem suppliedOptions = (MapItem)arguments[1].head();
            Map<String, Sequence> options = this.getDetails().optionDetails.processSuppliedOptions(suppliedOptions, context);
            Options o = new Options();
            o.indent = ((BooleanValue)options.get("indent").head()).getBooleanValue();
            Sequence format = options.get("number-formatter");
            if (format != null) {
                o.numberFormatter = (Function)format.head();
            }
            return o;
        }
        return new Options();
    }

    @Override
    public void process(Outputter destination, XPathContext context, Sequence[] arguments) throws XPathException {
        NodeInfo xml = (NodeInfo)arguments[0].head();
        if (xml != null) {
            Options options = this.getOptions(context, arguments);
            PipelineConfiguration pipe = context.getController().makePipelineConfiguration();
            pipe.setXPathContext(context);
            this.convertToJson(xml, destination.getStringReceiver(false), options, context);
        }
    }

    private void convertToJson(NodeInfo xml, CharSequenceConsumer output, Options options, XPathContext context) throws XPathException {
        PipelineConfiguration pipe = context.getController().makePipelineConfiguration();
        pipe.setXPathContext(context);
        JsonReceiver receiver = new JsonReceiver(pipe, output);
        receiver.setIndenting(options.indent);
        if (options.numberFormatter != null) {
            receiver.setNumberFormatter(options.numberFormatter);
        }
        Receiver r = receiver;
        if (xml.getNodeKind() == 9) {
            r = new DocumentValidator(r, "FOJS0006");
        }
        r.open();
        xml.copy(r, 0, Loc.NONE);
        r.close();
    }

    @Override
    public String getStreamerName() {
        return "XmlToJsonFn";
    }

    private static class Options {
        public boolean indent;
        public Function numberFormatter;

        private Options() {
        }
    }
}

