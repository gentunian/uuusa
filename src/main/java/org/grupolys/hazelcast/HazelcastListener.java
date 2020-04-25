package org.grupolys.hazelcast;

import com.hazelcast.core.EntryEvent;
import com.hazelcast.map.listener.EntryAddedListener;
import com.hazelcast.map.listener.EntryUpdatedListener;

import org.grupolys.dictionary.DefaultDictionary;
import org.grupolys.spring.service.SamulanRulesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class HazelcastListener implements
        EntryUpdatedListener<String, DefaultDictionary>,
        EntryAddedListener<String, DefaultDictionary> {

    private final SamulanRulesService rulesService;

    @Autowired
    public HazelcastListener(SamulanRulesService rulesService) {
        this.rulesService = rulesService;
    }

    @Override
    public void entryAdded(EntryEvent<String, DefaultDictionary> event) {
        System.out.println("Handling hazelcast event: Entry Added:" + event);
        String dictionaryId = event.getKey();
        DefaultDictionary dictionary = event.getValue();
        rulesService.loadRulesForProfile(dictionaryId, dictionary);
    }

    @Override
    public void entryUpdated(EntryEvent<String, DefaultDictionary> event) {
        System.out.println("handling hazelcast event: Entry updated:" + event);
        String dictionaryId = event.getKey();
        DefaultDictionary dictionary = event.getValue();
        rulesService.loadRulesForProfile(dictionaryId, dictionary);
    }
}
