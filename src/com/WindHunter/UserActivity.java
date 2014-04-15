package com.WindHunter;


import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.WH.xListView.XListView;
import com.WindHunter.tools.WHActivity;
import com.WindHunter.tools.WeiboList;
import com.beardedhen.androidbootstrap.BootstrapButton;
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

public class UserActivity extends WHActivity {

    @ViewInject(R.id.user_avatar)
    ImageView user_avatar;

    @ViewInject(R.id.user_name)
    TextView user_name;

    @ViewInject(R.id.user_sex)
    TextView user_sex;

    @ViewInject(R.id.user_intro)
    TextView user_intro;

    @ViewInject(R.id.user_location)
    TextView user_location;

    @ViewInject(R.id.user_weibo_count)
    TextView user_weibo_count;

    @ViewInject(R.id.user_following_count)
    TextView user_following_count;

    @ViewInject(R.id.user_follower_count)
    TextView user_follower_count;

    @ViewInject(R.id.user_checkin)
    BootstrapButton user_checkin;

    @ViewInject(R.id.user_follow)
    BootstrapButton user_follow;

    // 用户id
    private String user_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user);

        // 注入此Activity
        ViewUtils.inject(this);

        // 获取需要绘制的用户id
        user_id = getIntent().getStringExtra("user_id");


        // 绘制个人信息部分
        makeUserUI(this);


        drawButton(this);

    }


    private void makeUserUI(final Context context){
        // 组装个人信息API 请求参数
        String userShowApi = "http://" + host + "index.php?app=api&mod=User&act=show";
        RequestParams requestParams = new RequestParams();
        requestParams.addQueryStringParameter("user_id", user_id);
        requestParams.addQueryStringParameter("oauth_token", oauth_token);
        requestParams.addQueryStringParameter("oauth_token_secret", oauth_token_secret);

        httpUtils.send(HttpRequest.HttpMethod.GET,
                userShowApi,
                requestParams,
                new RequestCallBack<String>(){

                    @Override
                    public void onSuccess(ResponseInfo<String> stringResponseInfo) {
                        try {
                            JSONObject user = new JSONObject(stringResponseInfo.result);

                            bitmapUtils.display(user_avatar, user.getString("avatar_middle"));
                            user_name.setText(user.getString("uname"));
                            user_sex.setText(user.getString("sex"));
                            user_location.setText(user.getString("location"));
                            user_weibo_count.setText(user.getJSONObject("count_info").getInt("weibo_count") + "");
                            user_following_count.setText(user.getJSONObject("count_info").getInt("following_count") + "");
                            user_follower_count.setText(user.getJSONObject("count_info").getInt("follower_count") + "");
                            user_intro.setText(user.getString("intro"));
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

    private void drawButton(final Context context){
        if (uid.equals(user_id)){
            // 自己的主页

            user_follow.setVisibility(View.GONE);
            user_checkin.setVisibility(View.VISIBLE);

            // 组装签到信息API 请求参数
            String checkinfoApi = "http://" + host + "index.php?app=api&mod=Checkin&act=get_check_info";
            RequestParams requestParams = new RequestParams();
            requestParams.addQueryStringParameter("oauth_token", oauth_token);
            requestParams.addQueryStringParameter("oauth_token_secret", oauth_token_secret);

            httpUtils.send(HttpRequest.HttpMethod.GET,
                    checkinfoApi,
                    requestParams,
                    new RequestCallBack<String>(){

                        @Override
                        public void onSuccess(ResponseInfo<String> stringResponseInfo) {
                            try {
                                JSONObject jsonObject = new JSONObject(stringResponseInfo.result);
                                if (jsonObject.getBoolean("ischeck")){
                                    user_checkin.setEnabled(false);
                                    user_checkin.setBootstrapType("success");
                                    user_checkin.setLeftIcon("fa-check");
                                    user_checkin.setText("已签到");
                                }else {
                                    user_checkin.setEnabled(true);
                                    user_checkin.setBootstrapType("warning");
                                    user_checkin.setLeftIcon("fa-calendar");
                                    user_checkin.setText("点击签到");
                                }
                            } catch (JSONException e) {
                                Toast.makeText(context, "网络出错", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(HttpException e, String s) {
                            Toast.makeText(context, "网络出错", Toast.LENGTH_SHORT).show();
                        }
                    });
        }else{
            // 别人的主页
            user_follow.setVisibility(View.VISIBLE);
            user_checkin.setVisibility(View.GONE);

            // 组装个人信息API 请求参数
            String UserInfoApi = "http://" + host + "index.php?app=api&mod=User&act=show";
            RequestParams requestParams = new RequestParams();
            requestParams.addQueryStringParameter("user_id", user_id);
            requestParams.addQueryStringParameter("oauth_token", oauth_token);
            requestParams.addQueryStringParameter("oauth_token_secret", oauth_token_secret);

            httpUtils.send(HttpRequest.HttpMethod.GET,
                    UserInfoApi,
                    requestParams,
                    new RequestCallBack<String>() {
                        @Override
                        public void onSuccess(ResponseInfo<String> responseInfo) {
                            try {
                                JSONObject jsonObject = new JSONObject(responseInfo.result);
                                if (jsonObject.getJSONObject("follow_state").getInt("following") == 0){
                                    user_follow.setEnabled(true);
                                    user_follow.setBootstrapType("primary");
                                    user_follow.setLeftIcon("fa-plus");
                                    user_follow.setText("加关注");
                                }else{
                                    user_follow.setEnabled(false);
                                    user_follow.setBootstrapType("success");
                                    if (jsonObject.getJSONObject("follow_state").getInt("follower") == 0){
                                        user_follow.setText("已关注");
                                        user_follow.setLeftIcon("fa-check");
                                    }else{
                                        user_follow.setText("互相关注");
                                        user_follow.setLeftIcon("fa-exchange");
                                    }
                                }
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
    }

    @OnClick(R.id.user_checkin)
    public void checkInClick(View view){
        // 组装签到信息API 请求参数
        String checkinfoApi = "http://" + host + "index.php?app=api&mod=Checkin&act=checkin";
        RequestParams requestParams = new RequestParams();
        requestParams.addQueryStringParameter("oauth_token", oauth_token);
        requestParams.addQueryStringParameter("oauth_token_secret", oauth_token_secret);

        httpUtils.send(HttpRequest.HttpMethod.GET,
                checkinfoApi,
                requestParams,
                new RequestCallBack<String>() {
                    @Override
                    public void onSuccess(ResponseInfo<String> responseInfo) {
                        user_checkin.setEnabled(false);
                        user_checkin.setBootstrapType("success");
                        user_checkin.setLeftIcon("fa-check");
                        user_checkin.setText("已签到");

                        try {
                            JSONObject jsonObject = new JSONObject(responseInfo.result);
                            String msg = "已连续签到" + jsonObject.getString("con_num") + "天，共签到" + jsonObject.getString("total_num") + "天";
                            Toast.makeText(UserActivity.this, msg, Toast.LENGTH_LONG).show();
                        } catch (JSONException e) {
                            Toast.makeText(UserActivity.this, "网络出错", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(HttpException e, String s) {
                        Toast.makeText(UserActivity.this, "网络出错", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
