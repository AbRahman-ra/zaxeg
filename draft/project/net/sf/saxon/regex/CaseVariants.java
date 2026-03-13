/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.regex;

import java.io.InputStream;
import java.util.ArrayList;
import javax.xml.transform.stream.StreamSource;
import net.sf.saxon.Configuration;
import net.sf.saxon.lib.ParseOptions;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.pattern.NameTest;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.AxisIterator;
import net.sf.saxon.z.IntArraySet;
import net.sf.saxon.z.IntHashMap;
import net.sf.saxon.z.IntToIntHashMap;
import net.sf.saxon.z.IntToIntMap;

public class CaseVariants {
    private static IntToIntMap monoVariants = null;
    private static IntHashMap<int[]> polyVariants = null;
    public static int[] ROMAN_VARIANTS = new int[]{304, 305, 8490, 383};

    static void build() {
        NodeInfo item;
        NodeInfo doc;
        monoVariants = new IntToIntHashMap(2500);
        polyVariants = new IntHashMap(100);
        InputStream in = Configuration.locateResource("casevariants.xml", new ArrayList<String>(), new ArrayList<ClassLoader>());
        if (in == null) {
            throw new RuntimeException("Unable to read casevariants.xml file");
        }
        Configuration config = new Configuration();
        ParseOptions options = new ParseOptions();
        options.setSchemaValidationMode(4);
        options.setDTDValidationMode(4);
        try {
            doc = config.buildDocumentTree(new StreamSource(in, "casevariants.xml"), options).getRootNode();
        } catch (XPathException e) {
            throw new RuntimeException("Failed to build casevariants.xml", e);
        }
        AxisIterator iter = doc.iterateAxis(4, new NameTest(1, "", "c", config.getNamePool()));
        while ((item = iter.next()) != null) {
            String code = item.getAttributeValue("", "n");
            int icode = Integer.parseInt(code, 16);
            String variants = item.getAttributeValue("", "v");
            String[] vhex = variants.split(",");
            int[] vint = new int[vhex.length];
            for (int i = 0; i < vhex.length; ++i) {
                vint[i] = Integer.parseInt(vhex[i], 16);
            }
            if (vhex.length == 1) {
                monoVariants.put(icode, vint[0]);
                continue;
            }
            polyVariants.put(icode, vint);
        }
    }

    public static synchronized int[] getCaseVariants(int code) {
        int mono;
        if (monoVariants == null) {
            CaseVariants.build();
        }
        if ((mono = monoVariants.get(code)) != monoVariants.getDefaultValue()) {
            return new int[]{mono};
        }
        int[] result = polyVariants.get(code);
        if (result == null) {
            return IntArraySet.EMPTY_INT_ARRAY;
        }
        return result;
    }
}

