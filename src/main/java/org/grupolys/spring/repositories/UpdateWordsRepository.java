package org.grupolys.spring.repositories;

import org.grupolys.spring.model.persistence.PersistentWord;

import java.util.List;

interface UpdateWordsRepository {
    List<PersistentWord> upsertWord(PersistentWord word);
}

