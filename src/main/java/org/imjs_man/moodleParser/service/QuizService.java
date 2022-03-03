package org.imjs_man.moodleParser.service;

import org.imjs_man.moodleParser.entity.dataBase.ComparisonQuizQuestionEntity;
import org.imjs_man.moodleParser.entity.dataBase.CourseEntity;
import org.imjs_man.moodleParser.entity.dataBase.QuizEntity;
import org.imjs_man.moodleParser.repository.QuizRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class QuizService {

    @Autowired
    private QuizRepository quizRepository;

    public void saveAll(ArrayList<QuizEntity> quizEntities)
    {
        quizRepository.saveAll(quizEntities);
    }
    public Boolean checkId(long id)
    {
        return quizRepository.findById(id) != null;
    }
    public ArrayList<QuizEntity> getAllQuizes() {
        return quizRepository.findAll();
    }
    public void setManyParent(ArrayList<QuizEntity> quizEntities, CourseEntity courseEntity)
    {
        for (QuizEntity quizEntity:quizEntities)
        {
            quizEntity.setCourseEntity(courseEntity);
        }
    }
    public ArrayList<QuizEntity> getForIndexing()
    {
        return quizRepository.findByIndexesLowIsNull();
    }
}
