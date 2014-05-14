package com.WindHunter;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import com.beardedhen.androidbootstrap.BootstrapButton;
import com.beardedhen.androidbootstrap.BootstrapEditText;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;
import org.json.JSONException;
import org.json.JSONObject;

public class RegisterActivity extends ActionBarActivity{

    @ViewInject(R.id.register_email)
    BootstrapEditText emailView;

    @ViewInject(R.id.register_uname)
    BootstrapEditText unameView;

    @ViewInject(R.id.register_password)
    BootstrapEditText passwordView;

    @ViewInject(R.id.register_password_confirm)
    BootstrapEditText confirmView;

    @ViewInject(R.id.register_man)
    RadioButton manView;

    @ViewInject(R.id.register_woman)
    RadioButton womanView;

    @ViewInject(R.id.register)
    BootstrapButton register;

    @ViewInject(R.id.register_error_message)
    TextView errorMsg;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("注册新用户");
        actionBar.setDisplayShowHomeEnabled(false);

        // 默认不显示进度条
        setProgressBarIndeterminateVisibility(false);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 获取进度条
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        setContentView(R.layout.register);

        ViewUtils.inject(this);

        manView.setChecked(true);
    }

    @OnClick(R.id.register)
    public void registerClick(View view){
        String name = unameView.getText().toString();
        final String email = emailView.getText().toString();
        final String password = passwordView.getText().toString();
        String confirm = confirmView.getText().toString();

        if (name.isEmpty() ||
                email.isEmpty() ||
                password.isEmpty() ||
                confirm.isEmpty()){
            Toast.makeText(this, "请填写完整信息", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirm)){
            Toast.makeText(this, "两次密码不同", Toast.LENGTH_SHORT).show();
            passwordView.setText("");
            confirmView.setText("");
            return;
        }

        SharedPreferences settings = getSharedPreferences("settings", MODE_PRIVATE);
        String host = settings.getString("Host", "demo.thinksns.com/t3/");

        String registerApi = "http://" + host + "index.php?app=api&mod=Oauth&act=register";
        RequestParams requestParams = new RequestParams();
        requestParams.addQueryStringParameter("uname", name);
        requestParams.addQueryStringParameter("email", email);
        requestParams.addQueryStringParameter("password", password);
        if (manView.isChecked()){
            requestParams.addQueryStringParameter("sex", "1");
        }else if (womanView.isChecked()){
            requestParams.addQueryStringParameter("sex", "2");
        }

        HttpUtils httpUtils = new HttpUtils();
        httpUtils.configDefaultHttpCacheExpiry(2000);

        httpUtils.send(HttpRequest.HttpMethod.GET,
                registerApi,
                requestParams,
                new RequestCallBack<String>() {
                    @Override
                    public void onStart() {
                        setUIEnable(false);
                    }

                    @Override
                    public void onSuccess(ResponseInfo<String> responseInfo) {
                        try {
                            JSONObject result = new JSONObject(responseInfo.result);

                            if (result.getInt("status") == 0){
                                errorMsg.setText("注册失败," + result.getString("msg"));

                                setUIEnable(true);
                            }else{
                                Intent intent = new Intent(RegisterActivity.this, RecommendActivity.class);
                                intent.putExtra("email", email);
                                intent.putExtra("password", password);

                                startActivity(intent);
                                finish();

                                // 记住账号密码
                                SharedPreferences remember = getSharedPreferences("remember", MODE_PRIVATE);
                                SharedPreferences.Editor rememberEditor = remember.edit();

                                rememberEditor.putString("email", email);
                                rememberEditor.putString("password", password);
                                rememberEditor.commit();
                            }
                        } catch (JSONException e) {
                            Toast.makeText(RegisterActivity.this, "网络出错", Toast.LENGTH_SHORT).show();
                            setUIEnable(true);
                        }
                    }

                    @Override
                    public void onFailure(HttpException e, String s) {
                        Toast.makeText(RegisterActivity.this, "网络出错", Toast.LENGTH_SHORT).show();
                        setUIEnable(true);
                    }
                });
    }

    // 登录界面控件的控制
    private void setUIEnable(boolean b){
        setProgressBarIndeterminateVisibility(!b);
        emailView.setEnabled(b);
        unameView.setEnabled(b);
        passwordView.setEnabled(b);
        confirmView.setEnabled(b);
        register.setEnabled(b);
    }


    @Override
    public void onBackPressed() {
        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
