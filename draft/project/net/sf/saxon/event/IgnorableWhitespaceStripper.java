/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.event;

import java.util.Arrays;
import net.sf.saxon.event.ProxyReceiver;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.om.AttributeMap;
import net.sf.saxon.om.NamespaceMap;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.ComplexType;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.type.Untyped;
import net.sf.saxon.value.Whitespace;

public class IgnorableWhitespaceStripper
extends ProxyReceiver {
    private boolean[] stripStack = new boolean[100];
    private int top = 0;

    public IgnorableWhitespaceStripper(Receiver next) {
        super(next);
    }

    @Override
    public void startElement(NodeName elemName, SchemaType type, AttributeMap attributes, NamespaceMap namespaces, Location location, int properties) throws XPathException {
        this.nextReceiver.startElement(elemName, type, attributes, namespaces, location, properties);
        boolean strip = false;
        if (type != Untyped.getInstance() && type.isComplexType() && !((ComplexType)type).isSimpleContent() && !((ComplexType)type).isMixedContent()) {
            strip = true;
        }
        ++this.top;
        if (this.top >= this.stripStack.length) {
            this.stripStack = Arrays.copyOf(this.stripStack, this.top * 2);
        }
        this.stripStack[this.top] = strip;
    }

    @Override
    public void endElement() throws XPathException {
        this.nextReceiver.endElement();
        --this.top;
    }

    @Override
    public void characters(CharSequence chars, Location locationId, int properties) throws XPathException {
        if (!(chars.length() <= 0 || this.stripStack[this.top] && Whitespace.isWhite(chars))) {
            this.nextReceiver.characters(chars, locationId, properties);
        }
    }

    @Override
    public boolean usesTypeAnnotations() {
        return true;
    }
}

