/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.serialize;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.lib.SerializerFactory;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.serialize.CharacterMap;
import net.sf.saxon.serialize.CharacterMapExpander;
import net.sf.saxon.trans.XPathException;

public class CharacterMapIndex
implements Iterable<CharacterMap> {
    private HashMap<StructuredQName, CharacterMap> index = new HashMap(10);

    public CharacterMap getCharacterMap(StructuredQName name) {
        return this.index.get(name);
    }

    public void putCharacterMap(StructuredQName name, CharacterMap charMap) {
        this.index.put(name, charMap);
    }

    @Override
    public Iterator<CharacterMap> iterator() {
        return this.index.values().iterator();
    }

    public boolean isEmpty() {
        return this.index.isEmpty();
    }

    public CharacterMapIndex copy() {
        CharacterMapIndex copy = new CharacterMapIndex();
        copy.index = new HashMap<StructuredQName, CharacterMap>(this.index);
        return copy;
    }

    public CharacterMapExpander makeCharacterMapExpander(String useMaps, Receiver next, SerializerFactory sf) throws XPathException {
        CharacterMapExpander characterMapExpander = null;
        ArrayList<CharacterMap> characterMaps = new ArrayList<CharacterMap>(5);
        StringTokenizer st = new StringTokenizer(useMaps, " \t\n\r", false);
        while (st.hasMoreTokens()) {
            String expandedName = st.nextToken();
            StructuredQName qName = StructuredQName.fromClarkName(expandedName);
            CharacterMap map = this.getCharacterMap(qName);
            if (map == null) {
                throw new XPathException("Character map '" + expandedName + "' has not been defined", "SEPM0016");
            }
            characterMaps.add(map);
        }
        if (!characterMaps.isEmpty()) {
            characterMapExpander = sf.newCharacterMapExpander(next);
            if (characterMaps.size() == 1) {
                characterMapExpander.setCharacterMap((CharacterMap)characterMaps.get(0));
            } else {
                characterMapExpander.setCharacterMap(new CharacterMap(characterMaps));
            }
        }
        return characterMapExpander;
    }
}

