package org.grupolys.profiles;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.FindOneAndReplaceOptions;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.conversions.Bson;
import org.bson.json.JsonReader;
import org.bson.json.JsonWriter;
import org.grupolys.profiles.exception.DictionaryNotFoundException;
import org.grupolys.profiles.exception.ProfileNotFoundException;
import org.grupolys.spring.model.mappers.DictionaryProfileMapper;
import org.grupolys.spring.model.payloads.DictionaryProfilePayload;
import org.grupolys.spring.model.payloads.ProfileDictionaryPayload;
import org.grupolys.spring.model.persistence.PersistentDictionary;
import org.grupolys.spring.model.persistence.PersistentDictionaryProfile;
import org.grupolys.spring.model.persistence.PersistentWord;
import org.grupolys.spring.repositories.ProfilesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;

@Service
@ConditionalOnProperty(prefix = "dictionaryProfileCreator", name = "Impl", havingValue = "Mongo")
public class MongoProfileStore implements DictionaryProfileStore {
    @Autowired
    private MongoClient mongoClient;

    @Autowired
    private DictionaryProfileMapper mapper;

    @Override
    public List<DictionaryProfile> profiles() {
        MongoCollection<DictionaryProfile> collection = getCollection();
        FindIterable<DictionaryProfile> profiles = collection.find();
        return StreamSupport.stream(profiles.spliterator(), false).collect(Collectors.toList());
    }
    @Override
    public boolean saveProfile(String profileName, DictionaryProfile profile) {
        MongoCollection<DictionaryProfile> collection = getCollection();
        FindOneAndReplaceOptions options = new FindOneAndReplaceOptions().upsert(true);
        collection.findOneAndReplace(
                eq("name", profile.getName()),
                profile,
                options
        );
        return false;
    }

    @Override
    public DictionaryProfile loadProfile(String profileName) throws ProfileNotFoundException {
        MongoCollection<PersistentDictionaryProfile> collection = getCollection2();
        PersistentDictionaryProfile profile = collection.find(eq("name", profileName)).first();
        if (profile == null) {
            throwProfileNotFoundException(profileName);
        }
        return mapper.toDictionaryProfile(profile);
    }

    private MongoCollection<PersistentDictionaryProfile> getCollection2() {
        MongoDatabase db = mongoClient.getDatabase("dictionary");
        return db.getCollection("profiles", PersistentDictionaryProfile.class);
    }

    private MongoCollection<DictionaryProfile> getCollection() {
        MongoDatabase db = mongoClient.getDatabase("dictionary");
        return db.getCollection("profiles", DictionaryProfile.class);
    }

    private Codec<DictionaryProfile> getCodec() {
        return getCollection().getCodecRegistry().get(DictionaryProfile.class);
    }

    private void throwDictionaryNotFoundException(String profileName, String dictionary)
            throws DictionaryNotFoundException {
        throw new DictionaryNotFoundException(
                "Dictionary '" + dictionary + "' not found for profile '" + profileName + "'."
        );
    }

    private void throwProfileNotFoundException(String profileName) throws ProfileNotFoundException {
        throw new ProfileNotFoundException("Profile '" + profileName + "' not found.");
    }
}
