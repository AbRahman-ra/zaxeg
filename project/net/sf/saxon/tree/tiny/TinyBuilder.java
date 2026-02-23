/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.tree.tiny;

import java.util.Arrays;
import java.util.Stack;
import net.sf.saxon.Configuration;
import net.sf.saxon.event.Builder;
import net.sf.saxon.event.BuilderMonitor;
import net.sf.saxon.event.PipelineConfiguration;
import net.sf.saxon.event.ReceiverOption;
import net.sf.saxon.event.ReceivingContentHandler;
import net.sf.saxon.lib.Feature;
import net.sf.saxon.om.AttributeInfo;
import net.sf.saxon.om.AttributeMap;
import net.sf.saxon.om.NamespaceMap;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.tiny.CompressedWhitespace;
import net.sf.saxon.tree.tiny.Statistics;
import net.sf.saxon.tree.tiny.TinyBuilderMonitor;
import net.sf.saxon.tree.tiny.TinyDocumentImpl;
import net.sf.saxon.tree.tiny.TinyNodeImpl;
import net.sf.saxon.tree.tiny.TinyTree;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.type.SimpleType;

public class TinyBuilder
extends Builder {
    private static final int PARENT_POINTER_INTERVAL = 10;
    private TinyTree tree;
    private Stack<NamespaceMap> namespaceStack = new Stack();
    private int currentDepth = 0;
    private int nodeNr = 0;
    private boolean ended = false;
    private boolean noNewNamespaces = true;
    private Statistics statistics;
    private boolean markDefaultedAttributes = false;
    private Eligibility textualElementEligibilityState = Eligibility.INELIGIBLE;
    private int[] prevAtDepth = new int[100];
    private int[] siblingsAtDepth = new int[100];
    private boolean isIDElement = false;

    public TinyBuilder(PipelineConfiguration pipe) {
        super(pipe);
        Configuration config = pipe.getConfiguration();
        this.statistics = config.getTreeStatistics().TEMPORARY_TREE_STATISTICS;
        this.markDefaultedAttributes = config.isExpandAttributeDefaults() && config.getBooleanProperty(Feature.MARK_DEFAULTED_ATTRIBUTES);
    }

    public void setStatistics(Statistics stats) {
        this.statistics = stats;
    }

    public TinyTree getTree() {
        return this.tree;
    }

    public int getCurrentDepth() {
        return this.currentDepth;
    }

    @Override
    public void open() {
        if (this.started) {
            return;
        }
        if (this.tree == null) {
            this.tree = new TinyTree(this.config, this.statistics);
            this.currentDepth = 0;
            if (this.lineNumbering) {
                this.tree.setLineNumbering();
            }
            this.uniformBaseURI = true;
            this.tree.setUniformBaseUri(this.baseURI);
        }
        super.open();
    }

    @Override
    public void startDocument(int properties) throws XPathException {
        int nodeNr;
        if (this.started && !this.ended || this.currentDepth > 0) {
            return;
        }
        this.started = true;
        this.ended = false;
        TinyTree tt = this.tree;
        assert (tt != null);
        this.currentRoot = new TinyDocumentImpl(tt);
        TinyDocumentImpl doc = (TinyDocumentImpl)this.currentRoot;
        doc.setSystemId(this.getSystemId());
        doc.setBaseURI(this.getBaseURI());
        this.currentDepth = 0;
        this.prevAtDepth[0] = nodeNr = tt.addDocumentNode((TinyDocumentImpl)this.currentRoot);
        this.prevAtDepth[1] = -1;
        this.siblingsAtDepth[0] = 0;
        this.siblingsAtDepth[1] = 0;
        tt.next[nodeNr] = -1;
        ++this.currentDepth;
    }

    @Override
    public void endDocument() throws XPathException {
        this.tree.addNode((short)11, 0, 0, 0, -1);
        --this.tree.numberOfNodes;
        if (this.currentDepth > 1) {
            return;
        }
        if (this.ended) {
            return;
        }
        this.ended = true;
        this.prevAtDepth[this.currentDepth] = -1;
        --this.currentDepth;
    }

    @Override
    public void reset() {
        super.reset();
        this.tree = null;
        this.currentDepth = 0;
        this.nodeNr = 0;
        this.ended = false;
        this.statistics = this.config.getTreeStatistics().TEMPORARY_TREE_STATISTICS;
    }

    @Override
    public void close() throws XPathException {
        TinyTree tt = this.tree;
        if (tt != null) {
            tt.addNode((short)11, 0, 0, 0, -1);
            tt.condense(this.statistics);
        }
        super.close();
    }

    @Override
    public void startElement(NodeName elemName, SchemaType type, AttributeMap attributes, NamespaceMap namespaces, Location location, int properties) throws XPathException {
        TinyTree tt = this.tree;
        assert (tt != null);
        this.textualElementEligibilityState = Eligibility.INELIGIBLE;
        this.noNewNamespaces = true;
        if (this.namespaceStack.isEmpty()) {
            this.noNewNamespaces = false;
            this.namespaceStack.push(namespaces);
        } else {
            this.noNewNamespaces = namespaces == this.namespaceStack.peek();
            this.namespaceStack.push(namespaces);
        }
        if (this.siblingsAtDepth[this.currentDepth] > 10) {
            this.nodeNr = tt.addNode((short)12, this.currentDepth, this.prevAtDepth[this.currentDepth - 1], 0, 0);
            int prev = this.prevAtDepth[this.currentDepth];
            if (prev > 0) {
                tt.next[prev] = this.nodeNr;
            }
            tt.next[this.nodeNr] = this.prevAtDepth[this.currentDepth - 1];
            this.prevAtDepth[this.currentDepth] = this.nodeNr;
            this.siblingsAtDepth[this.currentDepth] = 0;
        }
        int fp = elemName.obtainFingerprint(this.namePool);
        int prefixCode = this.tree.prefixPool.obtainPrefixCode(elemName.getPrefix());
        int nameCode = prefixCode << 20 | fp;
        this.nodeNr = tt.addNode((short)1, this.currentDepth, -1, -1, nameCode);
        this.isIDElement = ReceiverOption.contains(properties, 2048);
        int typeCode = type.getFingerprint();
        if (typeCode != 630) {
            tt.setElementAnnotation(this.nodeNr, type);
            if (ReceiverOption.contains(properties, 16)) {
                tt.setNilled(this.nodeNr);
            }
            if (!this.isIDElement && type.isIdType()) {
                this.isIDElement = true;
            }
        }
        if (this.currentDepth == 0) {
            this.prevAtDepth[0] = this.nodeNr;
            this.prevAtDepth[1] = -1;
            this.currentRoot = tt.getNode(this.nodeNr);
        } else {
            int prev = this.prevAtDepth[this.currentDepth];
            if (prev > 0) {
                tt.next[prev] = this.nodeNr;
            }
            tt.next[this.nodeNr] = this.prevAtDepth[this.currentDepth - 1];
            this.prevAtDepth[this.currentDepth] = this.nodeNr;
            int n = this.currentDepth;
            this.siblingsAtDepth[n] = this.siblingsAtDepth[n] + 1;
        }
        ++this.currentDepth;
        if (this.currentDepth == this.prevAtDepth.length) {
            this.prevAtDepth = Arrays.copyOf(this.prevAtDepth, this.currentDepth * 2);
            this.siblingsAtDepth = Arrays.copyOf(this.siblingsAtDepth, this.currentDepth * 2);
        }
        this.prevAtDepth[this.currentDepth] = -1;
        this.siblingsAtDepth[this.currentDepth] = 0;
        String localSystemId = location.getSystemId();
        if (this.isUseEventLocation() && localSystemId != null) {
            tt.setSystemId(this.nodeNr, localSystemId);
        } else if (this.currentDepth == 1) {
            tt.setSystemId(this.nodeNr, this.systemId);
        }
        if (this.uniformBaseURI && localSystemId != null && !localSystemId.equals(this.baseURI)) {
            this.uniformBaseURI = false;
            tt.setUniformBaseUri(null);
        }
        if (this.lineNumbering) {
            tt.setLineNumber(this.nodeNr, location.getLineNumber(), location.getColumnNumber());
        }
        if (location instanceof ReceivingContentHandler.LocalLocator && ((ReceivingContentHandler.LocalLocator)location).levelInEntity == 0 && this.currentDepth >= 1) {
            tt.markTopWithinEntity(this.nodeNr);
        }
        for (AttributeInfo att : attributes) {
            this.attribute2(att.getNodeName(), att.getType(), this.getAttValue(att), location, att.getProperties());
        }
        this.textualElementEligibilityState = this.noNewNamespaces ? Eligibility.PRIMED : Eligibility.INELIGIBLE;
        this.tree.addNamespaces(this.nodeNr, this.namespaceStack.peek());
        ++this.nodeNr;
    }

    protected String getAttValue(AttributeInfo att) {
        return att.getValue();
    }

    private void attribute2(NodeName attName, SimpleType type, CharSequence value, Location locationId, int properties) throws XPathException {
        int nameCode;
        int fp = attName.obtainFingerprint(this.namePool);
        String prefix = attName.getPrefix();
        int n = nameCode = prefix.isEmpty() ? fp : this.tree.prefixPool.obtainPrefixCode(prefix) << 20 | fp;
        assert (this.tree != null);
        assert (this.currentRoot != null);
        this.tree.addAttribute(this.currentRoot, this.nodeNr, nameCode, type, value, properties);
        if (this.markDefaultedAttributes && ReceiverOption.contains(properties, 8)) {
            this.tree.markDefaultedAttribute(this.tree.numberOfAttributes - 1);
        }
        if (fp == 385) {
            this.uniformBaseURI = false;
            this.tree.setUniformBaseUri(null);
        }
    }

    @Override
    public void endElement() throws XPathException {
        assert (this.tree != null);
        boolean eligibleAsTextualElement = this.textualElementEligibilityState == Eligibility.ELIGIBLE;
        this.textualElementEligibilityState = Eligibility.INELIGIBLE;
        this.prevAtDepth[this.currentDepth] = -1;
        this.siblingsAtDepth[this.currentDepth] = 0;
        --this.currentDepth;
        this.namespaceStack.pop();
        if (this.isIDElement) {
            this.tree.indexIDElement(this.currentRoot, this.prevAtDepth[this.currentDepth]);
            this.isIDElement = false;
        } else if (eligibleAsTextualElement && this.tree.nodeKind[this.nodeNr] == 3 && this.tree.nodeKind[this.nodeNr - 1] == 1 && this.tree.alpha[this.nodeNr - 1] == -1 && this.noNewNamespaces) {
            this.tree.nodeKind[this.nodeNr - 1] = 17;
            this.tree.alpha[this.nodeNr - 1] = this.tree.alpha[this.nodeNr];
            this.tree.beta[this.nodeNr - 1] = this.tree.beta[this.nodeNr];
            --this.nodeNr;
            --this.tree.numberOfNodes;
            if (this.currentDepth == 0) {
                this.currentRoot = this.tree.getNode(this.nodeNr);
            }
        }
    }

    public TinyNodeImpl getLastCompletedElement() {
        if (this.tree == null) {
            return null;
        }
        return this.tree.getNode(this.currentDepth >= 0 ? this.prevAtDepth[this.currentDepth] : 0);
    }

    @Override
    public void characters(CharSequence chars, Location locationId, int properties) throws XPathException {
        if (chars instanceof CompressedWhitespace && ReceiverOption.contains(properties, 1024)) {
            TinyTree tt = this.tree;
            assert (tt != null);
            long lvalue = ((CompressedWhitespace)chars).getCompressedValue();
            this.nodeNr = tt.addNode((short)4, this.currentDepth, (int)(lvalue >> 32), (int)lvalue, -1);
            int prev = this.prevAtDepth[this.currentDepth];
            if (prev > 0) {
                tt.next[prev] = this.nodeNr;
            }
            tt.next[this.nodeNr] = this.prevAtDepth[this.currentDepth - 1];
            this.prevAtDepth[this.currentDepth] = this.nodeNr;
            int n = this.currentDepth;
            this.siblingsAtDepth[n] = this.siblingsAtDepth[n] + 1;
            if (this.lineNumbering) {
                tt.setLineNumber(this.nodeNr, locationId.getLineNumber(), locationId.getColumnNumber());
            }
            return;
        }
        int len = chars.length();
        if (len > 0) {
            this.nodeNr = this.makeTextNode(chars, len);
            if (this.lineNumbering) {
                this.tree.setLineNumber(this.nodeNr, locationId.getLineNumber(), locationId.getColumnNumber());
            }
            this.textualElementEligibilityState = this.textualElementEligibilityState == Eligibility.PRIMED ? Eligibility.ELIGIBLE : Eligibility.INELIGIBLE;
        }
    }

    protected int makeTextNode(CharSequence chars, int len) {
        TinyTree tt = this.tree;
        assert (tt != null);
        int bufferStart = tt.getCharacterBuffer().length();
        tt.appendChars(chars);
        int n = tt.numberOfNodes - 1;
        if (tt.nodeKind[n] == 3 && tt.depth[n] == this.currentDepth) {
            int n2 = n;
            tt.beta[n2] = tt.beta[n2] + len;
        } else {
            this.nodeNr = tt.addNode((short)3, this.currentDepth, bufferStart, len, -1);
            int prev = this.prevAtDepth[this.currentDepth];
            if (prev > 0) {
                tt.next[prev] = this.nodeNr;
            }
            tt.next[this.nodeNr] = this.prevAtDepth[this.currentDepth - 1];
            this.prevAtDepth[this.currentDepth] = this.nodeNr;
            int n3 = this.currentDepth;
            this.siblingsAtDepth[n3] = this.siblingsAtDepth[n3] + 1;
        }
        return this.nodeNr;
    }

    @Override
    public void processingInstruction(String piname, CharSequence remainder, Location locationId, int properties) throws XPathException {
        TinyTree tt = this.tree;
        assert (tt != null);
        this.textualElementEligibilityState = Eligibility.INELIGIBLE;
        if (tt.commentBuffer == null) {
            tt.commentBuffer = new FastStringBuffer(256);
        }
        int s = tt.commentBuffer.length();
        tt.commentBuffer.append(remainder.toString());
        int nameCode = this.namePool.allocateFingerprint("", piname);
        this.nodeNr = tt.addNode((short)7, this.currentDepth, s, remainder.length(), nameCode);
        int prev = this.prevAtDepth[this.currentDepth];
        if (prev > 0) {
            tt.next[prev] = this.nodeNr;
        }
        tt.next[this.nodeNr] = this.prevAtDepth[this.currentDepth - 1];
        this.prevAtDepth[this.currentDepth] = this.nodeNr;
        int n = this.currentDepth;
        this.siblingsAtDepth[n] = this.siblingsAtDepth[n] + 1;
        String localLocation = locationId.getSystemId();
        tt.setSystemId(this.nodeNr, localLocation);
        if (localLocation != null && !localLocation.equals(this.baseURI)) {
            this.uniformBaseURI = false;
            this.tree.setUniformBaseUri(null);
        }
        if (this.lineNumbering) {
            tt.setLineNumber(this.nodeNr, locationId.getLineNumber(), locationId.getColumnNumber());
        }
    }

    @Override
    public void comment(CharSequence chars, Location locationId, int properties) throws XPathException {
        TinyTree tt = this.tree;
        assert (tt != null);
        this.textualElementEligibilityState = Eligibility.INELIGIBLE;
        if (tt.commentBuffer == null) {
            tt.commentBuffer = new FastStringBuffer(256);
        }
        int s = tt.commentBuffer.length();
        tt.commentBuffer.append(chars.toString());
        this.nodeNr = tt.addNode((short)8, this.currentDepth, s, chars.length(), -1);
        int prev = this.prevAtDepth[this.currentDepth];
        if (prev > 0) {
            tt.next[prev] = this.nodeNr;
        }
        tt.next[this.nodeNr] = this.prevAtDepth[this.currentDepth - 1];
        this.prevAtDepth[this.currentDepth] = this.nodeNr;
        int n = this.currentDepth;
        this.siblingsAtDepth[n] = this.siblingsAtDepth[n] + 1;
        if (this.lineNumbering) {
            tt.setLineNumber(this.nodeNr, locationId.getLineNumber(), locationId.getColumnNumber());
        }
    }

    @Override
    public void setUnparsedEntity(String name, String uri, String publicId) {
        if (this.tree.getUnparsedEntity(name) == null) {
            this.tree.setUnparsedEntity(name, uri, publicId);
        }
    }

    @Override
    public BuilderMonitor getBuilderMonitor() {
        return new TinyBuilderMonitor(this);
    }

    private static enum Eligibility {
        INELIGIBLE,
        PRIMED,
        ELIGIBLE;

    }
}

