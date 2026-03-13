/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import net.sf.saxon.Configuration;
import net.sf.saxon.TransformerFactoryImpl;
import net.sf.saxon.Version;
import net.sf.saxon.event.Builder;
import net.sf.saxon.event.ComplexContentOutputter;
import net.sf.saxon.event.PipelineConfiguration;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.expr.parser.Loc;
import net.sf.saxon.expr.parser.XPathParser;
import net.sf.saxon.gizmo.DefaultTalker;
import net.sf.saxon.gizmo.JLine2Talker;
import net.sf.saxon.gizmo.Talker;
import net.sf.saxon.lib.Feature;
import net.sf.saxon.lib.ParseOptions;
import net.sf.saxon.om.FingerprintedQName;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.MutableNodeInfo;
import net.sf.saxon.om.NameOfNode;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.om.TreeModel;
import net.sf.saxon.pattern.NameTest;
import net.sf.saxon.pattern.NodeKindTest;
import net.sf.saxon.query.DynamicQueryContext;
import net.sf.saxon.query.QueryResult;
import net.sf.saxon.query.StaticQueryContext;
import net.sf.saxon.query.XQueryExpression;
import net.sf.saxon.s9api.UnprefixedElementMatchingPolicy;
import net.sf.saxon.serialize.SerializationProperties;
import net.sf.saxon.sxpath.IndependentContext;
import net.sf.saxon.sxpath.XPathDynamicContext;
import net.sf.saxon.sxpath.XPathEvaluator;
import net.sf.saxon.sxpath.XPathExpression;
import net.sf.saxon.sxpath.XPathVariable;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.NamespaceNode;
import net.sf.saxon.tree.iter.AxisIterator;
import net.sf.saxon.tree.linked.DocumentImpl;
import net.sf.saxon.tree.linked.LinkedTreeBuilder;
import net.sf.saxon.tree.util.Navigator;
import net.sf.saxon.tree.util.Orphan;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.SimpleType;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.QNameValue;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;
import net.sf.saxon.value.Whitespace;

public class Gizmo {
    private Configuration config;
    private IndependentContext env;
    private DocumentImpl currentDoc;
    private boolean unsaved = false;
    private Map<StructuredQName, Sequence> variables = new HashMap<StructuredQName, Sequence>();
    private Map<String, SubCommand> subCommands = new HashMap<String, SubCommand>();
    private Talker talker;
    private boolean typed = false;
    private List<DocumentImpl> undoBuffer = new LinkedList<DocumentImpl>();
    private static String[] keywords = new String[]{"ancestor::", "ancestor-or-self::", "array", "attribute", "cast as", "castable as", "child::", "comment()", "descendant::", "descendant-or-self::", "document-node()", "element()", "else", "empty-sequence()", "every", "except", "following::", "following-sibling::", "function", "instance of", "intersect", "item()", "namespace::", "namespace-node()", "node()", "parent::", "preceding::", "preceding-sibling::", "processing-instruction()", "return", "satisfies", "schema-attribute", "schema-element", "self::", "some", "text()", "then", "treat as", "union"};

    private void addCommand(String name, String helpText, Action action) {
        SubCommand c = new SubCommand();
        c.name = name;
        c.helpText = helpText;
        c.action = action;
        this.subCommands.put(name, c);
    }

