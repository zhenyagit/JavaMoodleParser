package org.imjs_man.moodleParser.repository;

import org.imjs_man.moodleParser.entity.dataBase.QuizEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.ArrayList;

public interface QuizRepository extends CrudRepository<QuizEntity, Long> {
    QuizEntity findById(long Id);
    ArrayList<QuizEntity> findByIndexesLowIsNull();
    ArrayList<QuizEntity> findAll();
}
