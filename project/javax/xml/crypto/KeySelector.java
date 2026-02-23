/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package javax.xml.crypto;

import java.security.Key;
import javax.xml.crypto.AlgorithmMethod;
import javax.xml.crypto.KeySelectorException;
import javax.xml.crypto.KeySelectorResult;
import javax.xml.crypto.XMLCryptoContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;

public abstract class KeySelector {
    protected KeySelector() {
    }

    public abstract KeySelectorResult select(KeyInfo var1, Purpose var2, AlgorithmMethod var3, XMLCryptoContext var4) throws KeySelectorException;

    public static KeySelector singletonKeySelector(Key key) {
        return new SingletonKeySelector(key);
    }

    private static class SingletonKeySelector
    extends KeySelector {
        private final Key key;

        SingletonKeySelector(Key key) {
            if (key == null) {
                throw new NullPointerException();
            }
            this.key = key;
        }

        public KeySelectorResult select(KeyInfo keyInfo, Purpose purpose, AlgorithmMethod method, XMLCryptoContext context) throws KeySelectorException {
            return new KeySelectorResult(){

                public Key getKey() {
                    return SingletonKeySelector.this.key;
                }
            };
        }
    }

    public static class Purpose {
        private final String name;
        public static final Purpose SIGN = new Purpose("sign");
        public static final Purpose VERIFY = new Purpose("verify");
        public static final Purpose ENCRYPT = new Purpose("encrypt");
        public static final Purpose DECRYPT = new Purpose("decrypt");

        private Purpose(String name) {
            this.name = name;
        }

        public String toString() {
            return this.name;
        }
    }
}

