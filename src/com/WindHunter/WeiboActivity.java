package com.WindHunter;


import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.*;
import android.widget.*;
import com.WindHunter.tools.WHActivity;
import com.WindHunter.tools.WeiboList;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class WeiboActivity extends WHActivity {

    @ViewInject(R.id.weibo_user)
    LinearLayout weibo_user;

    @ViewInject(R.id.weibo_user_avatar)
    ImageView weibo_user_avatar;

    @ViewInject(R.id.weibo_user_name)
    TextView weibo_user_name;

    @ViewInject(R.id.weibo_repost)
    LinearLayout weibo_repost;

    @ViewInject(R.id.weibo_ctime)
    TextView weibo_ctime;

    @ViewInject(R.id.weibo_content)
    TextView weibo_content;

    @ViewInject(R.id.weibo_from)
    TextView weibo_from;

    @ViewInject(R.id.weibo_num)
    TextView weibo_num;

    @ViewInject(R.id.weibo_img)
    LinearLayout weibo_img;

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
                            JSONObject weibo = new JSONObject(responseInfo.result);

                            user_id = weibo.getString("uid");
                            weibo_user_name.setText(weibo.getString("uname"));
                            bitmapUtils.display(weibo_user_avatar, weibo.getString("avatar_middle"));

                            weibo_ctime.setText(weibo.getString("ctime"));

                            if (weibo.isNull("feed_content"))
                                weibo_content.setText("");
                            else
                                weibo_content.setText(weibo.getString("feed_content"));

                            weibo_num.setText("赞(" +
                                    weibo.getString("digg_count") +
                                    ") | 转发(" + weibo.getString("repost_count") +
                                    ") | 评论(" + weibo.getString("comment_count") +
                                    ")");

                            weibo_from.setText(WeiboList.switchFromCode(weibo.getString("from")));


                            addImageToLayout(context, weibo, weibo_img, 3);

                            addRepostToLayout(context, weibo, weibo_repost);


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

    private void addImageToLayout(Context context, JSONObject weibo, LinearLayout layout, int maxRow) throws JSONException {
        if (weibo.getString("feedType").equals("postimage")){
            JSONArray attaches = weibo.getJSONArray("attach");

            if (attaches.length() != 0){
                LinearLayout imgBox;
                ImageView img;

                int row = attaches.length() / maxRow;
                int single = attaches.length() % maxRow;
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                layoutParams.setMargins(5,5,5,5);

                // 整行的
                for (int i = 0; i < row; i++){
                    imgBox = new LinearLayout(context);
                    for (int j = 0; j < maxRow; j++){
                        img = new ImageView(context);
                        imgBox.addView(img,layoutParams);
                        bitmapUtils.display(img, attaches.getJSONObject(i * maxRow + j).getString("attach_small"));
                    }
                    layout.addView(imgBox);
                }

                // 单个的
                imgBox = new LinearLayout(context);
                for (int i = 0; i < single; i++){
                    img = new ImageView(context);
                    imgBox.addView(img,layoutParams);
                    bitmapUtils.display(img, attaches.getJSONObject(row * maxRow + i).getString("attach_small"));
                }
                layout.addView(imgBox);
            }
        }
    }

    private void addRepostToLayout(final Context context, JSONObject weibo, LinearLayout layout) throws JSONException {
        if (weibo.getString("feedType").equals("repost")){
            final JSONObject repost = weibo.getJSONObject("transpond_data");

            if (repost.getString("is_del").equals("1")){
                // 原文内容已删除
                TextView delMsg = new TextView(context);
                layout.addView(delMsg);
                delMsg.setText("内容已被删除");
                delMsg.setGravity(Gravity.CENTER);
                delMsg.setHeight(100);
                delMsg.setBackgroundResource(R.drawable.shadow_bg);
            }else{
                View repostView = LayoutInflater.from(context).inflate(R.layout.weibo_repost, null);
                layout.addView(repostView);

                // 点击名字跳转
                final TextView unameView = (TextView)repostView.findViewById(R.id.weibo_repost_uname);
                unameView.setText("@" + repost.getString("uname"));
                unameView.setTextColor(Color.BLUE);
                final String repostUid = repost.getString("uid");
                unameView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(context, UserActivity.class);
                        intent.putExtra("user_id", repostUid);
                        context.startActivity(intent);
                    }
                });

                ((TextView)repostView.findViewById(R.id.weibo_repost_ctime))
                        .setText(repost.getString("ctime"));

                // 空内容
                TextView feedContentView = (TextView)repostView.findViewById(R.id.weibo_repost_content);
                if (repost.isNull("feed_content"))
                    feedContentView.setText("");
                else
                    feedContentView.setText(repost.getString("feed_content"));

                ((TextView)repostView.findViewById(R.id.weibo_repost_from))
                        .setText(WeiboList.switchFromCode(repost.getString("from")));

                ((TextView)repostView.findViewById(R.id.weibo_repost_num))
                        .setText("转发(" + repost.getString("repost_count") +
                                    ") | 评论(" + repost.getString("comment_count") +
                                    ")");

                LinearLayout imgLayout = (LinearLayout)repostView.findViewById(R.id.weibo_repost_img);
                addImageToLayout(context, repost, imgLayout, 3);
            }
        }
    }
}
