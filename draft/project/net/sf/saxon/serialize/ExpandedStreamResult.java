/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.serialize;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;
import javax.xml.transform.stream.StreamResult;
import net.sf.saxon.Configuration;
import net.sf.saxon.serialize.UTF8Writer;
import net.sf.saxon.serialize.charcode.CharacterSet;
import net.sf.saxon.serialize.charcode.UTF16CharacterSet;
import net.sf.saxon.serialize.charcode.UTF8CharacterSet;
import net.sf.saxon.trans.XPathException;

public class ExpandedStreamResult {
    private Configuration config;
    private Properties outputProperties;
    private String systemId;
    private Writer writer;
    private OutputStream outputStream;
    private CharacterSet characterSet;
    private String encoding;
    private boolean mustClose;
    private boolean allCharactersEncodable;

    public ExpandedStreamResult(Configuration config, StreamResult result, Properties outputProperties) throws XPathException {
        String byteOrderMark;
        this.config = config;
        this.systemId = result.getSystemId();
        this.writer = result.getWriter();
        this.outputStream = result.getOutputStream();
        this.encoding = outputProperties.getProperty("encoding");
        if (this.encoding == null) {
            this.encoding = "UTF8";
            this.allCharactersEncodable = true;
        } else if (this.encoding.equalsIgnoreCase("UTF-8")) {
            this.encoding = "UTF8";
            this.allCharactersEncodable = true;
        } else if (this.encoding.equalsIgnoreCase("UTF-16")) {
            this.encoding = "UTF16";
        }
        if (this.characterSet == null) {
            this.characterSet = config.getCharacterSetFactory().getCharacterSet(this.encoding);
        }
        if ("no".equals(byteOrderMark = outputProperties.getProperty("byte-order-mark")) && "UTF16".equals(this.encoding)) {
            this.encoding = "UTF-16BE";
        } else if (!(this.characterSet instanceof UTF8CharacterSet)) {
            this.encoding = this.characterSet.getCanonicalName();
        }
    }

    public Writer obtainWriter() throws XPathException {
        if (this.writer != null) {
            return this.writer;
        }
        OutputStream os = this.obtainOutputStream();
        this.writer = this.makeWriterFromOutputStream(os);
        return this.writer;
    }

    protected OutputStream obtainOutputStream() throws XPathException {
        if (this.outputStream != null) {
            return this.outputStream;
        }
        String uriString = this.systemId;
        if (uriString == null) {
            throw new XPathException("Result has no system ID, writer, or output stream defined");
        }
        try {
            File file = ExpandedStreamResult.makeWritableOutputFile(uriString);
            this.outputStream = new FileOutputStream(file);
            this.mustClose = true;
        } catch (FileNotFoundException | IllegalArgumentException | URISyntaxException fnf) {
            throw new XPathException(fnf);
        }
        return this.outputStream;
    }

    public static File makeWritableOutputFile(String uriString) throws URISyntaxException, XPathException {
        URI uri = new URI(uriString);
        if (!uri.isAbsolute()) {
            try {
                uri = new File(uriString).getAbsoluteFile().toURI();
            } catch (Exception exception) {
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
            if (file.isDirectory()) {
                throw new XPathException("Cannot write to a directory: " + uriString, "SXRD0004");
            }
            if (!file.canWrite()) {
                throw new XPathException("Cannot write to URI " + uriString, "SXRD0004");
            }
        } catch (IOException err) {
            throw new XPathException("Failed to create output file " + uri, err);
        }
        return file;
    }

    public boolean usesWriter() {
        return true;
    }

    public void setWriter(Writer writer) throws XPathException {
        this.writer = writer;
        if (writer instanceof OutputStreamWriter && this.outputProperties != null) {
            String enc = ((OutputStreamWriter)writer).getEncoding();
            this.outputProperties.setProperty("encoding", enc);
            this.characterSet = this.config.getCharacterSetFactory().getCharacterSet(this.outputProperties);
            this.allCharactersEncodable = this.characterSet instanceof UTF8CharacterSet || this.characterSet instanceof UTF16CharacterSet;
        }
    }

    public Writer getWriter() {
        return this.writer;
    }

    private Writer makeWriterFromOutputStream(OutputStream stream) throws XPathException {
        this.outputStream = stream;
        try {
            String javaEncoding = this.encoding;
            if (this.encoding.equalsIgnoreCase("iso-646") || this.encoding.equalsIgnoreCase("iso646")) {
                javaEncoding = "US-ASCII";
            }
            this.writer = this.encoding.equalsIgnoreCase("UTF8") ? new UTF8Writer(this.outputStream) : new BufferedWriter(new OutputStreamWriter(this.outputStream, javaEncoding));
            return this.writer;
        } catch (Exception err) {
            if (this.encoding.equalsIgnoreCase("UTF8")) {
                throw new XPathException("Failed to create a UTF8 output writer");
            }
            throw new XPathException("Encoding " + this.encoding + " is not supported", "SESU0007");
        }
    }

    public OutputStream getOutputStream() {
        return this.outputStream;
    }

    public CharacterSet getCharacterSet() {
        return this.characterSet;
    }
}

