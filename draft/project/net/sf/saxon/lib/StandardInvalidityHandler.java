/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.lib;

import javax.xml.transform.dom.DOMLocator;
import net.sf.saxon.Configuration;
import net.sf.saxon.lib.Invalidity;
import net.sf.saxon.lib.InvalidityHandler;
import net.sf.saxon.lib.Logger;
import net.sf.saxon.lib.StandardDiagnostics;
import net.sf.saxon.om.AbsolutePath;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.util.Navigator;
import net.sf.saxon.type.ValidationFailure;
import net.sf.saxon.value.EmptySequence;

public class StandardInvalidityHandler
extends StandardDiagnostics
implements InvalidityHandler {
    private Configuration config;
    private Logger logger;

    public StandardInvalidityHandler(Configuration config) {
        this.config = config;
        this.logger = config.getLogger();
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    public Logger getLogger() {
        return this.logger;
    }

    public Configuration getConfiguration() {
        return this.config;
    }

    @Override
    public void startReporting(String systemId) throws XPathException {
    }

    @Override
    public void reportInvalidity(Invalidity failure) throws XPathException {
        Logger localLogger = this.logger;
        if (localLogger == null) {
            localLogger = this.config.getLogger();
        }
        String explanation = this.getExpandedMessage(failure);
        String constraintReference = this.getConstraintReferenceMessage(failure);
        String validationLocation = ((ValidationFailure)failure).getValidationLocationText();
        String contextLocation = ((ValidationFailure)failure).getContextLocationText();
        String finalMessage = "Validation error " + this.getLocationMessage(failure) + "\n  " + this.wordWrap(explanation) + this.wordWrap(contextLocation.isEmpty() ? "" : "\n  " + contextLocation) + this.wordWrap(constraintReference == null ? "" : "\n  " + constraintReference) + this.formatListOfOffendingNodes((ValidationFailure)failure);
        localLogger.error(finalMessage);
    }

    public String getLocationMessage(Invalidity err) {
        boolean containsLineNumber;
        AbsolutePath path;
        String locMessage = "";
        String systemId = null;
        NodeInfo node = err.getInvalidNode();
        String nodeMessage = null;
        int lineNumber = err.getLineNumber();
        if (err instanceof DOMLocator) {
            nodeMessage = "at " + ((DOMLocator)((Object)err)).getOriginatingNode().getNodeName() + ' ';
        } else if (lineNumber == -1 && (path = err.getPath()) != null) {
            nodeMessage = "at " + path + ' ';
        } else if (node != null) {
            nodeMessage = "at " + Navigator.getPath(node) + ' ';
        }
        boolean bl = containsLineNumber = lineNumber != -1;
        if (nodeMessage != null) {
            locMessage = locMessage + nodeMessage;
        }
        if (containsLineNumber) {
            locMessage = locMessage + "on line " + lineNumber + ' ';
            if (err.getColumnNumber() != -1) {
                locMessage = locMessage + "column " + err.getColumnNumber() + ' ';
            }
        }
        if ((systemId = err.getSystemId()) != null && systemId.length() != 0) {
            locMessage = locMessage + (containsLineNumber ? "of " : "in ") + this.abbreviateLocationURI(systemId) + ':';
        }
        return locMessage;
    }

    public String getExpandedMessage(Invalidity err) {
        String code = err.getErrorCode();
        return (code == null ? "" : code + ": ") + err.getMessage();
    }

    public String getConstraintReferenceMessage(Invalidity err) {
        if (err.getSchemaPart() == -1) {
            return null;
        }
        return "See http://www.w3.org/TR/xmlschema-" + err.getSchemaPart() + "/#" + err.getConstraintName() + " clause " + err.getConstraintClauseNumber();
    }

    @Override
    public Sequence endReporting() throws XPathException {
        return EmptySequence.getInstance();
    }
}

