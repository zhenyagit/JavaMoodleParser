package org.imjs_man.moodleParser.parser;

import org.imjs_man.moodleParser.entity.*;
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

import java.util.*;

@Component
@EnableScheduling
public class MoodleParser {

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
    // todo здесь сделаем такую штуку, вся хрень с сервиса в обертку

    private final RequestCounter requestCounter = new RequestCounter();
    private final PriorityQueue<EntityWithAuthData<CourseEntity>>     courseToParseQueue    = new PriorityQueue<>();
    private final PriorityQueue<EntityWithAuthData<QuizEntity>>       quizToParseQueue      = new PriorityQueue<>();
    private final PriorityQueue<EntityWithAuthData<ExerciseEntity>>   exerciseToParseQueue  = new PriorityQueue<>();
    private final PriorityQueue<PersonEntity>  personToParseQueue    = new PriorityQueue<>();
    private static final Logger logger = LoggerFactory.getLogger(MoodleParser.class);

    private static final int MAX_REQUEST_COUNTER = 20;

    @EventListener(ApplicationReadyEvent.class)
    public void addPersonsToQueue()
    {
        for (PersonEntity person : personService.getPersonsToParse()) {
            personToParseQueue.add(person);
            logger.info("Person added to queue : "+ person.getFullName());
        }
    }

    @Scheduled(fixedDelay = 1000)
    private void AutoParsePersons() {
        while((personToParseQueue.peek()!=null) && (requestCounter.getCount()< MAX_REQUEST_COUNTER))
        {
            PersonEntity person = personToParseQueue.poll();
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
    }

    @Scheduled(fixedDelay = 1000)
    private void AutoParseCourses() {
        if ((courseToParseQueue.peek()!=null) && (requestCounter.getCount()< MAX_REQUEST_COUNTER))
        {
            EntityWithAuthData<CourseEntity> temp = courseToParseQueue.poll();
            assert temp != null;
            getQuizExerciseByCourseId(temp.getAuthData(),temp.getEntity());
        }
    }
    @Scheduled(fixedDelay = 1000)
    private void AutoParseQuiz() {
        while ((quizToParseQueue.peek()!=null && (requestCounter.getCount()< MAX_REQUEST_COUNTER)))
        {
            EntityWithAuthData<QuizEntity> temp = quizToParseQueue.poll();
            assert temp != null;
            getQuizAttemptsByQuizId(temp.getAuthData(),temp.getEntity());
        }

    }

    public PersonEntity auth(String login, String password) throws CantAuthoriseInMoodle {
        try {
            PersonEntity tempPerson = new PersonEntity(login,password);
            MoodleAuthToken moodleAuthToken = moodleService.getAuthTokenOld();
            Connection.Response respAuth = moodleService.getAuthDataOld(tempPerson, moodleAuthToken);
            AuthData authData = moodleDecryptor.getParsedAuthData(tempPerson, respAuth);
            PersonEntity personReturned = moodleDecryptor.getParsedPersonInfo(authData);
            personReturned.setToken(tokenGenerator.generateNewToken());
            personService.savePerson(personReturned);
            personToParseQueue.add(personReturned);
            return personReturned;
        } catch (CantGetPersonInfo | CantGetAuthoriseToken | CantFindSessKey | EmptyAuthCookie | CantParseMainDocument e) {
            throw new CantAuthoriseInMoodle("When authorise :" + e.getMessage());
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

    private void processingCourses(String rawCourses, AuthData authData, PersonEntity personEntity)
    {
        requestCounter.removeItem(personEntity);
        try {
            Set<CourseEntity> courseEntities = moodleDecryptor.getParsedCoursesList(rawCourses);
            courseService.saveMany(courseEntities);
            for (CourseEntity courseEntity:courseEntities)
            {
                EntityWithAuthData<CourseEntity> temp = new EntityWithAuthData<>(courseEntity,authData);
                courseToParseQueue.add(temp);
            }
        } catch (ParseException e) {
            logger.warn("Error while parse raw courses list. Person with name :" + authData.getPersonEntity().getFullName());
            personToParseQueue.add(authData.getPersonEntity());
        }
    }

    private void processingQuizAttempts(String rawQuizAttempts, AuthData authData, QuizEntity quizEntity)
    {
        requestCounter.removeItem(quizEntity);
        Set<QuizAttemptEntity> quizAttemptEntities = moodleDecryptor.getParsedQuizAttempts(rawQuizAttempts);
        quizAttemptService.setManyQuiz(quizAttemptEntities, quizEntity);
        quizAttemptService.setManyPerson(quizAttemptEntities, authData.getPersonEntity());
        quizAttemptService.saveAll(quizAttemptEntities);
    }

    private void processingQuizExercise(String rawActivityInstances, AuthData authData, CourseEntity courseEntity)
    {
        requestCounter.removeItem(courseEntity);
        Set<ActivityInstance> activityInstances = moodleDecryptor.getParsedActivityInstances(rawActivityInstances);
        QuiExeLists quiExeLists = moodleDecryptor.getParsedQuiExeListsFromActivityInstanses(activityInstances);
        Set<QuizEntity> quizEntities = quiExeLists.getQuizes();
        Set<ExerciseEntity> exerciseEntities = quiExeLists.getExercises();
        quizService.setManyParent(quizEntities, courseEntity);
        quizService.saveAll(quizEntities);
        exerciseService.setManyParent(exerciseEntities, courseEntity);
        exerciseService.saveAll(exerciseEntities);
        for (QuizEntity quizEntity:quizEntities)
        {
            EntityWithAuthData<QuizEntity> temp = new EntityWithAuthData<>(quizEntity,authData);
            quizToParseQueue.add(temp);
        }
        for (ExerciseEntity exerciseEntity:exerciseEntities)
        {
            EntityWithAuthData<ExerciseEntity> temp = new EntityWithAuthData<>(exerciseEntity,authData);
            exerciseToParseQueue.add(temp);
        }
    }

    private <T extends SuperEntity> void errorProcessing(Throwable error, AuthData authData, T item)
    {
        requestCounter.removeItem(item);
        logger.warn(error.getMessage());
        if (item.getClass().getName().equals("org.imjs_man.moodleParser.entity.PersonEntity"))
        {
            personToParseQueue.add((PersonEntity) item);
        }
        if (item.getClass().getName().equals("org.imjs_man.moodleParser.entity.QuizEntity"))
        {
            EntityWithAuthData<QuizEntity> temp = new EntityWithAuthData<>((QuizEntity) item, authData);
            quizToParseQueue.add(temp);
        }
        if (item.getClass().getName().equals("org.imjs_man.moodleParser.entity.CourseEntity"))
        {
            EntityWithAuthData<CourseEntity> temp = new EntityWithAuthData<>((CourseEntity) item, authData);
            courseToParseQueue.add(temp);
        }

    }

}
