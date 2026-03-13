/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.lib;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
import net.sf.saxon.Configuration;
import net.sf.saxon.event.FilterFactory;
import net.sf.saxon.expr.accum.Accumulator;
import net.sf.saxon.lib.AugmentedSource;
import net.sf.saxon.lib.ErrorReporter;
import net.sf.saxon.lib.InvalidityHandler;
import net.sf.saxon.lib.StandardErrorReporter;
import net.sf.saxon.lib.ValidationStatisticsRecipient;
import net.sf.saxon.om.SpaceStrippingRule;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.om.TreeModel;
import net.sf.saxon.trans.Maker;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.type.ValidationParams;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

public class ParseOptions {
    private int schemaValidation = 0;
    private int dtdValidation = 0;
    private StructuredQName topLevelElement;
    private SchemaType topLevelType;
    private transient XMLReader parser = null;
    private Maker<XMLReader> parserMaker;
    private Boolean wrapDocument = null;
    private TreeModel treeModel = null;
    private SpaceStrippingRule spaceStrippingRule = null;
    private Boolean lineNumbering = null;
    private boolean pleaseClose = false;
    private transient ErrorReporter errorReporter = null;
    private transient EntityResolver entityResolver = null;
    private transient ErrorHandler errorHandler = null;
    private List<FilterFactory> filters = null;
    private boolean continueAfterValidationErrors = false;
    private boolean addCommentsAfterValidationErrors = false;
    private boolean expandAttributeDefaults = true;
    private boolean useXsiSchemaLocation = true;
    private boolean checkEntityReferences = false;
    private boolean stable = true;
    private int validationErrorLimit = Integer.MAX_VALUE;
    private ValidationParams validationParams = null;
    private ValidationStatisticsRecipient validationStatisticsRecipient = null;
    private Map<String, Boolean> parserFeatures = null;
    private Map<String, Object> parserProperties = null;
    private InvalidityHandler invalidityHandler = null;
    private Set<? extends Accumulator> applicableAccumulators = null;

    public ParseOptions() {
    }

    public ParseOptions(ParseOptions p) {
        this.schemaValidation = p.schemaValidation;
        this.validationParams = p.validationParams;
        this.setDTDValidationMode(p.dtdValidation);
        this.topLevelElement = p.topLevelElement;
        this.topLevelType = p.topLevelType;
        this.parserMaker = p.getXMLReaderMaker();
        this.parser = p.parser;
        this.wrapDocument = p.wrapDocument;
        this.treeModel = p.treeModel;
        this.spaceStrippingRule = p.spaceStrippingRule;
        this.lineNumbering = p.lineNumbering;
        this.pleaseClose = p.pleaseClose;
        this.errorHandler = p.errorHandler;
        this.errorReporter = p.errorReporter;
        this.entityResolver = p.entityResolver;
        this.invalidityHandler = p.invalidityHandler;
        this.stable = p.stable;
        if (p.filters != null) {
            this.filters = new ArrayList<FilterFactory>(p.filters);
        }
        this.setExpandAttributeDefaults(p.expandAttributeDefaults);
        this.useXsiSchemaLocation = p.useXsiSchemaLocation;
        this.validationErrorLimit = p.validationErrorLimit;
        this.continueAfterValidationErrors = p.continueAfterValidationErrors;
        this.addCommentsAfterValidationErrors = p.addCommentsAfterValidationErrors;
        if (p.parserFeatures != null) {
            this.parserFeatures = new HashMap<String, Boolean>(p.parserFeatures);
        }
        if (p.parserProperties != null) {
            this.parserProperties = new HashMap<String, Object>(p.parserProperties);
        }
        this.applicableAccumulators = p.applicableAccumulators;
        this.checkEntityReferences = p.checkEntityReferences;
        this.validationStatisticsRecipient = p.validationStatisticsRecipient;
    }

    public void merge(ParseOptions options) {
        if (options.dtdValidation != 0) {
            this.setDTDValidationMode(options.dtdValidation);
        }
        if (options.schemaValidation != 0) {
            this.schemaValidation = options.schemaValidation;
        }
        if (options.invalidityHandler != null) {
            this.invalidityHandler = options.invalidityHandler;
        }
        if (options.topLevelElement != null) {
            this.topLevelElement = options.topLevelElement;
        }
        if (options.topLevelType != null) {
            this.topLevelType = options.topLevelType;
        }
        if (options.parser != null) {
            this.parser = options.parser;
        }
        if (options.wrapDocument != null) {
            this.wrapDocument = options.wrapDocument;
        }
        if (options.treeModel != null) {
            this.treeModel = options.treeModel;
        }
        if (options.spaceStrippingRule != null) {
            this.spaceStrippingRule = options.spaceStrippingRule;
        }
        if (options.lineNumbering != null) {
            this.lineNumbering = options.lineNumbering;
        }
        if (options.pleaseClose) {
            this.pleaseClose = true;
        }
        if (options.errorReporter != null) {
            this.errorReporter = options.errorReporter;
        }
        if (options.entityResolver != null) {
            this.entityResolver = options.entityResolver;
        }
        if (options.filters != null) {
            if (this.filters == null) {
                this.filters = new ArrayList<FilterFactory>();
            }
            this.filters.addAll(options.filters);
        }
        if (options.parserFeatures != null) {
            if (this.parserFeatures == null) {
                this.parserFeatures = new HashMap<String, Boolean>();
            }
            this.parserFeatures.putAll(options.parserFeatures);
        }
        if (options.parserProperties != null) {
            if (this.parserProperties == null) {
                this.parserProperties = new HashMap<String, Object>();
            }
            this.parserProperties.putAll(options.parserProperties);
        }
        if (!options.expandAttributeDefaults) {
            this.setExpandAttributeDefaults(false);
        }
        if (!options.useXsiSchemaLocation) {
            this.useXsiSchemaLocation = false;
        }
        if (options.addCommentsAfterValidationErrors) {
            this.addCommentsAfterValidationErrors = true;
        }
        this.validationErrorLimit = Math.min(this.validationErrorLimit, options.validationErrorLimit);
    }

