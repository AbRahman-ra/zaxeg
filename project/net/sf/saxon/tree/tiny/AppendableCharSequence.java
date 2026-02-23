/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.tree.tiny;

public interface AppendableCharSequence
extends CharSequence {
    public AppendableCharSequence cat(CharSequence var1);

    public AppendableCharSequence cat(char var1);

    public void setLength(int var1);
}

