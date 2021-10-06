package org.imjs_man.moodleParser.service;

import org.imjs_man.moodleParser.entity.CourseEntity;
import org.imjs_man.moodleParser.entity.ExerciseEntity;
import org.imjs_man.moodleParser.entity.QuizEntity;
import org.imjs_man.moodleParser.repository.QuizRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Set;

@Service
public class QuizService {

    @Autowired
    private QuizRepository quizRepository;

    public void saveAll(Set<QuizEntity> quizEntities)
    {
        quizRepository.saveAll(quizEntities);
    }
    public Boolean checkId(long id)
    {
        return quizRepository.findById(id) != null;
    }
    public List<QuizEntity> getAllQuizes() {
        return (List<QuizEntity>) quizRepository.findAll();
    }
    public void setManyParent(Set<QuizEntity> quizEntities, CourseEntity courseEntity)
    {
        for (QuizEntity quizEntity:quizEntities)
        {
            quizEntity.setCourseEntity(courseEntity);
        }
    }
}
