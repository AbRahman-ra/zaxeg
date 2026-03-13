/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.serialize;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import net.sf.saxon.event.Event;
import net.sf.saxon.event.ProxyReceiver;
import net.sf.saxon.expr.parser.Loc;
import net.sf.saxon.om.AttributeInfo;
import net.sf.saxon.om.AttributeMap;
import net.sf.saxon.om.FingerprintedQName;
import net.sf.saxon.om.NamespaceBinding;
import net.sf.saxon.om.NamespaceBindingSet;
import net.sf.saxon.om.NamespaceMap;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.serialize.XMLEmitter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.tiny.CharSlice;
import net.sf.saxon.type.AnyType;
import net.sf.saxon.type.ComplexType;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.type.Untyped;
import net.sf.saxon.value.Whitespace;

public class XMLIndenter
extends ProxyReceiver {
    private int level = 0;
    protected char[] indentChars = new char[]{'\n', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '};
    private boolean sameline = false;
    private boolean afterStartTag = false;
    private boolean afterEndTag = true;
    private Event.Text pendingWhitespace = null;
    private int line = 0;
    private int column = 0;
    private int suppressedAtLevel = -1;
    private Set<NodeName> suppressedElements = null;
    private XMLEmitter emitter;

    public XMLIndenter(XMLEmitter next) {
        super(next);
        this.emitter = next;
    }

    public void setOutputProperties(Properties props) {
        String omit = props.getProperty("omit-xml-declaration");
        this.afterEndTag = omit == null || !"yes".equals(Whitespace.trim(omit)) || props.getProperty("doctype-system") != null;
        String s = props.getProperty("suppress-indentation");
        if (s == null) {
            s = props.getProperty("{http://saxon.sf.net/}suppress-indentation");
        }
        if (s != null) {
            this.suppressedElements = new HashSet<NodeName>(8);
            StringTokenizer st = new StringTokenizer(s, " \t\r\n");
            while (st.hasMoreTokens()) {
                String eqName = st.nextToken();
                this.suppressedElements.add(FingerprintedQName.fromEQName(eqName));
            }
        }
    }

    @Override
    public void open() throws XPathException {
        this.emitter.open();
    }

    @Override
    public void startElement(NodeName nameCode, SchemaType type, AttributeMap attributes, NamespaceMap namespaces, Location location, int properties) throws XPathException {
        String xmlSpace;
        if (this.afterStartTag || this.afterEndTag) {
            if (this.isDoubleSpaced(nameCode)) {
                this.nextReceiver.characters("\n", location, 0);
                this.line = 0;
                this.column = 0;
            }
            this.indent();
        } else {
            this.flushPendingWhitespace();
        }
        ++this.level;
        if (this.suppressedAtLevel < 0 && (xmlSpace = attributes.getValue("http://www.w3.org/XML/1998/namespace", "space")) != null && xmlSpace.trim().equals("preserve")) {
            this.suppressedAtLevel = this.level;
        }
        this.sameline = true;
        this.afterStartTag = true;
        this.afterEndTag = false;
        this.line = 0;
        if (this.suppressedElements != null && this.suppressedAtLevel == -1 && this.suppressedElements.contains(nameCode)) {
            this.suppressedAtLevel = this.level;
        }
        if (type != AnyType.getInstance() && type != Untyped.getInstance() && this.suppressedAtLevel < 0 && type.isComplexType() && ((ComplexType)type).isMixedContent()) {
            this.suppressedAtLevel = this.level;
        }
        if (this.suppressedAtLevel < 0) {
            int len = 0;
            for (NamespaceBindingSet nbs : namespaces) {
                for (NamespaceBinding binding : nbs) {
                    String prefix = binding.getPrefix();
                    if (prefix.isEmpty()) {
                        len += 9 + binding.getURI().length();
                        continue;
                    }
                    len += prefix.length() + 10 + binding.getURI().length();
                }
            }
            for (AttributeInfo att : attributes) {
                NodeName name = att.getNodeName();
                String prefix = name.getPrefix();
                len += name.getLocalPart().length() + att.getValue().length() + 4 + (prefix.isEmpty() ? 4 : prefix.length() + 5);
            }
            if (len > this.getLineLength()) {
                int indent = (this.level - 1) * this.getIndentation() + 3;
                this.emitter.setIndentForNextAttribute(indent);
            }
        }
        this.nextReceiver.startElement(nameCode, type, attributes, namespaces, location, properties);
    }

    @Override
    public void endElement() throws XPathException {
        --this.level;
        if (this.afterEndTag && !this.sameline) {
            this.indent();
        } else {
            this.flushPendingWhitespace();
        }
        this.emitter.endElement();
        this.sameline = false;
        this.afterEndTag = true;
        this.afterStartTag = false;
        this.line = 0;
        if (this.level == this.suppressedAtLevel - 1) {
            this.suppressedAtLevel = -1;
        }
    }

    @Override
    public void processingInstruction(String target, CharSequence data, Location locationId, int properties) throws XPathException {
        if (this.afterEndTag) {
            this.indent();
        } else {
            this.flushPendingWhitespace();
        }
        this.emitter.processingInstruction(target, data, locationId, properties);
    }

    @Override
    public void characters(CharSequence chars, Location locationId, int properties) throws XPathException {
        if (this.suppressedAtLevel < 0 && Whitespace.isWhite(chars)) {
            this.pendingWhitespace = new Event.Text(chars, locationId, properties);
        } else {
            this.flushPendingWhitespace();
            for (int i = 0; i < chars.length(); ++i) {
                char c = chars.charAt(i);
                if (c == '\n') {
                    this.sameline = false;
                    ++this.line;
                    this.column = 0;
                }
                ++this.column;
            }
            this.emitter.characters(chars, locationId, properties);
            this.afterStartTag = false;
            this.afterEndTag = false;
        }
    }

    @Override
    public void comment(CharSequence chars, Location locationId, int properties) throws XPathException {
        if (this.afterEndTag) {
            this.indent();
        } else {
            this.flushPendingWhitespace();
        }
        this.emitter.comment(chars, locationId, properties);
    }

    @Override
    public boolean usesTypeAnnotations() {
        return true;
    }

    private void indent() throws XPathException {
        if (this.suppressedAtLevel >= 0) {
            this.flushPendingWhitespace();
            return;
        }
        this.pendingWhitespace = null;
        int spaces = this.level * this.getIndentation();
        if (this.line > 0 && (spaces -= this.column) <= 0) {
            return;
        }
        if (spaces + 2 >= this.indentChars.length) {
            int increment = 5 * this.getIndentation();
            if (spaces + 2 > this.indentChars.length + increment) {
                increment += spaces + 2;
            }
            char[] c2 = new char[this.indentChars.length + increment];
            System.arraycopy(this.indentChars, 0, c2, 0, this.indentChars.length);
            Arrays.fill(c2, this.indentChars.length, c2.length, ' ');
            this.indentChars = c2;
        }
        int start = this.line == 0 ? 0 : 1;
        this.emitter.characters(new CharSlice(this.indentChars, start, spaces + 1), Loc.NONE, 4);
        this.sameline = false;
    }

    private void flushPendingWhitespace() throws XPathException {
        if (this.pendingWhitespace != null) {
            this.pendingWhitespace.replay(this.nextReceiver);
            this.pendingWhitespace = null;
        }
    }

    @Override
    public void endDocument() throws XPathException {
        if (this.afterEndTag) {
            this.emitter.characters("\n", Loc.NONE, 0);
        }
        super.endDocument();
    }

    protected int getIndentation() {
        return 3;
    }

    protected boolean isDoubleSpaced(NodeName name) {
        return false;
    }

    protected int getLineLength() {
        return 80;
    }
}

