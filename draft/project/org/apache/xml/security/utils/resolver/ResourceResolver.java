/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.xml.security.utils.resolver;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xml.security.signature.XMLSignatureInput;
import org.apache.xml.security.utils.ClassLoaderUtils;
import org.apache.xml.security.utils.JavaUtils;
import org.apache.xml.security.utils.resolver.ResourceResolverContext;
import org.apache.xml.security.utils.resolver.ResourceResolverException;
import org.apache.xml.security.utils.resolver.ResourceResolverSpi;
import org.apache.xml.security.utils.resolver.implementations.ResolverDirectHTTP;
import org.apache.xml.security.utils.resolver.implementations.ResolverFragment;
import org.apache.xml.security.utils.resolver.implementations.ResolverLocalFilesystem;
import org.apache.xml.security.utils.resolver.implementations.ResolverXPointer;
import org.w3c.dom.Attr;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class ResourceResolver {
    private static Log log = LogFactory.getLog(ResourceResolver.class);
    private static List<ResourceResolver> resolverList = new ArrayList<ResourceResolver>();
    private final ResourceResolverSpi resolverSpi;

    public ResourceResolver(ResourceResolverSpi resourceResolver) {
        this.resolverSpi = resourceResolver;
    }

    public static final ResourceResolver getInstance(Attr uri, String baseURI) throws ResourceResolverException {
        return ResourceResolver.getInstance(uri, baseURI, false);
    }

    public static final ResourceResolver getInstance(Attr uriAttr, String baseURI, boolean secureValidation) throws ResourceResolverException {
        ResourceResolverContext context = new ResourceResolverContext(uriAttr, baseURI, secureValidation);
        return ResourceResolver.internalGetInstance(context);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static <N> ResourceResolver internalGetInstance(ResourceResolverContext context) throws ResourceResolverException {
        List<ResourceResolver> list = resolverList;
        synchronized (list) {
            Iterator<ResourceResolver> i$ = resolverList.iterator();
            while (i$.hasNext()) {
                ResourceResolver resolver;
                ResourceResolver resolverTmp = resolver = i$.next();
                if (!resolver.resolverSpi.engineIsThreadSafe()) {
                    try {
                        resolverTmp = new ResourceResolver((ResourceResolverSpi)resolver.resolverSpi.getClass().newInstance());
                    } catch (InstantiationException e) {
                        throw new ResourceResolverException("", e, context.attr, context.baseUri);
                    } catch (IllegalAccessException e) {
                        throw new ResourceResolverException("", e, context.attr, context.baseUri);
                    }
                }
                if (log.isDebugEnabled()) {
                    log.debug("check resolvability by class " + resolverTmp.getClass().getName());
                }
                if (resolverTmp == null || !resolverTmp.canResolve(context)) continue;
                if (context.secureValidation && (resolverTmp.resolverSpi instanceof ResolverLocalFilesystem || resolverTmp.resolverSpi instanceof ResolverDirectHTTP)) {
                    Object[] exArgs = new Object[]{resolverTmp.resolverSpi.getClass().getName()};
                    throw new ResourceResolverException("signature.Reference.ForbiddenResolver", exArgs, context.attr, context.baseUri);
                }
                return resolverTmp;
            }
        }
        Object[] exArgs = new Object[]{context.uriToResolve != null ? context.uriToResolve : "null", context.baseUri};
        throw new ResourceResolverException("utils.resolver.noClass", exArgs, context.attr, context.baseUri);
    }

    public static ResourceResolver getInstance(Attr uri, String baseURI, List<ResourceResolver> individualResolvers) throws ResourceResolverException {
        return ResourceResolver.getInstance(uri, baseURI, individualResolvers, false);
    }

    public static ResourceResolver getInstance(Attr uri, String baseURI, List<ResourceResolver> individualResolvers, boolean secureValidation) throws ResourceResolverException {
        if (log.isDebugEnabled()) {
            log.debug("I was asked to create a ResourceResolver and got " + (individualResolvers == null ? 0 : individualResolvers.size()));
        }
        ResourceResolverContext context = new ResourceResolverContext(uri, baseURI, secureValidation);
        if (individualResolvers != null) {
            for (int i = 0; i < individualResolvers.size(); ++i) {
                ResourceResolver resolver = individualResolvers.get(i);
                if (resolver == null) continue;
                if (log.isDebugEnabled()) {
                    String currentClass = resolver.resolverSpi.getClass().getName();
                    log.debug("check resolvability by class " + currentClass);
                }
                if (!resolver.canResolve(context)) continue;
                return resolver;
            }
        }
        return ResourceResolver.internalGetInstance(context);
    }

    public static void register(String className) {
        JavaUtils.checkRegisterPermission();
        try {
            Class<?> resourceResolverClass = ClassLoaderUtils.loadClass(className, ResourceResolver.class);
            ResourceResolver.register(resourceResolverClass, false);
        } catch (ClassNotFoundException e) {
            log.warn("Error loading resolver " + className + " disabling it");
        }
    }

    public static void registerAtStart(String className) {
        JavaUtils.checkRegisterPermission();
        try {
            Class<?> resourceResolverClass = ClassLoaderUtils.loadClass(className, ResourceResolver.class);
            ResourceResolver.register(resourceResolverClass, true);
        } catch (ClassNotFoundException e) {
            log.warn("Error loading resolver " + className + " disabling it");
        }
    }

    public static void register(Class<? extends ResourceResolverSpi> className, boolean start) {
        JavaUtils.checkRegisterPermission();
        try {
            ResourceResolverSpi resourceResolverSpi = className.newInstance();
            ResourceResolver.register(resourceResolverSpi, start);
        } catch (IllegalAccessException e) {
            log.warn("Error loading resolver " + className + " disabling it");
        } catch (InstantiationException e) {
            log.warn("Error loading resolver " + className + " disabling it");
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void register(ResourceResolverSpi resourceResolverSpi, boolean start) {
        JavaUtils.checkRegisterPermission();
        List<ResourceResolver> list = resolverList;
        synchronized (list) {
            if (start) {
                resolverList.add(0, new ResourceResolver(resourceResolverSpi));
            } else {
                resolverList.add(new ResourceResolver(resourceResolverSpi));
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("Registered resolver: " + resourceResolverSpi.toString());
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void registerDefaultResolvers() {
        List<ResourceResolver> list = resolverList;
        synchronized (list) {
            resolverList.add(new ResourceResolver(new ResolverFragment()));
            resolverList.add(new ResourceResolver(new ResolverLocalFilesystem()));
            resolverList.add(new ResourceResolver(new ResolverXPointer()));
            resolverList.add(new ResourceResolver(new ResolverDirectHTTP()));
        }
    }

    public XMLSignatureInput resolve(Attr uri, String baseURI) throws ResourceResolverException {
        return this.resolve(uri, baseURI, true);
    }

    public XMLSignatureInput resolve(Attr uri, String baseURI, boolean secureValidation) throws ResourceResolverException {
        ResourceResolverContext context = new ResourceResolverContext(uri, baseURI, secureValidation);
        return this.resolverSpi.engineResolveURI(context);
    }

    public void setProperty(String key, String value) {
        this.resolverSpi.engineSetProperty(key, value);
    }

    public String getProperty(String key) {
        return this.resolverSpi.engineGetProperty(key);
    }

    public void addProperties(Map<String, String> properties) {
        this.resolverSpi.engineAddProperies(properties);
    }

    public String[] getPropertyKeys() {
        return this.resolverSpi.engineGetPropertyKeys();
    }

    public boolean understandsProperty(String propertyToTest) {
        return this.resolverSpi.understandsProperty(propertyToTest);
    }

    private boolean canResolve(ResourceResolverContext context) {
        return this.resolverSpi.engineCanResolveURI(context);
    }
}

