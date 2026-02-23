/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.jaxen.saxpath.base;

final class Verifier {
    Verifier() {
    }

    static boolean isXMLNCNameCharacter(char c) {
        return Verifier.isXMLLetter(c) || Verifier.isXMLDigit(c) || c == '.' || c == '-' || c == '_' || Verifier.isXMLCombiningChar(c) || Verifier.isXMLExtender(c);
    }

    static boolean isXMLNCNameStartCharacter(char c) {
        return Verifier.isXMLLetter(c) || c == '_';
    }

    static boolean isXMLLetter(char c) {
        if (c < 'A') {
            return false;
        }
        if (c <= 'Z') {
            return true;
        }
        if (c < 'a') {
            return false;
        }
        if (c <= 'z') {
            return true;
        }
        if (c < '\u00c0') {
            return false;
        }
        if (c <= '\u00d6') {
            return true;
        }
        if (c < '\u00d8') {
            return false;
        }
        if (c <= '\u00f6') {
            return true;
        }
        if (c < '\u00f8') {
            return false;
        }
        if (c <= '\u00ff') {
            return true;
        }
        if (c < '\u0100') {
            return false;
        }
        if (c <= '\u0131') {
            return true;
        }
        if (c < '\u0134') {
            return false;
        }
        if (c <= '\u013e') {
            return true;
        }
        if (c < '\u0141') {
            return false;
        }
        if (c <= '\u0148') {
            return true;
        }
        if (c < '\u014a') {
            return false;
        }
        if (c <= '\u017e') {
            return true;
        }
        if (c < '\u0180') {
            return false;
        }
        if (c <= '\u01c3') {
            return true;
        }
        if (c < '\u01cd') {
            return false;
        }
        if (c <= '\u01f0') {
            return true;
        }
        if (c < '\u01f4') {
            return false;
        }
        if (c <= '\u01f5') {
            return true;
        }
        if (c < '\u01fa') {
            return false;
        }
        if (c <= '\u0217') {
            return true;
        }
        if (c < '\u0250') {
            return false;
        }
        if (c <= '\u02a8') {
            return true;
        }
        if (c < '\u02bb') {
            return false;
        }
        if (c <= '\u02c1') {
            return true;
        }
        if (c == '\u0386') {
            return true;
        }
        if (c < '\u0388') {
            return false;
        }
        if (c <= '\u038a') {
            return true;
        }
        if (c == '\u038c') {
            return true;
        }
        if (c < '\u038e') {
            return false;
        }
        if (c <= '\u03a1') {
            return true;
        }
        if (c < '\u03a3') {
            return false;
        }
        if (c <= '\u03ce') {
            return true;
        }
        if (c < '\u03d0') {
            return false;
        }
        if (c <= '\u03d6') {
            return true;
        }
        if (c == '\u03da') {
            return true;
        }
        if (c == '\u03dc') {
            return true;
        }
        if (c == '\u03de') {
            return true;
        }
        if (c == '\u03e0') {
            return true;
        }
        if (c < '\u03e2') {
            return false;
        }
        if (c <= '\u03f3') {
            return true;
        }
        if (c < '\u0401') {
            return false;
        }
        if (c <= '\u040c') {
            return true;
        }
        if (c < '\u040e') {
            return false;
        }
        if (c <= '\u044f') {
            return true;
        }
        if (c < '\u0451') {
            return false;
        }
        if (c <= '\u045c') {
            return true;
        }
        if (c < '\u045e') {
            return false;
        }
        if (c <= '\u0481') {
            return true;
        }
        if (c < '\u0490') {
            return false;
        }
        if (c <= '\u04c4') {
            return true;
        }
        if (c < '\u04c7') {
            return false;
        }
        if (c <= '\u04c8') {
            return true;
        }
        if (c < '\u04cb') {
            return false;
        }
        if (c <= '\u04cc') {
            return true;
        }
        if (c < '\u04d0') {
            return false;
        }
        if (c <= '\u04eb') {
            return true;
        }
        if (c < '\u04ee') {
            return false;
        }
        if (c <= '\u04f5') {
            return true;
        }
        if (c < '\u04f8') {
            return false;
        }
        if (c <= '\u04f9') {
            return true;
        }
        if (c < '\u0531') {
            return false;
        }
        if (c <= '\u0556') {
            return true;
        }
        if (c == '\u0559') {
            return true;
        }
        if (c < '\u0561') {
            return false;
        }
        if (c <= '\u0586') {
            return true;
        }
        if (c < '\u05d0') {
            return false;
        }
        if (c <= '\u05ea') {
            return true;
        }
        if (c < '\u05f0') {
            return false;
        }
        if (c <= '\u05f2') {
            return true;
        }
        if (c < '\u0621') {
            return false;
        }
        if (c <= '\u063a') {
            return true;
        }
        if (c < '\u0641') {
            return false;
        }
        if (c <= '\u064a') {
            return true;
        }
        if (c < '\u0671') {
            return false;
        }
        if (c <= '\u06b7') {
            return true;
        }
        if (c < '\u06ba') {
            return false;
        }
        if (c <= '\u06be') {
            return true;
        }
        if (c < '\u06c0') {
            return false;
        }
        if (c <= '\u06ce') {
            return true;
        }
        if (c < '\u06d0') {
            return false;
        }
        if (c <= '\u06d3') {
            return true;
        }
        if (c == '\u06d5') {
            return true;
        }
        if (c < '\u06e5') {
            return false;
        }
        if (c <= '\u06e6') {
            return true;
        }
        if (c < '\u0905') {
            return false;
        }
        if (c <= '\u0939') {
            return true;
        }
        if (c == '\u093d') {
            return true;
        }
        if (c < '\u0958') {
            return false;
        }
        if (c <= '\u0961') {
            return true;
        }
        if (c < '\u0985') {
            return false;
        }
        if (c <= '\u098c') {
            return true;
        }
        if (c < '\u098f') {
            return false;
        }
        if (c <= '\u0990') {
            return true;
        }
        if (c < '\u0993') {
            return false;
        }
        if (c <= '\u09a8') {
            return true;
        }
        if (c < '\u09aa') {
            return false;
        }
        if (c <= '\u09b0') {
            return true;
        }
        if (c == '\u09b2') {
            return true;
        }
        if (c < '\u09b6') {
            return false;
        }
        if (c <= '\u09b9') {
            return true;
        }
        if (c < '\u09dc') {
            return false;
        }
        if (c <= '\u09dd') {
            return true;
        }
        if (c < '\u09df') {
            return false;
        }
        if (c <= '\u09e1') {
            return true;
        }
        if (c < '\u09f0') {
            return false;
        }
        if (c <= '\u09f1') {
            return true;
        }
        if (c < '\u0a05') {
            return false;
        }
        if (c <= '\u0a0a') {
            return true;
        }
        if (c < '\u0a0f') {
            return false;
        }
        if (c <= '\u0a10') {
            return true;
        }
        if (c < '\u0a13') {
            return false;
        }
        if (c <= '\u0a28') {
            return true;
        }
        if (c < '\u0a2a') {
            return false;
        }
        if (c <= '\u0a30') {
            return true;
        }
        if (c < '\u0a32') {
            return false;
        }
        if (c <= '\u0a33') {
            return true;
        }
        if (c < '\u0a35') {
            return false;
        }
        if (c <= '\u0a36') {
            return true;
        }
        if (c < '\u0a38') {
            return false;
        }
        if (c <= '\u0a39') {
            return true;
        }
        if (c < '\u0a59') {
            return false;
        }
        if (c <= '\u0a5c') {
            return true;
        }
        if (c == '\u0a5e') {
            return true;
        }
        if (c < '\u0a72') {
            return false;
        }
        if (c <= '\u0a74') {
            return true;
        }
        if (c < '\u0a85') {
            return false;
        }
        if (c <= '\u0a8b') {
            return true;
        }
        if (c == '\u0a8d') {
            return true;
        }
        if (c < '\u0a8f') {
            return false;
        }
        if (c <= '\u0a91') {
            return true;
        }
        if (c < '\u0a93') {
            return false;
        }
        if (c <= '\u0aa8') {
            return true;
        }
        if (c < '\u0aaa') {
            return false;
        }
        if (c <= '\u0ab0') {
            return true;
        }
        if (c < '\u0ab2') {
            return false;
        }
        if (c <= '\u0ab3') {
            return true;
        }
        if (c < '\u0ab5') {
            return false;
        }
        if (c <= '\u0ab9') {
            return true;
        }
        if (c == '\u0abd') {
            return true;
        }
        if (c == '\u0ae0') {
            return true;
        }
        if (c < '\u0b05') {
            return false;
        }
        if (c <= '\u0b0c') {
            return true;
        }
        if (c < '\u0b0f') {
            return false;
        }
        if (c <= '\u0b10') {
            return true;
        }
        if (c < '\u0b13') {
            return false;
        }
        if (c <= '\u0b28') {
            return true;
        }
        if (c < '\u0b2a') {
            return false;
        }
        if (c <= '\u0b30') {
            return true;
        }
        if (c < '\u0b32') {
            return false;
        }
        if (c <= '\u0b33') {
            return true;
        }
        if (c < '\u0b36') {
            return false;
        }
        if (c <= '\u0b39') {
            return true;
        }
        if (c == '\u0b3d') {
            return true;
        }
        if (c < '\u0b5c') {
            return false;
        }
        if (c <= '\u0b5d') {
            return true;
        }
        if (c < '\u0b5f') {
            return false;
        }
        if (c <= '\u0b61') {
            return true;
        }
        if (c < '\u0b85') {
            return false;
        }
        if (c <= '\u0b8a') {
            return true;
        }
        if (c < '\u0b8e') {
            return false;
        }
        if (c <= '\u0b90') {
            return true;
        }
        if (c < '\u0b92') {
            return false;
        }
        if (c <= '\u0b95') {
            return true;
        }
        if (c < '\u0b99') {
            return false;
        }
        if (c <= '\u0b9a') {
            return true;
        }
        if (c == '\u0b9c') {
            return true;
        }
        if (c < '\u0b9e') {
            return false;
        }
        if (c <= '\u0b9f') {
            return true;
        }
        if (c < '\u0ba3') {
            return false;
        }
        if (c <= '\u0ba4') {
            return true;
        }
        if (c < '\u0ba8') {
            return false;
        }
        if (c <= '\u0baa') {
            return true;
        }
        if (c < '\u0bae') {
            return false;
        }
        if (c <= '\u0bb5') {
            return true;
        }
        if (c < '\u0bb7') {
            return false;
        }
        if (c <= '\u0bb9') {
            return true;
        }
        if (c < '\u0c05') {
            return false;
        }
        if (c <= '\u0c0c') {
            return true;
        }
        if (c < '\u0c0e') {
            return false;
        }
        if (c <= '\u0c10') {
            return true;
        }
        if (c < '\u0c12') {
            return false;
        }
        if (c <= '\u0c28') {
            return true;
        }
        if (c < '\u0c2a') {
            return false;
        }
        if (c <= '\u0c33') {
            return true;
        }
        if (c < '\u0c35') {
            return false;
        }
        if (c <= '\u0c39') {
            return true;
        }
        if (c < '\u0c60') {
            return false;
        }
        if (c <= '\u0c61') {
            return true;
        }
        if (c < '\u0c85') {
            return false;
        }
        if (c <= '\u0c8c') {
            return true;
        }
        if (c < '\u0c8e') {
            return false;
        }
        if (c <= '\u0c90') {
            return true;
        }
        if (c < '\u0c92') {
            return false;
        }
        if (c <= '\u0ca8') {
            return true;
        }
        if (c < '\u0caa') {
            return false;
        }
        if (c <= '\u0cb3') {
            return true;
        }
        if (c < '\u0cb5') {
            return false;
        }
        if (c <= '\u0cb9') {
            return true;
        }
        if (c == '\u0cde') {
            return true;
        }
        if (c < '\u0ce0') {
            return false;
        }
        if (c <= '\u0ce1') {
            return true;
        }
        if (c < '\u0d05') {
            return false;
        }
        if (c <= '\u0d0c') {
            return true;
        }
        if (c < '\u0d0e') {
            return false;
        }
        if (c <= '\u0d10') {
            return true;
        }
        if (c < '\u0d12') {
            return false;
        }
        if (c <= '\u0d28') {
            return true;
        }
        if (c < '\u0d2a') {
            return false;
        }
        if (c <= '\u0d39') {
            return true;
        }
        if (c < '\u0d60') {
            return false;
        }
        if (c <= '\u0d61') {
            return true;
        }
        if (c < '\u0e01') {
            return false;
        }
        if (c <= '\u0e2e') {
            return true;
        }
        if (c == '\u0e30') {
            return true;
        }
        if (c < '\u0e32') {
            return false;
        }
        if (c <= '\u0e33') {
            return true;
        }
        if (c < '\u0e40') {
            return false;
        }
        if (c <= '\u0e45') {
            return true;
        }
        if (c < '\u0e81') {
            return false;
        }
        if (c <= '\u0e82') {
            return true;
        }
        if (c == '\u0e84') {
            return true;
        }
        if (c < '\u0e87') {
            return false;
        }
        if (c <= '\u0e88') {
            return true;
        }
        if (c == '\u0e8a') {
            return true;
        }
        if (c == '\u0e8d') {
            return true;
        }
        if (c < '\u0e94') {
            return false;
        }
        if (c <= '\u0e97') {
            return true;
        }
        if (c < '\u0e99') {
            return false;
        }
        if (c <= '\u0e9f') {
            return true;
        }
        if (c < '\u0ea1') {
            return false;
        }
        if (c <= '\u0ea3') {
            return true;
        }
        if (c == '\u0ea5') {
            return true;
        }
        if (c == '\u0ea7') {
            return true;
        }
        if (c < '\u0eaa') {
            return false;
        }
        if (c <= '\u0eab') {
            return true;
        }
        if (c < '\u0ead') {
            return false;
        }
        if (c <= '\u0eae') {
            return true;
        }
        if (c == '\u0eb0') {
            return true;
        }
        if (c < '\u0eb2') {
            return false;
        }
        if (c <= '\u0eb3') {
            return true;
        }
        if (c == '\u0ebd') {
            return true;
        }
        if (c < '\u0ec0') {
            return false;
        }
        if (c <= '\u0ec4') {
            return true;
        }
        if (c < '\u0f40') {
            return false;
        }
        if (c <= '\u0f47') {
            return true;
        }
        if (c < '\u0f49') {
            return false;
        }
        if (c <= '\u0f69') {
            return true;
        }
        if (c < '\u10a0') {
            return false;
        }
        if (c <= '\u10c5') {
            return true;
        }
        if (c < '\u10d0') {
            return false;
        }
        if (c <= '\u10f6') {
            return true;
        }
        if (c == '\u1100') {
            return true;
        }
        if (c < '\u1102') {
            return false;
        }
        if (c <= '\u1103') {
            return true;
        }
        if (c < '\u1105') {
            return false;
        }
        if (c <= '\u1107') {
            return true;
        }
        if (c == '\u1109') {
            return true;
        }
        if (c < '\u110b') {
            return false;
        }
        if (c <= '\u110c') {
            return true;
        }
        if (c < '\u110e') {
            return false;
        }
        if (c <= '\u1112') {
            return true;
        }
        if (c == '\u113c') {
            return true;
        }
        if (c == '\u113e') {
            return true;
        }
        if (c == '\u1140') {
            return true;
        }
        if (c == '\u114c') {
            return true;
        }
        if (c == '\u114e') {
            return true;
        }
        if (c == '\u1150') {
            return true;
        }
        if (c < '\u1154') {
            return false;
        }
        if (c <= '\u1155') {
            return true;
        }
        if (c == '\u1159') {
            return true;
        }
        if (c < '\u115f') {
            return false;
        }
        if (c <= '\u1161') {
            return true;
        }
        if (c == '\u1163') {
            return true;
        }
        if (c == '\u1165') {
            return true;
        }
        if (c == '\u1167') {
            return true;
        }
        if (c == '\u1169') {
            return true;
        }
        if (c < '\u116d') {
            return false;
        }
        if (c <= '\u116e') {
            return true;
        }
        if (c < '\u1172') {
            return false;
        }
        if (c <= '\u1173') {
            return true;
        }
        if (c == '\u1175') {
            return true;
        }
        if (c == '\u119e') {
            return true;
        }
        if (c == '\u11a8') {
            return true;
        }
        if (c == '\u11ab') {
            return true;
        }
        if (c < '\u11ae') {
            return false;
        }
        if (c <= '\u11af') {
            return true;
        }
        if (c < '\u11b7') {
            return false;
        }
        if (c <= '\u11b8') {
            return true;
        }
        if (c == '\u11ba') {
            return true;
        }
        if (c < '\u11bc') {
            return false;
        }
        if (c <= '\u11c2') {
            return true;
        }
        if (c == '\u11eb') {
            return true;
        }
        if (c == '\u11f0') {
            return true;
        }
        if (c == '\u11f9') {
            return true;
        }
        if (c < '\u1e00') {
            return false;
        }
        if (c <= '\u1e9b') {
            return true;
        }
        if (c < '\u1ea0') {
            return false;
        }
        if (c <= '\u1ef9') {
            return true;
        }
        if (c < '\u1f00') {
            return false;
        }
        if (c <= '\u1f15') {
            return true;
        }
        if (c < '\u1f18') {
            return false;
        }
        if (c <= '\u1f1d') {
            return true;
        }
        if (c < '\u1f20') {
            return false;
        }
        if (c <= '\u1f45') {
            return true;
        }
        if (c < '\u1f48') {
            return false;
        }
        if (c <= '\u1f4d') {
            return true;
        }
        if (c < '\u1f50') {
            return false;
        }
        if (c <= '\u1f57') {
            return true;
        }
        if (c == '\u1f59') {
            return true;
        }
        if (c == '\u1f5b') {
            return true;
        }
        if (c == '\u1f5d') {
            return true;
        }
        if (c < '\u1f5f') {
            return false;
        }
        if (c <= '\u1f7d') {
            return true;
        }
        if (c < '\u1f80') {
            return false;
        }
        if (c <= '\u1fb4') {
            return true;
        }
        if (c < '\u1fb6') {
            return false;
        }
        if (c <= '\u1fbc') {
            return true;
        }
        if (c == '\u1fbe') {
            return true;
        }
        if (c < '\u1fc2') {
            return false;
        }
        if (c <= '\u1fc4') {
            return true;
        }
        if (c < '\u1fc6') {
            return false;
        }
        if (c <= '\u1fcc') {
            return true;
        }
        if (c < '\u1fd0') {
            return false;
        }
        if (c <= '\u1fd3') {
            return true;
        }
        if (c < '\u1fd6') {
            return false;
        }
        if (c <= '\u1fdb') {
            return true;
        }
        if (c < '\u1fe0') {
            return false;
        }
        if (c <= '\u1fec') {
            return true;
        }
        if (c < '\u1ff2') {
            return false;
        }
        if (c <= '\u1ff4') {
            return true;
        }
        if (c < '\u1ff6') {
            return false;
        }
        if (c <= '\u1ffc') {
            return true;
        }
        if (c == '\u2126') {
            return true;
        }
        if (c < '\u212a') {
            return false;
        }
        if (c <= '\u212b') {
            return true;
        }
        if (c == '\u212e') {
            return true;
        }
        if (c < '\u2180') {
            return false;
        }
        if (c <= '\u2182') {
            return true;
        }
        if (c == '\u3007') {
            return true;
        }
        if (c < '\u3021') {
            return false;
        }
        if (c <= '\u3029') {
            return true;
        }
        if (c < '\u3041') {
            return false;
        }
        if (c <= '\u3094') {
            return true;
        }
        if (c < '\u30a1') {
            return false;
        }
        if (c <= '\u30fa') {
            return true;
        }
        if (c < '\u3105') {
            return false;
        }
        if (c <= '\u312c') {
            return true;
        }
        if (c < '\u4e00') {
            return false;
        }
        if (c <= '\u9fa5') {
            return true;
        }
        if (c < '\uac00') {
            return false;
        }
        return c <= '\ud7a3';
    }

