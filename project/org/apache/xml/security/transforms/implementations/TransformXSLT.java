/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.xml.security.transforms.implementations;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xml.security.exceptions.XMLSecurityException;
import org.apache.xml.security.signature.XMLSignatureInput;
import org.apache.xml.security.transforms.Transform;
import org.apache.xml.security.transforms.TransformSpi;
import org.apache.xml.security.transforms.TransformationException;
import org.apache.xml.security.utils.XMLUtils;
import org.w3c.dom.Element;

public class TransformXSLT
extends TransformSpi {
    public static final String implementedTransformURI = "http://www.w3.org/TR/1999/REC-xslt-19991116";
    static final String XSLTSpecNS = "http://www.w3.org/1999/XSL/Transform";
    static final String defaultXSLTSpecNSprefix = "xslt";
    static final String XSLTSTYLESHEET = "stylesheet";
    private static Log log = LogFactory.getLog(TransformXSLT.class);

    protected String engineGetURI() {
        return implementedTransformURI;
    }

    protected XMLSignatureInput enginePerformTransform(XMLSignatureInput input, OutputStream baos, Transform transformObject) throws IOException, TransformationException {
        try {
            Element transformElement = transformObject.getElement();
            Element xsltElement = XMLUtils.selectNode(transformElement.getFirstChild(), XSLTSpecNS, XSLTSTYLESHEET, 0);
            if (xsltElement == null) {
                Object[] exArgs = new Object[]{"xslt:stylesheet", "Transform"};
                throw new TransformationException("xml.WrongContent", exArgs);
            }
            TransformerFactory tFactory = TransformerFactory.newInstance();
            tFactory.setFeature("http://javax.xml.XMLConstants/feature/secure-processing", Boolean.TRUE);
            StreamSource xmlSource = new StreamSource(new ByteArrayInputStream(input.getBytes()));
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            Transformer transformer = tFactory.newTransformer();
            DOMSource source = new DOMSource(xsltElement);
            StreamResult result = new StreamResult(os);
            transformer.transform(source, result);
            StreamSource stylesheet = new StreamSource(new ByteArrayInputStream(os.toByteArray()));
            Transformer transformer2 = tFactory.newTransformer(stylesheet);
            try {
                transformer2.setOutputProperty("{http://xml.apache.org/xalan}line-separator", "\n");
            } catch (Exception e) {
                log.warn("Unable to set Xalan line-separator property: " + e.getMessage());
            }
            if (baos == null) {
                ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
                StreamResult outputTarget = new StreamResult(baos1);
                transformer2.transform(xmlSource, outputTarget);
                XMLSignatureInput output = new XMLSignatureInput(baos1.toByteArray());
                output.setSecureValidation(this.secureValidation);
                return output;
            }
            StreamResult outputTarget = new StreamResult(baos);
            transformer2.transform(xmlSource, outputTarget);
            XMLSignatureInput output = new XMLSignatureInput((byte[])null);
            output.setSecureValidation(this.secureValidation);
            output.setOutputStream(baos);
            return output;
        } catch (XMLSecurityException ex) {
            Object[] exArgs = new Object[]{ex.getMessage()};
            throw new TransformationException("generic.EmptyMessage", exArgs, ex);
        } catch (TransformerConfigurationException ex) {
            Object[] exArgs = new Object[]{ex.getMessage()};
            throw new TransformationException("generic.EmptyMessage", exArgs, ex);
        } catch (TransformerException ex) {
            Object[] exArgs = new Object[]{ex.getMessage()};
            throw new TransformationException("generic.EmptyMessage", exArgs, ex);
        }
    }
}

