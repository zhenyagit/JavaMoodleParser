package org.imjs_man.moodleParser.repository;

import org.imjs_man.moodleParser.entity.dataBase.PersonEntity;
import org.imjs_man.moodleParser.entity.dataBase.QuizAttemptEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.ArrayList;

public interface QuizAttemptRepository  extends CrudRepository<QuizAttemptEntity, Long> {
    QuizAttemptEntity findById(long id);
    ArrayList<QuizAttemptEntity> findByOwnerIs(PersonEntity owner);
}
