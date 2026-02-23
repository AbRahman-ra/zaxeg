/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.lib;

import java.util.List;
import javax.xml.transform.SourceLocator;
import javax.xml.transform.dom.DOMLocator;
import net.sf.saxon.Controller;
import net.sf.saxon.expr.Component;
import net.sf.saxon.expr.ContextOriginator;
import net.sf.saxon.expr.Locatable;
import net.sf.saxon.expr.UserFunctionCall;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.XPathContextMajor;
import net.sf.saxon.expr.instruct.Actor;
import net.sf.saxon.expr.instruct.AttributeSet;
import net.sf.saxon.expr.instruct.FixedAttribute;
import net.sf.saxon.expr.instruct.FixedElement;
import net.sf.saxon.expr.instruct.GlobalParam;
import net.sf.saxon.expr.instruct.GlobalVariable;
import net.sf.saxon.expr.instruct.Instruction;
import net.sf.saxon.expr.instruct.NamedTemplate;
import net.sf.saxon.expr.instruct.UserFunction;
import net.sf.saxon.expr.parser.Loc;
import net.sf.saxon.expr.parser.XPathParser;
import net.sf.saxon.lib.Logger;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.StandardNames;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.regex.BMPString;
import net.sf.saxon.regex.GeneralUnicodeString;
import net.sf.saxon.regex.LatinString;
import net.sf.saxon.regex.UnicodeString;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.serialize.charcode.UTF16CharacterSet;
import net.sf.saxon.trans.Err;
import net.sf.saxon.trans.KeyDefinition;
import net.sf.saxon.trans.Mode;
import net.sf.saxon.trans.rules.BuiltInRuleSet;
import net.sf.saxon.trans.rules.Rule;
import net.sf.saxon.tree.AttributeLocation;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.tree.util.Navigator;
import net.sf.saxon.type.Type;
import net.sf.saxon.type.ValidationException;
import net.sf.saxon.type.ValidationFailure;

public class StandardDiagnostics {
    public int MAX_MESSAGE_LENGTH = 2000;
    public int MAX_MESSAGE_LINE_LENGTH = 100;
    public int MIN_MESSAGE_LINE_LENGTH = 10;
    public int TARGET_MESSAGE_LINE_LENGTH = 90;

    public String getLocationMessageText(SourceLocator loc) {
        boolean containsLineNumber;
        String path;
        String locMessage = "";
        String systemId = null;
        NodeInfo node = null;
        String nodeMessage = null;
        int lineNumber = -1;
        if (loc == null) {
            loc = Loc.NONE;
        }
        if (loc instanceof XPathParser.NestedLocation) {
            loc = ((XPathParser.NestedLocation)loc).getContainingLocation();
        }
        if (loc instanceof AttributeLocation) {
            AttributeLocation saLoc = (AttributeLocation)loc;
            nodeMessage = "in " + saLoc.getElementName().getDisplayName();
            if (saLoc.getAttributeName() != null) {
                nodeMessage = nodeMessage + "/@" + saLoc.getAttributeName();
            }
            nodeMessage = nodeMessage + ' ';
        } else if (loc instanceof DOMLocator) {
            nodeMessage = "at " + ((DOMLocator)loc).getOriginatingNode().getNodeName() + ' ';
        } else if (loc instanceof NodeInfo) {
            node = (NodeInfo)loc;
            nodeMessage = "at " + node.getDisplayName() + ' ';
        } else if (loc instanceof ValidationException && (node = ((ValidationException)((Object)loc)).getNode()) != null) {
            nodeMessage = "at " + node.getDisplayName() + ' ';
        } else if (loc instanceof ValidationException && loc.getLineNumber() == -1 && (path = ((ValidationException)((Object)loc)).getPath()) != null) {
            nodeMessage = "at " + path + ' ';
        } else if (loc instanceof Instruction) {
            String instructionName = this.getInstructionName((Instruction)((Object)loc));
            if (!"".equals(instructionName)) {
                nodeMessage = "at " + instructionName + ' ';
            }
            systemId = loc.getSystemId();
            lineNumber = loc.getLineNumber();
        } else if (loc instanceof Actor) {
            String kind = "procedure";
            if (loc instanceof UserFunction) {
                kind = "function";
            } else if (loc instanceof NamedTemplate) {
                kind = "template";
            } else if (loc instanceof AttributeSet) {
                kind = "attribute-set";
            } else if (loc instanceof KeyDefinition) {
                kind = "key";
            } else if (loc instanceof GlobalParam) {
                kind = "parameter";
            } else if (loc instanceof GlobalVariable) {
                kind = "variable";
            } else if (loc instanceof Mode) {
                kind = "mode";
            }
            systemId = loc.getSystemId();
            lineNumber = loc.getLineNumber();
            nodeMessage = "at " + kind + " ";
            StructuredQName name = ((Actor)loc).getComponentName();
            if (name != null) {
                String n = name.toString();
                if (n.equals("xsl:unnamed")) {
                    n = "(unnamed)";
                }
                nodeMessage = nodeMessage + n;
                nodeMessage = nodeMessage + " ";
            }
        }
        if (lineNumber == -1) {
            lineNumber = loc.getLineNumber();
        }
        boolean bl = containsLineNumber = lineNumber > 0;
        if (node != null && !containsLineNumber) {
            nodeMessage = "at " + Navigator.getPath(node) + ' ';
        }
        if (nodeMessage != null) {
            locMessage = locMessage + nodeMessage;
        }
        if (containsLineNumber) {
            locMessage = locMessage + "on line " + lineNumber + ' ';
            if (loc.getColumnNumber() != -1) {
                locMessage = locMessage + "column " + loc.getColumnNumber() + ' ';
            }
        }
        if (systemId != null && systemId.isEmpty()) {
            systemId = null;
        }
        if (systemId == null) {
            try {
                systemId = loc.getSystemId();
            } catch (Exception err) {
                err.printStackTrace();
            }
        }
        if (systemId != null && !systemId.isEmpty()) {
            locMessage = locMessage + (containsLineNumber ? "of " : "in ") + this.abbreviateLocationURI(systemId) + ':';
        }
        return locMessage;
    }

