/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.style;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;
import net.sf.saxon.expr.Component;
import net.sf.saxon.expr.accum.Accumulator;
import net.sf.saxon.om.AttributeInfo;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.style.Compilation;
import net.sf.saxon.style.ComponentDeclaration;
import net.sf.saxon.style.PrincipalStylesheetModule;
import net.sf.saxon.style.StyleElement;
import net.sf.saxon.style.StylesheetPackage;
import net.sf.saxon.trans.Mode;
import net.sf.saxon.trans.RecoveryPolicy;
import net.sf.saxon.trans.SimpleMode;
import net.sf.saxon.trans.SymbolicName;
import net.sf.saxon.trans.Visibility;
import net.sf.saxon.trans.VisibilityProvenance;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.trans.rules.BuiltInRuleSet;
import net.sf.saxon.trans.rules.DeepCopyRuleSet;
import net.sf.saxon.trans.rules.DeepSkipRuleSet;
import net.sf.saxon.trans.rules.FailRuleSet;
import net.sf.saxon.trans.rules.RuleManager;
import net.sf.saxon.trans.rules.RuleSetWithWarnings;
import net.sf.saxon.trans.rules.ShallowCopyRuleSet;
import net.sf.saxon.trans.rules.ShallowSkipRuleSet;
import net.sf.saxon.trans.rules.TextOnlyCopyRuleSet;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.Whitespace;

