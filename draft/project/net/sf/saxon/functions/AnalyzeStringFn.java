/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions;

import java.io.InputStream;
import java.util.ArrayList;
import javax.xml.transform.sax.SAXSource;
import net.sf.saxon.Configuration;
import net.sf.saxon.event.Builder;
import net.sf.saxon.event.ComplexContentOutputter;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.parser.Loc;
import net.sf.saxon.functions.RegexFunction;
import net.sf.saxon.om.FingerprintedQName;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NoNamespaceName;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.regex.RegexIterator;
import net.sf.saxon.regex.RegularExpression;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.type.SimpleType;
import net.sf.saxon.type.Untyped;
import net.sf.saxon.value.StringValue;
import org.xml.sax.InputSource;

public class AnalyzeStringFn
extends RegexFunction {
    private NodeName resultName;
    private NodeName nonMatchName;
    private NodeName matchName;
    private NodeName groupName;
    private NodeName groupNrName;
    private SchemaType resultType = Untyped.getInstance();
    private SchemaType nonMatchType = Untyped.getInstance();
    private SchemaType matchType = Untyped.getInstance();
    private SchemaType groupType = Untyped.getInstance();
    private SimpleType groupNrType = BuiltInAtomicType.UNTYPED_ATOMIC;

    @Override
    protected boolean allowRegexMatchingEmptyString() {
        return false;
    }

    private synchronized void init(Configuration config, boolean schemaAware) throws XPathException {
        this.resultName = new FingerprintedQName("", "http://www.w3.org/2005/xpath-functions", "analyze-string-result");
        this.nonMatchName = new FingerprintedQName("", "http://www.w3.org/2005/xpath-functions", "non-match");
        this.matchName = new FingerprintedQName("", "http://www.w3.org/2005/xpath-functions", "match");
        this.groupName = new FingerprintedQName("", "http://www.w3.org/2005/xpath-functions", "group");
        this.groupNrName = new NoNamespaceName("nr");
        if (schemaAware) {
            this.resultType = config.getSchemaType(new StructuredQName("", "http://www.w3.org/2005/xpath-functions", "analyze-string-result-type"));
            this.nonMatchType = BuiltInAtomicType.STRING;
            this.matchType = config.getSchemaType(new StructuredQName("", "http://www.w3.org/2005/xpath-functions", "match-type"));
            this.groupType = config.getSchemaType(new StructuredQName("", "http://www.w3.org/2005/xpath-functions", "group-type"));
            this.groupNrType = BuiltInAtomicType.POSITIVE_INTEGER;
            if (this.resultType == null || this.matchType == null || this.groupType == null) {
                throw new XPathException("Schema for analyze-string has not been successfully loaded");
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public NodeInfo call(XPathContext context, Sequence[] arguments) throws XPathException {
        StringValue item;
        Item inputItem = arguments[0].head();
        CharSequence input = inputItem == null ? "" : inputItem.getStringValueCS();
        RegularExpression re = this.getRegularExpression(arguments);
        RegexIterator iter = re.analyze(input);
        if (this.resultName == null) {
            Configuration config;
            boolean schemaAware = context.getController().getExecutable().isSchemaAware();
            Configuration configuration = config = context.getConfiguration();
            synchronized (configuration) {
                if (schemaAware && !config.isSchemaAvailable("http://www.w3.org/2005/xpath-functions")) {
                    InputStream inputStream = Configuration.locateResource("xpath-functions.scm", new ArrayList<String>(), new ArrayList<ClassLoader>());
                    if (inputStream == null) {
                        throw new XPathException("Failed to load xpath-functions.scm from the classpath");
                    }
                    InputSource is = new InputSource(inputStream);
                    if (config.isTiming()) {
                        config.getLogger().info("Loading schema from resources for: http://www.w3.org/2005/xpath-functions");
                    }
                    config.addSchemaSource(new SAXSource(is));
                }
            }
            this.init(context.getConfiguration(), schemaAware);
        }
        Builder builder = context.getController().makeBuilder();
        final ComplexContentOutputter out = new ComplexContentOutputter(builder);
        builder.setBaseURI(this.getStaticBaseUriString());
        out.open();
        out.startElement(this.resultName, this.resultType, Loc.NONE, 0);
        out.startContent();
        while ((item = iter.next()) != null) {
            if (iter.isMatching()) {
                out.startElement(this.matchName, this.matchType, Loc.NONE, 0);
                out.startContent();
                iter.processMatchingSubstring(new RegexIterator.MatchHandler(){

                    @Override
                    public void characters(CharSequence s) throws XPathException {
                        out.characters(s, Loc.NONE, 0);
                    }

                    @Override
                    public void onGroupStart(int groupNumber) throws XPathException {
                        out.startElement(AnalyzeStringFn.this.groupName, AnalyzeStringFn.this.groupType, Loc.NONE, 0);
                        out.attribute(AnalyzeStringFn.this.groupNrName, AnalyzeStringFn.this.groupNrType, "" + groupNumber, Loc.NONE, 0);
                        out.startContent();
                    }

                    @Override
                    public void onGroupEnd(int groupNumber) throws XPathException {
                        out.endElement();
                    }
                });
                out.endElement();
                continue;
            }
            out.startElement(this.nonMatchName, this.nonMatchType, Loc.NONE, 0);
            out.startContent();
            out.characters(item.getStringValueCS(), Loc.NONE, 0);
            out.endElement();
        }
        out.endElement();
        out.close();
        return builder.getCurrentRoot();
    }
}

