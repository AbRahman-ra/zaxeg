/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package javax.xml.crypto.dsig.dom;

import java.security.Key;
import javax.xml.crypto.KeySelector;
import javax.xml.crypto.dom.DOMCryptoContext;
import javax.xml.crypto.dsig.XMLValidateContext;
import org.w3c.dom.Node;

public class DOMValidateContext
extends DOMCryptoContext
implements XMLValidateContext {
    private Node node;

    public DOMValidateContext(KeySelector ks, Node node) {
        if (ks == null) {
            throw new NullPointerException("key selector is null");
        }
        if (node == null) {
            throw new NullPointerException("node is null");
        }
        this.setKeySelector(ks);
        this.node = node;
    }

    public DOMValidateContext(Key validatingKey, Node node) {
        if (validatingKey == null) {
            throw new NullPointerException("validatingKey is null");
        }
        if (node == null) {
            throw new NullPointerException("node is null");
        }
        this.setKeySelector(KeySelector.singletonKeySelector(validatingKey));
        this.node = node;
    }

    public void setNode(Node node) {
        if (node == null) {
            throw new NullPointerException();
        }
        this.node = node;
    }

    public Node getNode() {
        return this.node;
    }
}

