package org.imjs_man.moodleParser.parser;

import org.elasticsearch.common.recycler.Recycler;
import org.elasticsearch.search.SearchHit;
import org.imjs_man.moodleParser.elasticService.ElasticRestClientService;
import org.imjs_man.moodleParser.entity.dataBase.*;
import org.imjs_man.moodleParser.entity.supporting.DifferentQuizQuestions;
import org.imjs_man.moodleParser.entity.supporting.SuperEntity;
import org.imjs_man.moodleParser.exception.*;
import org.imjs_man.moodleParser.parser.decryptor.ActivityInstance;
import org.imjs_man.moodleParser.parser.decryptor.MoodleDecryptor;
import org.imjs_man.moodleParser.parser.decryptor.QuiExeLists;
import org.imjs_man.moodleParser.parser.service.AuthData;
import org.imjs_man.moodleParser.parser.service.MoodleAuthToken;
import org.imjs_man.moodleParser.parser.service.MoodleService;
import org.imjs_man.moodleParser.prettyTable.PrettyTable;
import org.imjs_man.moodleParser.service.*;
import org.imjs_man.moodleParser.tokenGenerator.TokenGenerator;
import org.json.simple.parser.ParseException;
import org.jsoup.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

@Component
@EnableScheduling
public class MoodleParser {
    @Autowired
    TokenGenerator tokenGenerator;
    @Autowired
    PersonService personService;
    @Autowired
    CourseService courseService;
    @Autowired
    QuizService quizService;
    @Autowired
    ExerciseService exerciseService;
    @Autowired
    QuizAttemptService quizAttemptService;
    @Autowired
    MoodleService moodleService;
    @Autowired
    MoodleDecryptor moodleDecryptor;
    @Autowired
    ComparisonQuizQuestionService comparasionQuizQuestionService;

    @Autowired
    ElasticRestClientService elasticRestClientService;
    // todo здесь сделаем такую штуку, вся хрень с сервиса в обертку

    private final RequestCounter requestCounter = new RequestCounter();
//    private final PriorityQueue<EntityWithAuthData<CourseEntity>> courseParseQueue = new PriorityQueue<>();
//    private final PriorityQueue<EntityWithAuthData<QuizEntity>> quizParseQueue = new PriorityQueue<>();
//    private final PriorityQueue<EntityWithAuthData<ExerciseEntity>> exerciseParseQueue = new PriorityQueue<>();
//    private final PriorityQueue<EntityWithAuthData<PersonEntity>> personParseQueue = new PriorityQueue<>();
//    private final PriorityQueue<EntityWithAuthData<QuizAttemptEntity>> quizAttemptParseEntity = new PriorityQueue<>();
    private final SuperEntityQueue mainQueue = new SuperEntityQueue();
//    private final PriorityQueue<EntityWithAuthData<SuperEntity>> mainQueue = new PriorityQueue<>();
    private static final Logger logger = LoggerFactory.getLogger(MoodleParser.class);

    private static final int MAX_REQUEST_COUNTER = 20;




    @EventListener(ApplicationReadyEvent.class)
    public void addPersonsToQueue() throws CantFindSessKey, CantParseMainDocument, EmptyAuthCookie, CantGetPersonInfo, CantGetAuthoriseToken {
        for (PersonEntity person : personService.getPersonsToParse()) {
            mainQueue.addToQueue(null,person);
            logger.info("Person added to queue : "+ person.getFullName());
        }
    }


    @Scheduled(fixedDelay = 1000)
    private void autoParseAll() throws NoItemInQueue {
        while(!mainQueue.isEmpty() && (requestCounter.getCount()< MAX_REQUEST_COUNTER))
        {
            getDataBySuperEntity(mainQueue.getFromQueue());
        }
    }





    private AuthData getJustAuthData(PersonEntity person) throws CantGetAuthoriseToken, CantGetPersonInfo, CantFindSessKey, CantParseMainDocument, EmptyAuthCookie {
        MoodleAuthToken moodleAuthToken = moodleService.getAuthTokenOld();
        Connection.Response respAuth = moodleService.getAuthDataOld(person, moodleAuthToken);
        return moodleDecryptor.getParsedAuthData(person, respAuth);
    }

