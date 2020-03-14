package org.grupolys.spring.model.persistence;

import lombok.Data;

@Data
public class PersistentDictionary {
    private String id;
    private String name;
    private String description;
    private String language;

}
