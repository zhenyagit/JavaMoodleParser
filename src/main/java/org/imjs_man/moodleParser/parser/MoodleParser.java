package org.imjs_man.moodleParser.parser;

import org.imjs_man.moodleParser.entity.CourseEntity;
import org.imjs_man.moodleParser.entity.ExerciseEntity;
import org.imjs_man.moodleParser.entity.PersonEntity;
import org.imjs_man.moodleParser.entity.QuizEntity;
import org.imjs_man.moodleParser.exception.*;
import org.imjs_man.moodleParser.repository.PersonRepository;
import org.imjs_man.moodleParser.service.CourseService;
import org.imjs_man.moodleParser.service.ExerciseService;
import org.imjs_man.moodleParser.service.PersonService;
import org.imjs_man.moodleParser.service.QuizService;
import org.imjs_man.moodleParser.tokenGenerator.TokenGenerator;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.util.*;

@Component
@EnableScheduling
public class MoodleParser {
    //todo add exceptions, static or anything else
    //todo start parsing with text and marks than parse all peoples
    //fixme check all exception text
    //fixme check all private public
    //fixme too many timeout, go into while

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

    @Scheduled(fixedDelay = 10000)
    public void AutoParseCourses()
    {
        for (PersonEntity person : personService.getPersonsToParse())
        {
            try {
                Set<CourseEntity> newCourses = getParsedCoursesList(getRawCoursesList(person.getLogin(),person.getPassword()));
                courseService.saveMany(newCourses);
                person.addCourseEntityList(newCourses);
                personService.savePerson(person);
                List<CourseEntity> allCourses = courseService.getAllCourses();
                for(CourseEntity course: allCourses)
                {
                    QuiExeLists quiExeLists = getParsedActivityInstances(getActivityInstanceFromCourse(person.getLogin(),person.getPassword(),course.getId()));
                    Set<QuizEntity> quizEntities = quiExeLists.quizes;
                    Set<ExerciseEntity> exerciseEntities = quiExeLists.exercises;
                    quizService.setManyParent(quizEntities, course);
                    quizService.saveAll(quizEntities);
                    exerciseService.setManyParent(exerciseEntities, course);
                    exerciseService.saveAll(exerciseEntities);
                    course.addQuizEntityList(quizEntities);
                    course.addExerciseEntityList(exerciseEntities);
                    courseService.saveCourse(course);
                }
            } catch (ParseException | CantGetCoursesList | CantGetActivityInstance e) {
                e.printStackTrace();
            }
        }
    }

