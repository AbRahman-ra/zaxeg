/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package javax.xml.crypto.dsig.spec;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import javax.xml.crypto.dsig.spec.XPathType;

public final class XPathFilter2ParameterSpec
implements TransformParameterSpec {
    private final List xPathList;

    public XPathFilter2ParameterSpec(List xPathList) {
        if (xPathList == null) {
            throw new NullPointerException("xPathList cannot be null");
        }
        this.xPathList = XPathFilter2ParameterSpec.unmodifiableCopyOfList(xPathList);
        if (this.xPathList.isEmpty()) {
            throw new IllegalArgumentException("xPathList cannot be empty");
        }
        int size = this.xPathList.size();
        for (int i = 0; i < size; ++i) {
            if (this.xPathList.get(i) instanceof XPathType) continue;
            throw new ClassCastException("xPathList[" + i + "] is not a valid type");
        }
    }

    private static List unmodifiableCopyOfList(List list) {
        return Collections.unmodifiableList(new ArrayList(list));
    }

    public List getXPathList() {
        return this.xPathList;
    }
}

