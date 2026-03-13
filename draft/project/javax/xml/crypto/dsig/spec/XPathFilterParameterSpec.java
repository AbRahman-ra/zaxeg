/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package javax.xml.crypto.dsig.spec;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;

public final class XPathFilterParameterSpec
implements TransformParameterSpec {
    private String xPath;
    private Map nsMap;

    public XPathFilterParameterSpec(String xPath) {
        if (xPath == null) {
            throw new NullPointerException();
        }
        this.xPath = xPath;
        this.nsMap = Collections.EMPTY_MAP;
    }

    public XPathFilterParameterSpec(String xPath, Map namespaceMap) {
        if (xPath == null || namespaceMap == null) {
            throw new NullPointerException();
        }
        this.xPath = xPath;
        this.nsMap = XPathFilterParameterSpec.unmodifiableCopyOfMap(namespaceMap);
        for (Map.Entry me : this.nsMap.entrySet()) {
            if (me.getKey() instanceof String && me.getValue() instanceof String) continue;
            throw new ClassCastException("not a String");
        }
    }

    private static Map unmodifiableCopyOfMap(Map map) {
        return Collections.unmodifiableMap(new HashMap(map));
    }

    public String getXPath() {
        return this.xPath;
    }

    public Map getNamespaceMap() {
        return this.nsMap;
    }
}

