/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.xml.security.utils.resolver;

import org.w3c.dom.Attr;

public class ResourceResolverContext {
    public final String uriToResolve;
    public final boolean secureValidation;
    public final String baseUri;
    public final Attr attr;

    public ResourceResolverContext(Attr attr, String baseUri, boolean secureValidation) {
        this.attr = attr;
        this.baseUri = baseUri;
        this.secureValidation = secureValidation;
        this.uriToResolve = attr != null ? attr.getValue() : null;
    }
}

