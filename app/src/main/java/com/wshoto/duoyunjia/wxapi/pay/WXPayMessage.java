package com.wshoto.duoyunjia.wxapi.pay;

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

    public String getJsonString() {
        JSONObject payJson = new JSONObject();
        try {
            if (errorCode == 0) {
//                payJson.put("statusCode", "1");
                return "{\"statusCode\":\"1\", \"data\":\"支付成功\"}";
            } else {
                payJson.put("statusCode", errorCode + "");
                return "{\"statusCode\":\"-1\", \"data\":\"支付失败\"}";
//                payJson.put("statusCode", errorCode + "");
            }
//            payJson.put("data", errorStr);
//            return payJson.toString();
//            return "{\"statusCode\":\"1\", \"data\":\"支付成功\"}";
        } catch (JSONException e) {
            return "{\"statusCode\":\"-1\", \"data\":\"\"}";
        }
    }
}
