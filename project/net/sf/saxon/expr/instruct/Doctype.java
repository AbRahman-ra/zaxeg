/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.instruct;

import net.sf.saxon.Controller;
import net.sf.saxon.event.ComplexContentOutputter;
import net.sf.saxon.event.Outputter;
import net.sf.saxon.event.PipelineConfiguration;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.Operand;
import net.sf.saxon.expr.OperandRole;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.instruct.Instruction;
import net.sf.saxon.expr.instruct.TailCall;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.AxisIterator;
import net.sf.saxon.tree.tiny.TinyBuilder;

public class Doctype
extends Instruction {
    private Operand contentOp;

    public Doctype(Expression content) {
        this.contentOp = new Operand(this, content, OperandRole.SINGLE_ATOMIC);
    }

    public Expression getContent() {
        return this.contentOp.getChildExpression();
    }

    public void setContent(Expression content) {
        this.contentOp.setChildExpression(content);
    }

    @Override
    public Iterable<Operand> operands() {
        return this.contentOp;
    }

    @Override
    public Expression copy(RebindingMap rebindings) {
        throw new UnsupportedOperationException("Doctype.copy()");
    }

    @Override
    public final boolean mayCreateNewNodes() {
        return true;
    }

    @Override
    public int getInstructionNameCode() {
        return 263;
    }

    @Override
    public TailCall processLeavingTail(Outputter out, XPathContext context) throws XPathException {
        Controller controller = context.getController();
        PipelineConfiguration pipe = controller.makePipelineConfiguration();
        pipe.setXPathContext(context);
        pipe.setHostLanguage(this.getPackageData().getHostLanguage());
        TinyBuilder builder = new TinyBuilder(pipe);
        builder.setStatistics(pipe.getConfiguration().getTreeStatistics().RESULT_TREE_STATISTICS);
        builder.open();
        builder.startDocument(0);
        this.getContent().process(ComplexContentOutputter.makeComplexContentReceiver(builder, null), context);
        builder.endDocument();
        builder.close();
        NodeInfo dtdRoot = builder.getCurrentRoot();
        AxisIterator children = dtdRoot.iterateAxis(3);
        NodeInfo docType = (NodeInfo)children.next();
        if (docType == null || !"doctype".equals(docType.getLocalPart())) {
            XPathException e = new XPathException("saxon:doctype instruction must contain dtd:doctype");
            e.setXPathContext(context);
            throw e;
        }
        String name = docType.getAttributeValue("", "name");
        String system = docType.getAttributeValue("", "system");
        String publicid = docType.getAttributeValue("", "public");
        if (name == null) {
            XPathException e = new XPathException("dtd:doctype must have a name attribute");
            e.setXPathContext(context);
            throw e;
        }
        this.write(out, "<!DOCTYPE " + name + ' ');
        if (system != null) {
            if (publicid != null) {
                this.write(out, "PUBLIC \"" + publicid + "\" \"" + system + '\"');
            } else {
                this.write(out, "SYSTEM \"" + system + '\"');
            }
        }
        boolean openSquare = false;
        children = docType.iterateAxis(3);
        NodeInfo child = (NodeInfo)children.next();
        if (child != null) {
            this.write(out, " [");
            openSquare = true;
        }
        while (child != null) {
            XPathException e;
            String elname;
            String localname = child.getLocalPart();
            if ("element".equals(localname)) {
                elname = child.getAttributeValue("", "name");
                String content = child.getAttributeValue("", "content");
                if (elname == null) {
                    XPathException e2 = new XPathException("dtd:element must have a name attribute");
                    e2.setXPathContext(context);
                    throw e2;
                }
                if (content == null) {
                    XPathException e2 = new XPathException("dtd:element must have a content attribute");
                    e2.setXPathContext(context);
                    throw e2;
                }
                this.write(out, "\n  <!ELEMENT " + elname + ' ' + content + '>');
            } else if (localname.equals("attlist")) {
                NodeInfo attDef;
                elname = child.getAttributeValue("", "element");
                if (elname == null) {
                    XPathException e3 = new XPathException("dtd:attlist must have an attribute named 'element'");
                    e3.setXPathContext(context);
                    throw e3;
                }
                this.write(out, "\n  <!ATTLIST " + elname + ' ');
                AxisIterator attributes = child.iterateAxis(3);
                while ((attDef = (NodeInfo)attributes.next()) != null) {
                    if ("attribute".equals(attDef.getLocalPart())) {
                        String atname = attDef.getAttributeValue("", "name");
                        String type = attDef.getAttributeValue("", "type");
                        String value = attDef.getAttributeValue("", "value");
                        if (atname == null) {
                            XPathException xPathException = new XPathException("dtd:attribute must have a name attribute");
                            xPathException.setXPathContext(context);
                            throw xPathException;
                        }
                        if (type == null) {
                            XPathException xPathException = new XPathException("dtd:attribute must have a type attribute");
                            xPathException.setXPathContext(context);
                            throw xPathException;
                        }
                        if (value == null) {
                            XPathException xPathException = new XPathException("dtd:attribute must have a value attribute");
                            xPathException.setXPathContext(context);
                            throw xPathException;
                        }
                        this.write(out, "\n    " + atname + ' ' + type + ' ' + value);
                        continue;
                    }
                    e = new XPathException("Unrecognized element within dtd:attlist");
                    e.setXPathContext(context);
                    throw e;
                }
                this.write(out, ">");
            } else if (localname.equals("entity")) {
                String entname = child.getAttributeValue("", "name");
                String parameter = child.getAttributeValue("", "parameter");
                String esystem = child.getAttributeValue("", "system");
                String epublicid = child.getAttributeValue("", "public");
                String notation = child.getAttributeValue("", "notation");
                if (entname == null) {
                    XPathException e5 = new XPathException("dtd:entity must have a name attribute");
                    e5.setXPathContext(context);
                    throw e5;
                }
                this.write(out, "\n  <!ENTITY ");
                if ("yes".equals(parameter)) {
                    this.write(out, "% ");
                }
                this.write(out, entname + ' ');
                if (esystem != null) {
                    if (epublicid != null) {
                        this.write(out, "PUBLIC \"" + epublicid + "\" \"" + esystem + "\" ");
                    } else {
                        this.write(out, "SYSTEM \"" + esystem + "\" ");
                    }
                }
                if (notation != null) {
                    this.write(out, "NDATA " + notation + ' ');
                }
                for (NodeInfo nodeInfo : child.children()) {
                    nodeInfo.copy(out, 0, this.getLocation());
                }
                this.write(out, ">");
            } else if (localname.equals("notation")) {
                String notname = child.getAttributeValue("", "name");
                String nsystem = child.getAttributeValue("", "system");
                String npublicid = child.getAttributeValue("", "public");
                if (notname == null) {
                    e = new XPathException("dtd:notation must have a name attribute");
                    e.setXPathContext(context);
                    throw e;
                }
                if (nsystem == null && npublicid == null) {
                    e = new XPathException("dtd:notation must have a system attribute or a public attribute");
                    e.setXPathContext(context);
                    throw e;
                }
                this.write(out, "\n  <!NOTATION " + notname);
                if (npublicid != null) {
                    this.write(out, " PUBLIC \"" + npublicid + "\" ");
                    if (nsystem != null) {
                        this.write(out, '\"' + nsystem + "\" ");
                    }
                } else {
                    this.write(out, " SYSTEM \"" + nsystem + "\" ");
                }
                this.write(out, ">");
            } else if (child.getNodeKind() == 3) {
                this.write(out, child.getStringValue());
            } else {
                XPathException e6 = new XPathException("Unrecognized element " + localname + " in DTD output");
                e6.setXPathContext(context);
                throw e6;
            }
            child = (NodeInfo)children.next();
        }
        if (openSquare) {
            this.write(out, "\n]");
        }
        this.write(out, ">\n");
        return null;
    }

    private void write(Outputter out, String s) throws XPathException {
        out.characters(s, this.getLocation(), 1);
    }

    @Override
    public void export(ExpressionPresenter out) throws XPathException {
        out.startElement("saxonDoctype", this);
        this.getContent().export(out);
        out.endElement();
    }
}

