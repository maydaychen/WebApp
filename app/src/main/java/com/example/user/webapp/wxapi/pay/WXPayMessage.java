package com.example.user.webapp.wxapi.pay;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by wjj on 2017/4/18.
 * EventBus微信支付事件信息
 */
public class WXPayMessage {
    public int errorCode = -1;
    public String errorStr;

    @Override
    public String toString() {
        return "PayMessage{" +
                "errorCode=" + errorCode +
                ", errorStr='" + errorStr + '\'' +
                '}';
    }

    public String getJsonString(){
        JSONObject payJson = new JSONObject();
        try {
            payJson.put("statusCode", errorCode+"");
            payJson.put("data", errorStr);
            return payJson.toString();
        } catch(JSONException e){
            return "{\"statusCode\":\"-1\", \"data\":\"\"}";
        }
    }
}
