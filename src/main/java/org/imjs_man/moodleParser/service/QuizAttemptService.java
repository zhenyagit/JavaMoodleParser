package org.imjs_man.moodleParser.service;

import org.imjs_man.moodleParser.entity.dataBase.PersonEntity;
import org.imjs_man.moodleParser.entity.dataBase.QuizAttemptEntity;
import org.imjs_man.moodleParser.entity.dataBase.QuizEntity;
import org.imjs_man.moodleParser.repository.QuizAttemptRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
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
    public ArrayList<QuizAttemptEntity> getAll()
    {
        return (ArrayList<QuizAttemptEntity>) quizAttemptRepository.findAll();
    }
    public ArrayList<QuizAttemptEntity> getAllByOwner(PersonEntity person)
    {
        return quizAttemptRepository.findByOwnerIs(person);
    }
    public QuizAttemptEntity getById(long id)
    {
        return quizAttemptRepository.findById(id);
    }

}
