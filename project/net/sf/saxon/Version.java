/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon;

import net.sf.saxon.Configuration;
import net.sf.saxon.Platform;
import net.sf.saxon.java.JavaPlatform;

public final class Version {
    private static final int MAJOR_VERSION = 10;
    private static final int MINOR_VERSION = 5;
    private static final int BUILD = 41415;
    private static final String MAJOR_RELEASE_DATE = "2020-03-16";
    private static final String MINOR_RELEASE_DATE = "2021-04-14";
    public static Class<? extends Configuration> configurationClass;
    public static String softwareEdition;
    public static Platform platform;

    private Version() {
    }

    public static String getProductName() {
        return "SAXON";
    }

    public static String getProductVendor() {
        return "Saxonica";
    }

    public static String getProductVariantAndVersion(String edition) {
        return edition + " " + Version.getProductVersion();
    }

    public static String getProductVersion() {
        return "10.5";
    }

    public static int[] getStructuredVersionNumber() {
        return new int[]{10, 5, 41415};
    }

    public static String getReleaseDate() {
        return MINOR_RELEASE_DATE;
    }

    public static String getMajorReleaseDate() {
        return MAJOR_RELEASE_DATE;
    }

    public static String getProductTitle() {
        return Version.getProductName() + '-' + softwareEdition + ' ' + Version.getProductVersion() + (platform.isJava() ? (char)'J' : 'N') + " from Saxonica";
    }

    public static String getWebSiteAddress() {
        return "http://www.saxonica.com/";
    }

    public static void main(String[] args) {
        System.err.println(Version.getProductTitle() + " (build " + 41415 + ')');
    }

    static {
        softwareEdition = "HE";
        platform = new JavaPlatform();
    }
}

