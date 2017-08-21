package com.wshoto.duoyunjia.bean;

/**
 * Created by user on 2017/7/27.
 */

public class AccessTokenBean {
    /**
     * statusCode : 1
     * result : {"accountid":"1","access_token":"78dddb9fe91d6ac654af8c4abd9fb036","auth_key":"MJJB614J","timestamp":1500922207}
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
         * accountid : 1
         * access_token : 78dddb9fe91d6ac654af8c4abd9fb036
         * auth_key : MJJB614J
         * timestamp : 1500922207
         */

        private String accountid;
        private String access_token;
        private String auth_key;
        private int timestamp;

        public String getAccountid() {
            return accountid;
        }

        public void setAccountid(String accountid) {
            this.accountid = accountid;
        }

        public String getAccess_token() {
            return access_token;
        }

        public void setAccess_token(String access_token) {
            this.access_token = access_token;
        }

        public String getAuth_key() {
            return auth_key;
        }

        public void setAuth_key(String auth_key) {
            this.auth_key = auth_key;
        }

        public int getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(int timestamp) {
            this.timestamp = timestamp;
        }
    }
}
