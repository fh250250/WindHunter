package com.WindHunter;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.WindHunter.tools.*;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;



public class LoginActivity extends Activity {


    // 默认加密request_key
    private String requestKey = "THINKSNS";

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
                // TODO: 以后美化
                if (email.isEmpty() || password.isEmpty()){
                    Toast.makeText(LoginActivity.this, R.string.login_alert_emptyerror, Toast.LENGTH_SHORT).show();
                    return;
                }


                SharedPreferences settings = getSharedPreferences("settings", MODE_PRIVATE);
                String host = settings.getString("Host", "demo.thinksns.com/t3/");


                // 加密账号密码
                String encodeUid = null;
                String encodePasswd = null;
                try {
                    encodeUid = URLEncoder.encode(new DES_MOBILE().setKey(requestKey).encrypt(email), "UTF-8");
                    encodePasswd = URLEncoder.encode(new DES_MOBILE().setKey(requestKey).encrypt(MD5.md5(password)), "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                // 去掉编码字串最后的 %0a
                encodeUid = encodeUid.substring(0, encodeUid.length()-3);
                encodePasswd = encodePasswd.substring(0, encodePasswd.length() - 3);

                // 认证api
                String authorizeApi = "http://" + host + "index.php?app=api&mod=Oauth&act=authorize" + "&uid=" + encodeUid + "&passwd=" + encodePasswd;
                new LoginTask().execute(authorizeApi);

            }
        });
    }

    private class LoginTask extends AsyncTask<String, Integer, String>{

        @Override
        protected String doInBackground(String... api) {
            try{
                HttpClient httpClient = new DefaultHttpClient();
                HttpGet httpGet = new HttpGet(api[0]);
                HttpResponse httpResponse = httpClient.execute(httpGet);
                // 返回请求数据
                return EntityUtils.toString(httpResponse.getEntity());
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return "error";
        }

        @Override
        protected void onPreExecute() {
            findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
            findViewById(R.id.email).setEnabled(false);
            findViewById(R.id.password).setEnabled(false);
            findViewById(R.id.submit).setEnabled(false);
        }

        @Override
        protected void onPostExecute(String result) {
            if ( !result.equals("error") ){
                try {
                    // 获取JSON对象
                    JSONObject jsonResult = new JSONObject(result);
                    // 先计算出是否有code 防止 '&&'后面的代码先执行而照成异常
                    boolean tmp = jsonResult.has("code");
                    if ( tmp && jsonResult.getString("code").equals("00001") ){
                        // 认证失败
                        Toast.makeText(LoginActivity.this, R.string.login_alert_autherror, Toast.LENGTH_SHORT).show();
                        ((EditText) findViewById(R.id.password)).setText("");
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
            }else{
                // TODO：以后可以考虑增加背景例如气泡效果
                Toast.makeText(LoginActivity.this,R.string.login_alert_neterror,Toast.LENGTH_SHORT).show();
            }

            findViewById(R.id.progressBar).setVisibility(View.GONE);
            findViewById(R.id.email).setEnabled(true);
            findViewById(R.id.password).setEnabled(true);
            findViewById(R.id.submit).setEnabled(true);
        }
    }

}
