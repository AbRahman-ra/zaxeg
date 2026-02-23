/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.xml.security.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public final class ClassLoaderUtils {
    private static final Log log = LogFactory.getLog(ClassLoaderUtils.class);

    private ClassLoaderUtils() {
    }

    public static URL getResource(String resourceName, Class<?> callingClass) {
        ClassLoader cl;
        ClassLoader cluClassloader;
        URL url = Thread.currentThread().getContextClassLoader().getResource(resourceName);
        if (url == null && resourceName.startsWith("/")) {
            url = Thread.currentThread().getContextClassLoader().getResource(resourceName.substring(1));
        }
        if ((cluClassloader = ClassLoaderUtils.class.getClassLoader()) == null) {
            cluClassloader = ClassLoader.getSystemClassLoader();
        }
        if (url == null) {
            url = cluClassloader.getResource(resourceName);
        }
        if (url == null && resourceName.startsWith("/")) {
            url = cluClassloader.getResource(resourceName.substring(1));
        }
        if (url == null && (cl = callingClass.getClassLoader()) != null) {
            url = cl.getResource(resourceName);
        }
        if (url == null) {
            url = callingClass.getResource(resourceName);
        }
        if (url == null && resourceName != null && resourceName.charAt(0) != '/') {
            return ClassLoaderUtils.getResource('/' + resourceName, callingClass);
        }
        return url;
    }

    public static List<URL> getResources(String resourceName, Class<?> callingClass) {
        URL url;
        Enumeration<URL> urls;
        ArrayList<URL> ret;
        block22: {
            ClassLoader cl;
            block21: {
                ClassLoader cluClassloader;
                block20: {
                    block19: {
                        block18: {
                            ret = new ArrayList<URL>();
                            urls = new Enumeration<URL>(){

                                @Override
                                public boolean hasMoreElements() {
                                    return false;
                                }

                                @Override
                                public URL nextElement() {
                                    return null;
                                }
                            };
                            try {
                                urls = Thread.currentThread().getContextClassLoader().getResources(resourceName);
                            } catch (IOException e) {
                                if (!log.isDebugEnabled()) break block18;
                                log.debug(e);
                            }
                        }
                        if (!urls.hasMoreElements() && resourceName.startsWith("/")) {
                            try {
                                urls = Thread.currentThread().getContextClassLoader().getResources(resourceName.substring(1));
                            } catch (IOException e) {
                                if (!log.isDebugEnabled()) break block19;
                                log.debug(e);
                            }
                        }
                    }
                    if ((cluClassloader = ClassLoaderUtils.class.getClassLoader()) == null) {
                        cluClassloader = ClassLoader.getSystemClassLoader();
                    }
                    if (!urls.hasMoreElements()) {
                        try {
                            urls = cluClassloader.getResources(resourceName);
                        } catch (IOException e) {
                            if (!log.isDebugEnabled()) break block20;
                            log.debug(e);
                        }
                    }
                }
                if (!urls.hasMoreElements() && resourceName.startsWith("/")) {
                    try {
                        urls = cluClassloader.getResources(resourceName.substring(1));
                    } catch (IOException e) {
                        if (!log.isDebugEnabled()) break block21;
                        log.debug(e);
                    }
                }
            }
            if (!urls.hasMoreElements() && (cl = callingClass.getClassLoader()) != null) {
                try {
                    urls = cl.getResources(resourceName);
                } catch (IOException e) {
                    if (!log.isDebugEnabled()) break block22;
                    log.debug(e);
                }
            }
        }
        if (!urls.hasMoreElements() && (url = callingClass.getResource(resourceName)) != null) {
            ret.add(url);
        }
        while (urls.hasMoreElements()) {
            ret.add(urls.nextElement());
        }
        if (ret.isEmpty() && resourceName != null && resourceName.charAt(0) != '/') {
            return ClassLoaderUtils.getResources('/' + resourceName, callingClass);
        }
        return ret;
    }

    public static InputStream getResourceAsStream(String resourceName, Class<?> callingClass) {
        URL url = ClassLoaderUtils.getResource(resourceName, callingClass);
        try {
            return url != null ? url.openStream() : null;
        } catch (IOException e) {
            if (log.isDebugEnabled()) {
                log.debug(e);
            }
            return null;
        }
    }

    public static Class<?> loadClass(String className, Class<?> callingClass) throws ClassNotFoundException {
        block3: {
            try {
                ClassLoader cl = Thread.currentThread().getContextClassLoader();
                if (cl != null) {
                    return cl.loadClass(className);
                }
            } catch (ClassNotFoundException e) {
                if (!log.isDebugEnabled()) break block3;
                log.debug(e);
            }
        }
        return ClassLoaderUtils.loadClass2(className, callingClass);
    }

    private static Class<?> loadClass2(String className, Class<?> callingClass) throws ClassNotFoundException {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException ex) {
            block6: {
                try {
                    if (ClassLoaderUtils.class.getClassLoader() != null) {
                        return ClassLoaderUtils.class.getClassLoader().loadClass(className);
                    }
                } catch (ClassNotFoundException exc) {
                    if (callingClass == null || callingClass.getClassLoader() == null) break block6;
                    return callingClass.getClassLoader().loadClass(className);
                }
            }
            if (log.isDebugEnabled()) {
                log.debug(ex);
            }
            throw ex;
        }
    }
}