    public void applyDefaults(Configuration config) {
        if (this.dtdValidation == 0) {
            this.setDTDValidationMode(config.isValidation() ? 1 : 4);
        }
        if (this.schemaValidation == 0) {
            this.schemaValidation = config.getSchemaValidationMode();
        }
        if (this.treeModel == null) {
            this.treeModel = TreeModel.getTreeModel(config.getTreeModel());
        }
        if (this.spaceStrippingRule == null) {
            this.spaceStrippingRule = config.getParseOptions().getSpaceStrippingRule();
        }
        if (this.lineNumbering == null) {
            this.lineNumbering = config.isLineNumbering();
        }
        if (this.errorReporter == null) {
            this.setErrorReporter(config.makeErrorReporter());
        }
    }

    public void addFilter(FilterFactory filterFactory) {
        if (this.filters == null) {
            this.filters = new ArrayList<FilterFactory>(5);
        }
        this.filters.add(filterFactory);
    }

    public List<FilterFactory> getFilters() {
        return this.filters;
    }

    public SpaceStrippingRule getSpaceStrippingRule() {
        return this.spaceStrippingRule;
    }

    public void setSpaceStrippingRule(SpaceStrippingRule rule) {
        this.spaceStrippingRule = rule;
    }

    public void setTreeModel(int model) {
        this.treeModel = TreeModel.getTreeModel(model);
    }

    public void addParserFeature(String uri, boolean value) {
        if (this.parserFeatures == null) {
            this.parserFeatures = new HashMap<String, Boolean>();
        }
        this.parserFeatures.put(uri, value);
    }

    public void addParserProperties(String uri, Object value) {
        if (this.parserProperties == null) {
            this.parserProperties = new HashMap<String, Object>();
        }
        this.parserProperties.put(uri, value);
    }

    public boolean getParserFeature(String uri) {
        return this.parserFeatures.get(uri);
    }

    public Object getParserProperty(String name) {
        return this.parserProperties.get(name);
    }

    public Map<String, Boolean> getParserFeatures() {
        return this.parserFeatures;
    }

    public Map<String, Object> getParserProperties() {
        return this.parserProperties;
    }

    public int getTreeModel() {
        if (this.treeModel == null) {
            return -1;
        }
        return this.treeModel.getSymbolicValue();
    }

    public void setModel(TreeModel model) {
        this.treeModel = model;
    }

    public TreeModel getModel() {
        return this.treeModel == null ? TreeModel.TINY_TREE : this.treeModel;
    }

    public void setSchemaValidationMode(int option) {
        this.schemaValidation = option;
    }

    public int getSchemaValidationMode() {
        return this.schemaValidation;
    }

    public void setExpandAttributeDefaults(boolean expand) {
        this.expandAttributeDefaults = expand;
    }

    public boolean isExpandAttributeDefaults() {
        return this.expandAttributeDefaults;
    }

    public void setTopLevelElement(StructuredQName elementName) {
        this.topLevelElement = elementName;
    }

    public StructuredQName getTopLevelElement() {
        return this.topLevelElement;
    }

    public void setTopLevelType(SchemaType type) {
        this.topLevelType = type;
    }

    public SchemaType getTopLevelType() {
        return this.topLevelType;
    }

    public void setUseXsiSchemaLocation(boolean use) {
        this.useXsiSchemaLocation = use;
    }

    public boolean isUseXsiSchemaLocation() {
        return this.useXsiSchemaLocation;
    }

    public int getValidationErrorLimit() {
        return this.validationErrorLimit;
    }

    public void setValidationErrorLimit(int validationErrorLimit) {
        this.validationErrorLimit = validationErrorLimit;
    }

    public void setDTDValidationMode(int option) {
        this.dtdValidation = option;
        this.addParserFeature("http://xml.org/sax/features/validation", option == 1 || option == 2);
    }

