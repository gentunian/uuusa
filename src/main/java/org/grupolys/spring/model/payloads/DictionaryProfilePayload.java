package org.grupolys.spring.model.payloads;

import lombok.Data;
import org.grupolys.spring.validations.IsSupportedLanguage;

import javax.validation.constraints.NotEmpty;

@Data
public class DictionaryProfilePayload {
    @NotEmpty(message = "name can't be empty.")
    private String name;
    @IsSupportedLanguage
    private String language;
    private String description;
}
