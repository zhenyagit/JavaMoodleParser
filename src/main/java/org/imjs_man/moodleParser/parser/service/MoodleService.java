package org.imjs_man.moodleParser.parser.service;

import org.imjs_man.moodleParser.entity.PersonEntity;
import org.imjs_man.moodleParser.exception.CantFindSessKey;
import org.imjs_man.moodleParser.exception.CantGetAuthoriseToken;
import org.imjs_man.moodleParser.exception.CantGetPersonInfo;
import org.imjs_man.moodleParser.exception.EmptyAuthCookie;
import org.imjs_man.moodleParser.parser.AuthData;
import org.imjs_man.moodleParser.parser.MoodleAuthToken;
import org.imjs_man.moodleParser.parser.MoodleParser;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.swing.text.Document;
import java.io.IOException;
import java.util.concurrent.Future;

@Service
public class MoodleService {

    RestTemplate restTemplate = new RestTemplate(getClientHttpRequestFactory());
    PersonEntity personEntity;
    AuthData authData;
    @Autowired
    private MoodleParser moodleParser;

    private SimpleClientHttpRequestFactory getClientHttpRequestFactory()
    {
        SimpleClientHttpRequestFactory clientHttpRequestFactory
                = new SimpleClientHttpRequestFactory();
        clientHttpRequestFactory.setConnectTimeout(15_000);
        clientHttpRequestFactory.setReadTimeout(15_000);
        return clientHttpRequestFactory;
    }

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

    @Async
    public Future<Document> findPage(String page) throws InterruptedException {
        System.out.println("Looking up " + page);
        Document results = restTemplate.getForObject("http://graph.facebook.com/" + page, Document.class);
        Thread.sleep(1000L);
        return new AsyncResult<Document>(results);
    }


}


