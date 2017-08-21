package com.wshoto.duoyunjia.wxapi.pay;

/**
 * Created by wjj on 2017/4/18.
 */
public class WXPayEntry {

    public String sign;
    public String timestamp;
    public String pack;
    public String partnerid;
    public String noncestr;
    public String appid;
    public String prepayid;

    @Override
    public String toString() {
        return "WXPayEntry{" +
                "sign='" + sign + '\'' +
                ", timestamp='" + timestamp + '\'' +
                ", pack='" + pack + '\'' +
                ", partnerid='" + partnerid + '\'' +
                ", noncestr='" + noncestr + '\'' +
                ", appid='" + appid + '\'' +
                ", prepayid='" + prepayid + '\'' +
                '}';
    }
}
