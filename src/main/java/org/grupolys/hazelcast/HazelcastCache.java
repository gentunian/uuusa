package org.grupolys.hazelcast;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

import org.grupolys.profiles.FilesystemProfileCreator;
import org.grupolys.profiles.Profile;
import org.grupolys.profiles.exception.ProfileNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class HazelcastCache {

    private HazelcastInstance hz;

    @Autowired
    HazelcastCache(HazelcastInstance hz, HazelcastListener hzl) {
        String directory = FilesystemProfileCreator.PROFILES_DIR;
        IMap<String, Profile> map = hz.getMap("profileData");
        map.addEntryListener(hzl, true);

        FilesystemProfileCreator fpc = new FilesystemProfileCreator();

        // FilesystemProfileCreator.PROFILES_DIR;
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(directory))) {
            for (Path path : directoryStream) {
                String profileName = path.getFileName().toString();
                if (!map.containsKey(profileName)) {
                    map.lock(profileName);
                    try {
                        Profile profile = fpc.loadProfile(profileName);
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

        this.hz = hz;
        // RuleBasedAnalyser rba = prepare(fspc.loadProfile(profileName));
        // Processor processor = prepareProcessor("/opt/uuusa/data/profiles/" +
        // profileName);
        // Map<String, RuleBasedAnalyser> rulesPerProfile;
        // u.set("profile", rba);
    }

    public void updateRules(String profileName) {
        FilesystemProfileCreator fpc = new FilesystemProfileCreator();
        IMap<String, Profile> map = hz.getMap("profileData");
        map.lock(profileName);
        try {
            Profile profile = fpc.loadProfile(profileName);
            map.put(profileName, profile);
        } catch (ProfileNotFoundException e) {
            e.printStackTrace();
        }
        map.unlock(profileName);
    }
}
