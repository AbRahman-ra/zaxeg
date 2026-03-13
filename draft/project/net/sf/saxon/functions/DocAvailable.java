/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions;

import net.sf.saxon.expr.PackageData;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.functions.DocumentFn;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.om.DocumentKey;
import net.sf.saxon.om.DocumentPool;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.TreeInfo;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.BooleanValue;

public class DocAvailable
extends SystemFunction {
    private boolean isDocAvailable(AtomicValue hrefVal, XPathContext context) throws XPathException {
        if (hrefVal == null) {
            return false;
        }
        String href = hrefVal.getStringValue();
        return this.docAvailable(href, context);
    }

    @Override
    public BooleanValue call(XPathContext context, Sequence[] arguments) throws XPathException {
        return BooleanValue.get(this.isDocAvailable((AtomicValue)arguments[0].head(), context));
    }

    public boolean docAvailable(String href, XPathContext context) {
        try {
            PackageData packageData = this.getRetainedStaticContext().getPackageData();
            DocumentKey documentKey = DocumentFn.computeDocumentKey(href, this.getStaticBaseUriString(), packageData, context);
            DocumentPool pool = context.getController().getDocumentPool();
            if (pool.isMarkedUnavailable(documentKey)) {
                return false;
            }
            TreeInfo doc = pool.find(documentKey);
            if (doc != null) {
                return true;
            }
            NodeInfo item = DocumentFn.makeDoc(href, this.getStaticBaseUriString(), packageData, null, context, null, true);
            if (item != null) {
                return true;
            }
            pool.markUnavailable(documentKey);
            return false;
        } catch (XPathException e) {
            return false;
        }
    }
}

