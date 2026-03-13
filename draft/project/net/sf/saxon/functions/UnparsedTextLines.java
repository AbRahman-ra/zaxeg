/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions;

import java.io.LineNumberReader;
import java.io.StringReader;
import java.net.URI;
import net.sf.saxon.expr.Callable;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.functions.UnparsedText;
import net.sf.saxon.functions.UnparsedTextFunction;
import net.sf.saxon.lib.Feature;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.SequenceTool;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.EmptyIterator;
import net.sf.saxon.tree.iter.UnparsedTextIterator;
import net.sf.saxon.value.StringValue;

public class UnparsedTextLines
extends UnparsedTextFunction
implements Callable {
    @Override
    public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
        StringValue hrefVal = (StringValue)arguments[0].head();
        String encoding = this.getArity() == 2 ? arguments[1].head().getStringValue() : null;
        try {
            return SequenceTool.toLazySequence(this.evalUnparsedTextLines(hrefVal, encoding, context));
        } catch (XPathException e) {
            if (this.getArity() == 2 && e.getErrorCodeLocalPart().equals("FOUT1200")) {
                e.setErrorCode("FOUT1190");
            }
            throw e;
        }
    }

    private SequenceIterator evalUnparsedTextLines(StringValue hrefVal, String encoding, XPathContext context) throws XPathException {
        if (hrefVal == null) {
            return EmptyIterator.ofAtomic();
        }
        String href = hrefVal.getStringValue();
        boolean stable = context.getConfiguration().getBooleanProperty(Feature.STABLE_UNPARSED_TEXT);
        if (stable) {
            StringValue content = UnparsedText.evalUnparsedText(hrefVal, this.getStaticBaseUriString(), encoding, context);
            assert (content != null);
            URI abs = UnparsedTextFunction.getAbsoluteURI(href, this.getStaticBaseUriString(), context);
            LineNumberReader reader = new LineNumberReader(new StringReader(content.getStringValue()));
            return new UnparsedTextIterator(reader, abs, context, encoding);
        }
        URI absoluteURI = UnparsedTextFunction.getAbsoluteURI(href, this.getRetainedStaticContext().getStaticBaseUriString(), context);
        return new UnparsedTextIterator(absoluteURI, context, encoding, null);
    }
}

