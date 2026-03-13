/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.jaxen.expr;

import java.util.Comparator;
import java.util.Iterator;
import org.jaxen.Navigator;
import org.jaxen.UnsupportedAxisException;

class NodeComparator
implements Comparator {
    private Navigator navigator;

    NodeComparator(Navigator navigator) {
        this.navigator = navigator;
    }

    public int compare(Object o1, Object o2) {
        if (o1 == o2) {
            return 0;
        }
        if (this.navigator == null) {
            return 0;
        }
        if (this.isNonChild(o1) && this.isNonChild(o2)) {
            try {
                Object p1 = this.navigator.getParentNode(o1);
                Object p2 = this.navigator.getParentNode(o2);
                if (p1 == p2) {
                    if (this.navigator.isNamespace(o1) && this.navigator.isAttribute(o2)) {
                        return -1;
                    }
                    if (this.navigator.isNamespace(o2) && this.navigator.isAttribute(o1)) {
                        return 1;
                    }
                    if (this.navigator.isNamespace(o1)) {
                        String prefix1 = this.navigator.getNamespacePrefix(o1);
                        String prefix2 = this.navigator.getNamespacePrefix(o2);
                        return prefix1.compareTo(prefix2);
                    }
                    if (this.navigator.isAttribute(o1)) {
                        String name1 = this.navigator.getAttributeQName(o1);
                        String name2 = this.navigator.getAttributeQName(o2);
                        return name1.compareTo(name2);
                    }
                }
                return this.compare(p1, p2);
            } catch (UnsupportedAxisException ex) {
                return 0;
            }
        }
        try {
            int depth1;
            int depth2 = this.getDepth(o2);
            Object a1 = o1;
            Object a2 = o2;
            for (depth1 = this.getDepth(o1); depth1 > depth2; --depth1) {
                a1 = this.navigator.getParentNode(a1);
            }
            if (a1 == o2) {
                return 1;
            }
            while (depth2 > depth1) {
                a2 = this.navigator.getParentNode(a2);
                --depth2;
            }
            if (a2 == o1) {
                return -1;
            }
            while (true) {
                Object p2;
                Object p1;
                if ((p1 = this.navigator.getParentNode(a1)) == (p2 = this.navigator.getParentNode(a2))) {
                    return this.compareSiblings(a1, a2);
                }
                a1 = p1;
                a2 = p2;
            }
        } catch (UnsupportedAxisException ex) {
            return 0;
        }
    }

    private boolean isNonChild(Object o) {
        return this.navigator.isAttribute(o) || this.navigator.isNamespace(o);
    }

    private int compareSiblings(Object sib1, Object sib2) throws UnsupportedAxisException {
        if (this.isNonChild(sib1)) {
            return 1;
        }
        if (this.isNonChild(sib2)) {
            return -1;
        }
        Iterator following = this.navigator.getFollowingSiblingAxisIterator(sib1);
        while (following.hasNext()) {
            Object next = following.next();
            if (!next.equals(sib2)) continue;
            return -1;
        }
        return 1;
    }

    private int getDepth(Object o) throws UnsupportedAxisException {
        int depth = 0;
        Object parent = o;
        while ((parent = this.navigator.getParentNode(parent)) != null) {
            ++depth;
        }
        return depth;
    }
}

