package org.imjs_man.moodleParser.service;

import org.imjs_man.moodleParser.entity.dataBase.CourseEntity;
import org.imjs_man.moodleParser.entity.dataBase.ExerciseEntity;
import org.imjs_man.moodleParser.repository.ExerciseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class ExerciseService {
    @Autowired
    private ExerciseRepository exerciseRepository;

    public void saveAll(Set<ExerciseEntity> exerciseEntities)
    {
        exerciseRepository.saveAll(exerciseEntities);
    }
    public Boolean checkId(long id)
    {
        return exerciseRepository.findById(id) != null;
    }
    public List<ExerciseEntity> getAllQuizes() {
        return (List<ExerciseEntity>) exerciseRepository.findAll();
    }
    public void setManyParent(Set<ExerciseEntity> exerciseEntities, CourseEntity courseEntity)
    {
        for (ExerciseEntity exerciseEntity:exerciseEntities)
        {
            exerciseEntity.setCourseEntity(courseEntity);
        }
    }
}
