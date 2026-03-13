/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.lib;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import net.sf.saxon.Configuration;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.expr.JPConverter;
import net.sf.saxon.expr.PJConverter;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.trans.XPathException;

public interface ExternalObjectModel {
    public String getDocumentClassName();

    public String getIdentifyingURI();

    public PJConverter getPJConverter(Class<?> var1);

    public JPConverter getJPConverter(Class var1, Configuration var2);

    public PJConverter getNodeListCreator(Object var1);

    public Receiver getDocumentBuilder(Result var1) throws XPathException;

    public boolean sendSource(Source var1, Receiver var2) throws XPathException;

    public NodeInfo unravel(Source var1, Configuration var2);
}

