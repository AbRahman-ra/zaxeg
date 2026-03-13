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
import java.net.URISyntaxException;
import java.util.Properties;
import javax.xml.transform.stream.StreamResult;
import net.sf.saxon.event.ReceiverWithOutputProperties;
import net.sf.saxon.event.SequenceReceiver;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.serialize.ExpandedStreamResult;
import net.sf.saxon.serialize.UTF8Writer;
import net.sf.saxon.serialize.charcode.CharacterSet;
import net.sf.saxon.serialize.charcode.UTF16CharacterSet;
import net.sf.saxon.serialize.charcode.UTF8CharacterSet;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.trans.XmlProcessingIncident;

public abstract class Emitter
extends SequenceReceiver
implements ReceiverWithOutputProperties {
    protected StreamResult streamResult;
    protected Writer writer;
    protected OutputStream outputStream;
    protected Properties outputProperties;
    protected CharacterSet characterSet;
    protected boolean allCharactersEncodable = false;
    private boolean mustClose = false;

    public Emitter() {
        super(null);
    }

    public void setOutputProperties(Properties details) throws XPathException {
        if (this.characterSet == null) {
            this.characterSet = this.getConfiguration().getCharacterSetFactory().getCharacterSet(details);
            this.allCharactersEncodable = this.characterSet instanceof UTF8CharacterSet || this.characterSet instanceof UTF16CharacterSet;
        }
        this.outputProperties = details;
    }

    @Override
    public Properties getOutputProperties() {
        return this.outputProperties;
    }

    public void setStreamResult(StreamResult result) throws XPathException {
        this.streamResult = result;
        if (this.systemId == null) {
            this.systemId = result.getSystemId();
        }
    }

    protected void makeWriter() throws XPathException {
        OutputStream os;
        if (this.writer != null) {
            return;
        }
        if (this.streamResult == null) {
            throw new IllegalStateException("Emitter must have either a Writer or a StreamResult to write to");
        }
        this.writer = this.streamResult.getWriter();
        if (this.writer == null && (os = this.streamResult.getOutputStream()) != null) {
            this.setOutputStream(os);
        }
        if (this.writer == null) {
            this.makeOutputStream();
        }
    }

    protected OutputStream makeOutputStream() throws XPathException {
        String uriString = this.streamResult.getSystemId();
        if (uriString == null) {
            throw new XPathException("Result has no system ID, writer, or output stream defined", "SXRD0004");
        }
        try {
            File file = ExpandedStreamResult.makeWritableOutputFile(uriString);
            this.setOutputStream(new FileOutputStream(file));
            this.streamResult.setOutputStream(this.outputStream);
            this.mustClose = true;
        } catch (FileNotFoundException | IllegalArgumentException | URISyntaxException fnf) {
            XPathException err = new XPathException("Unable to write to output destination", fnf);
            err.setErrorCode("SXRD0004");
            throw err;
        }
        return this.outputStream;
    }

    public boolean usesWriter() {
        return true;
    }

    public void setWriter(Writer writer) throws XPathException {
        this.writer = writer;
        if (writer instanceof OutputStreamWriter && this.outputProperties != null) {
            String enc = ((OutputStreamWriter)writer).getEncoding();
            this.outputProperties.setProperty("encoding", enc);
            this.characterSet = this.getConfiguration().getCharacterSetFactory().getCharacterSet(this.outputProperties);
            this.allCharactersEncodable = this.characterSet instanceof UTF8CharacterSet || this.characterSet instanceof UTF16CharacterSet;
        }
    }

    public Writer getWriter() {
        return this.writer;
    }

    public void setOutputStream(OutputStream stream) throws XPathException {
        this.outputStream = stream;
        if (this.usesWriter()) {
            String byteOrderMark;
            String encoding;
            if (this.outputProperties == null) {
                this.outputProperties = new Properties();
            }
            if ((encoding = this.outputProperties.getProperty("encoding")) == null) {
                encoding = "UTF8";
                this.allCharactersEncodable = true;
            } else if (encoding.equalsIgnoreCase("UTF-8")) {
                encoding = "UTF8";
                this.allCharactersEncodable = true;
            } else if (encoding.equalsIgnoreCase("UTF-16")) {
                encoding = "UTF16";
            }
            if (this.characterSet == null) {
                this.characterSet = this.getConfiguration().getCharacterSetFactory().getCharacterSet(this.outputProperties);
            }
            if ("no".equals(byteOrderMark = this.outputProperties.getProperty("byte-order-mark")) && "UTF16".equals(encoding)) {
                encoding = "UTF-16BE";
            } else if (!(this.characterSet instanceof UTF8CharacterSet)) {
                encoding = this.characterSet.getCanonicalName();
            }
            while (true) {
                try {
                    String javaEncoding = encoding;
                    if (encoding.equalsIgnoreCase("iso-646") || encoding.equalsIgnoreCase("iso646")) {
                        javaEncoding = "US-ASCII";
                    }
                    if (encoding.equalsIgnoreCase("UTF8")) {
                        this.writer = new UTF8Writer(this.outputStream);
                        break;
                    }
                    this.writer = new BufferedWriter(new OutputStreamWriter(this.outputStream, javaEncoding));
                } catch (Exception err) {
                    if (encoding.equalsIgnoreCase("UTF8")) {
                        throw new XPathException("Failed to create a UTF8 output writer");
                    }
                    XmlProcessingIncident de = new XmlProcessingIncident("Encoding " + encoding + " is not supported: using UTF8", "SESU0007");
                    this.getPipelineConfiguration().getErrorReporter().report(de);
                    encoding = "UTF8";
                    this.characterSet = UTF8CharacterSet.getInstance();
                    this.allCharactersEncodable = true;
                    this.outputProperties.setProperty("encoding", "UTF-8");
                    continue;
                }
                break;
            }
        }
    }

    public OutputStream getOutputStream() {
        return this.outputStream;
    }

    @Override
    public void setUnparsedEntity(String name, String uri, String publicId) throws XPathException {
    }

    @Override
    public void close() throws XPathException {
        if (this.mustClose && this.outputStream != null) {
            try {
                this.outputStream.close();
            } catch (IOException e) {
                throw new XPathException("Failed to close output stream");
            }
        }
    }

    @Override
    public boolean usesTypeAnnotations() {
        return false;
    }

    @Override
    public void append(Item item, Location locationId, int copyNamespaces) throws XPathException {
        if (item instanceof NodeInfo) {
            this.decompose(item, locationId, copyNamespaces);
        } else {
            this.characters(item.getStringValueCS(), locationId, 0);
        }
    }
}

