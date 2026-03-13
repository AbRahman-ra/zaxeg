/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.xml.security.utils.resolver.implementations;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import org.apache.xml.security.signature.XMLSignatureInput;
import org.apache.xml.security.utils.resolver.ResourceResolverContext;
import org.apache.xml.security.utils.resolver.ResourceResolverSpi;

public class ResolverAnonymous
extends ResourceResolverSpi {
    private InputStream inStream = null;

    public boolean engineIsThreadSafe() {
        return true;
    }

    public ResolverAnonymous(String filename) throws FileNotFoundException, IOException {
        this.inStream = new FileInputStream(filename);
    }

    public ResolverAnonymous(InputStream is) {
        this.inStream = is;
    }

    public XMLSignatureInput engineResolveURI(ResourceResolverContext context) {
        XMLSignatureInput input = new XMLSignatureInput(this.inStream);
        input.setSecureValidation(context.secureValidation);
        return input;
    }

    public boolean engineCanResolveURI(ResourceResolverContext context) {
        return context.uriToResolve == null;
    }

    public String[] engineGetPropertyKeys() {
        return new String[0];
    }
}

