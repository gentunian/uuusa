package org.grupolys.spring.model.payloads;

import lombok.Data;

@Data
public class UpdateProfilePayload {
    private String activeDictionary;
    private String description;
    private String language;
}
