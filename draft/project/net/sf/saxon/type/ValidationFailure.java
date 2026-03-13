/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.type;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.transform.SourceLocator;
import net.sf.saxon.expr.parser.Loc;
import net.sf.saxon.lib.Invalidity;
import net.sf.saxon.om.AbsolutePath;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.type.ConversionResult;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.type.ValidationException;
import net.sf.saxon.value.AtomicValue;

public class ValidationFailure
implements Location,
ConversionResult,
Invalidity {
    private String message;
    private String systemId;
    private String publicId;
    private int lineNumber = -1;
    private int columnNumber = -1;
    private AbsolutePath path;
    private AbsolutePath contextPath;
    private NodeInfo invalidNode;
    private List<NodeInfo> offendingNodes;
    private int schemaPart = -1;
    private String constraintName;
    private String clause;
    private SchemaType schemaType;
    private StructuredQName errorCode;
    private ValidationException exception;
    private boolean hasBeenReported;

    public ValidationFailure(String message) {
        this.message = message;
        this.setErrorCode("FORG0001");
    }

    public static ValidationFailure fromException(Exception exception) {
        if (exception instanceof ValidationException) {
            return ((ValidationException)exception).getValidationFailure();
        }
        if (exception instanceof XPathException) {
            ValidationFailure failure = new ValidationFailure(exception.getMessage());
            if (((XPathException)exception).getErrorCodeQName() == null) {
                failure.setErrorCode("FORG0001");
            } else {
                failure.setErrorCodeQName(((XPathException)exception).getErrorCodeQName());
            }
            failure.setLocator(((XPathException)exception).getLocator());
            return failure;
        }
        return new ValidationFailure(exception.getMessage());
    }

    public void setConstraintReference(int schemaPart, String constraintName, String clause) {
        this.schemaPart = schemaPart;
        this.constraintName = constraintName;
        this.clause = clause;
    }

    public void setConstraintReference(ValidationFailure e) {
        this.schemaPart = e.schemaPart;
        this.constraintName = e.constraintName;
        this.clause = e.clause;
    }

    @Override
    public int getSchemaPart() {
        return this.schemaPart;
    }

    @Override
    public String getConstraintName() {
        return this.constraintName;
    }

    @Override
    public String getConstraintClauseNumber() {
        return this.clause;
    }

    @Override
    public String getConstraintReference() {
        return this.constraintName + '.' + this.clause;
    }

    public String getConstraintReferenceMessage() {
        if (this.schemaPart == -1) {
            return null;
        }
        return "See http://www.w3.org/TR/xmlschema11-" + this.schemaPart + "/#" + this.constraintName + " clause " + this.clause;
    }

    public void addOffendingNode(NodeInfo node) {
        if (this.offendingNodes == null) {
            this.offendingNodes = new ArrayList<NodeInfo>();
        }
        this.offendingNodes.add(node);
    }

    public List<NodeInfo> getOffendingNodes() {
        if (this.offendingNodes == null) {
            return Collections.emptyList();
        }
        return this.offendingNodes;
    }

    @Override
    public AbsolutePath getPath() {
        return this.path;
    }

    public void setPath(AbsolutePath path) {
        this.path = path;
    }

    @Override
    public AbsolutePath getContextPath() {
        return this.contextPath;
    }

    public void setContextPath(AbsolutePath contextPath) {
        this.contextPath = contextPath;
    }

    @Override
    public NodeInfo getInvalidNode() {
        return this.invalidNode;
    }

    public void setInvalidNode(NodeInfo invalidNode) {
        this.invalidNode = invalidNode;
    }

    @Override
    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String toString() {
        FastStringBuffer sb = new FastStringBuffer("ValidationException: ");
        String message = this.getMessage();
        if (message != null) {
            sb.append(message);
        }
        return sb.toString();
    }

    @Override
    public String getPublicId() {
        Location loc = this.getLocator();
        if (this.publicId == null && loc != null && loc != this) {
            return loc.getPublicId();
        }
        return this.publicId;
    }

    @Override
    public String getSystemId() {
        Location loc = this.getLocator();
        if (this.systemId == null && loc != null && loc != this) {
            return loc.getSystemId();
        }
        return this.systemId;
    }

    @Override
    public int getLineNumber() {
        Location loc = this.getLocator();
        if (this.lineNumber == -1 && loc != null && loc != this) {
            return loc.getLineNumber();
        }
        return this.lineNumber;
    }

    @Override
    public int getColumnNumber() {
        Location loc = this.getLocator();
        if (this.columnNumber == -1 && loc != null && loc != this) {
            return loc.getColumnNumber();
        }
        return this.columnNumber;
    }

    @Override
    public Location saveLocation() {
        return new Loc(this);
    }

    public void setPublicId(String id) {
        this.publicId = id;
    }

    public void setSystemId(String id) {
        this.systemId = id;
    }

    public void setLineNumber(int line) {
        this.lineNumber = line;
    }

    public void setColumnNumber(int column) {
        this.columnNumber = column;
    }

    public void setLocator(SourceLocator locator) {
        if (locator != null) {
            this.setPublicId(locator.getPublicId());
            this.setSystemId(locator.getSystemId());
            this.setLineNumber(locator.getLineNumber());
            this.setColumnNumber(locator.getColumnNumber());
        }
    }

    public void setSourceLocator(SourceLocator locator) {
        if (locator != null) {
            this.setPublicId(locator.getPublicId());
            this.setSystemId(locator.getSystemId());
            this.setLineNumber(locator.getLineNumber());
            this.setColumnNumber(locator.getColumnNumber());
        }
    }

    public Location getLocator() {
        return this;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode == null ? null : new StructuredQName("err", "http://www.w3.org/2005/xqt-errors", errorCode);
    }

    public void setErrorCodeQName(StructuredQName errorCode) {
        this.errorCode = errorCode;
    }

    @Override
    public String getErrorCode() {
        if (this.errorCode == null) {
            return null;
        }
        if (this.errorCode.hasURI("http://www.w3.org/2005/xqt-errors")) {
            return this.errorCode.getLocalPart();
        }
        return this.errorCode.getEQName();
    }

    public StructuredQName getErrorCodeQName() {
        return this.errorCode;
    }

    public void setSchemaType(SchemaType type) {
        this.schemaType = type;
    }

    public SchemaType getSchemaType() {
        return this.schemaType;
    }

    public ValidationException makeException() {
        if (this.exception != null) {
            this.exception.maybeSetLocation(this);
            return this.exception;
        }
        ValidationException ve = new ValidationException(this);
        if (this.errorCode == null) {
            ve.setErrorCode("FORG0001");
        } else {
            ve.setErrorCodeQName(this.errorCode);
        }
        ve.setHasBeenReported(this.hasBeenReported);
        this.exception = ve;
        return ve;
    }

    @Override
    public AtomicValue asAtomic() throws ValidationException {
        throw this.makeException();
    }

    public boolean hasBeenReported() {
        return this.hasBeenReported;
    }

    public void setHasBeenReported(boolean reported) {
        this.hasBeenReported = reported;
        if (this.exception != null) {
            this.exception.setHasBeenReported(reported);
        }
    }

    public String getValidationLocationText() {
        FastStringBuffer fsb = new FastStringBuffer(256);
        AbsolutePath valPath = this.getAbsolutePath();
        if (valPath != null) {
            fsb.append("Validating ");
            fsb.append(valPath.getPathUsingPrefixes());
            if (valPath.getSystemId() != null) {
                fsb.append(" in ");
                fsb.append(valPath.getSystemId());
            }
        }
        return fsb.toString();
    }

    public String getContextLocationText() {
        FastStringBuffer fsb = new FastStringBuffer(256);
        AbsolutePath contextPath = this.getContextPath();
        if (contextPath != null) {
            fsb.append("Currently processing ");
            fsb.append(contextPath.getPathUsingPrefixes());
            if (contextPath.getSystemId() != null) {
                fsb.append(" in ");
                fsb.append(contextPath.getSystemId());
            }
        }
        return fsb.toString();
    }

    public AbsolutePath getAbsolutePath() {
        if (this.path != null) {
            return this.path;
        }
        return null;
    }
}

