/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.dom;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import net.sf.saxon.Transform;
import net.sf.saxon.dom.DocumentWrapper;
import net.sf.saxon.trans.XPathException;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class DOMTransform
extends Transform {
    @Override
    public List<Source> preprocess(List<Source> sources) throws XPathException {
        try {
            ArrayList<Source> domSources = new ArrayList<Source>(sources.size());
            for (Source source : sources) {
                StreamSource src = (StreamSource)source;
                InputSource ins = new InputSource(src.getSystemId());
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                factory.setNamespaceAware(true);
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document doc = builder.parse(ins);
                DocumentWrapper dom = new DocumentWrapper(doc, src.getSystemId(), this.getConfiguration());
                domSources.add(dom.getRootNode());
            }
            return domSources;
        } catch (IOException | ParserConfigurationException | SAXException e) {
            throw new XPathException(e);
        }
    }

    public static void main(String[] args) {
        new DOMTransform().doTransform(args, "DOMTransform");
    }
}

