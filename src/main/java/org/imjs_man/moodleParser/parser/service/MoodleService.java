package org.imjs_man.moodleParser.parser.service;

import io.netty.channel.ChannelOption;
import org.imjs_man.moodleParser.entity.PersonEntity;
import org.imjs_man.moodleParser.entity.QuizAttemptEntity;
import org.imjs_man.moodleParser.exception.*;
import org.imjs_man.moodleParser.parser.decryptor.ActivityInstance;
import org.imjs_man.moodleParser.parser.MoodleParser;
import org.imjs_man.moodleParser.service.*;
import org.imjs_man.moodleParser.tokenGenerator.TokenGenerator;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.server.Session;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import java.io.IOException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Service
public class MoodleService {

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

    WebClientConfiguration webClientConfiguration = new WebClientConfiguration();
    private final WebClient webClient = webClientConfiguration.webClientWithTimeout();


    // todo здесь только запросы
    public Mono<String>  getAuthToken() {
        MultiValueMap<String, String> myCookies = new LinkedMultiValueMap<String, String>();
        myCookies.add("auth", "token");
        WebClient tempClient = WebClient.builder().clientConnector(new ReactorClientHttpConnector(
                HttpClient.create().followRedirect(true))).build();
        return tempClient
                .get()
//                .uri(uriBuilder -> uriBuilder.path("/login/index.php")
//                        .queryParam("authSSO", "OSSO")
//                        .queryParam("query", "Java")
//                        .build())
                .uri("https://stud.lms.tpu.ru/login/index.php?authSSO=OSSO&query=Java")
//                .cookies(cookies -> cookies.addAll(myCookies))
                .header("User-Agent", "insomnia/2021.7.2")
                .retrieve()
                .bodyToMono(String.class);
    }

    public MoodleAuthToken getAuthTokenOld() throws CantGetAuthoriseToken {
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
            throw new CantGetAuthoriseToken("Timeout exceeded when receiving token");
        }
    }


    public Connection.Response getAuthDataOld(PersonEntity person, MoodleAuthToken authToken) throws CantGetPersonInfo
    {
        try {
            String personLogin = person.getLogin();
            String personPassword = person.getPassword();
            return Jsoup.connect("https://aid.main.tpu.ru/sso/auth")
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
        } catch (IOException e) {
            throw new CantGetPersonInfo(e.getMessage());
        }
    }

    public WebClient.RequestBodySpec getDefaultPost(AuthData authData, String uri, MultiValueMap<String, String> queryParamsMap)
    {
        MultiValueMap<String, String> myCookies = new LinkedMultiValueMap<>();
        myCookies.add("_ga", "GA1.2.653870628.1616950848");
        myCookies.add("_ym_d", "1617257634");
        myCookies.add("_ym_uid", "161725763494258622");
        myCookies.add("auth_ldaposso_authprovider", authData.getAuth_ldapossoCookie());
        myCookies.add("MoodleSession", authData.getMoodleSessionCookie());
        return webClient
                .post()
                .uri(uriBuilder -> uriBuilder.path(uri)
                        .queryParams(queryParamsMap)
                        .build())
                .cookies(cookies -> cookies.addAll(myCookies))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

    }

    public Mono<String> getDataFromDefaultPost(AuthData authData, String uri, MultiValueMap<String, String> queryParamsMap, String bodyMap) {
        return getDefaultPost(authData, uri, queryParamsMap)
                .body(BodyInserters.fromValue(bodyMap))
                .retrieve()
                .bodyToMono(String.class);
    }

    public Mono<String> getDataFromDefaultPost(AuthData authData, String uri, MultiValueMap<String, String> queryParamsMap) {
        return getDefaultPost(authData, uri, queryParamsMap)
                .retrieve()
                .bodyToMono(String.class);
    }

    public Mono<String> getDataFromDefaultGet(AuthData authData, String uri, MultiValueMap<String, String> queryParamsMap)
    {
        MultiValueMap<String, String> myCookies = new LinkedMultiValueMap<>();
        myCookies.add("_ga", "GA1.2.653870628.1616950848");
        myCookies.add("_ym_d", "1617257634");
        myCookies.add("_ym_uid", "161725763494258622");
        myCookies.add("auth_ldaposso_authprovider", authData.getAuth_ldapossoCookie());
        myCookies.add("MoodleSession", authData.getMoodleSessionCookie());
//        Session.Cookie cookieee = new Session.Cookie();
//        cookieee.setDomain("asdasd");

        return webClient
                .get()
                .uri(uriBuilder -> uriBuilder.path(uri)
                        .queryParams(queryParamsMap)
                        .build())
                .cookies(cookies -> cookies.addAll(myCookies))
                .header(HttpHeaders.CONTENT_TYPE, String.valueOf(MediaType.TEXT_HTML))
                .retrieve()
                .bodyToMono(String.class);
    }


    public Mono<String> getRawCoursesList(AuthData authData) {
        String uri = "/lib/ajax/service.php";
        String body = "[{\"index\":0,\"methodname\":\"core_course_get_enrolled_courses_by_timeline_classification\",\"args\":{\"offset\":0,\"limit\":0,\"classification\":\"all\",\"sort\":\"fullname\",\"customfieldname\":\"\",\"customfieldvalue\":\"\"}}]";
        MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        queryParams.add("sesskey", authData.getSessKey());
        queryParams.add("info", "core_calendar_get_calendar_monthly_view");
        return getDataFromDefaultPost(authData, uri, queryParams, body);
    }

    public Mono<String> getRawActivityInstances(AuthData authData, long courseId) {
        String uri = "/course/view.php";
        MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        queryParams.add("id", Long.toString(courseId));
        queryParams.add("authSSO","OSSO");
        queryParams.add("query","Java");
        return getDataFromDefaultGet(authData, uri, queryParams);
    }

    public Mono<String> getRawQuizAttempts(AuthData authData, long quizId)  {
        String uri = "/mod/quiz/view.php";
        MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        queryParams.add("id", Long.toString(quizId));
        return getDataFromDefaultPost(authData, uri, queryParams);
    }

//    public Mono<Document> getUserByIdAsync(final String id) throws CantFindSessKey {
//        String mainPageData = authData.getMainPageDataParsed();
//        String sessKey = moodleParser.findSessKey(mainPageData);
//        String auth_ldapossoCookie = authData.getAuth_ldapossoCookie();
//        String moodleSessionCookie = authData.getMoodleSessionCookie();
//        MultiValueMap<String, String> myCookies = new LinkedMultiValueMap<String, String>();
//        String jsonRequest = "[{\"index\":0,\"methodname\":\"core_course_get_enrolled_courses_by_timeline_classification\",\"args\":{\"offset\":0,\"limit\":0,\"classification\":\"all\",\"sort\":\"fullname\",\"customfieldname\":\"\",\"customfieldvalue\":\"\"}}]";
//        myCookies.add("_ga", "GA1.2.653870628.1616950848");
//        myCookies.add("_ym_d", "1617257634");
//        myCookies.add("_ym_uid", "161725763494258622");
//        myCookies.add("auth_ldaposso_authprovider", auth_ldapossoCookie);
//        myCookies.add("MoodleSession", moodleSessionCookie);
//        logger.info("Looking up " + "asdasd");
//        return webClient
//                .post()
//                .uri(uriBuilder -> uriBuilder.path("/course/view.php")
//                        .queryParam("sesskey", sessKey)
//                        .queryParam("info", "core_calendar_get_calendar_monthly_view")
//                        .build())
//                .cookies(cookies -> cookies.addAll(myCookies))
//                .retrieve()
//                .bodyToMono(Document.class);
//    }

}

