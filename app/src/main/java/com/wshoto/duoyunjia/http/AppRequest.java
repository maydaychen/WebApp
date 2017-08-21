package com.wshoto.duoyunjia.http;


import org.json.JSONObject;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

/**
 * Created by Weshine on 2017/5/8.
 */

public interface AppRequest {
    @Headers("addons: ewei_shop")
    @POST("/uploads")
    Call<JSONObject> getAva(@Body RequestBody updatedBody);
 /*   @Multipart
    @POST(HttpConstants.PHOTO_URL)
    Call<BaseBean> postPhoto(@PartMap Map<String, RequestBody> params);*/
}