    private MoodleAuthToken getAuthToken() throws CantGetAuthoriseToken {
        try {
            Document tokensData = Jsoup.connect("https://stud.lms.tpu.ru/login/index.php?authSSO=OSSO")
                    .data("query", "Java")
                    .userAgent("Mozilla")
                    .cookie("auth", "token")
                    .timeout(30000)
                    .get();
            Elements inputs = tokensData.select("input");
            String value_v = "";
            String value_token = "";
            String value_locale = "";
            String value_app = "";
            for (Element inp : inputs) {
                if (inp.attr("name").equals("v")) value_v = inp.attr("value");
                if (inp.attr("name").equals("site2pstoretoken")) value_token = inp.attr("value");
                if (inp.attr("name").equals("locale")) value_locale = inp.attr("value");
                if (inp.attr("name").equals("appctx")) value_app = inp.attr("value");
            }
            MoodleAuthToken authToken =  new MoodleAuthToken();
            authToken.setV(value_v);
            authToken.setSite2pstoretoken(value_token);
            authToken.setLocale(value_locale);
            authToken.setAppctx(value_app);
            return authToken;
        }
        catch (IOException e)
        {
            throw new CantGetAuthoriseToken("Error token");
        }
    }
    private AuthData getAuthData(String personLogin, String personPassword) throws CantGetPersonInfo {
        try {
            MoodleAuthToken authToken = getAuthToken();
            Connection.Response res = Jsoup.connect("https://aid.main.tpu.ru/sso/auth")
                    .data("v", authToken.getV())
                    .data("site2pstoretoken", authToken.getSite2pstoretoken())
                    .data("locale", authToken.getLocale())
                    .data("appctx", authToken.getAppctx())
                    .data("ssousername", personLogin)
                    .data("password", personPassword)
                    .data("domen", "main")
                    .userAgent("Mozilla")
                    .timeout(300000)
                    .method(Connection.Method.POST)
                    .execute();
            AuthData authData = new AuthData();
            authData.setMainPageData(res.parse());
            authData.setMainPageDataParsed(authData.getMainPageData().toString());
            authData.setSessKey(findSessKey(authData.getMainPageDataParsed()));
            authData.setAuth_ldapossoCookie(res.cookie("auth_ldaposso_authprovider"));
            authData.setMoodleSessionCookie(res.cookie("MoodleSession"));
            if(authData.getAuth_ldapossoCookie().length()==0 || authData.getMoodleSessionCookie().length()==0) throw new EmptyAuthCookie("Empty cookie");
            return authData;
        } catch (IOException | CantGetAuthoriseToken | EmptyAuthCookie | CantFindSessKey e) {
            throw new CantGetPersonInfo(e.getMessage());
        }
    }
    private PersonEntity getPersonInfo(String personLogin, String personPassword) throws CantGetPersonInfo {
        try {
            Document mainPageData = getAuthData(personLogin, personPassword).getMainPageData();
            Elements mainPageDivs = mainPageData.select("div");
            Element loginInfoElem = null;
            Element uservisibilityElem = null;
            Element userpictureElem = null;
            for (Element item : mainPageDivs) {
                if (item.attr("class").equals("logininfo")) loginInfoElem = item;
                if (item.attr("class").equals("uservisibility")) uservisibilityElem = item;
            }
            if (uservisibilityElem == null) {throw new CantGetPersonInfo("Incorrect data");}
            int userId = Integer.parseInt(Objects.requireNonNull(uservisibilityElem.select("a").first()).attr("data-userid"));
            assert loginInfoElem != null;
            String[] temp = Objects.requireNonNull(loginInfoElem.select("a").first()).text().split(" ");
            String personName = temp[1];
            String personSurname = temp[0];
            String personPatronymic = temp[2];
            Elements mainPageImgs = mainPageData.select("img");
            for (Element item : mainPageImgs) {
                if (item.attr("width").equals("100")) userpictureElem = item;
            }
            assert userpictureElem != null;
            String personGroupName = userpictureElem.attr("title");
            PersonEntity newPerson = new PersonEntity();
            newPerson.setId(userId);
            newPerson.setName(personName);
            newPerson.setSurname(personSurname);
            newPerson.setPatronymic(personPatronymic);
            newPerson.setGroupName(personGroupName);
            newPerson.setPassword(personPassword);
            newPerson.setLogin(personLogin);
            if (personService.checkId(userId)) {throw new PersonAlreadyExist(personService.getTokenById(userId));}
            else {
                String generatedToken = tokenGenerator.generateNewToken();
                newPerson.setToken(generatedToken);
            }
            return newPerson;
        } catch (CantGetPersonInfo | NumberFormatException | PersonAlreadyExist e) {
            throw new CantGetPersonInfo(e.getMessage());
        }
    }

    public PersonEntity auth(String login, String password) throws CantAuthoriseInMoodle {
        try{
            return getPersonInfo(login, password);
        }
        catch (CantGetPersonInfo e) {throw new CantAuthoriseInMoodle(e.getMessage());}
    }


    public String findSessKey(String mainText) throws CantFindSessKey {
        char[] words = mainText.toCharArray();
        int startIndex = mainText.indexOf("sesskey=");
        StringBuilder sesskeyBytes = new StringBuilder();
        for (int i=0; i< 10; i++)
        {
            sesskeyBytes.append(words[startIndex + 8 + i]);
        }
        if (sesskeyBytes.length()==0) throw new CantFindSessKey("Can`t find session key");
        return sesskeyBytes.toString();
    }

