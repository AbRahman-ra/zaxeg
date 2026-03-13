/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.event;

import java.util.HashMap;
import java.util.Stack;
import net.sf.saxon.event.ProxyReceiver;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.om.AbsolutePath;
import net.sf.saxon.om.AttributeMap;
import net.sf.saxon.om.NamespaceMap;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.type.SchemaType;

public class PathMaintainer
extends ProxyReceiver {
    private final Stack<AbsolutePath.PathElement> path = new Stack();
    private final Stack<HashMap<NodeName, Integer>> siblingCounters = new Stack();

    public PathMaintainer(Receiver next) {
        super(next);
        this.siblingCounters.push(new HashMap());
    }

    @Override
    public void startElement(NodeName elemName, SchemaType type, AttributeMap attributes, NamespaceMap namespaces, Location location, int properties) throws XPathException {
        this.nextReceiver.startElement(elemName, type, attributes, namespaces, location, properties);
        HashMap<NodeName, Integer> counters = this.siblingCounters.peek();
        int index = 1;
        Integer preceding = counters.get(elemName);
        if (preceding != null) {
            index = preceding + 1;
            counters.put(elemName, index);
        } else {
            counters.put(elemName, 1);
        }
        this.path.push(new AbsolutePath.PathElement(1, elemName, index));
        this.siblingCounters.push(new HashMap());
    }

    @Override
    public void endElement() throws XPathException {
        this.nextReceiver.endElement();
        this.siblingCounters.pop();
        this.path.pop();
    }

    public String getPath(boolean useURIs) {
        FastStringBuffer fsb = new FastStringBuffer(256);
        for (AbsolutePath.PathElement pe : this.path) {
            fsb.cat('/');
            if (useURIs) {
                String uri = pe.getName().getURI();
                if (!uri.isEmpty()) {
                    fsb.cat('\"');
                    fsb.append(uri);
                    fsb.cat('\"');
                }
            } else {
                String prefix = pe.getName().getPrefix();
                if (!prefix.isEmpty()) {
                    fsb.append(prefix);
                    fsb.cat(':');
                }
            }
            fsb.append(pe.getName().getLocalPart());
            fsb.cat('[');
            fsb.append(pe.getIndex() + "");
            fsb.cat(']');
        }
        return fsb.toString();
    }

    public AbsolutePath getAbsolutePath() {
        return new AbsolutePath(this.path);
    }
}

