package org.grupolys.spring.repositories;

import org.grupolys.profiles.PartOfSpeech;
import org.grupolys.spring.model.persistence.PersistentWord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface WordsExtraRepository {
    List<PersistentWord> findAllWordsByLemmas(String dictionary,
                                              Map<PartOfSpeech, PersistentWord.Value> values);

    Page<PersistentWord> findAllBySearch(String search,
                                         String dictionary,
                                         PartOfSpeech[] pos,
                                         Pageable pageable);
}
