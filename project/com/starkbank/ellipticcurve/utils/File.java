/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.starkbank.ellipticcurve.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class File {
    public static String read(String fileName) {
        String content = "";
        try {
            content = new String(Files.readAllBytes(Paths.get(fileName, new String[0])));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content;
    }

    public static byte[] readBytes(String fileName) {
        byte[] content = null;
        try {
            content = Files.readAllBytes(Paths.get(fileName, new String[0]));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content;
    }
}

