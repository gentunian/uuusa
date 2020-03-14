package org.grupolys.spring.model.payloads;

import lombok.Data;
import org.grupolys.spring.validations.IsSupportedLanguage;

import javax.validation.constraints.NotBlank;

@Data
public class ProfileDictionaryPayload {
    @NotBlank
    private String name;
    @IsSupportedLanguage
    private String language;
    private String description;
}
