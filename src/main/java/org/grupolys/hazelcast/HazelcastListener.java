package org.grupolys.hazelcast;

import com.hazelcast.core.EntryEvent;
import com.hazelcast.map.listener.EntryAddedListener;
import com.hazelcast.map.listener.EntryUpdatedListener;

import org.grupolys.profiles.Profile;
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
        String profileName = event.getKey();
        Profile profile = event.getValue();

        rulesService.loadRulesForProfile(profileName, profile);
    }

    @Override
    public void entryUpdated(EntryEvent<String, Profile> event) {
        System.out.println("handling hazelcast event: Entry updated:" + event);
        String profileName = event.getKey();
        Profile profile = event.getValue();

        rulesService.loadRulesForProfile(profileName, profile);
    }
}
