/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.style;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.sf.saxon.expr.Component;
import net.sf.saxon.expr.instruct.GlobalContextRequirement;
import net.sf.saxon.om.AttributeInfo;
import net.sf.saxon.om.AttributeMap;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.pattern.NodeKindTest;
import net.sf.saxon.style.ComponentDeclaration;
import net.sf.saxon.style.PackageVersionRanges;
import net.sf.saxon.style.PrincipalStylesheetModule;
import net.sf.saxon.style.StyleElement;
import net.sf.saxon.style.StylesheetComponent;
import net.sf.saxon.style.StylesheetPackage;
import net.sf.saxon.style.XSLAccept;
import net.sf.saxon.style.XSLOverride;
import net.sf.saxon.style.XSLTemplate;
import net.sf.saxon.trans.CompilerInfo;
import net.sf.saxon.trans.ComponentTest;
import net.sf.saxon.trans.CompoundMode;
import net.sf.saxon.trans.Err;
import net.sf.saxon.trans.Mode;
import net.sf.saxon.trans.SimpleMode;
import net.sf.saxon.trans.SymbolicName;
import net.sf.saxon.trans.Visibility;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.trans.packages.PackageDetails;
import net.sf.saxon.trans.rules.RuleManager;
import net.sf.saxon.tree.iter.AxisIterator;
import net.sf.saxon.value.Whitespace;

