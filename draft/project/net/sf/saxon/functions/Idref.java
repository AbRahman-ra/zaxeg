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
import net.sf.saxon.om.StandardNames;
import net.sf.saxon.om.TreeInfo;
import net.sf.saxon.om.ZeroOrMore;
import net.sf.saxon.trans.KeyDefinitionSet;
import net.sf.saxon.trans.KeyManager;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.StringValue;

public class Idref
extends SystemFunction {
    @Override
    public int getSpecialProperties(Expression[] arguments) {
        int prop = 25296896;
        if (this.getArity() == 1 || arguments[1].hasSpecialProperty(65536)) {
            prop |= 0x10000;
        }
        return prop;
    }

    public static SequenceIterator getIdrefMultiple(TreeInfo doc, SequenceIterator keys, XPathContext context) throws XPathException {
        IdrefMappingFunction map = new IdrefMappingFunction();
        map.document = doc;
        map.keyContext = context;
        map.keyManager = context.getController().getExecutable().getTopLevelPackage().getKeyManager();
        map.keySet = map.keyManager.getKeyDefinitionSet(StandardNames.getStructuredQName(562));
        MappingIterator allValues = new MappingIterator(keys, map);
        return new DocumentOrderIterator(allValues, LocalOrderComparer.getInstance());
    }

    @Override
    public ZeroOrMore<NodeInfo> call(XPathContext context, Sequence[] arguments) throws XPathException {
        NodeInfo start = arguments.length == 1 ? this.getContextNode(context) : (NodeInfo)arguments[1].head();
        NodeInfo arg2 = start.getRoot();
        if (arg2.getNodeKind() != 9) {
            throw new XPathException("In the idref() function, the tree being searched must be one whose root is a document node", "FODC0001", context);
        }
        return new ZeroOrMore<NodeInfo>(Idref.getIdrefMultiple(arg2.getTreeInfo(), arguments[0].iterate(), context));
    }

    private static class IdrefMappingFunction
    implements MappingFunction {
        public TreeInfo document;
        public XPathContext keyContext;
        public KeyManager keyManager;
        public KeyDefinitionSet keySet;

        private IdrefMappingFunction() {
        }

        @Override
        public SequenceIterator map(Item item) throws XPathException {
            return this.keyManager.selectByKey(this.keySet, this.document, (StringValue)item, this.keyContext);
        }
    }
}

