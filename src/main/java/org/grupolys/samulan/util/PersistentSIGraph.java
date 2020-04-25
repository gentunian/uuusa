package org.grupolys.samulan.util;

import lombok.Data;

import java.util.List;

@Data
public class PersistentSIGraph {

    List<PersistentSIGraph> children;
    float so;
    boolean is_negation;
    boolean is_intensifier;
    String postag;
    String word;
    int address;
    String getDependency_type;
    String operationExplained;
    float weight;
}
