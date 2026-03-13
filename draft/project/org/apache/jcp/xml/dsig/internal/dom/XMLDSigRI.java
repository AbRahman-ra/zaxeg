/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.jcp.xml.dsig.internal.dom;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.Provider;
import java.util.HashMap;
import java.util.Map;

public final class XMLDSigRI
extends Provider {
    static final long serialVersionUID = -5049765099299494554L;
    private static final String INFO = "Apache Santuario XMLDSig (DOM XMLSignatureFactory; DOM KeyInfoFactory; C14N 1.0, C14N 1.1, Exclusive C14N, Base64, Enveloped, XPath, XPath2, XSLT TransformServices)";

    public XMLDSigRI() {
        super("ApacheXMLDSig", 1.58, INFO);
        final HashMap<String, String> map = new HashMap<String, String>();
        map.put("XMLSignatureFactory.DOM", "org.apache.jcp.xml.dsig.internal.dom.DOMXMLSignatureFactory");
        map.put("KeyInfoFactory.DOM", "org.apache.jcp.xml.dsig.internal.dom.DOMKeyInfoFactory");
        map.put("TransformService.http://www.w3.org/TR/2001/REC-xml-c14n-20010315", "org.apache.jcp.xml.dsig.internal.dom.DOMCanonicalXMLC14NMethod");
        map.put("Alg.Alias.TransformService.INCLUSIVE", "http://www.w3.org/TR/2001/REC-xml-c14n-20010315");
        map.put("TransformService.http://www.w3.org/TR/2001/REC-xml-c14n-20010315 MechanismType", "DOM");
        map.put("TransformService.http://www.w3.org/TR/2001/REC-xml-c14n-20010315#WithComments", "org.apache.jcp.xml.dsig.internal.dom.DOMCanonicalXMLC14NMethod");
        map.put("Alg.Alias.TransformService.INCLUSIVE_WITH_COMMENTS", "http://www.w3.org/TR/2001/REC-xml-c14n-20010315#WithComments");
        map.put("TransformService.http://www.w3.org/TR/2001/REC-xml-c14n-20010315#WithComments MechanismType", "DOM");
        map.put("TransformService.http://www.w3.org/2006/12/xml-c14n11", "org.apache.jcp.xml.dsig.internal.dom.DOMCanonicalXMLC14N11Method");
        map.put("TransformService.http://www.w3.org/2006/12/xml-c14n11 MechanismType", "DOM");
        map.put("TransformService.http://www.w3.org/2006/12/xml-c14n11#WithComments", "org.apache.jcp.xml.dsig.internal.dom.DOMCanonicalXMLC14N11Method");
        map.put("TransformService.http://www.w3.org/2006/12/xml-c14n11#WithComments MechanismType", "DOM");
        map.put("TransformService.http://www.w3.org/2001/10/xml-exc-c14n#", "org.apache.jcp.xml.dsig.internal.dom.DOMExcC14NMethod");
        map.put("Alg.Alias.TransformService.EXCLUSIVE", "http://www.w3.org/2001/10/xml-exc-c14n#");
        map.put("TransformService.http://www.w3.org/2001/10/xml-exc-c14n# MechanismType", "DOM");
        map.put("TransformService.http://www.w3.org/2001/10/xml-exc-c14n#WithComments", "org.apache.jcp.xml.dsig.internal.dom.DOMExcC14NMethod");
        map.put("Alg.Alias.TransformService.EXCLUSIVE_WITH_COMMENTS", "http://www.w3.org/2001/10/xml-exc-c14n#WithComments");
        map.put("TransformService.http://www.w3.org/2001/10/xml-exc-c14n#WithComments MechanismType", "DOM");
        map.put("TransformService.http://www.w3.org/2000/09/xmldsig#base64", "org.apache.jcp.xml.dsig.internal.dom.DOMBase64Transform");
        map.put("Alg.Alias.TransformService.BASE64", "http://www.w3.org/2000/09/xmldsig#base64");
        map.put("TransformService.http://www.w3.org/2000/09/xmldsig#base64 MechanismType", "DOM");
        map.put("TransformService.http://www.w3.org/2000/09/xmldsig#enveloped-signature", "org.apache.jcp.xml.dsig.internal.dom.DOMEnvelopedTransform");
        map.put("Alg.Alias.TransformService.ENVELOPED", "http://www.w3.org/2000/09/xmldsig#enveloped-signature");
        map.put("TransformService.http://www.w3.org/2000/09/xmldsig#enveloped-signature MechanismType", "DOM");
        map.put("TransformService.http://www.w3.org/2002/06/xmldsig-filter2", "org.apache.jcp.xml.dsig.internal.dom.DOMXPathFilter2Transform");
        map.put("Alg.Alias.TransformService.XPATH2", "http://www.w3.org/2002/06/xmldsig-filter2");
        map.put("TransformService.http://www.w3.org/2002/06/xmldsig-filter2 MechanismType", "DOM");
        map.put("TransformService.http://www.w3.org/TR/1999/REC-xpath-19991116", "org.apache.jcp.xml.dsig.internal.dom.DOMXPathTransform");
        map.put("Alg.Alias.TransformService.XPATH", "http://www.w3.org/TR/1999/REC-xpath-19991116");
        map.put("TransformService.http://www.w3.org/TR/1999/REC-xpath-19991116 MechanismType", "DOM");
        map.put("TransformService.http://www.w3.org/TR/1999/REC-xslt-19991116", "org.apache.jcp.xml.dsig.internal.dom.DOMXSLTTransform");
        map.put("Alg.Alias.TransformService.XSLT", "http://www.w3.org/TR/1999/REC-xslt-19991116");
        map.put("TransformService.http://www.w3.org/TR/1999/REC-xslt-19991116 MechanismType", "DOM");
        AccessController.doPrivileged(new PrivilegedAction<Void>(){

            @Override
            public Void run() {
                XMLDSigRI.this.putAll((Map<?, ?>)map);
                return null;
            }
        });
    }
}

