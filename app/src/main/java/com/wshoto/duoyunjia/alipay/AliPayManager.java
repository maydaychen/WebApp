package com.wshoto.duoyunjia.alipay;

import android.app.Activity;
import android.util.Log;

import com.alipay.sdk.app.PayTask;
import com.wshoto.duoyunjia.R;

import org.greenrobot.eventbus.EventBus;

import java.util.Map;

/**
 * Created by Weshine on 2017/4/18.
 * 支付宝工具类
 */
public class AliPayManager {

    private static AliPayManager intance;

    protected AliPayManager(){}

    public static AliPayManager getInstance(){
        if(intance == null){
            intance = new AliPayManager();
        }
        return intance;
    }

    public void payV2(final Activity activity, String param){
        final String orderInfo = param;
        Runnable payRunnable = new Runnable() {

            @Override
            public void run() {
                PayTask alipay = new PayTask(activity);
                Log.i("wjj", "payV2 start");
                Map<String, String> result = alipay.payV2(orderInfo, true);
                Log.i("wjj", "payV2 " + result.toString());

                AliPayMessage msg = new AliPayMessage();
                if(result != null){
                    String resultStatus = result.get("resultStatus");
                    if("9000".equals(resultStatus)){
                        msg.errorCode = 1;
                        msg.result = activity.getString(R.string.alipay_result_success);
                        Log.i("wjj", "success");
                    }
                    else if("8000".equals(resultStatus)){
                        msg.errorCode = -2;
                        msg.result = activity.getString(R.string.alipay_result_delay);
                        Log.i("wjj", "delay");
                    }
                    else{
                        msg.errorCode = -1;
                        msg.result = activity.getString(R.string.alipay_result_fail);
                        Log.i("wjj", "fail");
                    }
                }
                //eventBus发送消息，准备通知服务器支付宝支付完成
                EventBus.getDefault().post(msg);
            }
        };

        Thread payThread = new Thread(payRunnable);
        payThread.start();
    }
}
