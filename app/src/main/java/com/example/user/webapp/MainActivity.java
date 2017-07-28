package com.example.user.webapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.github.lzyzsd.jsbridge.BridgeWebView;
import com.github.lzyzsd.jsbridge.BridgeWebViewClient;
import com.github.lzyzsd.jsbridge.DefaultHandler;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.alipay.android.phone.mrpc.core.NetworkUtils.isNetworkAvailable;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.wv_main)
    BridgeWebView webView;
    @BindView(R.id.bt_refresh)
    Button btn;
    @BindView(R.id.rl_gone)
    RelativeLayout rl_gone;

    private String web_url;
    private boolean IS_OK = true;
    private boolean NETWORK_OK = true;
    private IntenterBoradCastReceiver receiver;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        getSupportActionBar().hide();
        registerBroadrecevicer();
        preferences = getSharedPreferences("user", Context.MODE_PRIVATE);
        btn.setOnClickListener(v -> {
            webView.loadUrl(web_url);
            IS_OK = true;
        });
        init();
//        webView.loadUrl(HttpConstants.BASEURL4);
    }

    private void init() {
        WebSettings webSettings = webView.getSettings();
        webSettings.setUseWideViewPort(true);//设置此属性，可任意比例缩放
        webSettings.setJavaScriptEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        //add by wjj
        //cache mode
        //设置缓存模式
        if (isNetworkAvailable(MainActivity.this)) {
            webView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        } else {
            webView.getSettings().setCacheMode(
                    WebSettings.LOAD_CACHE_ELSE_NETWORK);
        }
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
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                int a = (int) (System.currentTimeMillis() / 1000);
                int b = preferences.getInt("session_time", 0);
                int c = preferences.getInt("access_time",0);
                if (a + 3 * 24 * 60 * 60 * 1000 > b) {
                    RefreshMessage msg = new RefreshMessage(1);
                    EventBus.getDefault().post(msg);
                }
                if (a + 3 * 24 * 60 * 60 * 1000 > c) {
                    RefreshMessage msg = new RefreshMessage(2);
                    EventBus.getDefault().post(msg);
                }
                return super.shouldOverrideUrlLoading(view, url);
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

}
