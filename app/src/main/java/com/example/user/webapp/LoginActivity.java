package com.example.user.webapp;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jakewharton.rxbinding.widget.RxTextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LoginActivity extends AppCompatActivity {
    @BindView(R.id.bt_send_yanzhengma)
    TextView mBtSendYanzhengma;
    @BindView(R.id.editText)
    EditText et_mobile;
    @BindView(R.id.editText2)
    EditText et_pass;
    @BindView(R.id.et_yanzhengma)
    EditText et_yanzhengma;
    @BindView(R.id.linearLayout)
    RelativeLayout mLinearLayout;
    @BindView(R.id.editText3)
    EditText mEditText3;
    private int recLen = 10;
    private boolean flag = true;
    private CallbackOnResponse mCallbackOnResponse;

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
        getSupportActionBar().hide();

        RxTextView.textChanges(et_mobile).subscribe(charSequence -> {
            if (charSequence.length() == 11) {
                if (Utils.isChinaPhoneLegal(charSequence.toString())) {
                    mLinearLayout.setVisibility(View.VISIBLE);
                    et_pass.setVisibility(View.VISIBLE);
                    mEditText3.setVisibility(View.VISIBLE);
                }
            } else {
                mLinearLayout.setVisibility(View.GONE);
                et_pass.setVisibility(View.GONE);
                mEditText3.setVisibility(View.GONE);
            }

        });

        mCallbackOnResponse = new CallbackOnResponse() {
            @Override
            public void onResponse(Object o) {

            }
        };

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
                if (flag && !et_mobile.getText().toString().equals("")) {
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
