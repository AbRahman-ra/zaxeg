/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.event;

import java.util.Arrays;
import net.sf.saxon.event.ProxyReceiver;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.om.AttributeMap;
import net.sf.saxon.om.FingerprintedQName;
import net.sf.saxon.om.NamespaceMap;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.om.SpaceStrippingRule;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.trans.rules.Rule;
import net.sf.saxon.trans.rules.RuleTarget;
import net.sf.saxon.type.ComplexType;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.type.Untyped;
import net.sf.saxon.value.Whitespace;

public class Stripper
extends ProxyReceiver {
    public static final StripRuleTarget STRIP = new StripRuleTarget(){};
    public static final StripRuleTarget PRESERVE = new StripRuleTarget(){};
    protected SpaceStrippingRule rule;
    private byte[] stripStack = new byte[100];
    private int top = 0;
    public static final byte ALWAYS_PRESERVE = 1;
    public static final byte ALWAYS_STRIP = 2;
    public static final byte STRIP_DEFAULT = 0;
    public static final byte PRESERVE_PARENT = 4;
    public static final byte SIMPLE_CONTENT = 8;
    public static final byte ASSERTIONS_EXIST = 16;
    private static NodeName XML_SPACE = new FingerprintedQName("xml", "http://www.w3.org/XML/1998/namespace", "space", 386);

    public Stripper(SpaceStrippingRule rule, Receiver next) {
        super(next);
        assert (rule != null);
        this.rule = rule;
    }

    public Stripper getAnother(Receiver next) {
        return new Stripper(this.rule, next);
    }

    private int isSpacePreserving(NodeName name, SchemaType type) throws XPathException {
        return this.rule.isSpacePreserving(name, type);
    }

    @Override
    public void open() throws XPathException {
        this.top = 0;
        this.stripStack[this.top] = 1;
        super.open();
    }

    @Override
    public void startElement(NodeName elemName, SchemaType type, AttributeMap attributes, NamespaceMap namespaces, Location location, int properties) throws XPathException {
        this.nextReceiver.startElement(elemName, type, attributes, namespaces, location, properties);
        byte preserveParent = this.stripStack[this.top];
        byte preserve = (byte)(preserveParent & 0x14);
        int elementStrip = this.isSpacePreserving(elemName, type);
        if (elementStrip == 1) {
            preserve = (byte)(preserve | 1);
        } else if (elementStrip == 2) {
            preserve = (byte)(preserve | 2);
        }
        if (type != Untyped.getInstance()) {
            if (preserve == 0 && (type.isSimpleType() || ((ComplexType)type).isSimpleContent())) {
                preserve = (byte)(preserve | 8);
            }
            if (type instanceof ComplexType && ((ComplexType)type).hasAssertions()) {
                preserve = (byte)(preserve | 0x10);
            }
        }
        ++this.top;
        if (this.top >= this.stripStack.length) {
            this.stripStack = Arrays.copyOf(this.stripStack, this.top * 2);
        }
        this.stripStack[this.top] = preserve;
        String xmlSpace = attributes.getValue("http://www.w3.org/XML/1998/namespace", "space");
        if (xmlSpace != null) {
            if (Whitespace.normalizeWhitespace(xmlSpace).equals("preserve")) {
                int n = this.top;
                this.stripStack[n] = (byte)(this.stripStack[n] | 4);
            } else {
                int n = this.top;
                this.stripStack[n] = (byte)(this.stripStack[n] & 0xFFFFFFFB);
            }
        }
    }

    @Override
    public void endElement() throws XPathException {
        this.nextReceiver.endElement();
        --this.top;
    }

    @Override
    public void characters(CharSequence chars, Location locationId, int properties) throws XPathException {
        if (((this.stripStack[this.top] & 0x1D) != 0 && (this.stripStack[this.top] & 2) == 0 || !Whitespace.isWhite(chars)) && chars.length() > 0) {
            this.nextReceiver.characters(chars, locationId, properties);
        }
    }

    @Override
    public boolean usesTypeAnnotations() {
        return true;
    }

    public static class StripRuleTarget
    implements RuleTarget {
        @Override
        public void export(ExpressionPresenter presenter) throws XPathException {
        }

        @Override
        public void registerRule(Rule rule) {
        }
    }
}

