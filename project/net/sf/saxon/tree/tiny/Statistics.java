/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.tree.tiny;

public class Statistics {
    private int treesCreated = 5;
    private double averageNodes = 4000.0;
    private double averageAttributes = 100.0;
    private double averageNamespaces = 20.0;
    private double averageCharacters = 4000.0;

    public Statistics() {
    }

    public Statistics(int nodes, int atts, int namespaces, int chars) {
        this.averageNodes = nodes;
        this.averageAttributes = atts;
        this.averageNamespaces = namespaces;
        this.averageCharacters = chars;
    }

    public double getAverageNodes() {
        return this.averageNodes;
    }

    public double getAverageAttributes() {
        return this.averageAttributes;
    }

    public double getAverageNamespaces() {
        return this.averageNamespaces;
    }

    public double getAverageCharacters() {
        return this.averageCharacters;
    }

    public synchronized void updateStatistics(int numberOfNodes, int numberOfAttributes, int numberOfNamespaces, int chars) {
        int n0 = this.treesCreated;
        if (n0 < 1000000) {
            int n1;
            this.treesCreated = n1 = this.treesCreated + 1;
            this.averageNodes = (this.averageNodes * (double)n0 + (double)numberOfNodes) / (double)n1;
            if (this.averageNodes < 10.0) {
                this.averageNodes = 10.0;
            }
            this.averageAttributes = (this.averageAttributes * (double)n0 + (double)numberOfAttributes) / (double)n1;
            if (this.averageAttributes < 10.0) {
                this.averageAttributes = 10.0;
            }
            this.averageNamespaces = (this.averageNamespaces * (double)n0 + (double)numberOfNamespaces) / (double)n1;
            if (this.averageNamespaces < 5.0) {
                this.averageNamespaces = 5.0;
            }
            this.averageCharacters = (this.averageCharacters * (double)n0 + (double)chars) / (double)n1;
            if (this.averageCharacters < 100.0) {
                this.averageCharacters = 100.0;
            }
        }
    }

    public String toString() {
        return this.treesCreated + "(" + this.averageNodes + "," + this.averageAttributes + "," + this.averageNamespaces + "," + this.averageCharacters + ")";
    }
}

