/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.dom4j;

import java.util.List;
import org.dom4j.Node;

public interface DocumentType
extends Node {
    public String getElementName();

    public void setElementName(String var1);

    public String getPublicID();

    public void setPublicID(String var1);

    public String getSystemID();

    public void setSystemID(String var1);

    public List getInternalDeclarations();

    public void setInternalDeclarations(List var1);

    public List getExternalDeclarations();

    public void setExternalDeclarations(List var1);
}

