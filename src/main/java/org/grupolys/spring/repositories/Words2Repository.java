package org.grupolys.spring.repositories;

import org.grupolys.spring.model.persistence.PersistentWord2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Set;

public interface Words2Repository extends MongoRepository<PersistentWord2, String>, Words2ExtraRepository  {
    @Aggregation({"{ $lookup: { from: 'profiles', localField: 'dictionary', foreignField: 'activeDictionary', as: 'g'}}",
            "{ $match: { 'g': { $size: 1 }}}", "{ $project: { words: 0 }}"})
    List<PersistentWord2> findAllFromActiveDictionaries();

    @Aggregation("{$group: {_id: '$dictionary' }}")
    Set<String> findDictionaries();

    @Query("{profile: ?#{[0]}, dictionary: ?#{[1]}}")
    List<PersistentWord2> findByProfileAndDictionary(String profile, String dictionary);

    Page<PersistentWord2> findAllBy(TextCriteria criteria, Pageable pageable);

    @Query("{word: { $regex: ?0, $options: 'i' }, dictionary: ?1}")
    Page<PersistentWord2> findAllBySearchAndDictionary(String word, String dictionary, Pageable pageable);

    PersistentWord2 findByWord(String word);

    List<PersistentWord2> findAllByDictionary(String dictionary);

}
