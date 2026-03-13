/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.value;

import net.sf.saxon.tree.iter.AtomicIterator;
import net.sf.saxon.tree.tiny.CharSlice;
import net.sf.saxon.tree.tiny.CompressedWhitespace;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.value.StringValue;

public class Whitespace {
    public static final int PRESERVE = 0;
    public static final int REPLACE = 1;
    public static final int COLLAPSE = 2;
    public static final int TRIM = 3;
    public static final int NONE = 0;
    public static final int IGNORABLE = 1;
    public static final int ALL = 2;
    public static final int UNSPECIFIED = 3;
    public static final int XSLT = 4;
    private static boolean[] C0WHITE = new boolean[]{false, false, false, false, false, false, false, false, false, true, true, false, false, true, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, true};

    private Whitespace() {
    }

    public static boolean isWhitespace(int ch) {
        switch (ch) {
            case 9: 
            case 10: 
            case 13: 
            case 32: {
                return true;
            }
        }
        return false;
    }

    public static CharSequence applyWhitespaceNormalization(int action, CharSequence value) {
        switch (action) {
            case 0: {
                return value;
            }
            case 1: {
                FastStringBuffer sb = new FastStringBuffer(value.length());
                block9: for (int i = 0; i < value.length(); ++i) {
                    char c = value.charAt(i);
                    switch (c) {
                        case '\t': 
                        case '\n': 
                        case '\r': {
                            sb.cat(' ');
                            continue block9;
                        }
                        default: {
                            sb.cat(c);
                        }
                    }
                }
                return sb;
            }
            case 2: {
                return Whitespace.collapseWhitespace(value);
            }
            case 3: {
                return Whitespace.trimWhitespace(value);
            }
        }
        throw new IllegalArgumentException("Unknown whitespace facet value");
    }

    public static CharSequence removeAllWhitespace(CharSequence value) {
        if (Whitespace.containsWhitespace(value)) {
            FastStringBuffer sb = new FastStringBuffer(value.length());
            for (int i = 0; i < value.length(); ++i) {
                char c = value.charAt(i);
                if (c <= ' ' && C0WHITE[c]) continue;
                sb.cat(c);
            }
            return sb;
        }
        return value;
    }

    public static CharSequence removeLeadingWhitespace(CharSequence value) {
        int len = value.length();
        if (len == 0 || value.charAt(0) > ' ') {
            return value;
        }
        int start = -1;
        for (int i = 0; i < len; ++i) {
            char c = value.charAt(i);
            if (c <= ' ' && C0WHITE[c]) continue;
            start = i;
            break;
        }
        if (start == 0) {
            return value;
        }
        if (start < 0 || start == len) {
            return "";
        }
        return value.subSequence(start, len);
    }

    public static boolean containsWhitespace(CharSequence value) {
        int i = value.length() - 1;
        while (i >= 0) {
            char c;
            if ((c = value.charAt(i--)) > ' ' || !C0WHITE[c]) continue;
            return true;
        }
        return false;
    }

    public static boolean isWhite(CharSequence content) {
        if (content instanceof CompressedWhitespace) {
            return true;
        }
        int len = content.length();
        int i = 0;
        while (i < len) {
            char c;
            if ((c = content.charAt(i++)) <= ' ' && C0WHITE[c]) continue;
            return false;
        }
        return true;
    }

    public static boolean isWhite(char c) {
        return c <= ' ' && C0WHITE[c];
    }

    public static CharSequence normalizeWhitespace(CharSequence in) {
        FastStringBuffer sb = new FastStringBuffer(in.length());
        block3: for (int i = 0; i < in.length(); ++i) {
            char c = in.charAt(i);
            switch (c) {
                case '\t': 
                case '\n': 
                case '\r': {
                    sb.cat(' ');
                    continue block3;
                }
                default: {
                    sb.cat(c);
                }
            }
        }
        return sb;
    }

    public static CharSequence collapseWhitespace(CharSequence in) {
        if (!Whitespace.containsWhitespace(in)) {
            return in;
        }
        int len = in.length();
        FastStringBuffer sb = new FastStringBuffer(len);
        boolean inWhitespace = true;
        block3: for (int i = 0; i < len; ++i) {
            char c = in.charAt(i);
            switch (c) {
                case '\t': 
                case '\n': 
                case '\r': 
                case ' ': {
                    if (inWhitespace) continue block3;
                    sb.cat(' ');
                    inWhitespace = true;
                    continue block3;
                }
                default: {
                    sb.cat(c);
                    inWhitespace = false;
                }
            }
        }
        int nlen = sb.length();
        if (nlen > 0 && sb.charAt(nlen - 1) == ' ') {
            sb.setLength(nlen - 1);
        }
        return sb;
    }

    public static CharSequence trimWhitespace(CharSequence in) {
        char x;
        if (in.length() == 0) {
            return in;
        }
        int first = 0;
        int last = in.length() - 1;
        while ((x = in.charAt(first)) <= ' ' && C0WHITE[x]) {
            if (first++ < last) continue;
            return "";
        }
        while ((x = in.charAt(last)) <= ' ' && C0WHITE[x]) {
            --last;
        }
        if (first == 0 && last == in.length() - 1) {
            return in;
        }
        return in.subSequence(first, last + 1);
    }

    public static String trim(CharSequence s) {
        if (s == null) {
            return null;
        }
        return Whitespace.trimWhitespace(s).toString();
    }

    public static class Tokenizer
    implements AtomicIterator<StringValue> {
        private char[] input;
        private int position;

        public Tokenizer(char[] input) {
            this.input = input;
            this.position = 0;
        }

        public Tokenizer(CharSequence input) {
            this.input = CharSlice.toCharArray(input);
            this.position = 0;
        }

        @Override
        public StringValue next() {
            int end;
            int start;
            int eol = this.input.length;
            for (start = this.position; start < eol && Whitespace.isWhite(this.input[start]); ++start) {
            }
            if (start >= eol) {
                return null;
            }
            for (end = start; end < eol && !Whitespace.isWhite(this.input[end]); ++end) {
            }
            this.position = end;
            return StringValue.makeStringValue(new CharSlice(this.input, start, end - start));
        }

        @Override
        public void close() {
        }
    }
}

