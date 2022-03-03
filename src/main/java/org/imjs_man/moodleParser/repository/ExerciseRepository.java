package org.imjs_man.moodleParser.repository;

import org.imjs_man.moodleParser.entity.dataBase.ExerciseEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.ArrayList;

public interface ExerciseRepository extends CrudRepository<ExerciseEntity, Long> {
    ExerciseEntity findById(long id);
    ArrayList<ExerciseEntity> findAll();
}
