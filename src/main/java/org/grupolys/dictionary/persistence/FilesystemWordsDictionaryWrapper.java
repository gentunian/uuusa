package org.grupolys.dictionary.persistence;

import com.mongodb.MongoClient;
import org.bson.BsonReader;
import org.bson.BsonType;
import org.bson.BsonWriter;
import org.bson.Document;
import org.bson.codecs.*;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.json.JsonReader;
import org.grupolys.dictionary.AbstractWordsDictionary;
import org.grupolys.dictionary.DictionaryWord;
import org.grupolys.dictionary.Word;
import org.grupolys.dictionary.WordsDictionary;
import org.grupolys.dictionary.persistence.codecs.WordCodec;
import org.grupolys.profiles.PartOfSpeech;

import javax.servlet.http.Part;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class FilesystemWordsDictionaryWrapper {
    private WordsDictionary dictionary;

    public FilesystemWordsDictionaryWrapper(String path) {
        dictionary = new AbstractWordsDictionary("asdf") {};
    }
    public static abstract class Foo {
        private String name;

        Foo(String s) {
            name = s;
        }
    }

    public static class Bar extends  Foo {

        Bar(String s) {
            super(s);
        }
    }

    WordsDictionary getDictionary() { return dictionary; }

    public static class DocumentCodecProvider implements CodecProvider {
        private final BsonTypeClassMap bsonTypeClassMap;

        public DocumentCodecProvider(final BsonTypeClassMap bsonTypeClassMap) {
            this.bsonTypeClassMap = bsonTypeClassMap;
        }

        @Override
        public <T> Codec<T> get(final Class<T> clazz, final CodecRegistry registry) {
            if (clazz == Document.class) {
                // construct DocumentCodec with a CodecRegistry
                return (Codec<T>) new DocumentCodec(registry, bsonTypeClassMap);
            }

            // CodecProvider returns null if it's not a provider for the requresed Class
            return null;
        }
    }

    public static void main(String[] args) {
//        CodecRegistry registry = CodecRegistries.fromCodecs()
        DocumentCodecProvider documentCodecProvider = new DocumentCodecProvider(new BsonTypeClassMap());
        CodecRegistry foo = CodecRegistries.fromRegistries(
                CodecRegistries.fromCodecs(new WordCodec(null)),
                CodecRegistries.fromProviders(documentCodecProvider),
                CodecRegistries.fromRegistries(MongoClient.getDefaultCodecRegistry())
                );

//        String json = "{ \"words\" : { \"debe\" : { \"word\" : \"debe\", \"VERB\" : { \"value\" : 1.0, \"lemma\" : \"deber\" } } }, \"language\" : \"fa\" }";
//        BsonReader bsonReader = new JsonReader("{ \"word\" : \"debe\", \"VERB\" : { \"value\" : 1.0, \"lemma\" : \"deber\" } }");
//        WordCodec c = new WordCodec(null);
//        Word f = c.decode(bsonReader, DecoderContext.builder().build());
//        System.out.println(f.getLemma(PartOfSpeech.VERB));

        WordsDictionary dictionary = new AbstractWordsDictionary("fa") {};
        DictionaryWord l = dictionary.addWord("ser", 12, PartOfSpeech.NOUN);
        dictionary.addWord("es", "ser", 1, PartOfSpeech.NOUN);

        dictionary.addWord("seba");
        Document doc = new Document(dictionary.toMap());

        System.out.println(doc.toBsonDocument(DictionaryWord.class, foo).toJson());

        System.out.println(dictionary.getWord("ser").getValue(PartOfSpeech.NOUN));
        System.out.println(dictionary.getWord("es")
                .getOptimisticValue(PartOfSpeech.NOUN));
    }
}