    public int getDTDValidationMode() {
        return this.dtdValidation;
    }

    public void setValidationStatisticsRecipient(ValidationStatisticsRecipient recipient) {
        this.validationStatisticsRecipient = recipient;
    }

    public ValidationStatisticsRecipient getValidationStatisticsRecipient() {
        return this.validationStatisticsRecipient;
    }

    public void setLineNumbering(boolean lineNumbering) {
        this.lineNumbering = lineNumbering;
    }

    public boolean isLineNumbering() {
        return this.lineNumbering != null && this.lineNumbering != false;
    }

    public boolean isLineNumberingSet() {
        return this.lineNumbering != null;
    }

    public void setXMLReader(XMLReader parser) {
        this.parser = parser;
    }

    public XMLReader getXMLReader() {
        return this.parser;
    }

    public void setXMLReaderMaker(Maker<XMLReader> parserMaker) {
        this.parserMaker = parserMaker;
    }

    public Maker<XMLReader> getXMLReaderMaker() {
        return this.parserMaker;
    }

    public XMLReader obtainXMLReader() throws XPathException {
        if (this.parserMaker != null) {
            return this.parserMaker.make();
        }
        if (this.parser != null) {
            return this.parser;
        }
        return null;
    }

    public void setEntityResolver(EntityResolver resolver) {
        this.entityResolver = resolver;
    }

    public EntityResolver getEntityResolver() {
        return this.entityResolver;
    }

    public void setErrorHandler(ErrorHandler handler) {
        this.errorHandler = handler;
    }

    public ErrorHandler getErrorHandler() {
        return this.errorHandler;
    }

    public void setWrapDocument(Boolean wrap) {
        this.wrapDocument = wrap;
    }

    public Boolean getWrapDocument() {
        return this.wrapDocument;
    }

    public void setXIncludeAware(boolean state) {
        this.addParserFeature("http://apache.org/xml/features/xinclude", state);
    }

    public boolean isXIncludeAwareSet() {
        return this.parserFeatures != null && this.parserFeatures.get("http://apache.org/xml/features/xinclude") != null;
    }

    public boolean isXIncludeAware() {
        if (this.parserFeatures == null) {
            return false;
        }
        Boolean b = this.parserFeatures.get("http://apache.org/xml/features/xinclude");
        return b != null && b != false;
    }

    public void setErrorReporter(ErrorReporter reporter) {
        if (reporter == null) {
            reporter = new StandardErrorReporter();
        }
        this.errorReporter = reporter;
    }

    public ErrorReporter getErrorReporter() {
        return this.errorReporter;
    }

    public void setContinueAfterValidationErrors(boolean keepGoing) {
        this.continueAfterValidationErrors = keepGoing;
    }

    public boolean isContinueAfterValidationErrors() {
        return this.continueAfterValidationErrors;
    }

    public void setAddCommentsAfterValidationErrors(boolean keepGoing) {
        this.addCommentsAfterValidationErrors = keepGoing;
    }

    public boolean isAddCommentsAfterValidationErrors() {
        return this.addCommentsAfterValidationErrors;
    }

    public void setValidationParams(ValidationParams params) {
        this.validationParams = params;
    }

    public ValidationParams getValidationParams() {
        return this.validationParams;
    }

    public void setCheckEntityReferences(boolean check) {
        this.checkEntityReferences = check;
    }

    public boolean isCheckEntityReferences() {
        return this.checkEntityReferences;
    }

    public boolean isStable() {
        return this.stable;
    }

    public void setStable(boolean stable) {
        this.stable = stable;
    }

    public InvalidityHandler getInvalidityHandler() {
        return this.invalidityHandler;
    }

    public void setInvalidityHandler(InvalidityHandler invalidityHandler) {
        this.invalidityHandler = invalidityHandler;
    }

    public void setApplicableAccumulators(Set<? extends Accumulator> accumulators) {
        this.applicableAccumulators = accumulators;
    }

    public Set<? extends Accumulator> getApplicableAccumulators() {
        return this.applicableAccumulators;
    }

    public void setPleaseCloseAfterUse(boolean close) {
        this.pleaseClose = close;
    }

    public boolean isPleaseCloseAfterUse() {
        return this.pleaseClose;
    }

    public static void close(Source source) {
        try {
            if (source instanceof StreamSource) {
                StreamSource ss = (StreamSource)source;
                if (ss.getInputStream() != null) {
                    ss.getInputStream().close();
                }
                if (ss.getReader() != null) {
                    ss.getReader().close();
                }
            } else if (source instanceof SAXSource) {
                InputSource is = ((SAXSource)source).getInputSource();
                if (is != null) {
                    if (is.getByteStream() != null) {
                        is.getByteStream().close();
                    }
                    if (is.getCharacterStream() != null) {
                        is.getCharacterStream().close();
                    }
                }
            } else if (source instanceof AugmentedSource) {
                ((AugmentedSource)source).close();
            }
        } catch (IOException iOException) {
            // empty catch block
        }
    }
}

