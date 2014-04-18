package com.WindHunter;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.lidroid.xutils.view.annotation.ViewInject;

public class PostActivity extends ActionBarActivity {

    private String host, oauth_token, oauth_token_secret, uid;
    private HttpUtils httpUtils;

    @ViewInject(R.id.post_edit_text)
    EditText post_edit_text;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // 默认不显示进度条
        setProgressBarIndeterminateVisibility(false);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        // actionBar.setIcon();
        actionBar.setDisplayShowTitleEnabled(false);

        menu.add("submit")
//                .setIcon()
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // 发微博按钮逻辑
        if (item.getTitle().equals("submit")){
            String content = post_edit_text.getText().toString();

            if (content.isEmpty()){
                Toast.makeText(this, "还没有输入内容哦", Toast.LENGTH_SHORT).show();
                return super.onOptionsItemSelected(item);
            }else{

                // 组装发微博API 请求参数
                String postApi = "http://" + host + "index.php?app=api&mod=WeiboStatuses&act=update";
                RequestParams requestParams = new RequestParams();
                requestParams.addQueryStringParameter("from", "2");
                requestParams.addQueryStringParameter("content", content);
                requestParams.addQueryStringParameter("oauth_token", oauth_token);
                requestParams.addQueryStringParameter("oauth_token_secret", oauth_token_secret);

                httpUtils.send(HttpRequest.HttpMethod.GET,
                        postApi,
                        requestParams,
                        new RequestCallBack<String>() {
                            @Override
                            public void onStart() {
                                // 显示进度条
                                setProgressBarIndeterminateVisibility(true);
                            }

                            @Override
                            public void onSuccess(ResponseInfo<String> responseInfo) {
                                if (responseInfo.result.equals("0")){
                                    Toast.makeText(PostActivity.this, "发表失败", Toast.LENGTH_SHORT).show();
                                }else{
                                    Toast.makeText(PostActivity.this, "发表成功", Toast.LENGTH_SHORT).show();

                                    finish();
                                }

                                // 不显示进度条
                                setProgressBarIndeterminateVisibility(false);
                            }

                            @Override
                            public void onFailure(HttpException e, String s) {
                                Toast.makeText(PostActivity.this, "网络出错", Toast.LENGTH_SHORT).show();
                                // 不显示进度条
                                setProgressBarIndeterminateVisibility(false);
                            }
                        });
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 获取进度条
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        setContentView(R.layout.post);

        ViewUtils.inject(this);


        // 初始化HttpUtils
        httpUtils = new HttpUtils();

        // 从全局对象中获取认证数据
        SharedPreferences settings = getSharedPreferences("settings", MODE_PRIVATE);
        host = settings.getString("Host", "demo.thinksns.com/t3/");
        oauth_token = settings.getString("oauth_token", "");
        oauth_token_secret = settings.getString("oauth_token_secret", "");
        uid = settings.getString("uid", "");
    }
}
