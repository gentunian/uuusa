package org.grupolys.spring.repositories;

import org.grupolys.spring.model.persistence.PersistentDictionary;
import org.grupolys.spring.model.persistence.PersistentDictionaryProfile;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ProfilesRepository extends MongoRepository<PersistentDictionaryProfile, String> {
    @Aggregation({"{ $match: { 'name': ?#{[0]}, dictionaries: { $exists: true, $not: { $size: 0 }} } }",
            "{ $project: { dictionaries: 1 }}"})
    List<PersistentDictionary> findProfileDictionaries(String name);

    @Aggregation({"{ $match: { '_id': ObjectId('?0'), 'dictionaries._id': ObjectId('?1')} }",
            "{ $addFields: { dictionaries: { $filter: {" +
                    "input: '$dictionaries', cond: { $eq: ['$$this._id', ObjectId('?1') ] } } } } }"})
    PersistentDictionaryProfile findByProfileAndDictionary(String id, String dictionaryId);

    PersistentDictionaryProfile findByName(String name);
}
