/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.xml.security.utils.resolver.implementations;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xml.security.signature.XMLSignatureInput;
import org.apache.xml.security.utils.Base64;
import org.apache.xml.security.utils.resolver.ResourceResolverContext;
import org.apache.xml.security.utils.resolver.ResourceResolverException;
import org.apache.xml.security.utils.resolver.ResourceResolverSpi;

public class ResolverDirectHTTP
extends ResourceResolverSpi {
    private static Log log = LogFactory.getLog(ResolverDirectHTTP.class);
    private static final String[] properties = new String[]{"http.proxy.host", "http.proxy.port", "http.proxy.username", "http.proxy.password", "http.basic.username", "http.basic.password"};
    private static final int HttpProxyHost = 0;
    private static final int HttpProxyPort = 1;
    private static final int HttpProxyUser = 2;
    private static final int HttpProxyPass = 3;
    private static final int HttpBasicUser = 4;
    private static final int HttpBasicPass = 5;

    public boolean engineIsThreadSafe() {
        return true;
    }

    public XMLSignatureInput engineResolveURI(ResourceResolverContext context) throws ResourceResolverException {
        InputStream inputStream = null;
        try {
            URI uriNew = ResolverDirectHTTP.getNewURI(context.uriToResolve, context.baseUri);
            URL url = uriNew.toURL();
            URLConnection urlConnection = this.openConnection(url);
            String auth = urlConnection.getHeaderField("WWW-Authenticate");
            if (auth != null && auth.startsWith("Basic")) {
                String user = this.engineGetProperty(properties[4]);
                String pass = this.engineGetProperty(properties[5]);
                if (user != null && pass != null) {
                    urlConnection = this.openConnection(url);
                    String password = user + ":" + pass;
                    String encodedPassword = Base64.encode(password.getBytes("ISO-8859-1"));
                    urlConnection.setRequestProperty("Authorization", "Basic " + encodedPassword);
                }
            }
            String mimeType = urlConnection.getHeaderField("Content-Type");
            inputStream = urlConnection.getInputStream();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buf = new byte[4096];
            int read = 0;
            int summarized = 0;
            while ((read = inputStream.read(buf)) >= 0) {
                baos.write(buf, 0, read);
                summarized += read;
            }
            if (log.isDebugEnabled()) {
                log.debug("Fetched " + summarized + " bytes from URI " + uriNew.toString());
            }
            XMLSignatureInput result = new XMLSignatureInput(baos.toByteArray());
            result.setSecureValidation(context.secureValidation);
            result.setSourceURI(uriNew.toString());
            result.setMIMEType(mimeType);
            XMLSignatureInput xMLSignatureInput = result;
            return xMLSignatureInput;
        } catch (URISyntaxException ex) {
            throw new ResourceResolverException("generic.EmptyMessage", ex, context.attr, context.baseUri);
        } catch (MalformedURLException ex) {
            throw new ResourceResolverException("generic.EmptyMessage", ex, context.attr, context.baseUri);
        } catch (IOException ex) {
            throw new ResourceResolverException("generic.EmptyMessage", ex, context.attr, context.baseUri);
        } catch (IllegalArgumentException e) {
            throw new ResourceResolverException("generic.EmptyMessage", e, context.attr, context.baseUri);
        } finally {
            block17: {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        if (!log.isDebugEnabled()) break block17;
                        log.debug(e.getMessage(), e);
                    }
                }
            }
        }
    }

    private URLConnection openConnection(URL url) throws IOException {
        URLConnection urlConnection;
        String proxyHostProp = this.engineGetProperty(properties[0]);
        String proxyPortProp = this.engineGetProperty(properties[1]);
        String proxyUser = this.engineGetProperty(properties[2]);
        String proxyPass = this.engineGetProperty(properties[3]);
        Proxy proxy = null;
        if (proxyHostProp != null && proxyPortProp != null) {
            int port = Integer.parseInt(proxyPortProp);
            proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHostProp, port));
        }
        if (proxy != null) {
            urlConnection = url.openConnection(proxy);
            if (proxyUser != null && proxyPass != null) {
                String password = proxyUser + ":" + proxyPass;
                String authString = "Basic " + Base64.encode(password.getBytes("ISO-8859-1"));
                urlConnection.setRequestProperty("Proxy-Authorization", authString);
            }
        } else {
            urlConnection = url.openConnection();
        }
        return urlConnection;
    }

    public boolean engineCanResolveURI(ResourceResolverContext context) {
        if (context.uriToResolve == null) {
            if (log.isDebugEnabled()) {
                log.debug("quick fail, uri == null");
            }
            return false;
        }
        if (context.uriToResolve.equals("") || context.uriToResolve.charAt(0) == '#') {
            if (log.isDebugEnabled()) {
                log.debug("quick fail for empty URIs and local ones");
            }
            return false;
        }
        if (log.isDebugEnabled()) {
            log.debug("I was asked whether I can resolve " + context.uriToResolve);
        }
        if (context.uriToResolve.startsWith("http:") || context.baseUri != null && context.baseUri.startsWith("http:")) {
            if (log.isDebugEnabled()) {
                log.debug("I state that I can resolve " + context.uriToResolve);
            }
            return true;
        }
        if (log.isDebugEnabled()) {
            log.debug("I state that I can't resolve " + context.uriToResolve);
        }
        return false;
    }

    public String[] engineGetPropertyKeys() {
        return (String[])properties.clone();
    }

    private static URI getNewURI(String uri, String baseURI) throws URISyntaxException {
        URI newUri = null;
        newUri = baseURI == null || "".equals(baseURI) ? new URI(uri) : new URI(baseURI).resolve(uri);
        if (newUri.getFragment() != null) {
            URI uriNewNoFrag = new URI(newUri.getScheme(), newUri.getSchemeSpecificPart(), null);
            return uriNewNoFrag;
        }
        return newUri;
    }
}

