/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.jaxen.expr;

import org.jaxen.ContextSupport;
import org.jaxen.Navigator;
import org.jaxen.expr.DefaultStep;
import org.jaxen.expr.PredicateSet;
import org.jaxen.expr.ProcessingInstructionNodeStep;
import org.jaxen.expr.iter.IterableAxis;

public class DefaultProcessingInstructionNodeStep
extends DefaultStep
implements ProcessingInstructionNodeStep {
    private static final long serialVersionUID = -4825000697808126927L;
    private String name;

    public DefaultProcessingInstructionNodeStep(IterableAxis axis, String name, PredicateSet predicateSet) {
        super(axis, predicateSet);
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public String getText() {
        StringBuffer buf = new StringBuffer();
        buf.append(this.getAxisName());
        buf.append("::processing-instruction(");
        String name = this.getName();
        if (name != null && name.length() != 0) {
            buf.append("'");
            buf.append(name);
            buf.append("'");
        }
        buf.append(")");
        buf.append(super.getText());
        return buf.toString();
    }

    public boolean matches(Object node, ContextSupport support) {
        Navigator nav = support.getNavigator();
        if (nav.isProcessingInstruction(node)) {
            String name = this.getName();
            if (name == null || name.length() == 0) {
                return true;
            }
            return name.equals(nav.getProcessingInstructionTarget(node));
        }
        return false;
    }
}

