/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.dom4j.tree;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import org.dom4j.Element;
import org.dom4j.ProcessingInstruction;
import org.dom4j.Visitor;
import org.dom4j.tree.AbstractNode;

public abstract class AbstractProcessingInstruction
extends AbstractNode
implements ProcessingInstruction {
    public short getNodeType() {
        return 7;
    }

    public String getPath(Element context) {
        Element parent = this.getParent();
        return parent != null && parent != context ? parent.getPath(context) + "/processing-instruction()" : "processing-instruction()";
    }

    public String getUniquePath(Element context) {
        Element parent = this.getParent();
        return parent != null && parent != context ? parent.getUniquePath(context) + "/processing-instruction()" : "processing-instruction()";
    }

    public String toString() {
        return super.toString() + " [ProcessingInstruction: &" + this.getName() + ";]";
    }

    public String asXML() {
        return "<?" + this.getName() + " " + this.getText() + "?>";
    }

    public void write(Writer writer) throws IOException {
        writer.write("<?");
        writer.write(this.getName());
        writer.write(" ");
        writer.write(this.getText());
        writer.write("?>");
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public void setValue(String name, String value) {
        throw new UnsupportedOperationException("This PI is read-only and cannot be modified");
    }

    public void setValues(Map data) {
        throw new UnsupportedOperationException("This PI is read-only and cannot be modified");
    }

    public String getName() {
        return this.getTarget();
    }

    public void setName(String name) {
        this.setTarget(name);
    }

    public boolean removeValue(String name) {
        return false;
    }

    protected String toString(Map values) {
        StringBuffer buffer = new StringBuffer();
        Iterator iter = values.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = iter.next();
            String name = (String)entry.getKey();
            String value = (String)entry.getValue();
            buffer.append(name);
            buffer.append("=\"");
            buffer.append(value);
            buffer.append("\" ");
        }
        buffer.setLength(buffer.length() - 1);
        return buffer.toString();
    }

    protected Map parseValues(String text) {
        HashMap<String, String> data = new HashMap<String, String>();
        StringTokenizer s = new StringTokenizer(text, " ='\"", true);
        while (s.hasMoreTokens()) {
            String name = this.getName(s);
            if (!s.hasMoreTokens()) continue;
            String value = this.getValue(s);
            data.put(name, value);
        }
        return data;
    }

    private String getName(StringTokenizer tokenizer) {
        String token = tokenizer.nextToken();
        StringBuffer name = new StringBuffer(token);
        while (tokenizer.hasMoreTokens() && !(token = tokenizer.nextToken()).equals("=")) {
            name.append(token);
        }
        return name.toString().trim();
    }

    private String getValue(StringTokenizer tokenizer) {
        String token = tokenizer.nextToken();
        StringBuffer value = new StringBuffer();
        while (tokenizer.hasMoreTokens() && !token.equals("'") && !token.equals("\"")) {
            token = tokenizer.nextToken();
        }
        String quote = token;
        while (tokenizer.hasMoreTokens() && !quote.equals(token = tokenizer.nextToken())) {
            value.append(token);
        }
        return value.toString();
    }
}

