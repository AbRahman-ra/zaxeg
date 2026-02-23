/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.om;

import net.sf.saxon.event.Builder;
import net.sf.saxon.event.PipelineConfiguration;
import net.sf.saxon.tree.linked.LinkedTreeBuilder;
import net.sf.saxon.tree.tiny.TinyBuilder;
import net.sf.saxon.tree.tiny.TinyBuilderCondensed;

public abstract class TreeModel {
    public static final TreeModel TINY_TREE = new TinyTree();
    public static final TreeModel TINY_TREE_CONDENSED = new TinyTreeCondensed();
    public static final TreeModel LINKED_TREE = new LinkedTree();

    public abstract Builder makeBuilder(PipelineConfiguration var1);

    public int getSymbolicValue() {
        return -1;
    }

    public static TreeModel getTreeModel(int symbolicValue) {
        switch (symbolicValue) {
            case 1: {
                return TINY_TREE;
            }
            case 2: {
                return TINY_TREE_CONDENSED;
            }
            case 0: {
                return LINKED_TREE;
            }
        }
        throw new IllegalArgumentException("tree model " + symbolicValue);
    }

    public boolean isMutable() {
        return false;
    }

    public boolean isSchemaAware() {
        return false;
    }

    public String getName() {
        return this.toString();
    }

    private static class LinkedTree
    extends TreeModel {
        private LinkedTree() {
        }

        @Override
        public Builder makeBuilder(PipelineConfiguration pipe) {
            return new LinkedTreeBuilder(pipe);
        }

        @Override
        public int getSymbolicValue() {
            return 0;
        }

        @Override
        public boolean isSchemaAware() {
            return true;
        }

        @Override
        public String getName() {
            return "LinkedTree";
        }
    }

    private static class TinyTreeCondensed
    extends TreeModel {
        private TinyTreeCondensed() {
        }

        @Override
        public Builder makeBuilder(PipelineConfiguration pipe) {
            TinyBuilderCondensed tbc = new TinyBuilderCondensed(pipe);
            tbc.setStatistics(pipe.getConfiguration().getTreeStatistics().SOURCE_DOCUMENT_STATISTICS);
            return tbc;
        }

        @Override
        public int getSymbolicValue() {
            return 2;
        }

        @Override
        public boolean isSchemaAware() {
            return true;
        }

        @Override
        public String getName() {
            return "TinyTreeCondensed";
        }
    }

    private static class TinyTree
    extends TreeModel {
        private TinyTree() {
        }

        @Override
        public Builder makeBuilder(PipelineConfiguration pipe) {
            TinyBuilder builder = new TinyBuilder(pipe);
            builder.setStatistics(pipe.getConfiguration().getTreeStatistics().SOURCE_DOCUMENT_STATISTICS);
            return builder;
        }

        @Override
        public int getSymbolicValue() {
            return 1;
        }

        @Override
        public boolean isSchemaAware() {
            return true;
        }

        @Override
        public String getName() {
            return "TinyTree";
        }
    }
}

