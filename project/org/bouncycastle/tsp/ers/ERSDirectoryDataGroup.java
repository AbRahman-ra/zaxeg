/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.tsp.ers;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import org.bouncycastle.tsp.ers.ERSData;
import org.bouncycastle.tsp.ers.ERSDataGroup;
import org.bouncycastle.tsp.ers.ERSFileData;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class ERSDirectoryDataGroup
extends ERSDataGroup {
    public ERSDirectoryDataGroup(File file) throws FileNotFoundException {
        super(ERSDirectoryDataGroup.buildGroup(file));
    }

    private static List<ERSData> buildGroup(File file) throws FileNotFoundException {
        if (file.isDirectory()) {
            File[] fileArray = file.listFiles();
            ArrayList<ERSData> arrayList = new ArrayList<ERSData>(fileArray.length);
            for (int i = 0; i != fileArray.length; ++i) {
                if (fileArray[i].isDirectory()) {
                    if (fileArray[i].listFiles().length == 0) continue;
                    arrayList.add(new ERSDirectoryDataGroup(fileArray[i]));
                    continue;
                }
                arrayList.add(new ERSFileData(fileArray[i]));
            }
            return arrayList;
        }
        throw new IllegalArgumentException("file reference does not refer to directory");
    }

    public List<ERSFileData> getFiles() {
        ArrayList<ERSFileData> arrayList = new ArrayList<ERSFileData>();
        for (int i = 0; i != this.dataObjects.size(); ++i) {
            if (!(this.dataObjects.get(i) instanceof ERSFileData)) continue;
            arrayList.add((ERSFileData)this.dataObjects.get(i));
        }
        return arrayList;
    }

    public List<ERSDirectoryDataGroup> getSubdirectories() {
        ArrayList<ERSDirectoryDataGroup> arrayList = new ArrayList<ERSDirectoryDataGroup>();
        for (int i = 0; i != this.dataObjects.size(); ++i) {
            if (!(this.dataObjects.get(i) instanceof ERSDirectoryDataGroup)) continue;
            arrayList.add((ERSDirectoryDataGroup)this.dataObjects.get(i));
        }
        return arrayList;
    }
}

