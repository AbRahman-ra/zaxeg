/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.s9api;

import java.util.function.Predicate;
import net.sf.saxon.event.FilterFactory;
import net.sf.saxon.event.ProxyReceiver;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.event.Stripper;
import net.sf.saxon.om.AllElementsSpaceStrippingRule;
import net.sf.saxon.om.IgnorableSpaceStrippingRule;
import net.sf.saxon.om.NoElementsSpaceStrippingRule;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.om.SpaceStrippingRule;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.style.StylesheetPackage;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.SchemaType;

public class WhitespaceStrippingPolicy {
    private int policy;
    private SpaceStrippingRule stripperRules;
    public static final WhitespaceStrippingPolicy NONE = new WhitespaceStrippingPolicy(0);
    public static final WhitespaceStrippingPolicy IGNORABLE = new WhitespaceStrippingPolicy(1);
    public static final WhitespaceStrippingPolicy ALL = new WhitespaceStrippingPolicy(2);
    public static final WhitespaceStrippingPolicy UNSPECIFIED = new WhitespaceStrippingPolicy(3);

    public static WhitespaceStrippingPolicy makeCustomPolicy(final Predicate<QName> elementTest) {
        SpaceStrippingRule rule = new SpaceStrippingRule(){

            @Override
            public int isSpacePreserving(NodeName nodeName, SchemaType schemaType) {
                return elementTest.test(new QName(nodeName.getStructuredQName())) ? 2 : 1;
            }

            @Override
            public ProxyReceiver makeStripper(Receiver next) {
                return new Stripper(this, next);
            }

            @Override
            public void export(ExpressionPresenter presenter) throws XPathException {
                throw new UnsupportedOperationException();
            }
        };
        WhitespaceStrippingPolicy wsp = new WhitespaceStrippingPolicy(4);
        wsp.stripperRules = rule;
        return wsp;
    }

    private WhitespaceStrippingPolicy(int policy) {
        this.policy = policy;
        switch (policy) {
            case 2: {
                this.stripperRules = AllElementsSpaceStrippingRule.getInstance();
                break;
            }
            case 0: {
                this.stripperRules = NoElementsSpaceStrippingRule.getInstance();
                break;
            }
            case 1: {
                this.stripperRules = IgnorableSpaceStrippingRule.getInstance();
                break;
            }
        }
    }

    protected WhitespaceStrippingPolicy(StylesheetPackage pack) {
        this.policy = 4;
        this.stripperRules = pack.getStripperRules();
    }

    protected int ordinal() {
        return this.policy;
    }

    protected SpaceStrippingRule getSpaceStrippingRule() {
        return this.stripperRules;
    }

    protected FilterFactory makeStripper() {
        return new FilterFactory(){

            @Override
            public ProxyReceiver makeFilter(Receiver next) {
                return new Stripper(WhitespaceStrippingPolicy.this.stripperRules, next);
            }
        };
    }
}

