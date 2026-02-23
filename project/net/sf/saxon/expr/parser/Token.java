/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.parser;

import java.util.HashMap;

public abstract class Token {
    public static final int IMPLICIT_EOF = -1;
    public static final int EOF = 0;
    public static final int UNION = 1;
    public static final int SLASH = 2;
    public static final int AT = 3;
    public static final int LSQB = 4;
    public static final int LPAR = 5;
    public static final int EQUALS = 6;
    public static final int COMMA = 7;
    public static final int SLASH_SLASH = 8;
    public static final int OR = 9;
    public static final int AND = 10;
    public static final int GT = 11;
    public static final int LT = 12;
    public static final int GE = 13;
    public static final int LE = 14;
    public static final int PLUS = 15;
    public static final int MINUS = 16;
    public static final int MULT = 17;
    public static final int DIV = 18;
    public static final int MOD = 19;
    public static final int IS = 20;
    public static final int DOLLAR = 21;
    public static final int NE = 22;
    public static final int INTERSECT = 23;
    public static final int EXCEPT = 24;
    public static final int RETURN = 25;
    public static final int THEN = 26;
    public static final int ELSE = 27;
    public static final int WHERE = 28;
    public static final int TO = 29;
    public static final int CONCAT = 30;
    public static final int IN = 31;
    public static final int SOME = 32;
    public static final int EVERY = 33;
    public static final int SATISFIES = 34;
    public static final int FUNCTION = 35;
    public static final int AXIS = 36;
    public static final int IF = 37;
    public static final int PRECEDES = 38;
    public static final int FOLLOWS = 39;
    public static final int BANG = 40;
    public static final int COLONCOLON = 41;
    public static final int COLONSTAR = 42;
    public static final int NAMED_FUNCTION_REF = 43;
    public static final int HASH = 44;
    public static final int INSTANCE_OF = 45;
    public static final int CAST_AS = 46;
    public static final int TREAT_AS = 47;
    public static final int FEQ = 50;
    public static final int FNE = 51;
    public static final int FGT = 52;
    public static final int FLT = 53;
    public static final int FGE = 54;
    public static final int FLE = 55;
    public static final int IDIV = 56;
    public static final int CASTABLE_AS = 57;
    public static final int ASSIGN = 58;
    public static final int LCURLY = 59;
    public static final int KEYWORD_CURLY = 60;
    public static final int ELEMENT_QNAME = 61;
    public static final int ATTRIBUTE_QNAME = 62;
    public static final int PI_QNAME = 63;
    public static final int NAMESPACE_QNAME = 64;
    public static final int TYPESWITCH = 65;
    public static final int SWITCH = 66;
    public static final int CASE = 67;
    public static final int MODIFY = 68;
    public static final int NODEKIND = 69;
    public static final int SUFFIX = 70;
    public static final int AS = 71;
    public static final int GROUP_BY = 72;
    public static final int FOR_TUMBLING = 73;
    public static final int FOR_SLIDING = 74;
    public static final int FOR_MEMBER = 75;
    public static final int COLON = 76;
    public static final int ARROW = 77;
    public static final int STRING_CONSTRUCTOR_INITIAL = 78;
    public static final int OTHERWISE = 79;
    public static final int AND_ALSO = 80;
    public static final int OR_ELSE = 81;
    public static final int XQUERY_VERSION = 88;
    public static final int XQUERY_ENCODING = 89;
    public static final int DECLARE_NAMESPACE = 90;
    public static final int DECLARE_DEFAULT = 91;
    public static final int DECLARE_CONSTRUCTION = 92;
    public static final int DECLARE_BASEURI = 93;
    public static final int DECLARE_BOUNDARY_SPACE = 94;
    public static final int DECLARE_DECIMAL_FORMAT = 95;
    public static final int IMPORT_SCHEMA = 96;
    public static final int IMPORT_MODULE = 97;
    public static final int DECLARE_VARIABLE = 98;
    public static final int DECLARE_CONTEXT = 99;
    public static final int DECLARE_FUNCTION = 100;
    public static final int MODULE_NAMESPACE = 101;
    public static final int VALIDATE = 102;
    public static final int VALIDATE_STRICT = 103;
    public static final int VALIDATE_LAX = 104;
    public static final int VALIDATE_TYPE = 105;
    public static final int PERCENT = 106;
    public static final int DECLARE_ORDERING = 107;
    public static final int DECLARE_COPY_NAMESPACES = 108;
    public static final int DECLARE_OPTION = 109;
    public static final int DECLARE_REVALIDATION = 110;
    public static final int INSERT_NODE = 111;
    public static final int DELETE_NODE = 112;
    public static final int REPLACE_NODE = 113;
    public static final int REPLACE_VALUE = 114;
    public static final int RENAME_NODE = 115;
    public static final int FIRST_INTO = 116;
    public static final int LAST_INTO = 117;
    public static final int AFTER = 118;
    public static final int BEFORE = 119;
    public static final int INTO = 120;
    public static final int WITH = 121;
    public static final int DECLARE_UPDATING = 122;
    public static final int DECLARE_ANNOTATED = 123;
    public static final int DECLARE_TYPE = 124;
    public static final int SEMICOLON = 149;
    static int LAST_OPERATOR = 150;
    public static final int NAME = 201;
    public static final int STRING_LITERAL = 202;
    public static final int RSQB = 203;
    public static final int RPAR = 204;
    public static final int DOT = 205;
    public static final int DOTDOT = 206;
    public static final int STAR = 207;
    public static final int PREFIX = 208;
    public static final int NUMBER = 209;
    public static final int FOR = 211;
    public static final int DEFAULT = 212;
    public static final int QMARK = 213;
    public static final int RCURLY = 215;
    public static final int LET = 216;
    public static final int TAG = 217;
    public static final int PRAGMA = 218;
    public static final int COPY = 219;
    public static final int COUNT = 220;
    public static final int STRING_LITERAL_BACKTICKED = 222;
    public static final int NEGATE = 299;
    public static final String[] tokens = new String[300];
    public static HashMap<String, Integer> doubleKeywords;
    public static final int UNKNOWN = -1;

