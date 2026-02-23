/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.regex.charclass;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.IntPredicate;
import javax.xml.transform.stream.StreamSource;
import net.sf.saxon.Configuration;
import net.sf.saxon.lib.ParseOptions;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.pattern.NameTest;
import net.sf.saxon.pattern.NodeKindTest;
import net.sf.saxon.regex.charclass.CharacterClass;
import net.sf.saxon.regex.charclass.IntSetCharacterClass;
import net.sf.saxon.regex.charclass.InverseCharacterClass;
import net.sf.saxon.regex.charclass.PredicateCharacterClass;
import net.sf.saxon.regex.charclass.SingletonCharacterClass;
import net.sf.saxon.serialize.charcode.XMLCharacterData;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.AxisIterator;
import net.sf.saxon.tree.tiny.TinyElementImpl;
import net.sf.saxon.z.IntArraySet;
import net.sf.saxon.z.IntIterator;
import net.sf.saxon.z.IntRangeSet;
import net.sf.saxon.z.IntSet;
import net.sf.saxon.z.IntSetPredicate;

public class Categories {
    private static HashMap<String, Category> CATEGORIES = null;
    public static final CharacterClass ESCAPE_s = new IntSetCharacterClass(IntArraySet.make(new int[]{9, 10, 13, 32}, 4));
    public static final CharacterClass ESCAPE_S = new InverseCharacterClass(ESCAPE_s);
    public static final PredicateCharacterClass ESCAPE_i = new PredicateCharacterClass(value -> XMLCharacterData.isNCNameStart11(value) || value == 58);
    public static final CharacterClass ESCAPE_I = new InverseCharacterClass(ESCAPE_i);
    public static final PredicateCharacterClass ESCAPE_c = new PredicateCharacterClass(value -> XMLCharacterData.isNCName11(value) || value == 58);
    public static final CharacterClass ESCAPE_C = new InverseCharacterClass(ESCAPE_c);
    public static final Category ESCAPE_d = Categories.getCategory("Nd");
    public static final CharacterClass ESCAPE_D = new InverseCharacterClass(ESCAPE_d);
    static Category CATEGORY_P = Categories.getCategory("P");
    static Category CATEGORY_Z = Categories.getCategory("Z");
    static Category CATEGORY_C = Categories.getCategory("C");
    public static final PredicateCharacterClass ESCAPE_w = new PredicateCharacterClass(value -> !CATEGORY_P.test(value) && !CATEGORY_Z.test(value) && !CATEGORY_C.test(value));
    public static final CharacterClass ESCAPE_W = new InverseCharacterClass(ESCAPE_w);

    static void build() {
        NodeInfo doc;
        CATEGORIES = new HashMap(30);
        InputStream in = Configuration.locateResource("categories.xml", new ArrayList<String>(), new ArrayList<ClassLoader>());
        if (in == null) {
            throw new RuntimeException("Unable to read categories.xml file");
        }
        Configuration config = new Configuration();
        ParseOptions options = new ParseOptions();
        options.setSchemaValidationMode(4);
        options.setDTDValidationMode(4);
        options.setTreeModel(1);
        try {
            doc = config.buildDocumentTree(new StreamSource(in, "categories.xml"), options).getRootNode();
        } catch (XPathException e) {
            throw new RuntimeException("Failed to build categories.xml", e);
        }
        int fp_name = config.getNamePool().allocateFingerprint("", "name");
        int fp_f = config.getNamePool().allocateFingerprint("", "f");
        int fp_t = config.getNamePool().allocateFingerprint("", "t");
        AxisIterator iter = doc.iterateAxis(4, new NameTest(1, "", "cat", config.getNamePool()));
        iter.forEach(item -> {
            String cat = ((TinyElementImpl)item).getAttributeValue(fp_name);
            IntRangeSet irs = new IntRangeSet();
            for (NodeInfo nodeInfo : ((NodeInfo)item).children(NodeKindTest.ELEMENT)) {
                String from = ((TinyElementImpl)nodeInfo).getAttributeValue(fp_f);
                String to = ((TinyElementImpl)nodeInfo).getAttributeValue(fp_t);
                irs.addRange(Integer.parseInt(from, 16), Integer.parseInt(to, 16));
            }
            CATEGORIES.put(cat, new Category(cat, new IntSetPredicate(irs)));
        });
        String c = "CLMNPSZ";
        for (int i = 0; i < c.length(); ++i) {
            char ch = c.charAt(i);
            IntPredicate ip = null;
            for (Map.Entry<String, Category> entry : CATEGORIES.entrySet()) {
                if (entry.getKey().charAt(0) != ch) continue;
                ip = ip == null ? (IntPredicate)entry.getValue() : ip.or(entry.getValue());
            }
            String label = ch + "";
            CATEGORIES.put(label, new Category(label, ip));
        }
    }

    public static synchronized Category getCategory(String cat) {
        if (CATEGORIES == null) {
            Categories.build();
        }
        return CATEGORIES.get(cat);
    }

    public static class Category
    implements CharacterClass {
        private String label;
        private IntPredicate predicate;

        public Category(String label, IntPredicate predicate) {
            this.label = label;
            this.predicate = predicate;
        }

        @Override
        public boolean test(int value) {
            return this.predicate.test(value);
        }

        @Override
        public boolean isDisjoint(CharacterClass other) {
            if (other instanceof Category) {
                String otherLabel;
                char majorCat1;
                char majorCat0 = this.label.charAt(0);
                return majorCat0 != (majorCat1 = (otherLabel = ((Category)other).label).charAt(0)) || this.label.length() > 1 && otherLabel.length() > 1 && !this.label.equals(otherLabel);
            }
            if (other instanceof InverseCharacterClass) {
                return other.isDisjoint(this);
            }
            if (other instanceof SingletonCharacterClass) {
                return !this.test(((SingletonCharacterClass)other).getCodepoint());
            }
            if (other instanceof IntSetCharacterClass) {
                IntSet intSet = other.getIntSet();
                if (intSet.size() > 100) {
                    return false;
                }
                IntIterator ii = intSet.iterator();
                while (ii.hasNext()) {
                    if (!this.test(ii.next())) continue;
                    return false;
                }
                return true;
            }
            return false;
        }

        @Override
        public IntSet getIntSet() {
            return Category.extent(this.predicate);
        }

        private static IntSet extent(IntPredicate predicate) {
            if (predicate instanceof IntSetPredicate) {
                return ((IntSetPredicate)predicate).getIntSet();
            }
            return null;
        }
    }
}

