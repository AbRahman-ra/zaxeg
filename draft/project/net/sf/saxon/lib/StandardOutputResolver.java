/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.lib;

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
import net.sf.saxon.lib.OutputURIResolver;
import net.sf.saxon.lib.StandardResultDocumentResolver;
import net.sf.saxon.trans.XPathException;

public class StandardOutputResolver
implements OutputURIResolver {
    private static StandardOutputResolver theInstance = new StandardOutputResolver();

    public static StandardOutputResolver getInstance() {
        return theInstance;
    }

    @Override
    public StandardOutputResolver newInstance() {
        return this;
    }

    @Override
    public Result resolve(String href, String base) throws XPathException {
        String which = "base";
        try {
            URI absoluteURI;
            if (href.isEmpty()) {
                if (base == null) {
                    throw new XPathException("The system identifier of the principal output file is unknown", "SXRD0002");
                }
                absoluteURI = new URI(base);
            } else {
                which = "relative";
                absoluteURI = new URI(href);
            }
            if (!absoluteURI.isAbsolute()) {
                if (base == null) {
                    throw new XPathException("The system identifier of the principal output file is unknown", "SXRD0002");
                }
                which = "base";
                URI baseURI = new URI(base);
                which = "relative";
                absoluteURI = baseURI.resolve(href);
            }
            return this.createResult(absoluteURI);
        } catch (URISyntaxException err) {
            XPathException xe = new XPathException("Invalid syntax for " + which + " URI");
            xe.setErrorCode("SXRD0001");
            throw xe;
        } catch (IllegalArgumentException err2) {
            XPathException xe = new XPathException("Invalid " + which + " URI syntax");
            xe.setErrorCode("SXRD0001");
            throw xe;
        } catch (MalformedURLException err3) {
            XPathException xe = new XPathException("Resolved URL is malformed", err3);
            xe.setErrorCode("SXRD0001");
            throw xe;
        } catch (UnknownServiceException err4) {
            XPathException xe = new XPathException("Specified protocol does not allow output", err4);
            xe.setErrorCode("SXRD0001");
            throw xe;
        } catch (IOException err5) {
            XPathException xe = new XPathException("Cannot open connection to specified URL", err5);
            xe.setErrorCode("SXRD0001");
            throw xe;
        }
    }

    protected Result createResult(URI absoluteURI) throws XPathException, IOException {
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

    @Override
    public void close(Result result) throws XPathException {
        if (result instanceof StreamResult) {
            Writer writer;
            OutputStream stream = ((StreamResult)result).getOutputStream();
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException err) {
                    XPathException xe = new XPathException("Failed while closing output file", err);
                    xe.setErrorCode("SXRD0003");
                    throw xe;
                }
            }
            if ((writer = ((StreamResult)result).getWriter()) != null) {
                try {
                    writer.close();
                } catch (IOException err) {
                    XPathException xe = new XPathException("Failed while closing output file", err);
                    xe.setErrorCode("SXRD0003");
                    throw xe;
                }
            }
        }
    }
}

