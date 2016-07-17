package com.bangaloretalkies.corehacker.cheinthusan;

/**
 * Created by corehacker on 7/12/16.
 */
public class ChEinthusanMovieInfo {
    String url;
    String id;
    String name;
    String lang;

    public ChEinthusanMovieInfo (String url, String id, String name, String lang) {
        this.url = url;
        this.id = id;
        this.name = name;
        this.lang = lang;
    }

    public String getUrl() {
        return url;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getLang() {
        return lang;
    }
}
