/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.event;

import java.util.Properties;
import java.util.Stack;
import net.sf.saxon.event.ProxyReceiver;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.om.AttributeMap;
import net.sf.saxon.om.NamespaceBinding;
import net.sf.saxon.om.NamespaceDeltaMap;
import net.sf.saxon.om.NamespaceMap;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.SchemaType;

public class NamespaceDifferencer
extends ProxyReceiver {
    private boolean undeclareNamespaces = false;
    private Stack<NamespaceMap> namespaceStack = new Stack();
    private NodeName currentElement;

    public NamespaceDifferencer(Receiver next, Properties details) {
        super(next);
        this.undeclareNamespaces = "yes".equals(details.getProperty("undeclare-prefixes"));
        this.namespaceStack.push(NamespaceMap.emptyMap());
    }

    @Override
    public void startElement(NodeName elemName, SchemaType type, AttributeMap attributes, NamespaceMap namespaces, Location location, int properties) throws XPathException {
        this.currentElement = elemName;
        NamespaceMap parentMap = this.namespaceStack.peek();
        this.namespaceStack.push(namespaces);
        NamespaceMap delta = this.getDifferences(namespaces, parentMap, this.currentElement.hasURI(""));
        this.nextReceiver.startElement(elemName, type, attributes, delta, location, properties);
    }

    @Override
    public void endElement() throws XPathException {
        this.namespaceStack.pop();
        super.endElement();
    }

    private NamespaceMap getDifferences(NamespaceMap thisMap, NamespaceMap parentMap, boolean elementInDefaultNamespace) throws XPathException {
        if (thisMap != parentMap) {
            NamespaceMap delta = NamespaceDeltaMap.emptyMap();
            for (NamespaceBinding nb : thisMap) {
                String parentUri = parentMap.getURI(nb.getPrefix());
                if (parentUri == null) {
                    delta = ((NamespaceMap)delta).put(nb.getPrefix(), nb.getURI());
                    continue;
                }
                if (parentUri.equals(nb.getURI())) continue;
                delta = ((NamespaceMap)delta).put(nb.getPrefix(), nb.getURI());
            }
            if (this.undeclareNamespaces) {
                for (NamespaceBinding nb : parentMap) {
                    if (thisMap.getURI(nb.getPrefix()) != null) continue;
                    delta = ((NamespaceMap)delta).put(nb.getPrefix(), "");
                }
            } else if (!parentMap.getDefaultNamespace().isEmpty() && thisMap.getDefaultNamespace().isEmpty()) {
                delta = ((NamespaceMap)delta).put("", "");
            }
            return delta;
        }
        return NamespaceMap.emptyMap();
    }
}

