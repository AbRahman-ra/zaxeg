/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package javax.xml.crypto.dsig;

import java.security.spec.AlgorithmParameterSpec;
import javax.xml.crypto.dsig.Transform;

public interface CanonicalizationMethod
extends Transform {
    public static final String INCLUSIVE = "http://www.w3.org/TR/2001/REC-xml-c14n-20010315";
    public static final String INCLUSIVE_WITH_COMMENTS = "http://www.w3.org/TR/2001/REC-xml-c14n-20010315#WithComments";
    public static final String EXCLUSIVE = "http://www.w3.org/2001/10/xml-exc-c14n#";
    public static final String EXCLUSIVE_WITH_COMMENTS = "http://www.w3.org/2001/10/xml-exc-c14n#WithComments";

    public AlgorithmParameterSpec getParameterSpec();
}

