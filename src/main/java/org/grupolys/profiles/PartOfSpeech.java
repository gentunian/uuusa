package org.grupolys.profiles;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.List;

public enum PartOfSpeech {
    ADV("ADV", new String[] { "adverbs", "adverb", "adv" }),
    ADJ("ADJ", new String[] { "adjetive", "adjetives", "adj" }), VERB("VERB", new String[] { "verb", "verbs" }),
    NOUN("NOUN", new String[] { "noun", "nouns" });

    private Map<String, List<String>> partOfSpeech;

    PartOfSpeech(String partOfSpeech, String[] alias) {
        this.partOfSpeech = new HashMap<String, List<String>>();
        this.partOfSpeech.put(partOfSpeech, Arrays.asList(alias));
    }

    public static String getPartOfSpeech(String alias) {
        // Get all the enums, i.e., ADV, ADJ, VERB, and NOUN.
        PartOfSpeech[] pos = PartOfSpeech.values();
        String partOfSpeech = null;

        // For each enum, try to find its alias
        for (int i = 0; i < pos.length; i++) {
            Map<String, List<String>> aliasesMap = pos[i].getAliasesMap();
            Iterator<String> it = aliasesMap.keySet().iterator();

            // iterate through all keys and try to find for an alias
            while(it.hasNext() && partOfSpeech == null) {
                String key = it.next();
                List<String> aliases = aliasesMap.get(key);
                if (aliases.contains(alias.toLowerCase())) {
                    partOfSpeech = key;
                }
            }
        }

        return partOfSpeech;
    }

    public List<String> getAliases(String partOfSpeech) {
        return Collections.unmodifiableList(this.partOfSpeech.get(partOfSpeech));
    }

    public Map<String, List<String>> getAliasesMap() {
        return Collections.unmodifiableMap(this.partOfSpeech);
    }
}
