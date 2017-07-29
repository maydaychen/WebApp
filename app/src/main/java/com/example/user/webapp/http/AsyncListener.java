package com.example.user.webapp.http;

import org.json.JSONException;

import cz.msebera.android.httpclient.Header;

/**
 * Created by user on 2017/7/29.
 */

public interface AsyncListener {
    void onNext(int i, Header[] headers, byte[] bytes) throws JSONException;
}
