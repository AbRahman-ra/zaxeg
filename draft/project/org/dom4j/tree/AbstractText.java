/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.dom4j.tree;

import java.io.IOException;
import java.io.Writer;
import org.dom4j.Text;
import org.dom4j.Visitor;
import org.dom4j.tree.AbstractCharacterData;

public abstract class AbstractText
extends AbstractCharacterData
implements Text {
    public short getNodeType() {
        return 3;
    }

    public String toString() {
        return super.toString() + " [Text: \"" + this.getText() + "\"]";
    }

    public String asXML() {
        return this.getText();
    }

    public void write(Writer writer) throws IOException {
        writer.write(this.getText());
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}

