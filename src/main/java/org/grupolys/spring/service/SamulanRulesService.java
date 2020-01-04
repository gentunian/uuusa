package org.grupolys.spring.service;

import java.util.HashMap;
import java.util.Map;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

import org.grupolys.profiles.Profile;
import org.grupolys.samulan.analyser.AnalyserConfiguration;
import org.grupolys.samulan.analyser.RuleBasedAnalyser;
import org.grupolys.samulan.analyser.SyntacticRuleBasedAnalyser;
import org.grupolys.samulan.rule.RulesManager;
import org.grupolys.samulan.rule.XmlRulesManager;
import org.grupolys.samulan.util.dictionary.Dictionary;
import org.grupolys.samulan.util.dictionary.FilesystemDictionary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SamulanRulesService {
    private Map<String, RuleBasedAnalyser> rules = null;

    private final HazelcastInstance hz;
    private final ConfigService configService;
    private final AnalyserConfiguration configuration;

    @Autowired
    SamulanRulesService(HazelcastInstance hz, ConfigService configService) {
        rules = new HashMap<>();
        this.hz = hz;
        this.configService = configService;
        this.configuration = new AnalyserConfiguration();

        // HazelcastInstance hz = Hazelcast.getHazelcastInstanceByName(System.getenv("HOSTNAME"));
        IMap<String, Profile> map = hz.getMap("profileData");

        for (String profileName : map.keySet()) {
            loadRulesForProfile(profileName, map.get(profileName));
        }
    }

    public void loadRulesForProfile(String profileName, Profile profile) {
        Dictionary dictionary = new FilesystemDictionary(configService.UUUSA_PROFILES_PATH + "/default");

        RulesManager rm = new XmlRulesManager(dictionary, configService.UUUSA_PATH + "/config/configuration-ES.xml");
        rm.setAlwaysShift(configuration.isAlwaysShift());
//        rm.readRules(configService.UUUSA_PATH + "/config/configuration-ES.xml" );
        rules.put(profileName, new SyntacticRuleBasedAnalyser(configuration, rm));
    }

    public Map<String, RuleBasedAnalyser> getRules() {
        return rules;
    }
}
