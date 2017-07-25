package com.example.user.webapp;

import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.zhy.http.okhttp.callback.Callback;

import java.io.IOException;

/**
 * Created by user on 2017/7/12.
 */

public class MyCallback extends Callback{
    CallbackOnResponse mCallbackOnResponse;
    @Override
    public Object parseNetworkResponse(Response response) throws IOException {
        return null;
    }

    @Override
    public void onError(Request request, Exception e) {

    }

    @Override
    public void onResponse(Object response) {
        mCallbackOnResponse.onResponse(response);
    }
}
