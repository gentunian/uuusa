package org.grupolys.spring.service;

import org.grupolys.dictionary.DefaultWordType;
import org.grupolys.dictionary.WordType;
import org.grupolys.profiles.PartOfSpeech;
import org.grupolys.spring.model.payloads.PostWordPayload;
import org.grupolys.spring.model.persistence.PersistentWord;
import org.grupolys.spring.service.exception.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Service
public class MigrationService {

    interface CreateOperation {
        PersistentWord create(String ...arg);
    }

    private WordsService wordsService;

    @Autowired
    MigrationService(WordsService wordsService) {
        this.wordsService = wordsService;
    }

    /**
     * Migrate words from table lookup files to mongo db.
     *
     * @param profileId the profile that holds the dictionary.
     * @param dictionaryId the dictionary id whree to put the words.
     * @param input the file input reader to read words from.
     */
    public void migrateLookupTablesByPartOfSpeech(String profileId,
                                                  String dictionaryId,
                                                  WordType wordType,
                                                  InputStreamReader input) {
        CreateOperation createOperation = (args) -> {
            PersistentWord persistentWord = null;

            try {
                String word = args[0];
                double value = Double.parseDouble(args[1]);
                persistentWord = getOrAddWord(buildWordPayload(word, profileId, dictionaryId));
                if (wordType.equals(DefaultWordType.BOOSTER)) {
                    persistentWord.setBooster(value);

                } else if (wordType.equals(DefaultWordType.EMOTICON)) {
                    persistentWord.setEmoticon(value);

                } else {
                    persistentWord.setWordValue((PartOfSpeech) wordType, value, null);
                }

            } catch (ServiceException | ClassCastException | IndexOutOfBoundsException | NumberFormatException e) {
                e.printStackTrace();
            }

            return persistentWord;
        };

        readTSVFile(input, 10000, createOperation);
    }

    /**
     * Migrate lemmas from table lookup files to database.
     *
     * @param profileId the profile ID
     * @param dictionaryId the dictionary ID where lemmas will be migrated
     * @param input the input file reader from where to take lemmas.
     */
    public void migrateLemmas(String profileId, String dictionaryId, InputStreamReader input) {
        CreateOperation createOperation = (args) -> {
            PersistentWord persistentWord = null;
            try {
                PartOfSpeech partOfSpeech = PartOfSpeech.getPartOfSpeech(args[0]);
                String word = args[1];
                String lemma = args[2];
                persistentWord = getOrAddWord(buildWordPayload(word, profileId, dictionaryId));
                persistentWord.getPartOfSpeech().get(partOfSpeech).setLemma(lemma);
            } catch (IndexOutOfBoundsException | ServiceException e) {
                e.printStackTrace();
            }
            return persistentWord;
        };

        readTSVFile(input, 10000, createOperation);
    }

    /**
     * Migrate negation words from file to database.
     *
     * @param profileId the profile ID.
     * @param dictionaryId the dictionary ID where to put words.
     * @param input the input file where to read words from.
     */
    public void migrateNegation(String profileId, String dictionaryId, InputStreamReader input) {
        CreateOperation createOperation = (args) -> {
            PersistentWord persistentWord = null;
            try {
                String word = args[0];
                persistentWord = getOrAddWord(buildWordPayload(word, profileId, dictionaryId));
                persistentWord.setNegating(true);
            } catch (IndexOutOfBoundsException | ServiceException e) {
                e.printStackTrace();
            }
            return persistentWord;
        };

        readTSVFile(input, 5000, createOperation);
    }

    /**
     * Helper method that returns a persistent word from database.
     */
    private PersistentWord getOrAddWord(PostWordPayload payload) throws ServiceException {
        PersistentWord persistentWord = wordsService.getWordByWord(payload.getWord());
        if (persistentWord == null) {
            persistentWord = wordsService.addWord(payload, false);
        }
        return persistentWord;
    }

    /**
     * Helper method that builds a payload object.
     */
    private PostWordPayload buildWordPayload(String word, String profileId, String dictionaryId) {
        PostWordPayload payload = new PostWordPayload();
        payload.setWord(word);
        payload.setProfile(profileId);
        payload.setDictionary(dictionaryId);
        return payload;
    }

    /**
     * Generic method to read from a TSV file.
     */
    private void readTSVFile(InputStreamReader input,
                             int bulkSize,
                             CreateOperation createOperation) {
        try(BufferedReader reader = new BufferedReader(input)) {
            String line;
            List<PersistentWord> words = new ArrayList<>();
            while ((line = reader.readLine()) != null) {
                if (words.size() == bulkSize) {
                    wordsService.store(words.get(0).getDictionary(), words, true);
                    words = new ArrayList<>();
                }
                if (!line.isEmpty()) {
                    words.add(createOperation.create(line.split("\t")));
                }
            }
            if (words.size() > 0) {
                wordsService.store(words.get(0).getDictionary(), words, true);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
