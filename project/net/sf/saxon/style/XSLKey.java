/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.style;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.function.Predicate;
import net.sf.saxon.Configuration;
import net.sf.saxon.Version;
import net.sf.saxon.expr.AtomicSequenceConverter;
import net.sf.saxon.expr.Atomizer;
import net.sf.saxon.expr.Component;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.GlobalVariableReference;
import net.sf.saxon.expr.UserFunctionCall;
import net.sf.saxon.expr.instruct.Actor;
import net.sf.saxon.expr.instruct.ApplyTemplates;
import net.sf.saxon.expr.instruct.CallTemplate;
import net.sf.saxon.expr.instruct.SlotManager;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.Optimizer;
import net.sf.saxon.expr.parser.RoleDiagnostic;
import net.sf.saxon.expr.sort.CodepointCollator;
import net.sf.saxon.lib.StringCollator;
import net.sf.saxon.om.AttributeInfo;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.pattern.NodeTestPattern;
import net.sf.saxon.pattern.Pattern;
import net.sf.saxon.style.Compilation;
import net.sf.saxon.style.ComponentDeclaration;
import net.sf.saxon.style.ExpressionContext;
import net.sf.saxon.style.PrincipalStylesheetModule;
import net.sf.saxon.style.StyleElement;
import net.sf.saxon.style.StylesheetComponent;
import net.sf.saxon.trans.Err;
import net.sf.saxon.trans.KeyDefinition;
import net.sf.saxon.trans.KeyManager;
import net.sf.saxon.trans.SymbolicName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.ErrorType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.Whitespace;

