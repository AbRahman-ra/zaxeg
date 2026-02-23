/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.lib;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import javax.xml.transform.stream.StreamSource;
import net.sf.saxon.Configuration;
import net.sf.saxon.functions.ResolveURI;
import net.sf.saxon.lib.ModuleURIResolver;
import net.sf.saxon.trans.XPathException;

public class StandardModuleURIResolver
implements ModuleURIResolver {
    Configuration config;

    public StandardModuleURIResolver() {
    }

    public StandardModuleURIResolver(Configuration config) {
        this.config = config;
    }

    @Override
    public StreamSource[] resolve(String moduleURI, String baseURI, String[] locations) throws XPathException {
        if (locations.length == 0) {
            XPathException err = new XPathException("Cannot locate module for namespace " + moduleURI);
            err.setErrorCode("XQST0059");
            err.setIsStaticError(true);
            throw err;
        }
        StreamSource[] sources = new StreamSource[locations.length];
        for (int m = 0; m < locations.length; ++m) {
            URI absoluteURI;
            String href = locations[m];
            try {
                absoluteURI = ResolveURI.makeAbsolute(href, baseURI);
            } catch (URISyntaxException err) {
                XPathException se = new XPathException("Cannot resolve relative URI " + href, err);
                se.setErrorCode("XQST0059");
                se.setIsStaticError(true);
                throw se;
            }
            if (this.config != null && !this.config.getAllowedUriTest().test(absoluteURI)) {
                throw new XPathException("URI scheme '" + absoluteURI.getScheme() + "' has been disallowed");
            }
            sources[m] = this.getQuerySource(absoluteURI);
        }
        return sources;
    }

    protected StreamSource getQuerySource(URI absoluteURI) throws XPathException {
        String encoding = null;
        try {
            InputStream is;
            if ("classpath".equals(absoluteURI.getScheme())) {
                String path = absoluteURI.getPath();
                is = this.config.getDynamicLoader().getResourceAsStream(path);
                if (is == null) {
                    XPathException se = new XPathException("Cannot locate module " + path + " on class path");
                    se.setErrorCode("XQST0059");
                    se.setIsStaticError(true);
                    throw se;
                }
            } else {
                int pos;
                String contentType;
                URL absoluteURL = absoluteURI.toURL();
                URLConnection connection = absoluteURL.openConnection();
                connection.connect();
                is = connection.getInputStream();
                if (!"file".equals(connection.getURL().getProtocol()) && (contentType = connection.getContentType()) != null && (pos = contentType.indexOf("charset")) >= 0) {
                    if ((pos = contentType.indexOf(61, pos + 7)) >= 0) {
                        contentType = contentType.substring(pos + 1);
                    }
                    if ((pos = contentType.indexOf(59)) > 0) {
                        contentType = contentType.substring(0, pos);
                    }
                    if ((pos = contentType.indexOf(40)) > 0) {
                        contentType = contentType.substring(0, pos);
                    }
                    if ((pos = contentType.indexOf(34)) > 0) {
                        contentType = contentType.substring(pos + 1, contentType.indexOf(34, pos + 2));
                    }
                    encoding = contentType.trim();
                }
            }
            if (!is.markSupported()) {
                is = new BufferedInputStream(is);
            }
            StreamSource ss = new StreamSource();
            if (encoding == null) {
                ss.setInputStream(is);
            } else {
                ss.setReader(new InputStreamReader(is, encoding));
            }
            ss.setSystemId(absoluteURI.toString());
            return ss;
        } catch (IOException err) {
            XPathException se = new XPathException(err);
            se.setErrorCode("XQST0059");
            se.setIsStaticError(true);
            throw se;
        }
    }
}

