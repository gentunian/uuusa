package org.grupolys.spring.model.payloads;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class DictionaryWordPayload {
    @NotEmpty(message = "word must not be empty.")
    private String word;
    @NotEmpty(message = "dictionary must not be empty.")
    private String dictionary;
    @NotEmpty(message = "profile must not be empty.")
    private String profile;
    private Double value = 0.0;
    private String partOfSpeech;
    private String lemma;
    private Double booster = 1.0;
    private Boolean negating = Boolean.FALSE;
}
