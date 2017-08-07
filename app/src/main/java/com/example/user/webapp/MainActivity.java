package com.example.user.webapp;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.user.webapp.alipay.AliPayManager;
import com.example.user.webapp.alipay.AliPayMessage;
import com.example.user.webapp.bean.AccessTokenBean;
import com.example.user.webapp.bean.LoginBean;
import com.example.user.webapp.http.AsyncHandler;
import com.example.user.webapp.http.AsyncListener;
import com.example.user.webapp.http.HttpJsonMethod;
import com.example.user.webapp.http.ProgressErrorSubscriber;
import com.example.user.webapp.http.ProgressSubscriber;
import com.example.user.webapp.http.SubscriberOnNextAndErrorListener;
import com.example.user.webapp.http.SubscriberOnNextListener;
import com.example.user.webapp.wxapi.pay.WXPayEntry;
import com.example.user.webapp.wxapi.pay.WXPayMessage;
import com.example.user.webapp.wxapi.pay.WXUtils;
import com.github.lzyzsd.jsbridge.BridgeWebView;
import com.github.lzyzsd.jsbridge.BridgeWebViewClient;
import com.github.lzyzsd.jsbridge.DefaultHandler;
import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.umeng.message.PushAgent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import cz.msebera.android.httpclient.Header;