    static boolean isXMLCombiningChar(char c) {
        if (c < '\u0300') {
            return false;
        }
        if (c <= '\u0345') {
            return true;
        }
        if (c < '\u0360') {
            return false;
        }
        if (c <= '\u0361') {
            return true;
        }
        if (c < '\u0483') {
            return false;
        }
        if (c <= '\u0486') {
            return true;
        }
        if (c < '\u0591') {
            return false;
        }
        if (c <= '\u05a1') {
            return true;
        }
        if (c < '\u05a3') {
            return false;
        }
        if (c <= '\u05b9') {
            return true;
        }
        if (c < '\u05bb') {
            return false;
        }
        if (c <= '\u05bd') {
            return true;
        }
        if (c == '\u05bf') {
            return true;
        }
        if (c < '\u05c1') {
            return false;
        }
        if (c <= '\u05c2') {
            return true;
        }
        if (c == '\u05c4') {
            return true;
        }
        if (c < '\u064b') {
            return false;
        }
        if (c <= '\u0652') {
            return true;
        }
        if (c == '\u0670') {
            return true;
        }
        if (c < '\u06d6') {
            return false;
        }
        if (c <= '\u06dc') {
            return true;
        }
        if (c < '\u06dd') {
            return false;
        }
        if (c <= '\u06df') {
            return true;
        }
        if (c < '\u06e0') {
            return false;
        }
        if (c <= '\u06e4') {
            return true;
        }
        if (c < '\u06e7') {
            return false;
        }
        if (c <= '\u06e8') {
            return true;
        }
        if (c < '\u06ea') {
            return false;
        }
        if (c <= '\u06ed') {
            return true;
        }
        if (c < '\u0901') {
            return false;
        }
        if (c <= '\u0903') {
            return true;
        }
        if (c == '\u093c') {
            return true;
        }
        if (c < '\u093e') {
            return false;
        }
        if (c <= '\u094c') {
            return true;
        }
        if (c == '\u094d') {
            return true;
        }
        if (c < '\u0951') {
            return false;
        }
        if (c <= '\u0954') {
            return true;
        }
        if (c < '\u0962') {
            return false;
        }
        if (c <= '\u0963') {
            return true;
        }
        if (c < '\u0981') {
            return false;
        }
        if (c <= '\u0983') {
            return true;
        }
        if (c == '\u09bc') {
            return true;
        }
        if (c == '\u09be') {
            return true;
        }
        if (c == '\u09bf') {
            return true;
        }
        if (c < '\u09c0') {
            return false;
        }
        if (c <= '\u09c4') {
            return true;
        }
        if (c < '\u09c7') {
            return false;
        }
        if (c <= '\u09c8') {
            return true;
        }
        if (c < '\u09cb') {
            return false;
        }
        if (c <= '\u09cd') {
            return true;
        }
        if (c == '\u09d7') {
            return true;
        }
        if (c < '\u09e2') {
            return false;
        }
        if (c <= '\u09e3') {
            return true;
        }
        if (c == '\u0a02') {
            return true;
        }
        if (c == '\u0a3c') {
            return true;
        }
        if (c == '\u0a3e') {
            return true;
        }
        if (c == '\u0a3f') {
            return true;
        }
        if (c < '\u0a40') {
            return false;
        }
        if (c <= '\u0a42') {
            return true;
        }
        if (c < '\u0a47') {
            return false;
        }
        if (c <= '\u0a48') {
            return true;
        }
        if (c < '\u0a4b') {
            return false;
        }
        if (c <= '\u0a4d') {
            return true;
        }
        if (c < '\u0a70') {
            return false;
        }
        if (c <= '\u0a71') {
            return true;
        }
        if (c < '\u0a81') {
            return false;
        }
        if (c <= '\u0a83') {
            return true;
        }
        if (c == '\u0abc') {
            return true;
        }
        if (c < '\u0abe') {
            return false;
        }
        if (c <= '\u0ac5') {
            return true;
        }
        if (c < '\u0ac7') {
            return false;
        }
        if (c <= '\u0ac9') {
            return true;
        }
        if (c < '\u0acb') {
            return false;
        }
        if (c <= '\u0acd') {
            return true;
        }
        if (c < '\u0b01') {
            return false;
        }
        if (c <= '\u0b03') {
            return true;
        }
        if (c == '\u0b3c') {
            return true;
        }
        if (c < '\u0b3e') {
            return false;
        }
        if (c <= '\u0b43') {
            return true;
        }
        if (c < '\u0b47') {
            return false;
        }
        if (c <= '\u0b48') {
            return true;
        }
        if (c < '\u0b4b') {
            return false;
        }
        if (c <= '\u0b4d') {
            return true;
        }
        if (c < '\u0b56') {
            return false;
        }
        if (c <= '\u0b57') {
            return true;
        }
        if (c < '\u0b82') {
            return false;
        }
        if (c <= '\u0b83') {
            return true;
        }
        if (c < '\u0bbe') {
            return false;
        }
        if (c <= '\u0bc2') {
            return true;
        }
        if (c < '\u0bc6') {
            return false;
        }
        if (c <= '\u0bc8') {
            return true;
        }
        if (c < '\u0bca') {
            return false;
        }
        if (c <= '\u0bcd') {
            return true;
        }
        if (c == '\u0bd7') {
            return true;
        }
        if (c < '\u0c01') {
            return false;
        }
        if (c <= '\u0c03') {
            return true;
        }
        if (c < '\u0c3e') {
            return false;
        }
        if (c <= '\u0c44') {
            return true;
        }
        if (c < '\u0c46') {
            return false;
        }
        if (c <= '\u0c48') {
            return true;
        }
        if (c < '\u0c4a') {
            return false;
        }
        if (c <= '\u0c4d') {
            return true;
        }
        if (c < '\u0c55') {
            return false;
        }
        if (c <= '\u0c56') {
            return true;
        }
        if (c < '\u0c82') {
            return false;
        }
        if (c <= '\u0c83') {
            return true;
        }
        if (c < '\u0cbe') {
            return false;
        }
        if (c <= '\u0cc4') {
            return true;
        }
        if (c < '\u0cc6') {
            return false;
        }
        if (c <= '\u0cc8') {
            return true;
        }
        if (c < '\u0cca') {
            return false;
        }
        if (c <= '\u0ccd') {
            return true;
        }
        if (c < '\u0cd5') {
            return false;
        }
        if (c <= '\u0cd6') {
            return true;
        }
        if (c < '\u0d02') {
            return false;
        }
        if (c <= '\u0d03') {
            return true;
        }
        if (c < '\u0d3e') {
            return false;
        }
        if (c <= '\u0d43') {
            return true;
        }
        if (c < '\u0d46') {
            return false;
        }
        if (c <= '\u0d48') {
            return true;
        }
        if (c < '\u0d4a') {
            return false;
        }
        if (c <= '\u0d4d') {
            return true;
        }
        if (c == '\u0d57') {
            return true;
        }
        if (c == '\u0e31') {
            return true;
        }
        if (c < '\u0e34') {
            return false;
        }
        if (c <= '\u0e3a') {
            return true;
        }
        if (c < '\u0e47') {
            return false;
        }
        if (c <= '\u0e4e') {
            return true;
        }
        if (c == '\u0eb1') {
            return true;
        }
        if (c < '\u0eb4') {
            return false;
        }
        if (c <= '\u0eb9') {
            return true;
        }
        if (c < '\u0ebb') {
            return false;
        }
        if (c <= '\u0ebc') {
            return true;
        }
        if (c < '\u0ec8') {
            return false;
        }
        if (c <= '\u0ecd') {
            return true;
        }
        if (c < '\u0f18') {
            return false;
        }
        if (c <= '\u0f19') {
            return true;
        }
        if (c == '\u0f35') {
            return true;
        }
        if (c == '\u0f37') {
            return true;
        }
        if (c == '\u0f39') {
            return true;
        }
        if (c == '\u0f3e') {
            return true;
        }
        if (c == '\u0f3f') {
            return true;
        }
        if (c < '\u0f71') {
            return false;
        }
        if (c <= '\u0f84') {
            return true;
        }
        if (c < '\u0f86') {
            return false;
        }
        if (c <= '\u0f8b') {
            return true;
        }
        if (c < '\u0f90') {
            return false;
        }
        if (c <= '\u0f95') {
            return true;
        }
        if (c == '\u0f97') {
            return true;
        }
        if (c < '\u0f99') {
            return false;
        }
        if (c <= '\u0fad') {
            return true;
        }
        if (c < '\u0fb1') {
            return false;
        }
        if (c <= '\u0fb7') {
            return true;
        }
        if (c == '\u0fb9') {
            return true;
        }
        if (c < '\u20d0') {
            return false;
        }
        if (c <= '\u20dc') {
            return true;
        }
        if (c == '\u20e1') {
            return true;
        }
        if (c < '\u302a') {
            return false;
        }
        if (c <= '\u302f') {
            return true;
        }
        if (c == '\u3099') {
            return true;
        }
        return c == '\u309a';
    }

