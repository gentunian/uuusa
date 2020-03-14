package org.grupolys.spring.model.persistence;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Null;

@EqualsAndHashCode(callSuper = true)
@Document(collection = "words")
@Data
public class PersistentWord extends PersistentAudit {
    @Id private String id;
    private String profile;
    private String dictionary;
    private String language;
    @TextIndexed(weight = 2) private String word;
    private Double value = 0.0;
    private String partOfSpeech;
    private String lemma;
    private Double booster = 1.0;
    private Boolean negating = Boolean.FALSE;

    public void setLemma(String lemma) {
        if (lemma != null && lemma.equals("")) {
            this.lemma = null;
        } else {
            this.lemma = lemma;
        }
    }
}
