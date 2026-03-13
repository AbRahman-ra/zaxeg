/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.gizmo;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.NoSuchElementException;
import java.util.Scanner;
import net.sf.saxon.gizmo.Talker;

public class DefaultTalker
implements Talker {
    private Scanner in;
    private PrintStream output;

    public DefaultTalker() {
        new DefaultTalker(System.in, System.out);
    }

    public DefaultTalker(InputStream in, PrintStream out) {
        this.in = new Scanner(in);
        this.output = out;
    }

    @Override
    public String exchange(String message) {
        if (message != null && !message.isEmpty()) {
            this.output.println(message);
        }
        try {
            String response = this.in.nextLine();
            if (response == null) {
                return "";
            }
            return response.trim();
        } catch (NoSuchElementException e) {
            return "";
        }
    }
}

