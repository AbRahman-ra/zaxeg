/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.query;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;
import net.sf.saxon.Configuration;
import net.sf.saxon.event.PipelineConfiguration;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.event.SequenceCopier;
import net.sf.saxon.expr.parser.Loc;
import net.sf.saxon.lib.SerializerFactory;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.serialize.SerializationProperties;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.SingletonIterator;
import net.sf.saxon.tree.tiny.TinyBuilder;

public class QueryResult {
    public static String RESULT_NS = "http://saxon.sf.net/xquery-results";

    private QueryResult() {
    }

    public static String serialize(NodeInfo nodeInfo) throws XPathException {
        StringWriter sw = new StringWriter();
        Properties props = new Properties();
        props.setProperty("method", "xml");
        props.setProperty("indent", "yes");
        props.setProperty("omit-xml-declaration", "yes");
        QueryResult.serialize(nodeInfo, (Result)new StreamResult(sw), props);
        return sw.toString();
    }

    public static NodeInfo wrap(SequenceIterator iterator, Configuration config) throws XPathException {
        PipelineConfiguration pipe = config.makePipelineConfiguration();
        TinyBuilder builder = new TinyBuilder(pipe);
        builder.setStatistics(config.getTreeStatistics().SOURCE_DOCUMENT_STATISTICS);
        QueryResult.sendWrappedSequence(iterator, builder);
        return builder.getCurrentRoot();
    }

    public static void sendWrappedSequence(SequenceIterator iterator, Receiver destination) throws XPathException {
        SerializerFactory sf = destination.getPipelineConfiguration().getConfiguration().getSerializerFactory();
        SequenceCopier.copySequence(iterator, sf.newSequenceWrapper(destination));
    }

    public static void serialize(NodeInfo node, Result destination, Properties outputProperties) throws XPathException {
        Configuration config = node.getConfiguration();
        QueryResult.serializeSequence((SequenceIterator)SingletonIterator.makeIterator(node), config, destination, outputProperties);
    }

    public static void serialize(NodeInfo node, Result destination, SerializationProperties properties) throws XPathException {
        Configuration config = node.getConfiguration();
        QueryResult.serializeSequence((SequenceIterator)SingletonIterator.makeIterator(node), config, destination, properties);
    }

    public static void serializeSequence(SequenceIterator iterator, Configuration config, OutputStream destination, Properties outputProps) throws XPathException {
        QueryResult.serializeSequence(iterator, config, (Result)new StreamResult(destination), outputProps);
        try {
            destination.flush();
        } catch (IOException err) {
            throw new XPathException(err);
        }
    }

    public static void serializeSequence(SequenceIterator iterator, Configuration config, Writer writer, Properties outputProps) throws XPathException {
        QueryResult.serializeSequence(iterator, config, (Result)new StreamResult(writer), outputProps);
        try {
            writer.flush();
        } catch (IOException err) {
            throw new XPathException(err);
        }
    }

    public static void serializeSequence(SequenceIterator iterator, Configuration config, Result result, Properties outputProperties) throws XPathException {
        SerializerFactory sf = config.getSerializerFactory();
        Receiver tr = sf.getReceiver(result, new SerializationProperties(outputProperties));
        SequenceCopier.copySequence(iterator, tr);
    }

    public static void serializeSequence(SequenceIterator iterator, Configuration config, Result result, SerializationProperties properties) throws XPathException {
        SerializerFactory sf = config.getSerializerFactory();
        Receiver tr = sf.getReceiver(result, properties);
        SequenceCopier.copySequence(iterator, tr);
    }

    public static void rewriteToDisk(NodeInfo doc, Properties outputProperties, boolean backup, PrintStream log) throws XPathException {
        URI u;
        switch (doc.getNodeKind()) {
            case 9: {
                break;
            }
            case 1: {
                NodeInfo parent = doc.getParent();
                if (parent == null || parent.getNodeKind() == 9) break;
                throw new XPathException("Cannot rewrite an element node unless it is top-level");
            }
            default: {
                throw new XPathException("Node to be rewritten must be a document or element node");
            }
        }
        String uri = doc.getSystemId();
        if (uri == null || uri.isEmpty()) {
            throw new XPathException("Cannot rewrite a document with no known URI");
        }
        try {
            u = new URI(uri);
        } catch (URISyntaxException e) {
            throw new XPathException("SystemId of updated document is not a valid URI: " + uri);
        }
        File existingFile = new File(u);
        File dir = existingFile.getParentFile();
        if (backup && existingFile.exists()) {
            boolean success;
            File backupFile = new File(dir, existingFile.getName() + ".bak");
            if (log != null) {
                log.println("Creating backup file " + backupFile);
            }
            if (!(success = existingFile.renameTo(backupFile))) {
                throw new XPathException("Failed to create backup file of " + backupFile);
            }
        }
        if (!existingFile.exists()) {
            if (log != null) {
                log.println("Creating file " + existingFile);
            }
            try {
                existingFile.createNewFile();
            } catch (IOException e) {
                throw new XPathException("Failed to create new file " + existingFile);
            }
        } else if (log != null) {
            log.println("Overwriting file " + existingFile);
        }
        Configuration config = doc.getConfiguration();
        SerializerFactory factory = config.getSerializerFactory();
        Receiver r = factory.getReceiver(new StreamResult(existingFile), new SerializationProperties(outputProperties));
        doc.copy(r, 2, Loc.NONE);
        r.close();
    }
}

