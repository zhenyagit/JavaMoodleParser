package org.imjs_man.moodleParser.parser.decryptor;

import org.imjs_man.moodleParser.entity.CourseEntity;
import org.imjs_man.moodleParser.entity.PersonEntity;
import org.imjs_man.moodleParser.entity.QuizAttemptEntity;
import org.imjs_man.moodleParser.exception.CantFindSessKey;
import org.imjs_man.moodleParser.exception.CantGetPersonInfo;
import org.imjs_man.moodleParser.exception.EmptyAuthCookie;
import org.imjs_man.moodleParser.exception.PersonAlreadyExist;
import org.imjs_man.moodleParser.parser.service.AuthData;
import org.imjs_man.moodleParser.parser.service.MoodleAuthToken;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import javax.xml.parsers.DocumentBuilder;
import java.io.IOException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;


@Service
public class MoodleDecryptor{
    //todo здесь только string to entity

    public MoodleAuthToken getParsedAuthToken(String tokenData)
    {
        Elements inputs = Jsoup.parse(tokenData).select("input");
        String value_v = "";
        String value_token = "";
        String value_locale = "";
        String value_app = "";
        for (Element inp : inputs) {
            String tempName = inp.attr("name");
            if (tempName.equals("v")) value_v = inp.attr("value");
            if (tempName.equals("site2pstoretoken")) value_token = inp.attr("value");
            if (tempName.equals("locale")) value_locale = inp.attr("value");
            if (tempName.equals("appctx")) value_app = inp.attr("value");
        }
        MoodleAuthToken authToken = new MoodleAuthToken();
        authToken.setV(value_v);
        authToken.setSite2pstoretoken(value_token);
        authToken.setLocale(value_locale);
        authToken.setAppctx(value_app);
        return authToken;
    }

    public AuthData getParsedAuthData(PersonEntity person, Connection.Response res) throws CantFindSessKey, EmptyAuthCookie, IOException {
        AuthData authData = new AuthData();
        authData.setMainPageData(res.parse());
        authData.setMainPageDataParsed(authData.getMainPageData().toString());
        authData.setSessKey(findSessKey(authData.getMainPageDataParsed()));
        authData.setAuth_ldapossoCookie(res.cookie("auth_ldaposso_authprovider"));
        authData.setMoodleSessionCookie(res.cookie("MoodleSession"));
        authData.setPersonLogin(person.getLogin());
        authData.setPersonPassword(person.getPassword());
        if (authData.getAuth_ldapossoCookie() == null || authData.getMoodleSessionCookie() == null)
            throw new EmptyAuthCookie("Empty cookie");
        return  authData;
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

    private PersonEntity getParsedPersonInfo(AuthData authData) throws CantGetPersonInfo
    {
        Document mainPageData = authData.getMainPageData();
        Elements mainPageDivs = mainPageData.select("div");
        Element loginInfoElem = null;
        Element uservisibilityElem = null;
        Element userpictureElem = null;
        for (Element item : mainPageDivs) {
            if (item.attr("class").equals("logininfo")) loginInfoElem = item;
            if (item.attr("class").equals("uservisibility")) uservisibilityElem = item;
        }
        if (uservisibilityElem == null) {
            throw new CantGetPersonInfo("Incorrect data");
        }
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
        return newPerson;
    }

    public Set<CourseEntity> getParsedCoursesList(String jsonResponse) throws ParseException {
        JSONArray jsonparsedresponse = (JSONArray) new JSONParser().parse(jsonResponse);
        JSONObject tempObj =  (JSONObject) jsonparsedresponse.get(0);
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
    public Set<ActivityInstance> getParsedActivityInstancesFromCourse(String rawActivityInstances) {
        Elements activities = Jsoup.parse(rawActivityInstances).getElementsByClass("activityinstance");
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
                tempActivity.setType(getTypeFromInstanceURL(href));
                tempActivity.setId(getIdFromInstanceURL(href));
                activityInstances.add(tempActivity);
            }
            //      fixme aaaaa
            //      if (aalink==null)    throw new CantFindAalinkInInstance("Can`t find aalink");
            //      if (activityicon==null) throw new CantFindImgInInstance("Can`t find img");
            //      if (instancename==null) throw new CantFindNameInInstance("Can`t find name");

        }
        return activityInstances;
    }

    public Set<QuizAttemptEntity> getParsedQuizAttempts(String rawQuizAttempts) {
        Element table = Jsoup.parse(rawQuizAttempts).getElementsByClass("generaltable quizattemptsummary").first();
        if (table == null) return new HashSet<>();
        Element temp = table.select("tbody").first();
        if (temp == null) return new HashSet<>();
        Elements attempts = temp.select("tr");
        if (attempts.size() == 0) return new HashSet<>();
        Elements collNames = table.select("th");
        Set<QuizAttemptEntity> quizAttemptEntities = new HashSet<>();
        for (Element attempt : attempts) {
            QuizAttemptEntity quizAttemptEntity = new QuizAttemptEntity();
            Elements collum = attempt.select("td");
            for (int collIndex = 0; collIndex < collNames.size(); collIndex++) {
                if (collNames.get(collIndex).text().equals("Состояние"))
                    quizAttemptEntity.setAttemptState(collum.get(collIndex).text());
                if (collNames.get(collIndex).text().split("/")[0].equals("Оценка ")) {
                    quizAttemptEntity.setMaxMark(Double.parseDouble(collNames.get(collIndex).text().split("/")[1]));
                    String tempMark = collum.get(collIndex).text();
                    if (tempMark.length() != 0)
                        if (tempMark.equals("Еще не оценено"))
                            quizAttemptEntity.setNowMark(-1.0);
                        else
                            quizAttemptEntity.setNowMark(Double.parseDouble(tempMark));
                }
                if (collNames.get(collIndex).text().equals("Попытка"))
                    quizAttemptEntity.setAttemptNumber(Integer.parseInt(collum.get(collIndex).text()));
                if (collNames.get(collIndex).text().equals("Просмотр")) {
                    Element title = collum.get(collIndex).select("a").first();
                    if (title != null) {
                        String href = title.attr("href");
                        quizAttemptEntity.setHref(href);
                        quizAttemptEntity.setId(getIdFromQuizAttemptUrl(href));
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

    public String getTypeFromInstanceURL(String url)
    {
        //todo exception
        String[] parseUrl= url.split("/");
        return parseUrl[parseUrl.length-2];
    }
    public long getIdFromQuizAttemptUrl(String url)
    {
        //todo exception
        String[] parseUrl= url.split("/");
        String lastWord = parseUrl[parseUrl.length-1];
        char[] words = lastWord.toCharArray();
        int startIndex = lastWord.indexOf("attempt=");
        StringBuilder sesskeyBytes = new StringBuilder();
        int index = startIndex+"attempt=".length();
        while(words[index]!= '&')
        {
            sesskeyBytes.append(words[index]);
            index++;
        }
        return Long.parseLong(sesskeyBytes.toString());
    }
    public long getIdFromInstanceURL(String url)
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
}