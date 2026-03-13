/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.lib;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.zip.GZIPInputStream;
import net.sf.saxon.Configuration;
import net.sf.saxon.lib.Logger;
import net.sf.saxon.lib.Resource;
import net.sf.saxon.lib.UnparsedTextURIResolver;
import net.sf.saxon.resource.BinaryResource;
import net.sf.saxon.resource.DataURIScheme;
import net.sf.saxon.resource.UnparsedTextResource;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.util.FastStringBuffer;

public class StandardUnparsedTextResolver
implements UnparsedTextURIResolver {
    private boolean debug = false;

    public void setDebugging(boolean debug) {
        this.debug = debug;
    }

    @Override
    public Reader resolve(URI absoluteURI, String encoding, Configuration config) throws XPathException {
        Logger err = config.getLogger();
        if (this.debug) {
            err.info("unparsed-text(): processing " + absoluteURI);
            err.info("unparsed-text(): requested encoding = " + encoding);
        }
        if (!absoluteURI.isAbsolute()) {
            throw new XPathException("Resolved URI supplied to unparsed-text() is not absolute: " + absoluteURI.toString(), "FOUT1170");
        }
        if (!config.getAllowedUriTest().test(absoluteURI)) {
            throw new XPathException("URI scheme '" + absoluteURI.getScheme() + "' has been disallowed");
        }
        InputStream inputStream = null;
        boolean isXmlMediaType = false;
        if (absoluteURI.getScheme().equals("data")) {
            String mediaType;
            Resource resource;
            try {
                resource = DataURIScheme.decode(absoluteURI);
            } catch (IllegalArgumentException e) {
                throw new XPathException("Invalid URI in 'data' scheme: " + e.getMessage(), "FOUT1170");
            }
            if (!(resource instanceof BinaryResource)) {
                assert (resource instanceof UnparsedTextResource);
                return new StringReader(((UnparsedTextResource)resource).getContent());
            }
            byte[] octets = ((BinaryResource)resource).getData();
            inputStream = new ByteArrayInputStream(octets);
            String contentEncoding = "utf-8";
            if (encoding == null) {
                encoding = contentEncoding;
            }
            isXmlMediaType = !(!(mediaType = resource.getContentType()).startsWith("application/") && !mediaType.startsWith("text/") || !mediaType.endsWith("/xml") && !mediaType.endsWith("+xml"));
        } else if (absoluteURI.getScheme().equals("classpath")) {
            InputStream is = config.getDynamicLoader().getResourceAsStream(absoluteURI.toString().substring(10));
            if (is != null) {
                try {
                    if (encoding == null) {
                        encoding = "UTF-8";
                    }
                    return new InputStreamReader(is, encoding);
                } catch (UnsupportedEncodingException e) {
                    throw new AssertionError((Object)e);
                }
            }
        } else {
            URL absoluteURL;
            try {
                absoluteURL = absoluteURI.toURL();
            } catch (MalformedURLException mue) {
                XPathException e = new XPathException("Cannot convert absolute URI " + absoluteURI + " to URL", mue);
                e.setErrorCode("FOUT1170");
                throw e;
            }
            try {
                URLConnection connection = absoluteURL.openConnection();
                connection.setRequestProperty("Accept-Encoding", "gzip");
                try {
                    connection.connect();
                } catch (IOException ioe) {
                    if (this.debug) {
                        err.error("unparsed-text(): connection failure on " + absoluteURL + ". " + ioe.getMessage());
                    }
                    XPathException xpe = new XPathException("Failed to read input file " + absoluteURL, ioe);
                    xpe.setErrorCode("FOUT1170");
                    throw xpe;
                }
                inputStream = connection.getInputStream();
                String contentEncoding = connection.getContentEncoding();
                if ("gzip".equals(contentEncoding)) {
                    inputStream = new GZIPInputStream(inputStream);
                }
                if (this.debug) {
                    err.info("unparsed-text(): established connection " + ("gzip".equals(contentEncoding) ? " (zipped)" : ""));
                }
                if (!((InputStream)inputStream).markSupported()) {
                    inputStream = new BufferedInputStream(inputStream);
                }
                isXmlMediaType = false;
                if (!"file".equals(connection.getURL().getProtocol())) {
                    String contentType = connection.getContentType();
                    if (this.debug) {
                        err.info("unparsed-text(): content type = " + contentType);
                    }
                    if (contentType != null) {
                        int pos = contentType.indexOf(59);
                        String mediaType = pos >= 0 ? contentType.substring(0, pos) : contentType;
                        mediaType = mediaType.trim();
                        if (this.debug) {
                            err.info("unparsed-text(): media type = " + mediaType);
                        }
                        isXmlMediaType = !(!mediaType.startsWith("application/") && !mediaType.startsWith("text/") || !mediaType.endsWith("/xml") && !mediaType.endsWith("+xml"));
                        String charset = "";
                        pos = contentType.toLowerCase().indexOf("charset");
                        if (pos >= 0) {
                            if ((pos = contentType.indexOf(61, pos + 7)) >= 0) {
                                charset = contentType.substring(pos + 1);
                            }
                            if ((pos = charset.indexOf(59)) > 0) {
                                charset = charset.substring(0, pos);
                            }
                            if ((pos = charset.indexOf(40)) > 0) {
                                charset = charset.substring(0, pos);
                            }
                            if ((pos = charset.indexOf(34)) > 0) {
                                charset = charset.substring(pos + 1, charset.indexOf(34, pos + 2));
                            }
                            if (this.debug) {
                                err.info("unparsed-text(): charset = " + charset.trim());
                            }
                            encoding = charset.trim();
                        }
                    }
                }
                try {
                    if (encoding == null || isXmlMediaType) {
                        encoding = StandardUnparsedTextResolver.inferStreamEncoding(inputStream, this.debug ? err : null);
                        if (this.debug) {
                            err.info("unparsed-text(): inferred encoding = " + encoding);
                        }
                    }
                } catch (IOException e) {
                    encoding = "UTF-8";
                }
            } catch (IOException ioe) {
                throw new XPathException(ioe.getMessage(), "FOUT1170");
            } catch (IllegalCharsetNameException icne) {
                throw new XPathException("Invalid encoding name: " + encoding, "FOUT1190");
            } catch (UnsupportedCharsetException uce) {
                throw new XPathException("Invalid encoding name: " + encoding, "FOUT1190");
            }
        }
        Charset charset = Charset.forName(encoding);
        CharsetDecoder decoder = charset.newDecoder();
        decoder = decoder.onMalformedInput(CodingErrorAction.REPORT);
        decoder = decoder.onUnmappableCharacter(CodingErrorAction.REPORT);
        return new BufferedReader(new InputStreamReader(inputStream, decoder));
    }

    public static String inferStreamEncoding(InputStream is, Logger err) throws IOException {
        is.mark(100);
        byte[] start = new byte[100];
        int read = is.read(start, 0, 100);
        is.reset();
        return StandardUnparsedTextResolver.inferEncoding(start, read, err);
    }

    private static String inferEncoding(byte[] start, int read, Logger logger) {
        boolean debug;
        boolean bl = debug = logger != null;
        if (read >= 2) {
            if (StandardUnparsedTextResolver.ch(start[0]) == 254 && StandardUnparsedTextResolver.ch(start[1]) == 255) {
                if (debug) {
                    logger.info("unparsed-text(): found UTF-16 byte order mark");
                }
                return "UTF-16";
            }
            if (StandardUnparsedTextResolver.ch(start[0]) == 255 && StandardUnparsedTextResolver.ch(start[1]) == 254) {
                if (debug) {
                    logger.info("unparsed-text(): found UTF-16LE byte order mark");
                }
                return "UTF-16LE";
            }
        }
        if (read >= 3 && StandardUnparsedTextResolver.ch(start[0]) == 239 && StandardUnparsedTextResolver.ch(start[1]) == 187 && StandardUnparsedTextResolver.ch(start[2]) == 191) {
            if (debug) {
                logger.info("unparsed-text(): found UTF-8 byte order mark");
            }
            return "UTF-8";
        }
        if (read >= 4) {
            if (StandardUnparsedTextResolver.ch(start[0]) == 60 && StandardUnparsedTextResolver.ch(start[1]) == 63 && StandardUnparsedTextResolver.ch(start[2]) == 120 && StandardUnparsedTextResolver.ch(start[3]) == 109 && StandardUnparsedTextResolver.ch(start[4]) == 108) {
                if (debug) {
                    logger.info("unparsed-text(): found XML declaration");
                }
                FastStringBuffer sb = new FastStringBuffer(read);
                for (int b = 0; b < read; ++b) {
                    sb.cat((char)start[b]);
                }
                String p = sb.toString();
                int v = p.indexOf("encoding");
                if (v >= 0) {
                    v += 8;
                    while (v < p.length() && " \n\r\t=\"'".indexOf(p.charAt(v)) >= 0) {
                        ++v;
                    }
                    sb.setLength(0);
                    while (v < p.length() && p.charAt(v) != '\"' && p.charAt(v) != '\'') {
                        sb.cat(p.charAt(v++));
                    }
                    if (debug) {
                        logger.info("unparsed-text(): encoding in XML declaration = " + sb.toString());
                    }
                    return sb.toString();
                }
                if (debug) {
                    logger.info("unparsed-text(): no encoding found in XML declaration");
                }
            }
        } else {
            if (read > 0 && start[0] == 0 && start[2] == 0 && start[4] == 0 && start[6] == 0) {
                if (debug) {
                    logger.info("unparsed-text(): even-numbered bytes are zero, inferring UTF-16");
                }
                return "UTF-16";
            }
            if (read > 1 && start[1] == 0 && start[3] == 0 && start[5] == 0 && start[7] == 0) {
                if (debug) {
                    logger.info("unparsed-text(): odd-numbered bytes are zero, inferring UTF-16LE");
                }
                return "UTF-16LE";
            }
        }
        if (debug) {
            logger.info("unparsed-text(): assuming fallback encoding (UTF-8)");
        }
        return "UTF-8";
    }

    private static int ch(byte b) {
        return b & 0xFF;
    }
}

