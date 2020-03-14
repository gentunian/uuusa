package org.grupolys.profiles;

import org.grupolys.dictionary.WordsDictionary;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Deprecated
public class DictionaryProfile implements Serializable {
    private String name;
    private String description;
    private String language;
    private String activeDictionaryIdentifier;
    private List<WordsDictionary> dictionaries = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getActiveDictionaryIdentifier() {
        return activeDictionaryIdentifier;
    }

    public void setActiveDictionaryIdentifier(String activeDictionaryIdentifier) {
        this.activeDictionaryIdentifier = activeDictionaryIdentifier;
    }

    public List<WordsDictionary> getDictionaries() {
        return dictionaries;
    }

    public boolean addDictionary(WordsDictionary dictionary) {
        return dictionaries.add(dictionary);
    }
}