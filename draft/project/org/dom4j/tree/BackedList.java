/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.dom4j.tree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.dom4j.IllegalAddException;
import org.dom4j.Node;
import org.dom4j.tree.AbstractBranch;

public class BackedList
extends ArrayList {
    private List branchContent;
    private AbstractBranch branch;

    public BackedList(AbstractBranch branch, List branchContent) {
        this(branch, branchContent, branchContent.size());
    }

    public BackedList(AbstractBranch branch, List branchContent, int capacity) {
        super(capacity);
        this.branch = branch;
        this.branchContent = branchContent;
    }

    public BackedList(AbstractBranch branch, List branchContent, List initialContent) {
        super(initialContent);
        this.branch = branch;
        this.branchContent = branchContent;
    }

    public boolean add(Object object) {
        this.branch.addNode(this.asNode(object));
        return super.add(object);
    }

    public void add(int index, Object object) {
        int size = this.size();
        if (index < 0) {
            throw new IndexOutOfBoundsException("Index value: " + index + " is less than zero");
        }
        if (index > size) {
            throw new IndexOutOfBoundsException("Index value: " + index + " cannot be greater than " + "the size: " + size);
        }
        int realIndex = size == 0 ? this.branchContent.size() : (index < size ? this.branchContent.indexOf(this.get(index)) : this.branchContent.indexOf(this.get(size - 1)) + 1);
        this.branch.addNode(realIndex, this.asNode(object));
        super.add(index, object);
    }

    public Object set(int index, Object object) {
        int realIndex = this.branchContent.indexOf(this.get(index));
        if (realIndex < 0) {
            int n = realIndex = index == 0 ? 0 : Integer.MAX_VALUE;
        }
        if (realIndex < this.branchContent.size()) {
            this.branch.removeNode(this.asNode(this.get(index)));
            this.branch.addNode(realIndex, this.asNode(object));
        } else {
            this.branch.removeNode(this.asNode(this.get(index)));
            this.branch.addNode(this.asNode(object));
        }
        this.branch.childAdded(this.asNode(object));
        return super.set(index, object);
    }

    public boolean remove(Object object) {
        this.branch.removeNode(this.asNode(object));
        return super.remove(object);
    }

    public Object remove(int index) {
        Object object = super.remove(index);
        if (object != null) {
            this.branch.removeNode(this.asNode(object));
        }
        return object;
    }

    public boolean addAll(Collection collection) {
        this.ensureCapacity(this.size() + collection.size());
        int count = this.size();
        Iterator iter = collection.iterator();
        while (iter.hasNext()) {
            this.add(iter.next());
            --count;
        }
        return count != 0;
    }

    public boolean addAll(int index, Collection collection) {
        this.ensureCapacity(this.size() + collection.size());
        int count = this.size();
        Iterator iter = collection.iterator();
        while (iter.hasNext()) {
            this.add(index++, iter.next());
            --count;
        }
        return count != 0;
    }

    public void clear() {
        Iterator iter = this.iterator();
        while (iter.hasNext()) {
            Object object = iter.next();
            this.branchContent.remove(object);
            this.branch.childRemoved(this.asNode(object));
        }
        super.clear();
    }

    public void addLocal(Object object) {
        super.add(object);
    }

    protected Node asNode(Object object) {
        if (object instanceof Node) {
            return (Node)object;
        }
        throw new IllegalAddException("This list must contain instances of Node. Invalid type: " + object);
    }
}

