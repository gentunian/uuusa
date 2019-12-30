package org.grupolys;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

import org.grupolys.hazelcast.HazelcastListener;
import org.grupolys.profiles.FilesystemProfileCreator;
import org.grupolys.profiles.Profile;
import org.grupolys.profiles.exception.ProfileNotFoundException;
import org.grupolys.samulan.Samulan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;

@SpringBootApplication
public class Application {

    public static void main(String[] args) throws FileNotFoundException {
        if (springboot(args)) {
            ApplicationContext context = SpringApplication.run(Application.class, args);
            // HazelcastInitializer hzi = context.getBean(HazelcastInitializer.class);
            // initDataFromFilesystem(hz, FilesystemProfileCreator.PROFILES_DIR);
            // Utils.getProcessorInstance();
            // Utils.getRules();
        } else {
            Samulan.main(args);
        }
    }

    private static boolean springboot(String[] args) {
        ArgumentParser ap = ArgumentParsers.newArgumentParser("Samulan").defaultHelp(true)
                .description("Apply Sentiment Analysis to files");
        ap.addArgument("-springboot", "--springboot").required(false).help("Spring boot the application.")
                .action(Arguments.storeTrue());
        try {
            return ap.parseArgs(args).getBoolean("springboot");
        } catch (ArgumentParserException e) {
            return false;
        }
    }

//    private static void initDataFromFilesystem(HazelcastInstance hz, String directory) {
        // Config cfg = new Config(System.getenv("HOSTNAME"));
        // HazelcastInstance hz = Hazelcast.newHazelcastInstance(cfg);
//        IMap<String, Profile> map = hz.getMap("profileData");
//        map.addEntryListener(new HazelcastListener(), true);

//        FilesystemProfileCreator fpc = new FilesystemProfileCreator();

        // FilesystemProfileCreator.PROFILES_DIR;
//        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(directory))) {
//            for (Path path : directoryStream) {
//                String profileName = path.getFileName().toString();
//                if (!map.containsKey(profileName)) {
//                    map.lock(profileName);
//                    try {
//                        Profile profile = fpc.loadProfile(profileName);
//                        map.put(profileName, profile);
//                    } catch (ProfileNotFoundException e) {
//                        e.printStackTrace();
//                    } finally {
//                        map.unlock(profileName);
//                    }
//                }
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        // RuleBasedAnalyser rba = prepare(fspc.loadProfile(profileName));
        // Processor processor = prepareProcessor("/opt/uuusa/data/profiles/" +
        // profileName);
        // Map<String, RuleBasedAnalyser> rulesPerProfile;

        // u.set("profile", rba);
//    }
}
