/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.jcp.xml.dsig.internal.dom;

import java.security.InvalidAlgorithmParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.crypto.MarshalException;
import javax.xml.crypto.XMLCryptoContext;
import javax.xml.crypto.XMLStructure;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import javax.xml.crypto.dsig.spec.XPathFilter2ParameterSpec;
import javax.xml.crypto.dsig.spec.XPathType;
import org.apache.jcp.xml.dsig.internal.dom.ApacheTransform;
import org.apache.jcp.xml.dsig.internal.dom.DOMUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

public final class DOMXPathFilter2Transform
extends ApacheTransform {
    public void init(TransformParameterSpec params) throws InvalidAlgorithmParameterException {
        if (params == null) {
            throw new InvalidAlgorithmParameterException("params are required");
        }
        if (!(params instanceof XPathFilter2ParameterSpec)) {
            throw new InvalidAlgorithmParameterException("params must be of type XPathFilter2ParameterSpec");
        }
        this.params = params;
    }

    public void init(XMLStructure parent, XMLCryptoContext context) throws InvalidAlgorithmParameterException {
        super.init(parent, context);
        try {
            this.unmarshalParams(DOMUtils.getFirstChildElement(this.transformElem));
        } catch (MarshalException me) {
            throw new InvalidAlgorithmParameterException(me);
        }
    }

    private void unmarshalParams(Element curXPathElem) throws MarshalException {
        ArrayList<XPathType> list = new ArrayList<XPathType>();
        while (curXPathElem != null) {
            String xPath = curXPathElem.getFirstChild().getNodeValue();
            String filterVal = DOMUtils.getAttributeValue(curXPathElem, "Filter");
            if (filterVal == null) {
                throw new MarshalException("filter cannot be null");
            }
            XPathType.Filter filter = null;
            if (filterVal.equals("intersect")) {
                filter = XPathType.Filter.INTERSECT;
            } else if (filterVal.equals("subtract")) {
                filter = XPathType.Filter.SUBTRACT;
            } else if (filterVal.equals("union")) {
                filter = XPathType.Filter.UNION;
            } else {
                throw new MarshalException("Unknown XPathType filter type" + filterVal);
            }
            NamedNodeMap attributes = curXPathElem.getAttributes();
            if (attributes != null) {
                int length = attributes.getLength();
                HashMap<String, String> namespaceMap = new HashMap<String, String>(length);
                for (int i = 0; i < length; ++i) {
                    Attr attr = (Attr)attributes.item(i);
                    String prefix = attr.getPrefix();
                    if (prefix == null || !prefix.equals("xmlns")) continue;
                    namespaceMap.put(attr.getLocalName(), attr.getValue());
                }
                list.add(new XPathType(xPath, filter, namespaceMap));
            } else {
                list.add(new XPathType(xPath, filter));
            }
            curXPathElem = DOMUtils.getNextSiblingElement(curXPathElem);
        }
        this.params = new XPathFilter2ParameterSpec(list);
    }

    public void marshalParams(XMLStructure parent, XMLCryptoContext context) throws MarshalException {
        super.marshalParams(parent, context);
        XPathFilter2ParameterSpec xp = (XPathFilter2ParameterSpec)this.getParameterSpec();
        String prefix = DOMUtils.getNSPrefix(context, "http://www.w3.org/2002/06/xmldsig-filter2");
        String qname = prefix == null || prefix.length() == 0 ? "xmlns" : "xmlns:" + prefix;
        List xpathList = xp.getXPathList();
        for (XPathType xpathType : xpathList) {
            Element elem = DOMUtils.createElement(this.ownerDoc, "XPath", "http://www.w3.org/2002/06/xmldsig-filter2", prefix);
            elem.appendChild(this.ownerDoc.createTextNode(xpathType.getExpression()));
            DOMUtils.setAttribute(elem, "Filter", xpathType.getFilter().toString());
            elem.setAttributeNS("http://www.w3.org/2000/xmlns/", qname, "http://www.w3.org/2002/06/xmldsig-filter2");
            Set entries = xpathType.getNamespaceMap().entrySet();
            for (Map.Entry entry : entries) {
                elem.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:" + (String)entry.getKey(), (String)entry.getValue());
            }
            this.transformElem.appendChild(elem);
        }
    }
}

