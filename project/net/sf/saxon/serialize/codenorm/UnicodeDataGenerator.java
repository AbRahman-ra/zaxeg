/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.serialize.codenorm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.stream.StreamResult;
import net.sf.saxon.Configuration;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.event.StreamWriterToReceiver;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.util.FastStringBuffer;

class UnicodeDataGenerator {
    static final String copyright = "Copyright \u00ef\u00bf\u00bd 1998-1999 Unicode, Inc.";
    private static final boolean DEBUG = false;
    private static String dir;
    private static final String UNICODE_DATA = "UnicodeData.txt";
    private static final String COMPOSITION_EXCLUSIONS = "CompositionExclusions.txt";
    private static List<Integer> canonicalClassKeys;
    private static List<Integer> canonicalClassValues;
    private static List<Integer> decompositionKeys;
    private static List<String> decompositionValues;
    private static List<Integer> exclusionList;
    private static List<Integer> compatibilityList;

    private UnicodeDataGenerator() {
    }

    static void build() {
        try {
            UnicodeDataGenerator.readExclusionList();
            UnicodeDataGenerator.buildDecompositionTables();
        } catch (IOException e) {
            System.err.println("Can't load data file." + e + ", " + e.getMessage());
        }
    }

    private static void readExclusionList() throws IOException {
        String line;
        BufferedReader in = new BufferedReader(new FileReader(dir + '/' + COMPOSITION_EXCLUSIONS), 5120);
        while ((line = in.readLine()) != null) {
            int comment = line.indexOf(35);
            if (comment != -1) {
                line = line.substring(0, comment);
            }
            if (line.isEmpty()) continue;
            int z = line.indexOf(32);
            if (z < 0) {
                z = line.length();
            }
            int value = Integer.parseInt(line.substring(0, z), 16);
            exclusionList.add(value);
        }
        in.close();
    }

    private static void buildDecompositionTables() throws IOException {
        String line;
        BufferedReader in = new BufferedReader(new FileReader(dir + '/' + UNICODE_DATA), 65536);
        boolean counter = false;
        while ((line = in.readLine()) != null) {
            String decomp;
            boolean compat;
            int value;
            int comment = line.indexOf(35);
            if (comment != -1) {
                line = line.substring(0, comment);
            }
            if (line.isEmpty()) continue;
            int start = 0;
            int end = line.indexOf(59);
            try {
                value = Integer.parseInt(line.substring(start, end), 16);
            } catch (NumberFormatException e) {
                throw new IllegalStateException("Bad hex value in line:\n" + line);
            }
            end = line.indexOf(59, end + 1);
            end = line.indexOf(59, end + 1);
            start = end + 1;
            end = line.indexOf(59, start);
            int cc = Integer.parseInt(line.substring(start, end));
            if (cc != (cc & 0xFF)) {
                System.err.println("Bad canonical class at: " + line);
            }
            canonicalClassKeys.add(value);
            canonicalClassValues.add(cc);
            end = line.indexOf(59, end + 1);
            start = end + 1;
            if (start == (end = line.indexOf(59, start))) continue;
            String segment = line.substring(start, end);
            boolean bl = compat = segment.charAt(0) == '<';
            if (compat) {
                compatibilityList.add(value);
            }
            if ((decomp = UnicodeDataGenerator.fromHex(segment)).length() < 1 || decomp.length() > 2 && !compat) {
                System.err.println("Bad decomp at: " + line);
            }
            decompositionKeys.add(value);
            decompositionValues.add(decomp);
        }
        in.close();
    }

    public static String fromHex(String source) {
        FastStringBuffer result = new FastStringBuffer(8);
        block7: for (int i = 0; i < source.length(); ++i) {
            char c = source.charAt(i);
            switch (c) {
                case ' ': {
                    continue block7;
                }
                case '0': 
                case '1': 
                case '2': 
                case '3': 
                case '4': 
                case '5': 
                case '6': 
                case '7': 
                case '8': 
                case '9': 
                case 'A': 
                case 'B': 
                case 'C': 
                case 'D': 
                case 'E': 
                case 'F': 
                case 'a': 
                case 'b': 
                case 'c': 
                case 'd': 
                case 'e': 
                case 'f': {
                    int z = source.indexOf(32, i);
                    if (z < 0) {
                        z = source.length();
                    }
                    try {
                        result.cat((char)Integer.parseInt(source.substring(i, z), 16));
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException("Bad hex value in " + source);
                    }
                    i = z;
                    continue block7;
                }
                case '<': {
                    int j = source.indexOf(62, i);
                    if (j > 0) {
                        i = j;
                        continue block7;
                    }
                }
                default: {
                    throw new IllegalArgumentException("Bad hex value in " + source);
                }
            }
        }
        return result.toString();
    }

