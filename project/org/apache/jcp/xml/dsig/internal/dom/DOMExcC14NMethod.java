/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.jcp.xml.dsig.internal.dom;

import java.security.InvalidAlgorithmParameterException;
import java.security.spec.AlgorithmParameterSpec;
import java.util.ArrayList;
import java.util.List;
import javax.xml.crypto.Data;
import javax.xml.crypto.MarshalException;
import javax.xml.crypto.XMLCryptoContext;
import javax.xml.crypto.XMLStructure;
import javax.xml.crypto.dsig.TransformException;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.ExcC14NParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import org.apache.jcp.xml.dsig.internal.dom.ApacheCanonicalizer;
import org.apache.jcp.xml.dsig.internal.dom.DOMSubTreeData;
import org.apache.jcp.xml.dsig.internal.dom.DOMUtils;
import org.apache.jcp.xml.dsig.internal.dom.Utils;
import org.apache.xml.security.c14n.Canonicalizer;
import org.apache.xml.security.c14n.InvalidCanonicalizerException;
import org.w3c.dom.Element;

public final class DOMExcC14NMethod
extends ApacheCanonicalizer {
    public void init(TransformParameterSpec params) throws InvalidAlgorithmParameterException {
        if (params != null) {
            if (!(params instanceof ExcC14NParameterSpec)) {
                throw new InvalidAlgorithmParameterException("params must be of type ExcC14NParameterSpec");
            }
            this.params = (C14NMethodParameterSpec)params;
        }
    }

    public void init(XMLStructure parent, XMLCryptoContext context) throws InvalidAlgorithmParameterException {
        super.init(parent, context);
        Element paramsElem = DOMUtils.getFirstChildElement(this.transformElem);
        if (paramsElem == null) {
            this.params = null;
            this.inclusiveNamespaces = null;
            return;
        }
        this.unmarshalParams(paramsElem);
    }

    private void unmarshalParams(Element paramsElem) {
        String prefixListAttr;
        this.inclusiveNamespaces = prefixListAttr = paramsElem.getAttributeNS(null, "PrefixList");
        int begin = 0;
        int end = prefixListAttr.indexOf(32);
        ArrayList<String> prefixList = new ArrayList<String>();
        while (end != -1) {
            prefixList.add(prefixListAttr.substring(begin, end));
            begin = end + 1;
            end = prefixListAttr.indexOf(32, begin);
        }
        if (begin <= prefixListAttr.length()) {
            prefixList.add(prefixListAttr.substring(begin));
        }
        this.params = new ExcC14NParameterSpec(prefixList);
    }

    public void marshalParams(XMLStructure parent, XMLCryptoContext context) throws MarshalException {
        super.marshalParams(parent, context);
        AlgorithmParameterSpec spec = this.getParameterSpec();
        if (spec == null) {
            return;
        }
        String prefix = DOMUtils.getNSPrefix(context, "http://www.w3.org/2001/10/xml-exc-c14n#");
        Element eElem = DOMUtils.createElement(this.ownerDoc, "InclusiveNamespaces", "http://www.w3.org/2001/10/xml-exc-c14n#", prefix);
        if (prefix == null || prefix.length() == 0) {
            eElem.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns", "http://www.w3.org/2001/10/xml-exc-c14n#");
        } else {
            eElem.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:" + prefix, "http://www.w3.org/2001/10/xml-exc-c14n#");
        }
        ExcC14NParameterSpec params = (ExcC14NParameterSpec)spec;
        StringBuffer prefixListAttr = new StringBuffer("");
        List prefixList = params.getPrefixList();
        int size = prefixList.size();
        for (int i = 0; i < size; ++i) {
            prefixListAttr.append((String)prefixList.get(i));
            if (i >= size - 1) continue;
            prefixListAttr.append(" ");
        }
        DOMUtils.setAttribute(eElem, "PrefixList", prefixListAttr.toString());
        this.inclusiveNamespaces = prefixListAttr.toString();
        this.transformElem.appendChild(eElem);
    }

    public String getParamsNSURI() {
        return "http://www.w3.org/2001/10/xml-exc-c14n#";
    }

    public Data transform(Data data, XMLCryptoContext xc) throws TransformException {
        DOMSubTreeData subTree;
        if (data instanceof DOMSubTreeData && (subTree = (DOMSubTreeData)data).excludeComments()) {
            try {
                this.apacheCanonicalizer = Canonicalizer.getInstance("http://www.w3.org/2001/10/xml-exc-c14n#");
                boolean secVal = Utils.secureValidation(xc);
                this.apacheCanonicalizer.setSecureValidation(secVal);
            } catch (InvalidCanonicalizerException ice) {
                throw new TransformException("Couldn't find Canonicalizer for: http://www.w3.org/2001/10/xml-exc-c14n#: " + ice.getMessage(), ice);
            }
        }
        return this.canonicalize(data, xc);
    }
}

