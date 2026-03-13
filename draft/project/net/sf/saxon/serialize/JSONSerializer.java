/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.serialize;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Properties;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;
import net.sf.saxon.event.PipelineConfiguration;
import net.sf.saxon.event.ReceiverWithOutputProperties;
import net.sf.saxon.event.SequenceWriter;
import net.sf.saxon.ma.arrays.ArrayItem;
import net.sf.saxon.ma.map.KeyValuePair;
import net.sf.saxon.ma.map.MapItem;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.query.QueryResult;
import net.sf.saxon.serialize.CharacterMap;
import net.sf.saxon.serialize.JSONEmitter;
import net.sf.saxon.serialize.charcode.CharacterSet;
import net.sf.saxon.serialize.codenorm.Normalizer;
import net.sf.saxon.trans.Err;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.StringValue;

public class JSONSerializer
extends SequenceWriter
implements ReceiverWithOutputProperties {
    private boolean allowDuplicateKeys = false;
    private String nodeOutputMethod = "xml";
    private int level = 0;
    private int topLevelCount = 0;
    private int maxLineLength = 80;
    private JSONEmitter emitter;
    private Properties outputProperties;
    private CharacterSet characterSet;
    private boolean isIndenting;
    private Comparator<AtomicValue> propertySorter;
    private boolean unfailing = false;

    public JSONSerializer(PipelineConfiguration pipe, JSONEmitter emitter, Properties outputProperties) throws XPathException {
        super(pipe);
        this.setOutputProperties(outputProperties);
        this.emitter = emitter;
    }

    public void setOutputProperties(Properties details) {
        String max;
        String jnom;
        this.outputProperties = details;
        if ("yes".equals(details.getProperty("allow-duplicate-names"))) {
            this.allowDuplicateKeys = true;
        }
        if ("yes".equals(details.getProperty("indent"))) {
            this.isIndenting = true;
        }
        if ("yes".equals(details.getProperty("{http://saxon.sf.net/}unfailing"))) {
            this.unfailing = true;
            this.allowDuplicateKeys = true;
        }
        if ((jnom = details.getProperty("json-node-output-method")) != null) {
            this.nodeOutputMethod = jnom;
        }
        if ((max = details.getProperty("{http://saxon.sf.net/}line-length")) != null) {
            try {
                this.maxLineLength = Integer.parseInt(max);
            } catch (NumberFormatException numberFormatException) {
                // empty catch block
            }
        }
    }

    public void setPropertySorter(Comparator<AtomicValue> sorter) {
        this.propertySorter = sorter;
    }

    @Override
    public Properties getOutputProperties() {
        return this.outputProperties;
    }

    public void setNormalizer(Normalizer normalizer) {
        this.emitter.setNormalizer(normalizer);
    }

    public void setCharacterMap(CharacterMap map) {
        this.emitter.setCharacterMap(map);
    }

    @Override
    public void write(Item item) throws XPathException {
        if (this.level == 0 && ++this.topLevelCount >= 2) {
            throw new XPathException("JSON output method cannot handle sequences of two or more items", "SERE0023");
        }
        if (item instanceof AtomicValue) {
            this.emitter.writeAtomicValue((AtomicValue)item);
        } else if (item instanceof MapItem) {
            HashSet<String> keys = null;
            if (!this.allowDuplicateKeys) {
                keys = new HashSet<String>();
            }
            boolean oneLiner = !this.isIndenting || this.isOneLinerMap((MapItem)item);
            this.emitter.startMap(oneLiner);
            boolean first = true;
            ArrayList<AtomicValue> arrayList = new ArrayList<AtomicValue>();
            for (KeyValuePair pair : ((MapItem)item).keyValuePairs()) {
                arrayList.add(pair.key);
            }
            if (this.propertySorter != null) {
                arrayList.sort(this.propertySorter);
            }
            for (AtomicValue key : arrayList) {
                String stringKey = key.getStringValue();
                this.emitter.writeKey(stringKey);
                if (!this.allowDuplicateKeys && !keys.add(stringKey)) {
                    throw new XPathException("Key value \"" + stringKey + "\" occurs more than once in JSON map", "SERE0022");
                }
                GroundedValue value = ((MapItem)item).get(key);
                this.writeSequence(value.materialize());
            }
            this.emitter.endMap();
        } else if (item instanceof ArrayItem) {
            boolean oneLiner = !this.isIndenting || this.isOneLinerArray((ArrayItem)item);
            this.emitter.startArray(oneLiner);
            boolean first = true;
            for (Sequence sequence : ((ArrayItem)item).members()) {
                this.writeSequence(sequence.materialize());
            }
            this.emitter.endArray();
        } else if (item instanceof NodeInfo) {
            String s = this.serializeNode((NodeInfo)item);
            this.emitter.writeAtomicValue(new StringValue(s));
        } else if (this.unfailing) {
            String s = item.getStringValue();
            this.emitter.writeAtomicValue(new StringValue(s));
        } else {
            throw new XPathException("JSON output method cannot handle an item of type " + item.getClass(), "SERE0021");
        }
    }

    private boolean isOneLinerArray(ArrayItem array) {
        int totalSize = 0;
        if (array.arrayLength() < 2) {
            return true;
        }
        for (Sequence sequence : array.members()) {
            if (!(sequence instanceof AtomicValue)) {
                return false;
            }
            if ((totalSize += ((AtomicValue)sequence).getStringValueCS().length() + 1) <= this.maxLineLength) continue;
            return false;
        }
        return true;
    }

    private boolean isOneLinerMap(MapItem map) {
        int totalSize = 0;
        if (map.size() < 2) {
            return true;
        }
        for (KeyValuePair entry : map.keyValuePairs()) {
            if (!(entry.value instanceof AtomicValue)) {
                return false;
            }
            if ((totalSize += entry.key.getStringValueCS().length() + ((AtomicValue)entry.value).getStringValueCS().length() + 4) <= this.maxLineLength) continue;
            return false;
        }
        return true;
    }

    private String serializeNode(NodeInfo node) throws XPathException {
        StringWriter sw = new StringWriter();
        Properties props = new Properties();
        props.setProperty("method", this.nodeOutputMethod);
        props.setProperty("indent", "no");
        props.setProperty("omit-xml-declaration", "yes");
        QueryResult.serialize(node, (Result)new StreamResult(sw), props);
        return sw.toString().trim();
    }

    private void writeSequence(GroundedValue seq) throws XPathException {
        int len = seq.getLength();
        if (len == 0) {
            this.emitter.writeAtomicValue(null);
        } else if (len == 1) {
            ++this.level;
            this.write(seq.head());
            --this.level;
        } else {
            throw new XPathException("JSON serialization: cannot handle a sequence of length " + len + Err.depictSequence(seq), "SERE0023");
        }
    }

    @Override
    public void close() throws XPathException {
        if (this.topLevelCount == 0) {
            this.emitter.writeAtomicValue(null);
        }
        this.emitter.close();
        super.close();
    }
}

