/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.lib;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;
import java.util.StringTokenizer;
import net.sf.saxon.Configuration;
import net.sf.saxon.Version;
import net.sf.saxon.lib.CollationURIResolver;
import net.sf.saxon.lib.StringCollator;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.AnyURIValue;

public class StandardCollationURIResolver
implements CollationURIResolver {
    private static final StandardCollationURIResolver theInstance = new StandardCollationURIResolver();

    public static StandardCollationURIResolver getInstance() {
        return theInstance;
    }

    @Override
    public StringCollator resolve(String uri, Configuration config) throws XPathException {
        if (uri.equals("http://saxon.sf.net/collation")) {
            return Version.platform.makeCollation(config, new Properties(), uri);
        }
        if (uri.startsWith("http://saxon.sf.net/collation?")) {
            URI uuri;
            try {
                uuri = new URI(uri);
            } catch (URISyntaxException err) {
                throw new XPathException(err);
            }
            Properties props = new Properties();
            String query = uuri.getRawQuery();
            StringTokenizer queryTokenizer = new StringTokenizer(query, ";&");
            while (queryTokenizer.hasMoreElements()) {
                String param = queryTokenizer.nextToken();
                int eq = param.indexOf(61);
                if (eq <= 0 || eq >= param.length() - 1) continue;
                String kw = param.substring(0, eq);
                String val = AnyURIValue.decode(param.substring(eq + 1));
                props.setProperty(kw, val);
            }
            return Version.platform.makeCollation(config, props, uri);
        }
        if (uri.startsWith("http://www.w3.org/2013/collation/UCA")) {
            URI uuri;
            StringCollator uca = Version.platform.makeUcaCollator(uri, config);
            if (uca != null) {
                return uca;
            }
            if (uri.contains("fallback=no")) {
                return null;
            }
            try {
                uuri = new URI(uri);
            } catch (URISyntaxException err) {
                throw new XPathException(err);
            }
            Properties props = new Properties();
            String query = AnyURIValue.decode(uuri.getRawQuery());
            for (String param : query.split(";")) {
                String[] tokens = param.split("=");
                if (tokens.length != 2) continue;
                String kw = tokens[0];
                String val = tokens[1];
                if (kw.equals("fallback")) {
                    if (val.equals("no")) {
                        return null;
                    }
                    if (!val.equals("yes")) {
                        return null;
                    }
                }
                switch (kw) {
                    case "strength": {
                        switch (val) {
                            case "1": {
                                val = "primary";
                                break;
                            }
                            case "2": {
                                val = "secondary";
                                break;
                            }
                            case "3": {
                                val = "tertiary";
                                break;
                            }
                            case "quaternary": 
                            case "4": 
                            case "5": {
                                val = "identical";
                            }
                        }
                        break;
                    }
                    case "caseFirst": {
                        kw = "case-order";
                        val = val + "-first";
                        break;
                    }
                    case "numeric": {
                        kw = "alphanumeric";
                    }
                }
                props.setProperty(kw, val);
            }
            return Version.platform.makeCollation(config, props, uri);
        }
        return null;
    }
}

