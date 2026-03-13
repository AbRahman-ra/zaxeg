/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.trans;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.instruct.Instruction;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.value.Whitespace;

public class Err {
    public static final int ELEMENT = 1;
    public static final int ATTRIBUTE = 2;
    public static final int FUNCTION = 3;
    public static final int VALUE = 4;
    public static final int VARIABLE = 5;
    public static final int GENERAL = 6;
    public static final int URI = 7;
    public static final int EQNAME = 8;

    public static String wrap(CharSequence cs) {
        return Err.wrap(cs, 6);
    }

    public static String wrap(CharSequence cs, int valueType) {
        String s;
        if (cs == null) {
            return "(NULL)";
        }
        FastStringBuffer sb = new FastStringBuffer(64);
        int len = cs.length();
        block15: for (int i = 0; i < len; ++i) {
            char c = cs.charAt(i);
            switch (c) {
                case '\n': {
                    sb.append("\\n");
                    continue block15;
                }
                case '\t': {
                    sb.append("\\t");
                    continue block15;
                }
                case '\r': {
                    sb.append("\\r");
                    continue block15;
                }
                default: {
                    if (c < ' ') {
                        sb.append("\\x");
                        sb.append(Integer.toHexString(c));
                        continue block15;
                    }
                    sb.cat(c);
                }
            }
        }
        if (valueType == 1 || valueType == 2) {
            s = sb.toString();
            if (s.startsWith("{")) {
                s = "Q" + s;
            }
            if (s.startsWith("Q{")) {
                try {
                    StructuredQName qn = StructuredQName.fromEQName(sb.toString());
                    String uri = Err.abbreviateURI(qn.getURI());
                    s = "Q{" + uri + "}" + qn.getLocalPart();
                } catch (Exception e) {
                    s = sb.toString();
                }
            }
        } else {
            s = valueType == 7 ? Err.abbreviateURI(sb.toString()) : (valueType == 8 ? Err.abbreviateEQName(sb.toString()) : (len > 30 ? sb.toString().substring(0, 30) + "..." : sb.toString()));
        }
        switch (valueType) {
            case 1: {
                return "<" + s + ">";
            }
            case 2: {
                return "@" + s;
            }
            case 3: {
                return s + "()";
            }
            case 5: {
                return "$" + s;
            }
            case 4: {
                return "\"" + s + "\"";
            }
            case 8: {
                return s;
            }
        }
        return "{" + s + "}";
    }

    public static CharSequence depict(Item item) {
        if (item instanceof NodeInfo) {
            NodeInfo node = (NodeInfo)item;
            switch (node.getNodeKind()) {
                case 9: {
                    return "doc(" + Err.abbreviateURI(node.getSystemId()) + ')';
                }
                case 1: {
                    return '<' + node.getDisplayName() + '>';
                }
                case 2: {
                    return '@' + node.getDisplayName() + "=\"" + node.getStringValueCS() + '\"';
                }
                case 3: {
                    return "text{" + Err.truncate30(node.getStringValueCS()) + "}";
                }
                case 8: {
                    return "<!--...-->";
                }
                case 7: {
                    return "<?" + node.getLocalPart() + "...?>";
                }
                case 13: {
                    return "xmlns:" + node.getLocalPart() + "=" + Err.abbreviateURI(node.getStringValue());
                }
            }
            return "";
        }
        return item.toShortString();
    }

    public static CharSequence depictSequence(Sequence seq) {
        if (seq == null) {
            return "(*null*)";
        }
        try {
            GroundedValue val = seq.materialize();
            if (val.getLength() == 0) {
                return "()";
            }
            if (val.getLength() == 1) {
                return Err.depict(seq.head());
            }
            return Err.depictSequenceStart(val.iterate(), 3, val.getLength());
        } catch (Exception e) {
            return "(*unreadable*)";
        }
    }

    public static String depictSequenceStart(SequenceIterator seq, int max, int actual) {
        try {
            Item next;
            FastStringBuffer sb = new FastStringBuffer(64);
            int count = 0;
            sb.append(" (");
            while ((next = seq.next()) != null) {
                if (count++ > 0) {
                    sb.append(", ");
                }
                if (count > max) {
                    sb.append("... [" + actual + "])");
                    return sb.toString();
                }
                sb.cat(Err.depict(next));
            }
            sb.append(") ");
            return sb.toString();
        } catch (XPathException e) {
            return "";
        }
    }

    public static CharSequence truncate30(CharSequence cs) {
        if (cs.length() <= 30) {
            return Whitespace.collapseWhitespace(cs);
        }
        return Whitespace.collapseWhitespace(cs.subSequence(0, 30)) + "...";
    }

    public static String abbreviateURI(String uri) {
        if (uri == null) {
            return "";
        }
        int lastSlash = (uri.endsWith("/") ? uri.substring(0, uri.length() - 1) : uri).lastIndexOf(47);
        if (lastSlash < 0) {
            if (uri.length() > 15) {
                uri = "..." + uri.substring(uri.length() - 15);
            }
            return uri;
        }
        return "..." + uri.substring(lastSlash);
    }

    public static String abbreviateEQName(String eqName) {
        try {
            if (eqName.startsWith("{")) {
                eqName = "Q" + eqName;
            }
            StructuredQName sq = StructuredQName.fromEQName(eqName);
            return "Q{" + Err.abbreviateURI(sq.getURI()) + "}" + sq.getLocalPart();
        } catch (Exception e) {
            return eqName;
        }
    }

    public static String wrap(Expression exp) {
        if (ExpressionTool.expressionSize(exp) < 10 && !(exp instanceof Instruction)) {
            return "{" + exp + "}";
        }
        return exp.getExpressionName();
    }
}

