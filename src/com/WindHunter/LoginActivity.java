package com.WindHunter;

import android.widget.ProgressBar;
import com.WindHunter.tools.*;
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

        // 进度条不显示
        ProgressBar progressBar = (ProgressBar)findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);

        // 登录逻辑
        Button loginBtn = (Button)findViewById(R.id.submit);
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // 获取输入
                String email = ((EditText) findViewById(R.id.email)).getText().toString();
                String password = ((EditText) findViewById(R.id.password)).getText().toString();

                // 检测空白输入
                // TODO: 改为屏幕下面的浮动框，编辑框变红
                if (email.isEmpty() || password.isEmpty()){
                    new AlertDialog.Builder(LoginActivity.this)
                            .setMessage(R.string.login_alert)
                            .setPositiveButton(R.string.login_alert_ok, null)
                            .show();
                    return;
                }


                // TODO: 构造api
                LoginNetWorker loginNetWorker = new LoginNetWorker();
                loginNetWorker.execute("http://www.baidu.com");
            }
        });
    }

    private class LoginNetWorker extends NetWorker{

        @Override
        protected void onPostExecute(String result) {
            findViewById(R.id.progressBar).setVisibility(View.GONE);
        }

        @Override
        protected void onPreExecute() {
            findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
            findViewById(R.id.email).setEnabled(false);
            findViewById(R.id.password).setEnabled(false);
            findViewById(R.id.submit).setEnabled(false);
        }

        @Override
        // TODO: 解析数据并放入全局共享对象中
        protected void parser(String jsonStr) {
            new AlertDialog.Builder(LoginActivity.this)
                    .setMessage(jsonStr)
                    .show();
        }

        @Override
        // TODO: 需要改成浮动框
        protected void alert() {
            new AlertDialog.Builder(LoginActivity.this)
                    .setMessage("NetWork Error")
                    .show();
        }
    }
}
