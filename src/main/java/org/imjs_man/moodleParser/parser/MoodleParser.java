package org.imjs_man.moodleParser.parser;

import org.imjs_man.moodleParser.entity.PersonEntity;
import org.imjs_man.moodleParser.exception.CantAuthoriseInMoodle;
import org.imjs_man.moodleParser.exception.CantGetAuthoriseToken;
import org.imjs_man.moodleParser.exception.CantGetPersonInfo;
import org.imjs_man.moodleParser.exception.PersonAlreadyExist;
import org.imjs_man.moodleParser.repository.PersonRepository;
import org.imjs_man.moodleParser.tokenGenerator.TokenGenerator;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.util.Objects;

@Component
public class MoodleParser {
    //todo add exeptions, static or anything else

    @Autowired
    TokenGenerator tokenGenerator;
    @Autowired
    PersonRepository personRepository;

    private MoodleAuthToken getAuthToken() throws CantGetAuthoriseToken {
        try {
            Document tokensData = Jsoup.connect("https://stud.lms.tpu.ru/login/index.php?authSSO=OSSO")
                    .data("query", "Java")
                    .userAgent("Mozilla")
                    .cookie("auth", "token")
                    .timeout(3000)
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
    private PersonEntity getPersonInfo(MoodleAuthToken authToken, String personLogin, String personPassword) throws CantGetPersonInfo, PersonAlreadyExist {
        try {
            Document mainPageData = Jsoup.connect("https://aid.main.tpu.ru/sso/auth")
                    .data("v", authToken.getV())
                    .data("site2pstoretoken", authToken.getSite2pstoretoken())
                    .data("locale", authToken.getLocale())
                    .data("appctx", authToken.getAppctx())
                    .data("ssousername", personLogin)
                    .data("password", personPassword)
                    .data("domen", "main")
                    .userAgent("Mozilla")
                    .cookie("auth", "token")
                    .timeout(300000)
                    .post();
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
            PersonEntity tempPerson = personRepository.findById(userId);
            if (tempPerson!=null) {throw new PersonAlreadyExist(personRepository.findById(userId).getToken());}
            else {
                String generatedToken = tokenGenerator.generateNewToken();
                newPerson.setToken(generatedToken);
            }
            return newPerson;
        }
        catch (IOException e)
        {
            throw new CantGetPersonInfo("Error person info");
        }
    }

    public String auth(String login, String password) throws CantAuthoriseInMoodle {
        try{
            MoodleAuthToken authToken = getAuthToken();
            PersonEntity personInfo = getPersonInfo(authToken, login, password);
            personRepository.save(personInfo);
            return personInfo.getToken();
        }
        catch (CantGetAuthoriseToken | CantGetPersonInfo e) {throw new CantAuthoriseInMoodle(e.getMessage());}
        catch (PersonAlreadyExist e) {return e.getMessage();}
    }

}
