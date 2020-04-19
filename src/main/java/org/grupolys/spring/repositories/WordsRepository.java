package org.grupolys.spring.repositories;

import org.grupolys.spring.model.persistence.PersistentWord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.CountQuery;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

@Deprecated
public interface WordsRepository extends MongoRepository<PersistentWord, String>, UpdateWordsRepository {

    @Aggregation({"{ $lookup: { from: 'profiles', localField: 'dictionary', foreignField: 'activeDictionary', as: 'g'}}",
    "{ $match: { 'g': { $size: 1 }}}", "{ $project: { words: 0 }}"})
    List<PersistentWord> findAllFromActiveDictionaries();

    @Query("{profile: ?#{[0]}, dictionary: ?#{[1]}}")
    List<PersistentWord> findByProfileAndDictionary(String profile, String dictionary);
    Page<PersistentWord> findAllBy(TextCriteria criteria, Pageable pageable);

    @Query("{word: { $regex: ?0, $options: 'i' " +
            "}, profile: ?1, dictionary: ?2}")
    Page<PersistentWord> findAllByWord(String word, String profile, String dictionary, Pageable pageable);

    @CountQuery("{ profile: ?0, dictionary: ?1 }")
    long countWordsInDictionary(String profileId, String dictionaryId);

    List<PersistentWord> findAllByDictionary(String dictionary);

    @Query("{profile: ?0, dictionary: ?1, language: ?2, word: ?3, partOfSpeech: ?4}")
    PersistentWord findByPrimaryKey(String profile,
                                          String dictionary,
                                          String language,
                                          String word,
                                          String partOfSpeech);
}
