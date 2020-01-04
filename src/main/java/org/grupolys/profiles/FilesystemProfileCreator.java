package org.grupolys.profiles;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.Arrays;

import org.grupolys.profiles.exception.ProfileNotFoundException;
import org.grupolys.samulan.util.dictionary.FilesystemDictionary;
import org.grupolys.spring.service.ConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(prefix = "profileCreator", name = "Impl", havingValue = "Filesystem")
public class FilesystemProfileCreator implements ProfileCreator {

    public class MyException extends Exception {
        private static final long serialVersionUID = 1L;

        MyException(Throwable e) {
            super(e);
        }
    }

    private ConfigService configService;

    @Autowired
    public final void setConfigService(ConfigService configService) {
        this.configService = configService;
    }

    public static final String DEFAULT_TAGGER = "es-bidirectional-distsim.tagger.tagger";
//    public static final String DATA_DIR = "/opt/uuusa/data";
//    public static final String PROFILES_DIR = configService.UUUSA_PROFILES_PATH; // DATA_DIR + File.separator + "profiles";
//    public static final String TAGGERS_DIR = configService.UUUSA_TAGGERS_PATH; // DATA_DIR + File.separator + "taggers";
//    public static final String PARSERS_DIR = configService.UUUSA_PARSERS_PATH; // DATA_DIR + File.separator + "parsers";

    @Override
    public boolean saveProfile(String profileName, Profile profile) {
        String profileDirectory = configService.UUUSA_PROFILES_PATH + File.separator + profileName;

        // if the directory already exists this will return false
        // we need to check for dir existence anyhow
        createProfileDirectory(profileDirectory);
        assignDefaultParser(profileDirectory + File.separator + "maltparser");
        assignDefaultTagger(profileDirectory);

        try {
            // Gets the emotions categorized as part of speech.
            // Each emotion is classified as adverb, noun, verb and adjetive.
            Map<String, Map<String, Float>> emotions = profile.getEmotions();
            for (String key : emotions.keySet()) {
                String partOfSpeech = getPartOfSpeech(key);
                if (partOfSpeech != null) {
                    String filename = getEmotionsFilePath(profileDirectory, partOfSpeech);
                    saveMapOfWeights(filename, emotions.get(key));
                }
            }
            saveMapOfWeights(getBoosterFilePath(profileDirectory), profile.getBoosters());
            saveMapOfWeights(getEmoticonsFilePath(profileDirectory), profile.getEmoticons());
            saveListOfWords(getNegatingFilePath(profileDirectory), profile.getNegating());
        } catch (MyException e) {
            // nothing to save
            e.printStackTrace();
        } catch (NullPointerException e) {
            // nothing to save
            e.printStackTrace();
        }

        return false;
    }

    private void saveListOfWords(String filename, List<String> words) throws MyException {
        // We are going to use PrintWriter to write text into the file
        PrintWriter printWriter = null;

        try {
            printWriter = new PrintWriter(filename, StandardCharsets.UTF_8.name());
            for (String word : words) {
                printWriter.printf("%s\n", word);
            }
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            throw new MyException(e);
        } finally {
            if (printWriter != null) {
                printWriter.close();
            }
        }
    }

    private void saveMapOfWeights(String filename, Map<String, Float> weights) throws MyException {
        // We are going to use PrintWriter to write text into the file
        PrintWriter printWriter = null;

        try {
            printWriter = new PrintWriter(filename, StandardCharsets.UTF_8.name());
            for (String word : weights.keySet()) {
                Float weight = weights.get(word);
                printWriter.printf("%s\t%s\n", word, weight);
            }
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            throw new MyException(e);
        } finally {
            if (printWriter != null) {
                printWriter.close();
            }
        }
    }

    private String getPartOfSpeech(String alias) {
        return PartOfSpeech.getPartOfSpeech(alias);
    }

    private boolean createProfileDirectory(String profileDirectory) {
        return new File(profileDirectory + File.separator + "maltparser").mkdirs();
    }

    private void assignParser() {

    }

