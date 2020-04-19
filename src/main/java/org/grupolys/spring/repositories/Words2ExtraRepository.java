package org.grupolys.spring.repositories;

import org.grupolys.profiles.PartOfSpeech;
import org.grupolys.spring.model.persistence.PersistentWord2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface Words2ExtraRepository {
    List<PersistentWord2> findAllWordsByLemmas(String dictionary,
                                               Map<PartOfSpeech, PersistentWord2.Value> values);

    Page<PersistentWord2> findAllBySearch(String search,
                                          String dictionary,
                                          PartOfSpeech[] pos,
                                          Pageable pageable);
}
