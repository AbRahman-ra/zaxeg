/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.trans.packages;

import javax.xml.transform.Source;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.style.StylesheetPackage;
import net.sf.saxon.trans.XPathException;

public interface IPackageLoader {
    public StylesheetPackage loadPackageDoc(NodeInfo var1) throws XPathException;

    public StylesheetPackage loadPackage(Source var1) throws XPathException;
}

