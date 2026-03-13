/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.trans.packages;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.transform.Source;
import net.sf.saxon.Configuration;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.style.Compilation;
import net.sf.saxon.style.PackageVersion;
import net.sf.saxon.style.PackageVersionRanges;
import net.sf.saxon.style.PrincipalStylesheetModule;
import net.sf.saxon.style.StylesheetPackage;
import net.sf.saxon.trans.CompilerInfo;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.trans.packages.IPackageLoader;
import net.sf.saxon.trans.packages.PackageDetails;
import net.sf.saxon.trans.packages.PackageInspector;
import net.sf.saxon.trans.packages.VersionedPackageName;
import net.sf.saxon.tree.util.FastStringBuffer;

public class PackageLibrary {
    private final Configuration config;
    private final CompilerInfo compilerInfo;
    private Map<String, List<PackageVersion>> packageVersions = new HashMap<String, List<PackageVersion>>();
    private Map<VersionedPackageName, PackageDetails> packages = new HashMap<VersionedPackageName, PackageDetails>();

    public PackageLibrary(CompilerInfo compilerInfo) {
        this.compilerInfo = compilerInfo;
        this.config = compilerInfo.getConfiguration();
    }

    public CompilerInfo getCompilerInfo() {
        return this.compilerInfo;
    }

    public PackageLibrary(PackageLibrary library) {
        this.packageVersions = new HashMap<String, List<PackageVersion>>(library.packageVersions);
        this.packages = new HashMap<VersionedPackageName, PackageDetails>(library.packages);
        this.compilerInfo = library.compilerInfo;
        this.config = library.config;
    }

    public PackageLibrary(CompilerInfo info, Set<File> files) throws XPathException {
        this.compilerInfo = info;
        this.config = info.getConfiguration();
        for (File file : files) {
            PackageDetails details = PackageInspector.getPackageDetails(file, this.config);
            if (details == null) {
                throw new XPathException("Unable to get package name and version for file " + file.getName());
            }
            this.addPackage(details);
        }
    }

    public synchronized void addPackage(StylesheetPackage packageIn) {
        String name = packageIn.getPackageName();
        PackageVersion version = packageIn.getPackageVersion();
        VersionedPackageName vp = new VersionedPackageName(name, version);
        PackageDetails details = new PackageDetails();
        details.nameAndVersion = vp;
        details.loadedPackage = packageIn;
        if (vp.packageName != null) {
            this.packages.put(vp, details);
            this.addPackage(details);
        }
    }

    public synchronized void addPackage(PackageDetails details) {
        VersionedPackageName vp = details.nameAndVersion;
        String name = vp.packageName;
        PackageVersion version = vp.packageVersion;
        List versions = this.packageVersions.computeIfAbsent(name, k -> new ArrayList());
        versions.add(version);
        this.packages.put(vp, details);
    }

    public void addPackage(File file) throws XPathException {
        PackageDetails details = PackageInspector.getPackageDetails(file, this.config);
        if (details == null) {
            throw new XPathException("Unable to get package name and version for file " + file.getName());
        }
        this.addPackage(details);
    }

    public synchronized PackageDetails findPackage(String name, PackageVersionRanges ranges) {
        HashSet<PackageDetails> candidates = new HashSet<PackageDetails>();
        List<PackageVersion> available = this.packageVersions.get(name);
        if (available == null) {
            return null;
        }
        int maxPriority = Integer.MIN_VALUE;
        for (PackageVersion pv : available) {
            PackageDetails details = this.packages.get(new VersionedPackageName(name, pv));
            if (!ranges.contains(pv)) continue;
            candidates.add(details);
            Integer priority = details.priority;
            if (priority == null || priority <= maxPriority) continue;
            maxPriority = priority;
        }
        if (candidates.isEmpty()) {
            return null;
        }
        if (candidates.size() == 1) {
            return (PackageDetails)candidates.iterator().next();
        }
        HashSet shortList = new HashSet();
        PackageDetails highest = null;
        if (maxPriority == Integer.MIN_VALUE) {
            for (PackageDetails details : candidates) {
                if (highest != null && details.nameAndVersion.packageVersion.compareTo(highest.nameAndVersion.packageVersion) <= 0) continue;
                highest = details;
            }
        } else {
            for (PackageDetails details : candidates) {
                Integer priority = details.priority;
                PackageVersion pv = details.nameAndVersion.packageVersion;
                if (priority == null || priority != maxPriority || highest != null && pv.compareTo(highest.nameAndVersion.packageVersion) <= 0) continue;
                highest = details;
            }
        }
        return highest;
    }

