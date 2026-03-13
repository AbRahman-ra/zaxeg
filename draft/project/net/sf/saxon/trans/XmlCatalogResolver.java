/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 * 
 * Could not load the following classes:
 *  org.apache.xml.resolver.CatalogManager
 *  org.apache.xml.resolver.helpers.Debug
 */
package net.sf.saxon.trans;

import javax.xml.transform.TransformerException;
import net.sf.saxon.Configuration;
import net.sf.saxon.Version;
import net.sf.saxon.trans.XPathException;
import org.apache.xml.resolver.CatalogManager;
import org.apache.xml.resolver.helpers.Debug;

public class XmlCatalogResolver {
    public static void setCatalog(String catalog, final Configuration config, boolean isTracing) throws XPathException {
        System.setProperty("xml.catalog.files", catalog);
        Version.platform.setDefaultSAXParserFactory(config);
        if (isTracing) {
            CatalogManager.getStaticManager().debug = new Debug(){

                public void message(int level, String message) {
                    if (level <= this.getDebug()) {
                        config.getLogger().info(message);
                    }
                }

                public void message(int level, String message, String spec) {
                    if (level <= this.getDebug()) {
                        config.getLogger().info(message + ": " + spec);
                    }
                }

                public void message(int level, String message, String spec1, String spec2) {
                    if (level <= this.getDebug()) {
                        config.getLogger().info(message + ": " + spec1);
                        config.getLogger().info("\t" + spec2);
                    }
                }
            };
            if (CatalogManager.getStaticManager().getVerbosity() < 2) {
                CatalogManager.getStaticManager().setVerbosity(2);
            }
        }
        config.setSourceParserClass("org.apache.xml.resolver.tools.ResolvingXMLReader");
        config.setStyleParserClass("org.apache.xml.resolver.tools.ResolvingXMLReader");
        try {
            config.setURIResolver(config.makeURIResolver("org.apache.xml.resolver.tools.CatalogResolver"));
        } catch (TransformerException err) {
            throw XPathException.makeXPathException(err);
        }
    }
}

