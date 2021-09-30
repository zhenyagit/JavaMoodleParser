package org.imjs_man.moodleParser.parser;

public class MoodleAuthToken {
    private String v;
    private String site2pstoretoken;
    private String locale;
    private String appctx;

    public String getV() {
        return v;
    }

    public void setV(String v) {
        this.v = v;
    }

    public String getSite2pstoretoken() {
        return site2pstoretoken;
    }

    public void setSite2pstoretoken(String site2pstoretoken) {
        this.site2pstoretoken = site2pstoretoken;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public String getAppctx() {
        return appctx;
    }

    public void setAppctx(String appctx) {
        this.appctx = appctx;
    }
}
