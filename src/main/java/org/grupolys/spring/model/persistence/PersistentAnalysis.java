package org.grupolys.spring.model.persistence;

import lombok.Data;
import org.springframework.data.annotation.Id;

import java.util.List;
import java.util.Map;

@Data
public class PersistentAnalysis {
    @Id
    private String id;
    private String text;
    private String sentiment;
    private float sentimentWeight;
    private String profile;
    private String dictionary;
    private List<Map<String, Object>> analysisTree;

}
