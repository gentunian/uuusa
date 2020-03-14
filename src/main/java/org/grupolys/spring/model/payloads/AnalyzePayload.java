package org.grupolys.spring.model.payloads;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class AnalyzePayload {
    @NotBlank
    private String text;
}
