/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.serialize;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;
import net.sf.saxon.event.ProxyReceiver;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.trans.Err;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.tiny.CharSlice;
import net.sf.saxon.value.Base64BinaryValue;
import net.sf.saxon.value.HexBinaryValue;

public class BinaryTextDecoder
extends ProxyReceiver {
    String outputEncoding = "utf8";

    public BinaryTextDecoder(Receiver next, Properties details) throws XPathException {
        super(next);
        this.setOutputProperties(details);
    }

    public void setOutputProperties(Properties details) throws XPathException {
        this.outputEncoding = details.getProperty("encoding", "utf8");
    }

    @Override
    public void processingInstruction(String name, CharSequence value, Location locationId, int properties) throws XPathException {
        String encoding;
        byte[] bytes = null;
        int dot = name.indexOf(46);
        if (dot >= 0 && dot != name.length() - 1) {
            encoding = name.substring(dot + 1);
            name = name.substring(0, dot);
        } else {
            encoding = this.outputEncoding;
        }
        if (name.equals("hex")) {
            bytes = new HexBinaryValue(value).getBinaryValue();
        } else if (name.equals("b64")) {
            bytes = new Base64BinaryValue(value).getBinaryValue();
        }
        if (bytes != null) {
            try {
                ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
                InputStreamReader reader = new InputStreamReader((InputStream)stream, encoding);
                char[] array = new char[bytes.length];
                int used = reader.read(array, 0, array.length);
                this.nextReceiver.characters(new CharSlice(array, 0, used), locationId, properties);
            } catch (IOException e) {
                throw new XPathException("Text output method: failed to decode binary data " + Err.wrap(value.toString(), 4));
            }
        }
    }
}

