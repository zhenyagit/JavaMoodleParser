package org.imjs_man.moodleParser.repository;

import org.imjs_man.moodleParser.entity.dataBase.IndexDictionaryEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;

public interface IndexDictionaryRepository extends CrudRepository<IndexDictionaryEntity, Long> {
    IndexDictionaryEntity findById(long id);
    IndexDictionaryEntity findByWord(String word);
    boolean existsByWord(String word);
    ArrayList<IndexDictionaryEntity> findAll();
    @Query(value = "SELECT max(id) FROM IndexDictionaryEntity ")
    Long maxId();

}
