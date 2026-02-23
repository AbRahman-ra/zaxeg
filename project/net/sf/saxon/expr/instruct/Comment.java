/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.instruct;

import net.sf.saxon.event.Outputter;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.StringLiteral;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.instruct.SimpleNodeConstructor;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.pattern.NodeKindTest;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.ItemType;

public final class Comment
extends SimpleNodeConstructor {
    @Override
    public int getInstructionNameCode() {
        return 143;
    }

    @Override
    public ItemType getItemType() {
        return NodeKindTest.COMMENT;
    }

    @Override
    public int getCardinality() {
        return 16384;
    }

    @Override
    public Expression copy(RebindingMap rebindings) {
        Comment exp = new Comment();
        ExpressionTool.copyLocationInfo(this, exp);
        exp.setSelect(this.getSelect().copy(rebindings));
        return exp;
    }

    @Override
    public void localTypeCheck(ExpressionVisitor visitor, ContextItemStaticInfo contextItemType) throws XPathException {
        String s;
        String s2;
        if (this.getSelect() instanceof Literal && !(s2 = this.checkContent(s = ((Literal)this.getSelect()).getValue().getStringValue(), visitor.getStaticContext().makeEarlyEvaluationContext())).equals(s)) {
            this.setSelect(new StringLiteral(s2));
        }
    }

    @Override
    public void processValue(CharSequence value, Outputter output, XPathContext context) throws XPathException {
        String comment = this.checkContent(value.toString(), context);
        output.comment(comment, this.getLocation(), 0);
    }

    @Override
    protected String checkContent(String comment, XPathContext context) throws XPathException {
        if (this.isXSLT()) {
            return Comment.checkContentXSLT(comment);
        }
        try {
            return Comment.checkContentXQuery(comment);
        } catch (XPathException err) {
            err.setXPathContext(context);
            err.setLocation(this.getLocation());
            throw err;
        }
    }

    public static String checkContentXSLT(String comment) {
        int hh;
        while ((hh = comment.indexOf("--")) >= 0) {
            comment = comment.substring(0, hh + 1) + ' ' + comment.substring(hh + 1);
        }
        if (comment.endsWith("-")) {
            comment = comment + ' ';
        }
        return comment;
    }

    public static String checkContentXQuery(String comment) throws XPathException {
        if (comment.contains("--")) {
            throw new XPathException("Invalid characters (--) in comment", "XQDY0072");
        }
        if (comment.length() > 0 && comment.charAt(comment.length() - 1) == '-') {
            throw new XPathException("Comment cannot end in '-'", "XQDY0072");
        }
        return comment;
    }

    @Override
    public void export(ExpressionPresenter out) throws XPathException {
        out.startElement("comment", this);
        String flags = "";
        if (this.isLocal()) {
            flags = flags + "l";
        }
        if (!flags.isEmpty()) {
            out.emitAttribute("flags", flags);
        }
        this.getSelect().export(out);
        out.endElement();
    }
}

