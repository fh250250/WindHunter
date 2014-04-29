package com.WindHunter;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.Toast;
import com.WindHunter.tools.*;
import android.os.Bundle;
import android.view.View;
import com.beardedhen.androidbootstrap.BootstrapButton;
import com.beardedhen.androidbootstrap.BootstrapEditText;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;
import org.json.JSONException;
import org.json.JSONObject;




public class LoginActivity extends ActionBarActivity {


    // 默认加密request_key
    private final String requestKey = "THINKSNS";

    // 登录按钮
    @ViewInject(R.id.submit)      BootstrapButton loginBtn;
    // 账号输入框
    @ViewInject(R.id.email)       BootstrapEditText emailEditText;
    // 密码输入框
    @ViewInject(R.id.password)    BootstrapEditText passwordEditText;

    // 记住密码框
    @ViewInject(R.id.remember_password)
    CheckBox remember_password;


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(false);
        setTitle(R.string.login_title);

        // 默认不显示进度条
        setProgressBarIndeterminateVisibility(false);

        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 获取进度条
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        setContentView(R.layout.login);

        // ViewUtils 注入
        ViewUtils.inject(this);

        // 默认勾选
        remember_password.setChecked(true);

        // 填账号密码
        SharedPreferences remember = getSharedPreferences("remember", MODE_PRIVATE);
        emailEditText.setText(remember.getString("email", ""));
        passwordEditText.setText(remember.getString("password", ""));

        // 将光标移至末尾
        emailEditText.setSelection(emailEditText.getText().length());
    }


    // 登录按钮逻辑
    @OnClick(R.id.submit)
    public void submitClick(View view){

        // 获取输入
        final String email = emailEditText.getText().toString();
        final String password = passwordEditText.getText().toString();

        // 检测空白输入
        if (email.isEmpty() || password.isEmpty()){
            Toast.makeText(LoginActivity.this, R.string.login_alert_emptyerror, Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences settings = getSharedPreferences("settings", MODE_PRIVATE);
        String host = settings.getString("Host", "demo.thinksns.com/t3/");

        // 加密账号密码
        String encodeUid = new DES_MOBILE().setKey(requestKey).encrypt(email);
        String encodePasswd = new DES_MOBILE().setKey(requestKey).encrypt(MD5.md5(password));

        // 认证api
        String authorizeApi = "http://" + host + "index.php?app=api&mod=Oauth&act=authorize";

        // 请求的参数
        RequestParams requestParams = new RequestParams();
        requestParams.addQueryStringParameter("uid", encodeUid);
        requestParams.addQueryStringParameter("passwd", encodePasswd);

        // 完成HTTP请求
        HttpUtils http = new HttpUtils();
        http.configDefaultHttpCacheExpiry(1000);
        http.send(com.lidroid.xutils.http.client.HttpRequest.HttpMethod.GET,
                authorizeApi,
                requestParams,
                new RequestCallBack<String>(){

                    @Override
                    public void onStart() {
                        // 禁用控件
                        setUIEnable(false);
                    }

                    @Override
                    public void onSuccess(ResponseInfo<String> responseInfo) {
                        try {
                            // 获取JSON对象
                            JSONObject jsonResult = new JSONObject(responseInfo.result);
                            // 先计算出是否有code 防止 '&&'后面的代码先执行而照成异常
                            boolean tmp = jsonResult.has("code");
                            if ( tmp && jsonResult.getString("code").equals("00001") ){
                                // 认证失败
                                Toast.makeText(LoginActivity.this, R.string.login_alert_autherror, Toast.LENGTH_SHORT).show();
                                passwordEditText.setText("");
                                setUIEnable(true);
                            }else{
                                // 成功
                                SharedPreferences settings = getSharedPreferences("settings", MODE_PRIVATE);
                                SharedPreferences.Editor editor = settings.edit();
                                editor.putString("oauth_token", jsonResult.getString("oauth_token"));
                                editor.putString("oauth_token_secret", jsonResult.getString("oauth_token_secret"));
                                editor.putString("uid", jsonResult.getString("uid"));
                                editor.commit();

                                // 登录成功 页面跳转
                                startActivity(new Intent().setClass(LoginActivity.this, MainActivity.class));
                                LoginActivity.this.finish();

                                // 记住账号密码
                                SharedPreferences remember = getSharedPreferences("remember", MODE_PRIVATE);
                                SharedPreferences.Editor rememberEditor = remember.edit();
                                if (remember_password.isChecked()){
                                    rememberEditor.putString("email", email);
                                    rememberEditor.putString("password", password);
                                    rememberEditor.commit();
                                }else {
                                    rememberEditor.putString("email", "");
                                    rememberEditor.putString("password", "");
                                    rememberEditor.commit();
                                }

                            }
                        } catch (JSONException e) {
                            Toast.makeText(LoginActivity.this,R.string.login_alert_neterror,Toast.LENGTH_SHORT).show();
                            setUIEnable(true);
                        }
                    }

                    @Override
                    public void onFailure(HttpException e, String s) {
                        Toast.makeText(LoginActivity.this,R.string.login_alert_neterror,Toast.LENGTH_SHORT).show();
                        setUIEnable(true);
                    }
                });

    }

    // 登录界面控件的控制
    private void setUIEnable(boolean b){
        setProgressBarIndeterminateVisibility(!b);
        emailEditText.setEnabled(b);
        passwordEditText.setEnabled(b);
        loginBtn.setEnabled(b);
    }

}