    public PersonEntity auth(String login, String password) throws CantAuthoriseInMoodle {
        try {
            PersonEntity tempPerson = new PersonEntity(login,password);
            AuthData authData = getJustAuthData(tempPerson);
            PersonEntity personReturned = moodleDecryptor.getParsedPersonInfo(authData);
            personReturned.setToken(tokenGenerator.generateNewToken());
            personService.savePerson(personReturned);

            mainQueue.addToQueue(authData, personReturned);
            return personReturned;

            // todo check if wrong password or username
            // todo check if auth data is wrong

        } catch (CantGetPersonInfo | CantGetAuthoriseToken | CantFindSessKey | EmptyAuthCookie | CantParseMainDocument e) {
            throw new CantAuthoriseInMoodle("When authorise :" + e.getMessage());
        }
    }

    private AuthData manyTryAuth(PersonEntity person) {
        boolean successReceive = false;
        int missCounter = 0;
        AuthData authData = null;
        while (missCounter < 3 && !successReceive) {
            missCounter = missCounter + 1;
            try {
                authData = getJustAuthData(person);
                successReceive = true;
            } catch (CantGetAuthoriseToken e) {
                logger.warn("Server can't response. Can't get AuthToken for person : " + person.getFullName());
            } catch (CantGetPersonInfo e) {
                logger.warn("Server can't response. Can't get AuthData for person : " + person.getFullName());
            } catch (CantFindSessKey | EmptyAuthCookie | CantParseMainDocument e) {
                logger.warn("Server can't response. Returned data is wrong : " + person.getFullName());
            }
        }
        if (!successReceive) {
            logger.warn("Server can't response in 3 attempts");
        }
        return authData;
    }

    private void getDataBySuperEntity(EntityWithAuthData<SuperEntity> entityEntityWithAuthData)
    {
        SuperEntity superEntity = entityEntityWithAuthData.getEntity();
        requestCounter.addItem(superEntity);
        Mono<String>  requestedData = null;
        long ItemId = superEntity.getId();
        AuthData authData = entityEntityWithAuthData.getAuthData();
        switch (superEntity.getClass().getSimpleName())
        {
            case("CourseEntity"):
                requestedData = moodleService.getRawActivityInstances(authData,ItemId);
                break;
            case("QuizEntity"):
                requestedData = moodleService.getRawQuizAttempts(authData,ItemId);
                break;
            case("QuizAttemptEntity"):
                requestedData = moodleService.getRawQuizAttemptsQuestionsAndAnswers(authData,ItemId);
                break;
            case("PersonEntity"):

                authData = manyTryAuth((PersonEntity) superEntity);
                if (authData==null)
                    mainQueue.addToQueue(null, superEntity);
                else {
                    entityEntityWithAuthData.setAuthData(authData);
                    requestedData = moodleService.getRawCoursesList(authData);
                }
                break;

            default:
                requestCounter.removeItem(superEntity);
                break;
        }
        if (requestedData!=null)
            requestedData.subscribe(data->processingAny(data, entityEntityWithAuthData),
                    error ->  errorProcessing(error, entityEntityWithAuthData));


    }

    private void processingQuizAttemptsQuestionsAndAnswers(String rawQuizAttemptQuestionsAndAnswers, AuthData authData, QuizAttemptEntity quizAttemptEntity)
    {
        requestCounter.removeItem(quizAttemptEntity);
        DifferentQuizQuestions differentQuizQuestions = moodleDecryptor.getParsedQuizAttemptsQuestionsAndAnswers(rawQuizAttemptQuestionsAndAnswers);
        ArrayList<ComparisonQuizQuestionEntity> tempComparasion = differentQuizQuestions.getComparisonQuizQuestions();
        comparasionQuizQuestionService.setManyQuizAttempt(tempComparasion, quizAttemptEntity);
        comparasionQuizQuestionService.setManyPerson(tempComparasion, authData.getPersonEntity());
        comparasionQuizQuestionService.saveAll(tempComparasion);
        ArrayList<SuperEntity> toElastic = new ArrayList<>(tempComparasion);
        elasticRestClientService.indexObjectAsync(toElastic);
    }

