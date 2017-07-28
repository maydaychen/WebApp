package com.example.user.webapp;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
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

import com.jakewharton.rxbinding.widget.RxTextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

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
    @BindView(R.id.et_pass)
    EditText mEtPass;
    @BindView(R.id.ll_pass)
    LinearLayout mLlPass;
    @BindView(R.id.line_pass)
    View mLinePass;
    @BindView(R.id.et_pass2)
    EditText mEtPass2;
    @BindView(R.id.ll_pass2)
    LinearLayout mLlPass2;
    @BindView(R.id.line_pass2)
    View mLinePass2;
    @BindView(R.id.imageView)
    ImageView mImageView;
    @BindView(R.id.ll_login)
    LinearLayout mLlLogin;
    private int recLen = 10;
    private boolean flag = true;
    private CallbackOnResponse mCallbackOnResponse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        WindowManager wm = this.getWindowManager();
        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            decorView.setSystemUiVisibility(option);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        getSupportActionBar().hide();

        RxTextView.textChanges(mEditText).subscribe(charSequence -> {
            if (charSequence.length() == 11) {
                if (Utils.isChinaPhoneLegal(charSequence.toString())) {
                    mLlPass.setVisibility(View.VISIBLE);
                    mLlPass2.setVisibility(View.VISIBLE);
                    mLinePass.setVisibility(View.VISIBLE);
                    mLinePass2.setVisibility(View.VISIBLE);
                }
            } else {
                mLlPass.setVisibility(View.GONE);
                mLlPass2.setVisibility(View.GONE);
                mLinePass.setVisibility(View.GONE);
                mLinePass2.setVisibility(View.GONE);
            }

        });

        ObjectAnimator animator = ObjectAnimator.ofFloat(mImageView, "translationY", wm.getDefaultDisplay().getHeight() / 2, 100).setDuration(2000);

        ObjectAnimator animator2 = ObjectAnimator.ofFloat(mLlLogin, "alpha", 0, 1).setDuration(2000);
//
        AnimatorSet set = new AnimatorSet();
        set.play(animator2).after(animator);//animator2在显示完animator1之后再显示
        set.start();

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
                if (flag && !mEditText.getText().toString().equals("")) {
                    flag = false;
                    mBtSendYanzhengma.setClickable(false);
                    handler.post(runnable);
                } else {
                    Toast.makeText(LoginActivity.this, "请填写手机号！", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.button:
                break;
        }
    }
}
