package org.imjs_man.moodleParser.parser;

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
    // todo здесь сделаем такую штуку, вся хрень с сервиса в обертку

    private final RequestCounter requestCounter = new RequestCounter();
    private final PriorityQueue<EntityWithAuthData<CourseEntity>> courseParseQueue = new PriorityQueue<>();
    private final PriorityQueue<EntityWithAuthData<QuizEntity>> quizParseQueue = new PriorityQueue<>();
    private final PriorityQueue<EntityWithAuthData<ExerciseEntity>> exerciseParseQueue = new PriorityQueue<>();
    private final PriorityQueue<EntityWithAuthData<PersonEntity>> personParseQueue = new PriorityQueue<>();
    private final PriorityQueue<EntityWithAuthData<QuizAttemptEntity>> quizAttemptParseEntity = new PriorityQueue<>();
    private static final Logger logger = LoggerFactory.getLogger(MoodleParser.class);

    private static final int MAX_REQUEST_COUNTER = 20;

    @EventListener(ApplicationReadyEvent.class)
    public void addPersonsToQueue() throws CantFindSessKey, CantParseMainDocument, EmptyAuthCookie, CantGetPersonInfo, CantGetAuthoriseToken {
        for (PersonEntity person : personService.getPersonsToParse()) {
            AuthData authData = new AuthData();
            EntityWithAuthData<PersonEntity> temp = new EntityWithAuthData<>(person, authData);
//            personParseQueue.add(temp);

//                AuthData authData = getJustAuthData(person);
//                QuizAttemptEntity quizAttemptEntity = quizAttemptService.getById(2699624);
//                EntityWithAuthData<QuizAttemptEntity> temp = new EntityWithAuthData<>(quizAttemptEntity, authData);
//                quizAttemptParseEntity.add(temp);
            logger.info("Person added to queue : "+ person.getFullName());

        }
    }

    @Scheduled(fixedDelay = 1000)
    private void AutoParseCourses() {
        if ((courseParseQueue.peek()!=null) && (requestCounter.getCount()< MAX_REQUEST_COUNTER))
        {
            EntityWithAuthData<CourseEntity> temp = courseParseQueue.poll();
            assert temp != null;
            getQuizExerciseByCourseId(temp.getAuthData(),temp.getEntity());
        }
    }

    @Scheduled(fixedDelay = 1000)
    private void AutoParseQuiz() {
        while ((quizParseQueue.peek()!=null && (requestCounter.getCount()< MAX_REQUEST_COUNTER)))
        {
            EntityWithAuthData<QuizEntity> temp = quizParseQueue.poll();
            assert temp != null;
            getQuizAttemptsByQuizId(temp.getAuthData(),temp.getEntity());
        }

    }

    @Scheduled(fixedDelay = 1000)
    private void AutoParsePerson() {
        while ((personParseQueue.peek()!=null && (requestCounter.getCount()< MAX_REQUEST_COUNTER)))
        {
            EntityWithAuthData<PersonEntity> temp = personParseQueue.poll();
            assert temp != null;
            parsePerson(temp.getEntity());
        }

    }

    @Scheduled(fixedDelay = 1000)
    private void AutoParseQuizAttempt() {
        while ((quizAttemptParseEntity.peek()!=null && (requestCounter.getCount()< MAX_REQUEST_COUNTER)))
        {
            EntityWithAuthData<QuizAttemptEntity> temp = quizAttemptParseEntity.poll();
            assert temp != null;
            getQuizAttemptsQuestionsAndAnswers(temp.getAuthData(), temp.getEntity());
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
            EntityWithAuthData<PersonEntity> temp = new EntityWithAuthData<>(personReturned, new AuthData());
            personParseQueue.add(temp);
            return personReturned;
        } catch (CantGetPersonInfo | CantGetAuthoriseToken | CantFindSessKey | EmptyAuthCookie | CantParseMainDocument e) {
            throw new CantAuthoriseInMoodle("When authorise :" + e.getMessage());
        }
    }

    private void parsePerson(PersonEntity person)
    {
        boolean successReceive = false;
        int missCounter = 0;
        while(missCounter < 3 && !successReceive)
        {
            missCounter = missCounter+1;
            try {
                MoodleAuthToken moodleAuthToken = moodleService.getAuthTokenOld();
                assert person != null;
                Connection.Response respAuth = moodleService.getAuthDataOld(person, moodleAuthToken);
                AuthData authData = moodleDecryptor.getParsedAuthData(person, respAuth);
                successReceive = true;
                moodleService.getRawCoursesList(authData).subscribe(data-> processingCourses(data, authData, person), error ->  errorProcessing(error, authData, person));
                requestCounter.addItem(person);
            }
            catch (CantGetAuthoriseToken e) {
                assert person != null;
                logger.warn("Server can't response. Can't get AuthToken for person : "+person.getFullName());
            }
            catch (CantGetPersonInfo e){
                logger.warn("Server can't response. Can't get AuthData for person : "+person.getFullName());
            }
            catch (CantFindSessKey | EmptyAuthCookie | CantParseMainDocument e)
            {
                logger.warn("Server can't response. Returned data is wrong : "+person.getFullName());
            }
        }
        if (!successReceive)
        {
            logger.warn("Server can't response in 3 attempts");
        }
    }

    private void getQuizExerciseByCourseId(AuthData authData, CourseEntity courseEntity)
    {
        moodleService.getRawActivityInstances(authData, courseEntity.getId()).subscribe(data -> processingQuizExercise(data, authData, courseEntity), error ->  errorProcessing(error, authData, courseEntity));
        requestCounter.addItem(courseEntity);
    }

    private void getQuizAttemptsByQuizId(AuthData authData, QuizEntity quizEntity)
    {
        moodleService.getRawQuizAttempts(authData, quizEntity.getId()).subscribe(data -> processingQuizAttempts(data, authData, quizEntity), error ->  errorProcessing(error, authData, quizEntity));
        requestCounter.addItem(quizEntity);
    }

    private void getQuizAttemptsQuestionsAndAnswers(AuthData authData, QuizAttemptEntity quizAttemptEntity)
    {
        moodleService.getRawQuizAttemptsQuestionsAndAnswers(authData, quizAttemptEntity.getId(), quizAttemptEntity.getQuizEntity().getId()).subscribe(data -> processingQuizAttemptsQuestionsAndAnswers(data, authData, quizAttemptEntity), error ->  errorProcessing(error, authData, quizAttemptEntity));
        requestCounter.addItem(quizAttemptEntity);
    }


    private void processingQuizAttemptsQuestionsAndAnswers(String rawQuizAttemptQuestionsAndAnswers, AuthData authData, QuizAttemptEntity quizAttemptEntity)
    {
//        System.out.println(rawQuizAttemptQuestionsAndAnswers);
        requestCounter.removeItem(quizAttemptEntity);
        DifferentQuizQuestions differentQuizQuestions = moodleDecryptor.getParsedQuizAttemptsQuestionsAndAnswers(rawQuizAttemptQuestionsAndAnswers);
        ArrayList<ComparisonQuizQuestionEntity> tempComparasion = differentQuizQuestions.getComparisonQuizQuestions();
        comparasionQuizQuestionService.setManyQuizAttempt(tempComparasion, quizAttemptEntity);
        comparasionQuizQuestionService.setManyPerson(tempComparasion, authData.getPersonEntity());
        comparasionQuizQuestionService.saveAll(tempComparasion);
    }

    private void processingCourses(String rawCourses, AuthData authData, PersonEntity personEntity)
    {
        requestCounter.removeItem(personEntity);
        try {
            Set<CourseEntity> courseEntities = moodleDecryptor.getParsedCoursesList(rawCourses);
            courseService.saveMany(courseEntities);
            for (CourseEntity courseEntity:courseEntities)
            {
                EntityWithAuthData<CourseEntity> temp = new EntityWithAuthData<>(courseEntity,authData);
                courseParseQueue.add(temp);
            }
        } catch (ParseException e) {
            logger.warn("Error while parse raw courses list. Person with name :" + authData.getPersonEntity().getFullName());
            EntityWithAuthData<PersonEntity> temp = new EntityWithAuthData<>(authData.getPersonEntity(), new AuthData());
            personParseQueue.add(temp);
        }
    }

    private void processingQuizAttempts(String rawQuizAttempts, AuthData authData, QuizEntity quizEntity)
    {
        requestCounter.removeItem(quizEntity);
        Set<QuizAttemptEntity> quizAttemptEntities = moodleDecryptor.getParsedQuizAttempts(rawQuizAttempts);
        quizAttemptService.setManyQuiz(quizAttemptEntities, quizEntity);
        quizAttemptService.setManyPerson(quizAttemptEntities, authData.getPersonEntity());
        quizAttemptService.saveAll(quizAttemptEntities);
        for (QuizAttemptEntity quizAttemptEntity : quizAttemptEntities)
        {
            EntityWithAuthData<QuizAttemptEntity> temp = new EntityWithAuthData<>(quizAttemptEntity,authData);
            quizAttemptParseEntity.add(temp);
        }
    }

    private void processingQuizExercise(String rawActivityInstances, AuthData authData, CourseEntity courseEntity)
    {
        requestCounter.removeItem(courseEntity);
        Set<ActivityInstance> activityInstances = moodleDecryptor.getParsedActivityInstances(rawActivityInstances);
        QuiExeLists quiExeLists = moodleDecryptor.getParsedQuiExeListsFromActivityInstances(activityInstances);
        Set<QuizEntity> quizEntities = quiExeLists.getQuizes();
        Set<ExerciseEntity> exerciseEntities = quiExeLists.getExercises();
        quizService.setManyParent(quizEntities, courseEntity);
        quizService.saveAll(quizEntities);
        exerciseService.setManyParent(exerciseEntities, courseEntity);
        exerciseService.saveAll(exerciseEntities);
        for (QuizEntity quizEntity:quizEntities)
        {
            EntityWithAuthData<QuizEntity> temp = new EntityWithAuthData<>(quizEntity,authData);
            quizParseQueue.add(temp);
        }
        for (ExerciseEntity exerciseEntity:exerciseEntities)
        {
            EntityWithAuthData<ExerciseEntity> temp = new EntityWithAuthData<>(exerciseEntity,authData);
            exerciseParseQueue.add(temp);
        }
    }

    private <T extends SuperEntity> void errorProcessing(Throwable error, AuthData authData, T item)
    {
        requestCounter.removeItem(item);

        logger.warn(error.getMessage() +" : "+ item.getClass().getSimpleName()+ " " + item.getId()+ " " + authData.getPersonEntity().getFullName());
        if (item.getClass().getName().equals("org.imjs_man.moodleParser.entity.dataBase.PersonEntity"))
        {
            EntityWithAuthData<PersonEntity> temp = new EntityWithAuthData<>((PersonEntity) item, authData);
            personParseQueue.add(temp);
        }
        if (item.getClass().getName().equals("org.imjs_man.moodleParser.entity.dataBase.QuizEntity"))
        {
            EntityWithAuthData<QuizEntity> temp = new EntityWithAuthData<>((QuizEntity) item, authData);
            quizParseQueue.add(temp);
        }
        if (item.getClass().getName().equals("org.imjs_man.moodleParser.entity.dataBase.CourseEntity"))
        {
            EntityWithAuthData<CourseEntity> temp = new EntityWithAuthData<>((CourseEntity) item, authData);
            courseParseQueue.add(temp);
        }

    }

}
