/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.ZeroOrOne;
import net.sf.saxon.trans.Err;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.AnyURIValue;
import net.sf.saxon.value.AtomicValue;

public class ResolveURI
extends SystemFunction {
    @Override
    public ZeroOrOne call(XPathContext context, Sequence[] arguments) throws XPathException {
        String base;
        AtomicValue arg0 = (AtomicValue)arguments[0].head();
        if (arg0 == null) {
            return ZeroOrOne.empty();
        }
        String relative = arg0.getStringValue();
        if (this.getArity() == 2) {
            base = arguments[1].head().getStringValue();
        } else {
            base = this.getStaticBaseUriString();
            if (base == null) {
                throw new XPathException("Base URI in static context of resolve-uri() is unknown", "FONS0005", context);
            }
        }
        return new ZeroOrOne<AnyURIValue>(this.resolve(base, relative, context));
    }

    private AnyURIValue resolve(String base, String relative, XPathContext context) throws XPathException {
        String result;
        boolean escaped = false;
        if (relative.contains(" ")) {
            relative = ResolveURI.escapeSpaces(relative);
            escaped = true;
        }
        if (base.contains(" ")) {
            base = ResolveURI.escapeSpaces(base);
            escaped = true;
        }
        URI relativeURI = null;
        try {
            relativeURI = new URI(relative);
        } catch (URISyntaxException e) {
            throw new XPathException("Relative URI " + Err.wrap(relative) + " is invalid: " + e.getMessage(), "FORG0002", context);
        }
        if (relativeURI.isAbsolute()) {
            return new AnyURIValue(relative);
        }
        URI absoluteURI = null;
        try {
            absoluteURI = new URI(base);
        } catch (URISyntaxException e) {
            throw new XPathException("Base URI " + Err.wrap(base) + " is invalid: " + e.getMessage(), "FORG0002", context);
        }
        if (!absoluteURI.isAbsolute()) {
            throw new XPathException("Base URI " + Err.wrap(base) + " is not an absolute URI", "FORG0002", context);
        }
        if (absoluteURI.isOpaque() && !base.startsWith("jar:")) {
            throw new XPathException("Base URI " + Err.wrap(base) + " is a non-hierarchic URI", "FORG0002", context);
        }
        if (absoluteURI.getRawFragment() != null) {
            throw new XPathException("Base URI " + Err.wrap(base) + " contains a fragment identifier", "FORG0002", context);
        }
        if (!base.startsWith("jar:") && absoluteURI.getPath() != null && absoluteURI.getPath().isEmpty()) {
            try {
                absoluteURI = new URI(absoluteURI.getScheme(), absoluteURI.getUserInfo(), absoluteURI.getHost(), absoluteURI.getPort(), "/", absoluteURI.getQuery(), absoluteURI.getFragment());
            } catch (URISyntaxException e) {
                throw new XPathException("Failed to parse JAR scheme URI " + Err.wrap(absoluteURI.toASCIIString()), "FORG0002", context);
            }
            base = absoluteURI.toString();
        }
        URI resolved = null;
        try {
            resolved = ResolveURI.makeAbsolute(relative, base);
        } catch (URISyntaxException e) {
            throw new XPathException(e.getMessage(), "FORG0002");
        }
        if (!resolved.toASCIIString().startsWith("file:////")) {
            resolved = resolved.normalize();
        }
        String string = result = escaped ? ResolveURI.unescapeSpaces(resolved.toString()) : resolved.toString();
        while (result.endsWith("..")) {
            result = result.substring(0, result.length() - 2);
        }
        while (result.endsWith("../")) {
            result = result.substring(0, result.length() - 3);
        }
        return new AnyURIValue(result);
    }

    public static String tryToExpand(String systemId) {
        if (systemId == null) {
            systemId = "";
        }
        try {
            new URL(systemId);
            return systemId;
        } catch (MalformedURLException err) {
            String dir;
            try {
                dir = System.getProperty("user.dir");
            } catch (Exception geterr) {
                return systemId;
            }
            if (!dir.endsWith("/") && !systemId.startsWith("/")) {
                dir = dir + '/';
            }
            try {
                URI currentDirectoryURI = new File(dir).toURI();
                URI baseURI = currentDirectoryURI.resolve(systemId);
                return baseURI.toString();
            } catch (Exception e) {
                return systemId;
            }
        }
    }

    public static URI makeAbsolute(String relativeURI, String base) throws URISyntaxException {
        URI absoluteURI;
        block20: {
            if (relativeURI == null) {
                if (base == null) {
                    throw new URISyntaxException("", "Relative and Base URI must not both be null");
                }
                URI absoluteURI2 = new URI(ResolveURI.escapeSpaces(base));
                if (!absoluteURI2.isAbsolute()) {
                    throw new URISyntaxException(base, "Relative URI not supplied, so base URI must be absolute");
                }
                return absoluteURI2;
            }
            try {
                URI baseURI;
                if (base == null || base.isEmpty()) {
                    String expandedBase;
                    absoluteURI = new URI(relativeURI);
                    if (!absoluteURI.isAbsolute() && !(expandedBase = ResolveURI.tryToExpand(base)).equals(base)) {
                        return ResolveURI.makeAbsolute(relativeURI, expandedBase);
                    }
                    break block20;
                }
                if (base.startsWith("jar:") || base.startsWith("file:////")) {
                    try {
                        URL baseURL = new URL(base);
                        URL absoluteURL = new URL(baseURL, relativeURI);
                        absoluteURI = absoluteURL.toURI();
                        break block20;
                    } catch (MalformedURLException err) {
                        throw new URISyntaxException(base + " " + relativeURI, err.getMessage());
                    }
                }
                if (base.startsWith("classpath:")) {
                    absoluteURI = new URI(relativeURI);
                    if (!absoluteURI.isAbsolute()) {
                        absoluteURI = new URI("classpath:" + relativeURI);
                    }
                    break block20;
                }
                try {
                    baseURI = new URI(base);
                } catch (URISyntaxException e) {
                    throw new URISyntaxException(base, "Invalid base URI: " + e.getMessage());
                }
                if (baseURI.getFragment() != null) {
                    int hash = base.indexOf(35);
                    if (hash >= 0) {
                        base = base.substring(0, hash);
                    }
                    try {
                        baseURI = new URI(base);
                    } catch (URISyntaxException e) {
                        throw new URISyntaxException(base, "Invalid base URI: " + e.getMessage());
                    }
                }
                try {
                    new URI(relativeURI);
                } catch (URISyntaxException e) {
                    throw new URISyntaxException(base, "Invalid relative URI: " + e.getMessage());
                }
                absoluteURI = relativeURI.isEmpty() ? baseURI : baseURI.resolve(relativeURI);
            } catch (IllegalArgumentException err0) {
                throw new URISyntaxException(relativeURI, "Cannot resolve URI against base " + Err.wrap(base));
            }
        }
        return absoluteURI;
    }

    public static String escapeSpaces(String s) {
        int i = s.indexOf(32);
        if (i < 0) {
            return s;
        }
        return (i == 0 ? "" : s.substring(0, i)) + "%20" + (i == s.length() - 1 ? "" : ResolveURI.escapeSpaces(s.substring(i + 1)));
    }

    public static String unescapeSpaces(String uri) {
        return uri.replace("%20", " ");
    }
}

