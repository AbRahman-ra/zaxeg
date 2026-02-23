/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.xml.security.utils.resolver.implementations;

import java.io.FileInputStream;
import java.net.URI;
import java.net.URISyntaxException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xml.security.signature.XMLSignatureInput;
import org.apache.xml.security.utils.resolver.ResourceResolverContext;
import org.apache.xml.security.utils.resolver.ResourceResolverException;
import org.apache.xml.security.utils.resolver.ResourceResolverSpi;

public class ResolverLocalFilesystem
extends ResourceResolverSpi {
    private static final int FILE_URI_LENGTH = "file:/".length();
    private static Log log = LogFactory.getLog(ResolverLocalFilesystem.class);

    public boolean engineIsThreadSafe() {
        return true;
    }

    public XMLSignatureInput engineResolveURI(ResourceResolverContext context) throws ResourceResolverException {
        try {
            URI uriNew = ResolverLocalFilesystem.getNewURI(context.uriToResolve, context.baseUri);
            String fileName = ResolverLocalFilesystem.translateUriToFilename(uriNew.toString());
            FileInputStream inputStream = new FileInputStream(fileName);
            XMLSignatureInput result = new XMLSignatureInput(inputStream);
            result.setSecureValidation(context.secureValidation);
            result.setSourceURI(uriNew.toString());
            return result;
        } catch (Exception e) {
            throw new ResourceResolverException("generic.EmptyMessage", e, context.attr, context.baseUri);
        }
    }

    private static String translateUriToFilename(String uri) {
        String subStr = uri.substring(FILE_URI_LENGTH);
        if (subStr.indexOf("%20") > -1) {
            int offset = 0;
            int index = 0;
            StringBuilder temp = new StringBuilder(subStr.length());
            do {
                if ((index = subStr.indexOf("%20", offset)) == -1) {
                    temp.append(subStr.substring(offset));
                    continue;
                }
                temp.append(subStr.substring(offset, index));
                temp.append(' ');
                offset = index + 3;
            } while (index != -1);
            subStr = temp.toString();
        }
        if (subStr.charAt(1) == ':') {
            return subStr;
        }
        return "/" + subStr;
    }

    public boolean engineCanResolveURI(ResourceResolverContext context) {
        block8: {
            if (context.uriToResolve == null) {
                return false;
            }
            if (context.uriToResolve.equals("") || context.uriToResolve.charAt(0) == '#' || context.uriToResolve.startsWith("http:")) {
                return false;
            }
            try {
                if (log.isDebugEnabled()) {
                    log.debug("I was asked whether I can resolve " + context.uriToResolve);
                }
                if (context.uriToResolve.startsWith("file:") || context.baseUri.startsWith("file:")) {
                    if (log.isDebugEnabled()) {
                        log.debug("I state that I can resolve " + context.uriToResolve);
                    }
                    return true;
                }
            } catch (Exception e) {
                if (!log.isDebugEnabled()) break block8;
                log.debug(e);
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("But I can't");
        }
        return false;
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

