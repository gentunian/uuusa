package org.grupolys.profiles;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public enum PartOfSpeech {
    ADV("ADV", new String[] { "adverbs", "adverb", "adv" }),
    ADJ("ADJ", new String[] { "adjective", "adjectives", "adj" }),
    VERB("VERB", new String[] { "verb", "verbs" }),
    NOUN("NOUN", new String[] { "noun", "nouns" });

    private Map<String, String> partOfSpeech;

    PartOfSpeech(String pos, String[] alias) {
        this.partOfSpeech = new HashMap<>();
        for (String s : alias) {
            this.partOfSpeech.put(s, pos);
        }
    }

    public static PartOfSpeech getPartOfSpeech(String alias) {
        return alias == null ? null : Arrays.stream(PartOfSpeech.values())
                .filter(item -> item.getAliasesMap().get(alias.toLowerCase()) != null)
                .findFirst()
                .orElse(null);

    }

//    public List<String> getAliases(String partOfSpeech) {
//        return Collections.unmodifiableList(this.partOfSpeech.get(partOfSpeech));
//    }

    public Map<String, String> getAliasesMap() {
        return Collections.unmodifiableMap(this.partOfSpeech);
    }

}
