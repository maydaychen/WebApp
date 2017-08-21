package com.wshoto.duoyunjia.http;

import android.content.Context;
import android.util.Log;

import org.json.JSONException;

import rx.Subscriber;

/**
 * 作者：JTR on 2016/11/25 14:54
 * 邮箱：2091320109@qq.com
 */
public class ProgressSubscriber<T> extends Subscriber<T> {

    private SubscriberOnNextListener mSubscriberOnNextListener;
    private Context context;

    public ProgressSubscriber(SubscriberOnNextListener mSubscriberOnNextListener, Context context) {
        this.mSubscriberOnNextListener = mSubscriberOnNextListener;
        this.context = context;
    }

    @Override
    public void onStart() {
    }

    @Override
    public void onCompleted() {
//        Toast.makeText(context, "Get Top Movie Completed", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onError(Throwable e) {
//        Toast.makeText(context, "err" + e.getMessage(), Toast.LENGTH_SHORT).show();
        Log.e("======================>", e.getMessage().toString());
    }

    @Override
    public void onNext(T t) {
        try {
            mSubscriberOnNextListener.onNext(t);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}