    private Token() {
    }

    private static void mapDouble(String doubleKeyword, int token) {
        doubleKeywords.put(doubleKeyword, token);
        Token.tokens[token] = doubleKeyword;
    }

    public static int inverse(int operator) {
        switch (operator) {
            case 12: {
                return 11;
            }
            case 14: {
                return 13;
            }
            case 11: {
                return 12;
            }
            case 13: {
                return 14;
            }
            case 53: {
                return 52;
            }
            case 55: {
                return 54;
            }
            case 52: {
                return 53;
            }
            case 54: {
                return 55;
            }
        }
        return operator;
    }

    public static int negate(int operator) {
        switch (operator) {
            case 50: {
                return 51;
            }
            case 51: {
                return 50;
            }
            case 53: {
                return 54;
            }
            case 55: {
                return 52;
            }
            case 52: {
                return 55;
            }
            case 54: {
                return 53;
            }
        }
        throw new IllegalArgumentException("Invalid operator for negate()");
    }

    public static boolean isOrderedOperator(int operator) {
        return operator != 50 && operator != 51;
    }

    static {
        Token.tokens[0] = "<eof>";
        Token.tokens[1] = "|";
        Token.tokens[2] = "/";
        Token.tokens[3] = "@";
        Token.tokens[4] = "[";
        Token.tokens[5] = "(";
        Token.tokens[6] = "=";
        Token.tokens[7] = ",";
        Token.tokens[8] = "//";
        Token.tokens[9] = "or";
        Token.tokens[10] = "and";
        Token.tokens[11] = ">";
        Token.tokens[12] = "<";
        Token.tokens[13] = ">=";
        Token.tokens[14] = "<=";
        Token.tokens[15] = "+";
        Token.tokens[16] = "-";
        Token.tokens[17] = "*";
        Token.tokens[18] = "div";
        Token.tokens[19] = "mod";
        Token.tokens[20] = "is";
        Token.tokens[21] = "$";
        Token.tokens[22] = "!=";
        Token.tokens[40] = "!";
        Token.tokens[30] = "||";
        Token.tokens[23] = "intersect";
        Token.tokens[24] = "except";
        Token.tokens[25] = "return";
        Token.tokens[26] = "then";
        Token.tokens[27] = "else";
        Token.tokens[29] = "to";
        Token.tokens[31] = "in";
        Token.tokens[32] = "some";
        Token.tokens[33] = "every";
        Token.tokens[34] = "satisfies";
        Token.tokens[35] = "<function>(";
        Token.tokens[36] = "<axis>";
        Token.tokens[37] = "if(";
        Token.tokens[38] = "<<";
        Token.tokens[39] = ">>";
        Token.tokens[41] = "::";
        Token.tokens[42] = ":*";
        Token.tokens[44] = "#";
        Token.tokens[45] = "instance of";
        Token.tokens[46] = "cast as";
        Token.tokens[47] = "treat as";
        Token.tokens[50] = "eq";
        Token.tokens[51] = "ne";
        Token.tokens[52] = "gt";
        Token.tokens[54] = "ge";
        Token.tokens[53] = "lt";
        Token.tokens[55] = "le";
        Token.tokens[56] = "idiv";
        Token.tokens[57] = "castable as";
        Token.tokens[58] = ":=";
        Token.tokens[66] = "switch";
        Token.tokens[65] = "typeswitch";
        Token.tokens[67] = "case";
        Token.tokens[212] = "default";
        Token.tokens[118] = "after";
        Token.tokens[119] = "before";
        Token.tokens[120] = "into";
        Token.tokens[121] = "with";
        Token.tokens[68] = "modify";
        Token.tokens[71] = "as";
        Token.tokens[76] = ":";
        Token.tokens[77] = "=>";
        Token.tokens[80] = "andAlso";
        Token.tokens[81] = "orElse";
        Token.tokens[78] = "``[<string>`{";
        Token.tokens[222] = "``[<string>]``";
        Token.tokens[79] = "otherwise";
        Token.tokens[201] = "<name>";
        Token.tokens[202] = "<string-literal>";
        Token.tokens[203] = "]";
        Token.tokens[204] = ")";
        Token.tokens[205] = ".";
        Token.tokens[206] = "..";
        Token.tokens[207] = "*";
        Token.tokens[208] = "<prefix:*>";
        Token.tokens[209] = "<numeric-literal>";
        Token.tokens[69] = "<node-type>()";
        Token.tokens[211] = "for";
        Token.tokens[70] = "<*:local-name>";
        Token.tokens[213] = "?";
        Token.tokens[59] = "{";
        Token.tokens[60] = "<keyword> {";
        Token.tokens[215] = "}";
        Token.tokens[216] = "let";
        Token.tokens[102] = "validate {";
        Token.tokens[217] = "<element>";
        Token.tokens[218] = "(# ... #)";
        Token.tokens[149] = ";";
        Token.tokens[219] = "copy";
        Token.tokens[299] = "-";
        Token.tokens[106] = "%";
        doubleKeywords = new HashMap(30);
        Token.mapDouble("instance of", 45);
        Token.mapDouble("cast as", 46);
        Token.mapDouble("treat as", 47);
        Token.mapDouble("castable as", 57);
        Token.mapDouble("group by", 72);
        Token.mapDouble("for tumbling", 73);
        Token.mapDouble("for sliding", 74);
        Token.mapDouble("for member", 75);
        Token.mapDouble("xquery version", 88);
        Token.mapDouble("xquery encoding", 89);
        Token.mapDouble("declare namespace", 90);
        Token.mapDouble("declare default", 91);
        Token.mapDouble("declare construction", 92);
        Token.mapDouble("declare base-uri", 93);
        Token.mapDouble("declare boundary-space", 94);
        Token.mapDouble("declare decimal-format", 95);
        Token.mapDouble("declare ordering", 107);
        Token.mapDouble("declare copy-namespaces", 108);
        Token.mapDouble("declare option", 109);
        Token.mapDouble("declare revalidation", 110);
        Token.mapDouble("declare type", 124);
        Token.mapDouble("import schema", 96);
        Token.mapDouble("import module", 97);
        Token.mapDouble("declare variable", 98);
        Token.mapDouble("declare context", 99);
        Token.mapDouble("declare function", 100);
        Token.mapDouble("declare updating", 122);
        Token.mapDouble("module namespace", 101);
        Token.mapDouble("validate strict", 103);
        Token.mapDouble("validate lax", 104);
        Token.mapDouble("validate type", 105);
        Token.mapDouble("insert node", 111);
        Token.mapDouble("insert nodes", 111);
        Token.mapDouble("delete node", 112);
        Token.mapDouble("delete nodes", 112);
        Token.mapDouble("replace node", 113);
        Token.mapDouble("replace value", 114);
        Token.mapDouble("rename node", 115);
        Token.mapDouble("rename nodes", 115);
        Token.mapDouble("first into", 116);
        Token.mapDouble("last into", 117);
    }
}