import static com.example.user.webapp.Config.BASEURL4;
import static com.example.user.webapp.Utils.bitmaptoString;
import static com.example.user.webapp.Utils.md5;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.wv_main)
    BridgeWebView webView;
    @BindView(R.id.bt_refresh)
    Button btn;
    @BindView(R.id.rl_gone)
    RelativeLayout rl_gone;
    @BindView(R.id.rl_code)
    RelativeLayout mRlCode;

    private String web_url;
    private boolean IS_OK = true;
    private boolean NETWORK_OK = true;
    private IntenterBoradCastReceiver receiver;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private String final_string;
    private AsyncListener mAsyncListener;
    private SubscriberOnNextListener<JSONObject> changeOnNext;
    private SubscriberOnNextListener<JSONObject> logoutOnNext;
    private SubscriberOnNextListener<JSONObject> getTokenOnNext;
    private SubscriberOnNextAndErrorListener<JSONObject> uploadOnNext;
    final public static int REQUEST_CODE_ASK_CALL_PHONE = 123;
    final public static int REQUEST_WRITE = 222;
    final public static int REQUEST_SAVE_WRITE = 333;
    private File picFile;
    private Loading_view loading;
    private final static String RESPONSE_TEXT_SUCCESS = "{\"statusCode\":\"1\", \"data\":\"\"}";
    private final static String RESPONSE_TEXT_FAIL = "{\"statusCode\":\"-1\", \"data\":\"\"}";
    private Gson gson = new Gson();
    private boolean IS_SHOWING = false;
    private Button cancel;
    private View inflate;
    private Button choosePhoto;
    private ProgressDialog mSaveDialog = null;
    private ProgressDialog updateDialog = null;
    private String mSaveMessage;
    private String mUrl;
    private Bitmap bmp;
    private Boolean IS_ALI = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        if (Build.VERSION.SDK_INT >= 21) {
            StatusBarCompat.compat(this, ContextCompat.getColor(this, R.color.line_cc));
        }
        EventBus.getDefault().register(this);
        getSupportActionBar().hide();
        registerBroadrecevicer();
        PushAgent pushAgent = PushAgent.getInstance(this);
        pushAgent.onAppStart();
        loading = new Loading_view(this, R.style.CustomDialog);
        preferences = getSharedPreferences("user", Context.MODE_PRIVATE);
        editor = preferences.edit();
        btn.setOnClickListener(v -> {
            webView.loadUrl(web_url);
            IS_OK = true;
        });
        getTokenOnNext = resultBean -> {
            switch (resultBean.getInt("statusCode")) {
                case 1:
                    AccessTokenBean indexBean = gson.fromJson(resultBean.toString(), AccessTokenBean.class);
                    editor.putString("access_token", indexBean.getResult().getAccess_token());
                    editor.putString("auth_key", indexBean.getResult().getAuth_key());
                    editor.putInt("access_time", indexBean.getResult().getTimestamp());
                    editor.apply();
                    break;
                case 10003:
                    Toast.makeText(this, "服务器错误，请稍后再试...", Toast.LENGTH_SHORT).show();
                    break;
            }
        };

        changeOnNext = resultBean -> {
            if (resultBean.getInt("statusCode") == 1) {
                Toast.makeText(this, "修改成功！", Toast.LENGTH_SHORT).show();
                LoginBean indexBean = gson.fromJson(resultBean.toString(), LoginBean.class);
                editor.putString("sessionkey", indexBean.getResult().getSessionkey());
                editor.putInt("session_time", indexBean.getResult().getTimestamp());
                editor.apply();
            }
        };
        logoutOnNext = resultBean -> {
            Toast.makeText(this, "退出成功！", Toast.LENGTH_SHORT).show();
            editor.putString("sessionkey", "");
            editor.putBoolean("autoLog", false);
            if (editor.commit()) {
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
            }
        };
        uploadOnNext = new SubscriberOnNextAndErrorListener<JSONObject>() {
            @Override
            public void onNext(JSONObject jsonObject) {
                if (updateDialog != null) {
                    updateDialog.dismiss();
                    updateDialog = null;
                }
                String msg = jsonObject.toString();
                try {
                    if (jsonObject.getInt("statusCode") == 1) {
                        Toast.makeText(MainActivity.this, "上传成功！", Toast.LENGTH_SHORT).show();
                        msg = jsonObject.getString("result");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                deletePic();
                Log.d("wjj", msg);
                getResult(msg);
            }

            @Override
            public void onError(Throwable e) {
                if (updateDialog == null) {
                    updateDialog.dismiss();
                }
                Toast.makeText(MainActivity.this, "上传失败，请稍后再试", Toast.LENGTH_SHORT).show();
                Log.d("wjj", "err");
                deletePic();
                e.printStackTrace();
            }
        };

        init();
        webView.loadUrl(BASEURL4);
//        AliPayManager.getInstance().payV2(MainActivity.this, params);

        mAsyncListener = (i, headers, bytes) -> callResult(bytes);
//        webView.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View view) {
//                return false;
//            }
//        });
    }

    private void init() {
        WebSettings webSettings = webView.getSettings();
        webSettings.setUseWideViewPort(true);//设置此属性，可任意比例缩放
        webSettings.setJavaScriptEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        //add by wjj
        //cache mode
        //设置缓存模式
//        if (isNetworkAvailable(MainActivity.this)) {
//            webView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
//        } else {
//            webView.getSettings().setCacheMode(
//                    WebSettings.LOAD_CACHE_ELSE_NETWORK);
//        }
        webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        //open dom storage
        webSettings.setDomStorageEnabled(true);
        //priority high
        webSettings.setRenderPriority(WebSettings.RenderPriority.HIGH);
        webSettings.setAppCacheEnabled(true);
        webSettings.setDatabaseEnabled(true);
        //add by wjj end
        String ua = webSettings.getUserAgentString();
        webSettings.setUserAgentString(ua + ";wshoto");
        webView.setDefaultHandler(new DefaultHandler());
        webView.setWebViewClient(new BridgeWebViewClient(webView) {

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                long a = (System.currentTimeMillis() / 1000);
                long b = (long) preferences.getInt("session_time", 0);
                long c = (long) preferences.getInt("access_time", 0);
                if (a > ((a + b) / 2)) {
                    refreshSession();
                }
                if (a > ((a + c) / 2)) {
                    refreshAccess();
                }
                Boolean test = a > b || a > c;
                if (test) {
                    logout(MainActivity.this);
                }
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                if (IS_OK) {
                    rl_gone.setVisibility(View.INVISIBLE);
                }
                super.onPageFinished(view, url);
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                if (errorCode == -2) {
                    IS_OK = false;
                    web_url = failingUrl;
                    Toast.makeText(MainActivity.this, "无网络", Toast.LENGTH_SHORT).show();
                    rl_gone.setVisibility(View.VISIBLE);
                    NETWORK_OK = false;
                }
//                super.onReceivedError(view, errorCode, description, failingUrl);
            }
        });

        webView.setOnLongClickListener(view -> true);

        webView.registerHandler("requestx", (responseData, function) -> net(responseData));
        webView.registerHandler("uploadImg", (responseData, function) -> {
            try {
                showType();
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
//            try {
//               /* JSONTokener jsonTokener = new JSONTokener(responseData);
//                JSONObject wxJson = (JSONObject) jsonTokener.nextValue();*/
//                ActionSheet.createBuilder(MainActivity.this, getSupportFragmentManager())
//                        .setCancelButtonTitle("Cancel")
//                        .setOtherButtonTitles("相机", "图库")
//                        .setCancelableOnTouchOutside(true)
//                        .setListener(new ActionSheet.ActionSheetListener() {
//                            @Override
//                            public void onDismiss(ActionSheet actionSheet, boolean isCancel) {
//                            }
//
//                            @Override
//                            public void onOtherButtonClick(ActionSheet actionSheet, int index) {
//                                if (index == 0) {
//                                    camera();
//                                    Log.d("wjj", "camera");
//                                } else {
//                                    selectImage();
//                                    Log.d("wjj", "selectImage");
//                                }
//                            }
//                        }).show();
//
//                //Toast.makeText(WebAppActivity.this,"uploadImg",Toast.LENGTH_SHORT).show();
////                    function.onCallBack(RESPONSE_TEXT_SUCCESS);
//            } catch (Exception e) {
//                e.printStackTrace();
////                    function.onCallBack(RESPONSE_TEXT_FAIL);
//            }
        });
        webView.registerHandler("shellQrcode", (responseData, function) -> {
            try {
                show(responseData);
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
//            try {
//               /* JSONTokener jsonTokener = new JSONTokener(responseData);
//                JSONObject wxJson = (JSONObject) jsonTokener.nextValue();*/
//                ActionSheet.createBuilder(MainActivity.this, getSupportFragmentManager())
//                        .setCancelButtonTitle("取消")
//                        .setOtherButtonTitles("保存到本地")
//                        .setCancelableOnTouchOutside(true)
//                        .setListener(new ActionSheet.ActionSheetListener() {
//                            @Override
//                            public void onDismiss(ActionSheet actionSheet, boolean isCancel) {
//                            }
//
//                            @Override
//                            public void onOtherButtonClick(ActionSheet actionSheet, int index) {
//                                savePicture(getHttpBitmap(responseData));
//                            }
//                        }).show();
//
//                //Toast.makeText(WebAppActivity.this,"uploadImg",Toast.LENGTH_SHORT).show();
////                    function.onCallBack(RESPONSE_TEXT_SUCCESS);
//            } catch (Exception e) {
//                e.printStackTrace();
////                    function.onCallBack(RESPONSE_TEXT_FAIL);
//            }
        });
        webView.registerHandler("logOut", (responseData, function) -> {
            int time = (int) (System.currentTimeMillis() / 1000);
            String sign = "";
            sign = sign + "sessionkey=" + preferences.getString("sessionkey", "") + "&";
            sign = sign + "timestamp=" + time + "&";
            sign = sign + "key=" + preferences.getString("auth_key", "");
            sign = md5(sign);
            HttpJsonMethod.getInstance().dalete_token(
                    new ProgressSubscriber(logoutOnNext, MainActivity.this), preferences.getString("access_token", ""),
                    preferences.getString("sessionkey", ""), sign, time);
        });
        webView.registerHandler("payment", (responseData, function) -> {
            try {
                JSONTokener jsonTokener = new JSONTokener(responseData);
                JSONObject wxJson = (JSONObject) jsonTokener.nextValue();
                Log.d("wjj", "payment1");
                String type = wxJson.getString("type");
                String params = wxJson.getString("params");
                Log.d("wjj", params);
                if ("wechat_app".equals(type)) {
                    Log.d("wjj", "payment2");
                    WXPayEntry entry = WXUtils.parseWXData(params);
                    if (entry != null) {
                        Log.d("wjj", "payment3");
                        WXUtils.startWeChat(MainActivity.this, entry);
                        function.onCallBack(RESPONSE_TEXT_SUCCESS);
                    } else {
                        function.onCallBack(RESPONSE_TEXT_FAIL);
                    }
                } else if ("alipay_app".equals(type)) {
                    AliPayManager.getInstance().payV2(MainActivity.this, params);
                    function.onCallBack(RESPONSE_TEXT_SUCCESS);
                }
            } catch (JSONException e) {
                function.onCallBack(RESPONSE_TEXT_FAIL);
            }
        });
    }

    public void show(String url) throws JSONException, MalformedURLException {
        Dialog dialog = new Dialog(this, R.style.BottomDialog);
        inflate = LayoutInflater.from(this).inflate(R.layout.pop_pic, null);
        choosePhoto = inflate.findViewById(R.id.takePhoto);
        cancel = inflate.findViewById(R.id.btn_cancel);
        mUrl = url;
        choosePhoto.setOnClickListener(v -> {
            dialog.dismiss();
            mSaveDialog = ProgressDialog.show(MainActivity.this, "保存图片", "图片正在保存中，请稍等...", true);
            if (Build.VERSION.SDK_INT >= 23) {
                int checkCallPhonePermission = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
                if (checkCallPhonePermission != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_SAVE_WRITE);
                    return;
                } else {
                    //上面已经写好的拍照方法
                    new Thread(saveFileRunnable).start();
                }
            } else {
                //上面已经写好的拍照方法
                new Thread(saveFileRunnable).start();
            }
        });
        cancel.setOnClickListener(v -> dialog.dismiss());
        dialog.setContentView(inflate);
        Window dialogWindow = dialog.getWindow();
        dialogWindow.setGravity(Gravity.BOTTOM);
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
//        lp.y = 20;
        lp.width = -1;
        dialogWindow.setAttributes(lp);
        dialog.show();
    }

    public void showType() throws JSONException, MalformedURLException {
        Dialog dialog = new Dialog(this, R.style.BottomDialog);
        inflate = LayoutInflater.from(this).inflate(R.layout.pop_avatar, null);
        Button camera = inflate.findViewById(R.id.camera);
        Button img_lib = inflate.findViewById(R.id.img_lib);
        Button cancel = inflate.findViewById(R.id.btn_cancel);
        camera.setOnClickListener(v -> {
            dialog.dismiss();
            camera();
        });
        img_lib.setOnClickListener(v -> {
            selectImage();
            dialog.dismiss();
        });
        cancel.setOnClickListener(v -> dialog.dismiss());
        dialog.setContentView(inflate);
        Window dialogWindow = dialog.getWindow();
        dialogWindow.setGravity(Gravity.BOTTOM);
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
//        lp.y = 20;
        lp.width = -1;
        dialogWindow.setAttributes(lp);
        dialog.show();
    }

    private Runnable saveFileRunnable = new Runnable() {
        @Override
        public void run() {
            savePicture(getHttpBitmap(mUrl));
            mSaveMessage = "图片保存成功！";
            messageHandler.sendMessage(messageHandler.obtainMessage());
        }

    };
    private Handler messageHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            mSaveDialog.dismiss();
            Toast.makeText(MainActivity.this, mSaveMessage, Toast.LENGTH_SHORT).show();
        }
    };

    public Bitmap getHttpBitmap(String url) {
        Bitmap bitmap = null;
        try {
            JSONObject jsonObject = new JSONObject(url);
            url = jsonObject.getString("params");
            URL pictureUrl = new URL(url);
            InputStream in = pictureUrl.openStream();
            bitmap = BitmapFactory.decodeStream(in);
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    public void savePicture(Bitmap bitmap) {
        String pictureName = Environment.getExternalStorageDirectory() + "/" + System.currentTimeMillis() + ".jpg";
        File file = new File(pictureName);
        FileOutputStream out;
        try {
            out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public class IntenterBoradCastReceiver extends BroadcastReceiver {
        private ConnectivityManager mConnectivityManager;
        private NetworkInfo netInfo;

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                mConnectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                netInfo = mConnectivityManager.getActiveNetworkInfo();
                if (!NETWORK_OK && netInfo.isAvailable()) {
                    webView.loadUrl(web_url);
                    IS_OK = true;
                }
            }
        }
    }

    private void registerBroadrecevicer() {
        //获取广播对象
        receiver = new IntenterBoradCastReceiver();
        //创建意图过滤器
        IntentFilter filter = new IntentFilter();
        //添加动作，监听网络
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(receiver, filter);
    }

    public static void logout(Activity context) {
        new AlertDialog.Builder(context)
                .setTitle("警告")
                .setCancelable(false)
                .setMessage("账号验证失效，请重新登录！")
                .setPositiveButton("确定", (dialog, which) -> {
                    SharedPreferences mySharedPreferences = context.getSharedPreferences("user",
                            Activity.MODE_PRIVATE);
                    SharedPreferences.Editor editor = mySharedPreferences.edit();
                    editor.putBoolean("autoLog", false);
                    if (editor.commit()) {
                        context.finish();
                        Intent intent = new Intent(context, LoginActivity.class);
                        context.startActivity(intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
                    }
                })
                .show();
    }

    //EventBus阿里支付结果回调事件
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMoonEvent(AliPayMessage payMessage) {
        String payString = payMessage.getJsonString();
        Log.i("wjj", "onMoonEvent AliPayMessage " + payString);

        webView.callHandler("payment", payString, data -> {
            Log.i("wjj", "callHandler AliPayMessage result " + data);
        });
        //webview.send(payString);
    }

    //EventBus微信支付结果回调事件
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMoonEvent(WXPayMessage payMessage) {
        String payString = payMessage.getJsonString();
        Log.i("wjj", "onMoonEvent WXPayMessage " + payString);
        //java调用js，通知服务端支付完成
        webView.callHandler("payment", payString, jsResponseData -> Log.i("wjj", "callHandler WXPayMessage result " + jsResponseData));
    }

    private void net(String responseData) {
        Log.i("chenyi", "net=========>: " + responseData);
        if (!IS_SHOWING) {
            loading.show();
            IS_SHOWING = true;
        }
        try {
            JSONObject jsonObject = new JSONObject(responseData);
            Map<String, String> test = new TreeMap<>();
            RequestParams params = new RequestParams();
            JSONObject a = jsonObject.getJSONObject("params").optJSONObject("data");
            if (null != a) {
                Iterator it = a.keys();
                while (it.hasNext()) {
                    String key = String.valueOf(it.next());
                    String value = a.get(key) + "";
                    if (value != null && !value.equals("")) {
                        test.put(key, value);
                    }
                    params.add(key, value);
                }
            }
            AsyncHttpClient client = new AsyncHttpClient();
            client.addHeader("Content-Type", "application/x-www-form-urlencoded");
            client.addHeader("addons", "ewei_shop");
            client.setConnectTimeout(5000);
            client.setResponseTimeout(5000);
            String URL = jsonObject.getJSONObject("params").getString("url");
            int time = (int) (System.currentTimeMillis() / 1000);
            String sign = "";
            test.put("sessionkey", preferences.getString("sessionkey", ""));
            test.put("timestamp", time + "");
            Set<String> keySet = test.keySet();
            Iterator<String> iter = keySet.iterator();
            while (iter.hasNext()) {
                String key = iter.next();
                System.out.println(key + ":" + test.get(key));
                sign = sign + key + "=" + test.get(key) + "&";
            }
//            sign = sign + "sessionkey=" + preferences.getString("sessionkey", "") + "&";
//            sign = sign + "timestamp=" + time + "&";
            sign = sign + "key=" + preferences.getString("auth_key", "");
            sign = md5(sign);
            // 遍历jsonObject数据，添加到Map对象

            params.add("access_token", preferences.getString("access_token", ""));
            params.add("sessionkey", preferences.getString("sessionkey", ""));
            params.add("sign", sign);
            params.add("timestamp", time + "");
            switch (jsonObject.getJSONObject("params").getString("method")) {
                case "GET":
                    client.get(URL, params, new AsyncHandler(mAsyncListener, MainActivity.this));
                    break;
                case "POST":
                    if (URL.contains("orders/payment")) {
                        if (a.getString("type").equals("alipay_app")) {
                            IS_ALI = true;
                        } else {
                            IS_ALI = false;
                        }
                        client.post(this, URL, params, new AsyncHttpResponseHandler() {
                            @Override
                            public void onSuccess(int i, Header[] headers, byte[] bytes) {
                                if (IS_SHOWING) {
                                    new Handler().postDelayed(() -> {
            /* Create an Intent that will start the Main WordPress Activity. */
                                        loading.dismiss();
                                        IS_SHOWING = false;
                                    }, 500);
                                }
                                String pay = new String(bytes);
                                try {
                                    JSONObject jsonObject1 = new JSONObject(pay);
                                    String result = jsonObject1.getString("result");
                                    if (IS_ALI) {
                                        AliPayManager.getInstance().payV2(MainActivity.this, result);
                                    }else {
                                        WXPayEntry entry = WXUtils.parseWXData(result);
                                        WXUtils.startWeChat(MainActivity.this, entry);
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
                                if (IS_SHOWING) {
                                    new Handler().postDelayed(() -> {
            /* Create an Intent that will start the Main WordPress Activity. */
                                        loading.dismiss();
                                        IS_SHOWING = false;
                                    }, 500);
                                }

                            }
                        });
                    } else {
                        client.post(URL, params, new AsyncHandler(mAsyncListener, MainActivity.this));
                    }
                    break;
                case "PUT":
                    client.put(URL, params, new AsyncHandler(mAsyncListener, MainActivity.this));
                    break;
                case "PATCH":
                    client.patch(URL, params, new AsyncHandler(mAsyncListener, MainActivity.this));
                    break;
                case "DELETE":
                    client.delete(URL, params, new AsyncHandler(mAsyncListener, MainActivity.this));
                    break;
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void callResult(byte[] bytes) {
        if (IS_SHOWING) {
            new Handler().postDelayed(() -> {
            /* Create an Intent that will start the Main WordPress Activity. */
                loading.dismiss();
                IS_SHOWING = false;
            }, 500);
        }
        String result = new String(bytes);
        Log.i("chenyi", "callResult: ======>" + result);
        try {
            JSONObject object = new JSONObject(result);
            JSONObject newJson = new JSONObject();
            newJson.put("statusCode", object.getInt("statusCode"));
            newJson.put("data", object.get("result"));
            final_string = newJson.toString();
//                    JSONObject jsonObject = new JSONObject();
//                    jsonObject.put("statusCode", "1");
//                    jsonObject.put("data", "");
//                    String a  = jsonObject.toString();
            webView.callHandler("requestx", final_string, data -> {
                Log.i("chenyi", "======>test result is " + data);
            });
            switch (object.getInt("statusCode")) {
                case 1:
//                    newJson.put("statusCode", object.getInt("statusCode"));
//                    newJson.put("data", object.get("result"));
//                    final_string = newJson.toString();
////                    JSONObject jsonObject = new JSONObject();
////                    jsonObject.put("statusCode", "1");
////                    jsonObject.put("data", "");
////                    String a  = jsonObject.toString();
//                    webView.callHandler("requestx", final_string, data -> {
//                        Log.i("chenyi", "======>test result is " + data);
//                    });
                    break;
                case 10010:
                    logout(MainActivity.this);
                    break;
                case 10003:
                    Toast.makeText(this, "服务器错误，请稍后再试...", Toast.LENGTH_SHORT).show();
                    break;
                case 10005:
                    logout(MainActivity.this);
                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void refreshSession() {
        int time = (int) (System.currentTimeMillis() / 1000);
        String sign = "";
        sign = sign + "sessionkey=" + preferences.getString("sessionkey", "") + "&";
        sign = sign + "timestamp=" + time + "&";
        sign = sign + "key=" + preferences.getString("auth_key", "");
        sign = md5(sign);
        HttpJsonMethod.getInstance().change_token(
                new ProgressSubscriber(changeOnNext, MainActivity.this), preferences.getString("access_token", ""),
                preferences.getString("sessionkey", ""), sign, time);
    }

    private void refreshAccess() {
        HttpJsonMethod.getInstance().get_token(
                new ProgressSubscriber(getTokenOnNext, MainActivity.this),
                "duoyunjia", "69534b32ab51f8cb802720d30fedb523");
    }

    public void camera() {
        if (Build.VERSION.SDK_INT >= 23) {
            int checkCallPhonePermission = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA);
            if (checkCallPhonePermission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, REQUEST_CODE_ASK_CALL_PHONE);
                return;
            } else {
                //上面已经写好的拍照方法
                write(true);
            }
        } else {
            //上面已经写好的拍照方法
            write(true);
        }
    }

    public void write(boolean iscamera) {
        if (Build.VERSION.SDK_INT >= 23) {
            int checkCallPhonePermission = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (checkCallPhonePermission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE);
                return;
            } else {
                if (iscamera) {
                    //上面已经写好的拍照方法
                    takePhoto();
                } else {
                    selectImage();
                }
            }
        } else {
            if (iscamera) {
                //上面已经写好的拍照方法
                takePhoto();
            } else {
                selectImage();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            switch (requestCode) {
                case 123:
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        write(true);
                    } else {
                        Toast.makeText(this, "无此权限，请前往设置", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case 222:
                    takePhoto();
                    break;
                case REQUEST_SAVE_WRITE:
                    new Thread(saveFileRunnable).start();
                    break;
            }
        } else {
            Toast.makeText(this, "无此权限，请前往设置", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
//            case 0:
//                if (data == null) {
//                    return;
//                }
              /*  String id = data.getStringExtra("id");
                String title = data.getStringExtra("title");
                String request = "{\"id\":\""+id+"\", \"title\":\""+title+"\"}";
                Log.d("wjj",request);
                webView.callHandler("setDisease", request, new CallBackFunction() {
                    @Override
                    public void onCallBack(String jsResponseData) {
                        Log.d("wjj", "callHandler WXPayMessage result " + jsResponseData);
                    }
                });*/
//                break;
            case 100:
//         从图库裁减返回
                Log.d("wjj", "100");
                if (data != null) {
                    Bitmap test = (Bitmap) data.getExtras().get("data");
//                    upDataHeadImg();
                    Uri uri = data.getData();
                    ContentResolver cr = this.getContentResolver();
                    try {
                        if (bmp != null)//如果不释放的话，不断取图片，将会内存不够
                            bmp.recycle();
                        bmp = BitmapFactory.decodeStream(cr.openInputStream(uri));

//                        bmp = Bitmap.createScaledBitmap(bmp, 150, 150, true);
//                        Log.i("wechat", "压缩后图片的大小" + (bmp.getByteCount() / 1024) + "KB宽度为"
//                                + bmp.getWidth() + "高度为" + bmp.getHeight());
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        bmp.compress(Bitmap.CompressFormat.JPEG, 50, baos);
                        byte[] bytes = baos.toByteArray();
                        bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        upDataHeadImg();
                    } catch (FileNotFoundException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }

                break;
            case 101:
                // 从拍照返回
                if (data != null) {
                    bmp = (Bitmap) data.getExtras().get("data");
//                    bmp = Bitmap.createScaledBitmap(bmp, 150, 150, true);
//                    Log.i("wechat", "压缩后图片的大小" + (bmp.getByteCount() / 1024) + "KB宽度为"
//                            + bmp.getWidth() + "高度为" + bmp.getHeight());

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bmp.compress(Bitmap.CompressFormat.JPEG, 50, baos);
                    byte[] bytes = baos.toByteArray();
                    bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);


                    upDataHeadImg();

                }
                break;
//            case 102:
//                // 从拍照后裁减返回
//                Log.d("wjj", "102");
//                if (picFile != null && picFile.exists()) {
//                    upDataHeadImg();
//                }
//                break;
        }
    }


    public void deletePic() {
        if (picFile.exists()) {
            picFile.delete();
        }
    }

    /**
     * 拍照
     */
    private void takePhoto() {
        createPicFile();
        try {
            // 选择拍照
//            Intent cameraintent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            // 指定调用相机拍照后照片的储存路径
//            cameraintent.putExtra(MediaStore.EXTRA_OUTPUT,
//                    Uri.fromFile(picFile));
//            startActivityForResult(cameraintent, 101);
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(intent, 101);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 从相册选择
     */
    private void selectImage() {
        createPicFile();
        //小米手机调用相册失败（打开了文件）
//        Intent intent = new Intent("android.intent.action.PICK");
//        intent.setDataAndType(MediaStore.Images.Media.INTERNAL_CONTENT_URI,
//                "image/*");

//6.0调用相册失败（打开了文件）
//        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//        intent.addCategory(Intent.CATEGORY_OPENABLE);
//        intent.setType("image/*");
        Intent intent;
        if (Build.VERSION.SDK_INT >= 23) {
            intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setType("image/*");
        } else {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
        }

//        intent.putExtra("output", Uri.fromFile(picFile));
////        intent.putExtra("crop", "true");
////        intent.putExtra("aspectX", 1);// 裁剪框比例
////        intent.putExtra("aspectY", 1);
////        intent.putExtra("outputX", 480);// 输出图片大小
////        intent.putExtra("outputY", 480);
//        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(picFile));
//        intent.putExtra("return-data", false);
//        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
//        intent.putExtra("noFaceDetection", true); // no face detection
        if (isIntentAvailable(MainActivity.this, intent)) {
            startActivityForResult(Intent.createChooser(intent, "选择图片"), 100);
        } else {
            Toast.makeText(MainActivity.this, "请安装相关图片查看应用。", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 拍照后图片裁减
     *
     * @param uri
     * @param outputX
     * @param outputY
     * @param requestCode
     */
    private void cropImageUri(Uri uri, int outputX, int outputY, int requestCode) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", outputX);
        intent.putExtra("outputY", outputY);
        intent.putExtra("scale", true);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        intent.putExtra("return-data", false);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        intent.putExtra("noFaceDetection", true); // no face detection
        startActivityForResult(intent, requestCode);
    }

    /**
     * 创建上传图片文件
     */
    private void createPicFile() {
        String sdStatus = Environment.getExternalStorageState();
        if (!sdStatus.equals(Environment.MEDIA_MOUNTED)) { // 检测sd是否可用
            Toast.makeText(MainActivity.this, "请检查SD卡是否可用", Toast.LENGTH_SHORT).show();
            return;
        }
        File file = new File(Environment.getExternalStorageDirectory().toString());
        if (!file.exists()) {
            file.mkdirs();
        }
        picFile = new File(file
                + "/seawaterHeadImg.jpg");
    }

    /**
     * 检测当的网络（WLAN、3G/2G）状态
     *
     * @param context Context
     * @return true 表示网络可用
     */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo info = connectivity.getActiveNetworkInfo();
            if (info != null && info.isConnected()) {
                // 当前网络是连接的
                if (info.getState() == NetworkInfo.State.CONNECTED) {
                    // 当前所连接的网络可用
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isIntentAvailable(Context context, Intent intent) {
        final PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> list = packageManager.queryIntentActivities(intent,
                PackageManager.GET_ACTIVITIES);
        return list.size() > 0;
    }

    /**
     * 上传用户头像
     */
    private void upDataHeadImg() {
        if (updateDialog == null) {
            updateDialog = ProgressDialog.show(MainActivity.this, "上传头像", "头像正在上传中，请稍等...", true, true);
        }
        Log.d("wjj", "head");
        int time = (int) (System.currentTimeMillis() / 1000);
        String sign = "";
//        sign = sign + "avatar=" + fileToBase64(bmp) + "&";
        sign = sign + "avatar=" + bitmaptoString(bmp) + "&";
        sign = sign + "sessionkey=" + preferences.getString("sessionkey", "") + "&";
        sign = sign + "timestamp=" + time + "&";
        sign = sign + "key=" + preferences.getString("auth_key", "");
        sign = md5(sign);

        HttpJsonMethod.getInstance().getAva(
                new ProgressErrorSubscriber<>(uploadOnNext, MainActivity.this), preferences.getString("access_token", ""), bitmaptoString(bmp),
                preferences.getString("sessionkey", ""), sign, time);
    }

    public void getResult(String msg) {
        String result = "{\"statusCode\":\"1\", \"data\":\"" + msg + "\"}";
        webView.callHandler("uploadImg", result, jsResponseData -> Log.d("wjj", "uploadImg " + jsResponseData));
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack()) {
            webView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
