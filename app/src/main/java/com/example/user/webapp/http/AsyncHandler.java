package com.example.user.webapp.http;

import android.content.Context;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONException;

import cz.msebera.android.httpclient.Header;

/**
 * Created by user on 2017/7/29.
 */

public class AsyncHandler extends AsyncHttpResponseHandler {
    private AsyncListener mAsyncListener;
    private Context context;

    public AsyncHandler(AsyncListener asyncListener, Context context) {
        this.mAsyncListener = asyncListener;
        this.context = context;
    }

    @Override
    public void onSuccess(int i, Header[] headers, byte[] bytes) {
        try {
            mAsyncListener.onNext(i,headers,bytes);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
        Toast.makeText(context, "网络异常，请检查网络状态", Toast.LENGTH_SHORT).show();
    }
}
