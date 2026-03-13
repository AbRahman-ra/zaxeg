/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package javax.xml.crypto.dsig.dom;

import java.security.Key;
import javax.xml.crypto.KeySelector;
import javax.xml.crypto.dom.DOMCryptoContext;
import javax.xml.crypto.dsig.XMLSignContext;
import org.w3c.dom.Node;

public class DOMSignContext
extends DOMCryptoContext
implements XMLSignContext {
    private Node parent;
    private Node nextSibling;

    public DOMSignContext(Key signingKey, Node parent) {
        if (signingKey == null) {
            throw new NullPointerException("signingKey cannot be null");
        }
        if (parent == null) {
            throw new NullPointerException("parent cannot be null");
        }
        this.setKeySelector(KeySelector.singletonKeySelector(signingKey));
        this.parent = parent;
    }

    public DOMSignContext(Key signingKey, Node parent, Node nextSibling) {
        if (signingKey == null) {
            throw new NullPointerException("signingKey cannot be null");
        }
        if (parent == null) {
            throw new NullPointerException("parent cannot be null");
        }
        if (nextSibling == null) {
            throw new NullPointerException("nextSibling cannot be null");
        }
        this.setKeySelector(KeySelector.singletonKeySelector(signingKey));
        this.parent = parent;
        this.nextSibling = nextSibling;
    }

    public DOMSignContext(KeySelector ks, Node parent) {
        if (ks == null) {
            throw new NullPointerException("key selector cannot be null");
        }
        if (parent == null) {
            throw new NullPointerException("parent cannot be null");
        }
        this.setKeySelector(ks);
        this.parent = parent;
    }

    public DOMSignContext(KeySelector ks, Node parent, Node nextSibling) {
        if (ks == null) {
            throw new NullPointerException("key selector cannot be null");
        }
        if (parent == null) {
            throw new NullPointerException("parent cannot be null");
        }
        if (nextSibling == null) {
            throw new NullPointerException("nextSibling cannot be null");
        }
        this.setKeySelector(ks);
        this.parent = parent;
        this.nextSibling = nextSibling;
    }

    public void setParent(Node parent) {
        if (parent == null) {
            throw new NullPointerException("parent is null");
        }
        this.parent = parent;
    }

    public void setNextSibling(Node nextSibling) {
        this.nextSibling = nextSibling;
    }

    public Node getParent() {
        return this.parent;
    }

    public Node getNextSibling() {
        return this.nextSibling;
    }
}

