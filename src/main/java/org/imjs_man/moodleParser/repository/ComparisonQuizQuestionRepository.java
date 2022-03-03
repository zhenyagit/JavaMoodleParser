package org.imjs_man.moodleParser.repository;

import org.imjs_man.moodleParser.entity.dataBase.ComparisonQuizQuestionEntity;
import org.imjs_man.moodleParser.entity.dataBase.CourseEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.ArrayList;

public interface ComparisonQuizQuestionRepository extends CrudRepository<ComparisonQuizQuestionEntity, Long> {
    ArrayList<ComparisonQuizQuestionEntity> findByIndexesLowIsNull();
}
