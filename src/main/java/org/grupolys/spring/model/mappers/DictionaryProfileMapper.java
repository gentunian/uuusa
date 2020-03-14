package org.grupolys.spring.model.mappers;

import org.bson.types.ObjectId;
import org.grupolys.profiles.DictionaryProfile;
import org.grupolys.spring.model.payloads.DictionaryProfilePayload;
import org.grupolys.spring.model.payloads.DictionaryWordPayload;
import org.grupolys.spring.model.payloads.ProfileDictionaryPayload;
import org.grupolys.spring.model.persistence.PersistentDictionary;
import org.grupolys.spring.model.persistence.PersistentDictionaryProfile;
import org.grupolys.spring.model.persistence.PersistentWord;
import org.grupolys.spring.model.responses.DictionaryProfileResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DictionaryProfileMapper {

    public DictionaryProfilePayload toDictionaryProfilePayload(DictionaryProfile profile) {
        return null;
    }

    public List<DictionaryProfileResponse> toDictionaryProfileResponse(List<PersistentDictionaryProfile> profiles) {
        List<DictionaryProfileResponse> response = new ArrayList<>();
        if (profiles == null || profiles.size() == 0) {
            return response;
        }
        for (PersistentDictionaryProfile profile: profiles) {
            response.add(toDictionaryProfileResponse(profile));
        }

        return response;
    }

    public DictionaryProfileResponse toDictionaryProfileResponse(PersistentDictionaryProfile profile) {
        if (profile == null) {
            return null;
        }
        DictionaryProfileResponse response = new DictionaryProfileResponse(
                profile.getId(),
                profile.getName(),
                profile.getDescription(),
                profile.getLanguage(),
                profile.getActiveDictionary(),
                profile.getDictionaries().stream()
                        .map(d -> new DictionaryProfileResponse.Dictionary(
                                d.getId(),
                                d.getName(),
                                d.getLanguage(),
                                d.getDescription()
                                )
                        ).collect(Collectors.toList())
        );

        return response;
    }

//    public DictionaryProfileResponse toDictionaryProfileResponse(DictionaryProfile profile) {
//        if (profile == null) {
//            return null;
//        }
//        DictionaryProfileResponse response = new DictionaryProfileResponse();
//        response.setDescription(profile.getDescription());
//        response.setLanguage(profile.getLanguage());
//        response.setName(profile.getName());
////        response.setDictionaries(profile.getDictionaries().stream().map(WordsDictionary::getName).toArray(String[]::new));
//        return response;
//    }

    public DictionaryProfile toDictionaryProfile(DictionaryProfilePayload profilePayload) {
        if (profilePayload == null) {
            return null;
        }

        DictionaryProfile profile = new DictionaryProfile();
        profile.setDescription(profilePayload.getDescription());
        profile.setLanguage(profilePayload.getLanguage());
        profile.setName(profilePayload.getName());

        return profile;
    }

    public PersistentDictionary toPersistentDictionary(ProfileDictionaryPayload payload) {
        if (payload == null) {
            return null;
        }

        PersistentDictionary dictionary = new PersistentDictionary();
        dictionary.setId(new ObjectId().toString());
        dictionary.setLanguage(payload.getLanguage());
        dictionary.setDescription(payload.getDescription());
        dictionary.setName(payload.getName());

        return dictionary;
    }

    public PersistentWord toPersistentWord(DictionaryWordPayload wordPayload,
                                           String profileName,
                                           String dictionary,
                                           String language) {
        if (wordPayload == null) {
            return null;
        }

        PersistentWord word = new PersistentWord();
        word.setLemma(wordPayload.getLemma());
        word.setPartOfSpeech(wordPayload.getPartOfSpeech());
        word.setWord(wordPayload.getWord().toLowerCase());
        word.setBooster(wordPayload.getBooster());
        word.setNegating(wordPayload.getNegating());
        word.setValue(wordPayload.getValue());
        word.setDictionary(dictionary);
        word.setProfile(profileName);
        word.setLanguage(language);
        return word;
    }

    public List<PersistentWord> toPersistentWord(List<DictionaryWordPayload> wordsPayload,
                                                 String profileName,
                                                 String dictionary,
                                                 String language) {
        if (wordsPayload == null) {
            return null;
        }

        List<PersistentWord> words = new ArrayList<>();
        for (DictionaryWordPayload payload : wordsPayload) {
            words.add(toPersistentWord(payload, profileName, dictionary, language));
        }
        return words;
    }

    public DictionaryProfile toDictionaryProfile(PersistentDictionaryProfile persistentProfile) {
        if (persistentProfile == null) {
            return null;
        }
        DictionaryProfile profile = new DictionaryProfile();
        profile.setLanguage(persistentProfile.getLanguage());
        profile.setDescription(persistentProfile.getDescription());
        profile.setName(persistentProfile.getName());

        return profile;
    }

    public PersistentDictionaryProfile toPersistentDictionaryProfile(DictionaryProfilePayload profilePayload) {
        if (profilePayload == null) {
            return null;
        }
        PersistentDictionaryProfile profile = new PersistentDictionaryProfile();
        profile.setName(profilePayload.getName());
        profile.setDescription(profilePayload.getDescription());
        profile.setLanguage(profilePayload.getLanguage());

        return profile;
    }
}
