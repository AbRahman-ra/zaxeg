/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.serialize;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Properties;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;
import net.sf.saxon.event.PipelineConfiguration;
import net.sf.saxon.event.ReceiverWithOutputProperties;
import net.sf.saxon.event.SequenceWriter;
import net.sf.saxon.functions.FormatNumber;
import net.sf.saxon.ma.arrays.ArrayItem;
import net.sf.saxon.ma.map.KeyValuePair;
import net.sf.saxon.ma.map.MapItem;
import net.sf.saxon.om.Function;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.query.QueryResult;
import net.sf.saxon.serialize.CharacterMap;
import net.sf.saxon.serialize.CharacterMapIndex;
import net.sf.saxon.serialize.SerializationProperties;
import net.sf.saxon.serialize.codenorm.Normalizer;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.DoubleValue;
import net.sf.saxon.value.QualifiedNameValue;

public class AdaptiveEmitter
extends SequenceWriter
implements ReceiverWithOutputProperties {
    private Writer writer;
    private CharacterMap characterMap;
    private Properties outputProperties;
    private String itemSeparator = "\n";
    private boolean started = false;

    public AdaptiveEmitter(PipelineConfiguration pipe, Writer writer) {
        super(pipe);
        this.writer = writer;
    }

    public void setOutputProperties(Properties props) {
        this.outputProperties = props;
        String sep = props.getProperty("item-separator");
        if (sep != null && !"#absent".equals(sep)) {
            this.itemSeparator = sep;
        }
    }

    public void setNormalizer(Normalizer normalizer) {
    }

    public void setCharacterMap(CharacterMap map) {
        this.characterMap = map;
    }

    @Override
    public Properties getOutputProperties() {
        return this.outputProperties;
    }

    private void emit(CharSequence s) throws XPathException {
        try {
            this.writer.append(s);
        } catch (IOException e) {
            throw new XPathException(e);
        }
    }

    @Override
    public void write(Item item) throws XPathException {
        if (this.started) {
            this.emit(this.itemSeparator);
        } else {
            if (this.writer == null) {
                // empty if block
            }
            this.started = true;
        }
        this.serializeItem(item);
    }

    private void serializeItem(Item item) throws XPathException {
        if (item instanceof AtomicValue) {
            this.emit(this.serializeAtomicValue((AtomicValue)item));
        } else if (item instanceof NodeInfo) {
            this.serializeNode((NodeInfo)item);
        } else if (item instanceof MapItem) {
            this.serializeMap((MapItem)item);
        } else if (item instanceof ArrayItem) {
            this.serializeArray((ArrayItem)item);
        } else if (item instanceof Function) {
            this.serializeFunction((Function)item);
        }
    }

    private String serializeAtomicValue(AtomicValue value) throws XPathException {
        switch (value.getPrimitiveType().getFingerprint()) {
            case 513: 
            case 529: 
            case 631: {
                String s = value.getStringValue();
                if (s.contains("\"")) {
                    s = s.replace("\"", "\"\"");
                }
                if (this.characterMap != null) {
                    s = this.characterMap.map(s, false).toString();
                }
                return "\"" + s + "\"";
            }
            case 514: {
                return value.effectiveBooleanValue() ? "true()" : "false()";
            }
            case 515: 
            case 533: {
                return value.getStringValue();
            }
            case 517: {
                return FormatNumber.formatExponential((DoubleValue)value);
            }
            case 516: 
            case 518: 
            case 519: 
            case 520: 
            case 521: 
            case 522: 
            case 523: 
            case 524: 
            case 525: 
            case 526: 
            case 527: 
            case 528: {
                return value.getPrimitiveType().getDisplayName() + "(\"" + value.getStringValue() + "\")";
            }
            case 633: 
            case 634: {
                return "xs:duration(\"" + value.getStringValue() + "\")";
            }
            case 530: 
            case 531: {
                return ((QualifiedNameValue)value).getStructuredQName().getEQName();
            }
        }
        return "***";
    }

    private void serializeFunction(Function fn) throws XPathException {
        StructuredQName fname = fn.getFunctionName();
        if (fname == null || fname.hasURI("http://ns.saxonica.com/anonymous-type")) {
            this.emit("(anonymous-function)");
        } else if (fname.hasURI("http://www.w3.org/2005/xpath-functions")) {
            this.emit("fn:" + fname.getLocalPart());
        } else if (fname.hasURI("http://www.w3.org/2005/xpath-functions/math")) {
            this.emit("math:" + fname.getLocalPart());
        } else if (fname.hasURI("http://www.w3.org/2005/xpath-functions/map")) {
            this.emit("map:" + fname.getLocalPart());
        } else if (fname.hasURI("http://www.w3.org/2005/xpath-functions/array")) {
            this.emit("array:" + fname.getLocalPart());
        } else if (fname.hasURI("http://www.w3.org/2001/XMLSchema")) {
            this.emit("xs:" + fname.getLocalPart());
        } else {
            this.emit(fname.getEQName());
        }
        this.emit("#" + fn.getArity());
    }

    private void serializeNode(NodeInfo node) throws XPathException {
        switch (node.getNodeKind()) {
            case 2: {
                this.emit(node.getDisplayName());
                this.emit("=\"");
                this.emit(node.getStringValueCS());
                this.emit("\"");
                break;
            }
            case 13: {
                this.emit(node.getLocalPart().isEmpty() ? "xmlns" : "xmlns:" + node.getLocalPart());
                this.emit("=\"");
                this.emit(node.getStringValueCS());
                this.emit("\"");
                break;
            }
            default: {
                StringWriter sw = new StringWriter();
                Properties props = new Properties(this.outputProperties);
                props.setProperty("method", "xml");
                props.setProperty("indent", "no");
                if (props.getProperty("omit-xml-declaration") == null) {
                    props.setProperty("omit-xml-declaration", "no");
                }
                props.setProperty("{http://saxon.sf.net/}unfailing", "yes");
                CharacterMapIndex cmi = null;
                if (this.characterMap != null) {
                    cmi = new CharacterMapIndex();
                    cmi.putCharacterMap(this.characterMap.getName(), this.characterMap);
                }
                SerializationProperties sProps = new SerializationProperties(props, cmi);
                QueryResult.serialize(node, (Result)new StreamResult(sw), sProps);
                this.emit(sw.toString().trim());
            }
        }
    }

    private void serializeArray(ArrayItem array) throws XPathException {
        this.emit("[");
        boolean first = true;
        for (Sequence sequence : array.members()) {
            if (first) {
                first = false;
            } else {
                this.emit(",");
            }
            this.outputInternalSequence(sequence);
        }
        this.emit("]");
    }

    private void serializeMap(MapItem map) throws XPathException {
        this.emit("map{");
        boolean first = true;
        for (KeyValuePair pair : map.keyValuePairs()) {
            if (first) {
                first = false;
            } else {
                this.emit(",");
            }
            this.serializeItem(pair.key);
            this.emit(":");
            GroundedValue value = pair.value;
            this.outputInternalSequence(value);
        }
        this.emit("}");
    }

    private void outputInternalSequence(Sequence value) throws XPathException {
        Item it;
        boolean omitParens;
        boolean first = true;
        SequenceIterator iter = value.iterate();
        boolean bl = omitParens = value instanceof GroundedValue && ((GroundedValue)value).getLength() == 1;
        if (!omitParens) {
            this.emit("(");
        }
        while ((it = iter.next()) != null) {
            if (!first) {
                this.emit(",");
            }
            first = false;
            this.serializeItem(it);
        }
        if (!omitParens) {
            this.emit(")");
        }
    }

    @Override
    public void close() throws XPathException {
        super.close();
        if (this.writer != null) {
            try {
                this.writer.close();
            } catch (IOException e) {
                throw new XPathException(e);
            }
        }
    }
}

