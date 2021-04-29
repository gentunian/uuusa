package org.grupolys.spring.model.persistence;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.*;
import org.grupolys.profiles.PartOfSpeech;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.TextIndexed;

import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(toBuilder = true)
@JsonDeserialize(builder = TestingJson.TestingJsonBuilder.class)
public final class TestingJson {
    @Id
    String id;

    @NonNull
    String profile;

    @NonNull
    String dictionary;

    String language;

    @TextIndexed(weight = 2)
    @NonNull
    String name;

    Map<PartOfSpeech, Foo> partOfSpeech;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    Double booster;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    Boolean negating;

    @JsonCreator
    public static TestingJson of(
                                 @JsonProperty("dictionary") @NonNull String dictionary,
                                 @JsonProperty("profile") @NonNull String profile,
                                 @JsonProperty("name") @NonNull String name,
                                 @JsonProperty("language") String language,
                                 @JsonProperty("lemma") String lemma,
                                 @JsonProperty("booster") Double booster,
                                 @JsonProperty("negating") Boolean negating,
                                 @JsonProperty("partOfSpeech") String partOfSpeech,
                                 @JsonProperty("value") Double value) {
        PartOfSpeech p = PartOfSpeech.getPartOfSpeech(partOfSpeech);
        Map<PartOfSpeech, Foo> values = new HashMap<>();
        values.put(p, new Foo(
                value == null ? 0.0 : value,
                lemma != null && lemma.equals("") ? null: lemma));
        TestingJson t = new TestingJson("",
                profile,
                dictionary,
                language == null ? "ES" : language,
                name,
                values,
                booster == null ? 1.0 : booster,
                negating == null ? false : negating);
        return t;
    }

    @Value
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    static class Foo {
        @NonNull
        Double value;
        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        String lemma;
    }

    @JsonPOJOBuilder(withPrefix = "")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TestingJsonBuilder {

        protected TestingJsonBuilder() {
            this.booster = 1.0;
            this.negating = false;
            this.partOfSpeech = new HashMap<>();
        }

        public TestingJsonBuilder partOfSpeech(Map<PartOfSpeech, Foo> map) {
            return this;
        }

        // The idea of this method is to avoid exposing Foo
        public TestingJsonBuilder addValue(PartOfSpeech partOfSpeech, Double value, String lemma) {
            this.partOfSpeech.put(partOfSpeech, new Foo(
                    value == null ? 0 : value,
                    lemma != null && lemma.equals("") ? null : lemma));
            return this;
        }
    }
}
