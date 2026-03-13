/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.regex;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.xml.transform.stream.StreamSource;
import net.sf.saxon.Configuration;
import net.sf.saxon.lib.ParseOptions;
import net.sf.saxon.om.AllElementsSpaceStrippingRule;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.TreeInfo;
import net.sf.saxon.pattern.NameTest;
import net.sf.saxon.pattern.NodeKindTest;
import net.sf.saxon.regex.RESyntaxException;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.AxisIterator;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.z.IntBlockSet;
import net.sf.saxon.z.IntSet;

public class UnicodeBlocks {
    private static Map<String, IntSet> blocks = null;

    public static IntSet getBlock(String name) throws RESyntaxException {
        IntSet cc;
        if (blocks == null) {
            UnicodeBlocks.readBlocks(new Configuration());
        }
        if ((cc = blocks.get(name)) != null) {
            return cc;
        }
        cc = blocks.get(UnicodeBlocks.normalizeBlockName(name));
        return cc;
    }

    private static String normalizeBlockName(String name) {
        FastStringBuffer fsb = new FastStringBuffer(name.length());
        block3: for (int i = 0; i < name.length(); ++i) {
            char c = name.charAt(i);
            switch (c) {
                case '\t': 
                case '\n': 
                case '\r': 
                case ' ': 
                case '_': {
                    continue block3;
                }
                default: {
                    fsb.cat(c);
                }
            }
        }
        return fsb.toString();
    }

    private static synchronized void readBlocks(Configuration config) throws RESyntaxException {
        NodeInfo item;
        TreeInfo doc;
        blocks = new HashMap<String, IntSet>(250);
        InputStream in = Configuration.locateResource("unicodeBlocks.xml", new ArrayList<String>(), new ArrayList<ClassLoader>());
        if (in == null) {
            throw new RESyntaxException("Unable to read unicodeBlocks.xml file");
        }
        ParseOptions options = new ParseOptions();
        options.setSchemaValidationMode(4);
        options.setDTDValidationMode(4);
        options.setSpaceStrippingRule(AllElementsSpaceStrippingRule.getInstance());
        try {
            doc = config.buildDocumentTree(new StreamSource(in, "unicodeBlocks.xml"), options);
        } catch (XPathException e) {
            throw new RESyntaxException("Failed to process unicodeBlocks.xml: " + e.getMessage());
        }
        AxisIterator iter = doc.getRootNode().iterateAxis(4, new NameTest(1, "", "block", config.getNamePool()));
        while ((item = iter.next()) != null) {
            String blockName = UnicodeBlocks.normalizeBlockName(item.getAttributeValue("", "name"));
            IntSet range = null;
            for (NodeInfo nodeInfo : item.children(NodeKindTest.ELEMENT)) {
                int from = Integer.parseInt(nodeInfo.getAttributeValue("", "from").substring(2), 16);
                int to = Integer.parseInt(nodeInfo.getAttributeValue("", "to").substring(2), 16);
                IntBlockSet cr = new IntBlockSet(from, to);
                if (range == null) {
                    range = cr;
                    continue;
                }
                if (range instanceof IntBlockSet) {
                    range = range.mutableCopy().union(cr);
                    continue;
                }
                range = range.union(cr);
            }
            blocks.put(blockName, range);
        }
    }
}

