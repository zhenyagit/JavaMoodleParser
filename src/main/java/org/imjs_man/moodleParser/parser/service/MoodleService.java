package org.imjs_man.moodleParser.parser.service;

import org.imjs_man.moodleParser.entity.PersonEntity;
import org.imjs_man.moodleParser.entity.QuizAttemptEntity;
import org.imjs_man.moodleParser.exception.*;
import org.imjs_man.moodleParser.parser.ActivityInstance;
import org.imjs_man.moodleParser.parser.AuthData;
import org.imjs_man.moodleParser.parser.MoodleAuthToken;
import org.imjs_man.moodleParser.parser.MoodleParser;
import org.imjs_man.moodleParser.service.*;
import org.imjs_man.moodleParser.tokenGenerator.TokenGenerator;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Future;

@Service
public class MoodleService {

    RestTemplate restTemplate = new RestTemplate(getClientHttpRequestFactory());
    PersonEntity personEntity;
    AuthData authData;
    @Autowired
    private MoodleParser moodleParser;
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


    public void setPersonEntity(PersonEntity personEntity) {
        this.personEntity = personEntity;
    }
    private MoodleAuthToken getAuthToken() throws CantGetAuthoriseToken {
        try {
            org.jsoup.nodes.Document tokensData = Jsoup.connect("https://stud.lms.tpu.ru/login/index.php?authSSO=OSSO")
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
            throw new CantGetAuthoriseToken("Timeout exceeded when receiving token");
        }
    }

