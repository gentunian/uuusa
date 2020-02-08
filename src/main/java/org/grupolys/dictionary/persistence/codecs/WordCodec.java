package org.grupolys.dictionary.persistence.codecs;

import org.bson.BsonReader;
import org.bson.BsonType;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.grupolys.dictionary.DictionaryWord;
import org.grupolys.dictionary.Word;
import org.grupolys.dictionary.WordsDictionary;
import org.grupolys.profiles.PartOfSpeech;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class WordCodec implements Codec<Word> {

    private final WordsDictionary dictionary;

    public WordCodec(WordsDictionary dictionary) {
        this.dictionary = dictionary;
    }

    @Override
    public Word decode(BsonReader bsonReader, DecoderContext decoderContext) {
        String word = null;
        Map<String, Object> pos = new HashMap<>();

        bsonReader.readStartDocument();

        while (bsonReader.readBsonType() != BsonType.END_OF_DOCUMENT) {
            String fieldName = bsonReader.readName();
            PartOfSpeech partOfSpeech = PartOfSpeech.getPartOfSpeech(fieldName);
            if (partOfSpeech != null) {
                bsonReader.readStartDocument();
                Map<String, Object> posObject = new HashMap<>();
                while (bsonReader.readBsonType() != BsonType.END_OF_DOCUMENT) {
                    String posField = bsonReader.readName();
                    switch (bsonReader.getCurrentBsonType()) {
                        case STRING:
                            posObject.put(posField, bsonReader.readString());
                            break;
                        case DOUBLE:
                            posObject.put(posField, bsonReader.readDouble());
                            break;
                        case INT32:
                            posObject.put(posField, bsonReader.readInt32());
                            break;
                    }
                }
                pos.put(fieldName, posObject);
                bsonReader.readEndDocument();
            } else {
                word = bsonReader.readString();
            }
        }
        bsonReader.readEndDocument();
        Word myWord = new Word(word);
        pos.forEach( (key, value) -> {
            Map<String, Object> map = (Map<String, Object>) value;
            String lemma = (String) map.get("lemma");
            DictionaryWord wordLemma = null;
            if (lemma != null) {
                // find the lemma in dict. If null, create a new Word.
                wordLemma = dictionary.getWord(lemma);
                if (wordLemma == null) {
                    wordLemma = dictionary.addWord(lemma);
                }
            }
            myWord.addLemma(PartOfSpeech.getPartOfSpeech(key), wordLemma);
            myWord.addValue(PartOfSpeech.getPartOfSpeech(key), ((Double) map.get("value")).floatValue());
        });
        return myWord;
    }

    @Override
    public void encode(BsonWriter bsonWriter, Word word, EncoderContext encoderContext) {
        bsonWriter.writeStartDocument();
        bsonWriter.writeString("word", word.getWord());
        if (word.getValue() != null) {
            bsonWriter.writeDouble("value", word.getValue());
        } else {
            bsonWriter.writeNull("value");
        }
        Set<String> pos = word.getAllPartOfSpeech();
        if (pos.isEmpty()) {
            bsonWriter.writeNull("partOfSpeech");
        } else {
            bsonWriter.writeName("partOfSpeech");
            bsonWriter.writeStartDocument();
            pos.forEach(postag -> {
                Float value = word.getValue(PartOfSpeech.getPartOfSpeech(postag));
                bsonWriter.writeName(postag);
                bsonWriter.writeStartDocument();
                if (value != null) {
                    bsonWriter.writeDouble("value", value);
                } else {
                    bsonWriter.writeNull("value");
                }
                DictionaryWord wordLemma = word.getLemma(PartOfSpeech.getPartOfSpeech(postag));
                if (wordLemma != null) {
                    bsonWriter.writeString("lemma", wordLemma.getWord());
                } else {
                    bsonWriter.writeNull("lemma");
                }
                bsonWriter.writeEndDocument();
            });
            bsonWriter.writeEndDocument();
        }

        bsonWriter.writeEndDocument();
    }

    @Override
    public Class<Word> getEncoderClass() {
        return Word.class;
    }
}