/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions;

import java.io.IOException;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.MalformedInputException;
import java.nio.charset.UnmappableCharacterException;
import java.util.function.IntPredicate;
import net.sf.saxon.Configuration;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.functions.EncodeForUri;
import net.sf.saxon.functions.ResolveURI;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.lib.Feature;
import net.sf.saxon.serialize.charcode.UTF16CharacterSet;
import net.sf.saxon.trans.Err;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.tiny.CharSlice;
import net.sf.saxon.tree.util.CharSequenceConsumer;
import net.sf.saxon.tree.util.FastStringBuffer;

public abstract class UnparsedTextFunction
extends SystemFunction {
    @Override
    public int getSpecialProperties(Expression[] arguments) {
        int p = super.getSpecialProperties(arguments);
        if (this.getRetainedStaticContext().getConfiguration().getBooleanProperty(Feature.STABLE_UNPARSED_TEXT)) {
            return p;
        }
        return p & 0xFF7FFFFF;
    }

    public static void readFile(URI absoluteURI, String encoding, CharSequenceConsumer output, XPathContext context) throws XPathException {
        Reader reader;
        Configuration config = context.getConfiguration();
        IntPredicate checker = config.getValidCharacterChecker();
        try {
            reader = context.getController().getUnparsedTextURIResolver().resolve(absoluteURI, encoding, config);
        } catch (XPathException err) {
            err.maybeSetErrorCode("FOUT1170");
            throw err;
        }
        try {
            UnparsedTextFunction.readFile(checker, reader, output);
        } catch (UnsupportedEncodingException encErr) {
            XPathException e = new XPathException("Unknown encoding " + Err.wrap(encoding), encErr);
            e.setErrorCode("FOUT1190");
            throw e;
        } catch (IOException ioErr) {
            throw UnparsedTextFunction.handleIOError(absoluteURI, ioErr, context);
        }
    }

    public static URI getAbsoluteURI(String href, String baseURI, XPathContext context) throws XPathException {
        URI absoluteURI;
        try {
            absoluteURI = ResolveURI.makeAbsolute(href, baseURI);
        } catch (URISyntaxException err) {
            XPathException e = new XPathException(err.getReason() + ": " + err.getInput(), err);
            e.setErrorCode("FOUT1170");
            throw e;
        }
        if (absoluteURI.getFragment() != null) {
            XPathException e = new XPathException("URI for unparsed-text() must not contain a fragment identifier");
            e.setErrorCode("FOUT1170");
            throw e;
        }
        EncodeForUri.checkPercentEncoding(absoluteURI.toString());
        return absoluteURI;
    }

    public static XPathException handleIOError(URI absoluteURI, IOException ioErr, XPathContext context) {
        String message = "Failed to read input file";
        if (absoluteURI != null && !ioErr.getMessage().equals(absoluteURI.toString())) {
            message = message + ' ' + absoluteURI.toString();
        }
        message = message + " (" + ioErr.getClass().getName() + ')';
        XPathException e = new XPathException(message, ioErr);
        String errorCode = UnparsedTextFunction.getErrorCode(ioErr);
        e.setErrorCode(errorCode);
        return e;
    }

    private static String getErrorCode(IOException ioErr) {
        if (ioErr instanceof MalformedInputException) {
            return "FOUT1200";
        }
        if (ioErr instanceof UnmappableCharacterException) {
            return "FOUT1200";
        }
        if (ioErr instanceof CharacterCodingException) {
            return "FOUT1200";
        }
        return "FOUT1170";
    }

    public static CharSequence readFile(IntPredicate checker, Reader reader) throws IOException, XPathException {
        final FastStringBuffer buffer = new FastStringBuffer(2048);
        UnparsedTextFunction.readFile(checker, reader, new CharSequenceConsumer(){

            @Override
            public CharSequenceConsumer cat(CharSequence chars) {
                return buffer.cat(chars);
            }

            @Override
            public CharSequenceConsumer cat(char c) {
                return buffer.cat(c);
            }
        });
        return buffer.condense();
    }

    public static void readFile(IntPredicate checker, Reader reader, CharSequenceConsumer output) throws IOException, XPathException {
        int actual;
        char[] buffer = new char[2048];
        boolean first = true;
        int line = 1;
        int column = 1;
        boolean latin = true;
        while ((actual = reader.read(buffer, 0, buffer.length)) >= 0) {
            int c = 0;
            while (c < actual) {
                int ch32;
                if ((ch32 = buffer[c++]) == 10) {
                    ++line;
                    column = 0;
                }
                ++column;
                if (ch32 > 255) {
                    latin = false;
                    if (UTF16CharacterSet.isHighSurrogate(ch32)) {
                        if (c == actual) {
                            char[] buffer2 = new char[2048];
                            int actual2 = reader.read(buffer2, 0, 2048);
                            char[] buffer3 = new char[actual + actual2];
                            System.arraycopy(buffer, 0, buffer3, 0, actual);
                            System.arraycopy(buffer2, 0, buffer3, actual, actual2);
                            buffer = buffer3;
                            actual += actual2;
                        }
                        char low = buffer[c++];
                        ch32 = UTF16CharacterSet.combinePair((char)ch32, low);
                    }
                }
                if (checker.test(ch32)) continue;
                XPathException err = new XPathException("The text file contains a character that is illegal in XML (line=" + line + " column=" + column + " value=hex " + Integer.toHexString(ch32) + ')');
                err.setErrorCode("FOUT1190");
                throw err;
            }
            if (first) {
                first = false;
                if (buffer[0] == '\ufeff') {
                    output.cat(new CharSlice(buffer, 1, actual - 1));
                    continue;
                }
                output.cat(new CharSlice(buffer, 0, actual));
                continue;
            }
            output.cat(new CharSlice(buffer, 0, actual));
        }
        reader.close();
    }
}

