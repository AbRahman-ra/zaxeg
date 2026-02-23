/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions;

import net.sf.saxon.Configuration;
import net.sf.saxon.Version;
import net.sf.saxon.expr.Callable;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.StringLiteral;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.RetainedStaticContext;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.lib.Feature;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.StringValue;

public class SystemProperty
extends SystemFunction
implements Callable {
    @Override
    public Expression makeOptimizedFunctionCall(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo, Expression ... arguments) throws XPathException {
        if (arguments[0] instanceof Literal) {
            try {
                StringValue name = (StringValue)((Literal)arguments[0]).getValue();
                StructuredQName qName = StructuredQName.fromLexicalQName(name.getStringValue(), false, true, this.getRetainedStaticContext());
                String uri = qName.getURI();
                String local = qName.getLocalPart();
                if (uri.equals("http://www.w3.org/1999/XSL/Transform") && (local.equals("version") || local.equals("vendor") || local.equals("vendor-url") || local.equals("product-name") || local.equals("product-version") || local.equals("supports-backwards-compatibility") || local.equals("xpath-version") || local.equals("xsd-version"))) {
                    String result = SystemProperty.getProperty(uri, local, this.getRetainedStaticContext());
                    return new StringLiteral(result);
                }
            } catch (XPathException xPathException) {
                // empty catch block
            }
        }
        return null;
    }

    @Override
    public StringValue call(XPathContext context, Sequence[] arguments) throws XPathException {
        StringValue name = (StringValue)arguments[0].head();
        try {
            StructuredQName qName = StructuredQName.fromLexicalQName(name.getStringValue(), false, true, this.getRetainedStaticContext());
            return new StringValue(SystemProperty.getProperty(qName.getURI(), qName.getLocalPart(), this.getRetainedStaticContext()));
        } catch (XPathException err) {
            throw new XPathException("Invalid system property name. " + err.getMessage(), "XTDE1390", context);
        }
    }

    private boolean allowsEarlyEvaluation(Sequence[] arguments, XPathContext context) throws XPathException {
        StringValue name = (StringValue)arguments[0].head();
        try {
            StructuredQName qName = StructuredQName.fromLexicalQName(name.getStringValue(), false, true, this.getRetainedStaticContext());
            String uri = qName.getURI();
            String local = qName.getLocalPart();
            return uri.equals("http://www.w3.org/1999/XSL/Transform") && (local.equals("version") || local.equals("vendor") || local.equals("vendor-url") || local.equals("product-name") || local.equals("product-version") || local.equals("supports-backwards-compatibility") || local.equals("xpath-version") || local.equals("xsd-version"));
        } catch (XPathException err) {
            throw new XPathException("Invalid system property name. " + err.getMessage(), "XTDE1390", context);
        }
    }

    public static String yesOrNo(boolean whatever) {
        return whatever ? "yes" : "no";
    }

    public static String getProperty(String uri, String local, RetainedStaticContext rsc) {
        Configuration config = rsc.getConfiguration();
        String edition = rsc.getPackageData().getTargetEdition();
        if (uri.equals("http://www.w3.org/1999/XSL/Transform")) {
            switch (local) {
                case "version": {
                    return "3.0";
                }
                case "vendor": {
                    return Version.getProductVendor();
                }
                case "vendor-url": {
                    return Version.getWebSiteAddress();
                }
                case "product-name": {
                    return Version.getProductName();
                }
                case "product-version": {
                    return Version.getProductVariantAndVersion(edition);
                }
                case "is-schema-aware": {
                    boolean schemaAware = rsc.getPackageData().isSchemaAware();
                    return SystemProperty.yesOrNo(schemaAware);
                }
                case "supports-serialization": {
                    return SystemProperty.yesOrNo(!"JS".equals(edition));
                }
                case "supports-backwards-compatibility": {
                    return "yes";
                }
                case "supports-namespace-axis": {
                    return "yes";
                }
                case "supports-streaming": {
                    return SystemProperty.yesOrNo("EE".equals(edition) && config.isLicensedFeature(2) && !config.getConfigurationProperty(Feature.STREAMABILITY).equals("off"));
                }
                case "supports-dynamic-evaluation": {
                    return SystemProperty.yesOrNo(!config.getBooleanProperty(Feature.DISABLE_XSL_EVALUATE));
                }
                case "supports-higher-order-functions": {
                    return "yes";
                }
                case "xpath-version": {
                    return "3.1";
                }
                case "xsd-version": {
                    return rsc.getConfiguration().getXsdVersion() == 10 ? "1.0" : "1.1";
                }
            }
            return "";
        }
        if (uri.isEmpty() && config.getBooleanProperty(Feature.ALLOW_EXTERNAL_FUNCTIONS)) {
            String val = System.getProperty(local);
            return val == null ? "" : val;
        }
        return "";
    }
}

