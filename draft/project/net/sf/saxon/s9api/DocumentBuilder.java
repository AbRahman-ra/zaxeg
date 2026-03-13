/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.s9api;

import java.io.File;
import java.net.URI;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import net.sf.saxon.Configuration;
import net.sf.saxon.event.Builder;
import net.sf.saxon.event.FilterFactory;
import net.sf.saxon.event.NamespaceReducer;
import net.sf.saxon.event.PipelineConfiguration;
import net.sf.saxon.event.ProxyReceiver;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.event.ReceivingContentHandler;
import net.sf.saxon.expr.EarlyEvaluationContext;
import net.sf.saxon.expr.JPConverter;
import net.sf.saxon.lib.ParseOptions;
import net.sf.saxon.om.NoElementsSpaceStrippingRule;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.om.TreeInfo;
import net.sf.saxon.om.TreeModel;
import net.sf.saxon.query.XQueryExpression;
import net.sf.saxon.s9api.BuildingContentHandler;
import net.sf.saxon.s9api.BuildingStreamWriterImpl;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.SchemaValidator;
import net.sf.saxon.s9api.WhitespaceStrippingPolicy;
import net.sf.saxon.s9api.XQueryExecutable;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.trans.XPathException;

public class DocumentBuilder {
    private Configuration config;
    private SchemaValidator schemaValidator;
    private boolean dtdValidation;
    private boolean lineNumbering;
    private TreeModel treeModel = TreeModel.TINY_TREE;
    private WhitespaceStrippingPolicy whitespacePolicy = WhitespaceStrippingPolicy.UNSPECIFIED;
    private URI baseURI;
    private XQueryExecutable projectionQuery;

    protected DocumentBuilder(Configuration config) {
        this.config = config;
    }

    public void setTreeModel(TreeModel model) {
        this.treeModel = model;
    }

    public TreeModel getTreeModel() {
        return this.treeModel;
    }

    public void setLineNumbering(boolean option) {
        this.lineNumbering = option;
    }

    public boolean isLineNumbering() {
        return this.lineNumbering;
    }

    public void setSchemaValidator(SchemaValidator validator) {
        this.schemaValidator = validator;
    }

    public SchemaValidator getSchemaValidator() {
        return this.schemaValidator;
    }

    public void setDTDValidation(boolean option) {
        this.dtdValidation = option;
    }

    public boolean isDTDValidation() {
        return this.dtdValidation;
    }

    public void setWhitespaceStrippingPolicy(WhitespaceStrippingPolicy policy) {
        this.whitespacePolicy = policy;
    }

    public WhitespaceStrippingPolicy getWhitespaceStrippingPolicy() {
        return this.whitespacePolicy;
    }

    public void setBaseURI(URI uri) {
        if (!uri.isAbsolute()) {
            throw new IllegalArgumentException("Supplied base URI must be absolute");
        }
        this.baseURI = uri;
    }

    public URI getBaseURI() {
        return this.baseURI;
    }

    public void setDocumentProjectionQuery(XQueryExecutable query) {
        this.projectionQuery = query;
    }

    public XQueryExecutable getDocumentProjectionQuery() {
        return this.projectionQuery;
    }

