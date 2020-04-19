package org.grupolys.spring.model.payloads;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
public class PostWordPayload {
    @NotEmpty(message = "'word' cannot be null or empty.")
    String word;

    @NotNull(message = "'dictionary' cannot be null.")
    String dictionary;

    @NotNull(message = "'profile' cannot be null.")
    String profile;

    Boolean negating = Boolean.FALSE;

    Boolean emoticon = Boolean.FALSE;

    Double booster = 0.0;

    public void setEmoticon(Boolean value) {
        this.emoticon = (value != null) ? value : false;
    }

    public void setNegating(Boolean value) {
        this.negating = (value != null) ? value : false;
    }

    public void setBooster(Double value) {
        this.booster = (value != null) ? value : 1.0;
    }
}