    public String getRawCoursesList(String login, String password) throws CantGetCoursesList {
        try {
            AuthData authData = getAuthData(login, password);
            String mainPageData = authData.getMainPageDataParsed();
            String sessKey = findSessKey(mainPageData);
            String auth_ldapossoCookie = authData.getAuth_ldapossoCookie();
            String moodleSessionCookie = authData.getMoodleSessionCookie();
            String jsonRequest = "[{\"index\":0,\"methodname\":\"core_course_get_enrolled_courses_by_timeline_classification\",\"args\":{\"offset\":0,\"limit\":0,\"classification\":\"all\",\"sort\":\"fullname\",\"customfieldname\":\"\",\"customfieldvalue\":\"\"}}]";
            Connection.Response jsonResponse = Jsoup.connect("https://stud.lms.tpu.ru/lib/ajax/service.php")
                    .data("sesskey", sessKey)
                    .data("info", "core_calendar_get_calendar_monthly_view")
                    .followRedirects(true)
                    .ignoreHttpErrors(true)
                    .ignoreContentType(true)
                    .userAgent("Mozilla")
                    .cookie("_ga", "GA1.2.653870628.1616950848")
                    .cookie("_ym_d", "1617257634")
                    .cookie("_ym_uid", "161725763494258622")
                    .cookie("auth_ldaposso_authprovider", auth_ldapossoCookie)
                    .cookie("MoodleSession", moodleSessionCookie)
                    .method(Connection.Method.POST)
                    .requestBody(jsonRequest)
                    .maxBodySize(1_000_000 * 30)
                    .timeout(300000)
                    .execute();
            return jsonResponse.body();
        } catch (CantGetPersonInfo | IOException | CantFindSessKey e) {
            throw new CantGetCoursesList(e.getMessage());
        }
    }
    public Set<CourseEntity> getParsedCoursesList(String jsonResponse) throws ParseException {
        JSONArray jsonparsedresponse = (JSONArray) new JSONParser().parse(jsonResponse);
        JSONObject tempObj =  (JSONObject) jsonparsedresponse.get(0);
//        System.out.println(tempObj);
        tempObj =  (JSONObject) tempObj.get("data");
        JSONArray tempArr =  (JSONArray) tempObj.get("courses");
        Set<CourseEntity> courseList = new HashSet<>();
        for (Object o : tempArr) {
            JSONObject courseJson = (JSONObject) o;
            CourseEntity course = new CourseEntity();
            course.setId(Long.parseLong(courseJson.get("id").toString()));
            course.setName(courseJson.get("fullname").toString());
            course.setCoursecategory(courseJson.get("coursecategory").toString());
            course.setCourseimage(courseJson.get("courseimage").toString());
            course.setEnddate(Integer.parseInt(courseJson.get("enddate").toString()));
            course.setFullnamedisplay(courseJson.get("fullnamedisplay").toString());
            course.setHasprogress(Objects.equals(courseJson.get("hasprogress").toString(), "1"));
            course.setHidden(Objects.equals(courseJson.get("hidden").toString(), "1"));
            course.setProgress(Integer.parseInt(courseJson.get("progress").toString()));
            course.setIsfavourite(Objects.equals(courseJson.get("isfavourite").toString(), "1"));
            course.setStartdate(Integer.parseInt(courseJson.get("startdate").toString()));
            course.setSummaryformat(Integer.parseInt(courseJson.get("summaryformat").toString()));
            course.setSummary(courseJson.get("summary").toString());
            courseList.add(course);
        }
        return courseList;
    }
    public String getTypeFromInstanceURL(String url)
    {
        //todo exception
        String[] parseUrl= url.split("/");
        return parseUrl[parseUrl.length-2];
    }
    public long geIdeFromInstanceURL(String url)
    {
        //todo exception
        String[] parseUrl= url.split("/");
        String lastWord = parseUrl[parseUrl.length-1];
        char[] words = lastWord.toCharArray();
        int startIndex = lastWord.indexOf("id=");
        StringBuilder sesskeyBytes = new StringBuilder();
        for (int i = startIndex + 3; i < lastWord.length(); i++)
        {
            sesskeyBytes.append(words[i]);
        }
        return Long.parseLong(sesskeyBytes.toString());
    }

