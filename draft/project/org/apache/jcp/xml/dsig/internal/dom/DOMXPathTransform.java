/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.jcp.xml.dsig.internal.dom;

import java.security.InvalidAlgorithmParameterException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.xml.crypto.MarshalException;
import javax.xml.crypto.XMLCryptoContext;
import javax.xml.crypto.XMLStructure;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import javax.xml.crypto.dsig.spec.XPathFilterParameterSpec;
import org.apache.jcp.xml.dsig.internal.dom.ApacheTransform;
import org.apache.jcp.xml.dsig.internal.dom.DOMUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

public final class DOMXPathTransform
extends ApacheTransform {
    public void init(TransformParameterSpec params) throws InvalidAlgorithmParameterException {
        if (params == null) {
            throw new InvalidAlgorithmParameterException("params are required");
        }
        if (!(params instanceof XPathFilterParameterSpec)) {
            throw new InvalidAlgorithmParameterException("params must be of type XPathFilterParameterSpec");
        }
        this.params = params;
    }

    public void init(XMLStructure parent, XMLCryptoContext context) throws InvalidAlgorithmParameterException {
        super.init(parent, context);
        this.unmarshalParams(DOMUtils.getFirstChildElement(this.transformElem));
    }

    private void unmarshalParams(Element paramsElem) {
        String xPath = paramsElem.getFirstChild().getNodeValue();
        NamedNodeMap attributes = paramsElem.getAttributes();
        if (attributes != null) {
            int length = attributes.getLength();
            HashMap<String, String> namespaceMap = new HashMap<String, String>(length);
            for (int i = 0; i < length; ++i) {
                Attr attr = (Attr)attributes.item(i);
                String prefix = attr.getPrefix();
                if (prefix == null || !prefix.equals("xmlns")) continue;
                namespaceMap.put(attr.getLocalName(), attr.getValue());
            }
            this.params = new XPathFilterParameterSpec(xPath, namespaceMap);
        } else {
            this.params = new XPathFilterParameterSpec(xPath);
        }
    }

    public void marshalParams(XMLStructure parent, XMLCryptoContext context) throws MarshalException {
        super.marshalParams(parent, context);
        XPathFilterParameterSpec xp = (XPathFilterParameterSpec)this.getParameterSpec();
        Element xpathElem = DOMUtils.createElement(this.ownerDoc, "XPath", "http://www.w3.org/2000/09/xmldsig#", DOMUtils.getSignaturePrefix(context));
        xpathElem.appendChild(this.ownerDoc.createTextNode(xp.getXPath()));
        Set entries = xp.getNamespaceMap().entrySet();
        for (Map.Entry entry : entries) {
            xpathElem.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:" + (String)entry.getKey(), (String)entry.getValue());
        }
        this.transformElem.appendChild(xpathElem);
    }
}

