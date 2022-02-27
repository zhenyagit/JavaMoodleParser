package org.imjs_man.moodleParser.repository;

import org.imjs_man.moodleParser.entity.dataBase.QuizEntity;
import org.springframework.data.repository.CrudRepository;

public interface QuizRepository extends CrudRepository<QuizEntity, Long> {
    QuizEntity findById(long Id);
}
