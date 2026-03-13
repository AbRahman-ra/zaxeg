/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import net.sf.saxon.Controller;
import net.sf.saxon.event.Outputter;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.parser.Loc;
import net.sf.saxon.functions.PushableFunction;
import net.sf.saxon.functions.UnparsedTextFunction;
import net.sf.saxon.lib.Feature;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.ZeroOrOne;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.util.CharSequenceConsumer;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.value.StringValue;

public class UnparsedText
extends UnparsedTextFunction
implements PushableFunction {
    private static final String errorValue = "\u0000";

    @Override
    public ZeroOrOne<StringValue> call(XPathContext context, Sequence[] arguments) throws XPathException {
        StringValue hrefVal = (StringValue)arguments[0].head();
        String encoding = this.getArity() == 2 ? arguments[1].head().getStringValue() : null;
        try {
            return new ZeroOrOne<StringValue>(UnparsedText.evalUnparsedText(hrefVal, this.getStaticBaseUriString(), encoding, context));
        } catch (XPathException e) {
            if (this.getArity() == 2 && e.getErrorCodeLocalPart().equals("FOUT1200")) {
                e.setErrorCode("FOUT1190");
            }
            throw e;
        }
    }

    @Override
    public void process(Outputter destination, XPathContext context, Sequence[] arguments) throws XPathException {
        boolean stable = context.getConfiguration().getBooleanProperty(Feature.STABLE_UNPARSED_TEXT);
        if (stable) {
            Sequence result = this.call(context, arguments);
            StringValue value = (StringValue)((ZeroOrOne)result).head();
            if (value != null) {
                destination.append(value, Loc.NONE, 0);
            }
        } else {
            StringValue href = (StringValue)arguments[0].head();
            URI absoluteURI = UnparsedText.getAbsoluteURI(href.getStringValue(), this.getStaticBaseUriString(), context);
            String encoding = this.getArity() == 2 ? arguments[1].head().getStringValue() : null;
            CharSequenceConsumer consumer = destination.getStringReceiver(false);
            consumer.open();
            try {
                UnparsedText.readFile(absoluteURI, encoding, consumer, context);
                consumer.close();
            } catch (XPathException e) {
                if (this.getArity() == 2 && e.getErrorCodeLocalPart().equals("FOUT1200")) {
                    e.setErrorCode("FOUT1190");
                }
                throw e;
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static StringValue evalUnparsedText(StringValue hrefVal, String base, String encoding, XPathContext context) throws XPathException {
        StringValue result;
        boolean stable = context.getConfiguration().getBooleanProperty(Feature.STABLE_UNPARSED_TEXT);
        try {
            CharSequence content;
            if (hrefVal == null) {
                return null;
            }
            String href = hrefVal.getStringValue();
            URI absoluteURI = UnparsedText.getAbsoluteURI(href, base, context);
            if (stable) {
                Controller controller;
                Controller controller2 = controller = context.getController();
                synchronized (controller2) {
                    String existing;
                    HashMap<URI, String> cache = (HashMap<URI, String>)controller.getUserData("unparsed-text-cache", "");
                    if (cache != null && (existing = (String)cache.get(absoluteURI)) != null) {
                        if (existing.startsWith(errorValue)) {
                            throw new XPathException(existing.substring(1), "FOUT1170");
                        }
                        return new StringValue(existing);
                    }
                    XPathException error = null;
                    try {
                        StringValue.Builder consumer = new StringValue.Builder();
                        UnparsedText.readFile(absoluteURI, encoding, consumer, context);
                        content = consumer.getStringValue().getStringValueCS();
                    } catch (XPathException e) {
                        error = e;
                        content = errorValue + e.getMessage();
                    }
                    if (cache == null) {
                        cache = new HashMap<URI, String>();
                        controller.setUserData("unparsed-text-cache", "", cache);
                        cache.put(absoluteURI, content.toString());
                    }
                    if (error != null) {
                        throw error;
                    }
                }
            }
            StringValue.Builder consumer = new StringValue.Builder();
            UnparsedText.readFile(absoluteURI, encoding, consumer, context);
            return consumer.getStringValue();
            result = new StringValue(content);
        } catch (XPathException err) {
            err.maybeSetErrorCode("FOUT1170");
            throw err;
        }
        return result;
    }

    public static void main(String[] args) throws Exception {
        FastStringBuffer sb1 = new FastStringBuffer(256);
        FastStringBuffer sb2 = new FastStringBuffer(256);
        File file = new File(args[0]);
        FileInputStream is = new FileInputStream(file);
        while (true) {
            int b;
            if ((b = ((InputStream)is).read()) < 0) break;
            sb1.append(Integer.toHexString(b) + " ");
            sb2.append((char)b + " ");
            if (sb1.length() <= 80) continue;
            System.out.println(sb1);
            System.out.println(sb2);
            sb1 = new FastStringBuffer(256);
            sb2 = new FastStringBuffer(256);
        }
        System.out.println(sb1);
        System.out.println(sb2);
        ((InputStream)is).close();
    }
}

