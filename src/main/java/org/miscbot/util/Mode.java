package org.miscbot.util;

public enum Mode {
    DEBUG,
    PROD;

    public static Mode getMode(String debugToken) {
        return debugToken == null ? PROD : DEBUG;
    }
}
