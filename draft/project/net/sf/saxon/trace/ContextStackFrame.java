/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.trace;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.instruct.Actor;
import net.sf.saxon.expr.instruct.AttributeSet;
import net.sf.saxon.expr.instruct.GlobalVariable;
import net.sf.saxon.expr.instruct.NamedTemplate;
import net.sf.saxon.expr.instruct.TemplateRule;
import net.sf.saxon.expr.instruct.UserFunction;
import net.sf.saxon.lib.Logger;
import net.sf.saxon.ma.arrays.ArrayItem;
import net.sf.saxon.ma.map.MapItem;
import net.sf.saxon.om.Function;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.trans.KeyDefinition;
import net.sf.saxon.tree.util.Navigator;
import net.sf.saxon.value.AtomicValue;

public abstract class ContextStackFrame {
    private XPathContext context;
    private Location location;
    private Item contextItem;
    private Object container;

    public void setLocation(Location loc) {
        this.location = loc;
    }

    public String getSystemId() {
        return this.location.getSystemId();
    }

    public int getLineNumber() {
        return this.location.getLineNumber();
    }

    public void setComponent(Object container) {
        this.container = container;
    }

    public Object getContainer() {
        return this.container;
    }

    public void setContext(XPathContext context) {
        this.context = context;
    }

    public XPathContext getContext() {
        return this.context;
    }

    public void setContextItem(Item contextItem) {
        this.contextItem = contextItem;
    }

    public Item getContextItem() {
        return this.contextItem;
    }

    public abstract void print(Logger var1);

    protected String showLocation() {
        if (this.getSystemId() == null) {
            return "";
        }
        int line = this.getLineNumber();
        if (line == -1 || line == 1048575) {
            return "(" + this.getSystemId() + ")";
        }
        return "(" + this.getSystemId() + "#" + this.getLineNumber() + ")";
    }

    private static String displayContainer(Object container) {
        if (container instanceof Actor) {
            String objectName;
            StructuredQName name = ((Actor)container).getComponentName();
            String string = objectName = name == null ? "" : name.getDisplayName();
            if (container instanceof NamedTemplate) {
                return "template name=\"" + objectName + "\"";
            }
            if (container instanceof UserFunction) {
                return "function " + objectName + "()";
            }
            if (container instanceof AttributeSet) {
                return "attribute-set " + objectName;
            }
            if (container instanceof KeyDefinition) {
                return "key " + objectName;
            }
            if (container instanceof GlobalVariable) {
                StructuredQName qName = ((GlobalVariable)container).getVariableQName();
                if (qName.hasURI("http://saxon.sf.net/generated-variable")) {
                    return "optimizer-created global variable";
                }
                return "global variable $" + qName.getDisplayName();
            }
        } else if (container instanceof TemplateRule) {
            return "template match=\"" + ((TemplateRule)container).getMatchPattern().toString() + "\"";
        }
        return "";
    }

    public static class VariableEvaluation
    extends ContextStackFrame {
        StructuredQName variableName;

        public StructuredQName getVariableName() {
            return this.variableName;
        }

        public void setVariableName(StructuredQName variableName) {
            this.variableName = variableName;
        }

        @Override
        public void print(Logger out) {
            out.error("  in " + ContextStackFrame.displayContainer(this.getContainer()) + " " + this.showLocation());
        }
    }

    public static class CallTemplate
    extends ContextStackFrame {
        StructuredQName templateName;

        public StructuredQName getTemplateName() {
            return this.templateName;
        }

        public void setTemplateName(StructuredQName templateName) {
            this.templateName = templateName;
        }

        @Override
        public void print(Logger out) {
            String name = this.templateName == null ? "??" : this.templateName.getDisplayName();
            out.error("  at xsl:call-template name=\"" + name + "\" " + this.showLocation());
        }
    }

    public static class ApplyTemplates
    extends ContextStackFrame {
        @Override
        public void print(Logger out) {
            out.error("  at xsl:apply-templates " + this.showLocation());
            Item node = this.getContextItem();
            if (node instanceof NodeInfo) {
                out.error("     processing " + Navigator.getPath((NodeInfo)node));
            }
        }
    }

    public static class FunctionCall
    extends ContextStackFrame {
        StructuredQName functionName;

        public StructuredQName getFunctionName() {
            return this.functionName;
        }

        public void setFunctionName(StructuredQName functionName) {
            this.functionName = functionName;
        }

        @Override
        public void print(Logger out) {
            out.error("  at " + (this.functionName == null ? "(anonymous)" : this.functionName.getDisplayName()) + "() " + this.showLocation());
        }
    }

    public static class BuiltInTemplateRule
    extends ContextStackFrame {
        private XPathContext context;

        public BuiltInTemplateRule(XPathContext context) {
            this.context = context;
        }

        @Override
        public void print(Logger out) {
            Item contextItem = this.context.getContextItem();
            String diag = contextItem instanceof NodeInfo ? Navigator.getPath((NodeInfo)contextItem) : (contextItem instanceof AtomicValue ? "value " + contextItem.toString() : (contextItem instanceof MapItem ? "map" : (contextItem instanceof ArrayItem ? "array" : (contextItem instanceof Function ? "function" : "item"))));
            out.error("  in built-in template rule for " + diag + " in " + this.context.getCurrentMode().getActor().getModeTitle().toLowerCase());
        }
    }

    public static class CallingApplication
    extends ContextStackFrame {
        @Override
        public void print(Logger out) {
        }
    }
}

