/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.StringTokenizer;
import net.sf.saxon.Configuration;
import net.sf.saxon.lib.Validation;
import net.sf.saxon.om.AllElementsSpaceStrippingRule;
import net.sf.saxon.om.IgnorableSpaceStrippingRule;
import net.sf.saxon.om.NoElementsSpaceStrippingRule;
import net.sf.saxon.om.SpaceStrippingRule;
import net.sf.saxon.regex.ARegularExpression;
import net.sf.saxon.regex.JavaRegularExpression;
import net.sf.saxon.regex.RegularExpression;
import net.sf.saxon.trans.Instantiator;
import net.sf.saxon.trans.Maker;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.util.FastStringBuffer;
import org.xml.sax.XMLReader;

public class URIQueryParameters {
    FilenameFilter filter = null;
    Boolean recurse = null;
    Integer validation = null;
    SpaceStrippingRule strippingRule = null;
    Integer onError = null;
    Maker<XMLReader> parserMaker = null;
    Boolean xinclude = null;
    Boolean stable = null;
    Boolean metadata = null;
    String contentType = null;
    public static final int ON_ERROR_FAIL = 1;
    public static final int ON_ERROR_WARNING = 2;
    public static final int ON_ERROR_IGNORE = 3;

    public URIQueryParameters(String query, Configuration config) throws XPathException {
        if (query != null) {
            StringTokenizer t = new StringTokenizer(query, ";&");
            while (t.hasMoreTokens()) {
                String tok = t.nextToken();
                int eq = tok.indexOf(61);
                if (eq <= 0 || eq >= tok.length() - 1) continue;
                String keyword = tok.substring(0, eq);
                String value = tok.substring(eq + 1);
                this.processParameter(config, keyword, value);
            }
        }
    }

    private void processParameter(Configuration config, String keyword, String value) throws XPathException {
        if (keyword.equals("select")) {
            this.filter = URIQueryParameters.makeGlobFilter(value);
        } else if (keyword.equals("match")) {
            ARegularExpression regex = new ARegularExpression(value, "", "XP", new ArrayList<String>(), config);
            this.filter = new RegexFilter(regex);
        } else if (keyword.equals("recurse")) {
            this.recurse = "yes".equals(value);
        } else if (keyword.equals("validation")) {
            int v = Validation.getCode(value);
            if (v != -1) {
                this.validation = v;
            }
        } else if (keyword.equals("strip-space")) {
            switch (value) {
                case "yes": {
                    this.strippingRule = AllElementsSpaceStrippingRule.getInstance();
                    break;
                }
                case "ignorable": {
                    this.strippingRule = IgnorableSpaceStrippingRule.getInstance();
                    break;
                }
                case "no": {
                    this.strippingRule = NoElementsSpaceStrippingRule.getInstance();
                }
            }
        } else if (keyword.equals("stable")) {
            if (value.equals("yes")) {
                this.stable = Boolean.TRUE;
            } else if (value.equals("no")) {
                this.stable = Boolean.FALSE;
            }
        } else if (keyword.equals("metadata")) {
            if (value.equals("yes")) {
                this.metadata = Boolean.TRUE;
            } else if (value.equals("no")) {
                this.metadata = Boolean.FALSE;
            }
        } else if (keyword.equals("xinclude")) {
            if (value.equals("yes")) {
                this.xinclude = Boolean.TRUE;
            } else if (value.equals("no")) {
                this.xinclude = Boolean.FALSE;
            }
        } else if (keyword.equals("content-type")) {
            this.contentType = value;
        } else if (keyword.equals("on-error")) {
            switch (value) {
                case "warning": {
                    this.onError = 2;
                    break;
                }
                case "ignore": {
                    this.onError = 3;
                    break;
                }
                case "fail": {
                    this.onError = 1;
                }
            }
        } else if (keyword.equals("parser") && config != null) {
            this.parserMaker = new Instantiator<XMLReader>(value, config);
        }
    }

    public static FilenameFilter makeGlobFilter(String value) throws XPathException {
        FastStringBuffer sb = new FastStringBuffer(value.length() + 6);
        sb.cat('^');
        for (int i = 0; i < value.length(); ++i) {
            char c = value.charAt(i);
            if (c == '.') {
                sb.append("\\.");
                continue;
            }
            if (c == '*') {
                sb.append(".*");
                continue;
            }
            if (c == '?') {
                sb.append(".?");
                continue;
            }
            sb.cat(c);
        }
        sb.cat('$');
        try {
            return new RegexFilter(new JavaRegularExpression(sb, ""));
        } catch (XPathException e) {
            throw new XPathException("Invalid glob " + value + " in collection URI", "FODC0004");
        }
    }

    public SpaceStrippingRule getSpaceStrippingRule() {
        return this.strippingRule;
    }

    public Integer getValidationMode() {
        return this.validation;
    }

    public FilenameFilter getFilenameFilter() {
        return this.filter;
    }

    public Boolean getRecurse() {
        return this.recurse;
    }

    public Integer getOnError() {
        return this.onError;
    }

    public Boolean getXInclude() {
        return this.xinclude;
    }

    public Boolean getMetaData() {
        return this.metadata;
    }

    public String getContentType() {
        return this.contentType;
    }

    public Boolean getStable() {
        return this.stable;
    }

    public Maker<XMLReader> getXMLReaderMaker() {
        return this.parserMaker;
    }

    public static class RegexFilter
    implements FilenameFilter {
        private RegularExpression pattern;

        public RegexFilter(RegularExpression regex) {
            this.pattern = regex;
        }

        @Override
        public boolean accept(File dir, String name) {
            return new File(dir, name).isDirectory() || this.pattern.matches(name);
        }

        public boolean matches(String name) {
            return this.pattern.matches(name);
        }
    }
}

