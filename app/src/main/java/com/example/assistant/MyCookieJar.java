package com.example.assistant;


import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;

public class MyCookieJar implements CookieJar {

    private final Map<String, List<Cookie>> cookieStore = new HashMap<>();

    @Override
    public void saveFromResponse(HttpUrl url, @NonNull List<Cookie> cookies) {
        String key = url.host();
        List<Cookie> cookie = cookieStore.get(key);
        if (cookie == null) {
            ArrayList<Cookie> result = new ArrayList<Cookie>(cookies);
            cookieStore.put(key, result);
            return;
        }

        cookie.addAll(cookies);
    }

    @NonNull
    @Override
    public List<Cookie> loadForRequest(HttpUrl url) {
        String key = url.host();
        List<Cookie> cookies = cookieStore.get(key);
        return (cookies != null) ? cookies : new ArrayList<>();
    }
}