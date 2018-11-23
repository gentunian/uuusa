package org.grupolys.spring.dao.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.map.listener.EntryAddedListener;
import com.hazelcast.map.listener.EntryUpdatedListener;

import org.grupolys.profiles.FilesystemProfileCreator;
import org.grupolys.profiles.Profile;
import org.grupolys.profiles.exception.ProfileNotFoundException;
import org.grupolys.samulan.analyser.AnalyserConfiguration;
import org.grupolys.samulan.analyser.RuleBasedAnalyser;
import org.grupolys.samulan.analyser.SyntacticRuleBasedAnalyser;
import org.grupolys.samulan.processor.Processor;
import org.grupolys.samulan.processor.parser.MaltParserWrapper;
import org.grupolys.samulan.processor.tagger.MaxentStanfordTagger;
import org.grupolys.samulan.processor.tokenizer.ARKTwokenizer;
import org.grupolys.samulan.rule.RuleManager;
import org.grupolys.samulan.util.Dictionary;

public class Utils {

    public static final String SHA256 = "SHA-256";

    private static Map<String, RuleBasedAnalyser> rules = null;
    static {
        rules = new HashMap<String, RuleBasedAnalyser>();

        AnalyserConfiguration configuration = new AnalyserConfiguration();
        HazelcastInstance hz = Hazelcast.getHazelcastInstanceByName(System.getenv("HOSTNAME"));
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

    private static Processor processor = null;
    static {
        ARKTwokenizer arktokenizer = new ARKTwokenizer();

        String taggerDir = "/opt/uuusa/data/taggers";

        File folderSentiData = new File(taggerDir);
        File[] files = folderSentiData.listFiles();
        String pathStanfordTagger = null;

        // This code looks for file ending with STANFORD_TAGGER_SUFFIX (.tagger)
        // and sets the absolute path in pathStanfordTagger
        for (File f : files) {
            if (f.getAbsolutePath().endsWith(".tagger")) {
                pathStanfordTagger = f.getAbsolutePath();
                break;
            }
        }
        // If no file was found throw an exception
        if (pathStanfordTagger == null) {
            System.out.println(".tagger file not found in SentiData");
        } else {
            // builds the tagger
            MaxentStanfordTagger tagger = new MaxentStanfordTagger(pathStanfordTagger);
            String parserDir = "/opt/uuusa/data/parsers/0";
            MaltParserWrapper parser = new MaltParserWrapper(parserDir);
            processor = new Processor(arktokenizer, tagger, parser);
        }
    }

    protected Utils() {
    }

    public static Processor getProcessorInstance() throws FileNotFoundException {
        return processor;
    }

    public static Map<String, RuleBasedAnalyser> getRules() {
        return rules;
    }

    public static void updateRules(String profileName) {
        FilesystemProfileCreator fpc = new FilesystemProfileCreator();
        HazelcastInstance hz = Hazelcast.getHazelcastInstanceByName(System.getenv("HOSTNAME"));
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

    public static String hash(String text) {
        String generatedPassword = null;
        try {
            MessageDigest md = MessageDigest.getInstance(SHA256);
            byte[] bytes = md.digest(text.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < bytes.length; i++) {
                sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
            }
            generatedPassword = sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return generatedPassword;
    }

    public static class MapListener implements EntryUpdatedListener<String, Profile>, EntryAddedListener<String, Profile> {

        @Override
        public void entryAdded(EntryEvent<String, Profile> event) {
            System.out.println("Handling hazelcast event: Entry Added:" + event);
            Map<String, RuleBasedAnalyser> rules = Utils.getRules();
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
            Map<String, RuleBasedAnalyser> rules = Utils.getRules();
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
}
