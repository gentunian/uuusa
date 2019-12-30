package org.grupolys.spring.service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

import org.grupolys.profiles.Profile;
import org.grupolys.samulan.analyser.AnalyserConfiguration;
import org.grupolys.samulan.analyser.RuleBasedAnalyser;
import org.grupolys.samulan.analyser.SyntacticRuleBasedAnalyser;
import org.grupolys.samulan.rule.RuleManager;
import org.grupolys.samulan.util.Dictionary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SamulanRulesService {
    private Map<String, RuleBasedAnalyser> rules = null;

    @Autowired
    SamulanRulesService(HazelcastInstance hz) {
        rules = new HashMap<String, RuleBasedAnalyser>();

        AnalyserConfiguration configuration = new AnalyserConfiguration();
        // HazelcastInstance hz = Hazelcast.getHazelcastInstanceByName(System.getenv("HOSTNAME"));
        IMap<String, Profile> map = hz.getMap("profileData");

        for (String profileName : map.keySet()) {
            Dictionary dictionary = new Dictionary();
            RuleBasedAnalyser rba = null;
            Profile profile = map.get(profileName);

            dictionary.setEmoticons(profile.getEmoticons());
            dictionary.setNegatingWords(new HashSet<String>(profile.getNegating()));
            dictionary.setClassValues(profile.getEmotions());

            RuleManager rm = new RuleManager(dictionary);
            rm.setAlwaysShift(configuration.isAlwaysShift());
            // TODO: Read rules should load configuration-ES.xml
            // rm.readRules(ns.getString(RULES));
            rba = new SyntacticRuleBasedAnalyser(configuration, rm);
            rules.put(profileName, rba);
        }
    }

    public Map<String, RuleBasedAnalyser> getRules() {
        return rules;
    }
}