    public AuthData getAuthData() throws CantGetPersonInfo {
        String personLogin = this.personEntity.getLogin();
        String personPassword = this.personEntity.getPassword();
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
            authData.setSessKey(moodleParser.findSessKey(authData.getMainPageDataParsed()));
            authData.setAuth_ldapossoCookie(res.cookie("auth_ldaposso_authprovider"));
            authData.setMoodleSessionCookie(res.cookie("MoodleSession"));
            authData.setPersonLogin(personLogin);
            authData.setPersonPassword(personPassword);
            if(authData.getAuth_ldapossoCookie()==null || authData.getMoodleSessionCookie()==null) throw new EmptyAuthCookie("Empty cookie");
            return authData;
        } catch (IOException | CantGetAuthoriseToken | EmptyAuthCookie | CantFindSessKey e) {
            throw new CantGetPersonInfo(e.getMessage());
        }
    }

    private PersonEntity getPersonInfo() throws CantGetPersonInfo {
        try {
            Document mainPageData = authData.getMainPageData();
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
            newPerson.setPassword(authData.getPersonPassword());
            newPerson.setLogin(authData.getPersonLogin());
            if (personService.checkId(userId)) {throw new PersonAlreadyExist(personService.getTokenById(userId));}
            // fixme wrong exception
            else {
                String generatedToken = tokenGenerator.generateNewToken();
                newPerson.setToken(generatedToken);
            }
            return newPerson;
        } catch (CantGetPersonInfo | NumberFormatException | PersonAlreadyExist e) {
            throw new CantGetPersonInfo(e.getMessage());
        }
    }

    public String getRawCoursesList() throws CantGetCoursesList {
        try {
            String mainPageData = authData.getMainPageDataParsed();
            String sessKey = moodleParser.findSessKey(mainPageData);
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
        } catch (IOException | CantFindSessKey e) {
            throw new CantGetCoursesList(e.getMessage());
        }
    }
    public Set<ActivityInstance> getActivityInstanceFromCourse(long courseId) throws CantGetActivityInstance {
        try {
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
            for (Element activity : activities) {
                Element aalink = activity.getElementsByClass("aalink").first();
                Element activityicon = activity.getElementsByClass("conlarge activityicon").first();
                if (activityicon == null) activityicon = activity.getElementsByClass("iconlarge activityicon").first();
                Element instancename = activity.getElementsByClass("instancename").first();
                if (aalink != null && activityicon != null && instancename != null) {
                    String href = aalink.attr("href");
                    ActivityInstance tempActivity = new ActivityInstance();
                    tempActivity.setHref(href);
                    tempActivity.setIconSrc(activityicon.attr("src"));
                    tempActivity.setText(instancename.text());
                    tempActivity.setType(moodleParser.getTypeFromInstanceURL(href));
                    tempActivity.setId(moodleParser.getIdFromInstanceURL(href));
                    activityInstances.add(tempActivity);
                }
//                fixme aaaaa
//                if (aalink==null)    throw new CantFindAalinkInInstance("Can`t find aalink");
//                if (activityicon==null) throw new CantFindImgInInstance("Can`t find img");
//                if (instancename==null) throw new CantFindNameInInstance("Can`t find name");

            }
            return activityInstances;
        } catch (IOException e) {
            throw new CantGetActivityInstance(e.getMessage());
        }
    }

    public Set<QuizAttemptEntity> getQuizAttempts(long quizId) throws CantGetActivityInstance {
        try {
            Connection.Response jsonResponse = Jsoup.connect("https://stud.lms.tpu.ru/mod/quiz/view.php")
                    .data("id", Long.toString(quizId))
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
            Element table = jsonResponse.parse().getElementsByClass("generaltable quizattemptsummary").first();
            if (table == null) return new HashSet<>();
            Element temp = table.select("tbody").first();
            if (temp == null) return new HashSet<>();
            Elements attempts = temp.select("tr");
            if (attempts.size() == 0) return new HashSet<>();
            Elements collNames = table.select("th");
            Set<QuizAttemptEntity> quizAttemptEntities = new HashSet<>();
            for(Element attempt: attempts)
            {
                QuizAttemptEntity quizAttemptEntity = new QuizAttemptEntity();
                Elements collum = attempt.select("td");
                for (int collIndex=0; collIndex<collNames.size(); collIndex++)
                {
                    if (collNames.get(collIndex).text().equals("Состояние")) quizAttemptEntity.setAttemptState(collum.get(collIndex).text());
                    if (collNames.get(collIndex).text().split("/")[0].equals("Оценка ")) {
                        quizAttemptEntity.setMaxMark(Double.parseDouble(collNames.get(collIndex).text().split("/")[1]));
                        String tempMark = collum.get(collIndex).text();
                        if (tempMark.length() != 0)
                            if (tempMark.equals("Еще не оценено"))
                                quizAttemptEntity.setNowMark(-1.0);
                            else
                                quizAttemptEntity.setNowMark(Double.parseDouble(tempMark));
                    }
                    if (collNames.get(collIndex).text().equals("Попытка")) quizAttemptEntity.setAttemptNumber(Integer.parseInt(collum.get(collIndex).text()));
                    if (collNames.get(collIndex).text().equals("Просмотр"))
                    {
                        Element title = collum.get(collIndex).select("a").first();
                        if (title!=null) {
                            String href = title.attr("href");
                            quizAttemptEntity.setHref(href);
                            quizAttemptEntity.setId(moodleParser.getIdFromQuizAttemptUrl(href));
                        }
                    }
                }
                if (quizAttemptEntity.getAttemptNumber() == 0)
                    quizAttemptEntity.setAttemptNumber(1);
                if (quizAttemptEntity.getHref() != null)
                    quizAttemptEntities.add(quizAttemptEntity);
            }
            return quizAttemptEntities;
        }
        catch (IOException e)
        {
            throw new CantGetActivityInstance(e.getMessage());
        }
    }


    @Async
    public Future<Document> findPage(String page) throws InterruptedException {
        System.out.println("Looking up " + page);
        Document results = restTemplate.getForObject("http://graph.facebook.com/" + page, Document.class);

        return new AsyncResult<Document>(results);
    }
    private SimpleClientHttpRequestFactory getClientHttpRequestFactory()
    {
        SimpleClientHttpRequestFactory clientHttpRequestFactory
                = new SimpleClientHttpRequestFactory();
        clientHttpRequestFactory.setConnectTimeout(15_000);
        clientHttpRequestFactory.setReadTimeout(15_000);
        return clientHttpRequestFactory;
    }


}