    private void initSubCommands() {
        this.addCommand("call", "call {filename} -- execute script from a file", this::call);
        this.addCommand("copy", "copy {expression} -- make deep copy of all selected nodes", this::copy);
        this.addCommand("delete", "delete {expression} -- delete all selected nodes, with their content", this::delete);
        this.addCommand("follow", "follow {expression} with {query} -- add result of query after each selected node", cmd -> this.update(cmd, "follow"));
        this.addCommand("help", "help {keyword} -- help on a specific command, or '?' for all commands", this::help);
        this.addCommand("list", "list {expression} -- display paths of selected nodes", this::list);
        this.addCommand("load", "load {fileName} -- load new source document from file", this::load);
        this.addCommand("namespace", "namespace {prefix} {uri} -- bind namespace prefix to URI", this::namespace);
        this.addCommand("paths", "paths -- display all distinct element paths in the document", cmd -> this.list(new StringBuffer("distinct-values(//*!('/'||string-join(ancestor-or-self::*!name(),'/')))")));
        this.addCommand("precede", "precede {expression} with {query} -- add result of query before each selected nodes", cmd -> this.update(cmd, "precede"));
        this.addCommand("prefix", "prefix {expression} with {query} -- add result of query as first child of each selected nodes", cmd -> this.update(cmd, "prefix"));
        this.addCommand("quit", "quit [now] -- stop Gizmo", cmd -> {
            throw new RuntimeException();
        });
        this.addCommand("rename", "rename {expression-1} as {expression-2} -- change the name of selected nodes", this::rename);
        this.addCommand("replace", "replace {expression} with {query} -- replace selected nodes with result of query", this::replace);
        this.addCommand("save", "save {filename} {output-property=value}... -- save current document to file", this::save);
        this.addCommand("schema", "schema {filename} -- load XSD 1.1 schema for use in validation", this::schema);
        this.addCommand("set", "set {variable} {expression} -- set variable to value of expression", this::set);
        this.addCommand("show", "show {expression} -- display content of all selected nodes", this::show);
        this.addCommand("strip", "strip -- delete whitespace text nodes", cmd -> this.delete(new StringBuffer("//text()[not(normalize-space())]")));
        this.addCommand("suffix", "suffix {expression} with {query} -- add result of query as last child of each selected nodes", cmd -> this.update(cmd, "suffix"));
        this.addCommand("transform", "transform {filename} -- transform current document using stylesheet in named file", this::transform);
        this.addCommand("undo", "undo -- revert the most recent changes", this::undo);
        this.addCommand("update", "update {expression} with {query} -- replace content of selected nodes with result of query", cmd -> this.update(cmd, "content"));
        this.addCommand("validate", "validate -- validate against loaded schema and/or xsi:schemaLocation", this::validate);
        this.addCommand("?", "", this::help);
    }

    public static void main(String[] args) {
        new Gizmo(args);
    }

    public Gizmo(String[] args) {
        this.initSubCommands();
        this.config = Configuration.newConfiguration();
        this.config.setConfigurationProperty(Feature.ALLOW_SYNTAX_EXTENSIONS, true);
        this.env = new IndependentContext(this.config);
        String source = null;
        String script = null;
        boolean interactive = true;
        for (String arg : args) {
            if (arg.startsWith("-s:")) {
                source = arg.substring(3);
            }
            if (!arg.startsWith("-q:")) continue;
            script = arg.substring(3);
            interactive = false;
        }
        this.talker = this.initTalker(script);
        ArrayList<String> sortedNames = new ArrayList<String>(Arrays.asList(keywords));
        Collections.sort(sortedNames);
        this.talker.setAutoCompletion(sortedNames);
        if (source != null) {
            try {
                this.load(new StringBuffer(source));
            } catch (XPathException e) {
                System.err.println(e.getMessage());
                System.exit(2);
            }
        } else {
            try {
                String dummy = "<dummy/>";
                StreamSource ss = new StreamSource(new StringReader(dummy));
                ParseOptions options = new ParseOptions();
                options.setModel(TreeModel.LINKED_TREE);
                options.setLineNumbering(true);
                this.currentDoc = (DocumentImpl)this.config.buildDocumentTree(ss, options).getRootNode();
                this.typed = false;
            } catch (XPathException e) {
                System.err.println(e.getMessage());
                System.exit(2);
            }
        }
        this.env.declareNamespace("xml", "http://www.w3.org/XML/1998/namespace");
        this.env.declareNamespace("xsl", "http://www.w3.org/1999/XSL/Transform");
        this.env.declareNamespace("saxon", "http://saxon.sf.net/");
        this.env.declareNamespace("xs", "http://www.w3.org/2001/XMLSchema");
        this.env.declareNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
        this.env.declareNamespace("fn", "http://www.w3.org/2005/xpath-functions");
        this.env.declareNamespace("math", "http://www.w3.org/2005/xpath-functions/math");
        this.env.declareNamespace("map", "http://www.w3.org/2005/xpath-functions/map");
        this.env.declareNamespace("array", "http://www.w3.org/2005/xpath-functions/array");
        this.env.declareNamespace("", "");
        this.env.setUnprefixedElementMatchingPolicy(UnprefixedElementMatchingPolicy.ANY_NAMESPACE);
        System.out.println("Saxon Gizmo " + Version.getProductVersion());
        this.executeCommands(this.talker, interactive);
    }

