package org.grupolys.spring.repositories;

import org.grupolys.profiles.PartOfSpeech;
import org.grupolys.spring.model.persistence.PersistentWord2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.springframework.data.mongodb.core.query.Criteria.where;

public class Words2RepositoryImpl implements Words2ExtraRepository {

    @Autowired
    private final MongoTemplate mongoTemplate;
    @Autowired
    private MongoOperations mongoOperations;

    public Words2RepositoryImpl(MongoTemplate mongoTemplate, MongoOperations mongoOperations) {
        this.mongoTemplate = mongoTemplate;
        this.mongoOperations = mongoOperations;
    }

    @Override
    public List<PersistentWord2> findAllWordsByLemmas(String dictionary,
                                                      Map<PartOfSpeech, PersistentWord2.Value> values) {
        List<Criteria> criteriaList = new ArrayList<>();
        values.forEach((key, value) -> {
            String lemma = value.getLemma();
            if (lemma != null && !lemma.equals("")) {
                String posName = key.name();
                criteriaList.add(where("word").is(lemma)
                        .and("partOfSpeech." + posName).exists(true));
            }
        });

        Criteria criteria = new Criteria();
        if (!criteriaList.isEmpty()) {
            Criteria orCriteria = new Criteria();
            orCriteria = orCriteria.orOperator(criteriaList.toArray(new Criteria[0]));
            criteria.andOperator(where("dictionary").is(dictionary), orCriteria);
            return mongoTemplate.find(new Query(criteria), PersistentWord2.class);
        } else {
            return new ArrayList<>();
        }
    }

    @Override
    public Page<PersistentWord2> findAllBySearch(String search,
                                                 String dictionary,
                                                 PartOfSpeech[] pos,
                                                 Pageable pageable) {
        List<Criteria> criteriaList = new ArrayList<>();

        if (search != null) {
            criteriaList.add(where("word").regex(search));
        }

        if (dictionary != null) {
            criteriaList.add(where("dictionary").is(dictionary));
        }

        if (pos != null) {
            for (PartOfSpeech partOfSpeech : pos) {
                criteriaList.add(where("partOfSpeech." + partOfSpeech.name()).exists(true));
            }
        }
        Criteria criteria = new Criteria();
        criteria.andOperator(criteriaList.toArray(new Criteria[0]));
        Query query = new Query(criteria).with(pageable);
        List<PersistentWord2> words = mongoTemplate.find(query, PersistentWord2.class);
        long count = mongoOperations.count(query, PersistentWord2.class);
        return new PageImpl<>(words , pageable, count);
    }
}