    public static String hex(char i) {
        String result = Integer.toString(i, 16).toUpperCase();
        return "0000".substring(result.length(), 4) + result;
    }

    public static String hex(String s, String sep) {
        FastStringBuffer result = new FastStringBuffer(20);
        for (int i = 0; i < s.length(); ++i) {
            if (i != 0) {
                result.append(sep);
            }
            result.append(UnicodeDataGenerator.hex(s.charAt(i)));
        }
        return result.toString();
    }

    private static void generateJava(PrintStream o) {
        o.println("package net.sf.saxon.serialize.codenorm;");
        o.println();
        o.println("//This module was generated by running net.sf.saxon.serialize.codenorm.UnicodeDataGenerator");
        o.println("//*** DO NOT EDIT! ***");
        o.println("//The strange format of this file is carefully chosen to avoid breaking Java compiler limits");
        o.println();
        o.println("public class UnicodeData {");
        o.println("public static final String[] canonicalClassKeys = {");
        UnicodeDataGenerator.printArray(o, canonicalClassKeys.iterator());
        o.println("};");
        o.println("public static final String[] canonicalClassValues = {");
        UnicodeDataGenerator.printArray(o, canonicalClassValues.iterator());
        o.println("};");
        o.println("public static final String[] decompositionKeys = {");
        UnicodeDataGenerator.printArray(o, decompositionKeys.iterator());
        o.println("};");
        o.println("public static final String[] decompositionValues = {");
        UnicodeDataGenerator.printStringArray(o, decompositionValues.iterator());
        o.println("};");
        o.println("public static final String[] exclusionList = {");
        UnicodeDataGenerator.printArray(o, exclusionList.iterator());
        o.println("};");
        o.println("public static final String[] compatibilityList = {");
        UnicodeDataGenerator.printArray(o, compatibilityList.iterator());
        o.println("};");
        o.println("}");
    }

    private static void generateXML(PrintStream o) throws XPathException, XMLStreamException {
        Configuration config = new Configuration();
        StreamResult result = new StreamResult(o);
        Receiver receiver = config.getSerializerFactory().getReceiver(result);
        StreamWriterToReceiver w = new StreamWriterToReceiver(receiver);
        w.writeStartDocument();
        w.writeStartElement("UnicodeData");
        w.writeAttribute("version", "6.0.0");
        w.writeStartElement("CanonicalClassKeys");
        w.writeAttribute("format", "base32chars");
        w.writeCharacters(UnicodeDataGenerator.base32array(canonicalClassKeys));
        w.writeEndElement();
        w.writeStartElement("CanonicalClassValues");
        w.writeAttribute("format", "base32chars,runLength");
        w.writeCharacters(UnicodeDataGenerator.base32arrayRunLength(canonicalClassValues));
        w.writeEndElement();
        w.writeStartElement("DecompositionKeys");
        w.writeAttribute("format", "base32chars");
        w.writeCharacters(UnicodeDataGenerator.base32array(decompositionKeys));
        w.writeEndElement();
        w.writeStartElement("DecompositionValues");
        w.writeAttribute("format", "UCS16Strings,base16");
        w.writeCharacters(UnicodeDataGenerator.base32StringArray(decompositionValues));
        w.writeEndElement();
        w.writeStartElement("ExclusionList");
        w.writeAttribute("format", "base32chars");
        w.writeCharacters(UnicodeDataGenerator.base32array(exclusionList));
        w.writeEndElement();
        w.writeStartElement("CompatibilityList");
        w.writeAttribute("format", "base32chars");
        w.writeCharacters(UnicodeDataGenerator.base32array(compatibilityList));
        w.writeEndElement();
        w.writeEndElement();
        w.writeEndDocument();
        w.close();
    }

    private static void printArray(PrintStream o, Iterator<Integer> iter) {
        int count = 0;
        FastStringBuffer buff = new FastStringBuffer(128);
        if (!iter.hasNext()) {
            return;
        }
        buff.cat('\"');
        while (true) {
            if (++count == 20) {
                count = 0;
                buff.append("\",");
                o.println(buff);
                buff.setLength(0);
                buff.cat('\"');
            }
            int next = iter.next();
            buff.append(Integer.toString(next, 32));
            if (!iter.hasNext()) break;
            buff.append(",");
        }
        buff.append("\"");
        o.println(buff);
    }

