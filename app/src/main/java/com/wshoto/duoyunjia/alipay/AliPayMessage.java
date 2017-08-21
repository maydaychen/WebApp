package com.wshoto.duoyunjia.alipay;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Weshine on 2017/4/18.
 * 支付宝EventBus信息类
 */
public class AliPayMessage {
    public int errorCode = -1;
    public String result;

    @Override
    public String toString() {
        return "PayMessage{" +
                "errorCode=" + errorCode +
                ", errorStr='" + result + '\'' +
                '}';
    }

    public String getJsonString(){
        JSONObject payJson = new JSONObject();
        try {
            payJson.put("statusCode", errorCode+"");
            payJson.put("data", "支付成功");
            return payJson.toString();
        } catch(JSONException e){
            return "{\"statusCode\":\"-1\", \"data\":\"\"}";
        }
    }
}
