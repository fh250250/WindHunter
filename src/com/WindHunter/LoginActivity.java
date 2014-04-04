package com.WindHunter;

import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.WindHunter.tools.*;
import android.app.Activity;
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




public class LoginActivity extends Activity {


    // 默认加密request_key
    private final String requestKey = "THINKSNS";

    // 登录按钮
    @ViewInject(R.id.submit)      BootstrapButton loginBtn;
    // 进度条
    @ViewInject(R.id.progressBar) ProgressBar progressBar;
    // 账号输入框
    @ViewInject(R.id.email)       BootstrapEditText emailEditText;
    // 密码输入框
    @ViewInject(R.id.password)    BootstrapEditText passwordEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        // ViewUtils 注入
        ViewUtils.inject(this);

        // 进度条不显示
        progressBar.setVisibility(View.GONE);
    }


    // 登录按钮逻辑
    @OnClick(R.id.submit)
    public void submitClick(View view){

        // 获取输入
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        // 检测空白输入
        // TODO: 以后美化
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
                    public void onLoading(long total, long current, boolean isUploading) {
                        super.onLoading(total, current, isUploading);
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
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(HttpException e, String s) {
                        // TODO：以后可以考虑增加背景例如气泡效果
                        Toast.makeText(LoginActivity.this,R.string.login_alert_neterror,Toast.LENGTH_SHORT).show();
                        setUIEnable(true);
                    }
                });

    }

    // 登录界面控件的控制
    private void setUIEnable(boolean b){
        progressBar.setVisibility(b ? View.GONE : View.VISIBLE);
        emailEditText.setEnabled(b);
        passwordEditText.setEnabled(b);
        loginBtn.setEnabled(b);
    }

}
