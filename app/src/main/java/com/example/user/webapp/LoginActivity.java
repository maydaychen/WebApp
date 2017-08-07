package com.example.user.webapp;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
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

import static com.example.user.webapp.Utils.dip2px;
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
    @BindView(R.id.iv_start)
    LinearLayout mIvStart;
    @BindView(R.id.rl_login)
    RelativeLayout mRlLogin;
    @BindView(R.id.tv_support)
    TextView mTvSupport;
    @BindView(R.id.test)
    ImageView mTest;

    private int recLen = 10;
    private boolean flag = true;
    private SubscriberOnNextListener<JSONObject> getTokenOnNext;
    private SubscriberOnNextListener<JSONObject> sendOnNext;
    private SubscriberOnNextListener<JSONObject> loginOnNext;
    private SubscriberOnNextListener<JSONObject> changeOnNext;
    private Loading_view loading;
    private Gson gson = new Gson();
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private PushAgent mPushAgent;
    private boolean IS_SHOWING = false;

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
        loading = new Loading_view(this, R.style.CustomDialog);
        preferences = getSharedPreferences("user", Context.MODE_PRIVATE);
        editor = preferences.edit();
        mPushAgent = PushAgent.getInstance(this);
        editor.putString("device_token", mPushAgent.getRegistrationId());
        editor.commit();
        getSupportActionBar().hide();

        ObjectAnimator first = ObjectAnimator.ofFloat(mTvSupport, "alpha", 1, 0).setDuration(2000);
        first.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                if (preferences.getBoolean("autoLog", false)) {
                    int time = (int) (System.currentTimeMillis() / 1000);
                    int session_time = preferences.getInt("session_time", time);
                    if (session_time > time) {
                        finish();
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    }else {
                        mIvStart.setVisibility(View.GONE);
                        mRlLogin.setVisibility(View.VISIBLE);
                        Display display = getWindowManager().getDefaultDisplay();
                        Point size = new Point();
                        display.getSize(size);
                        int a = mRlLogin.getMeasuredHeight();
                        int b = mImageView.getMeasuredHeight();
                        a = a / 2;
                        ObjectAnimator animator1 = ObjectAnimator.ofFloat(mImageView, "translationY", (a - b - dip2px(50)), 0).setDuration(2000);
                        ObjectAnimator animator2 = ObjectAnimator.ofFloat(mLlLogin, "alpha", 0, 1).setDuration(2000);
                        AnimatorSet set = new AnimatorSet();
                        set.play(animator2).after(animator1);//animator2在显示完animator1之后再显示
                        set.start();
                    }
                } else {
                    mIvStart.setVisibility(View.GONE);
                    mRlLogin.setVisibility(View.VISIBLE);

                    Display display = getWindowManager().getDefaultDisplay();
                    Point size = new Point();
                    display.getSize(size);

                    int a = mRlLogin.getMeasuredHeight();
                    int b = mImageView.getMeasuredHeight();
                    a = a / 2;
                    ObjectAnimator animator1 = ObjectAnimator.ofFloat(mImageView, "translationY", (a - b - dip2px(50)), 0).setDuration(2000);
                    ObjectAnimator animator2 = ObjectAnimator.ofFloat(mLlLogin, "alpha", 0, 1).setDuration(2000);
                    AnimatorSet set = new AnimatorSet();
                    set.play(animator2).after(animator1);//animator2在显示完animator1之后再显示
                    set.start();
                }
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        first.start();


            /* Create an Intent that will start the Main WordPress Activity. */


        getTokenOnNext = resultBean -> {
            mEtYanzhengma.requestFocus();
            if (loading != null) {
                loading.dismiss();
            }
            if (IS_SHOWING) {
                loading.dismiss();
                IS_SHOWING = false;
            }
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
                default:
                    Toast.makeText(this, resultBean.getString("result"), Toast.LENGTH_SHORT).show();
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
            if (IS_SHOWING) {
                new Handler().postDelayed(() -> {
            /* Create an Intent that will start the Main WordPress Activity. */
                    loading.dismiss();
                    IS_SHOWING = false;
                }, 500);
            }
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
            } else {
                Toast.makeText(this, resultBean.getString("result"), Toast.LENGTH_SHORT).show();
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
                    if (!IS_SHOWING) {
                        loading.show();
                        IS_SHOWING = true;
                    }
                    HttpJsonMethod.getInstance().get_token(
                            new ProgressSubscriber(getTokenOnNext, LoginActivity.this),
                            "duoyunjia", "69534b32ab51f8cb802720d30fedb523");
                }
            }
        });

        RxTextView.textChanges(mEtYanzhengma).subscribe(charSequence -> {
            if (charSequence.length() == 6) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
            }
        });
    }

    Handler handler = new Handler();
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (recLen >= 1) {
                recLen--;
                mBtSendYanzhengma.setText(recLen + "");
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
                if (!IS_SHOWING) {
                    loading.show();
                    IS_SHOWING = true;
                }
                String sign = "";
                int time = (int) (System.currentTimeMillis() / 1000);
                if (!mPushAgent.getRegistrationId().equals("")) {
                    sign = sign + "device_tokens=" + mPushAgent.getRegistrationId() + "&";
                }
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
