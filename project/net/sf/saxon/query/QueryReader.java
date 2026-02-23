/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.query;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.function.IntPredicate;
import javax.xml.transform.stream.StreamSource;
import net.sf.saxon.functions.UnparsedTextFunction;
import net.sf.saxon.trans.Err;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.value.Whitespace;

public class QueryReader {
    private QueryReader() {
    }

    public static String readSourceQuery(StreamSource ss, IntPredicate charChecker) throws XPathException {
        String queryText;
        if (ss.getInputStream() != null) {
            InputStream is = ss.getInputStream();
            if (!is.markSupported()) {
                is = new BufferedInputStream(is);
            }
            String encoding = QueryReader.readEncoding(is);
            queryText = QueryReader.readInputStream(is, encoding, charChecker);
        } else if (ss.getReader() != null) {
            queryText = QueryReader.readQueryFromReader(ss.getReader(), charChecker);
        } else {
            throw new XPathException("Module URI Resolver must supply either an InputStream or a Reader");
        }
        return queryText.toString();
    }

    public static String readEncoding(InputStream is) throws XPathException {
        try {
            if (!is.markSupported()) {
                throw new IllegalArgumentException("InputStream must have markSupported() = true");
            }
            is.mark(100);
            byte[] start = new byte[100];
            int read = is.read(start, 0, 100);
            if (read == -1) {
                throw new XPathException("Query source file is empty");
            }
            is.reset();
            return QueryReader.inferEncoding(start, read);
        } catch (IOException e) {
            throw new XPathException("Failed to read query source file", e);
        }
    }

    public static String readInputStream(InputStream is, String encoding, IntPredicate nameChecker) throws XPathException {
        if (encoding == null) {
            if (!is.markSupported()) {
                is = new BufferedInputStream(is);
            }
            encoding = QueryReader.readEncoding(is);
        }
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, encoding));
            return QueryReader.readQueryFromReader(reader, nameChecker);
        } catch (UnsupportedEncodingException encErr) {
            XPathException err = new XPathException("Unknown encoding " + Err.wrap(encoding), encErr);
            err.setErrorCode("XQST0087");
            throw err;
        }
    }

    private static String readQueryFromReader(Reader reader, IntPredicate charChecker) throws XPathException {
        try {
            CharSequence content = UnparsedTextFunction.readFile(charChecker, reader);
            return content.toString();
        } catch (XPathException err) {
            err.setErrorCode("XPST0003");
            err.setIsStaticError(true);
            throw err;
        } catch (IOException ioErr) {
            throw new XPathException("Failed to read supplied query file", ioErr);
        }
    }

    private static String inferEncoding(byte[] start, int read) throws XPathException {
        if (read >= 2) {
            if (QueryReader.ch(start[0]) == 254 && QueryReader.ch(start[1]) == 255) {
                return "UTF-16";
            }
            if (QueryReader.ch(start[0]) == 255 && QueryReader.ch(start[1]) == 254) {
                return "UTF-16LE";
            }
        }
        if (read >= 3 && QueryReader.ch(start[0]) == 239 && QueryReader.ch(start[1]) == 187 && QueryReader.ch(start[2]) == 191) {
            return "UTF-8";
        }
        if (read >= 8 && start[0] == 0 && start[2] == 0 && start[4] == 0 && start[6] == 0) {
            return "UTF-16";
        }
        if (read >= 8 && start[1] == 0 && start[3] == 0 && start[5] == 0 && start[7] == 0) {
            return "UTF-16LE";
        }
        int i = 0;
        String tok = QueryReader.readToken(start, i, read);
        if (Whitespace.trim(tok).equals("xquery")) {
            i += tok.length();
        } else {
            return "UTF-8";
        }
        tok = QueryReader.readToken(start, i, read);
        if (Whitespace.trim(tok).equals("encoding")) {
            i += tok.length();
        } else {
            if (Whitespace.trim(tok).equals("version")) {
                i += tok.length();
            } else {
                return "UTF-8";
            }
            tok = QueryReader.readToken(start, i, read);
            i += tok.length();
            tok = QueryReader.readToken(start, i, read);
            if (Whitespace.trim(tok).equals("encoding")) {
                i += tok.length();
            } else {
                return "UTF-8";
            }
        }
        tok = Whitespace.trim(QueryReader.readToken(start, i, read));
        if (tok.startsWith("\"") && tok.endsWith("\"") && tok.length() > 2) {
            return tok.substring(1, tok.length() - 1);
        }
        if (tok.startsWith("'") && tok.endsWith("'") && tok.length() > 2) {
            return tok.substring(1, tok.length() - 1);
        }
        throw new XPathException("Unrecognized encoding " + Err.wrap(tok) + " in query prolog");
    }

    private static String readToken(byte[] in, int i, int len) {
        int p;
        for (p = i; p < len && " \n\r\t".indexOf(QueryReader.ch(in[p])) >= 0; ++p) {
        }
        if (QueryReader.ch(in[p]) == 34) {
            ++p;
            while (p < len && QueryReader.ch(in[p]) != 34) {
                ++p;
            }
        } else if (QueryReader.ch(in[p]) == 39) {
            ++p;
            while (p < len && QueryReader.ch(in[p]) != 39) {
                ++p;
            }
        } else {
            while (p < len && " \n\r\t".indexOf(QueryReader.ch(in[p])) < 0) {
                ++p;
            }
        }
        if (p >= len) {
            return new String(in, i, len - i);
        }
        FastStringBuffer sb = new FastStringBuffer(p - i + 1);
        for (int c = i; c <= p; ++c) {
            sb.cat((char)QueryReader.ch(in[c]));
        }
        return sb.toString();
    }

    private static int ch(byte b) {
        return b & 0xFF;
    }
}

