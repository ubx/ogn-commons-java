package org.ogn.commons.utils;

/**
 * This toolkit class provides conversion of version (in X.Y.Z format) to int and from int to X.Y.Z string 
 * Expected string version format: X.Y.Z
 * 
 * @author wbuczak
 */
public class Version {

    private static final int w1 = 100000;
    private static final int w2 = 1000;

    public static final String fromInt(final int version) {
        int p1 = version / w1;
        int p2 = ((version - p1 * w1)) / w2;
        int p3 = version - (p1 * w1 + p2 * w2);

        return p1 + "." + p2 + "." + p3;
    }

    public static final int fromString(final String ver) {
        if (ver == null || ver.split("\\.").length != 3) {
            throw new IllegalArgumentException("incorrect version");
        }
        String[] parts = ver.split("\\.");

        int v1 = Integer.parseInt(parts[0]);
        int v2 = Integer.parseInt(parts[1]);
        int v3 = Integer.parseInt(parts[2]);

        if (v1 < 0 || v2 < 0 || v3 < 0)
            throw new IllegalArgumentException("incorrect version");

        return w1 * v1 + w2 * v2 + v3;
    }
}