package org.grupolys.spring.model.persistence;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Document(collection = "profiles")
@Data
public class PersistentDictionaryProfile extends PersistentAudit {
    @Id
    private String id;
    private String name;
    private String description;
    private String language;
    private String activeDictionary;
    private List<PersistentDictionary> dictionaries = new ArrayList<>();
}
