/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.lib;

import java.io.Writer;
import javax.xml.transform.stream.StreamResult;

public abstract class Logger {
    public static final int INFO = 0;
    public static final int WARNING = 1;
    public static final int ERROR = 2;
    public static final int DISASTER = 3;
    private boolean unicodeAware = false;

    public void info(String message) {
        this.println(message, 0);
    }

    public void warning(String message) {
        this.println(message, 1);
    }

    public void error(String message) {
        this.println(message, 2);
    }

    public void disaster(String message) {
        this.println(message, 3);
    }

    public abstract void println(String var1, int var2);

    public void close() {
    }

    public boolean isUnicodeAware() {
        return this.unicodeAware;
    }

    public void setUnicodeAware(boolean aware) {
        this.unicodeAware = aware;
    }

    public Writer asWriter() {
        return new Writer(){
            StringBuilder builder = new StringBuilder();

            @Override
            public void write(char[] cbuf, int off, int len) {
                for (int i = 0; i < len; ++i) {
                    char ch = cbuf[off + i];
                    if (ch == '\n') {
                        Logger.this.println(this.builder.toString(), 0);
                        this.builder.setLength(0);
                        continue;
                    }
                    this.builder.append(ch);
                }
            }

            @Override
            public void flush() {
                if (this.builder.length() > 0) {
                    Logger.this.println(this.builder.toString(), 0);
                    this.builder.setLength(0);
                }
            }

            @Override
            public void close() {
                this.flush();
            }
        };
    }

    public StreamResult asStreamResult() {
        return new StreamResult(this.asWriter());
    }
}

