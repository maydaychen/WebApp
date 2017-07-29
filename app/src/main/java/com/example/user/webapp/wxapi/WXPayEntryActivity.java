package com.example.user.webapp.wxapi;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.example.user.webapp.Constants;
import com.example.user.webapp.wxapi.pay.WXPayMessage;
import com.tencent.mm.opensdk.constants.ConstantsAPI;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import org.greenrobot.eventbus.EventBus;

//import net.sourceforge.simcpux.Constants;
//import net.sourceforge.simcpux.R;

//import static com.baidu.location.h.j.R;

public class WXPayEntryActivity extends Activity implements IWXAPIEventHandler {

	private static final String TAG = "MicroMsg.SDKSample.WXPayEntryActivity";
	
    private IWXAPI api;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.pay_result);
        Log.i("wjj", "onCreate APP_ID " + Constants.WX_APP_ID);
    	api = WXAPIFactory.createWXAPI(this, Constants.WX_APP_ID);
        api.handleIntent(getIntent(), this);

		api.registerApp(Constants.WX_APP_ID);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
        api.handleIntent(intent, this);
	}

	@Override
	public void onReq(BaseReq req) {
	}

	@Override
	public void onResp(BaseResp resp) {
//		Log.d(TAG, "onPayFinish, errCode = " + resp.errCode);

//		if (resp.getType() == ConstantsAPI.COMMAND_PAY_BY_WX) {
//			AlertDialog.Builder builder = new AlertDialog.Builder(this);
//			builder.setTitle("提示");
////			builder.setMessage(getString(R.string.pay_result_callback_msg, String.valueOf(resp.errCode)));
//			builder.show();
//		}
		Log.d("wjj", "result：" + resp.errCode+ " resp.errStr() "+resp.errStr);
		if (resp.getType() == ConstantsAPI.COMMAND_PAY_BY_WX) {
//			if (resp.errCode == 0) {
//				Toast.makeText(this, "支付成功", Toast.LENGTH_SHORT).show();
//			} else {
//				Toast.makeText(this, "支付失败，请重试", Toast.LENGTH_SHORT).show();
//			}
			WXPayMessage msg = new WXPayMessage();
			msg.errorCode = resp.errCode;
			msg.errorStr = resp.errStr == null ? "" : resp.errStr;
			EventBus.getDefault().post(msg);
			finish();
		}
	}
}