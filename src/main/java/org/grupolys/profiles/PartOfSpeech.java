package org.grupolys.profiles;

import org.grupolys.dictionary.WordType;

import javax.servlet.http.Part;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public enum PartOfSpeech implements WordType {
    ADV("ADV", new String[] { "adverbs", "adverb", "adv" }),
    ADJ("ADJ", new String[] { "adjective", "adjectives", "adj" }),
    VERB("VERB", new String[] { "verb", "verbs" }),
    NOUN("NOUN", new String[] { "noun", "nouns" }),
    NOPOSTAG("NOPOSTAG", new String[] {null});

    private Map<String, String> partOfSpeech;

    PartOfSpeech(String pos, String[] alias) {
        this.partOfSpeech = new HashMap<>();
        for (String s : alias) {
            this.partOfSpeech.put(s, pos);
        }
    }

    public static PartOfSpeech getPartOfSpeech(String alias) {
        if (alias == null) {
            return PartOfSpeech.NOPOSTAG;
        }

        return Arrays.stream(PartOfSpeech.values())
                .filter(item -> item.isAlias(alias))
                .findFirst()
                .orElse(PartOfSpeech.NOPOSTAG);
    }

    public static PartOfSpeech getPartOfSpeech(PartOfSpeech partOfSpeech) {
        return partOfSpeech == null? PartOfSpeech.NOPOSTAG : partOfSpeech;
    }

    public boolean isAlias(String alias) {
        if (alias != null) {
            alias = alias.toLowerCase();
        }
        return this.partOfSpeech.get(alias) != null || this == PartOfSpeech.NOPOSTAG;
    }

    public Map<String, String> getAliasesMap() {
        return Collections.unmodifiableMap(this.partOfSpeech);
    }

    @Override
    public String getType() {
        return this.name();
    }
}
