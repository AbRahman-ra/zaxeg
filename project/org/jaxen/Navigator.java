/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.jaxen;

import java.io.Serializable;
import java.util.Iterator;
import org.jaxen.FunctionCallException;
import org.jaxen.UnsupportedAxisException;
import org.jaxen.XPath;
import org.jaxen.saxpath.SAXPathException;

public interface Navigator
extends Serializable {
    public Iterator getChildAxisIterator(Object var1) throws UnsupportedAxisException;

    public Iterator getDescendantAxisIterator(Object var1) throws UnsupportedAxisException;

    public Iterator getParentAxisIterator(Object var1) throws UnsupportedAxisException;

    public Iterator getAncestorAxisIterator(Object var1) throws UnsupportedAxisException;

    public Iterator getFollowingSiblingAxisIterator(Object var1) throws UnsupportedAxisException;

    public Iterator getPrecedingSiblingAxisIterator(Object var1) throws UnsupportedAxisException;

    public Iterator getFollowingAxisIterator(Object var1) throws UnsupportedAxisException;

    public Iterator getPrecedingAxisIterator(Object var1) throws UnsupportedAxisException;

    public Iterator getAttributeAxisIterator(Object var1) throws UnsupportedAxisException;

    public Iterator getNamespaceAxisIterator(Object var1) throws UnsupportedAxisException;

    public Iterator getSelfAxisIterator(Object var1) throws UnsupportedAxisException;

    public Iterator getDescendantOrSelfAxisIterator(Object var1) throws UnsupportedAxisException;

    public Iterator getAncestorOrSelfAxisIterator(Object var1) throws UnsupportedAxisException;

    public Object getDocument(String var1) throws FunctionCallException;

    public Object getDocumentNode(Object var1);

    public Object getParentNode(Object var1) throws UnsupportedAxisException;

    public String getElementNamespaceUri(Object var1);

    public String getElementName(Object var1);

    public String getElementQName(Object var1);

    public String getAttributeNamespaceUri(Object var1);

    public String getAttributeName(Object var1);

    public String getAttributeQName(Object var1);

    public String getProcessingInstructionTarget(Object var1);

    public String getProcessingInstructionData(Object var1);

    public boolean isDocument(Object var1);

    public boolean isElement(Object var1);

    public boolean isAttribute(Object var1);

    public boolean isNamespace(Object var1);

    public boolean isComment(Object var1);

    public boolean isText(Object var1);

    public boolean isProcessingInstruction(Object var1);

    public String getCommentStringValue(Object var1);

    public String getElementStringValue(Object var1);

    public String getAttributeStringValue(Object var1);

    public String getNamespaceStringValue(Object var1);

    public String getTextStringValue(Object var1);

    public String getNamespacePrefix(Object var1);

    public String translateNamespacePrefixToUri(String var1, Object var2);

    public XPath parseXPath(String var1) throws SAXPathException;

    public Object getElementById(Object var1, String var2);

    public short getNodeType(Object var1);
}

