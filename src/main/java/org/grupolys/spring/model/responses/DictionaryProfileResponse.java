package org.grupolys.spring.model.responses;

import lombok.Value;

import java.util.List;

@Value
public class DictionaryProfileResponse {

    @Value
    public static class Dictionary {
        private String id;
        private String name;
        private String language;
        private String description;
    }

    private String id;
    private String name;
    private String description;
    private String language;
    private String activeDictionary;
    private List<Dictionary> dictionaries;
}

