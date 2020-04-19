package org.grupolys.spring.repositories;

import org.bson.Document;
import org.grupolys.spring.model.persistence.PersistentWord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.List;

import static org.springframework.data.mongodb.core.query.Query.query;
import static org.springframework.data.mongodb.core.query.Update.update;
import static org.springframework.data.mongodb.core.query.Criteria.where;


public class UpdateWordsRepositoryImpl implements UpdateWordsRepository {

    @Autowired
    private final MongoTemplate mongoTemplate;

    public UpdateWordsRepositoryImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public List<PersistentWord> upsertWord(PersistentWord word) {
        mongoTemplate.save(word);
        Update update = setUpdateForKey(null, "negating", word.getNegating());
        update = setUpdateForKey(update, "booster", word.getBooster());

        // update all the words that matches the profile, dictionary and word from `word`
        // to set booster and negating values.
        if (update != null) {
            Query query = query(
                    where("dictionary").is(word.getDictionary())
                            .and("profile").is(word.getProfile())
                            .and("word").is(word.getWord()));
            mongoTemplate.upsert(query, update, PersistentWord.class);
        }
        return null;
    }
//    String collection = mongoTemplate.getCollectionName(PersistentWord.class);
//    Query query = query(
//            where("dictionary").is(word.getDictionary())
//                    .and("profile").is(word.getProfile())
//                    .and("word").is(word.getWord()));
//    Update update = new Update();
//update.set("foo", "bar");
//mongoTemplate.doUpdate(collection, query, update, PersistentWord.class, true, true);
    private Update setUpdateForKey(Update update, String key, Object value) {
        if (value != null) {
            if (update == null) {
                update = new Update();
            }
            update.set(key, value);
        }
        return update;
    }
}
