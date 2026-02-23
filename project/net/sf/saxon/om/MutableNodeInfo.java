/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.om;

import net.sf.saxon.event.Builder;
import net.sf.saxon.om.AttributeMap;
import net.sf.saxon.om.NamespaceBinding;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.type.SimpleType;

public interface MutableNodeInfo
extends NodeInfo {
    public void setTypeAnnotation(SchemaType var1);

    public void insertChildren(NodeInfo[] var1, boolean var2, boolean var3);

    public void insertSiblings(NodeInfo[] var1, boolean var2, boolean var3);

    public void setAttributes(AttributeMap var1);

    public void removeAttribute(NodeInfo var1);

    public void addAttribute(NodeName var1, SimpleType var2, CharSequence var3, int var4);

    default public void removeNamespace(String prefix) {
    }

    default public void addNamespace(String prefix, String uri) {
    }

    public void delete();

    public boolean isDeleted();

    public void replace(NodeInfo[] var1, boolean var2);

    public void replaceStringValue(CharSequence var1);

    public void rename(NodeName var1);

    public void addNamespace(NamespaceBinding var1);

    public void removeTypeAnnotation();

    public Builder newBuilder();
}