    public String getInstructionName(Instruction inst) {
        return StandardDiagnostics.getInstructionNameDefault(inst);
    }

    public static String getInstructionNameDefault(Instruction inst) {
        try {
            if (inst instanceof FixedElement) {
                StructuredQName qName = inst.getObjectName();
                return "element constructor <" + qName.getDisplayName() + '>';
            }
            if (inst instanceof FixedAttribute) {
                StructuredQName qName = inst.getObjectName();
                return "attribute constructor " + qName.getDisplayName() + "=\"{...}\"";
            }
            int construct = inst.getInstructionNameCode();
            if (construct < 0) {
                return "";
            }
            if (construct < 1024 && construct != 158 && construct != 200) {
                if (inst.getPackageData().isXSLT()) {
                    return StandardNames.getDisplayName(construct);
                }
                String s = StandardNames.getDisplayName(construct);
                int colon = s.indexOf(58);
                if (colon > 0) {
                    String local = s.substring(colon + 1);
                    if (local.equals("document")) {
                        return "document node constructor";
                    }
                    if (local.equals("text") || s.equals("value-of")) {
                        return "text node constructor";
                    }
                    if (local.equals("element")) {
                        return "computed element constructor";
                    }
                    if (local.equals("attribute")) {
                        return "computed attribute constructor";
                    }
                    if (local.equals("variable")) {
                        return "variable declaration";
                    }
                    if (local.equals("param")) {
                        return "external variable declaration";
                    }
                    if (local.equals("comment")) {
                        return "comment constructor";
                    }
                    if (local.equals("processing-instruction")) {
                        return "processing-instruction constructor";
                    }
                    if (local.equals("namespace")) {
                        return "namespace node constructor";
                    }
                }
                return s;
            }
            return "";
        } catch (Exception err) {
            return "";
        }
    }

    public void printStackTrace(XPathContext context, Logger out, int level) {
        if (level > 0) {
            int depth = 20;
            while (depth-- > 0) {
                Component component = context.getCurrentComponent();
                if (component != null) {
                    if (component.getActor() instanceof Mode) {
                        Rule rule = context.getCurrentTemplateRule();
                        if (rule != null) {
                            StringBuilder sb = new StringBuilder();
                            Location loc = rule.getPattern().getLocation();
                            sb.append("  In template rule with match=\"").append(rule.getPattern().toShortString()).append("\" ");
                            if (loc != null && loc.getLineNumber() != -1) {
                                sb.append("on line ").append(loc.getLineNumber()).append(" ");
                            }
                            if (loc != null && loc.getSystemId() != null) {
                                sb.append("of ").append(this.abbreviateLocationURI(loc.getSystemId()));
                            }
                            out.error(sb.toString());
                        }
                    } else {
                        out.error(this.getLocationMessageText(component.getActor()).replace("$at ", "In "));
                    }
                }
                try {
                    context.getStackFrame().getStackFrameMap().showStackFrame(context, out);
                } catch (Exception rule) {
                    // empty catch block
                }
                while (!(context instanceof XPathContextMajor)) {
                    context = context.getCaller();
                }
                ContextOriginator originator = ((XPathContextMajor)context).getOrigin();
                if (originator == null || originator instanceof Controller) {
                    return;
                }
                out.error("     invoked by " + this.showOriginator(originator));
                context = context.getCaller();
            }
        }
    }

