package org.grupolys.spring.repositories;

import org.grupolys.spring.model.persistence.PersistentAnalysis;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SentimentRepository extends MongoRepository<PersistentAnalysis, String> {

    PersistentAnalysis findByTextAndDictionary(String text, String dictionary);
}
