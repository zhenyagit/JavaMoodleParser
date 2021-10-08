package org.imjs_man.moodleParser.repository;

import org.imjs_man.moodleParser.entity.QuizAttemptEntity;
import org.springframework.data.repository.CrudRepository;

public interface QuizAttemptRepository  extends CrudRepository<QuizAttemptEntity, Long> {
    QuizAttemptRepository findById(long id);
}
