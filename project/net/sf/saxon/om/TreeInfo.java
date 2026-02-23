/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.om;

import java.util.Iterator;
import javax.xml.transform.Source;
import net.sf.saxon.Configuration;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.SpaceStrippingRule;

public interface TreeInfo
extends Source {
    public NodeInfo getRootNode();

    public Configuration getConfiguration();

    public long getDocumentNumber();

    default public boolean isTyped() {
        return false;
    }

    default public boolean isMutable() {
        return false;
    }

    public NodeInfo selectID(String var1, boolean var2);

    public Iterator<String> getUnparsedEntityNames();

    public String[] getUnparsedEntity(String var1);

    public void setSpaceStrippingRule(SpaceStrippingRule var1);

    public SpaceStrippingRule getSpaceStrippingRule();

    public void setUserData(String var1, Object var2);

    public Object getUserData(String var1);
}

