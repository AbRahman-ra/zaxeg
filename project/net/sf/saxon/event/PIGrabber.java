/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.event;

import java.util.ArrayList;
import java.util.List;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.sax.SAXSource;
import net.sf.saxon.Configuration;
import net.sf.saxon.event.ProxyReceiver;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.lib.StandardURIResolver;
import net.sf.saxon.om.AttributeMap;
import net.sf.saxon.om.NamespaceMap;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.util.ProcInstParser;
import net.sf.saxon.type.SchemaType;

public class PIGrabber
extends ProxyReceiver {
    private Configuration config = null;
    private String reqMedia = null;
    private String reqTitle = null;
    private String baseURI = null;
    private URIResolver uriResolver = null;
    private List<String> stylesheets = new ArrayList<String>();
    private boolean terminated = false;

    public PIGrabber(Receiver next) {
        super(next);
    }

    public void setFactory(Configuration config) {
        this.config = config;
    }

    public void setCriteria(String media, String title) {
        this.reqMedia = media;
        this.reqTitle = title;
    }

    public void setBaseURI(String uri) {
        this.baseURI = uri;
    }

    public void setURIResolver(URIResolver resolver) {
        this.uriResolver = resolver;
    }

    @Override
    public void startElement(NodeName elemName, SchemaType type, AttributeMap attributes, NamespaceMap namespaces, Location location, int properties) throws XPathException {
        this.terminated = true;
        throw new XPathException("#start#");
    }

    public boolean isTerminated() {
        return this.terminated;
    }

    @Override
    public void processingInstruction(String target, CharSequence data, Location locationId, int properties) throws XPathException {
        if (target.equals("xml-stylesheet")) {
            String value = data.toString();
            String piMedia = ProcInstParser.getPseudoAttribute(value, "media");
            String piTitle = ProcInstParser.getPseudoAttribute(value, "title");
            String piType = ProcInstParser.getPseudoAttribute(value, "type");
            String piAlternate = ProcInstParser.getPseudoAttribute(value, "alternate");
            if (piType == null) {
                return;
            }
            if ((piType.equals("text/xml") || piType.equals("application/xml") || piType.equals("text/xsl") || piType.equals("applicaton/xsl") || piType.equals("application/xml+xslt")) && (this.reqMedia == null || piMedia == null || this.getConfiguration().getMediaQueryEvaluator().compare(piMedia, this.reqMedia) == 0) && (piTitle == null && (piAlternate == null || piAlternate.equals("no")) || this.reqTitle == null || piTitle != null && piTitle.equals(this.reqTitle))) {
                String href = ProcInstParser.getPseudoAttribute(value, "href");
                if (href == null) {
                    throw new XPathException("xml-stylesheet PI has no href attribute");
                }
                if (piTitle == null && (piAlternate == null || piAlternate.equals("no"))) {
                    this.stylesheets.add(0, href);
                } else {
                    this.stylesheets.add(href);
                }
            }
        }
    }

    public Source[] getAssociatedStylesheets() throws TransformerException {
        if (this.stylesheets.isEmpty()) {
            return null;
        }
        if (this.uriResolver == null) {
            this.uriResolver = new StandardURIResolver(this.config);
        }
        Source[] result = new Source[this.stylesheets.size()];
        for (int i = 0; i < this.stylesheets.size(); ++i) {
            String href = this.stylesheets.get(i);
            Source s = this.uriResolver.resolve(href, this.baseURI);
            if (s instanceof SAXSource) {
                ((SAXSource)s).setXMLReader(this.config.getStyleParser());
            }
            if (s == null) {
                s = this.config.getSystemURIResolver().resolve(href, this.baseURI);
            }
            result[i] = s;
        }
        return result;
    }
}

