package org.grupolys.spring.service;

import java.util.HashMap;
import java.util.Map;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

import com.mongodb.client.MongoClient;
import org.grupolys.dictionary.WordsDictionary;
import org.grupolys.profiles.DictionaryProfile;
import org.grupolys.samulan.analyser.AnalyserConfiguration;
import org.grupolys.samulan.analyser.RuleBasedAnalyser;
import org.grupolys.samulan.analyser.SyntacticRuleBasedAnalyser;
import org.grupolys.samulan.rule.RulesManager;
import org.grupolys.samulan.rule.XmlRulesManager;
import org.grupolys.samulan.util.dictionary.Dictionary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SamulanRulesService {
    private Map<String, RuleBasedAnalyser> rules = null;

    private final HazelcastInstance hz;
    private final ConfigService configService;
    private final AnalyserConfiguration configuration;
//    @Autowired private MongoClient mongoClient;

    @Autowired
    SamulanRulesService(HazelcastInstance hz, ConfigService configService) {
        rules = new HashMap<>();
        this.hz = hz;
        this.configService = configService;
        this.configuration = new AnalyserConfiguration();

        // HazelcastInstance hz = Hazelcast.getHazelcastInstanceByName(System.getenv("HOSTNAME"));
        IMap<String, WordsDictionary> map = hz.getMap("profileData");

        for (String profileName : map.keySet()) {
            loadRulesForProfile(profileName, map.get(profileName));
        }
    }

    public void loadRulesForProfile(String profileName, WordsDictionary dictionary) {
        // TODO: configuration-ES.xml port to mongo
        RulesManager rm = new XmlRulesManager(dictionary, configService.UUUSA_PATH + "/config/configuration-ES.xml");
        rm.setAlwaysShift(configuration.isAlwaysShift());
        rules.put(profileName, new SyntacticRuleBasedAnalyser(configuration, rm));

        /* I think this should be the code */

//        Dictionary dictionary = profile.getActiveDictionary();
//        RulesManager rm = new XmlRulesManager(dictionary, configService.UUUSA_PATH + "/config/configuration-ES.xml");
//        rm.setAlwaysShift(configuration.isAlwaysShift());
//        rules.put(profileName, new SyntacticRuleBasedAnalyser(configuration, rm));


//        Dictionary dictionary = new FilesystemDictionary(configService.UUUSA_PROFILES_PATH + "/default");
//
//        RulesManager rm = new XmlRulesManager(dictionary, configService.UUUSA_PATH + "/config/configuration-ES.xml");
//        rm.setAlwaysShift(configuration.isAlwaysShift());
////        rm.readRules(configService.UUUSA_PATH + "/config/configuration-ES.xml" );
//        rules.put(profileName, new SyntacticRuleBasedAnalyser(configuration, rm));
    }

    public Map<String, RuleBasedAnalyser> getRules() {
        return rules;
    }
}