    private void assignDefaultParser(String profileParserDirectory) {
        String defaultParserDir = configService.UUUSA_PARSERS_PATH + File.separator + '0';
        Stream<Path> s = null;

        try {
            s = Files.list(Paths.get(defaultParserDir));
            Iterator<Path> it = s.iterator();
            while (it.hasNext()) {
                Path targetPath = it.next();
                Path linkPath = Paths.get(profileParserDirectory, targetPath.getFileName().toString());
                Files.createSymbolicLink(linkPath, targetPath);
            }
        } catch (FileAlreadyExistsException e) {
            System.out.println("Symbolic link for parser " + defaultParserDir + "already exists: ");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (s != null) {
                s.close();
            }
        }
    }

    private void assignDefaultTagger(String profileDirectory) {
        String target = configService.UUUSA_TAGGERS_PATH + File.separator + DEFAULT_TAGGER;
        String link = profileDirectory + File.separator + DEFAULT_TAGGER;

        try {
            Files.createSymbolicLink(Paths.get(link), Paths.get(target));
        } catch (FileAlreadyExistsException e) {
            System.out.println("Symbolic link for tagger " + target + "already exists: " + link);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Profile loadProfile(String profileName) throws ProfileNotFoundException {
        String profileDirectory = configService.UUUSA_PROFILES_PATH + File.separator + profileName;

        if (!(new File(profileDirectory)).isDirectory()) {
            throw new ProfileNotFoundException("Profile not found at: " + profileDirectory);
        }

        Profile profile = new Profile();
        Map<String, Map<String, Float>> emotions = new HashMap<String, Map<String, Float>>();
        for (PartOfSpeech pos : PartOfSpeech.values()) {
            String emotionLookupFile = getEmotionsFilePath(profileDirectory, pos.name());
            Map<String, Float> m = loadWordWeightFile(emotionLookupFile);
            if (m != null) {
                emotions.put(pos.name(), m);
            }
        }
        profile.setEmotions(emotions);

        Map<String, Float> boosters = loadWordWeightFile(getBoosterFilePath(profileDirectory));
        if (boosters == null) {
            // if null, create an empty map
            boosters = new HashMap<String, Float>();
        }
        profile.setBoosters(boosters);

        Map<String, Float> emoticons = loadWordWeightFile(getEmoticonsFilePath(profileDirectory));
        if (emoticons == null) {
            // if null, create an empty map
            emoticons = new HashMap<String, Float>();
        }
        profile.setEmoticons(emoticons);

        List<String> negating = loadNegatingWordsFile(getNegatingFilePath(profileDirectory));
        if (negating == null) {
            // if null, create an empty list
            negating = new ArrayList<String>();
        }
        profile.setNegating(negating);

        return profile;
    }

    private Map<String, Float> loadWordWeightFile(String file) {
        Map<String, Float> map = null;
        try (Stream<String> stream = Files.lines(Paths.get(file))) {
            map = stream.filter(Utils::notEmpty).map(Utils::split)
                    .collect(Collectors.toMap(Utils::getWord, Utils::getWeight, Utils::noDuplicates));
        } catch (NoSuchFileException e) {
            System.out.println("Warning: loading profile weights file. No such file: " + file);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return map;
    }

    private List<String> loadNegatingWordsFile(String file) {
        List<String> words = null;
        try (Stream<String> stream = Files.lines(Paths.get(file))) {
            words = stream.filter(Utils::notEmpty).collect(Collectors.toList());
        } catch (NoSuchFileException e) {
            System.out.println("Warning: loading profile negating words file. No such file: " + file);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return words;
    }

    @Override
    public String[] profiles() {
        String profileDirectory = configService.UUUSA_PROFILES_PATH + File.separator ;

        return  Arrays
            .stream(new File(profileDirectory).listFiles(File::isDirectory))
            .map(File::getName)
            .toArray(String[]::new);

    }

    private String getBoosterFilePath(String dir) {
        return Paths.get(dir, FilesystemDictionary.LookupFiles.BOOSTER_LIST.getFilename()).toString();
    }

    private String getEmotionsFilePath(String dir, String pos) {
        return Paths.get(dir, pos +
                FilesystemDictionary.LookupFiles.POSTAG_SEPARATOR.getFilename() +
                FilesystemDictionary.LookupFiles.EMOTION_LIST.getFilename())
                .toString();
    }

    private String getEmoticonsFilePath(String dir) {
        return Paths.get(dir, FilesystemDictionary.LookupFiles.EMOTICON_LIST.getFilename()).toString();
    }

    private String getNegatingFilePath(String dir) {
        return Paths.get(dir, FilesystemDictionary.LookupFiles.NEGATING_LIST.getFilename()).toString();
    }
}
