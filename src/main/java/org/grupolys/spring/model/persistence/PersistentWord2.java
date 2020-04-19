package org.grupolys.spring.model.persistence;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.grupolys.profiles.PartOfSpeech;
import org.grupolys.spring.validations.NullableButNotEmpty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Document(collection = "words2")
@Data
public class PersistentWord2 extends PersistentAudit  {
    @Id private String id;

    @NotBlank(message = "profile can't be null or blank.")
    private String profile;

    @NotBlank(message = "dictionary can't be null or blank.")
    private String dictionary;

    @NotBlank(message = "language can't be null or blank.")
    private String language = "ES";

    @NotBlank(message = "word can't be null or blank.")
    @TextIndexed(weight = 2)
    private String word;

    private Map<PartOfSpeech, Value> partOfSpeech = new HashMap<PartOfSpeech, Value>() {
        {
            Arrays.stream(PartOfSpeech.values()).forEach(partOfSpeech -> {
                put(partOfSpeech, new Value(0.0, null));
            });
        }
    };

    private Double booster = 1.0;

    private Boolean negating = Boolean.FALSE;

    private Boolean emoticon = Boolean.FALSE;

    public void setWordValue(PartOfSpeech partOfSpeech, Double value, String lemma) {
        Value wordValue = new Value(
                value == null ? 0.0 : value,
                lemma != null && lemma.equals("") ? null : lemma);
        this.partOfSpeech.put(partOfSpeech, wordValue);
    }

    public Double getValue(PartOfSpeech partOfSpeech) {
        Value value = this.partOfSpeech.get(partOfSpeech);
        if (value != null) {
            return value.value;
        }
        return null;
    }

    public String getLemma(PartOfSpeech partOfSpeech) {
        Value value = this.partOfSpeech.get(partOfSpeech);
        if (value != null) {
            return value.lemma;
        }
        return null;
    }

    @Data
    public static class Value {
        Double value;

        @NullableButNotEmpty
        String lemma;

        Value() {
            this(0.0, null);
        }

        Value(Double value, String lemma) {
            this.value = value;
            this.lemma = lemma;
        }
    }
}