public class XSLKey
extends StyleElement
implements StylesheetComponent {
    private Pattern match;
    private Expression use;
    private String collationName;
    private StructuredQName keyName;
    private SlotManager stackFrameMap;
    private boolean rangeKey;
    private boolean composite = false;
    private KeyDefinition keyDefinition;
    private static ContainsGlobalVariable containsGlobalVariable = new ContainsGlobalVariable();

    @Override
    public Actor getActor() {
        throw new UnsupportedOperationException();
    }

    @Override
    public SymbolicName getSymbolicName() {
        return null;
    }

    @Override
    public void checkCompatibility(Component component) {
    }

    @Override
    public boolean isDeclaration() {
        return true;
    }

    @Override
    public boolean mayContainSequenceConstructor() {
        return true;
    }

    @Override
    public SlotManager getSlotManager() {
        return this.stackFrameMap;
    }

    @Override
    public void prepareAttributes() {
        String nameAtt = null;
        String matchAtt = null;
        for (AttributeInfo att : this.attributes()) {
            NodeName attName = att.getNodeName();
            String value = att.getValue();
            String uri = attName.getURI();
            String local = attName.getLocalPart();
            if ("".equals(uri)) {
                switch (local) {
                    case "name": {
                        nameAtt = Whitespace.trim(value);
                        break;
                    }
                    case "use": {
                        String useAtt = value;
                        this.use = this.makeExpression(useAtt, att);
                        break;
                    }
                    case "match": {
                        matchAtt = value;
                        break;
                    }
                    case "collation": {
                        this.collationName = Whitespace.trim(value);
                        break;
                    }
                    case "composite": {
                        this.composite = this.processBooleanAttribute("composite", value);
                        break;
                    }
                    default: {
                        this.checkUnknownAttribute(attName);
                        break;
                    }
                }
                continue;
            }
            if (local.equals("range-key") && uri.equals("http://saxon.sf.net/")) {
                this.rangeKey = this.processBooleanAttribute("range-key", value);
                continue;
            }
            this.checkUnknownAttribute(attName);
        }
        if (nameAtt == null) {
            this.reportAbsence("name");
            nameAtt = "_dummy_key_name";
        }
        this.keyName = this.makeQName(nameAtt, null, "name");
        this.setObjectName(this.keyName);
        if (matchAtt == null) {
            this.reportAbsence("match");
            matchAtt = "*";
        }
        this.match = this.makePattern(matchAtt, "match");
        if (this.match == null) {
            this.match = new NodeTestPattern(ErrorType.getInstance());
        }
    }

    public StructuredQName getKeyName() {
        String nameAtt;
        if (this.getObjectName() == null && (nameAtt = this.getAttributeValue("", "name")) != null) {
            this.setObjectName(this.makeQName(nameAtt, null, "name"));
        }
        return this.getObjectName();
    }

    @Override
    public void validate(ComponentDeclaration decl) throws XPathException {
        Configuration config = this.getConfiguration();
        this.stackFrameMap = config.makeSlotManager();
        this.checkTopLevel("XTSE0010", false);
        if (this.use != null) {
            if (this.hasChildNodes()) {
                this.compileError("An xsl:key element with a @use attribute must be empty", "XTSE1205");
            }
            try {
                RoleDiagnostic role = new RoleDiagnostic(4, "xsl:key/use", 0);
                this.use = config.getTypeChecker(false).staticTypeCheck(this.use, SequenceType.ATOMIC_SEQUENCE, role, this.makeExpressionVisitor());
            } catch (XPathException err) {
                this.compileError(err);
            }
        } else if (!this.hasChildNodes()) {
            this.compileError("An xsl:key element must either have a @use attribute or have content", "XTSE1205");
        }
        this.use = this.typeCheck("use", this.use);
        this.match = this.typeCheck("match", this.match);
        if (this.use != null) {
            this.use = this.use.typeCheck(this.makeExpressionVisitor(), config.makeContextItemStaticInfo(this.match.getItemType(), false));
        }
        if (this.collationName != null) {
            try {
                URI collationURI = new URI(this.collationName);
                if (!collationURI.isAbsolute()) {
                    URI base = new URI(this.getBaseURI());
                    collationURI = base.resolve(collationURI);
                    this.collationName = collationURI.toString();
                }
            } catch (URISyntaxException err) {
                this.compileError("Collation name '" + this.collationName + "' is not a valid URI");
            }
        } else {
            this.collationName = this.getDefaultCollationName();
        }
    }

    @Override
    public void index(ComponentDeclaration decl, PrincipalStylesheetModule top) {
        StructuredQName keyName = this.getKeyName();
        if (keyName != null) {
            top.getKeyManager().preRegisterKeyDefinition(keyName);
        }
    }

    @Override
    public void compileDeclaration(Compilation compilation, ComponentDeclaration decl) throws XPathException {
        ItemType useItemType;
        ExpressionContext env = this.getStaticContext();
        Configuration config = env.getConfiguration();
        StringCollator collator = null;
        if (this.collationName != null) {
            collator = this.findCollation(this.collationName, this.getBaseURI());
            if (collator == null) {
                this.compileError("The collation name " + Err.wrap(this.collationName, 7) + " is not recognized", "XTSE1210");
                collator = CodepointCollator.getInstance();
            }
            if (collator instanceof CodepointCollator) {
                collator = null;
                this.collationName = null;
            } else if (!Version.platform.canReturnCollationKeys(collator)) {
                this.compileError("The collation used for xsl:key must be capable of generating collation keys", "XTSE1210");
            }
        }
        if (this.use == null) {
            Expression body = this.compileSequenceConstructor(compilation, decl, true);
            try {
                this.use = Atomizer.makeAtomizer(body, null);
                this.use = this.use.simplify();
            } catch (XPathException e) {
                this.compileError(e);
            }
            try {
                RoleDiagnostic role = new RoleDiagnostic(4, "xsl:key/use", 0);
                this.use = config.getTypeChecker(false).staticTypeCheck(this.use, SequenceType.ATOMIC_SEQUENCE, role, this.makeExpressionVisitor());
                assert (this.match != null);
                this.use = this.use.typeCheck(this.makeExpressionVisitor(), config.makeContextItemStaticInfo(this.match.getItemType(), false));
            } catch (XPathException err) {
                this.compileError(err);
            }
        }
        if ((useItemType = this.use.getItemType()) == ErrorType.getInstance()) {
            useItemType = BuiltInAtomicType.STRING;
        }
        BuiltInAtomicType useType = (BuiltInAtomicType)useItemType.getPrimitiveItemType();
        if (this.xPath10ModeIsEnabled() && !useType.equals(BuiltInAtomicType.STRING) && !useType.equals(BuiltInAtomicType.UNTYPED_ATOMIC)) {
            this.use = new AtomicSequenceConverter(this.use, BuiltInAtomicType.STRING);
            ((AtomicSequenceConverter)this.use).allocateConverter(config, false);
            useType = BuiltInAtomicType.STRING;
        }
        int nextFree = 0;
        if ((this.match.getDependencies() & 1) != 0) {
            nextFree = 1;
        }
        this.match.allocateSlots(this.stackFrameMap, nextFree);
        boolean sensitive = ExpressionTool.contains(this.use, false, containsGlobalVariable) || ExpressionTool.contains(this.match, false, containsGlobalVariable);
        KeyManager km = this.getCompilation().getPrincipalStylesheetModule().getKeyManager();
        SymbolicName symbolicName = new SymbolicName(165, this.keyName);
        KeyDefinition keydef = new KeyDefinition(symbolicName, this.match, this.use, this.collationName, collator);
        keydef.setPackageData(this.getCompilation().getPackageData());
        keydef.setRangeKey(this.rangeKey);
        keydef.setIndexedItemType(useType);
        keydef.setStackFrameMap(this.stackFrameMap);
        keydef.setLocation(this.getSystemId(), this.getLineNumber());
        keydef.setBackwardsCompatible(this.xPath10ModeIsEnabled());
        keydef.setComposite(this.composite);
        keydef.obtainDeclaringComponent(this);
        try {
            km.addKeyDefinition(this.keyName, keydef, !sensitive, compilation.getConfiguration());
        } catch (XPathException err) {
            this.compileError(err);
        }
        this.keyDefinition = keydef;
    }

    @Override
    public void optimize(ComponentDeclaration declaration) throws XPathException {
        ExpressionVisitor visitor = this.makeExpressionVisitor();
        ContextItemStaticInfo contextItemType = this.getConfiguration().makeContextItemStaticInfo(this.match.getItemType(), false);
        Expression useExp = this.keyDefinition.getUse();
        useExp = useExp.optimize(visitor, contextItemType);
        this.allocateLocalSlots(useExp);
        this.keyDefinition.setBody(useExp);
    }

    @Override
    public void generateByteCode(Optimizer opt) {
    }

    private static class ContainsGlobalVariable
    implements Predicate<Expression> {
        private ContainsGlobalVariable() {
        }

        @Override
        public boolean test(Expression e) {
            return e instanceof GlobalVariableReference || e instanceof UserFunctionCall || e instanceof CallTemplate || e instanceof ApplyTemplates;
        }
    }
}