    public QuiExeLists getParsedActivityInstances(Set<ActivityInstance> activityInstances)
    {
        QuiExeLists listQuizExercise = new QuiExeLists();
        Set<QuizEntity> quizEntities = new HashSet<>();
        Set<ExerciseEntity> exerciseEntities = new HashSet<>();
        for (ActivityInstance activityInstance : activityInstances)
        {
            //todo instance type can be book,resource,url,page,forum,glossary
            switch (activityInstance.getType()) {
                case ("quiz"):
                    if (quizService.checkId(activityInstance.getId()))
                        break;
                    else {
                        QuizEntity quizEntity = new QuizEntity();
                        quizEntity.setId(activityInstance.getId());
                        quizEntity.setName(activityInstance.getText());
                        quizEntity.setHref(activityInstance.getHref());
                        quizEntities.add(quizEntity);
                        //todo add to db, add to user
                        break;
                    }
                case ("assign"):
                    if (exerciseService.checkId(activityInstance.getId()))
                        break;
                    else {
                        ExerciseEntity exerciseEntity = new ExerciseEntity();
                        exerciseEntity.setId(activityInstance.getId());
                        exerciseEntity.setName(activityInstance.getText());
                        exerciseEntity.setHref(activityInstance.getHref());
                        exerciseEntities.add(exerciseEntity);
                        break;
                    }
            }
        }
        listQuizExercise.quizes = quizEntities;
        listQuizExercise.exercises = exerciseEntities;
        return listQuizExercise;
    }

    public Set<ActivityInstance> getActivityInstanceFromCourse(String login, String password, long courseId) throws CantGetActivityInstance {
        try {
            AuthData authData = getAuthData(login, password);
            Connection.Response jsonResponse = Jsoup.connect("https://stud.lms.tpu.ru/course/view.php")
                    .data("id", Long.toString(courseId))
                    .followRedirects(true)
                    .ignoreHttpErrors(true)
                    .ignoreContentType(true)
                    .userAgent("Mozilla")
                    .cookie("_ga", "GA1.2.653870628.1616950848")
                    .cookie("_ym_d", "1617257634")
                    .cookie("_ym_uid", "161725763494258622")
                    .cookie("auth_ldaposso_authprovider", authData.getAuth_ldapossoCookie())
                    .cookie("MoodleSession", authData.getMoodleSessionCookie())
                    .method(Connection.Method.POST)
                    .maxBodySize(1_000_000 * 30)
                    .timeout(300000)
                    .execute();
            Elements activities = jsonResponse.parse().getElementsByClass("activityinstance");
//            System.out.println(activities);
            Set<ActivityInstance> activityInstances = new HashSet<>();
            for(Element activity: activities)
            {
                Element aalink = activity.getElementsByClass("aalink").first();
                Element activityicon = activity.getElementsByClass("conlarge activityicon").first();
                if (activityicon==null) activityicon = activity.getElementsByClass("iconlarge activityicon").first();
                Element instancename = activity.getElementsByClass("instancename").first();
                if (aalink!=null && activityicon!=null && instancename!=null)
                {
                    String href = aalink.attr("href");
                    ActivityInstance tempActivity = new ActivityInstance();
                    tempActivity.setHref(href);
                    tempActivity.setIconSrc(activityicon.attr("src"));
                    tempActivity.setText(instancename.text());
                    tempActivity.setType(getTypeFromInstanceURL(href));
                    tempActivity.setId(geIdeFromInstanceURL(href));
                    activityInstances.add(tempActivity);
                }
//                fixme aaaaa
//                if (aalink==null)    throw new CantFindAalinkInInstance("Can`t find aalink");
//                if (activityicon==null) throw new CantFindImgInInstance("Can`t find img");
//                if (instancename==null) throw new CantFindNameInInstance("Can`t find name");

            }
            return activityInstances;
        }
        catch (CantGetPersonInfo | IOException e)
        {
            throw new CantGetActivityInstance(e.getMessage());
        }
    }



}
