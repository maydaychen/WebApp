package com.example.user.webapp.http;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.example.user.webapp.Utils;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 作者：JTR on 2016/9/2 12:50
 * 邮箱：2091320109@qq.com
 */
public class Http {

    private static Http http = null;
    private final static String TAG = "测试";
    private static final String MSG = "{\"success\":\"F\",\"msg\":\"网络连接错误\",\"data\":\"\"}";
    private static final String MSG1 = "{\"success\":\"F\",\"msg\":\"网络连接失败\",\"data\":\"\"}";

    public static Http getInstance() {
        // if (http == null) {
        http = new Http();
        // }
        return http;
    }

    OkHttpClient okHttpClient;
    CacheControl my_cache;
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private String map;
    private String url;
    private Handler handler;
    private Context context;
    private int what;
    Interceptor interceptor = null;

    public Http init(Context context, Handler handle, String map, String url, int what) {
        interceptor = intiIntercepter(context);
        CacheControl.Builder builder =
                new CacheControl.Builder().
                        maxAge(6, TimeUnit.SECONDS).//这个是控制缓存的最大生命时间
                        maxStale(6, TimeUnit.SECONDS);//这个是控制缓存的过时时间

        my_cache = builder.build();

        this.map = map;
        this.url = url;
        this.handler = handle;
        this.context = context.getApplicationContext();
        this.what = what;
        return http;
    }


    public void sendMsg() {
        //设置到sd卡里面
//        File cacheDirectory = new File(context.getExternalCacheDir(), "mall");
//        Log.i(TAG, "cacheDirectory == " + cacheDirectory.getAbsolutePath());
//        Cache cache = new Cache(cacheDirectory, 40 * 1024 * 1024);

//        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
//        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)//请求超时时间
                .readTimeout(30, TimeUnit.SECONDS)
                .cache(new Cache(new File(context.getExternalCacheDir(), "mall"),
                        10 * 1024 * 1024))//设置缓存
                .addInterceptor(interceptor)
                .addNetworkInterceptor(interceptor)
                //.addInterceptor(httpLoggingInterceptor)
                .build();
        RequestBody requestBody;
        if (map == null) {
            requestBody = RequestBody.create(JSON, "");
        } else {
            requestBody = RequestBody.create(JSON, map);
        }

        Request request = new Request.Builder()
                .url(url)
                .method("POST", requestBody)
                .cacheControl(my_cache)
                .build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
//                String error = e.toString();
                handler.sendMessage(handler.obtainMessage(what, MSG1));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String data = response.body().string();
                if (response.isSuccessful()) {
                    handler.sendMessage(handler.obtainMessage(what, data));
                } else {
                    handler.sendMessage(handler.obtainMessage(what, MSG));
                }
            }
        });

    }


    public Interceptor intiIntercepter(final Context context) {

        return new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request request = chain.request();

                if (Utils.isNetworkAvailable(context)) {
                    Response response = chain.proceed(request);
                    int maxAge = 60; // 在线缓存在1分钟内可读取
                    String cacheControl = request.cacheControl().toString();
                    Log.d(TAG, "在线缓存在1分钟内可读取" + cacheControl);
                    return response.newBuilder()
                            .removeHeader("Pragma")
                            .removeHeader("Cache-Control")
                            .header("Cache-Control", "public, max-age=" + maxAge)
                            .build();
                } else {
                    request = request.newBuilder()
                            .cacheControl(CacheControl.FORCE_CACHE)//或者直接用系统的
                            .build();

                    Response response = chain.proceed(request);
                    return response.newBuilder()
                            .removeHeader("Pragma")
                            .removeHeader("Cache-Control")
                            .header("Cache-Control", "public, only-if-cached, max-stale=50")
                            .build();
                }
            }
        };

    }


}
