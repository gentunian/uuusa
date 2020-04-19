package org.grupolys.dictionary;

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
}
