package com.wshoto.duoyunjia.bean;

/**
 * Created by user on 2017/7/28.
 */

public class LoginBean {
    /**
     * statusCode : 1
     * result : {"sessionkey":"5a42277cbd1680e446e6e07ee78be19f","timestamp":1500997527}
     */

    private int statusCode;
    private ResultBean result;

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public ResultBean getResult() {
        return result;
    }

    public void setResult(ResultBean result) {
        this.result = result;
    }

    public static class ResultBean {
        /**
         * sessionkey : 5a42277cbd1680e446e6e07ee78be19f
         * timestamp : 1500997527
         */

        private String sessionkey;
        private int timestamp;

        public String getSessionkey() {
            return sessionkey;
        }

        public void setSessionkey(String sessionkey) {
            this.sessionkey = sessionkey;
        }

        public int getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(int timestamp) {
            this.timestamp = timestamp;
        }
    }
}
