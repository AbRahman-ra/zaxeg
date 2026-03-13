/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.jaxp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamResult;
import net.sf.saxon.Configuration;
import net.sf.saxon.Controller;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.expr.JPConverter;
import net.sf.saxon.expr.XPathContextMajor;
import net.sf.saxon.expr.parser.Loc;
import net.sf.saxon.expr.parser.RoleDiagnostic;
import net.sf.saxon.jaxp.IdentityTransformer;
import net.sf.saxon.jaxp.ReceivingDestination;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.s9api.AbstractDestination;
import net.sf.saxon.s9api.DOMDestination;
import net.sf.saxon.s9api.Destination;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SAXDestination;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XdmValue;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.JavaExternalObjectType;
import net.sf.saxon.value.EmptySequence;
import net.sf.saxon.value.ObjectValue;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.UntypedAtomicValue;
import org.w3c.dom.Node;
import org.xml.sax.XMLFilter;

abstract class AbstractTransformerImpl
extends IdentityTransformer {
    private XsltExecutable xsltExecutable;
    private Map<String, Object> parameters = new HashMap<String, Object>(8);

    AbstractTransformerImpl(XsltExecutable e) {
        super(e.getProcessor().getUnderlyingConfiguration());
        this.xsltExecutable = e;
    }

    Destination makeDestination(Result outputTarget) throws XPathException {
        AbstractDestination destination;
        if (outputTarget instanceof StreamResult) {
            StreamResult sr = (StreamResult)outputTarget;
            if (sr.getOutputStream() != null) {
                destination = this.xsltExecutable.getProcessor().newSerializer(sr.getOutputStream());
            } else if (sr.getWriter() != null) {
                destination = this.xsltExecutable.getProcessor().newSerializer(sr.getWriter());
            } else if (sr.getSystemId() != null) {
                FileOutputStream stream;
                URI uri;
                try {
                    uri = new URI(sr.getSystemId());
                } catch (URISyntaxException e) {
                    throw new XPathException("System ID in Result object is not a valid URI: " + sr.getSystemId(), e);
                }
                if (!uri.isAbsolute()) {
                    try {
                        uri = new File(sr.getSystemId()).getAbsoluteFile().toURI();
                    } catch (Exception e) {
                        // empty catch block
                    }
                }
                File file = new File(uri);
                try {
                    if ("file".equals(uri.getScheme()) && !file.exists()) {
                        File directory = file.getParentFile();
                        if (directory != null && !directory.exists()) {
                            directory.mkdirs();
                        }
                        file.createNewFile();
                    }
                } catch (IOException err) {
                    throw new XPathException("Failed to create output file " + uri, err);
                }
                try {
                    stream = new FileOutputStream(file);
                } catch (FileNotFoundException e) {
                    throw new XPathException("Failed to create output file", e);
                }
                destination = this.xsltExecutable.getProcessor().newSerializer(stream);
                ((Serializer)destination).setCloseOnCompletion(true);
            } else {
                throw new IllegalArgumentException("StreamResult supplies neither an OutputStream nor a Writer");
            }
            Properties localOutputProperties = this.getLocalOutputProperties();
            for (String key : localOutputProperties.stringPropertyNames()) {
                QName propertyName = QName.fromClarkName(key);
                if (propertyName.getNamespaceURI().equals("http://saxon.sf.net/") && propertyName.getLocalName().equals("next-in-chain")) continue;
                ((Serializer)destination).setOutputProperty(QName.fromClarkName(key), localOutputProperties.getProperty(key));
            }
        } else if (outputTarget instanceof SAXResult) {
            destination = new SAXDestination(((SAXResult)outputTarget).getHandler());
        } else if (outputTarget instanceof DOMResult) {
            Node root = ((DOMResult)outputTarget).getNode();
            if (root == null) {
                try {
                    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                    dbf.setNamespaceAware(true);
                    root = dbf.newDocumentBuilder().newDocument();
                    ((DOMResult)outputTarget).setNode(root);
                } catch (ParserConfigurationException e) {
                    throw new XPathException(e);
                }
            }
            destination = new DOMDestination(root);
        } else if (outputTarget instanceof Receiver) {
            destination = new ReceivingDestination((Receiver)outputTarget);
        } else {
            return null;
        }
        return destination;
    }

    @Override
    public void setParameter(String name, Object value) {
        Sequence converted;
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(value, "value");
        this.parameters.put(name, value);
        QName qName = QName.fromClarkName(name);
        XsltExecutable.ParameterDetails details = this.xsltExecutable.getGlobalParameters().get(qName);
        if (details == null) {
            return;
        }
        Configuration config = this.getConfiguration();
        SequenceType required = details.getUnderlyingDeclaredType();
        try {
            if (value instanceof Sequence) {
                converted = (EmptySequence)value;
            } else if (value instanceof String) {
                converted = new UntypedAtomicValue((String)value);
            } else if (required.getPrimaryType() instanceof JavaExternalObjectType) {
                converted = new ObjectValue<Object>(value);
            } else {
                JPConverter converter = JPConverter.allocate(value.getClass(), null, config);
                XPathContextMajor context = this.getUnderlyingController().newXPathContext();
                converted = converter.convert(value, context);
            }
            if (converted == null) {
                converted = EmptySequence.getInstance();
            }
            if (required != null && !required.matches(converted, config.getTypeHierarchy())) {
                RoleDiagnostic role = new RoleDiagnostic(3, qName.toString(), -1);
                converted = config.getTypeHierarchy().applyFunctionConversionRules(converted, required, role, Loc.NONE);
            }
        } catch (XPathException e) {
            throw new IllegalArgumentException(e);
        }
        this.setConvertedParameter(qName, XdmValue.wrap(converted));
    }

    protected abstract void setConvertedParameter(QName var1, XdmValue var2);

    @Override
    public Object getParameter(String name) {
        return this.parameters.get(name);
    }

    @Override
    public void clearParameters() {
        this.parameters.clear();
    }

    @Override
    protected Properties getStylesheetOutputProperties() {
        return this.xsltExecutable.getUnderlyingCompiledStylesheet().getPrimarySerializationProperties().getProperties();
    }

    public XsltExecutable getUnderlyingXsltExecutable() {
        return this.xsltExecutable;
    }

    public abstract Controller getUnderlyingController();

    public abstract XMLFilter newXMLFilter();
}