    public synchronized PackageDetails findDetailsForAlias(String shortName) {
        assert (shortName != null);
        PackageDetails selected = null;
        for (PackageDetails details : this.packages.values()) {
            if (!shortName.equals(details.shortName)) continue;
            if (selected == null) {
                selected = details;
                continue;
            }
            throw new IllegalStateException("Non-unique shortName in package library: " + shortName);
        }
        return selected;
    }

    public StylesheetPackage obtainLoadedPackage(PackageDetails details, List<VersionedPackageName> disallowed) throws XPathException {
        if (details.loadedPackage != null) {
            return details.loadedPackage;
        }
        if (details.exportLocation != null) {
            this.testForCycles(details, disallowed);
            details.beingProcessed = Thread.currentThread();
            Source input = details.exportLocation;
            IPackageLoader loader = this.config.makePackageLoader();
            StylesheetPackage pack = loader.loadPackage(input);
            this.checkNameAndVersion(pack, details);
            details.loadedPackage = pack;
            details.beingProcessed = null;
            return pack;
        }
        if (details.sourceLocation != null) {
            this.testForCycles(details, disallowed);
            details.beingProcessed = Thread.currentThread();
            Compilation compilation = new Compilation(this.config, this.compilerInfo);
            compilation.setUsingPackages(disallowed);
            compilation.setExpectedNameAndVersion(details.nameAndVersion);
            compilation.clearParameters();
            compilation.setLibraryPackage(true);
            if (details.staticParams != null) {
                for (Map.Entry<StructuredQName, GroundedValue> entry : details.staticParams.entrySet()) {
                    compilation.setParameter(entry.getKey(), entry.getValue());
                }
            }
            PrincipalStylesheetModule psm = compilation.compilePackage(details.sourceLocation);
            details.beingProcessed = null;
            if (compilation.getErrorCount() > 0) {
                throw new XPathException("Errors found in package " + details.nameAndVersion.packageName);
            }
            StylesheetPackage styPack = psm.getStylesheetPackage();
            this.checkNameAndVersion(styPack, details);
            details.loadedPackage = styPack;
            return styPack;
        }
        return null;
    }

    private void testForCycles(PackageDetails details, List<VersionedPackageName> disallowed) throws XPathException {
        if (details.beingProcessed == Thread.currentThread()) {
            FastStringBuffer buffer = new FastStringBuffer(1024);
            for (VersionedPackageName n : disallowed) {
                buffer.append(n.packageName);
                buffer.append(", ");
            }
            buffer.append("and ");
            buffer.append(details.nameAndVersion.packageName);
            throw new XPathException("There is a cycle of package dependencies involving " + buffer, "XTSE3005");
        }
    }

    private void checkNameAndVersion(StylesheetPackage pack, PackageDetails details) throws XPathException {
        PackageVersion actualVersion;
        String storedName = pack.getPackageName();
        if (details.baseName != null) {
            if (!details.baseName.equals(storedName)) {
                throw new XPathException("Base name of package (" + details.baseName + ") does not match the value in the XSLT source (" + storedName + ")");
            }
        } else if (!details.nameAndVersion.packageName.equals(storedName)) {
            throw new XPathException("Registered name of package (" + details.nameAndVersion.packageName + ") does not match the value in the XSLT source (" + storedName + ")");
        }
        if (!(actualVersion = pack.getPackageVersion()).equals(details.nameAndVersion.packageVersion)) {
            throw new XPathException("Registered version number of package (" + details.nameAndVersion.packageVersion + ") does not match the value in the XSLT source (" + actualVersion + ")");
        }
    }

    public synchronized List<StylesheetPackage> getPackages() {
        ArrayList<StylesheetPackage> result = new ArrayList<StylesheetPackage>();
        for (PackageDetails details : this.packages.values()) {
            if (details.loadedPackage == null) continue;
            result.add(details.loadedPackage);
        }
        return result;
    }
}

