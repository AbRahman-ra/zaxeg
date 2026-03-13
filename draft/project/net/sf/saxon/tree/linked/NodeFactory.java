/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.tree.linked;

import net.sf.saxon.event.PipelineConfiguration;
import net.sf.saxon.om.AttributeMap;
import net.sf.saxon.om.NamespaceMap;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.tree.linked.ElementImpl;
import net.sf.saxon.tree.linked.TextImpl;
import net.sf.saxon.type.SchemaType;

public interface NodeFactory {
    public ElementImpl makeElementNode(NodeInfo var1, NodeName var2, SchemaType var3, boolean var4, AttributeMap var5, NamespaceMap var6, PipelineConfiguration var7, Location var8, int var9);

    public TextImpl makeTextNode(NodeInfo var1, CharSequence var2);
}

