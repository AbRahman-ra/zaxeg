/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.sort;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.CollationElementIterator;
import java.text.CollationKey;
import java.text.Collator;
import java.text.RuleBasedCollator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import net.sf.saxon.expr.sort.AtomicMatchKey;
import net.sf.saxon.expr.sort.CollationMatchKey;
import net.sf.saxon.lib.SubstringMatcher;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.StringConverter;
import net.sf.saxon.type.ValidationFailure;
import net.sf.saxon.value.AnyURIValue;

public class UcaCollatorUsingJava
implements SubstringMatcher {
    private String uri;
    private RuleBasedCollator uca;
    private Strength strengthLevel;
    private Properties properties;
    private static String[] keywords = new String[]{"fallback", "lang", "version", "strength", "alternate", "backwards", "normalization", "maxVariable", "caseLevel", "caseFirst", "numeric", "reorder"};
    private static Set<String> keys = new HashSet<String>(Arrays.asList(keywords));

    public UcaCollatorUsingJava(String uri) throws XPathException {
        this.uri = uri;
        this.uca = (RuleBasedCollator)RuleBasedCollator.getInstance();
        this.setProps(this.parseProps(uri));
    }

    public RuleBasedCollator getRuleBasedCollator() {
        return this.uca;
    }

    private void error(String field, String allowed) throws XPathException {
        this.error("value of " + field + " must be " + allowed);
    }

    private void error(String field, String allowed, String requested) throws XPathException {
        this.error("value of " + field + " must be " + allowed + ", requested was:" + requested);
    }

    private void error(String message) throws XPathException {
        throw new XPathException("Error in UCA Collation URI " + this.uri + ": " + message, "FOCH0002");
    }

    public CollationKey getJavaCollationKey(String source) {
        return this.uca.getCollationKey(source);
    }

    public int hashCode() {
        return this.uca.hashCode();
    }

    private void setProps(Properties props) throws XPathException {
        String normalization;
        String strength;
        String lang;
        this.properties = props;
        boolean fallbackError = false;
        String fallback = props.getProperty("fallback");
        if (fallback != null) {
            switch (fallback) {
                case "yes": {
                    break;
                }
                case "no": {
                    this.error("fallback=no is not supported in Saxon-HE");
                    break;
                }
                default: {
                    this.error("fallback", "yes|no");
                }
            }
        }
        if ((lang = props.getProperty("lang")) != null && !lang.isEmpty()) {
            ValidationFailure vf = StringConverter.StringToLanguage.INSTANCE.validate(lang);
            if (vf != null) {
                this.error("lang", "a valid language code");
            }
            String country = "";
            String variant = "";
            String[] parts = lang.split("-");
            String language = parts[0];
            if (parts.length > 1) {
                country = parts[1];
            }
            if (parts.length > 2) {
                variant = parts[2];
            }
            Locale loc = new Locale(language, country, variant);
            this.uca = (RuleBasedCollator)Collator.getInstance(loc);
        }
        if ((strength = props.getProperty("strength")) != null) {
            switch (strength) {
                case "primary": 
                case "1": {
                    this.setStrength(0);
                    break;
                }
                case "secondary": 
                case "2": {
                    this.setStrength(1);
                    break;
                }
                case "tertiary": 
                case "3": {
                    this.setStrength(2);
                    break;
                }
                case "quaternary": 
                case "4": {
                    this.setStrength(3);
                    break;
                }
                case "identical": 
                case "5": {
                    this.setStrength(3);
                }
            }
        }
        if ((normalization = props.getProperty("normalization")) != null) {
            if (normalization.equals("yes")) {
                this.uca.setDecomposition(1);
            } else if (normalization.equals("no")) {
                this.uca.setDecomposition(0);
            }
        }
    }

    public Properties getProperties() {
        return this.properties;
    }

    private Properties parseProps(String uri) throws XPathException {
        String fallback;
        URI uuri;
        try {
            uuri = new URI(uri);
        } catch (URISyntaxException err) {
            throw new XPathException(err);
        }
        ArrayList<String> unknownKeys = new ArrayList<String>();
        Properties props = new Properties();
        String query = AnyURIValue.decode(uuri.getRawQuery());
        if (query != null && !query.isEmpty()) {
            for (String s : query.split(";")) {
                String[] tokens = s.split("=");
                if (!keys.contains(tokens[0])) {
                    unknownKeys.add(tokens[0]);
                }
                props.setProperty(tokens[0], tokens[1]);
            }
        }
        if ((fallback = props.getProperty("fallback")) != null && fallback.equals("no") && !unknownKeys.isEmpty()) {
            StringBuilder message = new StringBuilder(unknownKeys.size() > 1 ? "unknown parameters:" : "unknown parameter:");
            for (String u : unknownKeys) {
                message.append(u).append(" ");
            }
            this.error(message.toString());
        }
        return props;
    }

    public void setStrength(int newStrength) {
        this.uca.setStrength(newStrength);
    }

    public int getStrength() {
        return this.uca.getStrength();
    }

    @Override
    public boolean comparesEqual(CharSequence s1, CharSequence s2) {
        return this.uca.compare(s1, s2) == 0;
    }

    @Override
    public String getCollationURI() {
        return this.uri;
    }

    @Override
    public int compareStrings(CharSequence o1, CharSequence o2) {
        return this.uca.compare(o1, o2);
    }

    @Override
    public AtomicMatchKey getCollationKey(CharSequence s) {
        CollationKey ck = this.uca.getCollationKey(s.toString());
        return new CollationMatchKey(ck);
    }

    @Override
    public boolean contains(String s1, String s2) {
        RuleBasedCollator collator = this.getRuleBasedCollator();
        CollationElementIterator iter1 = collator.getCollationElementIterator(s1);
        CollationElementIterator iter2 = collator.getCollationElementIterator(s2);
        return this.collationContains(iter1, iter2, null, false);
    }

    @Override
    public boolean endsWith(String s1, String s2) {
        RuleBasedCollator collator = this.getRuleBasedCollator();
        CollationElementIterator iter1 = collator.getCollationElementIterator(s1);
        CollationElementIterator iter2 = collator.getCollationElementIterator(s2);
        return this.collationContains(iter1, iter2, null, true);
    }

    @Override
    public boolean startsWith(String s1, String s2) {
        RuleBasedCollator collator = this.getRuleBasedCollator();
        CollationElementIterator iter1 = collator.getCollationElementIterator(s1);
        CollationElementIterator iter2 = collator.getCollationElementIterator(s2);
        return this.collationStartsWith(iter1, iter2);
    }

    @Override
    public String substringAfter(String s1, String s2) {
        int[] ia;
        CollationElementIterator iter2;
        RuleBasedCollator collator = this.getRuleBasedCollator();
        CollationElementIterator iter1 = collator.getCollationElementIterator(s1);
        boolean ba = this.collationContains(iter1, iter2 = collator.getCollationElementIterator(s2), ia = new int[2], false);
        if (ba) {
            return s1.substring(ia[1]);
        }
        return "";
    }

    @Override
    public String substringBefore(String s1, String s2) {
        int[] ib;
        CollationElementIterator iter2;
        RuleBasedCollator collator = this.getRuleBasedCollator();
        CollationElementIterator iter1 = collator.getCollationElementIterator(s1);
        boolean bb = this.collationContains(iter1, iter2 = collator.getCollationElementIterator(s2), ib = new int[2], false);
        if (bb) {
            return s1.substring(0, ib[0]);
        }
        return "";
    }

    private boolean collationStartsWith(CollationElementIterator s0, CollationElementIterator s1) {
        int e1;
        int e0;
        this.makeStrengthObject();
        do {
            if ((e1 = s1.next()) == -1) {
                return true;
            }
            e0 = s0.next();
            if (e0 != -1) continue;
            return false;
        } while (this.strengthLevel.compare(e0, e1) == 0);
        return false;
    }

    private String show(int ce) {
        return "" + CollationElementIterator.primaryOrder(ce) + "/" + CollationElementIterator.secondaryOrder(ce) + "/" + CollationElementIterator.tertiaryOrder(ce);
    }

    private void makeStrengthObject() {
        if (this.strengthLevel == null) {
            switch (this.getStrength()) {
                case 0: {
                    this.strengthLevel = new Primary();
                    break;
                }
                case 1: {
                    this.strengthLevel = new Secondary();
                    break;
                }
                case 2: {
                    this.strengthLevel = new Tertiary();
                    break;
                }
                default: {
                    this.strengthLevel = new Identical();
                }
            }
        }
    }

    private boolean collationContains(CollationElementIterator s0, CollationElementIterator s1, int[] offsets, boolean matchAtEnd) {
        this.makeStrengthObject();
        int e1 = s1.next();
        if (e1 == -1) {
            return true;
        }
        int e0 = -1;
        while (true) {
            if (this.strengthLevel.compare(e0, e1) != 0) {
                e0 = s0.next();
                if (e0 != -1) continue;
                return false;
            }
            int start = s0.getOffset();
            if (this.collationStartsWith(s0, s1)) {
                if (matchAtEnd) {
                    e0 = s0.next();
                    if (e0 == -1) {
                        return true;
                    }
                } else {
                    if (offsets != null) {
                        offsets[0] = start - 1;
                        offsets[1] = s0.getOffset();
                    }
                    return true;
                }
            }
            s0.setOffset(start);
            if (s0.getOffset() != start) {
                s0.next();
            }
            s1.reset();
            e0 = -1;
            e1 = s1.next();
        }
    }

    private class Identical
    implements Strength {
        private Identical() {
        }

        @Override
        public int compare(int ce1, int ce2) {
            return Integer.compare(ce1, ce2);
        }
    }

    private class Tertiary
    implements Strength {
        private Tertiary() {
        }

        @Override
        public int compare(int ce1, int ce2) {
            int c1 = Integer.compare(CollationElementIterator.primaryOrder(ce1), CollationElementIterator.primaryOrder(ce2));
            if (c1 == 0) {
                int c2 = Integer.compare(CollationElementIterator.secondaryOrder(ce1), CollationElementIterator.secondaryOrder(ce2));
                if (c2 == 0) {
                    return Integer.compare(CollationElementIterator.tertiaryOrder(ce1), CollationElementIterator.tertiaryOrder(ce2));
                }
                return c2;
            }
            return c1;
        }
    }

    private class Secondary
    implements Strength {
        private Secondary() {
        }

        @Override
        public int compare(int ce1, int ce2) {
            int c1 = Integer.compare(CollationElementIterator.primaryOrder(ce1), CollationElementIterator.primaryOrder(ce2));
            if (c1 == 0) {
                return Integer.compare(CollationElementIterator.secondaryOrder(ce1), CollationElementIterator.secondaryOrder(ce2));
            }
            return c1;
        }
    }

    private class Primary
    implements Strength {
        private Primary() {
        }

        @Override
        public int compare(int ce1, int ce2) {
            return Integer.compare(CollationElementIterator.primaryOrder(ce1), CollationElementIterator.primaryOrder(ce2));
        }
    }

    private static interface Strength {
        public int compare(int var1, int var2);
    }
}