public class XSLUsePackage
extends StyleElement {
    private String nameAtt = null;
    private PackageVersionRanges versionRanges = null;
    private StylesheetPackage usedPackage;
    private List<XSLAccept> acceptors = null;

    void findUsedPackage(CompilerInfo info) throws XPathException {
        if (this.usedPackage == null) {
            GlobalContextRequirement gcr;
            PackageVersionRanges ranges;
            if (this.nameAtt == null) {
                this.nameAtt = Whitespace.trim(this.getAttributeValue("", "name"));
            }
            if (this.nameAtt == null) {
                this.reportAbsence("name");
                this.nameAtt = "unnamed-package";
            }
            PackageDetails pack = (ranges = this.getPackageVersionRanges()) == null ? null : info.getPackageLibrary().findPackage(this.nameAtt, ranges);
            StylesheetPackage stylesheetPackage = this.usedPackage = pack == null ? null : pack.loadedPackage;
            if (this.usedPackage == null) {
                this.compileErrorInAttribute("Package " + this.nameAtt + " could not be found", "XTSE3000", "name");
                this.usedPackage = this.getConfiguration().makeStylesheetPackage();
                this.usedPackage.setJustInTimeCompilation(info.isJustInTimeCompilation());
            }
            if ((gcr = this.usedPackage.getContextItemRequirements()) != null && !gcr.isMayBeOmitted()) {
                this.compileError("Package " + this.getAttributeValue("name") + " requires a global context item, so it cannot be used as a library package", "XTTE0590");
            }
        }
    }

    @Override
    public StylesheetPackage getUsedPackage() {
        return this.usedPackage;
    }

    private PackageVersionRanges getPackageVersionRanges() throws XPathException {
        if (this.versionRanges == null) {
            this.prepareAttributes();
        }
        return this.versionRanges;
    }

    @Override
    protected void prepareAttributes() {
        AttributeMap atts = this.attributes();
        String ranges = "*";
        for (AttributeInfo att : atts) {
            NodeName attName = att.getNodeName();
            String f = attName.getDisplayName();
            if (f.equals("name")) {
                this.nameAtt = Whitespace.trim(att.getValue());
                continue;
            }
            if (f.equals("package-version")) {
                ranges = Whitespace.trim(att.getValue()).replaceAll("\\\\", "");
                continue;
            }
            this.checkUnknownAttribute(attName);
        }
        try {
            this.versionRanges = new PackageVersionRanges(ranges);
        } catch (XPathException e) {
            this.compileError(e);
        }
    }

    @Override
    public boolean isDeclaration() {
        return true;
    }

    @Override
    public void validate(ComponentDeclaration decl) {
        for (NodeInfo nodeInfo : this.children()) {
            if (nodeInfo.getNodeKind() == 3) {
                this.compileError("Character content is not allowed as a child of xsl:use-package");
                continue;
            }
            if (nodeInfo instanceof XSLAccept || nodeInfo instanceof XSLOverride) continue;
            this.compileError("Child element " + Err.wrap(nodeInfo.getDisplayName(), 1) + " is not allowed as a child of xsl:use-package", "XTSE0010");
        }
    }

    private Set<SymbolicName> getExplicitAcceptedComponentNames() throws XPathException {
        HashSet<SymbolicName> explicitAccepts = new HashSet<SymbolicName>();
        for (NodeInfo nodeInfo : this.children(XSLAccept.class::isInstance)) {
            Set<ComponentTest> explicitComponentTests = ((XSLAccept)nodeInfo).getExplicitComponentTests();
            for (ComponentTest test : explicitComponentTests) {
                SymbolicName name = test.getSymbolicNameIfExplicit();
                explicitAccepts.add(name);
            }
        }
        return explicitAccepts;
    }

    @Override
    public void postValidate() throws XPathException {
        for (NodeInfo nodeInfo : this.children()) {
            if (!(nodeInfo instanceof XSLOverride) && !(nodeInfo instanceof XSLAccept)) continue;
            ((StyleElement)nodeInfo).postValidate();
        }
        Set<SymbolicName> accepts = this.getExplicitAcceptedComponentNames();
        Set<SymbolicName> set = this.getNamedOverrides();
        if (!accepts.isEmpty()) {
            for (SymbolicName o : set) {
                SymbolicName.F n;
                if (accepts.contains(o)) {
                    this.compileError("Cannot accept and override the same component (" + o + ")", "XTSE3051");
                }
                if (o.getComponentKind() != 158 || !accepts.contains(n = new SymbolicName.F(o.getComponentName(), -1))) continue;
                this.compileError("Cannot accept and override the same function (" + o + ")", "XTSE3051");
            }
        }
    }

    List<XSLAccept> getAcceptors() {
        if (this.acceptors == null) {
            this.acceptors = new ArrayList<XSLAccept>();
            for (NodeInfo nodeInfo : this.children(XSLAccept.class::isInstance)) {
                this.acceptors.add((XSLAccept)nodeInfo);
            }
        }
        return this.acceptors;
    }

    void gatherNamedOverrides(PrincipalStylesheetModule module, List<ComponentDeclaration> topLevel, Set<SymbolicName> overrides) throws XPathException {
        if (this.usedPackage == null) {
            return;
        }
        for (NodeInfo nodeInfo : this.children(XSLOverride.class::isInstance)) {
            for (NodeInfo nodeInfo2 : nodeInfo.children(StylesheetComponent.class::isInstance)) {
                StructuredQName[] modeNames;
                ComponentDeclaration decl = new ComponentDeclaration(module, (StyleElement)nodeInfo2);
                topLevel.add(decl);
                SymbolicName name = ((StylesheetComponent)((Object)nodeInfo2)).getSymbolicName();
                if (name != null) {
                    overrides.add(name);
                    continue;
                }
                if (!(nodeInfo2 instanceof XSLTemplate) || nodeInfo2.getAttributeValue("", "match") == null) continue;
                for (StructuredQName m : modeNames = ((XSLTemplate)nodeInfo2).getModeNames()) {
                    overrides.add(new SymbolicName(174, m));
                }
            }
        }
    }

    private Set<SymbolicName> getNamedOverrides() {
        NodeInfo override;
        HashSet<SymbolicName> overrides = new HashSet<SymbolicName>();
        AxisIterator kids = this.iterateAxis(3, NodeKindTest.ELEMENT);
        while ((override = kids.next()) != null) {
            NodeInfo overridingDeclaration;
            if (!(override instanceof XSLOverride)) continue;
            AxisIterator overridings = override.iterateAxis(3, NodeKindTest.ELEMENT);
            while ((overridingDeclaration = overridings.next()) != null) {
                SymbolicName name;
                if (!(overridingDeclaration instanceof StylesheetComponent) || (name = ((StylesheetComponent)((Object)overridingDeclaration)).getSymbolicName()) == null) continue;
                overrides.add(name);
            }
        }
        return overrides;
    }

    void gatherRuleOverrides(PrincipalStylesheetModule module, Set<SymbolicName> overrides) throws XPathException {
        NodeInfo override;
        StylesheetPackage thisPackage = module.getStylesheetPackage();
        RuleManager ruleManager = module.getRuleManager();
        AxisIterator kids = this.iterateAxis(3, NodeKindTest.ELEMENT);
        HashSet<SymbolicName> overriddenModes = new HashSet<SymbolicName>();
        while ((override = kids.next()) != null) {
            NodeInfo overridingDeclaration;
            if (!(override instanceof XSLOverride)) continue;
            AxisIterator overridings = override.iterateAxis(3, NodeKindTest.ELEMENT);
            while ((overridingDeclaration = overridings.next()) != null) {
                StructuredQName[] modeNames;
                if (!(overridingDeclaration instanceof XSLTemplate) || overridingDeclaration.getAttributeValue("", "match") == null) continue;
                for (StructuredQName modeName : modeNames = ((XSLTemplate)overridingDeclaration).getModeNames()) {
                    if (modeName.equals(Mode.OMNI_MODE)) {
                        ((StyleElement)overridingDeclaration).compileError("The mode name #all must not appear in an overriding template rule", "XTSE3440");
                        continue;
                    }
                    SymbolicName symbolicName = new SymbolicName(174, modeName);
                    overrides.add(symbolicName);
                    overriddenModes.add(symbolicName);
                    Component.M derivedComponent = (Component.M)thisPackage.getComponent(symbolicName);
                    if (derivedComponent == null) {
                        ((StyleElement)overridingDeclaration).compileError("Mode " + modeName.getDisplayName() + " is not defined in the used package", "XTSE3060");
                        continue;
                    }
                    if (derivedComponent.getBaseComponent() == null) {
                        ((StyleElement)overridingDeclaration).compileError("Mode " + modeName.getDisplayName() + " cannot be overridden because it is local to this package", "XTSE3440");
                        continue;
                    }
                    Component.M usedComponent = (Component.M)derivedComponent.getBaseComponent();
                    if (usedComponent.getVisibility() == Visibility.FINAL) {
                        ((StyleElement)overridingDeclaration).compileError("Cannot define overriding template rules in mode " + modeName.getDisplayName() + " because it has visibility=final", "XTSE3060");
                        continue;
                    }
                    Mode usedMode = usedComponent.getActor();
                    if (usedComponent.getVisibility() != Visibility.PUBLIC) {
                        ((StyleElement)overridingDeclaration).compileError("Cannot override template rules in mode " + modeName.getDisplayName() + ", because the mode is not public", "XTSE3060");
                        continue;
                    }
                    if (derivedComponent.getActor() != usedMode) continue;
                    SimpleMode overridingMode = new SimpleMode(modeName);
                    CompoundMode newCompoundMode = new CompoundMode(usedMode, overridingMode);
                    newCompoundMode.setDeclaringComponent(derivedComponent);
                    ruleManager.registerMode(newCompoundMode);
                    derivedComponent.setActor(newCompoundMode);
                }
            }
        }
        RuleManager usedPackageRuleManager = this.usedPackage.getRuleManager();
        if (usedPackageRuleManager != null) {
            for (Mode m : usedPackageRuleManager.getAllNamedModes()) {
                Component c;
                SymbolicName sn = m.getSymbolicName();
                if (overriddenModes.contains(sn) || (c = thisPackage.getComponent(sn)) == null || c.getVisibility() == Visibility.PRIVATE) continue;
                ruleManager.registerMode((Mode)c.getActor());
            }
        }
    }
}