    private static String base32array(List<Integer> list) {
        int count = 0;
        Iterator<Integer> iter = list.iterator();
        FastStringBuffer buff = new FastStringBuffer(128);
        if (!iter.hasNext()) {
            return buff.toString();
        }
        while (true) {
            if (++count == 20) {
                count = 0;
                buff.append("\n");
            }
            int next = iter.next();
            buff.append(Integer.toString(next, 32));
            if (!iter.hasNext()) break;
            buff.append(" ");
        }
        buff.append("\n");
        return buff.toString();
    }

    private static String base32arrayRunLength(List<Integer> list) {
        int count = 0;
        Iterator<Integer> iter = list.iterator();
        FastStringBuffer buff = new FastStringBuffer(128);
        if (!iter.hasNext()) {
            return buff.toString();
        }
        int runLength = 1;
        int val = iter.next();
        while (true) {
            int next;
            if (iter.hasNext()) {
                next = iter.next();
                if (next == val) {
                    ++runLength;
                    continue;
                }
            } else {
                next = -1;
            }
            if (runLength != 1) {
                buff.append(Integer.toString(runLength));
                buff.append("*");
            }
            buff.append(Integer.toString(val, 32));
            if (++count == 20) {
                count = 0;
                buff.append("\n");
            } else {
                buff.append(" ");
            }
            val = next;
            runLength = 1;
            if (val == -1) break;
        }
        buff.append("\n");
        return buff.toString();
    }

    private static String base32StringArray(List<String> in) {
        Iterator<String> iter = in.iterator();
        int count = 0;
        FastStringBuffer buff = new FastStringBuffer(128);
        if (!iter.hasNext()) {
            return "";
        }
        while (true) {
            if (++count == 20) {
                count = 0;
                buff.append("\n");
            }
            String value = iter.next();
            for (int i = 0; i < value.length(); ++i) {
                char c = value.charAt(i);
                char b0 = "0123456789abcdef".charAt(c & 0xF);
                char b1 = "0123456789abcdef".charAt(c >> 4 & 0xF);
                char b2 = "0123456789abcdef".charAt(c >> 8 & 0xF);
                char b3 = "0123456789abcdef".charAt(c >> 12 & 0xF);
                buff.cat(b3);
                buff.cat(b2);
                buff.cat(b1);
                buff.cat(b0);
            }
            if (!iter.hasNext()) break;
            buff.append(" ");
        }
        return buff.toString();
    }

    private static void printStringArray(PrintStream o, Iterator<String> iter) {
        int count = 0;
        FastStringBuffer buff = new FastStringBuffer(128);
        if (!iter.hasNext()) {
            return;
        }
        while (true) {
            if (++count == 20) {
                count = 0;
                o.println(buff);
                buff.setLength(0);
            }
            String next = iter.next();
            UnicodeDataGenerator.appendJavaString(next, buff);
            if (!iter.hasNext()) break;
            buff.append(", ");
        }
        o.println(buff);
    }

    private static void appendJavaString(String value, FastStringBuffer buff) {
        buff.cat('\"');
        for (int i = 0; i < value.length(); ++i) {
            char c = value.charAt(i);
            if (c == '\\') {
                buff.append("\\\\");
                continue;
            }
            if (c == '\"') {
                buff.append("\\\"");
                continue;
            }
            if (c > ' ' && c < '\u007f') {
                buff.cat(c);
                continue;
            }
            buff.append("\\u");
            char b0 = "0123456789abcdef".charAt(c & 0xF);
            char b1 = "0123456789abcdef".charAt(c >> 4 & 0xF);
            char b2 = "0123456789abcdef".charAt(c >> 8 & 0xF);
            char b3 = "0123456789abcdef".charAt(c >> 12 & 0xF);
            buff.cat(b3);
            buff.cat(b2);
            buff.cat(b1);
            buff.cat(b0);
        }
        buff.cat('\"');
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.err.println("Usage: java UnicodeDataGenerator dir UnicodeData.java");
            System.err.println("where dir is the directory containing the files UnicodeData.text and CompositionExclusions.txt from the Unicode character database");
        }
        dir = args[0];
        UnicodeDataGenerator.build();
        PrintStream o = new PrintStream(new FileOutputStream(new File(args[1])));
        UnicodeDataGenerator.generateXML(o);
    }

    static {
        canonicalClassKeys = new ArrayList<Integer>(30000);
        canonicalClassValues = new ArrayList<Integer>(30000);
        decompositionKeys = new ArrayList<Integer>(6000);
        decompositionValues = new ArrayList<String>(6000);
        exclusionList = new ArrayList<Integer>(200);
        compatibilityList = new ArrayList<Integer>(8000);
    }
}

