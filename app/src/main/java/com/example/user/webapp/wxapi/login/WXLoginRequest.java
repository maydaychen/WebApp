package com.example.user.webapp.wxapi.login;


import com.example.user.webapp.wxapi.login.response.AccessToken;
import com.example.user.webapp.wxapi.login.response.RefreshToken;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by FanXiaodan on 2017/3/11.
 */
public interface WXLoginRequest {
    @GET("sns/oauth2/access_token")
    Call<AccessToken> getAccessToken(@Query("appid") String appid, @Query("secret") String secret,
                                     @Query("code") String code, @Query("grant_type") String grant_type);


    @GET("sns/oauth2/refresh_token")
    Call<RefreshToken> getRefreshToken(@Query("appid") String appid, @Query("grant_type") String grant_type,
                                       @Query("refresh_token") String refresh_token);

    @GET("sns/userinfo")
    Call<ResponseBody> getUserInfo(@Query("access_token") String access_token, @Query("openid") String openid,
                                   @Query("lang") String lang);

}
