package org.grupolys.hazelcast;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

import org.grupolys.profiles.Profile;
import org.grupolys.profiles.ProfileCreator;
import org.grupolys.profiles.exception.ProfileNotFoundException;
import org.grupolys.spring.service.ConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class HazelcastCache {

    private final HazelcastInstance hazelcastInstance;

    private final ProfileCreator profileCreator;

    @Autowired
    HazelcastCache(HazelcastInstance hazelcastInstance,
                   HazelcastListener hzl,
                   ConfigService configService,
                   ProfileCreator profileCreator) {
        String directory = ConfigService.UUUSA_PROFILES_PATH; //FilesystemProfileCreator.PROFILES_DIR;
        IMap<String, Profile> map = hazelcastInstance.getMap("profileData");
        map.addEntryListener(hzl, true);

//        FilesystemProfileCreator fpc = new FilesystemProfileCreator();

        // FilesystemProfileCreator.PROFILES_DIR;
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(directory))) {
            for (Path path : directoryStream) {
                String profileName = path.getFileName().toString();
                if (!map.containsKey(profileName)) {
                    map.lock(profileName);
                    try {
                        Profile profile = profileCreator.loadProfile(profileName);
                        map.put(profileName, profile);
                    } catch (ProfileNotFoundException e) {
                        e.printStackTrace();
                    } finally {
                        map.unlock(profileName);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.hazelcastInstance = hazelcastInstance;
        this.profileCreator = profileCreator;
        // RuleBasedAnalyser rba = prepare(fspc.loadProfile(profileName));
        // Processor processor = prepareProcessor("/opt/uuusa/data/profiles/" +
        // profileName);
        // Map<String, RuleBasedAnalyser> rulesPerProfile;
        // u.set("profile", rba);
    }

    public void updateRules(String profileName) {
//        FilesystemProfileCreator fpc = new FilesystemProfileCreator();
        IMap<String, Profile> map = hazelcastInstance.getMap("profileData");
        map.lock(profileName);
        try {
            Profile profile = profileCreator.loadProfile(profileName);
            map.put(profileName, profile);
        } catch (ProfileNotFoundException e) {
            e.printStackTrace();
        }
        map.unlock(profileName);
    }
}