    protected Talker initTalker(String script) {
        if (script == null) {
            try {
                return new JLine2Talker();
            } catch (IOException e) {
                System.err.println(e.getMessage());
                System.exit(2);
                return null;
            }
        }
        try {
            return new DefaultTalker(new FileInputStream(new File(script)), System.out);
        } catch (FileNotFoundException e) {
            System.err.println(e.getMessage());
            System.exit(2);
            return null;
        }
    }

    private void executeCommands(Talker talker, boolean interactive) {
        int emptyLines = 0;
        block2: while (true) {
            try {
                while (true) {
                    String command;
                    if ((command = talker.exchange("")).isEmpty() && !interactive) {
                        return;
                    }
                    while (command.isEmpty()) {
                        if (emptyLines++ <= 2) continue;
                        command = talker.exchange("To exit, type 'quit'. For help, type 'help'");
                    }
                    emptyLines = 0;
                    int space = command.indexOf(32);
                    if (space < 0) {
                        space = command.length();
                    }
                    String keyword = command.substring(0, space);
                    String remainder = command.substring(space).trim();
                    if (keyword.equals("quit")) {
                        boolean quit = true;
                        if (this.unsaved && !remainder.equals("now")) {
                            String answer;
                            while (!(answer = talker.exchange("Quit without saving? (Y|N)")).equalsIgnoreCase("y")) {
                                if (!answer.equalsIgnoreCase("n")) continue;
                                quit = false;
                                break;
                            }
                        }
                        if (!quit) continue;
                        break block2;
                    }
                    SubCommand cmd = this.subCommands.get(keyword);
                    if (cmd == null) {
                        if (interactive) {
                            System.out.println("Unknown command " + keyword + " (Use 'quit' to exit)");
                            this.help(new StringBuffer("?"));
                            continue;
                        }
                        throw new XPathException("\"Unknown command \" + cmd + \"");
                    }
                    cmd.action.perform(new StringBuffer(remainder));
                }
            } catch (XPathException e) {
                System.out.println(e.getErrorCodeLocalPart() + ": " + e.getMessage());
                if (interactive) continue;
                System.exit(2);
                continue;
            }
            break;
        }
    }

    private void help(StringBuffer command) {
        String cmd;
        String string = cmd = command == null ? null : command.toString().trim();
        if (cmd == null || cmd.isEmpty() || cmd.equals("help") || cmd.equals("?")) {
            System.out.println("Commands available:");
            ArrayList<String> commands = new ArrayList<String>(this.subCommands.keySet());
            commands.sort(null);
            for (String c : commands) {
                System.out.println("  " + this.subCommands.get((Object)c).helpText);
            }
        } else {
            SubCommand entry = this.subCommands.get(cmd);
            if (entry == null) {
                this.help(null);
            } else {
                System.out.println(entry.helpText);
            }
        }
    }

    private SequenceIterator getSelectedItems(StringBuffer selection, int terminator) throws XPathException {
        XPathExpression expr = this.getExpression(selection, terminator);
        return this.evaluateExpression(expr, this.currentDoc);
    }

    private XPathExpression getExpression(StringBuffer selection, int terminator) throws XPathException {
        XPathEvaluator evaluator = new XPathEvaluator(this.config);
        evaluator.setStaticContext(this.env);
        for (StructuredQName var : this.variables.keySet()) {
            this.env.declareVariable(var);
        }
        XPathParser scanner = this.config.newExpressionParser("XP", false, 31);
        scanner.parse(selection.toString(), 0, terminator, this.env);
        int endPoint = scanner.getTokenizer().currentTokenStartOffset;
        XPathExpression expr = evaluator.createExpression(selection.substring(0, endPoint));
        selection.replace(0, endPoint, "");
        return expr;
    }

