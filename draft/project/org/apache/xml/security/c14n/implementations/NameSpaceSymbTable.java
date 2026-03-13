/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.xml.security.c14n.implementations;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.xml.security.c14n.implementations.NameSpaceSymbEntry;
import org.apache.xml.security.c14n.implementations.SymbMap;
import org.w3c.dom.Attr;
import org.w3c.dom.Node;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class NameSpaceSymbTable {
    private static final String XMLNS = "xmlns";
    private static final SymbMap initialMap = new SymbMap();
    private SymbMap symb;
    private List<SymbMap> level = new ArrayList<SymbMap>();
    private boolean cloned = true;

    public NameSpaceSymbTable() {
        this.symb = (SymbMap)initialMap.clone();
    }

    public void getUnrenderedNodes(Collection<Attr> result) {
        for (NameSpaceSymbEntry n : this.symb.entrySet()) {
            if (n.rendered || n.n == null) continue;
            n = (NameSpaceSymbEntry)n.clone();
            this.needsClone();
            this.symb.put(n.prefix, n);
            n.lastrendered = n.uri;
            n.rendered = true;
            result.add(n.n);
        }
    }

    public void outputNodePush() {
        this.push();
    }

    public void outputNodePop() {
        this.pop();
    }

    public void push() {
        this.level.add(null);
        this.cloned = false;
    }

    public void pop() {
        int size = this.level.size() - 1;
        SymbMap ob = this.level.remove(size);
        if (ob != null) {
            this.symb = ob;
            this.cloned = size == 0 ? false : this.level.get(size - 1) != this.symb;
        } else {
            this.cloned = false;
        }
    }

    final void needsClone() {
        if (!this.cloned) {
            this.level.set(this.level.size() - 1, this.symb);
            this.symb = (SymbMap)this.symb.clone();
            this.cloned = true;
        }
    }

    public Attr getMapping(String prefix) {
        NameSpaceSymbEntry entry = this.symb.get(prefix);
        if (entry == null) {
            return null;
        }
        if (entry.rendered) {
            return null;
        }
        entry = (NameSpaceSymbEntry)entry.clone();
        this.needsClone();
        this.symb.put(prefix, entry);
        entry.rendered = true;
        entry.lastrendered = entry.uri;
        return entry.n;
    }

    public Attr getMappingWithoutRendered(String prefix) {
        NameSpaceSymbEntry entry = this.symb.get(prefix);
        if (entry == null) {
            return null;
        }
        if (entry.rendered) {
            return null;
        }
        return entry.n;
    }

    public boolean addMapping(String prefix, String uri, Attr n) {
        NameSpaceSymbEntry ob = this.symb.get(prefix);
        if (ob != null && uri.equals(ob.uri)) {
            return false;
        }
        NameSpaceSymbEntry ne = new NameSpaceSymbEntry(uri, n, false, prefix);
        this.needsClone();
        this.symb.put(prefix, ne);
        if (ob != null) {
            ne.lastrendered = ob.lastrendered;
            if (ob.lastrendered != null && ob.lastrendered.equals(uri)) {
                ne.rendered = true;
            }
        }
        return true;
    }

    public Node addMappingAndRender(String prefix, String uri, Attr n) {
        NameSpaceSymbEntry ob = this.symb.get(prefix);
        if (ob != null && uri.equals(ob.uri)) {
            if (!ob.rendered) {
                ob = (NameSpaceSymbEntry)ob.clone();
                this.needsClone();
                this.symb.put(prefix, ob);
                ob.lastrendered = uri;
                ob.rendered = true;
                return ob.n;
            }
            return null;
        }
        NameSpaceSymbEntry ne = new NameSpaceSymbEntry(uri, n, true, prefix);
        ne.lastrendered = uri;
        this.needsClone();
        this.symb.put(prefix, ne);
        if (ob != null && ob.lastrendered != null && ob.lastrendered.equals(uri)) {
            ne.rendered = true;
            return null;
        }
        return ne.n;
    }

    public int getLevel() {
        return this.level.size();
    }

    public void removeMapping(String prefix) {
        NameSpaceSymbEntry ob = this.symb.get(prefix);
        if (ob != null) {
            this.needsClone();
            this.symb.put(prefix, null);
        }
    }

    public void removeMappingIfNotRender(String prefix) {
        NameSpaceSymbEntry ob = this.symb.get(prefix);
        if (ob != null && !ob.rendered) {
            this.needsClone();
            this.symb.put(prefix, null);
        }
    }

    public boolean removeMappingIfRender(String prefix) {
        NameSpaceSymbEntry ob = this.symb.get(prefix);
        if (ob != null && ob.rendered) {
            this.needsClone();
            this.symb.put(prefix, null);
        }
        return false;
    }

    static {
        NameSpaceSymbEntry ne = new NameSpaceSymbEntry("", null, true, XMLNS);
        ne.lastrendered = "";
        initialMap.put(XMLNS, ne);
    }
}

