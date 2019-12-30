package org.grupolys.hazelcast;

import java.util.HashSet;
import java.util.Map;

import com.hazelcast.core.EntryEvent;
import com.hazelcast.map.listener.EntryAddedListener;
import com.hazelcast.map.listener.EntryUpdatedListener;

import org.grupolys.profiles.Profile;
import org.grupolys.samulan.analyser.AnalyserConfiguration;
import org.grupolys.samulan.analyser.RuleBasedAnalyser;
import org.grupolys.samulan.analyser.SyntacticRuleBasedAnalyser;
import org.grupolys.samulan.rule.RuleManager;
import org.grupolys.samulan.util.Dictionary;
import org.grupolys.spring.service.SamulanRulesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class HazelcastListener implements EntryUpdatedListener<String, Profile>, EntryAddedListener<String, Profile> {

    @Autowired
    private SamulanRulesService rulesService;

    @Override
    public void entryAdded(EntryEvent<String, Profile> event) {
        System.out.println("Handling hazelcast event: Entry Added:" + event);
        Map<String, RuleBasedAnalyser> rules = this.rulesService.getRules();
        String profileName = event.getKey();
        Profile profile = event.getValue();

        RuleBasedAnalyser rba = null;
        AnalyserConfiguration configuration = new AnalyserConfiguration();
        Dictionary dictionary = new Dictionary();

        dictionary.setEmoticons(profile.getEmoticons());
        dictionary.setNegatingWords(new HashSet<String>(profile.getNegating()));
        dictionary.setClassValues(profile.getEmotions());

        RuleManager rm = new RuleManager(dictionary);
        rm.setAlwaysShift(configuration.isAlwaysShift());
        rba = new SyntacticRuleBasedAnalyser(configuration, rm);
        rules.put(profileName, rba);
    }

    @Override
    public void entryUpdated(EntryEvent<String, Profile> event) {
        System.out.println("handling hazelcast event: Entry updated:" + event);
        Map<String, RuleBasedAnalyser> rules = this.rulesService.getRules();
        String profileName = event.getKey();
        Profile profile = event.getValue();

        RuleBasedAnalyser rba = null;
        AnalyserConfiguration configuration = new AnalyserConfiguration();
        Dictionary dictionary = new Dictionary();

        dictionary.setEmoticons(profile.getEmoticons());
        dictionary.setNegatingWords(new HashSet<String>(profile.getNegating()));
        dictionary.setClassValues(profile.getEmotions());

        RuleManager rm = new RuleManager(dictionary);
        rm.setAlwaysShift(configuration.isAlwaysShift());
        rba = new SyntacticRuleBasedAnalyser(configuration, rm);
        rules.put(profileName, rba);
    }
}
