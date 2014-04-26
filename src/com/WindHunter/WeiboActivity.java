package com.WindHunter;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.*;
import android.widget.*;
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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class WeiboActivity extends WHActivity {

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

    @ViewInject(R.id.weibo_digg_text)
    TextView weibo_digg_text;

    @ViewInject(R.id.digg)
    LinearLayout digg;

    @ViewInject(R.id.weibo_favorite_text)
    TextView weibo_favorite_text;

    @ViewInject(R.id.weibo_delete)
    BootstrapButton weibo_delete;

    private String feed_id;
    private String user_id;
    private boolean is_coll;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        super.onCreateOptionsMenu(menu);

        title.setText("微博详情");

        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weibo);

        // 注入此Activity
        ViewUtils.inject(this);

        feed_id = getIntent().getStringExtra("feed_id");

        weibo_delete.setVisibility(View.GONE);

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

                            if (weibo.getInt("is_digg") == 1){
                                weibo_digg_text.setText("已赞");
                                digg.setEnabled(false);
                            }else{
                                weibo_digg_text.setText("赞");
                                digg.setEnabled(true);
                            }

                            if (weibo.getJSONObject("iscoll").getInt("colled") == 1){
                                weibo_favorite_text.setText("取消");
                                is_coll = true;
                            }else{
                                weibo_favorite_text.setText("收藏");
                                is_coll = false;
                            }

                            if (uid.equals(user_id)){
                                weibo_delete.setVisibility(View.VISIBLE);
                            }else{
                                weibo_delete.setVisibility(View.GONE);
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


    @OnClick(R.id.weibo_user)
    public void weiboUserClick(View view){
        Intent intent = new Intent(this, UserActivity.class);
        intent.putExtra("user_id", user_id);
        startActivity(intent);
    }

    private void addImageToLayout(final Context context, JSONObject weibo, LinearLayout layout, int maxRow) throws JSONException {
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



                // 看大图
                final ArrayList<String> bigImgUrls = new ArrayList<String>();
                for (int i = 0; i < attaches.length(); i++){
                    bigImgUrls.add(attaches.getJSONObject(i).getString("attach_url"));
                }
                layout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(context, BigImageActivity.class);
                        intent.putStringArrayListExtra("attachUrls", bigImgUrls);
                        context.startActivity(intent);
                    }
                });
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


    //点赞事件
    @OnClick(R.id.digg)
    public void diggClick(View view){
        // 组装 赞API 请求参数
        String diggApi = "http://" + host + "index.php?app=api&mod=WeiboStatuses&act=add_digg";
        RequestParams requestParams = new RequestParams();
        requestParams.addQueryStringParameter("feed_id", feed_id);
        requestParams.addQueryStringParameter("oauth_token", oauth_token);
        requestParams.addQueryStringParameter("oauth_token_secret", oauth_token_secret);

        httpUtils.send(HttpRequest.HttpMethod.GET,
                diggApi,
                requestParams,
                new RequestCallBack<String>() {
                    @Override
                    public void onSuccess(ResponseInfo<String> responseInfo) {
                        String state = responseInfo.result;

                        if (state.equals("1")){
                            Toast.makeText(WeiboActivity.this, "成功", Toast.LENGTH_SHORT).show();
                            weibo_digg_text.setText("已赞");
                            digg.setEnabled(false);
                        }
                        else
                            Toast.makeText(WeiboActivity.this, "失败", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(HttpException e, String s) {
                        Toast.makeText(WeiboActivity.this, "网络出错", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // 点收藏事件
    @OnClick(R.id.favorite)
    public void favoriteClick(View view){

        String type;
        RequestParams requestParams = new RequestParams();

        if (is_coll){
            type = "destroy";
        }else{
            type = "create";
            requestParams.addQueryStringParameter("source_app", "public");
        }

        // 组装 收藏API 请求参数
        String favoriteApi = "http://" + host + "index.php?app=api&mod=WeiboStatuses&act=favorite_" + type;
        requestParams.addQueryStringParameter("source_table_name", "feed");
        requestParams.addQueryStringParameter("source_id", feed_id);
        requestParams.addQueryStringParameter("oauth_token", oauth_token);
        requestParams.addQueryStringParameter("oauth_token_secret", oauth_token_secret);

        httpUtils.send(HttpRequest.HttpMethod.GET,
                favoriteApi,
                requestParams,
                new RequestCallBack<String>() {
                    @Override
                    public void onSuccess(ResponseInfo<String> responseInfo) {
                        String state = responseInfo.result;

                        if (state.equals("1")){

                            if (is_coll){
                                weibo_favorite_text.setText("收藏");
                                is_coll = false;
                                Toast.makeText(WeiboActivity.this, "取消收藏成功", Toast.LENGTH_SHORT).show();
                            }else{
                                weibo_favorite_text.setText("取消");
                                is_coll = true;
                                Toast.makeText(WeiboActivity.this, "收藏成功", Toast.LENGTH_SHORT).show();
                            }
                        }else {
                            Toast.makeText(WeiboActivity.this, "失败", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(HttpException e, String s) {
                        Toast.makeText(WeiboActivity.this, "网络出错", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @OnClick(R.id.repost)
    public void repostClick(View view){
        Intent intent = new Intent(this, RepostActivity.class);
        intent.putExtra("feed_id", feed_id);
        startActivity(intent);
    }

    @OnClick(R.id.weibo_delete)
    public void deleteClick(View view){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("确认删除");
        String[] options = {"确定", "取消"};
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                switch (i){
                    case 0:

                        // 组装 收藏API 请求参数
                        String deleteApi = "http://" + host + "index.php?app=api&mod=WeiboStatuses&act=destroy";
                        RequestParams requestParams = new RequestParams();
                        requestParams.addQueryStringParameter("id", feed_id);
                        requestParams.addQueryStringParameter("oauth_token", oauth_token);
                        requestParams.addQueryStringParameter("oauth_token_secret", oauth_token_secret);

                        httpUtils.send(HttpRequest.HttpMethod.GET,
                                deleteApi,
                                requestParams,
                                new RequestCallBack<String>() {
                                    @Override
                                    public void onSuccess(ResponseInfo<String> responseInfo) {
                                        String state = responseInfo.result;

                                        if (state.equals("1")){
                                            Toast.makeText(WeiboActivity.this, "删除成功", Toast.LENGTH_SHORT).show();
                                            WeiboActivity.this.finish();
                                        }else{
                                            Toast.makeText(WeiboActivity.this, "删除失败", Toast.LENGTH_SHORT).show();
                                        }
                                    }

                                    @Override
                                    public void onFailure(HttpException e, String s) {
                                        Toast.makeText(WeiboActivity.this, "网络出错", Toast.LENGTH_SHORT).show();
                                    }
                                });

                        break;
                    case 1:
                        break;
                    default:
                        break;
                }
            }
        });

        builder.create().show();
    }
}
