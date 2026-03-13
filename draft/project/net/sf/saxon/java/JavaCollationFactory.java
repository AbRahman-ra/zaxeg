/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.java;

import java.text.Collator;
import java.text.ParseException;
import java.text.RuleBasedCollator;
import java.util.Comparator;
import java.util.Locale;
import java.util.Properties;
import net.sf.saxon.Configuration;
import net.sf.saxon.expr.sort.AlphanumericCollator;
import net.sf.saxon.expr.sort.CaseFirstCollator;
import net.sf.saxon.expr.sort.CodepointCollator;
import net.sf.saxon.expr.sort.SimpleCollation;
import net.sf.saxon.lib.StringCollator;
import net.sf.saxon.trans.XPathException;

public abstract class JavaCollationFactory {
    private JavaCollationFactory() {
    }

    public static StringCollator makeCollation(Configuration config, String uri, Properties props) throws XPathException {
        String alphanumeric;
        String caseOrder;
        Collator collator = null;
        StringCollator stringCollator = null;
        String classAtt = props.getProperty("class");
        if (classAtt != null) {
            Object comparator = config.getInstance(classAtt, null);
            if (comparator instanceof Collator) {
                collator = (Collator)comparator;
            } else if (comparator instanceof StringCollator) {
                stringCollator = (StringCollator)comparator;
            } else if (comparator instanceof Comparator) {
                stringCollator = new SimpleCollation(uri, (Comparator)comparator);
            } else {
                throw new XPathException("Requested collation class " + classAtt + " is not a Comparator");
            }
        }
        if (collator == null && stringCollator == null) {
            String rulesAtt = props.getProperty("rules");
            if (rulesAtt != null) {
                try {
                    collator = new RuleBasedCollator(rulesAtt);
                } catch (ParseException e) {
                    throw new XPathException("Invalid collation rules: " + e.getMessage());
                }
            }
            if (collator == null) {
                String langAtt = props.getProperty("lang");
                collator = langAtt != null ? Collator.getInstance(JavaCollationFactory.getLocale(langAtt)) : Collator.getInstance();
            }
        }
        if (collator != null) {
            String decompositionAtt;
            String ignore;
            String strengthAtt = props.getProperty("strength");
            if (strengthAtt != null) {
                switch (strengthAtt) {
                    case "primary": {
                        collator.setStrength(0);
                        break;
                    }
                    case "secondary": {
                        collator.setStrength(1);
                        break;
                    }
                    case "tertiary": {
                        collator.setStrength(2);
                        break;
                    }
                    case "identical": {
                        collator.setStrength(3);
                        break;
                    }
                    default: {
                        throw new XPathException("strength must be primary, secondary, tertiary, or identical");
                    }
                }
            }
            if ((ignore = props.getProperty("ignore-width")) != null) {
                if (ignore.equals("yes") && strengthAtt == null) {
                    collator.setStrength(2);
                } else if (!ignore.equals("no")) {
                    throw new XPathException("ignore-width must be yes or no");
                }
            }
            if ((ignore = props.getProperty("ignore-case")) != null && strengthAtt == null) {
                switch (ignore) {
                    case "yes": {
                        collator.setStrength(1);
                        break;
                    }
                    case "no": {
                        break;
                    }
                    default: {
                        throw new XPathException("ignore-case must be yes or no");
                    }
                }
            }
            if ((ignore = props.getProperty("ignore-modifiers")) != null) {
                if (ignore.equals("yes") && strengthAtt == null) {
                    collator.setStrength(0);
                } else if (!ignore.equals("no")) {
                    throw new XPathException("ignore-modifiers must be yes or no");
                }
            }
            if ((decompositionAtt = props.getProperty("decomposition")) != null) {
                switch (decompositionAtt) {
                    case "none": {
                        collator.setDecomposition(0);
                        break;
                    }
                    case "standard": {
                        collator.setDecomposition(1);
                        break;
                    }
                    case "full": {
                        collator.setDecomposition(2);
                        break;
                    }
                    default: {
                        throw new XPathException("decomposition must be none, standard, or full");
                    }
                }
            }
        }
        if (stringCollator == null) {
            stringCollator = new SimpleCollation(uri, collator);
        }
        if ((caseOrder = props.getProperty("case-order")) != null && !"#default".equals(caseOrder)) {
            if (collator != null) {
                collator.setStrength(1);
            }
            stringCollator = CaseFirstCollator.makeCaseOrderedCollator(uri, stringCollator, caseOrder);
        }
        if ((alphanumeric = props.getProperty("alphanumeric")) != null && !"no".equals(alphanumeric)) {
            switch (alphanumeric) {
                case "yes": {
                    stringCollator = new AlphanumericCollator(stringCollator);
                    break;
                }
                case "codepoint": {
                    stringCollator = new AlphanumericCollator(CodepointCollator.getInstance());
                    break;
                }
                default: {
                    throw new XPathException("alphanumeric must be yes, no, or codepoint");
                }
            }
        }
        return stringCollator;
    }

    private static Locale getLocale(String lang) {
        String country;
        String language;
        int hyphen = lang.indexOf("-");
        if (hyphen < 1) {
            language = lang;
            country = "";
        } else {
            language = lang.substring(0, hyphen);
            country = lang.substring(hyphen + 1);
        }
        return new Locale(language, country);
    }
}

