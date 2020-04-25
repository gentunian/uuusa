package org.grupolys.spring.model.payloads;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NonNull;
import org.grupolys.profiles.PartOfSpeech;

import java.util.Map;

@Data
public class PatchWordPayload {
    Map<PartOfSpeech, Value> partOfSpeech;
    Boolean negating;
    Double booster;
    Double emoticon;

    @lombok.Data
    public static class Value {
        @NonNull
        Double value = 0.0;
        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        String lemma;

        Value(Double value, String lemma) {
            this.value = value;
            this.lemma = lemma != null && lemma.equals("") ? null : lemma;
        }
    }
}
