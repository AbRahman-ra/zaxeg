/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.parser;

import java.util.Optional;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.om.AxisInfo;
import net.sf.saxon.om.Item;
import net.sf.saxon.trans.Err;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.TypeHierarchy;
import net.sf.saxon.type.UType;
import net.sf.saxon.value.SequenceType;

public class RoleDiagnostic {
    private int kind;
    private String operation;
    private int operand;
    private String errorCode = "XPTY0004";
    public static final int FUNCTION = 0;
    public static final int BINARY_EXPR = 1;
    public static final int TYPE_OP = 2;
    public static final int VARIABLE = 3;
    public static final int INSTRUCTION = 4;
    public static final int FUNCTION_RESULT = 5;
    public static final int ORDER_BY = 6;
    public static final int TEMPLATE_RESULT = 7;
    public static final int PARAM = 8;
    public static final int UNARY_EXPR = 9;
    public static final int UPDATING_EXPR = 10;
    public static final int EVALUATE_RESULT = 12;
    public static final int CONTEXT_ITEM = 13;
    public static final int AXIS_STEP = 14;
    public static final int OPTION = 15;
    public static final int CHARACTER_MAP_EXPANSION = 16;
    public static final int MATCH_PATTERN = 19;
    public static final int MISC = 20;

    public RoleDiagnostic(int kind, String operation, int operand) {
        this.kind = kind;
        this.operation = operation;
        this.operand = operand;
    }

    public void setErrorCode(String code) {
        if (code != null) {
            this.errorCode = code;
        }
    }

    public String getErrorCode() {
        return this.errorCode;
    }

    public boolean isTypeError() {
        return !this.errorCode.startsWith("FORG") && !this.errorCode.equals("XPDY0050");
    }

    public String getMessage() {
        String name = this.operation;
        switch (this.kind) {
            case 0: {
                if (name.equals("saxon:call") || name.equals("saxon:apply")) {
                    if (this.operand == 0) {
                        return "target of the dynamic function call";
                    }
                    return RoleDiagnostic.ordinal(this.operand) + " argument of the dynamic function call";
                }
                return RoleDiagnostic.ordinal(this.operand + 1) + " argument of " + (name.isEmpty() ? "the anonymous function" : name + "()");
            }
            case 1: {
                return RoleDiagnostic.ordinal(this.operand + 1) + " operand of '" + name + '\'';
            }
            case 9: {
                return "operand of '-'";
            }
            case 2: {
                return "value in '" + name + "' expression";
            }
            case 3: {
                if (name.equals("saxon:context-item")) {
                    return "context item";
                }
                return "value of variable $" + name;
            }
            case 4: {
                int slash = name.indexOf(47);
                String attributeName = "";
                if (slash >= 0) {
                    attributeName = name.substring(slash + 1);
                    name = name.substring(0, slash);
                }
                return "@" + attributeName + " attribute of " + (name.equals("LRE") ? "a literal result element" : name);
            }
            case 5: {
                if (name.isEmpty()) {
                    return "result of the anonymous function";
                }
                return "result of a call to " + name;
            }
            case 7: {
                return "result of template " + name;
            }
            case 6: {
                return RoleDiagnostic.ordinal(this.operand + 1) + " sort key";
            }
            case 8: {
                return "value of parameter $" + name;
            }
            case 10: {
                return "value of the " + RoleDiagnostic.ordinal(this.operand + 1) + " operand of " + name + " expression";
            }
            case 12: {
                return "result of the expression {" + name + "} evaluated by xsl:evaluate";
            }
            case 13: {
                return "context item";
            }
            case 14: {
                return "context item for the " + AxisInfo.axisName[this.operand] + " axis";
            }
            case 15: {
                return "value of the " + name + " option";
            }
            case 16: {
                return "substitute value for character '" + name + "' in the character map";
            }
            case 19: {
                return "match pattern";
            }
            case 20: {
                return this.operation;
            }
        }
        return "";
    }

    public String composeRequiredMessage(ItemType requiredItemType) {
        return "The required item type of the " + this.getMessage() + " is " + requiredItemType;
    }

    public String composeErrorMessage(ItemType requiredItemType, ItemType suppliedItemType) {
        return this.composeRequiredMessage(requiredItemType) + "; supplied value has item type " + suppliedItemType;
    }

    public String composeErrorMessage(ItemType requiredItemType, Expression supplied, TypeHierarchy th) {
        if (supplied instanceof Literal) {
            String s = this.composeRequiredMessage(requiredItemType);
            Optional<String> more = SequenceType.makeSequenceType(requiredItemType, 57344).explainMismatch(((Literal)supplied).getValue(), th);
            if (more.isPresent()) {
                s = s + ". " + more.get();
            }
            return s;
        }
        return this.composeRequiredMessage(requiredItemType) + ", but the supplied expression {" + supplied.toShortString() + "} has item type " + supplied.getItemType();
    }

    public String composeErrorMessage(ItemType requiredItemType, Item item, TypeHierarchy th) {
        FastStringBuffer message = new FastStringBuffer(256);
        message.append(this.composeRequiredMessage(requiredItemType));
        message.append("; the supplied value ");
        message.cat(Err.depict(item));
        if (requiredItemType.getGenre() != item.getGenre()) {
            message.append(" is ");
            message.append(item.getGenre().getDescription());
        } else {
            message.append(" does not match. ");
            if (th != null) {
                Optional<String> more = requiredItemType.explainMismatch(item, th);
                more.ifPresent(message::append);
            }
        }
        return message.toString();
    }

    public String composeErrorMessage(ItemType requiredItemType, UType suppliedItemType) {
        return this.composeRequiredMessage(requiredItemType) + "; supplied value has item type " + suppliedItemType;
    }

    public String save() {
        FastStringBuffer fsb = new FastStringBuffer(256);
        fsb.append(this.kind + "|");
        fsb.append(this.operand + "|");
        fsb.append(this.errorCode.equals("XPTY0004") ? "" : this.errorCode);
        fsb.append("|");
        fsb.append(this.operation);
        return fsb.toString();
    }

    public static RoleDiagnostic reconstruct(String in) {
        int v = in.indexOf(124);
        int kind = Integer.parseInt(in.substring(0, v));
        int w = in.indexOf(124, v + 1);
        int operand = Integer.parseInt(in.substring(v + 1, w));
        int x = in.indexOf(124, w + 1);
        String errorCode = in.substring(w + 1, x);
        String operation = in.substring(x + 1);
        RoleDiagnostic cd = new RoleDiagnostic(kind, operation, operand);
        if (!errorCode.isEmpty()) {
            cd.setErrorCode(errorCode);
        }
        return cd;
    }

    public static String ordinal(int n) {
        switch (n) {
            case 1: {
                return "first";
            }
            case 2: {
                return "second";
            }
            case 3: {
                return "third";
            }
        }
        if (n >= 21) {
            switch (n % 10) {
                case 1: {
                    return n + "st";
                }
                case 2: {
                    return n + "nd";
                }
                case 3: {
                    return n + "rd";
                }
            }
        }
        return n + "th";
    }
}

