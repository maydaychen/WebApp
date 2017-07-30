package com.example.user.webapp;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
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
import com.umeng.message.PushAgent;

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
    @BindView(R.id.ll_login)
    LinearLayout mLlLogin;
    @BindView(R.id.imageView)
    ImageView mImageView;

    private int recLen = 10;
    private boolean flag = true;
    private SubscriberOnNextListener<JSONObject> getTokenOnNext;
    private SubscriberOnNextListener<JSONObject> sendOnNext;
    private SubscriberOnNextListener<JSONObject> loginOnNext;
    private SubscriberOnNextListener<JSONObject> changeOnNext;
    private Gson gson = new Gson();
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private ProgressDialog dialog = null;
    private PushAgent mPushAgent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            decorView.setSystemUiVisibility(option);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        preferences = getSharedPreferences("user", Context.MODE_PRIVATE);
        editor = preferences.edit();
        mPushAgent = PushAgent.getInstance(this);
        editor.putString("device_token", mPushAgent.getRegistrationId());
        editor.commit();

        if (preferences.getBoolean("autoLog", false)) {
            int time = (int) (System.currentTimeMillis() / 1000);
            int session_time = preferences.getInt("session_time", time);
            if (session_time > time) {
                finish();
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
            }
        }

        getSupportActionBar().hide();
        WindowManager wm = this.getWindowManager();
        ObjectAnimator animator = ObjectAnimator.ofFloat(mImageView, "translationY", wm.getDefaultDisplay().getHeight() / 2 - 300, 100).setDuration(2000);
        ObjectAnimator animator2 = ObjectAnimator.ofFloat(mLlLogin, "alpha", 0, 1).setDuration(2000);
        AnimatorSet set = new AnimatorSet();
        set.play(animator2).after(animator);//animator2在显示完animator1之后再显示
        set.start();

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

        sendOnNext = resultBean -> {
            switch (resultBean.getInt("statusCode")) {
                case 1:
                    Toast.makeText(this, resultBean.getString("result"), Toast.LENGTH_SHORT).show();
                    break;
                case 10003:
                    Toast.makeText(this, "服务器错误，请稍后再试...", Toast.LENGTH_SHORT).show();
                    break;
            }
        };

        loginOnNext = resultBean -> {
            if (resultBean.getInt("statusCode") == 1) {
                Toast.makeText(this, "登录成功！", Toast.LENGTH_SHORT).show();
                LoginBean indexBean = gson.fromJson(resultBean.toString(), LoginBean.class);
                editor.putString("sessionkey", indexBean.getResult().getSessionkey());
                editor.putInt("session_time", indexBean.getResult().getTimestamp());
                editor.putBoolean("autoLog", true);
                editor.apply();
                finish();
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                int time = (int) (System.currentTimeMillis() / 1000);
                String sign = "";
                sign = sign + "sessionkey=" + preferences.getString("sessionkey", "") + "&";
                sign = sign + "timestamp=" + time + "&";
                sign = sign + "key=" + preferences.getString("auth_key", "");
                sign = md5(sign);

//                HttpJsonMethod.getInstance().change_token(
//                        new ProgressSubscriber(changeOnNext, LoginActivity.this), preferences.getString("access_token", ""),
//                        preferences.getString("sessionkey", ""), sign, time);
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

//        logoutOnNext = resultBean -> {
//            if (resultBean.getInt("statusCode") == 1) {
//                Toast.makeText(this, "登出成功！", Toast.LENGTH_SHORT).show();
//                editor.putString("sessionkey", "");
//                editor.apply();
//            }
//        };

        RxTextView.textChanges(mEditText).subscribe(charSequence -> {
            if (charSequence.length() == 11) {
                if (isChinaPhoneLegal(charSequence.toString())) {
                    HttpJsonMethod.getInstance().get_token(
                            new ProgressSubscriber(getTokenOnNext, LoginActivity.this),
                            "duoyunjia", "69534b32ab51f8cb802720d30fedb523");
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
                    sign = sign + "key=" + preferences.getString("auth_key", "");
                    sign = md5(sign);
                    editor.putString("username", tele);
                    HttpJsonMethod.getInstance().send_code(
                            new ProgressSubscriber(sendOnNext, LoginActivity.this), preferences.getString("access_token", ""), tele,
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
                sign = sign + "device_tokens=" + mPushAgent.getRegistrationId() + "&";
                sign = sign + "kapkey=" + mEtYanzhengma.getText().toString() + "&";
                sign = sign + "mobile=" + tele1 + "&";
                sign = sign + "timestamp=" + time + "&";
                sign = sign + "key=" + preferences.getString("auth_key", "");
                sign = md5(sign);
                HttpJsonMethod.getInstance().login(
                        new ProgressSubscriber(loginOnNext, LoginActivity.this), preferences.getString("access_token", ""),
                        mPushAgent.getRegistrationId(), mEtYanzhengma.getText().toString(), tele1, sign, time);
                break;
        }
    }
}
