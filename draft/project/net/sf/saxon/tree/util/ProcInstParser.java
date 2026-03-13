/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.tree.util;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import net.sf.saxon.Version;
import net.sf.saxon.trans.XPathException;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;

public class ProcInstParser {
    private ProcInstParser() {
    }

    public static String getPseudoAttribute(String content, final String name) throws XPathException {
        try {
            final ArrayList result = new ArrayList();
            XMLFilterImpl filter = new XMLFilterImpl(){

                @Override
                public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
                    String val = atts.getValue(name);
                    if (val != null) {
                        result.add(val);
                    }
                }
            };
            XMLReader reader = Version.platform.loadParserForXmlFragments();
            reader.setContentHandler(filter);
            StringReader in = new StringReader("<e " + content + "/>");
            reader.parse(new InputSource(in));
            return result.isEmpty() ? null : (String)result.get(0);
        } catch (IOException | SAXException e) {
            throw new XPathException("Invalid syntax for pseudo-attributes: " + e.getMessage(), "SXCH0005");
        }
    }
}

