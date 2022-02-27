package org.imjs_man.moodleParser.parser.service;

import org.imjs_man.moodleParser.entity.dataBase.PersonEntity;
import org.jsoup.nodes.Document;

public class AuthData {
    private Document mainPageData;
    private String mainPageDataParsed;
    private String sessKey;
    private String auth_ldapossoCookie;
    private String moodleSessionCookie;
    private String personLogin;
    private String personPassword;
    private PersonEntity personEntity;

    public PersonEntity getPersonEntity() {
        return personEntity;
    }

    public void setPersonEntity(PersonEntity personEntity) {
        this.personEntity = personEntity;
    }

    public String getPersonLogin() {
        return personLogin;
    }

    public void setPersonLogin(String personLogin) {
        this.personLogin = personLogin;
    }

    public String getPersonPassword() {
        return personPassword;
    }

    public void setPersonPassword(String personPassword) {
        this.personPassword = personPassword;
    }

    public String getMainPageDataParsed() {
        return mainPageDataParsed;
    }

    public void setMainPageDataParsed(String mainPageDataParsed) {
        this.mainPageDataParsed = mainPageDataParsed;
    }

    public Document getMainPageData() {
        return mainPageData;
    }

    public void setMainPageData(Document mainPageData) {
        this.mainPageData = mainPageData;
    }

    public String getSessKey() {
        return sessKey;
    }

    public void setSessKey(String sessKey) {
        this.sessKey = sessKey;
    }

    public String getAuth_ldapossoCookie() {
        return auth_ldapossoCookie;
    }

    public void setAuth_ldapossoCookie(String auth_ldapossoCookie) {
        this.auth_ldapossoCookie = auth_ldapossoCookie;
    }

    public String getMoodleSessionCookie() {
        return moodleSessionCookie;
    }

    public void setMoodleSessionCookie(String moodleSessionCookie) {
        this.moodleSessionCookie = moodleSessionCookie;
    }
}
