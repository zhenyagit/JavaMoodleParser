package org.imjs_man.moodleParser.service;


import org.imjs_man.moodleParser.entity.dataBase.ComparisonQuizQuestionEntity;
import org.imjs_man.moodleParser.entity.dataBase.PersonEntity;
import org.imjs_man.moodleParser.entity.dataBase.QuizAttemptEntity;
import org.imjs_man.moodleParser.entity.dataBase.QuizEntity;
import org.imjs_man.moodleParser.repository.ComparisonQuizQuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Set;

@Service
public class ComparisonQuizQuestionService {
    @Autowired
    ComparisonQuizQuestionRepository comparisonQuizQuestionRepository;
    public void setManyPerson(ArrayList<ComparisonQuizQuestionEntity> comparisonQuizQuestionEntities, PersonEntity personEntity)
    {
        for (ComparisonQuizQuestionEntity comparisonQuizQuestionEntity:comparisonQuizQuestionEntities)
        {
            comparisonQuizQuestionEntity.setOwner(personEntity);
        }
    }
    public void setManyQuizAttempt(ArrayList<ComparisonQuizQuestionEntity> comparisonQuizQuestionEntities, QuizAttemptEntity quizAttemptEntity)
    {
        for (ComparisonQuizQuestionEntity comparisonQuizQuestionEntity:comparisonQuizQuestionEntities)
        {
            comparisonQuizQuestionEntity.setQuizAttemptEntity(quizAttemptEntity);
        }
    }
    public void saveAll(ArrayList<ComparisonQuizQuestionEntity> comparisonQuizQuestionEntities)
    {
        comparisonQuizQuestionRepository.saveAll(comparisonQuizQuestionEntities);
    }
    public ArrayList<ComparisonQuizQuestionEntity> getForIndexing()
    {
        return comparisonQuizQuestionRepository.findByIndexesLowIsNull();
    }

}
