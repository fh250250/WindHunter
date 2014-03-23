package com.WindHunter;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;


public class LoginActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        // 登录逻辑
        Button loginBtn = (Button)findViewById(R.id.submit);
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // 获取输入
                String email = ((EditText) findViewById(R.id.email)).getText().toString();
                String password = ((EditText) findViewById(R.id.password)).getText().toString();

                // 检测空白输入
                if (email.isEmpty() || password.isEmpty()){
                    new AlertDialog.Builder(LoginActivity.this)
                            .setMessage(R.string.login_alert)
                            .setPositiveButton(R.string.login_alert_ok, null)
                            .show();
                    return;
                }
            }
        });
    }
}
