package com.example.user.webapp.wxapi.pay;

import android.content.Context;
import android.util.Log;

import com.example.user.webapp.Constants;
import com.tencent.mm.opensdk.modelpay.PayReq;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * Created by wjj on 2017/4/18.
 * 微信支付prepareId拼接，支付接口调用
 */
public class WXUtils {

    public static WXPayEntry parseWXData(String data){
        try {
            JSONTokener jsonTokener = new JSONTokener(data);
            JSONObject wxJson = (JSONObject)jsonTokener.nextValue();

            String sign = wxJson.getString("sign");
            String timestamp = wxJson.getString("timestamp");
            String pack = wxJson.getString("package");
            String partnerid = wxJson.getString("partnerid");
            String noncestr = wxJson.getString("noncestr");
            String appid = wxJson.getString("appid");
            String prepayid = wxJson.getString("prepayid");

            Constants.WX_APP_ID = appid;

            WXPayEntry entry = new WXPayEntry();
            entry.sign = sign;
            entry.timestamp = timestamp;
            entry.pack = pack;
            entry.partnerid = partnerid;
            entry.noncestr = noncestr;
            entry.appid = appid;
            entry.prepayid = prepayid;
            Log.i("WebAppActivity","request entry "+entry);

            return entry;

        } catch(JSONException e){

        }
        return null;
    }

    public static void startWeChat(Context context, WXPayEntry entry){
        Log.i("wjj","startWeChat");
        IWXAPI api = WXAPIFactory.createWXAPI(context, entry.appid);
        api.registerApp(entry.appid);

        PayReq payRequest = new PayReq();
        payRequest.appId = entry.appid;
        payRequest.partnerId = entry.partnerid;
        payRequest.prepayId = entry.prepayid;
        payRequest.packageValue = entry.pack;
        payRequest.nonceStr = entry.noncestr;
        payRequest.timeStamp = entry.timestamp;
        payRequest.sign = entry.sign;

        api.sendReq(payRequest);
    }

}
