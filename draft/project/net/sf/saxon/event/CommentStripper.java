/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.event;

import net.sf.saxon.event.ProxyReceiver;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.expr.parser.Loc;
import net.sf.saxon.om.AttributeMap;
import net.sf.saxon.om.NamespaceMap;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.tiny.CompressedWhitespace;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.type.SchemaType;

public class CommentStripper
extends ProxyReceiver {
    private CompressedWhitespace savedWhitespace = null;
    private final FastStringBuffer buffer = new FastStringBuffer(256);

    public CommentStripper(Receiver next) {
        super(next);
    }

    @Override
    public void startElement(NodeName elemName, SchemaType type, AttributeMap attributes, NamespaceMap namespaces, Location location, int properties) throws XPathException {
        this.flush();
        this.nextReceiver.startElement(elemName, type, attributes, namespaces, location, properties);
    }

    @Override
    public void endElement() throws XPathException {
        this.flush();
        this.nextReceiver.endElement();
    }

    @Override
    public void characters(CharSequence chars, Location locationId, int properties) throws XPathException {
        if (chars instanceof CompressedWhitespace) {
            if (this.buffer.isEmpty() && this.savedWhitespace == null) {
                this.savedWhitespace = (CompressedWhitespace)chars;
            } else {
                ((CompressedWhitespace)chars).uncompress(this.buffer);
            }
        } else {
            if (this.savedWhitespace != null) {
                this.savedWhitespace.uncompress(this.buffer);
                this.savedWhitespace = null;
            }
            this.buffer.cat(chars);
        }
    }

    @Override
    public void comment(CharSequence chars, Location locationId, int properties) {
    }

    @Override
    public void processingInstruction(String name, CharSequence data, Location locationId, int properties) {
    }

    private void flush() throws XPathException {
        if (!this.buffer.isEmpty()) {
            this.nextReceiver.characters(this.buffer, Loc.NONE, 0);
        } else if (this.savedWhitespace != null) {
            this.nextReceiver.characters(this.savedWhitespace, Loc.NONE, 0);
        }
        this.savedWhitespace = null;
        this.buffer.setLength(0);
    }
}

