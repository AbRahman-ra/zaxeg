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

public class XPathFilterCHGPContainer
extends ElementProxy
implements TransformParam {
    public static final String TRANSFORM_XPATHFILTERCHGP = "http://www.nue.et-inf.uni-siegen.de/~geuer-pollmann/#xpathFilter";
    private static final String _TAG_INCLUDE_BUT_SEARCH = "IncludeButSearch";
    private static final String _TAG_EXCLUDE_BUT_SEARCH = "ExcludeButSearch";
    private static final String _TAG_EXCLUDE = "Exclude";
    public static final String _TAG_XPATHCHGP = "XPathAlternative";
    public static final String _ATT_INCLUDESLASH = "IncludeSlashPolicy";
    public static final boolean IncludeSlash = true;
    public static final boolean ExcludeSlash = false;

    private XPathFilterCHGPContainer() {
    }

    private XPathFilterCHGPContainer(Document doc, boolean includeSlashPolicy, String includeButSearch, String excludeButSearch, String exclude) {
        super(doc);
        if (includeSlashPolicy) {
            this.constructionElement.setAttributeNS(null, _ATT_INCLUDESLASH, "true");
        } else {
            this.constructionElement.setAttributeNS(null, _ATT_INCLUDESLASH, "false");
        }
        if (includeButSearch != null && includeButSearch.trim().length() > 0) {
            Element includeButSearchElem = ElementProxy.createElementForFamily(doc, this.getBaseNamespace(), _TAG_INCLUDE_BUT_SEARCH);
            includeButSearchElem.appendChild(this.doc.createTextNode(XPathFilterCHGPContainer.indentXPathText(includeButSearch)));
            XMLUtils.addReturnToElement(this.constructionElement);
            this.constructionElement.appendChild(includeButSearchElem);
        }
        if (excludeButSearch != null && excludeButSearch.trim().length() > 0) {
            Element excludeButSearchElem = ElementProxy.createElementForFamily(doc, this.getBaseNamespace(), _TAG_EXCLUDE_BUT_SEARCH);
            excludeButSearchElem.appendChild(this.doc.createTextNode(XPathFilterCHGPContainer.indentXPathText(excludeButSearch)));
            XMLUtils.addReturnToElement(this.constructionElement);
            this.constructionElement.appendChild(excludeButSearchElem);
        }
        if (exclude != null && exclude.trim().length() > 0) {
            Element excludeElem = ElementProxy.createElementForFamily(doc, this.getBaseNamespace(), _TAG_EXCLUDE);
            excludeElem.appendChild(this.doc.createTextNode(XPathFilterCHGPContainer.indentXPathText(exclude)));
            XMLUtils.addReturnToElement(this.constructionElement);
            this.constructionElement.appendChild(excludeElem);
        }
        XMLUtils.addReturnToElement(this.constructionElement);
    }

    static String indentXPathText(String xp) {
        if (xp.length() > 2 && !Character.isWhitespace(xp.charAt(0))) {
            return "\n" + xp + "\n";
        }
        return xp;
    }

    private XPathFilterCHGPContainer(Element element, String BaseURI) throws XMLSecurityException {
        super(element, BaseURI);
    }

    public static XPathFilterCHGPContainer getInstance(Document doc, boolean includeSlashPolicy, String includeButSearch, String excludeButSearch, String exclude) {
        return new XPathFilterCHGPContainer(doc, includeSlashPolicy, includeButSearch, excludeButSearch, exclude);
    }

    public static XPathFilterCHGPContainer getInstance(Element element, String BaseURI) throws XMLSecurityException {
        return new XPathFilterCHGPContainer(element, BaseURI);
    }

    private String getXStr(String type) {
        if (this.length(this.getBaseNamespace(), type) != 1) {
            return "";
        }
        Element xElem = XMLUtils.selectNode(this.constructionElement.getFirstChild(), this.getBaseNamespace(), type, 0);
        return XMLUtils.getFullTextChildrenFromElement(xElem);
    }

    public String getIncludeButSearch() {
        return this.getXStr(_TAG_INCLUDE_BUT_SEARCH);
    }

    public String getExcludeButSearch() {
        return this.getXStr(_TAG_EXCLUDE_BUT_SEARCH);
    }

    public String getExclude() {
        return this.getXStr(_TAG_EXCLUDE);
    }

    public boolean getIncludeSlashPolicy() {
        return this.constructionElement.getAttributeNS(null, _ATT_INCLUDESLASH).equals("true");
    }

    private Node getHereContextNode(String type) {
        if (this.length(this.getBaseNamespace(), type) != 1) {
            return null;
        }
        return XMLUtils.selectNodeText(this.constructionElement.getFirstChild(), this.getBaseNamespace(), type, 0);
    }

    public Node getHereContextNodeIncludeButSearch() {
        return this.getHereContextNode(_TAG_INCLUDE_BUT_SEARCH);
    }

    public Node getHereContextNodeExcludeButSearch() {
        return this.getHereContextNode(_TAG_EXCLUDE_BUT_SEARCH);
    }

    public Node getHereContextNodeExclude() {
        return this.getHereContextNode(_TAG_EXCLUDE);
    }

    public final String getBaseLocalName() {
        return _TAG_XPATHCHGP;
    }

    public final String getBaseNamespace() {
        return TRANSFORM_XPATHFILTERCHGP;
    }
}

