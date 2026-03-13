/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.tree.util;

import java.util.Arrays;
import net.sf.saxon.Configuration;
import net.sf.saxon.expr.parser.Loc;
import net.sf.saxon.om.AttributeInfo;
import net.sf.saxon.om.NameChecker;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.om.QNameException;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.SimpleType;
import org.xml.sax.Attributes;

public class AttributeCollectionImpl
implements Attributes {
    private Configuration config;
    private NodeName[] names = null;
    private String[] values = null;
    private Location[] locations = null;
    private int[] props = null;
    private int used = 0;
    private SimpleType[] types = null;

    public AttributeCollectionImpl(Configuration config, int initialSize) {
        this.config = config;
        this.names = new NodeName[initialSize];
        this.values = new String[initialSize];
        this.props = new int[initialSize];
        this.locations = new Location[initialSize];
        this.used = 0;
    }

    public void addAttribute(NodeName nodeName, SimpleType type, String value, Location locationId, int properties) {
        if (this.values == null) {
            this.names = new NodeName[5];
            this.values = new String[5];
            this.props = new int[5];
            this.locations = new Location[5];
            if (!type.equals(BuiltInAtomicType.UNTYPED_ATOMIC)) {
                this.types = new SimpleType[5];
            }
            this.used = 0;
        }
        if (this.values.length == this.used) {
            int newsize = this.used == 0 ? 5 : this.used * 2;
            this.names = Arrays.copyOf(this.names, newsize);
            this.values = Arrays.copyOf(this.values, newsize);
            this.props = Arrays.copyOf(this.props, newsize);
            this.locations = Arrays.copyOf(this.locations, newsize);
            if (this.types != null) {
                this.types = Arrays.copyOf(this.types, newsize);
            }
        }
        int n = this.used;
        this.names[n] = nodeName;
        this.props[n] = properties;
        this.locations[n] = locationId.saveLocation();
        this.setTypeAnnotation(n, type);
        this.values[this.used++] = value;
    }

    public void setAttribute(int index, NodeName nodeName, SimpleType type, String value, Location locationId, int properties) {
        this.names[index] = nodeName;
        this.props[index] = properties;
        this.locations[index] = locationId;
        this.setTypeAnnotation(index, type);
        this.values[index] = value;
    }

    @Override
    public int getLength() {
        return this.values == null ? 0 : this.used;
    }

    public SimpleType getTypeAnnotation(int index) {
        if (this.types == null) {
            return BuiltInAtomicType.UNTYPED_ATOMIC;
        }
        if (index < 0 || index >= this.used) {
            return BuiltInAtomicType.UNTYPED_ATOMIC;
        }
        return this.types[index];
    }

    public Location getLocation(int index) {
        if (this.locations == null) {
            return Loc.NONE;
        }
        if (index < 0 || index >= this.used) {
            return Loc.NONE;
        }
        return this.locations[index];
    }

    public int getProperties(int index) {
        if (this.props == null) {
            return 0;
        }
        if (index < 0 || index >= this.used) {
            return 0;
        }
        return this.props[index];
    }

    @Override
    public String getQName(int index) {
        if (this.names == null) {
            return null;
        }
        if (index < 0 || index >= this.used) {
            return null;
        }
        return this.names[index].getDisplayName();
    }

    @Override
    public String getLocalName(int index) {
        if (this.names == null) {
            return null;
        }
        if (index < 0 || index >= this.used) {
            return null;
        }
        return this.names[index].getLocalPart();
    }

    @Override
    public String getURI(int index) {
        if (this.names == null) {
            return null;
        }
        if (index < 0 || index >= this.used) {
            return null;
        }
        return this.names[index].getURI();
    }

    @Override
    public String getType(int index) {
        int typeCode = this.getTypeAnnotation(index).getFingerprint();
        switch (typeCode) {
            case 560: {
                return "ID";
            }
            case 561: {
                return "IDREF";
            }
            case 556: {
                return "NMTOKEN";
            }
            case 563: {
                return "ENTITY";
            }
            case 562: {
                return "IDREFS";
            }
            case 557: {
                return "NMTOKENS";
            }
            case 564: {
                return "ENTITIES";
            }
        }
        return "CDATA";
    }

    @Override
    public String getType(String uri, String localname) {
        int index = this.findByName(uri, localname);
        return index < 0 ? null : this.getType(index);
    }

    @Override
    public String getValue(int index) {
        if (this.values == null) {
            return null;
        }
        if (index < 0 || index >= this.used) {
            return null;
        }
        return this.values[index];
    }

    @Override
    public String getValue(String uri, String localname) {
        int index = this.findByName(uri, localname);
        return index < 0 ? null : this.getValue(index);
    }

    @Override
    public int getIndex(String qname) {
        String[] parts;
        if (this.names == null) {
            return -1;
        }
        if (qname.indexOf(58) < 0) {
            return this.findByName("", qname);
        }
        try {
            parts = NameChecker.getQNameParts(qname);
        } catch (QNameException err) {
            return -1;
        }
        String prefix = parts[0];
        if (prefix.isEmpty()) {
            return this.findByName("", qname);
        }
        String localName = parts[1];
        for (int i = 0; i < this.used; ++i) {
            if (this.names[i] == null) continue;
            String lname = this.names[i].getLocalPart();
            String ppref = this.names[i].getPrefix();
            if (!localName.equals(lname) || !prefix.equals(ppref)) continue;
            return i;
        }
        return -1;
    }

    @Override
    public int getIndex(String uri, String localname) {
        return this.findByName(uri, localname);
    }

    @Override
    public String getType(String name) {
        int index = this.getIndex(name);
        return this.getType(index);
    }

    @Override
    public String getValue(String name) {
        int index = this.getIndex(name);
        return this.getValue(index);
    }

    private int findByName(String uri, String localName) {
        if (this.names == null || this.config == null) {
            return -1;
        }
        for (int i = 0; i < this.used; ++i) {
            if (this.names[i] == null || !this.names[i].hasURI(uri) || !localName.equals(this.names[i].getLocalPart())) continue;
            return i;
        }
        return -1;
    }

    public void setTypeAnnotation(int index, SimpleType type) {
        if (type.equals(BuiltInAtomicType.UNTYPED_ATOMIC)) {
            if (this.types != null) {
                this.types[index] = type;
            }
        } else if (this.types == null) {
            this.types = new SimpleType[this.names.length];
            Arrays.fill(this.types, BuiltInAtomicType.UNTYPED_ATOMIC);
            this.types[index] = type;
        } else {
            this.types[index] = type;
        }
    }

    public void setAttribute(AttributeInfo attribute) {
        NodeName name = attribute.getNodeName();
        int index = this.getIndex(name.getURI(), name.getLocalPart());
        if (index < 0) {
            this.addAttribute(name, attribute.getType(), attribute.getValue(), attribute.getLocation(), attribute.getProperties());
        } else {
            this.setAttribute(index, name, attribute.getType(), attribute.getValue(), attribute.getLocation(), attribute.getProperties());
        }
    }
}

