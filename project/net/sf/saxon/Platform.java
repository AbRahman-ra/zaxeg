/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon;

import java.util.List;
import java.util.Properties;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import net.sf.saxon.Configuration;
import net.sf.saxon.event.PipelineConfiguration;
import net.sf.saxon.expr.StaticContext;
import net.sf.saxon.expr.parser.RetainedStaticContext;
import net.sf.saxon.expr.sort.AtomicMatchKey;
import net.sf.saxon.expr.sort.SimpleCollation;
import net.sf.saxon.lib.ModuleURIResolver;
import net.sf.saxon.lib.StringCollator;
import net.sf.saxon.regex.RegularExpression;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.ExternalObjectType;
import org.xml.sax.XMLReader;

public interface Platform {
    public void initialize(Configuration var1);

    public boolean isJava();

    public boolean isDotNet();

    public String getPlatformVersion();

    public String getPlatformSuffix();

    public XMLReader loadParser();

    public XMLReader loadParserForXmlFragments();

    public Source getParserSource(PipelineConfiguration var1, StreamSource var2, int var3, boolean var4);

    public StringCollator makeCollation(Configuration var1, Properties var2, String var3) throws XPathException;

    public boolean canReturnCollationKeys(StringCollator var1);

    public AtomicMatchKey getCollationKey(SimpleCollation var1, String var2);

    public boolean hasICUCollator();

    public boolean hasICUNumberer();

    public StringCollator makeUcaCollator(String var1, Configuration var2) throws XPathException;

    public RegularExpression compileRegularExpression(Configuration var1, CharSequence var2, String var3, String var4, List<String> var5) throws XPathException;

    public ExternalObjectType getExternalObjectType(Configuration var1, String var2, String var3);

    public String getInstallationDirectory(String var1, Configuration var2);

    public void registerAllBuiltInObjectModels(Configuration var1);

    public void setDefaultSAXParserFactory(Configuration var1);

    public boolean JAXPStaticContextCheck(RetainedStaticContext var1, StaticContext var2);

    public ModuleURIResolver makeStandardModuleURIResolver(Configuration var1);
}

