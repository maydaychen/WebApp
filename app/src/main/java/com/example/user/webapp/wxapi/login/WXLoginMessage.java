package com.example.user.webapp.wxapi.login;

/**
 * Created by FanXiaodan on 2017/3/11.
 */
public class WXLoginMessage {
    public String loginCode = "-1";
    public String errorStr;

    @Override
    public String toString() {
        return "PayMessage{" +
                "errorCode=" + loginCode +
                ", errorStr='" + errorStr + '\'' +
                '}';
    }
}
