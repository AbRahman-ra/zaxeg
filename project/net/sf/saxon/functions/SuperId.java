/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.MappingFunction;
import net.sf.saxon.expr.MappingIterator;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.sort.DocumentOrderIterator;
import net.sf.saxon.expr.sort.LocalOrderComparer;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.SequenceTool;
import net.sf.saxon.om.TreeInfo;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.SingletonIterator;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.Whitespace;

public abstract class SuperId
extends SystemFunction {
    public static final int ID = 0;
    public static final int ELEMENT_WITH_ID = 1;

    public abstract int getOp();

    @Override
    public int getSpecialProperties(Expression[] arguments) {
        int prop = 25296896;
        if (this.getArity() == 1 || (arguments[1].getSpecialProperties() & 0x10000) != 0) {
            prop |= 0x10000;
        }
        return prop;
    }

    public static SequenceIterator getIdSingle(TreeInfo doc, String idrefs, int operation) throws XPathException {
        if (Whitespace.containsWhitespace(idrefs)) {
            Whitespace.Tokenizer tokens = new Whitespace.Tokenizer(idrefs);
            IdMappingFunction map = new IdMappingFunction();
            map.document = doc;
            map.operation = operation;
            MappingIterator result = new MappingIterator(tokens, map);
            return new DocumentOrderIterator(result, LocalOrderComparer.getInstance());
        }
        return SingletonIterator.makeIterator(doc.selectID(idrefs, operation == 1));
    }

    public static SequenceIterator getIdMultiple(TreeInfo doc, SequenceIterator idrefs, int operation) throws XPathException {
        IdMappingFunction map = new IdMappingFunction();
        map.document = doc;
        map.operation = operation;
        MappingIterator result = new MappingIterator(idrefs, map);
        return new DocumentOrderIterator(result, LocalOrderComparer.getInstance());
    }

    @Override
    public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
        SequenceIterator result;
        NodeInfo start = arguments.length == 1 ? this.getContextNode(context) : (NodeInfo)arguments[1].head();
        NodeInfo arg1 = start.getRoot();
        if (arg1.getNodeKind() != 9) {
            throw new XPathException("In the " + this.getFunctionName().getLocalPart() + "() function, the tree being searched must be one whose root is a document node", "FODC0001", context);
        }
        TreeInfo doc = arg1.getTreeInfo();
        if (arguments[0] instanceof AtomicValue) {
            result = SuperId.getIdSingle(doc, ((AtomicValue)arguments[0]).getStringValue(), this.getOp());
        } else {
            SequenceIterator idrefs = arguments[0].iterate();
            result = SuperId.getIdMultiple(doc, idrefs, this.getOp());
        }
        return SequenceTool.toLazySequence(result);
    }

    public static class ElementWithId
    extends SuperId {
        @Override
        public int getOp() {
            return 1;
        }
    }

    public static class Id
    extends SuperId {
        @Override
        public int getOp() {
            return 0;
        }
    }

    private static class IdMappingFunction
    implements MappingFunction {
        public TreeInfo document;
        private int operation;

        private IdMappingFunction() {
        }

        @Override
        public SequenceIterator map(Item item) {
            String idrefs = Whitespace.trim(item.getStringValueCS());
            if (Whitespace.containsWhitespace(idrefs)) {
                Whitespace.Tokenizer tokens = new Whitespace.Tokenizer(idrefs);
                IdMappingFunction submap = new IdMappingFunction();
                submap.document = this.document;
                submap.operation = this.operation;
                return new MappingIterator(tokens, submap);
            }
            return SingletonIterator.makeIterator(this.document.selectID(idrefs, this.operation == 1));
        }
    }
}

