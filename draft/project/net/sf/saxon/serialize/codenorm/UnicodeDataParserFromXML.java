/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.serialize.codenorm;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.StringTokenizer;
import javax.xml.transform.stream.StreamSource;
import net.sf.saxon.Configuration;
import net.sf.saxon.lib.ParseOptions;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.TreeInfo;
import net.sf.saxon.pattern.NodeKindTest;
import net.sf.saxon.serialize.codenorm.NormalizerData;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.AxisIterator;
import net.sf.saxon.z.IntHashMap;
import net.sf.saxon.z.IntToIntHashMap;
import net.sf.saxon.z.IntToIntMap;

class UnicodeDataParserFromXML {
    private static final int SBase = 44032;
    private static final int LBase = 4352;
    private static final int VBase = 4449;
    private static final int TBase = 4519;
    private static final int LCount = 19;
    private static final int VCount = 21;
    private static final int TCount = 28;
    private static final int NCount = 588;
    private static final int SCount = 11172;

    private UnicodeDataParserFromXML() {
    }

    static NormalizerData build(Configuration config) throws XPathException {
        NodeInfo item;
        InputStream in = Configuration.locateResource("normalizationData.xml", new ArrayList<String>(), new ArrayList<ClassLoader>());
        if (in == null) {
            throw new XPathException("Unable to read normalizationData.xml file");
        }
        BitSet isExcluded = new BitSet(128000);
        BitSet isCompatibility = new BitSet(128000);
        ParseOptions options = new ParseOptions();
        options.setSchemaValidationMode(4);
        options.setDTDValidationMode(4);
        TreeInfo doc = config.buildDocumentTree(new StreamSource(in, "normalizationData.xml"), options);
        NodeInfo canonicalClassKeys = null;
        NodeInfo canonicalClassValues = null;
        NodeInfo decompositionKeys = null;
        NodeInfo decompositionValues = null;
        AxisIterator iter = doc.getRootNode().iterateAxis(4, NodeKindTest.ELEMENT);
        while ((item = iter.next()) != null) {
            switch (item.getLocalPart()) {
                case "CanonicalClassKeys": {
                    canonicalClassKeys = item;
                    break;
                }
                case "CanonicalClassValues": {
                    canonicalClassValues = item;
                    break;
                }
                case "DecompositionKeys": {
                    decompositionKeys = item;
                    break;
                }
                case "DecompositionValues": {
                    decompositionValues = item;
                    break;
                }
                case "ExclusionList": {
                    UnicodeDataParserFromXML.readExclusionList(item.getStringValue(), isExcluded);
                    break;
                }
                case "CompatibilityList": {
                    UnicodeDataParserFromXML.readCompatibilityList(item.getStringValue(), isCompatibility);
                }
            }
        }
        IntToIntHashMap canonicalClass = new IntToIntHashMap(400);
        canonicalClass.setDefaultValue(0);
        UnicodeDataParserFromXML.readCanonicalClassTable(canonicalClassKeys.getStringValue(), canonicalClassValues.getStringValue(), canonicalClass);
        IntHashMap<String> decompose = new IntHashMap<String>(18000);
        IntToIntHashMap compose = new IntToIntHashMap(15000);
        compose.setDefaultValue(65535);
        UnicodeDataParserFromXML.readDecompositionTable(decompositionKeys.getStringValue(), decompositionValues.getStringValue(), decompose, compose, isExcluded, isCompatibility);
        return new NormalizerData(canonicalClass, decompose, compose, isCompatibility, isExcluded);
    }

    private static void readExclusionList(String s, BitSet isExcluded) {
        StringTokenizer st = new StringTokenizer(s);
        while (st.hasMoreTokens()) {
            String tok = st.nextToken();
            int value = Integer.parseInt(tok, 32);
            isExcluded.set(value);
        }
    }

    private static void readCompatibilityList(String s, BitSet isCompatible) {
        StringTokenizer st = new StringTokenizer(s);
        while (st.hasMoreTokens()) {
            String tok = st.nextToken();
            int value = Integer.parseInt(tok, 32);
            isCompatible.set(value);
        }
    }

    private static void readCanonicalClassTable(String keyString, String valueString, IntToIntMap canonicalClasses) {
        ArrayList<Integer> keys = new ArrayList<Integer>(5000);
        StringTokenizer st = new StringTokenizer(keyString);
        while (st.hasMoreTokens()) {
            String tok = st.nextToken();
            int value = Integer.parseInt(tok, 32);
            keys.add(value);
        }
        int k = 0;
        st = new StringTokenizer(valueString);
        while (st.hasMoreTokens()) {
            int clss;
            String tok = st.nextToken();
            int repeat = 1;
            int star = tok.indexOf(42);
            if (star < 0) {
                clss = Integer.parseInt(tok, 32);
            } else {
                repeat = Integer.parseInt(tok.substring(0, star));
                clss = Integer.parseInt(tok.substring(star + 1), 32);
            }
            for (int i = 0; i < repeat; ++i) {
                canonicalClasses.put((Integer)keys.get(k++), clss);
            }
        }
    }

    private static void readDecompositionTable(String decompositionKeyString, String decompositionValuesString, IntHashMap<String> decompose, IntToIntMap compose, BitSet isExcluded, BitSet isCompatibility) {
        String tok;
        int k = 0;
        ArrayList<String> values = new ArrayList<String>(1000);
        StringTokenizer st = new StringTokenizer(decompositionValuesString);
        while (st.hasMoreTokens()) {
            tok = st.nextToken();
            StringBuilder value = new StringBuilder();
            int c = 0;
            while (c < tok.length()) {
                char h0 = tok.charAt(c++);
                char h1 = tok.charAt(c++);
                char h2 = tok.charAt(c++);
                char h3 = tok.charAt(c++);
                int code = ("0123456789abcdef".indexOf(h0) << 12) + ("0123456789abcdef".indexOf(h1) << 8) + ("0123456789abcdef".indexOf(h2) << 4) + "0123456789abcdef".indexOf(h3);
                value.append((char)code);
            }
            values.add(value.toString());
        }
        st = new StringTokenizer(decompositionKeyString);
        while (st.hasMoreTokens()) {
            tok = st.nextToken();
            int key = Integer.parseInt(tok, 32);
            String value = (String)values.get(k++);
            decompose.put(key, value);
            if (isCompatibility.get(key) || isExcluded.get(key)) continue;
            char first = '\u0000';
            char second = value.charAt(0);
            if (value.length() > 1) {
                first = second;
                second = value.charAt(1);
            }
            int pair = first << 16 | second;
            compose.put(pair, key);
        }
        for (int SIndex = 0; SIndex < 11172; ++SIndex) {
            char second;
            char first;
            int TIndex = SIndex % 28;
            if (TIndex != 0) {
                first = (char)(44032 + SIndex - TIndex);
                second = (char)(4519 + TIndex);
            } else {
                first = (char)(4352 + SIndex / 588);
                second = (char)(4449 + SIndex % 588 / 28);
            }
            int pair = first << 16 | second;
            int key = SIndex + 44032;
            decompose.put(key, String.valueOf(first) + second);
            compose.put(pair, key);
        }
    }
}

