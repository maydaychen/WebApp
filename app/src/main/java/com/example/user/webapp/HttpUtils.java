package com.example.user.webapp;

import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.Callback;

/**
 * Created by user on 2017/7/12.
 */

public class HttpUtils {
    public static void get(String url, Callback callback){
        OkHttpUtils.get()
                .url(url)
                .addParams("username", "hyman")
                .addParams("password", "123")
                .build()
                .execute(callback);
    }

    public static void gost(String url, Callback callback){
        OkHttpUtils.post()
                .url(url)
                .addParams("username", "hyman")
                .addParams("password", "123")
                .build()
                .execute(callback);
    }

}
