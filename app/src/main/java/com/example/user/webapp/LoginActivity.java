package com.example.user.webapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.user.webapp.bean.AccessTokenBean;
import com.example.user.webapp.bean.LoginBean;
import com.example.user.webapp.http.HttpJsonMethod;
import com.example.user.webapp.http.ProgressSubscriber;
import com.example.user.webapp.http.SubscriberOnNextListener;
import com.google.gson.Gson;
import com.jakewharton.rxbinding.widget.RxTextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.example.user.webapp.Utils.isChinaPhoneLegal;
import static com.example.user.webapp.Utils.md5;

public class LoginActivity extends AppCompatActivity {

    @BindView(R.id.editText)
    EditText mEditText;
    @BindView(R.id.ll_1)
    LinearLayout mLl1;
    @BindView(R.id.iv_yanzhengma)
    ImageView mIvYanzhengma;
    @BindView(R.id.bt_send_yanzhengma)
    TextView mBtSendYanzhengma;
    @BindView(R.id.et_yanzhengma)
    EditText mEtYanzhengma;
    @BindView(R.id.ll_yanzhengma)
    RelativeLayout mLlYanzhengma;
    @BindView(R.id.line_yanzhengma)
    View mLineYanzhengma;

    private int recLen = 10;
    private boolean flag = true;
    private SubscriberOnNextListener<JSONObject> getTokenOnNext;
    private SubscriberOnNextListener<JSONObject> sendOnNext;
    private SubscriberOnNextListener<JSONObject> loginOnNext;
    private SubscriberOnNextListener<JSONObject> changeOnNext;
    private SubscriberOnNextListener<JSONObject> logoutOnNext;
    private Gson gson = new Gson();
    private AccessTokenBean mAccessTokenBean = new AccessTokenBean();
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private ProgressDialog dialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);
        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            decorView.setSystemUiVisibility(option);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        getSupportActionBar().hide();
        preferences = getSharedPreferences("user", Context.MODE_PRIVATE);
        editor = preferences.edit();
        getTokenOnNext = resultBean -> {
            if (resultBean.getInt("statusCode") == 1) {
                if (null!=dialog) {
                    dialog.dismiss();
                }
                AccessTokenBean indexBean = gson.fromJson(resultBean.toString(), AccessTokenBean.class);
                mAccessTokenBean = indexBean;
                editor.putString("access_token", indexBean.getResult().getAccess_token());
                editor.putString("auth_key", indexBean.getResult().getAuth_key());
                editor.putInt("access_time", indexBean.getResult().getTimestamp());
                editor.apply();
            } else {
                Toast.makeText(this, resultBean.getString("result"), Toast.LENGTH_SHORT).show();
            }
        };

        sendOnNext = resultBean -> {
            if (resultBean.getInt("statusCode") == 1) {
                Toast.makeText(this, resultBean.getString("result"), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, resultBean.getString("result"), Toast.LENGTH_SHORT).show();
            }
        };

        loginOnNext = resultBean -> {
            if (resultBean.getInt("statusCode") == 1) {
                Toast.makeText(this, "登录成功！", Toast.LENGTH_SHORT).show();
                LoginBean indexBean = gson.fromJson(resultBean.toString(), LoginBean.class);

                editor.putString("sessionkey", indexBean.getResult().getSessionkey());
                editor.putLong("session_time", indexBean.getResult().getTimestamp());
                editor.apply();
//                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                int time = (int) (System.currentTimeMillis() / 1000);
                String sign = "";
                sign = sign + "sessionkey=" + preferences.getString("sessionkey", "") + "&";
                sign = sign + "timestamp=" + time + "&";
                sign = sign + "key=" + mAccessTokenBean.getResult().getAuth_key();
                sign = md5(sign);
                HttpJsonMethod.getInstance().change_token(
                        new ProgressSubscriber(changeOnNext, LoginActivity.this), mAccessTokenBean.getResult().getAccess_token(),
                        preferences.getString("sessionkey", ""), sign, time);
            }
        };

        changeOnNext = resultBean -> {
            if (resultBean.getInt("statusCode") == 1) {
                Toast.makeText(this, "修改成功！", Toast.LENGTH_SHORT).show();
                LoginBean indexBean = gson.fromJson(resultBean.toString(), LoginBean.class);
                editor.putString("sessionkey", indexBean.getResult().getSessionkey());
                editor.putInt("session_time", indexBean.getResult().getTimestamp());
                editor.apply();
                int time = (int) (System.currentTimeMillis() / 1000);
                String sign = "";
                sign = sign + "sessionkey=" + preferences.getString("sessionkey", "") + "&";
                sign = sign + "timestamp=" + time + "&";
                sign = sign + "key=" + mAccessTokenBean.getResult().getAuth_key();
                sign = md5(sign);
                HttpJsonMethod.getInstance().dalete_token(
                        new ProgressSubscriber(logoutOnNext, LoginActivity.this), mAccessTokenBean.getResult().getAccess_token(),
                        preferences.getString("sessionkey", ""), sign, time);
            }
        };

        logoutOnNext = resultBean -> {
            if (resultBean.getInt("statusCode") == 1) {
                Toast.makeText(this, "登出成功！", Toast.LENGTH_SHORT).show();
                editor.putString("sessionkey", "");
                editor.apply();
            }
        };

        RxTextView.textChanges(mEditText).subscribe(charSequence -> {
            if (charSequence.length() == 11) {
                if (isChinaPhoneLegal(charSequence.toString())) {
                    HttpJsonMethod.getInstance().get_token(
                            new ProgressSubscriber(getTokenOnNext, LoginActivity.this),
                            "duoyunjia", "69534b32ab51f8cb802720d30fedb523");
                    dialog = ProgressDialog.show(LoginActivity.this, "提示", "正在查找数据，请稍等...", false, true);//创建ProgressDialog
                }
            }
        });
    }

    Handler handler = new Handler();
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (recLen >= 1) {
                recLen--;
                mBtSendYanzhengma.setText("重新获取(" + recLen + "s)");
                handler.postDelayed(this, 1000);
            } else {
                flag = true;
                recLen = 10;
                mBtSendYanzhengma.setClickable(true);
                mBtSendYanzhengma.setText("获取验证码");
            }
        }
    };

    @OnClick({R.id.bt_send_yanzhengma, R.id.button})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.bt_send_yanzhengma:
                String tele = mEditText.getText().toString();
                if (flag && isChinaPhoneLegal(tele)) {
                    flag = false;
                    mBtSendYanzhengma.setClickable(false);
                    handler.post(runnable);
                    int time = (int) (System.currentTimeMillis() / 1000);
                    String sign = "";
                    sign = sign + "mobile=" + tele + "&";
                    sign = sign + "timestamp=" + time + "&";
                    sign = sign + "key=" + mAccessTokenBean.getResult().getAuth_key();
                    sign = md5(sign);
                    editor.putString("username", tele);
                    HttpJsonMethod.getInstance().send_code(
                            new ProgressSubscriber(sendOnNext, LoginActivity.this), mAccessTokenBean.getResult().getAccess_token(), tele,
                            sign, time);
                } else {
                    Toast.makeText(LoginActivity.this, "请填写手机号！", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.button:
                String tele1 = mEditText.getText().toString();
                String yan = mEtYanzhengma.getText().toString();
                String sign = "";
                int time = (int) (System.currentTimeMillis() / 1000);
                sign = sign + "kapkey=" + mEtYanzhengma.getText().toString() + "&";
                sign = sign + "mobile=" + tele1 + "&";
                sign = sign + "timestamp=" + time + "&";
                sign = sign + "key=" + mAccessTokenBean.getResult().getAuth_key();
                sign = md5(sign);
                HttpJsonMethod.getInstance().login(
                        new ProgressSubscriber(loginOnNext, LoginActivity.this), mAccessTokenBean.getResult().getAccess_token(), mEtYanzhengma.getText().toString(), tele1,
                        sign, time);
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMoonEvent(RefreshMessage loginInfoMessage) {
        switch (loginInfoMessage.refresh) {
            case 1:
                refreshSession();
                break;
            case 2:
                refreshAccess();
                break;
        }
    }

    private void refreshSession() {
        int time = (int) (System.currentTimeMillis() / 1000);
        String sign = "";
        sign = sign + "sessionkey=" + preferences.getString("sessionkey", "") + "&";
        sign = sign + "timestamp=" + time + "&";
        sign = sign + "key=" + mAccessTokenBean.getResult().getAuth_key();
        sign = md5(sign);
        HttpJsonMethod.getInstance().change_token(
                new ProgressSubscriber(changeOnNext, LoginActivity.this), mAccessTokenBean.getResult().getAccess_token(),
                preferences.getString("sessionkey", ""), sign, time);
    }

    private void refreshAccess() {
        HttpJsonMethod.getInstance().get_token(
                new ProgressSubscriber(getTokenOnNext, LoginActivity.this),
                "duoyunjia", "69534b32ab51f8cb802720d30fedb523");
    }
}
