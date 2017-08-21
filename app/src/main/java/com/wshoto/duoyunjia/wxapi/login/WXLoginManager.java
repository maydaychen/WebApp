package com.wshoto.duoyunjia.wxapi.login;

import android.content.Context;
import android.util.Log;

import com.wshoto.duoyunjia.Constants;
import com.wshoto.duoyunjia.wxapi.login.response.AccessToken;
import com.wshoto.duoyunjia.wxapi.login.response.RefreshToken;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by FanXiaodan on 2017/3/11.
 */
public class WXLoginManager {

    private IWXAPI api;
    private WXLoginRequest request;

    private static WXLoginManager instance;

    public static WXLoginManager getInstance(Context context){
        if(instance == null){
            instance = new WXLoginManager(context);
        }
        return instance;
    }

    protected WXLoginManager(Context context) {
        Log.d("wjj","WXLoginManager");
        api = WXAPIFactory.createWXAPI(context, Constants.WX_APP_ID, true);
        api.registerApp( Constants.WX_APP_ID);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.weixin.qq.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        request = retrofit.create(WXLoginRequest.class);
    }

    public void login(){
        Log.i("WXLoginManager","login");
        SendAuth.Req req = new SendAuth.Req();
        req.scope = "snsapi_userinfo";
        req.state = "wesnsapi_userinfochat_sdk_wshoto_webapp";
        boolean temp = api.sendReq(req);
        Log.d("wjj","api == "+temp);
    }

    public AccessToken requestAccessToken(String code) throws IOException {
        Call<AccessToken> call = request.getAccessToken(Constants.WX_APP_ID, Constants.WX_SECRET, code, "authorization_code");
        return call.execute().body();
    }

    public String requestUserInfo(String accessToken, String openid) throws Exception {
        Call<ResponseBody> call = request.getUserInfo(accessToken, openid, "zh_CN");
        return call.execute().body().string();
    }

    public RefreshToken requestRefreshToken(String refreshToken) throws IOException {
        Call<RefreshToken> call = request.getRefreshToken(Constants.WX_APP_ID, "refresh_token", refreshToken);
        return call.execute().body();
    }

}
