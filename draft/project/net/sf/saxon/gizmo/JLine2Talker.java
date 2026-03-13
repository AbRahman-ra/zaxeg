/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 * 
 * Could not load the following classes:
 *  jline.console.ConsoleReader
 *  jline.console.completer.Completer
 *  jline.console.completer.FileNameCompleter
 *  jline.console.completer.StringsCompleter
 *  jline.internal.Preconditions
 */
package net.sf.saxon.gizmo;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import jline.console.ConsoleReader;
import jline.console.completer.Completer;
import jline.console.completer.FileNameCompleter;
import jline.console.completer.StringsCompleter;
import jline.internal.Preconditions;
import net.sf.saxon.gizmo.Talker;

public class JLine2Talker
implements Talker {
    private static final boolean DEBUG = false;
    public static StringBuilder debugLog;
    private ConsoleReader console = new ConsoleReader(System.in, (OutputStream)System.out);
    private Completer completer;

    public JLine2Talker() throws IOException {
        this.console.setExpandEvents(false);
        this.console.setHistoryEnabled(true);
    }

    @Override
    public String exchange(String message) {
        try {
            String in;
            if (message != null && !message.isEmpty()) {
                this.console.println((CharSequence)message);
            }
            if ((in = this.console.readLine("/>")) == null) {
                System.exit(0);
            }
            return in.trim();
        } catch (IOException e) {
            System.err.println(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private static void log(String message) {
    }

    @Override
    public void setAutoCompletion(List<String> candidates) {
        if (this.completer != null) {
            this.console.removeCompleter(this.completer);
        }
        this.completer = new XPathCompleter(candidates);
        this.console.addCompleter(this.completer);
    }

    public static class XPathCompleter
    extends StringsCompleter {
        public XPathCompleter(List<String> candidates) {
            super(candidates);
        }

        public int complete(String buffer, int cursor, List<CharSequence> candidates) {
            String match;
            String command;
            final int space = buffer.indexOf(32);
            if (space > 0 && ((command = buffer.substring(0, space)).equals("load") || command.equals("save") || command.equals("transform") || command.equals("schema"))) {
                FileNameCompleter fnc = new FileNameCompleter(){

                    public int complete(String buffer, int cursor, List<CharSequence> candidates) {
                        String cwd;
                        Preconditions.checkNotNull(candidates);
                        if (buffer == null) {
                            buffer = "";
                        }
                        if (File.separator.equals("\\")) {
                            buffer = buffer.replace('/', '\\');
                        }
                        String translated = buffer.substring(space + 1);
                        File homeDir = this.getUserHome();
                        if (translated.startsWith("~" + this.separator())) {
                            translated = homeDir.getPath() + translated.substring(1);
                        } else if (translated.startsWith("~")) {
                            translated = homeDir.getParentFile().getAbsolutePath();
                        } else if (!translated.contains(this.separator())) {
                            cwd = this.getUserDir().getAbsolutePath();
                            translated = cwd + this.separator() + translated;
                        } else if (!new File(translated).isAbsolute()) {
                            cwd = this.getUserDir().getAbsolutePath();
                            translated = cwd + this.separator() + translated;
                        }
                        File file = new File(translated);
                        File dir = translated.endsWith(this.separator()) ? file : file.getParentFile();
                        File[] entries = dir == null ? new File[]{} : dir.listFiles();
                        int index = this.matchFiles(buffer, translated, entries, candidates);
                        return index == 0 ? space + 1 : index;
                    }
                };
                int index = fnc.complete(buffer, cursor, candidates);
                return index;
            }
            int lastDelimiter = Integer.max(cursor - 1, 0);
            if (lastDelimiter > space) {
                char c;
                while (lastDelimiter >= 0 && (Character.isAlphabetic(c = buffer.charAt(lastDelimiter)) || Character.isDigit(c) || c == '-' || c == '_' || c == '@' || c == '.' || c == ' ')) {
                    --lastDelimiter;
                }
            }
            String currentWord = buffer.substring(lastDelimiter + 1);
            Iterator iterator = ((SortedSet)this.getStrings()).tailSet(currentWord).iterator();
            while (iterator.hasNext() && (match = (String)iterator.next()).startsWith(currentWord)) {
                candidates.add(match);
            }
            return candidates.isEmpty() ? -1 : lastDelimiter + 1;
        }
    }
}

