/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.lib;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLConnection;
import java.net.UnknownServiceException;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;
import net.sf.saxon.event.PipelineConfiguration;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ResultDocumentResolver;
import net.sf.saxon.lib.SerializerFactory;
import net.sf.saxon.serialize.SerializationProperties;
import net.sf.saxon.trans.XPathException;

public class StandardResultDocumentResolver
implements ResultDocumentResolver {
    private static StandardResultDocumentResolver theInstance = new StandardResultDocumentResolver();

    public static StandardResultDocumentResolver getInstance() {
        return theInstance;
    }

    @Override
    public Receiver resolve(XPathContext context, String href, String baseUri, SerializationProperties properties) throws XPathException {
        StreamResult result = this.resolve(href, baseUri);
        SerializerFactory factory = context.getConfiguration().getSerializerFactory();
        PipelineConfiguration pipe = context.getController().makePipelineConfiguration();
        return factory.getReceiver((Result)result, properties, pipe);
    }

    public StreamResult resolve(String href, String base) throws XPathException {
        String which = "base";
        try {
            URI absoluteURI;
            if (href.isEmpty()) {
                if (base == null) {
                    throw new XPathException("The system identifier of the principal output file is unknown");
                }
                absoluteURI = new URI(base);
            } else {
                which = "relative";
                absoluteURI = new URI(href);
            }
            if (!absoluteURI.isAbsolute()) {
                if (base == null) {
                    throw new XPathException("The system identifier of the principal output file is unknown");
                }
                which = "base";
                URI baseURI = new URI(base);
                which = "relative";
                absoluteURI = baseURI.resolve(href);
            }
            return this.createResult(absoluteURI);
        } catch (URISyntaxException err) {
            throw new XPathException("Invalid syntax for " + which + " URI", err);
        } catch (IllegalArgumentException err2) {
            throw new XPathException("Invalid " + which + " URI syntax", err2);
        } catch (MalformedURLException err3) {
            throw new XPathException("Resolved URL is malformed", err3);
        } catch (UnknownServiceException err5) {
            throw new XPathException("Specified protocol does not allow output", err5);
        } catch (IOException err4) {
            throw new XPathException("Cannot open connection to specified URL", err4);
        }
    }

    protected StreamResult createResult(URI absoluteURI) throws XPathException, IOException {
        if ("file".equals(absoluteURI.getScheme())) {
            return StandardResultDocumentResolver.makeOutputFile(absoluteURI);
        }
        URLConnection connection = absoluteURI.toURL().openConnection();
        connection.setDoInput(false);
        connection.setDoOutput(true);
        connection.connect();
        OutputStream stream = connection.getOutputStream();
        StreamResult result = new StreamResult(stream);
        result.setSystemId(absoluteURI.toASCIIString());
        return result;
    }

    public static synchronized StreamResult makeOutputFile(URI absoluteURI) throws XPathException {
        try {
            File outputFile = new File(absoluteURI);
            if (outputFile.isDirectory()) {
                throw new XPathException("Cannot write to a directory: " + absoluteURI, "SXRD0004");
            }
            if (outputFile.exists() && !outputFile.canWrite()) {
                throw new XPathException("Cannot write to URI " + absoluteURI, "SXRD0004");
            }
            return new StreamResult(outputFile);
        } catch (IllegalArgumentException err) {
            throw new XPathException("Cannot write to URI " + absoluteURI + " (" + err.getMessage() + ")");
        }
    }

    public void close(Result result) throws XPathException {
        if (result instanceof StreamResult) {
            Writer writer;
            OutputStream stream = ((StreamResult)result).getOutputStream();
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException err) {
                    throw new XPathException("Failed while closing output file", err);
                }
            }
            if ((writer = ((StreamResult)result).getWriter()) != null) {
                try {
                    writer.close();
                } catch (IOException err) {
                    throw new XPathException("Failed while closing output file", err);
                }
            }
        }
    }
}

