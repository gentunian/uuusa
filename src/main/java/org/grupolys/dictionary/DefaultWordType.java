package org.grupolys.dictionary;

import java.util.Arrays;

public enum DefaultWordType implements WordType {
    BOOSTER("booster"),
    EMOTICON("emoticon");

    private String type;

    DefaultWordType(String type) {
        this.type = type;
    }

    @Override
    public String getType() {
        return this.type;
    }

    public static DefaultWordType getDefaultWordType(String alias) {
        if (alias == null) {
            return null;
        }

        return Arrays.stream(DefaultWordType.values())
                .filter(item -> item.name().equals(alias.toUpperCase()))
                .findFirst()
                .orElse(null);
    }
}
