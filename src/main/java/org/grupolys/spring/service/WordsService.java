package org.grupolys.spring.service;

import org.grupolys.hazelcast.HazelcastCache;
import org.grupolys.profiles.PartOfSpeech;
import org.grupolys.spring.model.mappers.WordMapper;
import org.grupolys.spring.model.payloads.PostWordPayload;
import org.grupolys.spring.model.persistence.PersistentDictionaryProfile;
import org.grupolys.spring.model.persistence.PersistentWord;
import org.grupolys.spring.model.responses.ErrorResponse;
import org.grupolys.spring.repositories.WordsRepository;
import org.grupolys.spring.service.exception.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class WordsService {

    private final ProfilesService profilesService;
    private final WordsRepository wordsRepository;
    private final WordMapper wordMapper;
    private final HazelcastCache cache;

    @Autowired
    WordsService(ProfilesService profilesService,
                 WordsRepository wordsRepository,
                 WordMapper wordMapper,
                 @Lazy HazelcastCache cache) {
        this.profilesService = profilesService;
        this.wordsRepository = wordsRepository;
        this.wordMapper = wordMapper;
        this.cache = cache;
    }

    /**
     * Adds a word to the database and notifies if updateCache if true.
     * @param word the word to be added.
     * @param updateCache whether or not notify should be called.
     * @return the persistent representation of the word.
     * @throws ServiceException if something goes wrong.
     */
    public PersistentWord addWord(PostWordPayload word, boolean updateCache) throws ServiceException {
        PersistentDictionaryProfile profile = profilesService.getProfile(word.getProfile(), word.getDictionary());
        if (profile.getDictionaries().size() == 0) {
            throw new ServiceException(
                    new ErrorResponse("Profile '" + word.getProfile() + "'does not have any dictionaries yet.",
                            HttpStatus.BAD_REQUEST
                    )
            );
        }

        String wordId = getWordIdForWord(word.getWord(), word.getDictionary());
        if (wordId != null) {
            throw new ServiceException(
                    new ErrorResponse("Word already exists.", "/words/" + wordId, HttpStatus.CONFLICT)
            );
        }

        PersistentWord savedWord = wordsRepository.save(wordMapper.toPersistentWord(word));
        if (updateCache) {
            cache.onWordAdded(savedWord);
        }
        return savedWord;
    }

    /**
     * Returns the id of the word that matches exactly with `word`.
     * @param word the word to find.
     * @return the id of the word
     */
    public String getWordIdForWord(String word, String dictionaryId) {
        PersistentWord existingWord = wordsRepository.findByWordAndDictionary(word, dictionaryId);
        if (existingWord != null) {
            return existingWord.getId();
        }
        return null;
    }

    /**
     * Gets a word by id.
     * @param id the id of the word.
     * @return the word containing the id.
     * @throws ServiceException thrown is no word is found.
     */
    public PersistentWord getWordById(String id) throws ServiceException {
        PersistentWord word = wordsRepository.findById(id).orElse(null);
        if (word == null) {
            throw new ServiceException(
                    new ErrorResponse("Word with id '" + id + "' not found.", HttpStatus.NOT_FOUND)
            );
        }
        return word;
    }

    /**
     * Gets a word by name.
     * @param word the word name.
     * @return the word that matches exactly the name
     * @throws ServiceException if the word was not found.
     */
    public PersistentWord getWordByWord(String word) throws ServiceException {
        PersistentWord persistentWord = wordsRepository.findByWord(word);
        if (word == null) {
            throw new ServiceException(
                    new ErrorResponse("Word '" + word + "' not found.", HttpStatus.NOT_FOUND)
            );
        }
        return persistentWord;
    }
    /**
     * Paginates over the words.
     * @param search the term to search.
     * @param dictionary dictionary id.
     * @param partOfSpeech part of speeches
     * @param pageNumber page number
     * @param pageSize page size
     * @return a page containing the results.
     */
    public Page<PersistentWord> searchForWord(String search,
                                              String dictionary,
                                              PartOfSpeech[] partOfSpeech,
                                              Integer pageNumber,
                                              Integer pageSize) {
        Sort sort = Sort.by("word");
        PageRequest pageRequest = PageRequest.of(pageNumber, pageSize, sort);
        return  wordsRepository.findAllBySearch(search, dictionary, partOfSpeech, pageRequest);
    }

    /**
     * Edits a word by id.
     * @param wordId the word id.
     * @param payload payload that changes the word.
     * @param updateCache if in memory cache should be updated.
     * @return the persistent word representation.
     * @throws ServiceException if edit was not possible.
     */
    public PersistentWord editWord(String wordId, PersistentWord payload, boolean updateCache) throws ServiceException {
        PersistentWord word = wordsRepository.findById(wordId).orElse(null);
        if (word == null) {
            throw new ServiceException(
                    new ErrorResponse("Word not found", HttpStatus.NOT_FOUND)
            );
        } else if (!word.getDictionary().equals(payload.getDictionary())) {
           throw new ServiceException(
                    new ErrorResponse("You can't move word to another dictionary. " +
                            "Word belongs to dictionary '" + word.getDictionary() + "'", HttpStatus.BAD_REQUEST)
            );
        } else if (!word.getProfile().equals(payload.getProfile())) {
            throw new ServiceException(
                    new ErrorResponse("You can't move word to another profile. " +
                            "Word belongs to profile '" + word.getProfile() + "'", HttpStatus.BAD_REQUEST)
            );
        } else if (!word.getLanguage().equals(payload.getLanguage())) {
            throw new ServiceException(
                    new ErrorResponse("You can't change word language. This is managed by the dictionary",
                            HttpStatus.BAD_REQUEST
                    )
            );
        } else if (!word.getWord().equals(payload.getWord())) {
            throw new ServiceException(new ErrorResponse("You can't change the word. " +
                    "Word '" + payload.getWord() + "' is not valid.", HttpStatus.BAD_REQUEST));
        } else {
            payload.setId(word.getId());
            payload.setLanguage(word.getLanguage());
            if (updateCache) {
                cache.onWordUpdated(payload);
            }
            return wordsRepository.save(payload);
        }
    }

    /**
     *
     * @param dictionaryId
     * @return
     */
    public List<PersistentWord> getWordsFromDictionary(String dictionaryId) {
        return wordsRepository.findAllByDictionary(dictionaryId);
    }

    /**
     *
     * @return
     */
    public Set<String> getDictionaries() {
        return  wordsRepository.findDictionaries();
    }

    public List<PersistentWord> store(String dictionaryId,
                                      List<PersistentWord> persistentWords,
                                      boolean updateCache) {
        List<PersistentWord> savedWords = wordsRepository.saveAll(persistentWords);
        if (updateCache) {
            cache.lockAndReloadDictionary(dictionaryId, persistentWords);
        }
        return savedWords;
    }
}
