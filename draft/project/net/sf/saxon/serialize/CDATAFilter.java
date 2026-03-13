/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.serialize;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.Stack;
import java.util.StringTokenizer;
import net.sf.saxon.event.ProxyReceiver;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.event.ReceiverOption;
import net.sf.saxon.expr.parser.Loc;
import net.sf.saxon.om.AttributeMap;
import net.sf.saxon.om.FingerprintedQName;
import net.sf.saxon.om.NamespaceMap;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.serialize.UnicodeNormalizer;
import net.sf.saxon.serialize.charcode.CharacterSet;
import net.sf.saxon.serialize.charcode.UTF16CharacterSet;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.tiny.CharSlice;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.type.SchemaType;

public class CDATAFilter
extends ProxyReceiver {
    private FastStringBuffer buffer = new FastStringBuffer(256);
    private Stack<NodeName> stack = new Stack();
    private Set<NodeName> nameList;
    private CharacterSet characterSet;

    public CDATAFilter(Receiver next) {
        super(next);
    }

    public void setOutputProperties(Properties details) throws XPathException {
        this.getCdataElements(details);
        this.characterSet = this.getConfiguration().getCharacterSetFactory().getCharacterSet(details);
    }

    @Override
    public void startElement(NodeName elemName, SchemaType type, AttributeMap attributes, NamespaceMap namespaces, Location location, int properties) throws XPathException {
        this.flush();
        this.stack.push(elemName);
        this.nextReceiver.startElement(elemName, type, attributes, namespaces, location, properties);
    }

    @Override
    public void endElement() throws XPathException {
        this.flush();
        this.stack.pop();
        this.nextReceiver.endElement();
    }

    @Override
    public void processingInstruction(String target, CharSequence data, Location locationId, int properties) throws XPathException {
        this.flush();
        this.nextReceiver.processingInstruction(target, data, locationId, properties);
    }

    @Override
    public void characters(CharSequence chars, Location locationId, int properties) throws XPathException {
        if (!ReceiverOption.contains(properties, 1)) {
            this.buffer.append(chars.toString());
        } else {
            this.flush();
            this.nextReceiver.characters(chars, locationId, properties);
        }
    }

    @Override
    public void comment(CharSequence chars, Location locationId, int properties) throws XPathException {
        this.flush();
        this.nextReceiver.comment(chars, locationId, properties);
    }

    private void flush() throws XPathException {
        boolean cdata;
        int end = this.buffer.length();
        if (end == 0) {
            return;
        }
        if (this.stack.isEmpty()) {
            cdata = false;
        } else {
            NodeName top = this.stack.peek();
            cdata = this.isCDATA(top);
        }
        if (cdata) {
            if (this.getNextReceiver() instanceof UnicodeNormalizer) {
                this.buffer = new FastStringBuffer(((UnicodeNormalizer)this.getNextReceiver()).normalize(this.buffer, true));
                end = this.buffer.length();
            }
            int start = 0;
            int k = 0;
            while (k < end) {
                int next = this.buffer.charAt(k);
                int skip = 1;
                if (UTF16CharacterSet.isHighSurrogate((char)next)) {
                    next = UTF16CharacterSet.combinePair((char)next, this.buffer.charAt(k + 1));
                    skip = 2;
                }
                if (next != 0 && this.characterSet.inCharset(next)) {
                    ++k;
                    continue;
                }
                char[] array = new char[k - start];
                this.buffer.getChars(start, k, array, 0);
                this.flushCDATA(array, k - start);
                while (k < end) {
                    this.nextReceiver.characters(this.buffer.subSequence(k, k + skip), Loc.NONE, 2);
                    if ((k += skip) >= end) break;
                    next = this.buffer.charAt(k);
                    skip = 1;
                    if (UTF16CharacterSet.isHighSurrogate((char)next)) {
                        next = UTF16CharacterSet.combinePair((char)next, this.buffer.charAt(k + 1));
                        skip = 2;
                    }
                    if (!this.characterSet.inCharset(next)) continue;
                }
                start = k;
            }
            char[] rest = new char[end - start];
            this.buffer.getChars(start, end, rest, 0);
            this.flushCDATA(rest, end - start);
        } else {
            this.nextReceiver.characters(this.buffer, Loc.NONE, 0);
        }
        this.buffer.setLength(0);
    }

    private void flushCDATA(char[] array, int len) throws XPathException {
        if (len == 0) {
            return;
        }
        int chprop = 3;
        Loc loc = Loc.NONE;
        this.nextReceiver.characters("<![CDATA[", loc, 3);
        int doneto = 0;
        for (int i = 0; i < len - 2; ++i) {
            if (array[i] == ']' && array[i + 1] == ']' && array[i + 2] == '>') {
                this.nextReceiver.characters(new CharSlice(array, doneto, i + 2 - doneto), loc, 3);
                this.nextReceiver.characters("]]><![CDATA[", loc, 3);
                doneto = i + 2;
                continue;
            }
            if (array[i] != '\u0000') continue;
            this.nextReceiver.characters(new CharSlice(array, doneto, i - doneto), loc, 3);
            doneto = i + 1;
        }
        this.nextReceiver.characters(new CharSlice(array, doneto, len - doneto), loc, 3);
        this.nextReceiver.characters("]]>", loc, 3);
    }

    protected boolean isCDATA(NodeName elementName) {
        return this.nameList.contains(elementName);
    }

    private void getCdataElements(Properties details) {
        boolean isHTML = "html".equals(details.getProperty("method"));
        boolean isHTML5 = isHTML && "5.0".equals(details.getProperty("version"));
        boolean isHTML4 = isHTML && !isHTML5;
        String cdata = details.getProperty("cdata-section-elements");
        if (cdata == null) {
            this.nameList = new HashSet<NodeName>(0);
            return;
        }
        this.nameList = new HashSet<NodeName>(10);
        StringTokenizer st2 = new StringTokenizer(cdata, " \t\n\r", false);
        while (st2.hasMoreTokens()) {
            String expandedName = st2.nextToken();
            StructuredQName sq = StructuredQName.fromClarkName(expandedName);
            String uri = sq.getURI();
            if (isHTML && (!isHTML4 || uri.equals("")) && (!isHTML5 || uri.equals("") || uri.equals("http://www.w3.org/1999/xhtml"))) continue;
            this.nameList.add(new FingerprintedQName("", sq.getURI(), sq.getLocalPart()));
        }
    }
}

