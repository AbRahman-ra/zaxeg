/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.type;

import net.sf.saxon.om.AbsolutePath;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.util.Navigator;
import net.sf.saxon.type.ValidationFailure;

public class ValidationException
extends XPathException {
    private ValidationFailure failure;

    public ValidationException(Exception exception) {
        super(exception);
    }

    public ValidationException(String message, Exception exception) {
        super(message, exception);
    }

    public ValidationException(String message, Location locator) {
        super(message, null, locator);
    }

    public ValidationException(ValidationFailure failure) {
        super(failure.getMessage(), failure.getErrorCode(), failure.getLocator());
        this.failure = failure;
    }

    @Override
    public String getMessage() {
        if (this.failure != null) {
            return this.failure.getMessage();
        }
        return super.getMessage();
    }

    public ValidationFailure getValidationFailure() {
        if (this.failure != null) {
            return this.failure;
        }
        ValidationFailure failure = new ValidationFailure(this.getMessage());
        failure.setErrorCodeQName(this.getErrorCodeQName());
        failure.setLocator(this.getLocator());
        return failure;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("ValidationException: ");
        String message = this.getMessage();
        if (message != null) {
            sb.append(message);
        }
        return sb.toString();
    }

    public NodeInfo getNode() {
        if (this.failure != null) {
            return this.failure.getInvalidNode();
        }
        return null;
    }

    public String getPath() {
        AbsolutePath ap = this.getAbsolutePath();
        if (ap == null) {
            NodeInfo node = this.getNode();
            if (node != null) {
                return Navigator.getPath(node);
            }
            return null;
        }
        return ap.getPathUsingAbbreviatedUris();
    }

    public AbsolutePath getAbsolutePath() {
        if (this.failure != null) {
            return this.failure.getPath();
        }
        return null;
    }
}

