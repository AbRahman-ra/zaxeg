/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.ma.json;

import java.util.Map;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.functions.OptionsParameter;
import net.sf.saxon.ma.json.JsonHandlerMap;
import net.sf.saxon.ma.json.JsonParser;
import net.sf.saxon.ma.json.JsonToXMLFn;
import net.sf.saxon.ma.map.MapItem;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.SpecificFunctionType;
import net.sf.saxon.value.BooleanValue;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;

public class ParseJsonFn
extends JsonToXMLFn {
    public static OptionsParameter OPTION_DETAILS;

    @Override
    protected Item eval(String input, MapItem options, XPathContext context) throws XPathException {
        Map<String, Sequence> checkedOptions = null;
        if (options != null) {
            checkedOptions = this.getDetails().optionDetails.processSuppliedOptions(options, context);
        }
        return ParseJsonFn.parse(input, checkedOptions, context);
    }

    public static Item parse(String input, Map<String, Sequence> options, XPathContext context) throws XPathException {
        JsonParser parser = new JsonParser();
        int flags = 0;
        if (options != null) {
            flags = JsonParser.getFlags(options, context, false);
        }
        JsonHandlerMap handler = new JsonHandlerMap(context, flags);
        if ((flags & 0x20) != 0) {
            throw new XPathException("parse-json: duplicates=retain is not allowed", "FOJS0005");
        }
        if ((flags & 0x1E0) == 0) {
            flags |= 0x80;
        }
        if (options != null) {
            handler.setFallbackFunction(options, context);
        }
        parser.parse(input, flags, handler, context);
        return handler.getResult().head();
    }

    static {
        SpecificFunctionType fallbackType = new SpecificFunctionType(new SequenceType[]{SequenceType.SINGLE_STRING}, SequenceType.SINGLE_STRING);
        OptionsParameter parseJsonOptions = new OptionsParameter();
        parseJsonOptions.addAllowedOption("liberal", SequenceType.SINGLE_BOOLEAN, BooleanValue.FALSE);
        parseJsonOptions.addAllowedOption("duplicates", SequenceType.SINGLE_STRING, new StringValue("use-first"));
        parseJsonOptions.setAllowedValues("duplicates", "FOJS0005", "reject", "use-first", "use-last");
        parseJsonOptions.addAllowedOption("escape", SequenceType.SINGLE_BOOLEAN, BooleanValue.FALSE);
        parseJsonOptions.addAllowedOption("fallback", SequenceType.makeSequenceType(fallbackType, 16384), null);
        OPTION_DETAILS = parseJsonOptions;
    }
}

