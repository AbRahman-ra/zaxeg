/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.event;

import net.sf.saxon.event.ProxyReceiver;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.om.AttributeMap;
import net.sf.saxon.om.NamespaceMap;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.SchemaType;

public class Valve
extends ProxyReceiver {
    private boolean started = false;
    private String testNamespace;
    private Receiver alternativeReceiver;

    public Valve(String testNamespace, Receiver primary, Receiver secondary) {
        super(primary);
        this.testNamespace = testNamespace;
        this.alternativeReceiver = secondary;
    }

    @Override
    public void startElement(NodeName elemName, SchemaType type, AttributeMap attributes, NamespaceMap namespaces, Location location, int properties) throws XPathException {
        if (!this.started) {
            this.started = true;
            if (elemName.getURI().equals(this.testNamespace)) {
                this.alternativeReceiver.open();
                this.alternativeReceiver.startDocument(0);
                try {
                    this.getNextReceiver().close();
                } catch (XPathException xPathException) {
                    // empty catch block
                }
                this.setUnderlyingReceiver(this.alternativeReceiver);
            }
        }
        super.startElement(elemName, type, attributes, namespaces, location, properties);
    }

    public boolean wasDiverted() {
        return this.getNextReceiver() == this.alternativeReceiver;
    }
}

