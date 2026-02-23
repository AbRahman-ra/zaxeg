/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.serialize;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import net.sf.saxon.event.ProxyReceiver;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.event.ReceiverOption;
import net.sf.saxon.expr.parser.Loc;
import net.sf.saxon.om.AttributeMap;
import net.sf.saxon.om.FingerprintedQName;
import net.sf.saxon.om.NamespaceMap;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.tiny.CharSlice;
import net.sf.saxon.type.SchemaType;

public class HTMLIndenter
extends ProxyReceiver {
    private static final String[] formattedTags = new String[]{"pre", "script", "style", "textarea", "title", "xmp"};
    private static final String[] inlineTags = new String[]{"a", "abbr", "acronym", "applet", "area", "audio", "b", "basefont", "bdi", "bdo", "big", "br", "button", "canvas", "cite", "code", "data", "datalist", "del", "dfn", "em", "embed", "font", "i", "iframe", "img", "input", "ins", "kbd", "label", "map", "mark", "math", "meter", "noscript", "object", "output", "picture", "progress", "q", "ruby", "s", "samp", "script", "select", "small", "span", "strike", "strong", "sub", "sup", "svg", "template", "textarea", "time", "tt", "u", "var", "video", "wbr"};
    private static final Set<String> inlineTable = new HashSet<String>(70);
    private static final Set<String> formattedTable = new HashSet<String>(10);
    protected char[] indentChars = new char[]{'\n', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '};
    private static final int IS_INLINE = 1;
    private static final int IS_FORMATTED = 2;
    private static final int IS_SUPPRESSED = 4;
    private String method;
    private int level = 0;
    private boolean sameLine = false;
    private boolean inFormattedTag = false;
    private boolean afterInline = false;
    private boolean afterEndElement = false;
    private int[] propertyStack = new int[20];
    private Set<String> suppressed = null;

    public HTMLIndenter(Receiver next, String method) {
        super(next);
    }

    public void setOutputProperties(Properties props) {
        String s = props.getProperty("suppress-indentation");
        if (s != null) {
            this.suppressed = new HashSet<String>(8);
            StringTokenizer st = new StringTokenizer(s, " \t\r\n");
            while (st.hasMoreTokens()) {
                String eqName = st.nextToken();
                this.suppressed.add(FingerprintedQName.fromEQName(eqName).getLocalPart().toLowerCase());
            }
        }
    }

    public int classifyTag(NodeName name) {
        int r = 0;
        if (inlineTable.contains(name.getLocalPart().toLowerCase())) {
            r |= 1;
        }
        if (formattedTable.contains(name.getLocalPart().toLowerCase())) {
            r |= 2;
        }
        if (this.suppressed != null && this.suppressed.contains(name.getLocalPart().toLowerCase())) {
            r |= 4;
        }
        return r;
    }

    @Override
    public void startElement(NodeName elemName, SchemaType type, AttributeMap attributes, NamespaceMap namespaces, Location location, int properties) throws XPathException {
        boolean inlineTag;
        int withinSuppressed = this.level == 0 ? 0 : this.propertyStack[this.level - 1] & 4;
        int tagProps = this.classifyTag(elemName) | withinSuppressed;
        if (this.level >= this.propertyStack.length) {
            this.propertyStack = Arrays.copyOf(this.propertyStack, this.level * 2);
        }
        this.propertyStack[this.level] = tagProps;
        boolean bl = inlineTag = (tagProps & 1) != 0;
        if (!(inlineTag || this.inFormattedTag || this.afterInline || withinSuppressed != 0 || this.level == 0)) {
            this.indent();
        }
        this.nextReceiver.startElement(elemName, type, attributes, namespaces, location, properties);
        this.inFormattedTag = this.inFormattedTag || (tagProps & 2) != 0;
        ++this.level;
        this.sameLine = true;
        this.afterInline = false;
        this.afterEndElement = false;
    }

    @Override
    public void endElement() throws XPathException {
        boolean thisSuppressed;
        --this.level;
        boolean thisInline = (this.propertyStack[this.level] & 1) != 0;
        boolean thisFormatted = (this.propertyStack[this.level] & 2) != 0;
        boolean bl = thisSuppressed = (this.propertyStack[this.level] & 4) != 0;
        if (!(!this.afterEndElement || thisInline || thisSuppressed || this.afterInline || this.sameLine || this.inFormattedTag)) {
            this.indent();
            this.afterInline = false;
        } else {
            this.afterInline = thisInline;
        }
        this.nextReceiver.endElement();
        this.inFormattedTag = this.inFormattedTag && !thisFormatted;
        this.sameLine = false;
        this.afterEndElement = true;
    }

    @Override
    public void characters(CharSequence chars, Location locationId, int properties) throws XPathException {
        if (this.inFormattedTag || ReceiverOption.contains(properties, 256) || ReceiverOption.contains(properties, 1)) {
            this.nextReceiver.characters(chars, locationId, properties);
        } else {
            int lastNL = 0;
            for (int i = 0; i < chars.length(); ++i) {
                if (chars.charAt(i) != '\n' && (i - lastNL <= this.getLineLength() || chars.charAt(i) != ' ')) continue;
                this.sameLine = false;
                this.nextReceiver.characters(chars.subSequence(lastNL, i), locationId, properties);
                this.indent();
                for (lastNL = i + 1; lastNL < chars.length() && chars.charAt(lastNL) == ' '; ++lastNL) {
                }
            }
            if (lastNL < chars.length()) {
                this.nextReceiver.characters(chars.subSequence(lastNL, chars.length()), locationId, properties);
            }
        }
        this.afterInline = false;
        this.afterEndElement = false;
    }

    @Override
    public void processingInstruction(String target, CharSequence data, Location locationId, int properties) throws XPathException {
        if (this.afterEndElement && this.level != 0 && (this.propertyStack[this.level - 1] & 1) == 0) {
            this.indent();
        }
        this.nextReceiver.processingInstruction(target, data, locationId, properties);
        this.afterEndElement = false;
    }

    @Override
    public void comment(CharSequence chars, Location locationId, int properties) throws XPathException {
        if (this.afterEndElement && this.level != 0 && (this.propertyStack[this.level - 1] & 1) == 0) {
            this.indent();
        }
        this.nextReceiver.comment(chars, locationId, properties);
        this.afterEndElement = false;
    }

    protected int getLineLength() {
        return 80;
    }

    private void indent() throws XPathException {
        int spaces = this.level * this.getIndentation();
        if (spaces + 1 >= this.indentChars.length) {
            int increment = 5 * this.getIndentation();
            if (spaces + 1 > this.indentChars.length + increment) {
                increment += spaces + 1;
            }
            char[] c2 = new char[this.indentChars.length + increment];
            System.arraycopy(this.indentChars, 0, c2, 0, this.indentChars.length);
            Arrays.fill(c2, this.indentChars.length, c2.length, ' ');
            this.indentChars = c2;
        }
        this.nextReceiver.characters(new CharSlice(this.indentChars, 0, spaces + 1), Loc.NONE, 0);
        this.sameLine = false;
    }

    protected int getIndentation() {
        return 3;
    }

    static {
        Collections.addAll(inlineTable, inlineTags);
        Collections.addAll(formattedTable, formattedTags);
    }
}

