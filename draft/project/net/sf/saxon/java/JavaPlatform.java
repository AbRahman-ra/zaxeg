/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.java;

import java.lang.reflect.Method;
import java.text.CollationKey;
import java.text.Collator;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamSource;
import net.sf.saxon.Configuration;
import net.sf.saxon.Platform;
import net.sf.saxon.dom.DOMEnvelope;
import net.sf.saxon.dom.DOMObjectModel;
import net.sf.saxon.event.PipelineConfiguration;
import net.sf.saxon.expr.StaticContext;
import net.sf.saxon.expr.parser.RetainedStaticContext;
import net.sf.saxon.expr.sort.AlphanumericCollator;
import net.sf.saxon.expr.sort.AtomicMatchKey;
import net.sf.saxon.expr.sort.CollationMatchKey;
import net.sf.saxon.expr.sort.SimpleCollation;
import net.sf.saxon.expr.sort.UcaCollatorUsingJava;
import net.sf.saxon.functions.FunctionLibraryList;
import net.sf.saxon.java.JavaCollationFactory;
import net.sf.saxon.lib.ModuleURIResolver;
import net.sf.saxon.lib.StandardModuleURIResolver;
import net.sf.saxon.lib.StringCollator;
import net.sf.saxon.om.NamespaceResolver;
import net.sf.saxon.regex.ARegularExpression;
import net.sf.saxon.regex.JavaRegularExpression;
import net.sf.saxon.regex.RegularExpression;
import net.sf.saxon.resource.StandardCollectionFinder;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.ExternalObjectType;
import net.sf.saxon.xpath.JAXPXPathStaticContext;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

public class JavaPlatform
implements Platform {
    static boolean tryJdk9 = true;

    @Override
    public boolean JAXPStaticContextCheck(RetainedStaticContext retainedStaticContext, StaticContext sc) {
        if (sc instanceof JAXPXPathStaticContext && !(((JAXPXPathStaticContext)sc).getNamespaceContext() instanceof NamespaceResolver)) {
            this.setNamespacesFromJAXP(retainedStaticContext, (JAXPXPathStaticContext)sc);
            return true;
        }
        return false;
    }

    private void setNamespacesFromJAXP(RetainedStaticContext retainedStaticContext, JAXPXPathStaticContext sc) {
        final NamespaceContext nc = sc.getNamespaceContext();
        retainedStaticContext.setNamespaces(new NamespaceResolver(){

            @Override
            public String getURIForPrefix(String prefix, boolean useDefault) {
                return nc.getNamespaceURI(prefix);
            }

            @Override
            public Iterator<String> iteratePrefixes() {
                throw new UnsupportedOperationException();
            }
        });
    }

    @Override
    public void initialize(Configuration config) {
        config.registerExternalObjectModel(DOMEnvelope.getInstance());
        config.registerExternalObjectModel(DOMObjectModel.getInstance());
        config.setCollectionFinder(new StandardCollectionFinder());
    }

    @Override
    public boolean isJava() {
        return true;
    }

    @Override
    public boolean isDotNet() {
        return false;
    }

    @Override
    public String getPlatformVersion() {
        return "Java version " + System.getProperty("java.version");
    }

    @Override
    public String getPlatformSuffix() {
        return "J";
    }

    @Override
    public XMLReader loadParser() {
        XMLReader parser;
        try {
            parser = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
        } catch (ParserConfigurationException | SAXException err) {
            throw new TransformerFactoryConfigurationError(err);
        }
        return parser;
    }

    @Override
    public XMLReader loadParserForXmlFragments() {
        SAXParserFactory factory = null;
        if (tryJdk9) {
            try {
                Method method = SAXParserFactory.class.getMethod("newDefaultInstance", new Class[0]);
                Object result = method.invoke(null, new Object[0]);
                factory = (SAXParserFactory)result;
            } catch (Exception e) {
                tryJdk9 = false;
            }
        }
        if (factory == null) {
            try {
                Class<?> factoryClass = Class.forName("com.sun.org.apache.xerces.internal.jaxp.SAXParserFactoryImpl");
                factory = (SAXParserFactory)factoryClass.newInstance();
            } catch (Exception exception) {
                // empty catch block
            }
        }
        if (factory != null) {
            try {
                return factory.newSAXParser().getXMLReader();
            } catch (Exception exception) {
                // empty catch block
            }
        }
        return this.loadParser();
    }

    @Override
    public Source getParserSource(PipelineConfiguration pipe, StreamSource input, int validation, boolean dtdValidation) {
        return input;
    }

    @Override
    public StringCollator makeCollation(Configuration config, Properties props, String uri) throws XPathException {
        return JavaCollationFactory.makeCollation(config, uri, props);
    }

    @Override
    public boolean canReturnCollationKeys(StringCollator collation) {
        return !(collation instanceof SimpleCollation) || ((SimpleCollation)collation).getComparator() instanceof Collator;
    }

    @Override
    public AtomicMatchKey getCollationKey(SimpleCollation namedCollation, String value) {
        CollationKey ck = ((Collator)namedCollation.getComparator()).getCollationKey(value);
        return new CollationMatchKey(ck);
    }

    @Override
    public boolean hasICUCollator() {
        return false;
    }

    @Override
    public boolean hasICUNumberer() {
        return false;
    }

    @Override
    public StringCollator makeUcaCollator(String uri, Configuration config) throws XPathException {
        UcaCollatorUsingJava collator = new UcaCollatorUsingJava(uri);
        if ("yes".equals(collator.getProperties().getProperty("numeric"))) {
            return new AlphanumericCollator(collator);
        }
        return collator;
    }

    @Override
    public RegularExpression compileRegularExpression(Configuration config, CharSequence regex, String flags, String hostLanguage, List<String> warnings) throws XPathException {
        if (flags.contains("!")) {
            return new JavaRegularExpression(regex, flags.replace("!", ""));
        }
        boolean useJava = false;
        boolean useSaxon = false;
        int semi = flags.indexOf(59);
        if (semi >= 0) {
            useJava = flags.indexOf(106, semi) >= 0;
            useSaxon = flags.indexOf(115, semi) >= 0;
            flags = flags.substring(0, semi);
        }
        if ("J".equals(config.getDefaultRegexEngine()) && !useSaxon) {
            useJava = true;
        }
        if (useJava) {
            return new JavaRegularExpression(regex, flags);
        }
        return new ARegularExpression(regex, flags, hostLanguage, warnings, config);
    }

    public void addFunctionLibraries(FunctionLibraryList list, Configuration config, int hostLanguage) {
    }

    @Override
    public ExternalObjectType getExternalObjectType(Configuration config, String uri, String localName) {
        throw new UnsupportedOperationException("getExternalObjectType for Java");
    }

    @Override
    public String getInstallationDirectory(String edition, Configuration config) {
        try {
            return System.getenv("SAXON_HOME");
        } catch (SecurityException e) {
            return null;
        }
    }

    @Override
    public void registerAllBuiltInObjectModels(Configuration config) {
    }

    @Override
    public void setDefaultSAXParserFactory(Configuration config) {
    }

    @Override
    public ModuleURIResolver makeStandardModuleURIResolver(Configuration config) {
        return new StandardModuleURIResolver(config);
    }
}