    public XdmNode build(Source source) throws SaxonApiException {
        XQueryExpression exp;
        FilterFactory ff;
        if (source == null) {
            throw new NullPointerException("source");
        }
        if (this.whitespacePolicy != WhitespaceStrippingPolicy.UNSPECIFIED && this.whitespacePolicy != WhitespaceStrippingPolicy.IGNORABLE && this.whitespacePolicy.ordinal() != 4) {
            if (this.dtdValidation) {
                throw new SaxonApiException("When DTD validation is used, the whitespace stripping policy must be IGNORABLE");
            }
            if (this.schemaValidator != null) {
                throw new SaxonApiException("When schema validation is used, the whitespace stripping policy must be IGNORABLE");
            }
        }
        ParseOptions options = new ParseOptions(this.config.getParseOptions());
        options.setDTDValidationMode(this.dtdValidation ? 1 : 4);
        if (this.schemaValidator != null) {
            options.setSchemaValidationMode(this.schemaValidator.isLax() ? 2 : 1);
            if (this.schemaValidator.getDocumentElementName() != null) {
                QName qn = this.schemaValidator.getDocumentElementName();
                options.setTopLevelElement(new StructuredQName(qn.getPrefix(), qn.getNamespaceURI(), qn.getLocalName()));
            }
            if (this.schemaValidator.getDocumentElementType() != null) {
                options.setTopLevelType(this.schemaValidator.getDocumentElementType());
            }
        }
        if (this.treeModel != null) {
            options.setModel(this.treeModel);
        }
        if (this.whitespacePolicy != null && this.whitespacePolicy != WhitespaceStrippingPolicy.UNSPECIFIED) {
            int option = this.whitespacePolicy.ordinal();
            if (option == 4) {
                options.setSpaceStrippingRule(NoElementsSpaceStrippingRule.getInstance());
                options.addFilter(this.whitespacePolicy.makeStripper());
            } else {
                options.setSpaceStrippingRule(this.whitespacePolicy.getSpaceStrippingRule());
            }
        }
        options.setLineNumbering(this.lineNumbering);
        if (source.getSystemId() == null && this.baseURI != null) {
            source.setSystemId(this.baseURI.toString());
        }
        if (this.projectionQuery != null && (ff = this.config.makeDocumentProjector(exp = this.projectionQuery.getUnderlyingCompiledQuery())) != null) {
            options.addFilter(ff);
        }
        try {
            TreeInfo doc = this.config.buildDocumentTree(source, options);
            return new XdmNode(doc.getRootNode());
        } catch (XPathException e) {
            throw new SaxonApiException(e);
        }
    }

    public XdmNode build(File file) throws SaxonApiException {
        return this.build(new StreamSource(file));
    }

    public BuildingContentHandler newBuildingContentHandler() throws SaxonApiException {
        PipelineConfiguration pipe = this.config.makePipelineConfiguration();
        Builder builder = this.treeModel.makeBuilder(pipe);
        if (this.baseURI != null) {
            builder.setSystemId(this.baseURI.toASCIIString());
        }
        builder.setLineNumbering(this.lineNumbering);
        Receiver r = builder;
        r = new NamespaceReducer(r);
        r = this.injectValidator(r, builder);
        return new BuildingContentHandlerImpl(r, builder);
    }

    private Receiver injectValidator(Receiver r, Builder builder) throws SaxonApiException {
        if (this.schemaValidator != null) {
            PipelineConfiguration pipe = builder.getPipelineConfiguration();
            Receiver val = this.schemaValidator.getReceiver(pipe, this.config.obtainDefaultSerializationProperties());
            val.setPipelineConfiguration(pipe);
            if (val instanceof ProxyReceiver) {
                ((ProxyReceiver)val).setUnderlyingReceiver(r);
            }
            return val;
        }
        return r;
    }

    public BuildingStreamWriterImpl newBuildingStreamWriter() throws SaxonApiException {
        PipelineConfiguration pipe = this.config.makePipelineConfiguration();
        Builder builder = this.treeModel.makeBuilder(pipe);
        builder.setLineNumbering(this.lineNumbering);
        Receiver r = builder;
        r = new NamespaceReducer(r);
        r = this.injectValidator(r, builder);
        return new BuildingStreamWriterImpl(r, builder);
    }

    public XdmNode wrap(Object node) throws IllegalArgumentException {
        if (node instanceof NodeInfo) {
            NodeInfo nodeInfo = (NodeInfo)node;
            if (nodeInfo.getConfiguration().isCompatible(this.config)) {
                return new XdmNode(nodeInfo);
            }
            throw new IllegalArgumentException("Supplied NodeInfo was created using a different Configuration");
        }
        try {
            JPConverter converter = JPConverter.allocate(node.getClass(), null, this.config);
            NodeInfo nodeInfo = (NodeInfo)converter.convert(node, new EarlyEvaluationContext(this.config));
            return XdmItem.wrapItem(nodeInfo);
        } catch (XPathException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    private static class BuildingContentHandlerImpl
    extends ReceivingContentHandler
    implements BuildingContentHandler {
        private Builder builder;

        public BuildingContentHandlerImpl(Receiver r, Builder b) {
            this.setReceiver(r);
            this.setPipelineConfiguration(r.getPipelineConfiguration());
            this.builder = b;
        }

        @Override
        public XdmNode getDocumentNode() {
            return new XdmNode(this.builder.getCurrentRoot());
        }
    }
}

