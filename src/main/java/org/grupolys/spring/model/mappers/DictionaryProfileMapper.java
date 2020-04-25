package org.grupolys.spring.model.mappers;

import org.bson.types.ObjectId;
import org.grupolys.spring.model.payloads.DictionaryProfilePayload;
import org.grupolys.spring.model.payloads.ProfileDictionaryPayload;
import org.grupolys.spring.model.persistence.PersistentDictionary;
import org.grupolys.spring.model.persistence.PersistentDictionaryProfile;
import org.grupolys.spring.model.responses.DictionaryProfileResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DictionaryProfileMapper {

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

        return new DictionaryProfileResponse(
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
