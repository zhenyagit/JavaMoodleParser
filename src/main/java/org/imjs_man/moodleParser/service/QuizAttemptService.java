package org.imjs_man.moodleParser.service;

import org.imjs_man.moodleParser.entity.*;
import org.imjs_man.moodleParser.repository.QuizAttemptRepository;
import org.imjs_man.moodleParser.repository.QuizRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class QuizAttemptService {
    @Autowired
    QuizAttemptRepository quizAttemptRepository;

    public void setManyPerson(Set<QuizAttemptEntity> quizAttemptEntities, PersonEntity personEntity)
    {
        for (QuizAttemptEntity quizAttemptEntity:quizAttemptEntities)
        {
            quizAttemptEntity.setOwner(personEntity);
        }
    }
    public void setManyQuiz(Set<QuizAttemptEntity> quizAttemptEntities, QuizEntity quizEntity)
    {
        for (QuizAttemptEntity quizAttemptEntity:quizAttemptEntities)
        {
            quizAttemptEntity.setQuizEntity(quizEntity);
        }
    }
    public void saveAll(Set<QuizAttemptEntity> quizAttemptEntities)
    {
        quizAttemptRepository.saveAll(quizAttemptEntities);
    }

}
