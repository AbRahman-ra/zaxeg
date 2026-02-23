/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.value.StringValue;

public class StringLiteral
extends Literal {
    public StringLiteral(StringValue value) {
        super(value);
    }

    public StringLiteral(CharSequence value) {
        this(StringValue.makeStringValue(value));
    }

    @Override
    public StringValue getValue() {
        return (StringValue)super.getValue();
    }

    public String getStringValue() {
        return this.getValue().getStringValue();
    }

    @Override
    public Expression copy(RebindingMap rebindings) {
        StringLiteral stringLiteral = new StringLiteral(this.getValue());
        ExpressionTool.copyLocationInfo(this, stringLiteral);
        return stringLiteral;
    }
}