    private void processingCourses(String rawCourses, AuthData authData, PersonEntity personEntity)
    {
        requestCounter.removeItem(personEntity);
        try {
            ArrayList<CourseEntity> courseEntities = moodleDecryptor.getParsedCoursesList(rawCourses);
            courseService.saveAll(courseEntities);
            ArrayList<SuperEntity> toElastic = new ArrayList<>(courseEntities);
            elasticRestClientService.indexObjectAsync(toElastic);
            for (CourseEntity courseEntity:courseEntities)
            {
                mainQueue.addToQueue(authData, courseEntity);
            }
        } catch (ParseException e) {
            logger.warn("Error while parse raw courses list. Person with name :" + authData.getPersonEntity().getFullName());
            mainQueue.addToQueue(authData,authData.getPersonEntity());

        }
    }

    private void processingQuizAttempts(String rawQuizAttempts, AuthData authData, QuizEntity quizEntity)
    {
        requestCounter.removeItem(quizEntity);
        Set<QuizAttemptEntity> quizAttemptEntities = moodleDecryptor.getParsedQuizAttempts(rawQuizAttempts);

        quizAttemptService.setManyQuiz(quizAttemptEntities, quizEntity);
        quizAttemptService.setManyPerson(quizAttemptEntities, authData.getPersonEntity());
        quizAttemptService.saveAll(quizAttemptEntities);
        ArrayList<SuperEntity> toElastic = new ArrayList<>(quizAttemptEntities);
        elasticRestClientService.indexObjectAsync(toElastic);
        for (QuizAttemptEntity quizAttemptEntity : quizAttemptEntities)
        {
            mainQueue.addToQueue(authData,quizAttemptEntity);
        }
    }

    private void processingQuizExercise(String rawActivityInstances, AuthData authData, CourseEntity courseEntity)
    {
        requestCounter.removeItem(courseEntity);
        ArrayList<ActivityInstance> activityInstances = moodleDecryptor.getParsedActivityInstances(rawActivityInstances);
        QuiExeLists quiExeLists = moodleDecryptor.getParsedQuiExeListsFromActivityInstances(activityInstances);
        ArrayList<QuizEntity> quizEntities = quiExeLists.getQuizes();
        ArrayList<ExerciseEntity> exerciseEntities = quiExeLists.getExercises();
        quizService.setManyParent(quizEntities, courseEntity);
        quizService.saveAll(quizEntities);
        exerciseService.setManyParent(exerciseEntities, courseEntity);
        exerciseService.saveAll(exerciseEntities);
        ArrayList<SuperEntity> toElastic = new ArrayList<>(quizEntities);
        toElastic.addAll(exerciseEntities);
        elasticRestClientService.indexObjectAsync(toElastic);
        for (QuizEntity quizEntity:quizEntities)
        {
            mainQueue.addToQueue(authData,quizEntity);

        }
        for (ExerciseEntity exerciseEntity:exerciseEntities)
        {
            mainQueue.addToQueue(authData,exerciseEntity);

        }
    }

    private void processingAny(String data, EntityWithAuthData<SuperEntity> entityEntityWithAuthData)
    {
        SuperEntity superEntity = entityEntityWithAuthData.getEntity();
        AuthData authData = entityEntityWithAuthData.getAuthData();
        switch (superEntity.getClass().getSimpleName())
        {
            case("QuizAttemptEntity"):
                processingQuizAttemptsQuestionsAndAnswers(data,authData,(QuizAttemptEntity) superEntity);
                break;
            case("PersonEntity"):
                processingCourses(data,authData,(PersonEntity) superEntity);
                break;
            case("QuizEntity"):
                processingQuizAttempts(data,authData,(QuizEntity) superEntity);
                break;
            case("CourseEntity"):
                processingQuizExercise(data,authData,(CourseEntity) superEntity);
                break;
        }
    }

    private void errorProcessing(Throwable error, EntityWithAuthData<SuperEntity> entityWithAuthData)
    {
        SuperEntity item = entityWithAuthData.getEntity();
        AuthData authData = entityWithAuthData.getAuthData();
        requestCounter.removeItem(item);
        logger.warn(error.getMessage() +" : "+ item.getClass().getSimpleName()+ " " + item.getId()+ " " + authData.getPersonEntity().getFullName());
        mainQueue.addToQueue(authData, item);
    }

}
