/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.event;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import net.sf.saxon.event.ProxyReceiver;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.event.ReceiverOption;
import net.sf.saxon.om.AttributeMap;
import net.sf.saxon.om.NamespaceBinding;
import net.sf.saxon.om.NamespaceMap;
import net.sf.saxon.om.NamespaceResolver;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.SchemaType;

public class NamespaceReducer
extends ProxyReceiver
implements NamespaceResolver {
    private NamespaceBinding[] namespaces = new NamespaceBinding[50];
    private int namespacesSize = 0;
    private int[] countStack = new int[50];
    private int depth = 0;
    private boolean[] disinheritStack = new boolean[50];
    private NamespaceBinding[] pendingUndeclarations = null;

    public NamespaceReducer(Receiver next) {
        super(next);
    }

    @Override
    public void startElement(NodeName elemName, SchemaType type, AttributeMap attributes, NamespaceMap namespaceMap, Location location, int properties) throws XPathException {
        this.nextReceiver.startElement(elemName, type, attributes, namespaceMap, location, properties);
        if (ReceiverOption.contains(properties, 65536)) {
            this.pendingUndeclarations = Arrays.copyOf(this.namespaces, this.namespacesSize);
        } else if (this.depth > 0 && this.disinheritStack[this.depth - 1]) {
            ArrayList<NamespaceBinding> undeclarations = new ArrayList<NamespaceBinding>(this.namespacesSize);
            int k = this.namespacesSize;
            for (int d = this.depth - 1; d >= 0 && this.disinheritStack[d]; --d) {
                for (int i = 0; i < this.countStack[d]; ++i) {
                    undeclarations.add(this.namespaces[--k]);
                }
            }
            this.pendingUndeclarations = undeclarations.toArray(NamespaceBinding.EMPTY_ARRAY);
        } else {
            this.pendingUndeclarations = null;
        }
        this.countStack[this.depth] = 0;
        this.disinheritStack[this.depth] = ReceiverOption.contains(properties, 128);
        if (++this.depth >= this.countStack.length) {
            this.countStack = Arrays.copyOf(this.countStack, this.depth * 2);
            this.disinheritStack = Arrays.copyOf(this.disinheritStack, this.depth * 2);
        }
    }

    private boolean isNeeded(NamespaceBinding nsBinding) {
        if (nsBinding.isXmlNamespace()) {
            return false;
        }
        String prefix = nsBinding.getPrefix();
        if (this.pendingUndeclarations != null) {
            for (int p = 0; p < this.pendingUndeclarations.length; ++p) {
                NamespaceBinding nb = this.pendingUndeclarations[p];
                if (nb == null || !prefix.equals(nb.getPrefix())) continue;
                this.pendingUndeclarations[p] = null;
            }
        }
        for (int i = this.namespacesSize - 1; i >= 0; --i) {
            if (this.namespaces[i].equals(nsBinding)) {
                return false;
            }
            if (!this.namespaces[i].getPrefix().equals(nsBinding.getPrefix())) continue;
            return true;
        }
        return !nsBinding.isDefaultUndeclaration();
    }

    private void addToStack(NamespaceBinding nsBinding) {
        if (this.namespacesSize + 1 >= this.namespaces.length) {
            this.namespaces = Arrays.copyOf(this.namespaces, this.namespacesSize * 2);
        }
        this.namespaces[this.namespacesSize++] = nsBinding;
    }

    public boolean isDisinheritingNamespaces() {
        return this.depth > 0 && this.disinheritStack[this.depth - 1];
    }

    @Override
    public void endElement() throws XPathException {
        if (this.depth-- == 0) {
            throw new IllegalStateException("Attempt to output end tag with no matching start tag");
        }
        this.namespacesSize -= this.countStack[this.depth];
        this.nextReceiver.endElement();
    }

    @Override
    public String getURIForPrefix(String prefix, boolean useDefault) {
        if (prefix.isEmpty() && !useDefault) {
            return "";
        }
        if ("xml".equals(prefix)) {
            return "http://www.w3.org/XML/1998/namespace";
        }
        for (int i = this.namespacesSize - 1; i >= 0; --i) {
            if (!this.namespaces[i].getPrefix().equals(prefix)) continue;
            return this.namespaces[i].getURI();
        }
        return prefix.isEmpty() ? "" : null;
    }

    @Override
    public Iterator<String> iteratePrefixes() {
        ArrayList<String> prefixes = new ArrayList<String>(this.namespacesSize);
        for (int i = this.namespacesSize - 1; i >= 0; --i) {
            String prefix = this.namespaces[i].getPrefix();
            if (prefixes.contains(prefix)) continue;
            prefixes.add(prefix);
        }
        prefixes.add("xml");
        return prefixes.iterator();
    }
}

