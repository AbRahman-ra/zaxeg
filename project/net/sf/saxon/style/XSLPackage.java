/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.style;

import net.sf.saxon.om.AttributeInfo;
import net.sf.saxon.om.AttributeMap;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.style.AbsentExtensionElement;
import net.sf.saxon.style.ComponentDeclaration;
import net.sf.saxon.style.DataElement;
import net.sf.saxon.style.PackageVersion;
import net.sf.saxon.style.StyleElement;
import net.sf.saxon.style.XSLModuleRoot;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.trans.packages.VersionedPackageName;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.value.Whitespace;

public class XSLPackage
extends XSLModuleRoot {
    private String nameAtt = null;
    private PackageVersion packageVersion = null;
    private boolean declaredModes = true;
    private boolean prepared = false;

    @Override
    public void initialise(NodeName elemName, SchemaType elementType, AttributeMap atts, NodeInfo parent, int sequenceNumber) {
        super.initialise(elemName, elementType, atts, parent, sequenceNumber);
        this.declaredModes = this.getLocalPart().equals("package");
    }

    public String getName() {
        if (this.nameAtt == null) {
            this.prepareAttributes();
        }
        return this.nameAtt;
    }

    public int getVersion() {
        if (this.version == -1) {
            this.prepareAttributes();
        }
        return this.version;
    }

    public VersionedPackageName getNameAndVersion() {
        return new VersionedPackageName(this.getName(), this.getPackageVersion());
    }

    public PackageVersion getPackageVersion() {
        if (this.packageVersion == null) {
            this.prepareAttributes();
        }
        return this.packageVersion;
    }

    @Override
    protected void prepareAttributes() {
        if (this.prepared) {
            return;
        }
        this.prepared = true;
        String inputTypeAnnotationsAtt = null;
        String packageVersionAtt = null;
        for (AttributeInfo att : this.attributes()) {
            NodeName attName = att.getNodeName();
            String lexicalName = attName.getDisplayName();
            String value = att.getValue();
            if (lexicalName.equals("name") && this.getLocalPart().equals("package")) {
                this.nameAtt = Whitespace.trim(value);
                continue;
            }
            if (lexicalName.equals("id")) continue;
            if (lexicalName.equals("version")) {
                if (this.version != -1) continue;
                this.processVersionAttribute("");
                continue;
            }
            if (lexicalName.equals("package-version") && this.getLocalPart().equals("package")) {
                packageVersionAtt = Whitespace.trim(value);
                continue;
            }
            if (lexicalName.equals("declared-modes") && this.getLocalPart().equals("package")) {
                this.declaredModes = this.processBooleanAttribute("declared-modes", value);
                continue;
            }
            if (lexicalName.equals("input-type-annotations")) {
                inputTypeAnnotationsAtt = value;
                continue;
            }
            this.checkUnknownAttribute(attName);
        }
        if (packageVersionAtt == null) {
            this.packageVersion = PackageVersion.ONE;
        } else {
            try {
                this.packageVersion = new PackageVersion(packageVersionAtt);
            } catch (XPathException ex) {
                this.compileErrorInAttribute(ex.getMessage(), ex.getErrorCodeLocalPart(), "package-version");
            }
        }
        if (this.version == -1) {
            this.version = 30;
            this.reportAbsence("version");
        }
        if (inputTypeAnnotationsAtt != null) {
            switch (inputTypeAnnotationsAtt) {
                case "strip": {
                    break;
                }
                case "preserve": {
                    break;
                }
                case "unspecified": {
                    break;
                }
                default: {
                    this.compileError("Invalid value for input-type-annotations attribute. Permitted values are (strip, preserve, unspecified)", "XTSE0020");
                }
            }
        }
    }

    @Override
    public boolean isDeclaredModes() {
        if (this.nameAtt == null) {
            this.prepareAttributes();
        }
        return this.declaredModes;
    }

    @Override
    public void validate(ComponentDeclaration decl) throws XPathException {
        String defaultMode;
        for (NodeInfo nodeInfo : this.children()) {
            int fp = nodeInfo.getFingerprint();
            if (nodeInfo.getNodeKind() == 3 || nodeInfo instanceof StyleElement && ((StyleElement)nodeInfo).isDeclaration() || nodeInfo instanceof DataElement || !(nodeInfo instanceof StyleElement) || this.getLocalPart().equals("package") && (fp == 204 || fp == 152) || !"http://www.w3.org/1999/XSL/Transform".equals(nodeInfo.getURI()) && !"".equals(nodeInfo.getURI()) || nodeInfo instanceof AbsentExtensionElement && ((StyleElement)nodeInfo).forwardsCompatibleModeIsEnabled()) continue;
            if ("http://www.w3.org/1999/XSL/Transform".equals(nodeInfo.getURI())) {
                if (nodeInfo instanceof AbsentExtensionElement) continue;
                ((StyleElement)nodeInfo).compileError("Element " + nodeInfo.getDisplayName() + " must not appear directly within " + this.getDisplayName(), "XTSE0010");
                continue;
            }
            ((StyleElement)nodeInfo).compileError("Element " + nodeInfo.getDisplayName() + " must not appear directly within " + this.getDisplayName() + " because it is not in a namespace", "XTSE0130");
        }
        if (this.declaredModes && (defaultMode = this.getAttributeValue("default-mode")) != null && this.getPrincipalStylesheetModule().getRuleManager().obtainMode(this.getDefaultMode(), false) == null) {
            this.compileError("The default mode " + defaultMode + " has not been declared in an xsl:mode declaration", "XTSE3085");
        }
    }
}

