/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.serialize.charcode;

import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import net.sf.saxon.serialize.charcode.ASCIICharacterSet;
import net.sf.saxon.serialize.charcode.CharacterSet;
import net.sf.saxon.serialize.charcode.ISO88591CharacterSet;
import net.sf.saxon.serialize.charcode.JavaCharacterSet;
import net.sf.saxon.serialize.charcode.UTF16CharacterSet;
import net.sf.saxon.serialize.charcode.UTF8CharacterSet;
import net.sf.saxon.trans.XPathException;

public class CharacterSetFactory {
    private HashMap<String, CharacterSet> characterSets;

    public CharacterSetFactory() {
        HashMap<String, CharacterSet> c = this.characterSets = new HashMap(10);
        UTF8CharacterSet utf8 = UTF8CharacterSet.getInstance();
        c.put("utf8", utf8);
        UTF16CharacterSet utf16 = UTF16CharacterSet.getInstance();
        c.put("utf16", utf16);
        ASCIICharacterSet acs = ASCIICharacterSet.getInstance();
        c.put("ascii", acs);
        c.put("iso646", acs);
        c.put("usascii", acs);
        ISO88591CharacterSet lcs = ISO88591CharacterSet.getInstance();
        c.put("iso88591", lcs);
    }

    public void setCharacterSetImplementation(String encoding, CharacterSet charSet) {
        this.characterSets.put(CharacterSetFactory.normalizeCharsetName(encoding), charSet);
    }

    private static String normalizeCharsetName(String name) {
        return name.replace("-", "").replace("_", "").toLowerCase();
    }

    public CharacterSet getCharacterSet(Properties details) throws XPathException {
        String encoding = details.getProperty("encoding");
        if (encoding == null) {
            return UTF8CharacterSet.getInstance();
        }
        return this.getCharacterSet(encoding);
    }

    public CharacterSet getCharacterSet(String encoding) throws XPathException {
        if (encoding == null) {
            return UTF8CharacterSet.getInstance();
        }
        String encodingKey = CharacterSetFactory.normalizeCharsetName(encoding);
        CharacterSet cs = this.characterSets.get(encodingKey);
        if (cs != null) {
            return cs;
        }
        try {
            Charset charset = Charset.forName(encoding);
            JavaCharacterSet res = JavaCharacterSet.makeCharSet(charset);
            this.characterSets.put(encodingKey, res);
            return res;
        } catch (IllegalCharsetNameException err) {
            XPathException e = new XPathException("Invalid encoding name: " + encoding);
            e.setErrorCode("SESU0007");
            throw e;
        } catch (UnsupportedCharsetException err) {
            XPathException e = new XPathException("Unknown encoding requested: " + encoding);
            e.setErrorCode("SESU0007");
            throw e;
        }
    }

    public static void main(String[] args) throws Exception {
        System.err.println("Available Character Sets in the java.nio package for this Java VM:");
        for (String s : Charset.availableCharsets().keySet()) {
            System.err.println("    " + s);
        }
        System.err.println("Registered Character Sets in Saxon:");
        CharacterSetFactory factory = new CharacterSetFactory();
        for (Map.Entry<String, CharacterSet> e : factory.characterSets.entrySet()) {
            System.err.println("    " + e.getKey() + " = " + e.getValue().getClass().getName());
        }
    }
}