    private XQueryExpression getQuery(StringBuffer query) throws XPathException {
        StaticQueryContext sqc = this.config.makeStaticQueryContext(true);
        for (StructuredQName var : this.variables.keySet()) {
            sqc.declareGlobalVariable(var, SequenceType.ANY_SEQUENCE, this.variables.get(var), false);
        }
        if (this.typed) {
            sqc.setSchemaAware(true);
        }
        Iterator<String> prefixes = this.env.iteratePrefixes();
        while (prefixes.hasNext()) {
            String prefix = prefixes.next();
            sqc.declareNamespace(prefix, this.env.getURIForPrefix(prefix, true));
        }
        return sqc.compileQuery(query.toString());
    }

    private SequenceIterator evaluateExpression(XPathExpression expr, Item contextItem) throws XPathException {
        XPathDynamicContext context = expr.createDynamicContext(contextItem);
        for (Map.Entry<StructuredQName, Sequence> var : this.variables.entrySet()) {
            XPathVariable v = this.env.getExternalVariable(var.getKey());
            context.setVariable(v, var.getValue());
        }
        return expr.iterate(context);
    }

    private SequenceIterator evaluateQuery(XQueryExpression expr, Item contextItem) throws XPathException {
        DynamicQueryContext context = new DynamicQueryContext(this.config);
        context.setContextItem(contextItem);
        return expr.iterator(context);
    }

    private StructuredQName getQName(String in) throws XPathException {
        return StructuredQName.fromLexicalQName(in, false, true, this.env.getNamespaceResolver());
    }

    private void needCurrentDoc() throws XPathException {
        if (this.currentDoc == null) {
            throw new XPathException("No source document available");
        }
    }

    private void saveCurrentDoc() throws XPathException {
        LinkedTreeBuilder builder = new LinkedTreeBuilder(this.config.makePipelineConfiguration());
        this.currentDoc.copy(builder, 2, Loc.NONE);
        DocumentImpl copy = (DocumentImpl)((Builder)builder).getCurrentRoot();
        this.undoBuffer.add(this.currentDoc);
        this.currentDoc = copy;
        if (this.undoBuffer.size() > 20) {
            this.undoBuffer.remove(0);
        }
    }

    private void copy(StringBuffer buffer) throws XPathException {
        Item item;
        this.needCurrentDoc();
        SequenceIterator iter = this.getSelectedItems(buffer, 0);
        LinkedTreeBuilder builder = new LinkedTreeBuilder(this.config.makePipelineConfiguration());
        ((Builder)builder).open();
        builder.startDocument(0);
        while ((item = iter.next()) != null) {
            if (item instanceof NodeInfo) {
                ((NodeInfo)item).copy(builder, 2, Loc.NONE);
                continue;
            }
            throw new XPathException("Selected item is not a node");
        }
        this.currentDoc = (DocumentImpl)((Builder)builder).getCurrentRoot();
        this.unsaved = true;
    }

    private void delete(StringBuffer buffer) throws XPathException {
        this.needCurrentDoc();
        this.saveCurrentDoc();
        GroundedValue all = this.getSelectedItems(buffer, 0).materialize();
        for (Item item : all.asIterable()) {
            if (item instanceof MutableNodeInfo) {
                ((MutableNodeInfo)item).delete();
                this.unsaved = true;
                continue;
            }
            if (item instanceof NamespaceNode) {
                NodeInfo parent = ((NamespaceNode)item).getParent();
                if (parent instanceof MutableNodeInfo) {
                    try {
                        ((MutableNodeInfo)parent).removeNamespace(((NamespaceNode)item).getLocalPart());
                    } catch (Exception e) {
                        throw new XPathException("Cannot remove namespace: " + e.getMessage());
                    }
                }
                this.unsaved = true;
                continue;
            }
            throw new XPathException("Selected item is not a mutable node");
        }
    }

