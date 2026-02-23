/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.trans;

import net.sf.saxon.om.StandardNames;
import net.sf.saxon.om.StructuredQName;

public class SymbolicName {
    private int kind;
    private StructuredQName name;

    public SymbolicName(int kind, StructuredQName name) {
        this.kind = kind;
        this.name = name;
    }

    public int hashCode() {
        return this.kind << 16 ^ this.name.hashCode();
    }

    public boolean equals(Object obj) {
        return obj instanceof SymbolicName && ((SymbolicName)obj).kind == this.kind && ((SymbolicName)obj).name.equals(this.name);
    }

    public int getComponentKind() {
        return this.kind;
    }

    public StructuredQName getComponentName() {
        return this.name;
    }

    public String toString() {
        return StandardNames.getLocalName(this.kind) + " " + this.name.getDisplayName();
    }

    public String getShortName() {
        return this.name.getDisplayName();
    }

    public static class F
    extends SymbolicName {
        int arity;

        public F(StructuredQName name, int arity) {
            super(158, name);
            this.arity = arity;
        }

        public int getArity() {
            return this.arity;
        }

        @Override
        public int hashCode() {
            return super.hashCode() ^ this.arity;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof F && super.equals(obj) && ((F)obj).arity == this.arity;
        }

        @Override
        public String toString() {
            return super.toString() + "#" + this.arity;
        }

        @Override
        public String getShortName() {
            return super.getShortName() + "#" + this.arity;
        }
    }
}

