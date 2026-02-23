/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.lib;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import javax.xml.transform.stream.StreamResult;
import net.sf.saxon.lib.Logger;

public class StandardLogger
extends Logger {
    private PrintStream out = System.err;
    private int threshold = 0;
    private boolean mustClose = false;

    public StandardLogger() {
    }

    public StandardLogger(PrintStream stream) {
        this.setPrintStream(stream);
    }

    public StandardLogger(File fileName) throws FileNotFoundException {
        this.setPrintStream(new PrintStream(fileName));
        this.mustClose = true;
    }

    public void setPrintStream(PrintStream stream) {
        this.out = stream;
    }

    public PrintStream getPrintStream() {
        return this.out;
    }

    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }

    public int getThreshold() {
        return this.threshold;
    }

    @Override
    public StreamResult asStreamResult() {
        return new StreamResult(this.out);
    }

    @Override
    public void println(String message, int severity) {
        if (severity >= this.threshold) {
            this.out.println(message);
        }
    }

    @Override
    public void close() {
        if (this.mustClose) {
            this.out.close();
        }
    }
}

