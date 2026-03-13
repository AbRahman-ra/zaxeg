/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.pqc.crypto.xmss;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.TreeMap;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.pqc.crypto.xmss.BDSTreeHash;
import org.bouncycastle.pqc.crypto.xmss.HashTreeAddress;
import org.bouncycastle.pqc.crypto.xmss.LTreeAddress;
import org.bouncycastle.pqc.crypto.xmss.OTSHashAddress;
import org.bouncycastle.pqc.crypto.xmss.WOTSPlus;
import org.bouncycastle.pqc.crypto.xmss.WOTSPlusParameters;
import org.bouncycastle.pqc.crypto.xmss.WOTSPlusPublicKeyParameters;
import org.bouncycastle.pqc.crypto.xmss.XMSSNode;
import org.bouncycastle.pqc.crypto.xmss.XMSSNodeUtil;
import org.bouncycastle.pqc.crypto.xmss.XMSSParameters;
import org.bouncycastle.pqc.crypto.xmss.XMSSUtil;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public final class BDS
implements Serializable {
    private static final long serialVersionUID = 1L;
    private transient WOTSPlus wotsPlus;
    private final int treeHeight;
    private final List<BDSTreeHash> treeHashInstances;
    private int k;
    private XMSSNode root;
    private List<XMSSNode> authenticationPath;
    private Map<Integer, LinkedList<XMSSNode>> retain;
    private Stack<XMSSNode> stack;
    private Map<Integer, XMSSNode> keep;
    private int index;
    private boolean used;
    private transient int maxIndex;

    BDS(XMSSParameters xMSSParameters, int n, int n2) {
        this(xMSSParameters.getWOTSPlus(), xMSSParameters.getHeight(), xMSSParameters.getK(), n2);
        this.maxIndex = n;
        this.index = n2;
        this.used = true;
    }

    BDS(XMSSParameters xMSSParameters, byte[] byArray, byte[] byArray2, OTSHashAddress oTSHashAddress) {
        this(xMSSParameters.getWOTSPlus(), xMSSParameters.getHeight(), xMSSParameters.getK(), (1 << xMSSParameters.getHeight()) - 1);
        this.initialize(byArray, byArray2, oTSHashAddress);
    }

    BDS(XMSSParameters xMSSParameters, byte[] byArray, byte[] byArray2, OTSHashAddress oTSHashAddress, int n) {
        this(xMSSParameters.getWOTSPlus(), xMSSParameters.getHeight(), xMSSParameters.getK(), (1 << xMSSParameters.getHeight()) - 1);
        this.initialize(byArray, byArray2, oTSHashAddress);
        while (this.index < n) {
            this.nextAuthenticationPath(byArray, byArray2, oTSHashAddress);
            this.used = false;
        }
    }

    private BDS(WOTSPlus wOTSPlus, int n, int n2, int n3) {
        this.wotsPlus = wOTSPlus;
        this.treeHeight = n;
        this.maxIndex = n3;
        this.k = n2;
        if (n2 > n || n2 < 2 || (n - n2) % 2 != 0) {
            throw new IllegalArgumentException("illegal value for BDS parameter k");
        }
        this.authenticationPath = new ArrayList<XMSSNode>();
        this.retain = new TreeMap<Integer, LinkedList<XMSSNode>>();
        this.stack = new Stack();
        this.treeHashInstances = new ArrayList<BDSTreeHash>();
        for (int i = 0; i < n - n2; ++i) {
            this.treeHashInstances.add(new BDSTreeHash(i));
        }
        this.keep = new TreeMap<Integer, XMSSNode>();
        this.index = 0;
        this.used = false;
    }

    BDS(BDS bDS) {
        this.wotsPlus = new WOTSPlus(bDS.wotsPlus.getParams());
        this.treeHeight = bDS.treeHeight;
        this.k = bDS.k;
        this.root = bDS.root;
        this.authenticationPath = new ArrayList<XMSSNode>();
        this.authenticationPath.addAll(bDS.authenticationPath);
        this.retain = new TreeMap<Integer, LinkedList<XMSSNode>>();
        for (Integer n : bDS.retain.keySet()) {
            this.retain.put(n, (LinkedList)bDS.retain.get(n).clone());
        }
        this.stack = new Stack();
        this.stack.addAll(bDS.stack);
        this.treeHashInstances = new ArrayList<BDSTreeHash>();
        Iterator<Serializable> iterator = bDS.treeHashInstances.iterator();
        while (iterator.hasNext()) {
            this.treeHashInstances.add(((BDSTreeHash)iterator.next()).clone());
        }
        this.keep = new TreeMap<Integer, XMSSNode>(bDS.keep);
        this.index = bDS.index;
        this.maxIndex = bDS.maxIndex;
        this.used = bDS.used;
    }

    private BDS(BDS bDS, byte[] byArray, byte[] byArray2, OTSHashAddress oTSHashAddress) {
        this.wotsPlus = new WOTSPlus(bDS.wotsPlus.getParams());
        this.treeHeight = bDS.treeHeight;
        this.k = bDS.k;
        this.root = bDS.root;
        this.authenticationPath = new ArrayList<XMSSNode>();
        this.authenticationPath.addAll(bDS.authenticationPath);
        this.retain = new TreeMap<Integer, LinkedList<XMSSNode>>();
        for (Integer n : bDS.retain.keySet()) {
            this.retain.put(n, (LinkedList)bDS.retain.get(n).clone());
        }
        this.stack = new Stack();
        this.stack.addAll(bDS.stack);
        this.treeHashInstances = new ArrayList<BDSTreeHash>();
        Iterator<Serializable> iterator = bDS.treeHashInstances.iterator();
        while (iterator.hasNext()) {
            this.treeHashInstances.add(((BDSTreeHash)iterator.next()).clone());
        }
        this.keep = new TreeMap<Integer, XMSSNode>(bDS.keep);
        this.index = bDS.index;
        this.maxIndex = bDS.maxIndex;
        this.used = false;
        this.nextAuthenticationPath(byArray, byArray2, oTSHashAddress);
    }

    private BDS(BDS bDS, ASN1ObjectIdentifier aSN1ObjectIdentifier) {
        this.wotsPlus = new WOTSPlus(new WOTSPlusParameters(aSN1ObjectIdentifier));
        this.treeHeight = bDS.treeHeight;
        this.k = bDS.k;
        this.root = bDS.root;
        this.authenticationPath = new ArrayList<XMSSNode>();
        this.authenticationPath.addAll(bDS.authenticationPath);
        this.retain = new TreeMap<Integer, LinkedList<XMSSNode>>();
        for (Integer n : bDS.retain.keySet()) {
            this.retain.put(n, (LinkedList)bDS.retain.get(n).clone());
        }
        this.stack = new Stack();
        this.stack.addAll(bDS.stack);
        this.treeHashInstances = new ArrayList<BDSTreeHash>();
        Iterator<Serializable> iterator = bDS.treeHashInstances.iterator();
        while (iterator.hasNext()) {
            this.treeHashInstances.add(((BDSTreeHash)iterator.next()).clone());
        }
        this.keep = new TreeMap<Integer, XMSSNode>(bDS.keep);
        this.index = bDS.index;
        this.maxIndex = bDS.maxIndex;
        this.used = bDS.used;
        this.validate();
    }

    private BDS(BDS bDS, int n, ASN1ObjectIdentifier aSN1ObjectIdentifier) {
        this.wotsPlus = new WOTSPlus(new WOTSPlusParameters(aSN1ObjectIdentifier));
        this.treeHeight = bDS.treeHeight;
        this.k = bDS.k;
        this.root = bDS.root;
        this.authenticationPath = new ArrayList<XMSSNode>();
        this.authenticationPath.addAll(bDS.authenticationPath);
        this.retain = new TreeMap<Integer, LinkedList<XMSSNode>>();
        for (Integer n2 : bDS.retain.keySet()) {
            this.retain.put(n2, (LinkedList)bDS.retain.get(n2).clone());
        }
        this.stack = new Stack();
        this.stack.addAll(bDS.stack);
        this.treeHashInstances = new ArrayList<BDSTreeHash>();
        Iterator<Serializable> iterator = bDS.treeHashInstances.iterator();
        while (iterator.hasNext()) {
            this.treeHashInstances.add(((BDSTreeHash)iterator.next()).clone());
        }
        this.keep = new TreeMap<Integer, XMSSNode>(bDS.keep);
        this.index = bDS.index;
        this.maxIndex = n;
        this.used = bDS.used;
        this.validate();
    }

    public BDS getNextState(byte[] byArray, byte[] byArray2, OTSHashAddress oTSHashAddress) {
        return new BDS(this, byArray, byArray2, oTSHashAddress);
    }

    private void initialize(byte[] byArray, byte[] byArray2, OTSHashAddress oTSHashAddress) {
        if (oTSHashAddress == null) {
            throw new NullPointerException("otsHashAddress == null");
        }
        LTreeAddress lTreeAddress = (LTreeAddress)((LTreeAddress.Builder)((LTreeAddress.Builder)new LTreeAddress.Builder().withLayerAddress(oTSHashAddress.getLayerAddress())).withTreeAddress(oTSHashAddress.getTreeAddress())).build();
        HashTreeAddress hashTreeAddress = (HashTreeAddress)((HashTreeAddress.Builder)((HashTreeAddress.Builder)new HashTreeAddress.Builder().withLayerAddress(oTSHashAddress.getLayerAddress())).withTreeAddress(oTSHashAddress.getTreeAddress())).build();
        for (int i = 0; i < 1 << this.treeHeight; ++i) {
            oTSHashAddress = (OTSHashAddress)((OTSHashAddress.Builder)((OTSHashAddress.Builder)((OTSHashAddress.Builder)new OTSHashAddress.Builder().withLayerAddress(oTSHashAddress.getLayerAddress())).withTreeAddress(oTSHashAddress.getTreeAddress())).withOTSAddress(i).withChainAddress(oTSHashAddress.getChainAddress()).withHashAddress(oTSHashAddress.getHashAddress()).withKeyAndMask(oTSHashAddress.getKeyAndMask())).build();
            this.wotsPlus.importKeys(this.wotsPlus.getWOTSPlusSecretKey(byArray2, oTSHashAddress), byArray);
            WOTSPlusPublicKeyParameters wOTSPlusPublicKeyParameters = this.wotsPlus.getPublicKey(oTSHashAddress);
            lTreeAddress = (LTreeAddress)((LTreeAddress.Builder)((LTreeAddress.Builder)((LTreeAddress.Builder)new LTreeAddress.Builder().withLayerAddress(lTreeAddress.getLayerAddress())).withTreeAddress(lTreeAddress.getTreeAddress())).withLTreeAddress(i).withTreeHeight(lTreeAddress.getTreeHeight()).withTreeIndex(lTreeAddress.getTreeIndex()).withKeyAndMask(lTreeAddress.getKeyAndMask())).build();
            XMSSNode xMSSNode = XMSSNodeUtil.lTree(this.wotsPlus, wOTSPlusPublicKeyParameters, lTreeAddress);
            hashTreeAddress = (HashTreeAddress)((HashTreeAddress.Builder)((HashTreeAddress.Builder)((HashTreeAddress.Builder)new HashTreeAddress.Builder().withLayerAddress(hashTreeAddress.getLayerAddress())).withTreeAddress(hashTreeAddress.getTreeAddress())).withTreeIndex(i).withKeyAndMask(hashTreeAddress.getKeyAndMask())).build();
            while (!this.stack.isEmpty() && this.stack.peek().getHeight() == xMSSNode.getHeight()) {
                int n = i / (1 << xMSSNode.getHeight());
                if (n == 1) {
                    this.authenticationPath.add(xMSSNode);
                }
                if (n == 3 && xMSSNode.getHeight() < this.treeHeight - this.k) {
                    this.treeHashInstances.get(xMSSNode.getHeight()).setNode(xMSSNode);
                }
                if (n >= 3 && (n & 1) == 1 && xMSSNode.getHeight() >= this.treeHeight - this.k && xMSSNode.getHeight() <= this.treeHeight - 2) {
                    if (this.retain.get(xMSSNode.getHeight()) == null) {
                        LinkedList<XMSSNode> linkedList = new LinkedList<XMSSNode>();
                        linkedList.add(xMSSNode);
                        this.retain.put(xMSSNode.getHeight(), linkedList);
                    } else {
                        this.retain.get(xMSSNode.getHeight()).add(xMSSNode);
                    }
                }
                hashTreeAddress = (HashTreeAddress)((HashTreeAddress.Builder)((HashTreeAddress.Builder)((HashTreeAddress.Builder)new HashTreeAddress.Builder().withLayerAddress(hashTreeAddress.getLayerAddress())).withTreeAddress(hashTreeAddress.getTreeAddress())).withTreeHeight(hashTreeAddress.getTreeHeight()).withTreeIndex((hashTreeAddress.getTreeIndex() - 1) / 2).withKeyAndMask(hashTreeAddress.getKeyAndMask())).build();
                xMSSNode = XMSSNodeUtil.randomizeHash(this.wotsPlus, this.stack.pop(), xMSSNode, hashTreeAddress);
                xMSSNode = new XMSSNode(xMSSNode.getHeight() + 1, xMSSNode.getValue());
                hashTreeAddress = (HashTreeAddress)((HashTreeAddress.Builder)((HashTreeAddress.Builder)((HashTreeAddress.Builder)new HashTreeAddress.Builder().withLayerAddress(hashTreeAddress.getLayerAddress())).withTreeAddress(hashTreeAddress.getTreeAddress())).withTreeHeight(hashTreeAddress.getTreeHeight() + 1).withTreeIndex(hashTreeAddress.getTreeIndex()).withKeyAndMask(hashTreeAddress.getKeyAndMask())).build();
            }
            this.stack.push(xMSSNode);
        }
        this.root = this.stack.pop();
    }

    private void nextAuthenticationPath(byte[] byArray, byte[] byArray2, OTSHashAddress oTSHashAddress) {
        Serializable serializable;
        Object object;
        if (oTSHashAddress == null) {
            throw new NullPointerException("otsHashAddress == null");
        }
        if (this.used) {
            throw new IllegalStateException("index already used");
        }
        if (this.index > this.maxIndex - 1) {
            throw new IllegalStateException("index out of bounds");
        }
        int n = XMSSUtil.calculateTau(this.index, this.treeHeight);
        if ((this.index >> n + 1 & 1) == 0 && n < this.treeHeight - 1) {
            this.keep.put(n, this.authenticationPath.get(n));
        }
        LTreeAddress lTreeAddress = (LTreeAddress)((LTreeAddress.Builder)((LTreeAddress.Builder)new LTreeAddress.Builder().withLayerAddress(oTSHashAddress.getLayerAddress())).withTreeAddress(oTSHashAddress.getTreeAddress())).build();
        HashTreeAddress hashTreeAddress = (HashTreeAddress)((HashTreeAddress.Builder)((HashTreeAddress.Builder)new HashTreeAddress.Builder().withLayerAddress(oTSHashAddress.getLayerAddress())).withTreeAddress(oTSHashAddress.getTreeAddress())).build();
        if (n == 0) {
            oTSHashAddress = (OTSHashAddress)((OTSHashAddress.Builder)((OTSHashAddress.Builder)((OTSHashAddress.Builder)new OTSHashAddress.Builder().withLayerAddress(oTSHashAddress.getLayerAddress())).withTreeAddress(oTSHashAddress.getTreeAddress())).withOTSAddress(this.index).withChainAddress(oTSHashAddress.getChainAddress()).withHashAddress(oTSHashAddress.getHashAddress()).withKeyAndMask(oTSHashAddress.getKeyAndMask())).build();
            this.wotsPlus.importKeys(this.wotsPlus.getWOTSPlusSecretKey(byArray2, oTSHashAddress), byArray);
            object = this.wotsPlus.getPublicKey(oTSHashAddress);
            lTreeAddress = (LTreeAddress)((LTreeAddress.Builder)((LTreeAddress.Builder)((LTreeAddress.Builder)new LTreeAddress.Builder().withLayerAddress(lTreeAddress.getLayerAddress())).withTreeAddress(lTreeAddress.getTreeAddress())).withLTreeAddress(this.index).withTreeHeight(lTreeAddress.getTreeHeight()).withTreeIndex(lTreeAddress.getTreeIndex()).withKeyAndMask(lTreeAddress.getKeyAndMask())).build();
            serializable = XMSSNodeUtil.lTree(this.wotsPlus, (WOTSPlusPublicKeyParameters)object, lTreeAddress);
            this.authenticationPath.set(0, (XMSSNode)serializable);
        } else {
            int n2;
            hashTreeAddress = (HashTreeAddress)((HashTreeAddress.Builder)((HashTreeAddress.Builder)((HashTreeAddress.Builder)new HashTreeAddress.Builder().withLayerAddress(hashTreeAddress.getLayerAddress())).withTreeAddress(hashTreeAddress.getTreeAddress())).withTreeHeight(n - 1).withTreeIndex(this.index >> n).withKeyAndMask(hashTreeAddress.getKeyAndMask())).build();
            this.wotsPlus.importKeys(this.wotsPlus.getWOTSPlusSecretKey(byArray2, oTSHashAddress), byArray);
            object = XMSSNodeUtil.randomizeHash(this.wotsPlus, this.authenticationPath.get(n - 1), this.keep.get(n - 1), hashTreeAddress);
            object = new XMSSNode(((XMSSNode)object).getHeight() + 1, ((XMSSNode)object).getValue());
            this.authenticationPath.set(n, (XMSSNode)object);
            this.keep.remove(n - 1);
            for (n2 = 0; n2 < n; ++n2) {
                if (n2 < this.treeHeight - this.k) {
                    this.authenticationPath.set(n2, this.treeHashInstances.get(n2).getTailNode());
                    continue;
                }
                this.authenticationPath.set(n2, this.retain.get(n2).removeFirst());
            }
            n2 = Math.min(n, this.treeHeight - this.k);
            for (int i = 0; i < n2; ++i) {
                int n3 = this.index + 1 + 3 * (1 << i);
                if (n3 >= 1 << this.treeHeight) continue;
                this.treeHashInstances.get(i).initialize(n3);
            }
        }
        for (int i = 0; i < this.treeHeight - this.k >> 1; ++i) {
            serializable = this.getBDSTreeHashInstanceForUpdate();
            if (serializable == null) continue;
            ((BDSTreeHash)serializable).update(this.stack, this.wotsPlus, byArray, byArray2, oTSHashAddress);
        }
        ++this.index;
    }

    boolean isUsed() {
        return this.used;
    }

    void markUsed() {
        this.used = true;
    }

    private BDSTreeHash getBDSTreeHashInstanceForUpdate() {
        BDSTreeHash bDSTreeHash = null;
        for (BDSTreeHash bDSTreeHash2 : this.treeHashInstances) {
            if (bDSTreeHash2.isFinished() || !bDSTreeHash2.isInitialized()) continue;
            if (bDSTreeHash == null) {
                bDSTreeHash = bDSTreeHash2;
                continue;
            }
            if (bDSTreeHash2.getHeight() < bDSTreeHash.getHeight()) {
                bDSTreeHash = bDSTreeHash2;
                continue;
            }
            if (bDSTreeHash2.getHeight() != bDSTreeHash.getHeight() || bDSTreeHash2.getIndexLeaf() >= bDSTreeHash.getIndexLeaf()) continue;
            bDSTreeHash = bDSTreeHash2;
        }
        return bDSTreeHash;
    }

    private void validate() {
        if (this.authenticationPath == null) {
            throw new IllegalStateException("authenticationPath == null");
        }
        if (this.retain == null) {
            throw new IllegalStateException("retain == null");
        }
        if (this.stack == null) {
            throw new IllegalStateException("stack == null");
        }
        if (this.treeHashInstances == null) {
            throw new IllegalStateException("treeHashInstances == null");
        }
        if (this.keep == null) {
            throw new IllegalStateException("keep == null");
        }
        if (!XMSSUtil.isIndexValid(this.treeHeight, this.index)) {
            throw new IllegalStateException("index in BDS state out of bounds");
        }
    }

    protected int getTreeHeight() {
        return this.treeHeight;
    }

    protected XMSSNode getRoot() {
        return this.root;
    }

    protected List<XMSSNode> getAuthenticationPath() {
        ArrayList<XMSSNode> arrayList = new ArrayList<XMSSNode>();
        for (XMSSNode xMSSNode : this.authenticationPath) {
            arrayList.add(xMSSNode);
        }
        return arrayList;
    }

    protected int getIndex() {
        return this.index;
    }

    public int getMaxIndex() {
        return this.maxIndex;
    }

    public BDS withWOTSDigest(ASN1ObjectIdentifier aSN1ObjectIdentifier) {
        return new BDS(this, aSN1ObjectIdentifier);
    }

    public BDS withMaxIndex(int n, ASN1ObjectIdentifier aSN1ObjectIdentifier) {
        return new BDS(this, n, aSN1ObjectIdentifier);
    }

    private void readObject(ObjectInputStream objectInputStream) throws IOException, ClassNotFoundException {
        objectInputStream.defaultReadObject();
        this.maxIndex = objectInputStream.available() != 0 ? objectInputStream.readInt() : (1 << this.treeHeight) - 1;
        if (this.maxIndex > (1 << this.treeHeight) - 1 || this.index > this.maxIndex + 1 || objectInputStream.available() != 0) {
            throw new IOException("inconsistent BDS data detected");
        }
    }

    private void writeObject(ObjectOutputStream objectOutputStream) throws IOException {
        objectOutputStream.defaultWriteObject();
        objectOutputStream.writeInt(this.maxIndex);
    }
}

