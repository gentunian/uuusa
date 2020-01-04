package org.grupolys.samulan.rule;

import org.grupolys.samulan.analyser.operation.Operation;
import org.grupolys.samulan.util.SentimentDependencyGraph;
import org.grupolys.samulan.util.dictionary.Dictionary;

import java.util.List;

public interface RulesManager {

    Dictionary getDictionary();
    void setDictionary(Dictionary dictionary);

    List<Rule> getRules();
    void setRules(List<Rule> rules);
    void addRule(Rule rule);

    boolean isAlwaysShift();
    void setAlwaysShift(boolean alwaysShift);
    List<Operation> getOperations(SentimentDependencyGraph sdg, short address);

//    readRules(String): void

}
