package org.grupolys.hazelcast;

import com.hazelcast.core.EntryEvent;
import com.hazelcast.map.listener.EntryAddedListener;
import com.hazelcast.map.listener.EntryUpdatedListener;

import org.grupolys.dictionary.WordsDictionary;
import org.grupolys.spring.service.SamulanRulesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class HazelcastListener implements
        EntryUpdatedListener<String, WordsDictionary>,
        EntryAddedListener<String, WordsDictionary> {

    @Autowired
    private SamulanRulesService rulesService;

    @Override
    public void entryAdded(EntryEvent<String, WordsDictionary> event) {
        System.out.println("Handling hazelcast event: Entry Added:" + event);
        String dictionaryId = event.getKey();
        WordsDictionary dictionary = event.getValue();

        rulesService.loadRulesForProfile(dictionaryId, dictionary);
    }

    @Override
    public void entryUpdated(EntryEvent<String, WordsDictionary> event) {
        System.out.println("handling hazelcast event: Entry updated:" + event);
        String dictionaryId = event.getKey();
        WordsDictionary dictionary = event.getValue();

        rulesService.loadRulesForProfile(dictionaryId, dictionary);
    }
}