    protected String showOriginator(ContextOriginator originator) {
        Location loc;
        StringBuilder sb = new StringBuilder();
        if (originator == null) {
            sb.append("unknown caller (null)");
        } else if (originator instanceof Instruction) {
            sb.append(this.getInstructionName((Instruction)((Object)originator)));
        } else if (originator instanceof UserFunctionCall) {
            sb.append("function call");
        } else if (originator instanceof Controller) {
            sb.append("external application");
        } else if (originator instanceof BuiltInRuleSet) {
            sb.append("built-in template rule (").append(((BuiltInRuleSet)originator).getName()).append(")");
        } else if (originator instanceof KeyDefinition) {
            sb.append("xsl:key definition");
        } else if (originator instanceof GlobalParam) {
            sb.append("global parameter ").append(((GlobalParam)originator).getVariableQName().getDisplayName());
        } else if (originator instanceof GlobalVariable) {
            sb.append(((GlobalVariable)originator).getDescription());
        } else {
            sb.append("unknown caller (").append(originator.getClass()).append(")");
        }
        if (originator instanceof Locatable && (loc = ((Locatable)((Object)originator)).getLocation()).getLineNumber() != -1) {
            sb.append(" at ").append(loc.getSystemId() == null ? "line " : loc.getSystemId() + "#");
            sb.append(loc.getLineNumber());
        }
        return sb.toString();
    }

    protected String formatListOfOffendingNodes(ValidationFailure failure) {
        StringBuilder message = new StringBuilder();
        List<NodeInfo> offendingNodes = failure.getOffendingNodes();
        if (!offendingNodes.isEmpty()) {
            message.append("\n  Nodes for which the assertion fails:");
            for (NodeInfo offender : offendingNodes) {
                String nodeDesc = Type.displayTypeName(offender);
                if (offender.getNodeKind() == 3) {
                    nodeDesc = nodeDesc + " " + Err.wrap(offender.getStringValueCS(), 4);
                }
                if (offender.getLineNumber() != -1) {
                    nodeDesc = nodeDesc + " on line " + offender.getLineNumber();
                    if (offender.getColumnNumber() != -1) {
                        nodeDesc = nodeDesc + " column " + offender.getColumnNumber();
                    }
                    if (offender.getSystemId() != null) {
                        nodeDesc = nodeDesc + " of " + offender.getSystemId();
                    }
                } else {
                    nodeDesc = nodeDesc + " at " + Navigator.getPath(offender);
                }
                message.append("\n  * ").append(nodeDesc);
            }
        }
        return message.toString();
    }

    public String abbreviateLocationURI(String uri) {
        return StandardDiagnostics.abbreviateLocationURIDefault(uri);
    }

    public static String abbreviateLocationURIDefault(String uri) {
        if (uri == null) {
            return "*unknown*";
        }
        int slash = uri.lastIndexOf(47);
        if (slash >= 0 && slash < uri.length() - 1) {
            return uri.substring(slash + 1);
        }
        return uri;
    }

    public String wordWrap(String message) {
        int nl;
        if (message.length() > this.MAX_MESSAGE_LENGTH) {
            message = message.substring(0, this.MAX_MESSAGE_LENGTH);
        }
        if ((nl = message.indexOf(10)) < 0) {
            nl = message.length();
        }
        if (nl > this.MAX_MESSAGE_LINE_LENGTH) {
            int i;
            for (i = this.TARGET_MESSAGE_LINE_LENGTH; message.charAt(i) != ' ' && i > 0; --i) {
            }
            if (i > this.MIN_MESSAGE_LINE_LENGTH) {
                return message.substring(0, i) + "\n  " + this.wordWrap(message.substring(i + 1));
            }
            return message;
        }
        if (nl < message.length()) {
            return message.substring(0, nl) + '\n' + this.wordWrap(message.substring(nl + 1));
        }
        return message;
    }

    public CharSequence expandSpecialCharacters(CharSequence in, int threshold) {
        if (threshold >= 0x10FFFF) {
            return in;
        }
        int max = 0;
        boolean isAstral = false;
        for (int i = 0; i < in.length(); ++i) {
            char c = in.charAt(i);
            if (c > max) {
                max = c;
            }
            if (!UTF16CharacterSet.isSurrogate(c)) continue;
            isAstral = true;
        }
        if (max <= threshold && !isAstral) {
            return in;
        }
        UnicodeString str = max <= 255 ? new LatinString(in) : (!isAstral ? new BMPString(in) : new GeneralUnicodeString(in));
        FastStringBuffer fsb = new FastStringBuffer(str.uLength() * 2);
        for (int i = 0; i < str.uLength(); ++i) {
            int ch = str.uCharAt(i);
            fsb.appendWideChar(ch);
            if (ch <= threshold) continue;
            fsb.append("[x");
            fsb.append(Integer.toHexString(ch));
            fsb.append("]");
        }
        return fsb;
    }
}

