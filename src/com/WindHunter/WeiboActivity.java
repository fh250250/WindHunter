package com.WindHunter;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.View;
import android.widget.*;
import com.WindHunter.tools.WHActivity;
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

public class WeiboActivity extends WHActivity {

    @ViewInject(R.id.weibo_user)
    Button weibo_user;

    @ViewInject(R.id.weibo_user_avatar)
    ImageView weibo_user_avatar;

    @ViewInject(R.id.weibo_user_name)
    TextView weibo_user_name;

    private String feed_id;
    private String user_id;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("微博详情");

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weibo);

        // 注入此Activity
        ViewUtils.inject(this);

        feed_id = getIntent().getStringExtra("feed_id");

        drawWeiboView(this);
    }

    private void drawWeiboView(final Context context){
        // 组装关注列表API 请求参数
        String weiboShowApi = "http://" + host + "index.php?app=api&mod=WeiboStatuses&act=show";
        RequestParams requestParams = new RequestParams();
        requestParams.addQueryStringParameter("id", feed_id);
        requestParams.addQueryStringParameter("oauth_token", oauth_token);
        requestParams.addQueryStringParameter("oauth_token_secret", oauth_token_secret);

        httpUtils.send(HttpRequest.HttpMethod.GET,
                weiboShowApi,
                requestParams,
                new RequestCallBack<String>() {
                    @Override
                    public void onSuccess(ResponseInfo<String> responseInfo) {
                        try {
                            JSONObject jsonObject = new JSONObject(responseInfo.result);

                            user_id = jsonObject.getString("uid");
                            weibo_user_name.setText(jsonObject.getString("uname"));
                            bitmapUtils.display(weibo_user_avatar, jsonObject.getString("avatar_middle"));
                        } catch (JSONException e) {
                            Toast.makeText(context, "网络出错", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(HttpException e, String s) {
                        Toast.makeText(context, "网络出错", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    @OnClick(R.id.weibo_user)
    public void weiboUserClick(View view){
        Intent intent = new Intent(this, UserActivity.class);
        intent.putExtra("user_id", user_id);
        startActivity(intent);
    }

}