    private void load(StringBuffer source) throws XPathException {
        NodeInfo element;
        String fileName = source.toString();
        fileName = fileName.replaceFirst("^~", System.getProperty("user.home"));
        StreamSource ss = new StreamSource(new File(fileName));
        ParseOptions options = new ParseOptions();
        options.setModel(TreeModel.LINKED_TREE);
        options.setLineNumbering(true);
        this.currentDoc = (DocumentImpl)this.config.buildDocumentTree(ss, options).getRootNode();
        this.typed = false;
        HashSet<String> names = new HashSet<String>();
        AxisIterator allElements = this.currentDoc.iterateAxis(4, NodeKindTest.ELEMENT);
        while ((element = allElements.next()) != null) {
            NodeInfo att;
            names.add(element.getLocalPart());
            AxisIterator allAtts = element.iterateAxis(2);
            while ((att = allAtts.next()) != null) {
                names.add("@" + att.getLocalPart());
            }
        }
        ArrayList<String> sortedNames = new ArrayList<String>(names);
        sortedNames.addAll(Arrays.asList(keywords));
        Collections.sort(sortedNames);
        this.talker.setAutoCompletion(sortedNames);
    }

    private void call(StringBuffer source) throws XPathException {
        try {
            FileInputStream is = new FileInputStream(source.toString());
            DefaultTalker talker = new DefaultTalker(is, new PrintStream(System.out));
            this.executeCommands(talker, false);
        } catch (FileNotFoundException e) {
            throw new XPathException("Script not found: " + e.getMessage());
        }
    }

    private void namespace(StringBuffer buffer) throws XPathException {
        int ws = buffer.indexOf(" ");
        if (ws < 0) {
            throw new XPathException("No namespace prefix supplied");
        }
        String prefix = buffer.substring(0, ws).trim();
        String uri = buffer.substring(ws).trim();
        this.env.declareNamespace(prefix, uri);
    }

    private void rename(StringBuffer buffer) throws XPathException {
        Item item;
        this.needCurrentDoc();
        this.saveCurrentDoc();
        SequenceIterator iter = this.getSelectedItems(buffer, 71);
        buffer.replace(0, 3, "");
        XPathExpression renamer = this.getExpression(buffer, 0);
        while ((item = iter.next()) != null) {
            if (item instanceof MutableNodeInfo) {
                StructuredQName newQName;
                Item newName = this.evaluateExpression(renamer, item).next();
                if (newName instanceof QNameValue) {
                    newQName = ((QNameValue)newName).getStructuredQName();
                } else if (newName instanceof AtomicValue) {
                    newQName = this.getQName(newName.getStringValue());
                } else {
                    throw new XPathException("New name must evaluate to a string or QName");
                }
                ((MutableNodeInfo)item).rename(new FingerprintedQName(newQName, this.config.getNamePool()));
                continue;
            }
            throw new XPathException("Selected item is not a renameable node");
        }
    }

