package com.wshoto.duoyunjia.http;

import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by user on 2017/7/13.
 */

public class HttpJsonMethod {
    public static final String BASE_URL = "https://api.duoyunjiav2.wshoto.com";
    private static final int DEFAULT_TIMEOUT = 5;

    private Retrofit retrofit;
    private BlueService movieService;

    private HttpJsonMethod() {
        //手动创建一个OkHttpClient并设置超时时间
        OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder();
        httpClientBuilder.connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS).build();


        retrofit = new Retrofit.Builder()
                .client(httpClientBuilder.build())
//                .addConverterFactory(GsonConverterFactory.create())
                .addConverterFactory(JsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .baseUrl(BASE_URL)
                .build();

        movieService = retrofit.create(BlueService.class);
    }

    private static class SingletonHolder {
        private static final HttpJsonMethod INSTANCE = new HttpJsonMethod();
    }

    //获取单例
    public static HttpJsonMethod getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private class HttpResultFunc<T> implements Func1<HttpResult<T>, T> {
        @Override
        public T call(HttpResult<T> httpResult) {
            return httpResult.getOthers();
        }
    }


    public void get_token(Subscriber<JSONObject> subscriber, String apiname, String apipass) {
        movieService.index_info(apiname, apipass)
//                .map(new HttpResultFunc<>())
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

    public void send_code(Subscriber<JSONObject> subscriber, String access_token, String mobile, String sign, int timestamp) {
        movieService.send_code(access_token, mobile, sign, timestamp)
//                .map(new HttpResultFunc<>())
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

    public void login(Subscriber<JSONObject> subscriber, String access_token, String device_tokens, String kapkey, String mobile, String sign, int timestamp) {
        movieService.login(access_token, device_tokens, kapkey, mobile, sign, timestamp)
//                .map(new HttpResultFunc<>())
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

    public void change_token(Subscriber<JSONObject> subscriber, String acc, String access_token, String sign, int timestamp) {
        movieService.change_token(acc, access_token, sign, timestamp)
//                .map(new HttpResultFunc<>())
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

    public void dalete_token(Subscriber<JSONObject> subscriber, String acc, String access_token, String sign, int timestamp) {
        movieService.dalete_token(acc, access_token, sign, timestamp)
//                .map(new HttpResultFunc<>())
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

    public void getAva(Subscriber<JSONObject> subscriber, String access_token, String avatar, String session, String sign, int timestamp) {
        movieService.getAva(access_token, avatar, session, sign, timestamp)
//                .map(new HttpResultFunc<>())
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

}