    static boolean isXMLExtender(char c) {
        if (c < '\u00b6') {
            return false;
        }
        if (c == '\u00b7') {
            return true;
        }
        if (c == '\u02d0') {
            return true;
        }
        if (c == '\u02d1') {
            return true;
        }
        if (c == '\u0387') {
            return true;
        }
        if (c == '\u0640') {
            return true;
        }
        if (c == '\u0e46') {
            return true;
        }
        if (c == '\u0ec6') {
            return true;
        }
        if (c == '\u3005') {
            return true;
        }
        if (c < '\u3031') {
            return false;
        }
        if (c <= '\u3035') {
            return true;
        }
        if (c < '\u309d') {
            return false;
        }
        if (c <= '\u309e') {
            return true;
        }
        if (c < '\u30fc') {
            return false;
        }
        return c <= '\u30fe';
    }

    static boolean isXMLDigit(char c) {
        if (c < '0') {
            return false;
        }
        if (c <= '9') {
            return true;
        }
        if (c < '\u0660') {
            return false;
        }
        if (c <= '\u0669') {
            return true;
        }
        if (c < '\u06f0') {
            return false;
        }
        if (c <= '\u06f9') {
            return true;
        }
        if (c < '\u0966') {
            return false;
        }
        if (c <= '\u096f') {
            return true;
        }
        if (c < '\u09e6') {
            return false;
        }
        if (c <= '\u09ef') {
            return true;
        }
        if (c < '\u0a66') {
            return false;
        }
        if (c <= '\u0a6f') {
            return true;
        }
        if (c < '\u0ae6') {
            return false;
        }
        if (c <= '\u0aef') {
            return true;
        }
        if (c < '\u0b66') {
            return false;
        }
        if (c <= '\u0b6f') {
            return true;
        }
        if (c < '\u0be7') {
            return false;
        }
        if (c <= '\u0bef') {
            return true;
        }
        if (c < '\u0c66') {
            return false;
        }
        if (c <= '\u0c6f') {
            return true;
        }
        if (c < '\u0ce6') {
            return false;
        }
        if (c <= '\u0cef') {
            return true;
        }
        if (c < '\u0d66') {
            return false;
        }
        if (c <= '\u0d6f') {
            return true;
        }
        if (c < '\u0e50') {
            return false;
        }
        if (c <= '\u0e59') {
            return true;
        }
        if (c < '\u0ed0') {
            return false;
        }
        if (c <= '\u0ed9') {
            return true;
        }
        if (c < '\u0f20') {
            return false;
        }
        return c <= '\u0f29';
    }
}

