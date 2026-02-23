/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.dom4j;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import org.dom4j.DocumentFactory;
import org.dom4j.Namespace;
import org.dom4j.tree.QNameCache;
import org.dom4j.util.SingletonStrategy;

public class QName
implements Serializable {
    private static SingletonStrategy singleton = null;
    private String name;
    private String qualifiedName;
    private transient Namespace namespace;
    private int hashCode;
    private DocumentFactory documentFactory;
    static /* synthetic */ Class class$org$dom4j$tree$QNameCache;

    public QName(String name) {
        this(name, Namespace.NO_NAMESPACE);
    }

    public QName(String name, Namespace namespace) {
        this.name = name == null ? "" : name;
        this.namespace = namespace == null ? Namespace.NO_NAMESPACE : namespace;
    }

    public QName(String name, Namespace namespace, String qualifiedName) {
        this.name = name == null ? "" : name;
        this.qualifiedName = qualifiedName;
        this.namespace = namespace == null ? Namespace.NO_NAMESPACE : namespace;
    }

    public static QName get(String name) {
        return QName.getCache().get(name);
    }

    public static QName get(String name, Namespace namespace) {
        return QName.getCache().get(name, namespace);
    }

    public static QName get(String name, String prefix, String uri) {
        if ((prefix == null || prefix.length() == 0) && uri == null) {
            return QName.get(name);
        }
        if (prefix == null || prefix.length() == 0) {
            return QName.getCache().get(name, Namespace.get(uri));
        }
        if (uri == null) {
            return QName.get(name);
        }
        return QName.getCache().get(name, Namespace.get(prefix, uri));
    }

    public static QName get(String qualifiedName, String uri) {
        if (uri == null) {
            return QName.getCache().get(qualifiedName);
        }
        return QName.getCache().get(qualifiedName, uri);
    }

    public static QName get(String localName, Namespace namespace, String qualifiedName) {
        return QName.getCache().get(localName, namespace, qualifiedName);
    }

    public String getName() {
        return this.name;
    }

    public String getQualifiedName() {
        if (this.qualifiedName == null) {
            String prefix = this.getNamespacePrefix();
            this.qualifiedName = prefix != null && prefix.length() > 0 ? prefix + ":" + this.name : this.name;
        }
        return this.qualifiedName;
    }

    public Namespace getNamespace() {
        return this.namespace;
    }

    public String getNamespacePrefix() {
        if (this.namespace == null) {
            return "";
        }
        return this.namespace.getPrefix();
    }

    public String getNamespaceURI() {
        if (this.namespace == null) {
            return "";
        }
        return this.namespace.getURI();
    }

    public int hashCode() {
        if (this.hashCode == 0) {
            this.hashCode = this.getName().hashCode() ^ this.getNamespaceURI().hashCode();
            if (this.hashCode == 0) {
                this.hashCode = 47806;
            }
        }
        return this.hashCode;
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object instanceof QName) {
            QName that = (QName)object;
            if (this.hashCode() == that.hashCode()) {
                return this.getName().equals(that.getName()) && this.getNamespaceURI().equals(that.getNamespaceURI());
            }
        }
        return false;
    }

    public String toString() {
        return super.toString() + " [name: " + this.getName() + " namespace: \"" + this.getNamespace() + "\"]";
    }

    public DocumentFactory getDocumentFactory() {
        return this.documentFactory;
    }

    public void setDocumentFactory(DocumentFactory documentFactory) {
        this.documentFactory = documentFactory;
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeObject(this.namespace.getPrefix());
        out.writeObject(this.namespace.getURI());
        out.defaultWriteObject();
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        String prefix = (String)in.readObject();
        String uri = (String)in.readObject();
        in.defaultReadObject();
        this.namespace = Namespace.get(prefix, uri);
    }

    private static QNameCache getCache() {
        QNameCache cache = (QNameCache)singleton.instance();
        return cache;
    }

    static /* synthetic */ Class class$(String x0) {
        try {
            return Class.forName(x0);
        } catch (ClassNotFoundException x1) {
            throw new NoClassDefFoundError(x1.getMessage());
        }
    }

    static {
        try {
            String defaultSingletonClass = "org.dom4j.util.SimpleSingleton";
            Class<?> clazz = null;
            try {
                String singletonClass = defaultSingletonClass;
                singletonClass = System.getProperty("org.dom4j.QName.singleton.strategy", singletonClass);
                clazz = Class.forName(singletonClass);
            } catch (Exception exc1) {
                try {
                    String singletonClass = defaultSingletonClass;
                    clazz = Class.forName(singletonClass);
                } catch (Exception exception) {
                    // empty catch block
                }
            }
            singleton = (SingletonStrategy)clazz.newInstance();
            singleton.setSingletonClassName((class$org$dom4j$tree$QNameCache == null ? (class$org$dom4j$tree$QNameCache = QName.class$("org.dom4j.tree.QNameCache")) : class$org$dom4j$tree$QNameCache).getName());
        } catch (Exception exception) {
            // empty catch block
        }
    }
}