    private void replace(StringBuffer buffer) throws XPathException {
        Item item;
        this.needCurrentDoc();
        this.saveCurrentDoc();
        SequenceIterator iter = this.getSelectedItems(buffer, 121);
        buffer.replace(0, 5, "");
        XQueryExpression replacement = this.getQuery(buffer);
        block10: while ((item = iter.next()) != null) {
            if (item instanceof MutableNodeInfo) {
                MutableNodeInfo target = (MutableNodeInfo)item;
                GroundedValue newValue = this.evaluateQuery(replacement, item).materialize();
                if (newValue instanceof AtomicValue) {
                    Orphan orphan = new Orphan(this.config);
                    orphan.setNodeKind((short)3);
                    orphan.setStringValue(newValue.getStringValue());
                    newValue = orphan;
                }
                switch (target.getNodeKind()) {
                    case 9: {
                        throw new XPathException("Cannot replace a document node");
                    }
                    case 1: 
                    case 3: 
                    case 7: 
                    case 8: {
                        ArrayList<NodeInfo> newChildren = new ArrayList<NodeInfo>();
                        block11: for (Item item2 : newValue.asIterable()) {
                            if (item2 instanceof NodeInfo) {
                                switch (((NodeInfo)item2).getNodeKind()) {
                                    case 2: {
                                        throw new XPathException("Cannot replace non-attribute with attribute");
                                    }
                                    case 13: {
                                        throw new XPathException("Cannot replace non-namespace node with namespace node");
                                    }
                                    case 9: {
                                        for (NodeInfo nodeInfo : ((NodeInfo)item2).children()) {
                                            newChildren.add(nodeInfo);
                                        }
                                        continue block11;
                                    }
                                    default: {
                                        newChildren.add((NodeInfo)item2);
                                        break;
                                    }
                                }
                                continue;
                            }
                            if (!(item2 instanceof AtomicValue)) continue;
                            Orphan orphan = new Orphan(this.config);
                            orphan.setNodeKind((short)3);
                            orphan.setStringValue(item2.getStringValue());
                            newChildren.add(orphan);
                        }
                        if (newChildren.isEmpty()) continue block10;
                        NodeInfo[] childArray = newChildren.toArray(new NodeInfo[0]);
                        target.replace(childArray, true);
                        continue block10;
                    }
                    case 2: {
                        ((MutableNodeInfo)target.getParent()).removeAttribute(target);
                        if (newValue.getLength() == 0) break;
                        if (newValue.getLength() == 1 && newValue.itemAt(0) instanceof NodeInfo && ((NodeInfo)newValue.itemAt(0)).getNodeKind() == 2) {
                            NodeInfo att = (NodeInfo)newValue.itemAt(0);
                            ((MutableNodeInfo)target.getParent()).addAttribute(NameOfNode.makeName(att), (SimpleType)att.getSchemaType(), att.getStringValueCS(), 0);
                            break;
                        }
                        throw new XPathException("Replacement for an attribute must be an attribute");
                    }
                }
                throw new XPathException("Cannot replace a namespace node");
            }
            throw new XPathException("Selected item is not a mutable node");
        }
        this.unsaved = true;
    }

    private void undo(StringBuffer buffer) throws XPathException {
        int len = this.undoBuffer.size();
        if (len <= 0) {
            throw new XPathException("Nothing to undo");
        }
        this.currentDoc = this.undoBuffer.remove(len - 1);
    }

    private void update(StringBuffer buffer, String where) throws XPathException {
        Item item;
        this.needCurrentDoc();
        this.saveCurrentDoc();
        SequenceIterator iter = this.getSelectedItems(buffer, 121);
        buffer.replace(0, 5, "");
        XQueryExpression newContent = this.getQuery(buffer);
        while ((item = iter.next()) != null) {
            if (item instanceof MutableNodeInfo) {
                MutableNodeInfo target = (MutableNodeInfo)item;
                GroundedValue newValue = this.evaluateQuery(newContent, item).materialize();
                if (newValue instanceof AtomicValue && where.equals("content")) {
                    target.replaceStringValue(((AtomicValue)newValue).getStringValueCS());
                    continue;
                }
                ArrayList<NodeInfo> replacement = new ArrayList<NodeInfo>();
                ArrayList<NodeInfo> replacementAtts = new ArrayList<NodeInfo>();
                block19: for (Item item2 : newValue.asIterable()) {
                    if (item2 instanceof NodeInfo) {
                        switch (((NodeInfo)item2).getNodeKind()) {
                            case 2: {
                                replacementAtts.add((NodeInfo)item2);
                                break;
                            }
                            case 13: {
                                throw new XPathException("Cannot replace namespace nodes");
                            }
                            case 9: {
                                for (NodeInfo nodeInfo : ((NodeInfo)item2).children()) {
                                    replacement.add(nodeInfo);
                                }
                                continue block19;
                            }
                            default: {
                                replacement.add((NodeInfo)item2);
                                break;
                            }
                        }
                        continue;
                    }
                    if (!(item2 instanceof AtomicValue)) continue;
                    Orphan orphan = new Orphan(this.config);
                    orphan.setNodeKind((short)3);
                    orphan.setStringValue(item2.getStringValue());
                    replacement.add(orphan);
                }
                if (!(replacementAtts.isEmpty() || where.equals("prefix") || where.equals("update"))) {
                    throw new XPathException("Cannot supply attributes for " + where + " command (use 'prefix')");
                }
                if (!replacement.isEmpty()) {
                    NodeInfo[] childArray = replacement.toArray(new NodeInfo[0]);
                    switch (where) {
                        case "content": {
                            target.replace(childArray, true);
                            break;
                        }
                        case "precede": {
                            target.insertSiblings(childArray, true, true);
                            break;
                        }
                        case "follow": {
                            target.insertSiblings(childArray, false, true);
                            break;
                        }
                        case "prefix": {
                            target.insertChildren(childArray, true, true);
                            break;
                        }
                        default: {
                            target.insertChildren(childArray, false, true);
                        }
                    }
                }
                for (NodeInfo nodeInfo : replacementAtts) {
                    NodeName attName = NameOfNode.makeName(nodeInfo);
                    NodeInfo nodeInfo2 = target.iterateAxis(2, new NameTest(2, attName, this.config.getNamePool())).next();
                    if (nodeInfo2 != null) {
                        target.removeAttribute(nodeInfo2);
                    }
                    target.addAttribute(attName, BuiltInAtomicType.UNTYPED_ATOMIC, nodeInfo.getStringValue(), 0);
                }
                continue;
            }
            throw new XPathException("Selected item is not a mutable node");
        }
        this.unsaved = true;
    }

