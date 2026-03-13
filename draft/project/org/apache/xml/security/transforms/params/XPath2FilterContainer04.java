/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.xml.security.transforms.params;

import org.apache.xml.security.exceptions.XMLSecurityException;
import org.apache.xml.security.transforms.TransformParam;
import org.apache.xml.security.utils.ElementProxy;
import org.apache.xml.security.utils.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class XPath2FilterContainer04
extends ElementProxy
implements TransformParam {
    private static final String _ATT_FILTER = "Filter";
    private static final String _ATT_FILTER_VALUE_INTERSECT = "intersect";
    private static final String _ATT_FILTER_VALUE_SUBTRACT = "subtract";
    private static final String _ATT_FILTER_VALUE_UNION = "union";
    public static final String _TAG_XPATH2 = "XPath";
    public static final String XPathFilter2NS = "http://www.w3.org/2002/04/xmldsig-filter2";

    private XPath2FilterContainer04() {
    }

    private XPath2FilterContainer04(Document doc, String xpath2filter, String filterType) {
        super(doc);
        this.constructionElement.setAttributeNS(null, _ATT_FILTER, filterType);
        if (xpath2filter.length() > 2 && !Character.isWhitespace(xpath2filter.charAt(0))) {
            XMLUtils.addReturnToElement(this.constructionElement);
            this.constructionElement.appendChild(doc.createTextNode(xpath2filter));
            XMLUtils.addReturnToElement(this.constructionElement);
        } else {
            this.constructionElement.appendChild(doc.createTextNode(xpath2filter));
        }
    }

    private XPath2FilterContainer04(Element element, String BaseURI) throws XMLSecurityException {
        super(element, BaseURI);
        String filterStr = this.constructionElement.getAttributeNS(null, _ATT_FILTER);
        if (!(filterStr.equals(_ATT_FILTER_VALUE_INTERSECT) || filterStr.equals(_ATT_FILTER_VALUE_SUBTRACT) || filterStr.equals(_ATT_FILTER_VALUE_UNION))) {
            Object[] exArgs = new Object[]{_ATT_FILTER, filterStr, "intersect, subtract or union"};
            throw new XMLSecurityException("attributeValueIllegal", exArgs);
        }
    }

    public static XPath2FilterContainer04 newInstanceIntersect(Document doc, String xpath2filter) {
        return new XPath2FilterContainer04(doc, xpath2filter, _ATT_FILTER_VALUE_INTERSECT);
    }

    public static XPath2FilterContainer04 newInstanceSubtract(Document doc, String xpath2filter) {
        return new XPath2FilterContainer04(doc, xpath2filter, _ATT_FILTER_VALUE_SUBTRACT);
    }

    public static XPath2FilterContainer04 newInstanceUnion(Document doc, String xpath2filter) {
        return new XPath2FilterContainer04(doc, xpath2filter, _ATT_FILTER_VALUE_UNION);
    }

    public static XPath2FilterContainer04 newInstance(Element element, String BaseURI) throws XMLSecurityException {
        return new XPath2FilterContainer04(element, BaseURI);
    }

    public boolean isIntersect() {
        return this.constructionElement.getAttributeNS(null, _ATT_FILTER).equals(_ATT_FILTER_VALUE_INTERSECT);
    }

    public boolean isSubtract() {
        return this.constructionElement.getAttributeNS(null, _ATT_FILTER).equals(_ATT_FILTER_VALUE_SUBTRACT);
    }

    public boolean isUnion() {
        return this.constructionElement.getAttributeNS(null, _ATT_FILTER).equals(_ATT_FILTER_VALUE_UNION);
    }

    public String getXPathFilterStr() {
        return this.getTextFromTextChild();
    }

    public Node getXPathFilterTextNode() {
        for (Node childNode = this.constructionElement.getFirstChild(); childNode != null; childNode = childNode.getNextSibling()) {
            if (childNode.getNodeType() != 3) continue;
            return childNode;
        }
        return null;
    }

    public final String getBaseLocalName() {
        return _TAG_XPATH2;
    }

    public final String getBaseNamespace() {
        return XPathFilter2NS;
    }
}

