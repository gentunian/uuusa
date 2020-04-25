package org.grupolys.spring.repositories;

import org.grupolys.spring.model.persistence.PersistentWord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Set;

public interface WordsRepository extends MongoRepository<PersistentWord, String>, WordsExtraRepository {
    @Aggregation({"{ $lookup: { from: 'profiles', localField: 'dictionary', foreignField: 'activeDictionary', as: 'g'}}",
            "{ $match: { 'g': { $size: 1 }}}", "{ $project: { words: 0 }}"})
    List<PersistentWord> findAllFromActiveDictionaries();

    @Aggregation("{$group: {_id: '$dictionary' }}")
    Set<String> findDictionaries();

    @Query("{profile: ?#{[0]}, dictionary: ?#{[1]}}")
    List<PersistentWord> findByProfileAndDictionary(String profile, String dictionary);

    Page<PersistentWord> findAllBy(TextCriteria criteria, Pageable pageable);

    @Query("{word: { $regex: ?0, $options: 'i' }, dictionary: ?1}")
    Page<PersistentWord> findAllBySearchAndDictionary(String word, String dictionary, Pageable pageable);

    PersistentWord findByWord(String word);

    List<PersistentWord> findAllByDictionary(String dictionary);

}
