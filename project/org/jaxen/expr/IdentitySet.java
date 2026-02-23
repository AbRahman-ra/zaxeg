/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.jaxen.expr;

import java.util.HashSet;

final class IdentitySet {
    private HashSet contents = new HashSet();

    IdentitySet() {
    }

    void add(Object object) {
        IdentityWrapper wrapper = new IdentityWrapper(object);
        this.contents.add(wrapper);
    }

    public boolean contains(Object object) {
        IdentityWrapper wrapper = new IdentityWrapper(object);
        return this.contents.contains(wrapper);
    }

    private static class IdentityWrapper {
        private Object object;

        IdentityWrapper(Object object) {
            this.object = object;
        }

        public boolean equals(Object o) {
            IdentityWrapper w = (IdentityWrapper)o;
            return this.object == w.object;
        }

        public int hashCode() {
            return System.identityHashCode(this.object);
        }
    }
}

