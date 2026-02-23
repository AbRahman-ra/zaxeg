/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.xml.security.utils.resolver.implementations;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xml.security.signature.XMLSignatureInput;
import org.apache.xml.security.utils.XMLUtils;
import org.apache.xml.security.utils.resolver.ResourceResolverContext;
import org.apache.xml.security.utils.resolver.ResourceResolverException;
import org.apache.xml.security.utils.resolver.ResourceResolverSpi;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class ResolverFragment
extends ResourceResolverSpi {
    private static Log log = LogFactory.getLog(ResolverFragment.class);

    public boolean engineIsThreadSafe() {
        return true;
    }

    public XMLSignatureInput engineResolveURI(ResourceResolverContext context) throws ResourceResolverException {
        Document doc = context.attr.getOwnerElement().getOwnerDocument();
        Node selectedElem = null;
        if (context.uriToResolve.equals("")) {
            if (log.isDebugEnabled()) {
                log.debug("ResolverFragment with empty URI (means complete document)");
            }
            selectedElem = doc;
        } else {
            Element start;
            String id = context.uriToResolve.substring(1);
            selectedElem = doc.getElementById(id);
            if (selectedElem == null) {
                Object[] exArgs = new Object[]{id};
                throw new ResourceResolverException("signature.Verification.MissingID", exArgs, context.attr, context.baseUri);
            }
            if (context.secureValidation && !XMLUtils.protectAgainstWrappingAttack(start = context.attr.getOwnerDocument().getDocumentElement(), id)) {
                Object[] exArgs = new Object[]{id};
                throw new ResourceResolverException("signature.Verification.MultipleIDs", exArgs, context.attr, context.baseUri);
            }
            if (log.isDebugEnabled()) {
                log.debug("Try to catch an Element with ID " + id + " and Element was " + selectedElem);
            }
        }
        XMLSignatureInput result = new XMLSignatureInput(selectedElem);
        result.setSecureValidation(context.secureValidation);
        result.setExcludeComments(true);
        result.setMIMEType("text/xml");
        if (context.baseUri != null && context.baseUri.length() > 0) {
            result.setSourceURI(context.baseUri.concat(context.uriToResolve));
        } else {
            result.setSourceURI(context.uriToResolve);
        }
        return result;
    }

    public boolean engineCanResolveURI(ResourceResolverContext context) {
        if (context.uriToResolve == null) {
            if (log.isDebugEnabled()) {
                log.debug("Quick fail for null uri");
            }
            return false;
        }
        if (context.uriToResolve.equals("") || context.uriToResolve.charAt(0) == '#' && !context.uriToResolve.startsWith("#xpointer(")) {
            if (log.isDebugEnabled()) {
                log.debug("State I can resolve reference: \"" + context.uriToResolve + "\"");
            }
            return true;
        }
        if (log.isDebugEnabled()) {
            log.debug("Do not seem to be able to resolve reference: \"" + context.uriToResolve + "\"");
        }
        return false;
    }
}