public class XSLMode
extends StyleElement {
    private SimpleMode mode;
    private Set<? extends Accumulator> accumulators;
    private boolean prepared = false;
    private boolean streamable = false;
    private boolean failOnMultipleMatch = false;
    private boolean warningOnNoMatch = false;
    private boolean warningOnMultipleMatch = true;
    private boolean traceMatching = false;
    private BuiltInRuleSet defaultRules = TextOnlyCopyRuleSet.getInstance();

    @Override
    public boolean isDeclaration() {
        return true;
    }

    @Override
    public boolean isInstruction() {
        return false;
    }

    @Override
    public StructuredQName getObjectName() {
        StructuredQName qn = super.getObjectName();
        if (qn == null) {
            String nameAtt = Whitespace.trim(this.getAttributeValue("", "name"));
            if (nameAtt == null) {
                return Mode.UNNAMED_MODE_NAME;
            }
            qn = this.makeQName(nameAtt, null, "name");
            this.setObjectName(qn);
        }
        return qn;
    }

    @Override
    public void index(ComponentDeclaration decl, PrincipalStylesheetModule top) throws XPathException {
        Component other;
        StructuredQName name = this.getObjectName();
        SymbolicName sName = new SymbolicName(174, name);
        HashMap<SymbolicName, Component> componentIndex = top.getStylesheetPackage().getComponentIndex();
        if (!name.equals(Mode.UNNAMED_MODE_NAME) && (other = componentIndex.get(sName)) != null && other.getDeclaringPackage() != top.getStylesheetPackage()) {
            this.compileError("Mode " + name.getDisplayName() + " conflicts with a public named mode in package " + other.getDeclaringPackage().getPackageName(), "XTSE3050");
        }
        this.mode = (SimpleMode)top.getRuleManager().obtainMode(name, true);
        if (name.equals(Mode.UNNAMED_MODE_NAME)) {
            top.getRuleManager().setUnnamedModeExplicit(true);
        } else if (this.mode.getDeclaringComponent().getDeclaringPackage() != this.getContainingPackage()) {
            this.compileError("Mode name conflicts with a mode in a used package", "XTSE3050");
        } else {
            top.indexMode(decl);
            Visibility declaredVisibility = this.getDeclaredVisibility();
            Visibility actualVisibility = declaredVisibility == null ? Visibility.PRIVATE : declaredVisibility;
            VisibilityProvenance provenance = declaredVisibility == null ? VisibilityProvenance.DEFAULTED : VisibilityProvenance.EXPLICIT;
            this.mode.getDeclaringComponent().setVisibility(actualVisibility, provenance);
            top.indexMode(decl);
        }
    }

    @Override
    public void prepareAttributes() {
        String nameAtt = null;
        String visibilityAtt = null;
        String extraAsAtt = null;
        if (this.prepared) {
            return;
        }
        this.prepared = true;
        Visibility visibility = Visibility.PRIVATE;
        block48: for (AttributeInfo att : this.attributes()) {
            NodeName attName = att.getNodeName();
            String f = attName.getDisplayName();
            String value = att.getValue();
            block12 : switch (f) {
                case "streamable": {
                    this.streamable = this.processStreamableAtt(value);
                    break;
                }
                case "name": {
                    nameAtt = Whitespace.trim(value);
                    if (nameAtt.equals("#default")) continue block48;
                    this.setObjectName(this.makeQName(nameAtt, null, "name"));
                    break;
                }
                case "use-accumulators": {
                    this.accumulators = this.getPrincipalStylesheetModule().getStylesheetPackage().getAccumulatorRegistry().getUsedAccumulators(value, this);
                    break;
                }
                case "on-multiple-match": {
                    switch (Whitespace.trim(value)) {
                        case "fail": {
                            this.failOnMultipleMatch = true;
                            break block12;
                        }
                        case "use-last": {
                            this.failOnMultipleMatch = false;
                            break block12;
                        }
                    }
                    this.invalidAttribute(f, "fail|use-last");
                    break;
                }
                case "on-no-match": {
                    switch (Whitespace.trim(value)) {
                        case "text-only-copy": {
                            break block12;
                        }
                        case "shallow-copy": {
                            this.defaultRules = ShallowCopyRuleSet.getInstance();
                            break block12;
                        }
                        case "deep-copy": {
                            this.defaultRules = DeepCopyRuleSet.getInstance();
                            break block12;
                        }
                        case "shallow-skip": {
                            this.defaultRules = ShallowSkipRuleSet.getInstance();
                            break block12;
                        }
                        case "deep-skip": {
                            this.defaultRules = DeepSkipRuleSet.getInstance();
                            break block12;
                        }
                        case "fail": {
                            this.defaultRules = FailRuleSet.getInstance();
                            break block12;
                        }
                    }
                    this.invalidAttribute(f, "text-only-copy|shallow-copy|deep-copy|shallow-skip|deep-skip|fail");
                    break;
                }
                case "warning-on-multiple-match": {
                    this.warningOnMultipleMatch = this.processBooleanAttribute("warning-on-multiple-match", value);
                    break;
                }
                case "warning-on-no-match": {
                    this.warningOnNoMatch = this.processBooleanAttribute("warning-on-no-match", value);
                    break;
                }
                case "typed": {
                    this.checkAttributeValue("typed", Whitespace.trim(value), false, new String[]{"0", "1", "false", "lax", "no", "strict", "true", "unspecified", "yes"});
                    break;
                }
                case "visibility": {
                    visibilityAtt = Whitespace.trim(value);
                    visibility = this.interpretVisibilityValue(visibilityAtt, "");
                    if (visibility == Visibility.ABSTRACT) {
                        this.invalidAttribute(f, "public|private|final");
                    }
                    this.mode.setDeclaredVisibility(visibility);
                    break;
                }
                default: {
                    if (attName.hasURI("http://saxon.sf.net/")) {
                        this.isExtensionAttributeAllowed(attName.getDisplayName());
                        if (attName.getLocalPart().equals("trace")) {
                            this.traceMatching = this.processBooleanAttribute("saxon:trace", value);
                            break;
                        }
                        if (!attName.getLocalPart().equals("as")) continue block48;
                        extraAsAtt = value;
                        break;
                    }
                    this.checkUnknownAttribute(attName);
                }
            }
        }
        if (nameAtt == null && visibilityAtt != null && this.mode.getDeclaredVisibility() != Visibility.PRIVATE) {
            this.compileError("The unnamed mode must be private", "XTSE0020");
        }
        RuleManager manager = this.getCompilation().getPrincipalStylesheetModule().getRuleManager();
        if (this.getObjectName() == null) {
            this.mode = manager.getUnnamedMode();
        } else {
            Mode m = manager.obtainMode(this.getObjectName(), true);
            if (m instanceof SimpleMode) {
                this.mode = (SimpleMode)m;
            } else {
                this.compileError("Mode name refers to an overridden mode");
                this.mode = manager.getUnnamedMode();
            }
        }
        this.mode.setStreamable(this.streamable);
        if (this.streamable) {
            Mode omniMode = manager.obtainMode(Mode.OMNI_MODE, true);
            omniMode.setStreamable(true);
        }
        if (this.warningOnNoMatch) {
            this.defaultRules = new RuleSetWithWarnings(this.defaultRules);
        }
        this.mode.setBuiltInRuleSet(this.defaultRules);
        RecoveryPolicy recoveryPolicy = this.failOnMultipleMatch ? RecoveryPolicy.DO_NOT_RECOVER : (this.warningOnMultipleMatch ? RecoveryPolicy.RECOVER_WITH_WARNINGS : RecoveryPolicy.RECOVER_SILENTLY);
        this.mode.setRecoveryPolicy(recoveryPolicy);
        this.mode.obtainDeclaringComponent(this);
        this.mode.setModeTracing(this.traceMatching);
        if (extraAsAtt != null) {
            SequenceType extraResultType = null;
            try {
                extraResultType = this.makeExtendedSequenceType(extraAsAtt);
            } catch (XPathException e) {
                this.compileErrorInAttribute(e.getMessage(), e.getErrorCodeLocalPart(), "saxon:as");
                extraResultType = SequenceType.ANY_SEQUENCE;
            }
            this.mode.setDefaultResultType(extraResultType);
        }
    }

    @Override
    public void validate(ComponentDeclaration decl) throws XPathException {
        this.checkTopLevel("XTSE0010", false);
        for (AttributeInfo att : this.attributes()) {
            NodeName attName = att.getNodeName();
            String f = attName.getDisplayName();
            String attValue = att.getValue();
            if (f.equals("streamable") || f.equals("on-multiple-match") || f.equals("on-no-match") || f.equals("warning-on-multiple-match") || f.equals("warning-on-no-match") || f.equals("typed")) {
                String trimmed = Whitespace.trim(attValue);
                String normalizedAtt = "true".equals(trimmed) || "1".equals(trimmed) ? "yes" : ("false".equals(trimmed) || "0".equals(trimmed) ? "no" : trimmed);
                this.mode.getActivePart().setExplicitProperty(f, normalizedAtt, decl.getPrecedence());
                if (!this.mode.isMustBeTyped() || !this.getContainingPackage().getTargetEdition().matches("JS\\d?")) continue;
                this.compileWarning("In Saxon-JS, all data is untyped", "XTTE3110");
                continue;
            }
            if (!f.equals("use-accumulators") || this.accumulators == null) continue;
            Object[] names = new String[this.accumulators.size()];
            int i = 0;
            for (Accumulator accumulator : this.accumulators) {
                names[i++] = accumulator.getAccumulatorName().getEQName();
            }
            Arrays.sort(names);
            String allNames = Arrays.toString(names);
            this.mode.getActivePart().setExplicitProperty(f, allNames, decl.getPrecedence());
        }
        this.checkEmpty();
        this.checkTopLevel("XTSE0010", false);
    }

    @Override
    public void compileDeclaration(Compilation compilation, ComponentDeclaration decl) throws XPathException {
        StylesheetPackage pack = this.getPrincipalStylesheetModule().getStylesheetPackage();
        Component c = pack.getComponent(this.mode.getSymbolicName());
        if (c == null) {
            throw new AssertionError();
        }
        if (this.accumulators != null) {
            this.mode.setAccumulators(this.accumulators);
        }
    }
}

