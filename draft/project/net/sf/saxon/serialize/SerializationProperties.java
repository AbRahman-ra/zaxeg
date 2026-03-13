/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.serialize;

import java.util.Properties;
import net.sf.saxon.event.FilterFactory;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.event.SequenceNormalizer;
import net.sf.saxon.event.SequenceNormalizerWithItemSeparator;
import net.sf.saxon.event.SequenceNormalizerWithSpaceSeparator;
import net.sf.saxon.serialize.CharacterMap;
import net.sf.saxon.serialize.CharacterMapIndex;

public class SerializationProperties {
    Properties properties;
    CharacterMapIndex charMapIndex;
    FilterFactory validationFactory;

    public SerializationProperties() {
        this.properties = new Properties();
    }

    public SerializationProperties(Properties props) {
        this.properties = props;
    }

    public SerializationProperties(Properties props, CharacterMapIndex charMapIndex) {
        this.properties = props;
        this.charMapIndex = charMapIndex;
    }

    public void setProperty(String name, String value) {
        this.properties.setProperty(name, value);
    }

    public String getProperty(String name) {
        return this.getProperties().getProperty(name);
    }

    public Properties getProperties() {
        return this.properties;
    }

    public CharacterMapIndex getCharacterMapIndex() {
        return this.charMapIndex;
    }

    public void setValidationFactory(FilterFactory validationFactory) {
        this.validationFactory = validationFactory;
    }

    public FilterFactory getValidationFactory() {
        return this.validationFactory;
    }

    public SequenceNormalizer makeSequenceNormalizer(Receiver next) {
        String itemSeparator;
        if (this.getValidationFactory() != null) {
            next = this.getValidationFactory().makeFilter(next);
        }
        return (itemSeparator = this.properties.getProperty("item-separator")) == null || "#absent".equals(itemSeparator) ? new SequenceNormalizerWithSpaceSeparator(next) : new SequenceNormalizerWithItemSeparator(next, itemSeparator);
    }

    public SerializationProperties combineWith(SerializationProperties defaults) {
        FilterFactory validationFactory;
        CharacterMapIndex charMap = this.charMapIndex;
        if (charMap == null || charMap.isEmpty()) {
            charMap = defaults.getCharacterMapIndex();
        }
        if ((validationFactory = this.validationFactory) == null) {
            validationFactory = defaults.validationFactory;
        }
        Properties props = new Properties(defaults.getProperties());
        for (String prop : this.getProperties().stringPropertyNames()) {
            String value = this.getProperties().getProperty(prop);
            if (prop.equals("cdata-section-elements") || prop.equals("suppress-indentation") || prop.equals("use-character-maps")) {
                String existing = defaults.getProperty(prop);
                if (existing == null || existing.equals(value)) {
                    props.setProperty(prop, value);
                    continue;
                }
                props.setProperty(prop, existing + " " + value);
                if (!prop.equals("use-character-maps")) continue;
                CharacterMapIndex charMapIndex2 = charMap.copy();
                for (CharacterMap map : defaults.getCharacterMapIndex()) {
                    charMapIndex2.putCharacterMap(map.getName(), map);
                }
                charMap = charMapIndex2;
                continue;
            }
            props.setProperty(prop, value);
        }
        SerializationProperties newParams = new SerializationProperties(props, charMap);
        newParams.setValidationFactory(validationFactory);
        return newParams;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (String k : this.properties.stringPropertyNames()) {
            sb.append(k).append("=").append(this.properties.getProperty(k)).append(" ");
        }
        if (this.charMapIndex != null) {
            for (CharacterMap cm : this.charMapIndex) {
                sb.append(cm.getName().getEQName()).append("={").append(cm.toString()).append("} ");
            }
        }
        return sb.toString();
    }
}

