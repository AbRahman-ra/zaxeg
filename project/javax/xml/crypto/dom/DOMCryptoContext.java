/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package javax.xml.crypto.dom;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.xml.crypto.KeySelector;
import javax.xml.crypto.URIDereferencer;
import javax.xml.crypto.XMLCryptoContext;
import org.w3c.dom.Element;

public class DOMCryptoContext
implements XMLCryptoContext {
    private Map<String, String> nsMap = new HashMap<String, String>();
    private Map<String, Element> idMap = new HashMap<String, Element>();
    private Map<Object, Object> objMap = new HashMap<Object, Object>();
    private Map<String, Object> propMap = new HashMap<String, Object>();
    private String baseURI;
    private KeySelector ks;
    private URIDereferencer dereferencer;
    private String defaultPrefix;

    protected DOMCryptoContext() {
    }

    public String getNamespacePrefix(String namespaceURI, String defaultPrefix) {
        if (namespaceURI == null) {
            throw new NullPointerException("namespaceURI cannot be null");
        }
        String prefix = this.nsMap.get(namespaceURI);
        return prefix != null ? prefix : defaultPrefix;
    }

    public String putNamespacePrefix(String namespaceURI, String prefix) {
        if (namespaceURI == null) {
            throw new NullPointerException("namespaceURI is null");
        }
        return this.nsMap.put(namespaceURI, prefix);
    }

    public String getDefaultNamespacePrefix() {
        return this.defaultPrefix;
    }

    public void setDefaultNamespacePrefix(String defaultPrefix) {
        this.defaultPrefix = defaultPrefix;
    }

    public String getBaseURI() {
        return this.baseURI;
    }

    public void setBaseURI(String baseURI) {
        if (baseURI != null) {
            URI.create(baseURI);
        }
        this.baseURI = baseURI;
    }

    public URIDereferencer getURIDereferencer() {
        return this.dereferencer;
    }

    public void setURIDereferencer(URIDereferencer dereferencer) {
        this.dereferencer = dereferencer;
    }

    public Object getProperty(String name) {
        if (name == null) {
            throw new NullPointerException("name is null");
        }
        return this.propMap.get(name);
    }

    public Object setProperty(String name, Object value) {
        if (name == null) {
            throw new NullPointerException("name is null");
        }
        return this.propMap.put(name, value);
    }

    public KeySelector getKeySelector() {
        return this.ks;
    }

    public void setKeySelector(KeySelector ks) {
        this.ks = ks;
    }

    public Element getElementById(String idValue) {
        if (idValue == null) {
            throw new NullPointerException("idValue is null");
        }
        return this.idMap.get(idValue);
    }

    public void setIdAttributeNS(Element element, String namespaceURI, String localName) {
        if (element == null) {
            throw new NullPointerException("element is null");
        }
        if (localName == null) {
            throw new NullPointerException("localName is null");
        }
        String idValue = element.getAttributeNS(namespaceURI, localName);
        if (idValue == null || idValue.length() == 0) {
            throw new IllegalArgumentException(localName + " is not an " + "attribute");
        }
        this.idMap.put(idValue, element);
    }

    public Iterator iterator() {
        return Collections.unmodifiableMap(this.idMap).entrySet().iterator();
    }

    public Object get(Object key) {
        return this.objMap.get(key);
    }

    public Object put(Object key, Object value) {
        return this.objMap.put(key, value);
    }
}

