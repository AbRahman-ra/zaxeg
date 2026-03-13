/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.trans.packages;

import java.io.File;
import javax.xml.transform.stream.StreamSource;
import net.sf.saxon.Configuration;
import net.sf.saxon.Version;
import net.sf.saxon.event.PipelineConfiguration;
import net.sf.saxon.event.ProxyReceiver;
import net.sf.saxon.event.Sender;
import net.sf.saxon.event.Sink;
import net.sf.saxon.lib.ParseOptions;
import net.sf.saxon.om.AttributeInfo;
import net.sf.saxon.om.AttributeMap;
import net.sf.saxon.om.NamespaceMap;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.trans.packages.PackageDetails;
import net.sf.saxon.trans.packages.VersionedPackageName;
import net.sf.saxon.type.SchemaType;

public class PackageInspector
extends ProxyReceiver {
    private boolean isSefFile;
    private String packageName;
    private String packageVersion = "1";
    private int elementCount = 0;

    private PackageInspector(PipelineConfiguration pipe) {
        super(new Sink(pipe));
    }

    @Override
    public void startElement(NodeName elemName, SchemaType type, AttributeMap attributes, NamespaceMap namespaces, Location location, int properties) throws XPathException {
        AttributeInfo saxonVersion;
        if (this.elementCount++ >= 1) {
            throw new XPathException("#start#");
        }
        this.isSefFile = elemName.hasURI("http://ns.saxonica.com/xslt/export");
        if (attributes.get("", "name") != null) {
            this.packageName = attributes.get("", "name").getValue();
        }
        if (attributes.get("", "package-version") != null) {
            this.packageVersion = attributes.get("", "package-version").getValue();
        }
        if (attributes.get("", "packageVersion") != null) {
            this.packageVersion = attributes.get("", "packageVersion").getValue();
        }
        if ((saxonVersion = attributes.get("", "saxonVersion")) != null && saxonVersion.getValue().startsWith("9")) {
            throw new XPathException("Saxon " + Version.getProductVersion() + " cannot load a SEF file created using version " + saxonVersion.getValue());
        }
    }

    private VersionedPackageName getNameAndVersion() {
        if (this.packageName == null) {
            return null;
        }
        try {
            return new VersionedPackageName(this.packageName, this.packageVersion);
        } catch (XPathException e) {
            return null;
        }
    }

    public static PackageDetails getPackageDetails(File top, Configuration config) throws XPathException {
        PackageInspector inspector;
        block5: {
            inspector = new PackageInspector(config.makePipelineConfiguration());
            try {
                ParseOptions options = new ParseOptions();
                options.setDTDValidationMode(4);
                options.setSchemaValidationMode(4);
                Sender.send(new StreamSource(top), inspector, new ParseOptions());
            } catch (XPathException e) {
                if (e.getMessage().equals("#start#")) break block5;
                throw e;
            }
        }
        VersionedPackageName vp = inspector.getNameAndVersion();
        if (vp == null) {
            return null;
        }
        PackageDetails details = new PackageDetails();
        details.nameAndVersion = vp;
        if (inspector.isSefFile) {
            details.exportLocation = new StreamSource(top);
        } else {
            details.sourceLocation = new StreamSource(top);
        }
        return details;
    }
}

