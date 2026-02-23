/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.style;

import java.net.URI;
import java.net.URISyntaxException;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.StringLiteral;
import net.sf.saxon.expr.instruct.ForEachGroup;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.RoleDiagnostic;
import net.sf.saxon.expr.parser.TypeChecker;
import net.sf.saxon.lib.StringCollator;
import net.sf.saxon.om.AttributeInfo;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.pattern.Pattern;
import net.sf.saxon.style.Compilation;
import net.sf.saxon.style.ComponentDeclaration;
import net.sf.saxon.style.StyleElement;
import net.sf.saxon.style.XSLSort;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.Whitespace;

public final class XSLForEachGroup
extends StyleElement {
    private Expression select = null;
    private Expression groupBy = null;
    private Expression groupAdjacent = null;
    private Pattern starting = null;
    private Pattern ending = null;
    private Expression collationName;
    private boolean composite = false;

    @Override
    public boolean isInstruction() {
        return true;
    }

    @Override
    protected boolean isPermittedChild(StyleElement child) {
        return child instanceof XSLSort;
    }

    @Override
    public boolean mayContainSequenceConstructor() {
        return true;
    }

    @Override
    public void prepareAttributes() {
        int c;
        String selectAtt = null;
        String groupByAtt = null;
        String groupAdjacentAtt = null;
        String startingAtt = null;
        String endingAtt = null;
        String collationAtt = null;
        block24: for (AttributeInfo att : this.attributes()) {
            NodeName attName = att.getNodeName();
            String f = attName.getDisplayName();
            String value = att.getValue();
            switch (f) {
                case "select": {
                    selectAtt = value;
                    this.select = this.makeExpression(selectAtt, att);
                    continue block24;
                }
                case "group-by": {
                    groupByAtt = value;
                    this.groupBy = this.makeExpression(groupByAtt, att);
                    continue block24;
                }
                case "group-adjacent": {
                    groupAdjacentAtt = value;
                    this.groupAdjacent = this.makeExpression(groupAdjacentAtt, att);
                    continue block24;
                }
                case "group-starting-with": {
                    startingAtt = value;
                    continue block24;
                }
                case "group-ending-with": {
                    endingAtt = value;
                    continue block24;
                }
                case "collation": {
                    collationAtt = Whitespace.trim(value);
                    this.collationName = this.makeAttributeValueTemplate(collationAtt, att);
                    continue block24;
                }
                case "bind-group": {
                    this.compileError("The bind-group attribute has been dropped from the XSLT 3.0 specification", "XTSE0090");
                    continue block24;
                }
                case "bind-grouping-key": {
                    this.compileError("The bind-grouping-key attribute has been dropped from the XSLT 3.0 specification", "XTSE0090");
                    continue block24;
                }
                case "composite": {
                    this.composite = this.processBooleanAttribute("composite", value);
                    continue block24;
                }
            }
            this.checkUnknownAttribute(attName);
        }
        if (selectAtt == null) {
            this.reportAbsence("select");
            this.select = Literal.makeEmptySequence();
        }
        if ((c = (groupByAtt == null ? 0 : 1) + (groupAdjacentAtt == null ? 0 : 1) + (startingAtt == null ? 0 : 1) + (endingAtt == null ? 0 : 1)) != 1) {
            this.compileError("Exactly one of the attributes group-by, group-adjacent, group-starting-with, and group-ending-with must be specified", "XTSE1080");
        }
        if (startingAtt != null) {
            this.starting = this.makePattern(startingAtt, "group-starting-with");
        }
        if (endingAtt != null) {
            this.ending = this.makePattern(endingAtt, "group-ending-with");
        }
        if (collationAtt != null) {
            if (this.groupBy == null && this.groupAdjacent == null) {
                this.compileError("A collation may be specified only if group-by or group-adjacent is specified", "XTSE1090");
            } else if (this.collationName instanceof StringLiteral) {
                String collation = ((StringLiteral)this.collationName).getStringValue();
                try {
                    URI collationURI = new URI(collation);
                    if (!collationURI.isAbsolute()) {
                        URI base = new URI(this.getBaseURI());
                        collationURI = base.resolve(collationURI);
                        this.collationName = new StringLiteral(collationURI.toString());
                    }
                } catch (URISyntaxException err) {
                    this.compileError("Collation name '" + this.collationName + "' is not a valid URI", "XTDE1110");
                    this.collationName = new StringLiteral("http://www.w3.org/2005/xpath-functions/collation/codepoint");
                }
            }
        } else {
            String defaultCollation = this.getDefaultCollationName();
            if (defaultCollation != null) {
                this.collationName = new StringLiteral(defaultCollation);
            }
        }
        if (this.composite && (this.starting != null || this.ending != null)) {
            this.compileError("The composite attribute cannot be used with " + (this.starting == null ? "grouping-ending-with" : "group-starting-with"), "XTSE1090");
        }
    }

    @Override
    public void validate(ComponentDeclaration decl) throws XPathException {
        RoleDiagnostic role;
        this.checkSortComesFirst(false);
        TypeChecker tc = this.getConfiguration().getTypeChecker(false);
        this.select = this.typeCheck("select", this.select);
        ExpressionVisitor visitor = this.makeExpressionVisitor();
        if (this.groupBy != null) {
            this.groupBy = this.typeCheck("group-by", this.groupBy);
            try {
                role = new RoleDiagnostic(4, "xsl:for-each-group/group-by", 0);
                this.groupBy = tc.staticTypeCheck(this.groupBy, SequenceType.ATOMIC_SEQUENCE, role, visitor);
            } catch (XPathException err) {
                this.compileError(err);
            }
        } else if (this.groupAdjacent != null) {
            this.groupAdjacent = this.typeCheck("group-adjacent", this.groupAdjacent);
            try {
                role = new RoleDiagnostic(4, "xsl:for-each-group/group-adjacent", 0);
                role.setErrorCode("XTTE1100");
                this.groupAdjacent = tc.staticTypeCheck(this.groupAdjacent, this.composite ? SequenceType.ATOMIC_SEQUENCE : SequenceType.SINGLE_ATOMIC, role, visitor);
            } catch (XPathException err) {
                this.compileError(err);
            }
        }
        this.starting = this.typeCheck("starting", this.starting);
        this.ending = this.typeCheck("ending", this.ending);
        if ((this.starting != null || this.ending != null) && visitor.getStaticContext().getXPathVersion() < 30) {
            try {
                role = new RoleDiagnostic(4, "xsl:for-each-group/select", 0);
                role.setErrorCode("XTTE1120");
                this.select = tc.staticTypeCheck(this.select, SequenceType.NODE_SEQUENCE, role, visitor);
            } catch (XPathException err) {
                String prefix = this.starting != null ? "With group-starting-with attribute: " : "With group-ending-with attribute: ";
                this.compileError(prefix + err.getMessage(), err.getErrorCodeQName());
            }
        }
        if (!this.hasChildNodes()) {
            this.compileWarning("An empty xsl:for-each-group instruction has no effect", "SXWN9009");
        }
    }

    @Override
    public Expression compile(Compilation compilation, ComponentDeclaration decl) throws XPathException {
        String uri;
        StringCollator collator = null;
        if (this.collationName instanceof StringLiteral && (collator = this.findCollation(uri = ((StringLiteral)this.collationName).getStringValue(), this.getBaseURI())) == null) {
            this.compileError("The collation name '" + this.collationName + "' has not been defined", "XTDE1110");
        }
        byte algorithm = 0;
        Expression key = null;
        if (this.groupBy != null) {
            algorithm = 0;
            key = this.groupBy;
        } else if (this.groupAdjacent != null) {
            algorithm = 1;
            key = this.groupAdjacent;
        } else if (this.starting != null) {
            algorithm = 2;
            key = this.starting;
        } else if (this.ending != null) {
            algorithm = 3;
            key = this.ending;
        }
        Expression action = this.compileSequenceConstructor(compilation, decl, true);
        if (action == null) {
            return Literal.makeEmptySequence();
        }
        try {
            ForEachGroup instr = new ForEachGroup(this.select, action.simplify(), algorithm, key, collator, this.collationName, this.makeSortKeys(compilation, decl));
            instr.setIsInFork(this.getParent().getFingerprint() == 156);
            instr.setComposite(this.composite);
            return instr;
        } catch (XPathException e) {
            this.compileError(e);
            return null;
        }
    }
}

