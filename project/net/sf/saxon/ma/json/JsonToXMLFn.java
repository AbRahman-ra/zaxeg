/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.ma.json;

import java.util.Map;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.functions.OptionsParameter;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.ma.json.JsonHandlerXML;
import net.sf.saxon.ma.json.JsonParser;
import net.sf.saxon.ma.map.MapItem;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.SpecificFunctionType;
import net.sf.saxon.value.BooleanValue;
import net.sf.saxon.value.EmptySequence;
import net.sf.saxon.value.SequenceType;

public class JsonToXMLFn
extends SystemFunction {
    public static OptionsParameter OPTION_DETAILS;

    @Override
    public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
        Item result;
        Item arg0 = arguments[0].head();
        if (arg0 == null) {
            return EmptySequence.getInstance();
        }
        String input = arg0.getStringValue();
        MapItem options = null;
        if (this.getArity() == 2) {
            options = (MapItem)arguments[1].head();
        }
        return (result = this.eval(input, options, context)) == null ? EmptySequence.getInstance() : result;
    }

    protected Item eval(String input, MapItem options, XPathContext context) throws XPathException {
        JsonParser parser = new JsonParser();
        int flags = 0;
        Map<String, Sequence> checkedOptions = null;
        if (options != null) {
            checkedOptions = this.getDetails().optionDetails.processSuppliedOptions(options, context);
            flags = JsonParser.getFlags(checkedOptions, context, true);
            if ((flags & 0x40) != 0) {
                throw new XPathException("json-to-xml: duplicates=use-last is not allowed", "FOJS0005");
            }
            if ((flags & 0x1E0) == 0) {
                flags = (flags & 8) != 0 ? (flags |= 0x100) : (flags |= 0x20);
            }
        } else {
            flags = 32;
        }
        JsonHandlerXML handler = new JsonHandlerXML(context, this.getStaticBaseUriString(), flags);
        if (options != null) {
            handler.setFallbackFunction(checkedOptions, context);
        }
        parser.parse(input, flags, handler, context);
        return handler.getResult();
    }

    static {
        SpecificFunctionType fallbackType = new SpecificFunctionType(new SequenceType[]{SequenceType.SINGLE_STRING}, SequenceType.SINGLE_STRING);
        OptionsParameter jsonToXmlOptions = new OptionsParameter();
        jsonToXmlOptions.addAllowedOption("liberal", SequenceType.SINGLE_BOOLEAN, BooleanValue.FALSE);
        jsonToXmlOptions.addAllowedOption("duplicates", SequenceType.SINGLE_STRING, null);
        jsonToXmlOptions.setAllowedValues("duplicates", "FOJS0005", "reject", "use-first", "retain");
        jsonToXmlOptions.addAllowedOption("validate", SequenceType.SINGLE_BOOLEAN, BooleanValue.FALSE);
        jsonToXmlOptions.addAllowedOption("escape", SequenceType.SINGLE_BOOLEAN, BooleanValue.FALSE);
        jsonToXmlOptions.addAllowedOption("fallback", SequenceType.makeSequenceType(fallbackType, 16384), null);
        OPTION_DETAILS = jsonToXmlOptions;
    }
}

