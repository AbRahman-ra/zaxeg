/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.trans;

import java.io.InputStream;
import java.util.HashMap;
import net.sf.saxon.Configuration;
import net.sf.saxon.Version;
import net.sf.saxon.lib.Logger;
import net.sf.saxon.serialize.MessageEmitter;
import net.sf.saxon.trans.XPathException;

public class DynamicLoader {
    private ClassLoader classLoader;
    protected HashMap<String, Class> knownClasses = new HashMap(20);

    public DynamicLoader() {
        this.registerKnownClasses();
    }

    protected void registerKnownClasses() {
        this.knownClasses.put("net.sf.saxon.serialize.MessageEmitter", MessageEmitter.class);
        this.knownClasses.put("net.sf.saxon.Configuration", Configuration.class);
    }

    public void setClassLoader(ClassLoader loader) {
        this.classLoader = loader;
    }

    public ClassLoader getClassLoader() {
        return this.classLoader;
    }

    public Class getClass(String className, Logger traceOut, ClassLoader classLoader) throws XPathException {
        boolean tracing;
        Class known = this.knownClasses.get(className);
        if (known != null) {
            return known;
        }
        boolean bl = tracing = traceOut != null;
        if (tracing) {
            traceOut.info("Loading " + className);
        }
        try {
            ClassLoader loader = classLoader;
            if (loader == null) {
                loader = this.classLoader;
            }
            if (loader == null) {
                loader = Thread.currentThread().getContextClassLoader();
            }
            if (loader != null) {
                try {
                    return loader.loadClass(className);
                } catch (Throwable ex) {
                    return Class.forName(className);
                }
            }
            return Class.forName(className);
        } catch (Throwable e) {
            if (tracing) {
                traceOut.error("The class " + className + " could not be loaded: " + e.getMessage());
            }
            throw new XPathException("Failed to load " + className + this.getMissingJarFileMessage(className), e);
        }
    }

    public Object getInstance(String className, ClassLoader classLoader) throws XPathException {
        Class theclass = this.getClass(className, null, classLoader);
        try {
            return theclass.newInstance();
        } catch (Exception err) {
            throw new XPathException("Failed to instantiate class " + className + " (does it have a public zero-argument constructor?)", err);
        }
    }

    public Object getInstance(String className, Logger traceOut, ClassLoader classLoader) throws XPathException {
        Class theclass = this.getClass(className, traceOut, classLoader);
        try {
            return theclass.newInstance();
        } catch (NoClassDefFoundError err) {
            throw new XPathException("Failed to load instance of class " + className + this.getMissingJarFileMessage(className), err);
        } catch (Exception err) {
            throw new XPathException("Failed to instantiate class " + className, err);
        }
    }

    private String getJarFileForClass(String className) {
        if (className.startsWith("net.sf.saxon.option.sql.")) {
            return "saxon-sql-" + Version.getProductVersion() + ".jar";
        }
        if (className.startsWith("com.ibm.icu.")) {
            return "icu4j-59.1.jar";
        }
        if (className.startsWith("com.saxonica")) {
            return "saxon-" + Version.softwareEdition.toLowerCase() + "-" + Version.getProductVersion() + ".jar";
        }
        return null;
    }

    private String getMissingJarFileMessage(String className) {
        String jar = this.getJarFileForClass(className);
        return jar == null ? "" : ". Check that " + jar + " is on the classpath";
    }

    public InputStream getResourceAsStream(String name) {
        ClassLoader loader = this.getClassLoader();
        if (loader == null) {
            loader = Thread.currentThread().getContextClassLoader();
        }
        return loader.getResourceAsStream(name);
    }
}

