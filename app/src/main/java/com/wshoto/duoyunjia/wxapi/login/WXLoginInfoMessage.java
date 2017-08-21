package com.wshoto.duoyunjia.wxapi.login;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by FanXiaodan on 2017/3/11.
 */
public class WXLoginInfoMessage {
    public int status = -1;
    public String data;

    @Override
    public String toString() {
        return "PayMessage{" +
                "errorCode=" + status +
                ", errorStr='" + data + '\'' +
                '}';
    }

    public String getJsonString(){
        JSONObject payJson = new JSONObject();
        try {
            payJson.put("statusCode", status+"");
            payJson.put("data", data);
            return payJson.toString();
        } catch(JSONException e){
            return "{\"statusCode\":\"-1\", \"data\":\"\"}";
        }
    }
}
