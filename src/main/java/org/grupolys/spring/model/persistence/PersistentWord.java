package org.grupolys.spring.model.persistence;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.grupolys.profiles.PartOfSpeech;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotBlank;

@EqualsAndHashCode(callSuper = true)
@Document(collection = "words")
@Data
@Deprecated
public class PersistentWord extends PersistentAudit {
    @Id private String id;
    @NotBlank
    private String profile;
    @NotBlank
    private String dictionary;
    private String language = "ES"; // TODO: make a default language machinery
    @TextIndexed(weight = 2) private String word;
    private Double value = 0.0;
    private PartOfSpeech partOfSpeech = PartOfSpeech.NOPOSTAG;
    private String lemma;
    private Double booster;
    private Boolean negating;

    public void setLemma(String lemma) {
        if (lemma != null && lemma.equals("")) {
            this.lemma = null;
        } else {
            this.lemma = lemma;
        }
    }
}