    private void save(StringBuffer buffer) throws XPathException {
        StringValue prop;
        this.needCurrentDoc();
        Whitespace.Tokenizer tokens = new Whitespace.Tokenizer(buffer);
        StringValue fileName = tokens.next();
        if (fileName == null) {
            throw new XPathException("No file name supplied");
        }
        File dest = new File(fileName.getStringValue());
        if (dest.exists()) {
            String answer;
            while (!(answer = this.talker.exchange("Overwrite existing file? (Y|N)")).equalsIgnoreCase("y")) {
                if (!answer.equalsIgnoreCase("n")) continue;
                return;
            }
        }
        StreamResult out = new StreamResult(dest);
        SerializationProperties props = new SerializationProperties();
        while ((prop = tokens.next()) != null) {
            try {
                String[] parts = prop.getStringValue().split("=");
                props.setProperty(parts[0].trim(), parts[1].trim());
            } catch (Exception e) {
                System.out.println("Unrecognized output property '" + prop);
            }
        }
        Receiver s = this.config.getSerializerFactory().getReceiver(out, props);
        s.open();
        this.currentDoc.copy(s, 2, Loc.NONE);
        s.close();
        System.out.println("Written to " + new File(fileName.getStringValue()).getAbsolutePath());
        this.unsaved = false;
    }

    private void schema(StringBuffer buffer) throws XPathException {
        if (!this.config.isLicensedFeature(1)) {
            throw new XPathException("Schema processing is not supported in this Saxon configuration");
        }
        String fileName = buffer.toString();
        fileName = fileName.replaceFirst("^~", System.getProperty("user.home"));
        this.config.loadSchema(new File(fileName).getAbsoluteFile().toURI().toString());
    }

    private void set(StringBuffer buffer) throws XPathException {
        int ws = buffer.indexOf("=");
        if (ws < 0 || ws == buffer.length() - 1) {
            throw new XPathException("Format: set name = value");
        }
        String varName = buffer.substring(0, ws).trim();
        if (varName.startsWith("$")) {
            varName = varName.substring(1);
        }
        DocumentImpl saved = this.currentDoc;
        GroundedValue value = this.getSelectedItems(new StringBuffer(buffer.substring(ws + 1)), 0).materialize();
        if (varName.equals(".")) {
            this.saveCurrentDoc();
            if (value.getLength() == 1 && value.itemAt(0) instanceof DocumentImpl) {
                this.currentDoc = (DocumentImpl)value.itemAt(0);
            } else {
                try {
                    LinkedTreeBuilder builder = new LinkedTreeBuilder(this.config.makePipelineConfiguration());
                    ComplexContentOutputter cco = new ComplexContentOutputter(builder);
                    cco.open();
                    cco.startDocument(0);
                    for (Item item : value.asIterable()) {
                        cco.append(item);
                    }
                    cco.endDocument();
                    cco.close();
                    this.currentDoc = (DocumentImpl)((Builder)builder).getCurrentRoot();
                } catch (XPathException e) {
                    throw new XPathException("Cannot save the value as a document (" + e.getMessage() + ")");
                }
            }
        } else {
            StructuredQName name = this.getQName(varName);
            this.variables.put(name, value);
            this.currentDoc = saved;
        }
    }

