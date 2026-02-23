/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.s9api;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import net.sf.saxon.Configuration;
import net.sf.saxon.PreparedStylesheet;
import net.sf.saxon.Query;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.WhitespaceStrippingPolicy;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.style.Compilation;
import net.sf.saxon.style.PackageVersion;
import net.sf.saxon.style.StylesheetPackage;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.CompilerInfo;
import net.sf.saxon.trans.XPathException;

public class XsltPackage {
    private XsltCompiler compiler;
    private StylesheetPackage stylesheetPackage;

    protected XsltPackage(XsltCompiler compiler, StylesheetPackage pp) {
        this.compiler = compiler;
        this.stylesheetPackage = pp;
    }

    public Processor getProcessor() {
        return this.compiler.getProcessor();
    }

    public String getName() {
        return this.stylesheetPackage.getPackageName();
    }

    public String getVersion() {
        return this.stylesheetPackage.getPackageVersion().toString();
    }

    public PackageVersion getPackageVersion() {
        return this.stylesheetPackage.getPackageVersion();
    }

    public WhitespaceStrippingPolicy getWhitespaceStrippingPolicy() {
        return new WhitespaceStrippingPolicy(this.stylesheetPackage);
    }

    public XsltExecutable link() throws SaxonApiException {
        try {
            Configuration config = this.getProcessor().getUnderlyingConfiguration();
            CompilerInfo info = this.compiler.getUnderlyingCompilerInfo();
            Compilation compilation = new Compilation(config, info);
            compilation.setPackageData(this.stylesheetPackage);
            this.stylesheetPackage.checkForAbstractComponents();
            PreparedStylesheet pss = new PreparedStylesheet(compilation);
            this.stylesheetPackage.updatePreparedStylesheet(pss);
            pss.addPackage(this.stylesheetPackage);
            return new XsltExecutable(this.getProcessor(), pss);
        } catch (XPathException e) {
            throw new SaxonApiException(e);
        }
    }

    public void save(File file) throws SaxonApiException {
        String target = this.stylesheetPackage.getTargetEdition();
        if (target == null) {
            target = this.getProcessor().getSaxonEdition();
        }
        this.save(file, target);
    }

    public void save(File file, String target) throws SaxonApiException {
        try {
            Query.createFileIfNecessary(file);
            ExpressionPresenter presenter = this.getProcessor().getUnderlyingConfiguration().newExpressionExporter(target, new FileOutputStream(file), this.stylesheetPackage);
            presenter.setRelocatable(this.stylesheetPackage.isRelocatable());
            this.stylesheetPackage.export(presenter);
        } catch (IOException | XPathException e) {
            throw new SaxonApiException(e);
        }
    }

    public StylesheetPackage getUnderlyingPreparedPackage() {
        return this.stylesheetPackage;
    }
}

