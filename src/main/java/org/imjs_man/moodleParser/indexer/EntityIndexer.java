package org.imjs_man.moodleParser.indexer;

import org.imjs_man.moodleParser.entity.dataBase.ComparisonQuizQuestionEntity;
import org.imjs_man.moodleParser.entity.dataBase.CourseEntity;
import org.imjs_man.moodleParser.entity.dataBase.QuizAttemptEntity;
import org.imjs_man.moodleParser.entity.dataBase.QuizEntity;
import org.imjs_man.moodleParser.service.ComparisonQuizQuestionService;
import org.imjs_man.moodleParser.service.CourseService;
import org.imjs_man.moodleParser.service.IndexDictionaryService;
import org.imjs_man.moodleParser.service.QuizService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

@Component
@EnableScheduling
public class EntityIndexer {
    //todo one indexer, work async for all repo

    @Autowired
    IndexDictionaryService indexDictionaryService;
    @Autowired
    CourseService courseService;
    @Autowired
    QuizService quizService;
    @Autowired
    ComparisonQuizQuestionService comparisonQuizQuestionService;


    @Scheduled(fixedDelay = 1000)
    private void startIndexing()
    {
        ArrayList<CourseEntity> courseEntities = courseService.getForIndexing();
        ArrayList<QuizEntity> quizEntities = quizService.getForIndexing();
        ArrayList<ComparisonQuizQuestionEntity> comparisonQuizQuestionEntities = comparisonQuizQuestionService.getForIndexing();
        for (ComparisonQuizQuestionEntity comparisonQuizQuestionEntity:comparisonQuizQuestionEntities)
        {
            comparisonQuizQuestionEntity.setIndexes(getIndexes(comparisonQuizQuestionEntity));
        }
        comparisonQuizQuestionService.saveAll(comparisonQuizQuestionEntities);
        for (QuizEntity quizEntity:quizEntities)
        {
            quizEntity.setIndexes(getIndexes(quizEntity));
        }
        quizService.saveAll(quizEntities);
        for (CourseEntity courseEntity:courseEntities)
        {
            courseEntity.setIndexes(getIndexes(courseEntity));
        }
        courseService.saveAll(courseEntities);
    }


    public String lineToNormalView(String line)
    {
        return  line.replaceAll("[^a-zA-Z0-9а-яА-Я]", " ").replaceAll("[\\s]{2,}", " ").toLowerCase();
    }

    public Long[] getIndexes(ComparisonQuizQuestionEntity comparisonQuizQuestionEntity)
    {
        String allText = "";
        allText = allText + comparisonQuizQuestionEntity.getName();
        allText = allText + " " + Arrays.toString(comparisonQuizQuestionEntity.getListOfQuestions());
        allText = allText + " " + Arrays.toString(comparisonQuizQuestionEntity.getListOfVariants());
        String[] words = lineToNormalView(allText).split(" ");
        return indexDictionaryService.getIndexesByManyWordsUseBuffer(words);
    }
    public Long[] getIndexes(QuizEntity quizEntity)
    {
        String allText = "";
        allText = allText + quizEntity.getName();
        String[] words = lineToNormalView(allText).split(" ");
        return indexDictionaryService.getIndexesByManyWordsUseBuffer(words);
    }
    public Long[] getIndexes(CourseEntity courseEntity)
    {
        String allText = "";
        allText = allText + courseEntity.getName();
        allText = allText + courseEntity.getFullnamedisplay();
        String[] words = lineToNormalView(allText).split(" ");
        return indexDictionaryService.getIndexesByManyWordsUseBuffer(words);
    }







}