    private void validate(StringBuffer buffer) throws XPathException {
        if (!this.config.isLicensedFeature(1)) {
            throw new XPathException("Schema processing is not supported in this Saxon configuration");
        }
        this.needCurrentDoc();
        this.saveCurrentDoc();
        PipelineConfiguration pipe = this.config.makePipelineConfiguration();
        LinkedTreeBuilder builder = new LinkedTreeBuilder(pipe);
        ((Builder)builder).open();
        ParseOptions options = new ParseOptions();
        options.setSchemaValidationMode(1);
        Receiver val = this.config.getDocumentValidator(builder, this.currentDoc.getSystemId(), options, Loc.NONE);
        this.currentDoc.copy(val, 2, Loc.NONE);
        ((Builder)builder).close();
        this.currentDoc = (DocumentImpl)((Builder)builder).getCurrentRoot();
        this.unsaved = true;
        this.typed = true;
    }

    private void list(StringBuffer buffer) throws XPathException {
        this.needCurrentDoc();
        SequenceIterator iter = this.getSelectedItems(buffer, 0);
        GroundedValue value = iter.materialize();
        int size = value.getLength();
        if (size != 1) {
            System.out.println("Found " + size + " items");
        }
        for (Item item : value.asIterable()) {
            if (item instanceof NodeInfo) {
                int lineNumber = ((NodeInfo)item).getLineNumber();
                String prefix = lineNumber >= 0 ? "Line " + lineNumber + ": " : "";
                System.out.println(prefix + Navigator.getPath((NodeInfo)item));
                continue;
            }
            System.out.println(item.getStringValue());
        }
    }

    private void show(StringBuffer buffer) throws XPathException {
        SequenceIterator iter;
        GroundedValue value;
        int size;
        this.needCurrentDoc();
        if (buffer.toString().trim().isEmpty()) {
            buffer = new StringBuffer(".");
        }
        if ((size = (value = (iter = this.getSelectedItems(buffer, 0)).materialize()).getLength()) != 1) {
            System.out.println("Found " + size + " items");
        }
        for (Item item : value.asIterable()) {
            if (item instanceof NodeInfo) {
                System.out.println(QueryResult.serialize((NodeInfo)item));
                continue;
            }
            if (item instanceof AtomicValue) {
                System.out.println(item.getStringValue());
                continue;
            }
            StringWriter sw = new StringWriter();
            SerializationProperties props = new SerializationProperties();
            props.setProperty("method", "adaptive");
            Receiver r = this.config.getSerializerFactory().getReceiver(new StreamResult(sw), props);
            r.append(item);
            System.out.println(sw.toString());
        }
    }

    private void transform(StringBuffer buffer) throws XPathException {
        try {
            this.needCurrentDoc();
            this.saveCurrentDoc();
            String fileName = buffer.toString();
            fileName = fileName.replaceFirst("^~", System.getProperty("user.home"));
            StreamSource ss = new StreamSource(new File(fileName));
            Templates templates = new TransformerFactoryImpl(this.config).newTemplates(ss);
            Transformer transformer = templates.newTransformer();
            LinkedTreeBuilder result = new LinkedTreeBuilder(this.config.makePipelineConfiguration());
            ((Builder)result).open();
            transformer.transform(this.currentDoc, result);
            ((Builder)result).close();
            this.currentDoc = (DocumentImpl)((Builder)result).getCurrentRoot();
        } catch (TransformerException e) {
            throw XPathException.makeXPathException(e);
        }
    }

    @FunctionalInterface
    private static interface Action {
        public void perform(StringBuffer var1) throws XPathException;
    }

    private static class SubCommand {
        String name;
        String helpText;
        Action action;

        private SubCommand() {
        }
    }
}

