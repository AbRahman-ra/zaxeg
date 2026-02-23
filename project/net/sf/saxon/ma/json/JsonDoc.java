/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.ma.json;

import java.io.IOException;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.Map;
import java.util.function.IntPredicate;
import net.sf.saxon.Configuration;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.functions.UnparsedTextFunction;
import net.sf.saxon.ma.json.ParseJsonFn;
import net.sf.saxon.ma.map.MapItem;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.trans.Err;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.EmptySequence;
import net.sf.saxon.z.IntSetPredicate;

public class JsonDoc
extends SystemFunction {
    @Override
    public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
        Map<String, Sequence> checkedOptions;
        CharSequence content;
        Reader reader;
        Item arg0 = arguments[0].head();
        if (arg0 == null) {
            return EmptySequence.getInstance();
        }
        String href = arg0.getStringValue();
        Configuration config = context.getConfiguration();
        IntPredicate checker = IntSetPredicate.ALWAYS_TRUE;
        URI absoluteURI = UnparsedTextFunction.getAbsoluteURI(href, this.getStaticBaseUriString(), context);
        String encoding = "UTF-8";
        try {
            reader = context.getController().getUnparsedTextURIResolver().resolve(absoluteURI, encoding, config);
        } catch (XPathException err) {
            err.maybeSetErrorCode("FOUT1170");
            throw err;
        }
        try {
            content = UnparsedTextFunction.readFile(checker, reader);
        } catch (UnsupportedEncodingException encErr) {
            XPathException e = new XPathException("Unknown encoding " + Err.wrap(encoding), encErr);
            e.setErrorCode("FOUT1190");
            throw e;
        } catch (IOException ioErr) {
            throw UnparsedTextFunction.handleIOError(absoluteURI, ioErr, context);
        }
        if (this.getArity() == 2) {
            MapItem options = (MapItem)arguments[1].head();
            checkedOptions = this.getDetails().optionDetails.processSuppliedOptions(options, context);
        } else {
            checkedOptions = ParseJsonFn.OPTION_DETAILS.getDefaultOptions();
        }
        Item result = ParseJsonFn.parse(content.toString(), checkedOptions, context);
        return result == null ? EmptySequence.getInstance() : result;
    }
}

