/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package javax.xml.crypto.dsig.spec;

import javax.xml.crypto.XMLStructure;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;

public final class XSLTTransformParameterSpec
implements TransformParameterSpec {
    private XMLStructure stylesheet;

    public XSLTTransformParameterSpec(XMLStructure stylesheet) {
        if (stylesheet == null) {
            throw new NullPointerException();
        }
        this.stylesheet = stylesheet;
    }

    public XMLStructure getStylesheet() {
        return this.stylesheet;
    }
}

