/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 * 
 * Could not load the following classes:
 *  com.sun.msv.datatype.xsd.XSDatatype
 */
package org.dom4j.datatype;

import com.sun.msv.datatype.xsd.XSDatatype;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.QName;
import org.dom4j.datatype.DatatypeElementFactory;

class NamedTypeResolver {
    protected Map complexTypeMap = new HashMap();
    protected Map simpleTypeMap = new HashMap();
    protected Map typedElementMap = new HashMap();
    protected Map elementFactoryMap = new HashMap();
    protected DocumentFactory documentFactory;

    NamedTypeResolver(DocumentFactory documentFactory) {
        this.documentFactory = documentFactory;
    }

    void registerComplexType(QName type, DocumentFactory factory) {
        this.complexTypeMap.put(type, factory);
    }

    void registerSimpleType(QName type, XSDatatype datatype) {
        this.simpleTypeMap.put(type, datatype);
    }

    void registerTypedElement(Element element, QName type, DocumentFactory parentFactory) {
        this.typedElementMap.put(element, type);
        this.elementFactoryMap.put(element, parentFactory);
    }

    void resolveElementTypes() {
        Iterator iterator = this.typedElementMap.keySet().iterator();
        while (iterator.hasNext()) {
            Element element = (Element)iterator.next();
            QName elementQName = this.getQNameOfSchemaElement(element);
            QName type = (QName)this.typedElementMap.get(element);
            if (this.complexTypeMap.containsKey(type)) {
                DocumentFactory factory = (DocumentFactory)this.complexTypeMap.get(type);
                elementQName.setDocumentFactory(factory);
                continue;
            }
            if (!this.simpleTypeMap.containsKey(type)) continue;
            XSDatatype datatype = (XSDatatype)this.simpleTypeMap.get(type);
            DocumentFactory factory = (DocumentFactory)this.elementFactoryMap.get(element);
            if (!(factory instanceof DatatypeElementFactory)) continue;
            ((DatatypeElementFactory)factory).setChildElementXSDatatype(elementQName, datatype);
        }
    }

    void resolveNamedTypes() {
        this.resolveElementTypes();
    }

    private QName getQNameOfSchemaElement(Element element) {
        String name = element.attributeValue("name");
        return this.getQName(name);
    }

    private QName getQName(String name) {
        return this.documentFactory.createQName(name);
    }
}

