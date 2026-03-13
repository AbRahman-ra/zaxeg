/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.resource;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import net.sf.saxon.lib.ParseOptions;
import net.sf.saxon.lib.Resource;
import net.sf.saxon.resource.AbstractResourceCollection;
import net.sf.saxon.resource.BinaryResource;
import net.sf.saxon.resource.UnparsedTextResource;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.Base64BinaryValue;

public class DataURIScheme {
    public static Resource decode(URI uri) throws XPathException {
        assert (uri.getScheme().equals("data"));
        String path = uri.getSchemeSpecificPart();
        int comma = path.indexOf(44);
        if (comma < 0) {
            throw new IllegalArgumentException("Missing comma in data URI");
        }
        String header = path.substring(0, comma);
        String content = path.substring(comma + 1);
        boolean isBase64 = header.endsWith(";base64");
        String contentType = header.substring(0, isBase64 ? comma - 7 : comma);
        if (isBase64) {
            try {
                byte[] octets = Base64BinaryValue.decode(content);
                BinaryResource resource = new BinaryResource(uri.toString(), contentType, octets);
                resource.setData(octets);
                return resource;
            } catch (XPathException e) {
                throw new IllegalArgumentException(e.getMessage());
            }
        }
        String encoding = DataURIScheme.getEncoding(contentType);
        if (encoding == null) {
            encoding = "US-ASCII";
        }
        byte[] utf8content = content.getBytes(StandardCharsets.UTF_8);
        AbstractResourceCollection.InputDetails details = new AbstractResourceCollection.InputDetails();
        details.resourceUri = uri.toString();
        details.contentType = DataURIScheme.getMediaType(contentType);
        details.encoding = encoding;
        details.binaryContent = utf8content;
        details.onError = 1;
        details.parseOptions = new ParseOptions();
        return new UnparsedTextResource(details);
    }

    private static String getMediaType(String contentType) {
        int semicolon = contentType.indexOf(59);
        if (semicolon < 0) {
            return contentType;
        }
        return contentType.substring(0, semicolon);
    }

    private static String getEncoding(String contentType) {
        String[] parts;
        for (String part : parts = contentType.split(";")) {
            if (!part.startsWith("charset=")) continue;
            return part.substring(8);
        }
        return null;
    }
}

