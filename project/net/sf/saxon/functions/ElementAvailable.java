/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.StringLiteral;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.lib.Feature;
import net.sf.saxon.om.NameChecker;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.BooleanValue;

public class ElementAvailable
extends SystemFunction {
    public static boolean isXslt30Element(int fp) {
        switch (fp) {
            case 128: 
            case 129: 
            case 130: 
            case 131: 
            case 132: 
            case 133: 
            case 134: 
            case 135: 
            case 136: 
            case 137: 
            case 138: 
            case 139: 
            case 141: 
            case 142: 
            case 143: 
            case 144: 
            case 145: 
            case 146: 
            case 147: 
            case 150: 
            case 151: 
            case 152: 
            case 153: 
            case 154: 
            case 155: 
            case 156: 
            case 157: 
            case 158: 
            case 159: 
            case 160: 
            case 161: 
            case 162: 
            case 163: 
            case 164: 
            case 165: 
            case 166: 
            case 167: 
            case 168: 
            case 169: 
            case 170: 
            case 171: 
            case 172: 
            case 173: 
            case 174: 
            case 175: 
            case 176: 
            case 177: 
            case 178: 
            case 179: 
            case 180: 
            case 181: 
            case 182: 
            case 183: 
            case 184: 
            case 185: 
            case 186: 
            case 187: 
            case 188: 
            case 189: 
            case 190: 
            case 191: 
            case 192: 
            case 193: 
            case 194: 
            case 195: 
            case 196: 
            case 198: 
            case 199: 
            case 200: 
            case 201: 
            case 202: 
            case 203: 
            case 204: 
            case 205: 
            case 206: 
            case 207: 
            case 208: 
            case 209: {
                return true;
            }
        }
        return false;
    }

    @Override
    public int getSpecialProperties(Expression[] arguments) {
        try {
            String arg;
            StructuredQName elem;
            if (arguments[0] instanceof StringLiteral && (elem = this.getElementName(arg = ((StringLiteral)arguments[0]).getStringValue())).hasURI("http://www.w3.org/1999/XSL/Transform") && elem.getLocalPart().equals("evaluate")) {
                return super.getSpecialProperties(arguments) | 0x400;
            }
        } catch (XPathException xPathException) {
            // empty catch block
        }
        return super.getSpecialProperties(arguments);
    }

    private boolean isElementAvailable(String lexicalName, String edition, XPathContext context) throws XPathException {
        StructuredQName qName = this.getElementName(lexicalName);
        if (qName.hasURI("http://www.w3.org/1999/XSL/Transform")) {
            int fp = context.getConfiguration().getNamePool().getFingerprint("http://www.w3.org/1999/XSL/Transform", qName.getLocalPart());
            boolean known = ElementAvailable.isXslt30Element(fp);
            if (fp == 153) {
                known = known && !context.getConfiguration().getBooleanProperty(Feature.DISABLE_XSL_EVALUATE);
            }
            return known;
        }
        if (qName.hasURI("http://saxonica.com/ns/interactiveXSLT") && !edition.equals("JS")) {
            return false;
        }
        return context.getConfiguration().isExtensionElementAvailable(qName);
    }

    private StructuredQName getElementName(String lexicalName) throws XPathException {
        try {
            if (lexicalName.indexOf(58) < 0 && NameChecker.isValidNCName(lexicalName)) {
                String uri = this.getRetainedStaticContext().getURIForPrefix("", true);
                return new StructuredQName("", uri, lexicalName);
            }
            return StructuredQName.fromLexicalQName(lexicalName, false, true, this.getRetainedStaticContext());
        } catch (XPathException e) {
            XPathException err = new XPathException("Invalid element name passed to element-available(): " + e.getMessage());
            err.setErrorCode("XTDE1440");
            throw err;
        }
    }

    @Override
    public BooleanValue call(XPathContext context, Sequence[] arguments) throws XPathException {
        String lexicalQName = arguments[0].head().getStringValue();
        boolean b = this.isElementAvailable(lexicalQName, this.getRetainedStaticContext().getPackageData().getTargetEdition(), context);
        return BooleanValue.get(b);
    }
}

