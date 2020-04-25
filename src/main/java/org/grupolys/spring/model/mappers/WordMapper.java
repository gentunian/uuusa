package org.grupolys.spring.model.mappers;

import org.grupolys.profiles.PartOfSpeech;
import org.grupolys.spring.model.payloads.PostWordPayload;
import org.grupolys.spring.model.payloads.PatchWordPayload;
import org.grupolys.spring.model.persistence.PersistentWord;
import org.springframework.stereotype.Service;

import java.util.Map;


@Service
public class WordMapper {

    public PersistentWord toPersistentWord2(PostWordPayload payload) {
        PersistentWord word = null;
        if (payload != null) {
            word = new PersistentWord();
            word.setDictionary(payload.getDictionary());
            word.setProfile(payload.getProfile());
            word.setWord(payload.getWord());
            word.setNegating(payload.getNegating());
            word.setBooster(payload.getBooster());
            word.setEmoticon(payload.getEmoticon());
        }
        return word;
    }

    public PersistentWord toPersistentWord2(PersistentWord response, PatchWordPayload payload) {
        PersistentWord word = null;
        if (payload != null) {
            word = new PersistentWord();
            word.setId(response.getId());
            word.setDictionary(response.getDictionary());
            word.setProfile(response.getProfile());
            word.setWord(response.getWord());

            if (payload.getNegating() != null) {
                word.setNegating(payload.getNegating());
            } else {
                word.setNegating(response.getNegating());
            }

            if (payload.getEmoticon() != null) {
                word.setEmoticon(payload.getEmoticon());
            } else {
                word.setEmoticon(response.getEmoticon());
            }


            if (payload.getBooster() != null) {
                word.setBooster(payload.getBooster());
            } else {
                word.setBooster(response.getBooster());
            }

            Map<PartOfSpeech, PatchWordPayload.Value> map = payload.getPartOfSpeech();
            if (map != null) {
                for (PartOfSpeech partOfSpeech: map.keySet()) {
                    PatchWordPayload.Value data = map.get(partOfSpeech);
                    word.setWordValue(partOfSpeech,
                            data.getValue() == null? response.getValue(partOfSpeech) : data.getValue(),
                            data.getLemma() == null? response.getLemma(partOfSpeech) : data.getLemma());
                }
            }
            word.setLanguage(response.getLanguage());
        }
        return word;
    }

}
