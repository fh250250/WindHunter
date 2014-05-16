package com.WindHunter;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.*;
import com.WindHunter.tools.FaceUtils;
import com.WindHunter.tools.WHActivity;
import com.WindHunter.tools.WeiboList;
import com.beardedhen.androidbootstrap.BootstrapButton;
import com.beardedhen.androidbootstrap.BootstrapEditText;
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

    @ViewInject(R.id.weibo_comments)
    LinearLayout weibo_comments;

    @ViewInject(R.id.weibo_scroll_view)
    ScrollView weibo_scroll_view;


    private final int RIGHT_SCROLL_DELAY = 300;
    private String feed_id;
    private String user_id;
    private boolean is_coll;
    private GestureDetector gestureDetector;

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

        drawCommentsView(this);

        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener(){
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if ((e2.getX() - e1.getX()) > RIGHT_SCROLL_DELAY)
                    finish();

                return true;
            }
        });

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
                                weibo_content.setText(FaceUtils.getExpressionString(context, weibo.getString("feed_content")));

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

    private void drawCommentsView(final Context context){

        final int count = 10;
        final int[] page = {1};

        // 组装 微博评论API 请求参数
        String weiboCommentsApi = "http://" + host + "index.php?app=api&mod=WeiboStatuses&act=comments";
        RequestParams requestParams = new RequestParams();
        requestParams.addQueryStringParameter("id", feed_id);
        requestParams.addQueryStringParameter("count", count + "");
        requestParams.addQueryStringParameter("page", page[0] + "");
        requestParams.addQueryStringParameter("oauth_token", oauth_token);
        requestParams.addQueryStringParameter("oauth_token_secret", oauth_token_secret);

        httpUtils.send(HttpRequest.HttpMethod.GET,
                weiboCommentsApi,
                requestParams,
                new RequestCallBack<String>() {
                    @Override
                    public void onSuccess(ResponseInfo<String> responseInfo) {
                        try {
                            JSONArray jsonArray = new JSONArray(responseInfo.result);

                            if (jsonArray.length() == 0){
                                // 没有评论
                                TextView noComments = new TextView(context);
                                noComments.setText("还没有评论");

                                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                layoutParams.gravity = Gravity.CENTER;
                                layoutParams.setMargins(0,20,0,0);
                                weibo_comments.addView(noComments,layoutParams);
                            }else{
                                addCommentsToLayout(context, jsonArray, weibo_comments);
                            }
                        } catch (JSONException e) {
                            Toast.makeText(context, "网络出错", Toast.LENGTH_SHORT).show();
                            Log.e("jsonParseError", e.toString());
                        }
                    }

                    @Override
                    public void onFailure(HttpException e, String s) {
                        Toast.makeText(context, "网络出错", Toast.LENGTH_SHORT).show();
                        Log.e("netError", e.toString());
                    }
                });


        weibo_scroll_view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        break;
                    case MotionEvent.ACTION_UP:
                        if (weibo_scroll_view.getChildAt(0).getMeasuredHeight() <= v.getHeight() + v.getScrollY()) {
                            Log.d("scroll view", "bottom");
                            Log.d("scroll view", page[0] + "");

                            page[0]++;

                            // 组装 微博评论API 请求参数
                            String weiboCommentsApi = "http://" + host + "index.php?app=api&mod=WeiboStatuses&act=comments";
                            RequestParams requestParams = new RequestParams();
                            requestParams.addQueryStringParameter("id", feed_id);
                            requestParams.addQueryStringParameter("count", count + "");
                            requestParams.addQueryStringParameter("page", page[0] + "");
                            requestParams.addQueryStringParameter("oauth_token", oauth_token);
                            requestParams.addQueryStringParameter("oauth_token_secret", oauth_token_secret);

                            httpUtils.send(HttpRequest.HttpMethod.GET,
                                    weiboCommentsApi,
                                    requestParams,
                                    new RequestCallBack<String>() {
                                        @Override
                                        public void onSuccess(ResponseInfo<String> responseInfo) {
                                            try {
                                                JSONArray jsonArray = new JSONArray(responseInfo.result);

                                                if (jsonArray.length() == 0){
                                                    page[0]--;
                                                }else{
                                                    addCommentsToLayout(context, jsonArray, weibo_comments);
                                                }
                                            } catch (JSONException e) {
                                                page[0]--;
                                            }
                                        }

                                        @Override
                                        public void onFailure(HttpException e, String s) {
                                            page[0] --;
                                        }
                                    });
                        }
                        break;
                    default:
                        break;
                }

                return false;
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


                final TextView unameView = (TextView)repostView.findViewById(R.id.weibo_repost_uname);
                unameView.setText("@" + repost.getString("uname"));


                ((TextView)repostView.findViewById(R.id.weibo_repost_ctime))
                        .setText(repost.getString("ctime"));

                // 空内容
                TextView feedContentView = (TextView)repostView.findViewById(R.id.weibo_repost_content);
                if (repost.isNull("feed_content"))
                    feedContentView.setText("");
                else
                    feedContentView.setText(FaceUtils.getExpressionString(context, repost.getString("feed_content")));

                ((TextView)repostView.findViewById(R.id.weibo_repost_from))
                        .setText(WeiboList.switchFromCode(repost.getString("from")));

                ((TextView)repostView.findViewById(R.id.weibo_repost_num))
                        .setText("转发(" + repost.getString("repost_count") +
                                    ") | 评论(" + repost.getString("comment_count") +
                                    ")");

                LinearLayout imgLayout = (LinearLayout)repostView.findViewById(R.id.weibo_repost_img);
                addImageToLayout(context, repost, imgLayout, 3);


                final String repost_id = repost.getString("feed_id");
                layout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(WeiboActivity.this, WeiboActivity.class);
                        intent.putExtra("feed_id", repost_id);
                        startActivity(intent);
                    }
                });
            }
        }
    }

    private void addCommentsToLayout(Context context, JSONArray jsonArray, LinearLayout weibo_comments) throws JSONException {

        for (int i = 0; i < jsonArray.length(); i++){
            JSONObject comment = jsonArray.getJSONObject(i);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(0,0,0,8);
            weibo_comments.addView(buildCommentBox(context, comment),layoutParams);
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

        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
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
            }
        });

        builder.setNegativeButton("不,我再想想", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        builder.create().show();
    }

    @OnClick(R.id.comment)
    public void commentClick(View view){
        Intent intent = new Intent(this, CommentActivity.class);
        intent.putExtra("feed_id", feed_id);
        startActivity(intent);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        gestureDetector.onTouchEvent(ev);
        return super.dispatchTouchEvent(ev);
    }

    private void deleteComment(final Context context, final String comment_id, BootstrapButton delete, final View commentBox, final LinearLayout weibo_comments){
        delete.setVisibility(View.VISIBLE);

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("确定删除此评论？");

                builder.setPositiveButton("是的", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // 组装 删除评论API 请求参数
                        String deleteCommentApi = "http://" + host + "index.php?app=api&mod=WeiboStatuses&act=comment_destroy";
                        RequestParams requestParams = new RequestParams();
                        requestParams.addQueryStringParameter("comment_id", comment_id);
                        requestParams.addQueryStringParameter("oauth_token", oauth_token);
                        requestParams.addQueryStringParameter("oauth_token_secret", oauth_token_secret);

                        httpUtils.send(HttpRequest.HttpMethod.GET,
                                deleteCommentApi,
                                requestParams,
                                new RequestCallBack<String>() {
                                    @Override
                                    public void onSuccess(ResponseInfo<String> responseInfo) {
                                        String state = responseInfo.result;

                                        if (state.equals("1")){
                                            // 删除评论成功
                                            weibo_comments.removeView(commentBox);
                                        }else{
                                            // 删除评论失败
                                            Toast.makeText(context, "删除评论失败", Toast.LENGTH_SHORT).show();
                                        }
                                    }

                                    @Override
                                    public void onFailure(HttpException e, String s) {
                                        Toast.makeText(context, "网络出错", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                });

                builder.setNegativeButton("不，我再想想", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });

                builder.create().show();
            }
        });
    }

    private void setReplyCommentListener(final Context context, JSONObject comment, final String row_id, View contentTextView) throws JSONException {
        final String name = comment.getJSONObject("user_info").getString("uname");
        final String to_comment_id = comment.getString("comment_id");
        final String to_uid = comment.getJSONObject("user_info").getString("uid");

        contentTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("回复: " + name);

                View replyView =  LayoutInflater.from(context).inflate(R.layout.post_message, null);

                builder.setView(replyView);

                final BootstrapEditText contentView = (BootstrapEditText)replyView.findViewById(R.id.post_message_content);
                BootstrapButton submit = (BootstrapButton)replyView.findViewById(R.id.post_message_submit);
                ImageView face = (ImageView)replyView.findViewById(R.id.post_message_face);

                submit.setText("回复");
                final AlertDialog dialog = builder.create();
                dialog.show();

                face.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        FaceUtils.getFaceToEdit(context, contentView);
                    }
                });

                submit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // 具体回复逻辑
                        String content = contentView.getText().toString();
                        if (content.isEmpty()){
                            Toast.makeText(context, "还没有输入内容", Toast.LENGTH_SHORT).show();
                        }else {
                            // 组装 回复评论API 请求参数
                            String replyCommentApi = "http://" + host + "index.php?app=api&mod=WeiboStatuses&act=comment";
                            RequestParams requestParams = new RequestParams();
                            requestParams.addQueryStringParameter("row_id", row_id);
                            requestParams.addQueryStringParameter("to_comment_id", to_comment_id);
                            requestParams.addQueryStringParameter("to_uid", to_uid);
                            requestParams.addBodyParameter("content", "回复@" + name + " ：" + content);
                            requestParams.addQueryStringParameter("oauth_token", oauth_token);
                            requestParams.addQueryStringParameter("oauth_token_secret", oauth_token_secret);

                            httpUtils.send(HttpRequest.HttpMethod.POST,
                                    replyCommentApi,
                                    requestParams,
                                    new RequestCallBack<String>() {
                                        @Override
                                        public void onSuccess(ResponseInfo<String> responseInfo) {
                                            String state = responseInfo.result;

                                            if (state.equals("1")){
                                                // 回复成功
                                                Toast.makeText(context, "回复成功", Toast.LENGTH_SHORT).show();

                                                Intent intent = new Intent(context, context.getClass());
                                                intent.putExtra("feed_id", feed_id);
                                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                startActivity(intent);
                                            }else{
                                                // 回复失败
                                                Toast.makeText(context, "回复失败", Toast.LENGTH_SHORT).show();
                                            }
                                        }

                                        @Override
                                        public void onFailure(HttpException e, String s) {
                                            Toast.makeText(context, "网络出错", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    }
                });
            }
        });
    }

    private View buildCommentBox(Context context, JSONObject comment) throws JSONException {
        JSONObject user_info = comment.getJSONObject("user_info");

        View commentBox = LayoutInflater.from(context).inflate(R.layout.weibo_comments_item, null);
        ImageView avatar = (ImageView)commentBox.findViewById(R.id.weibo_comments_item_avatar);
        TextView name = (TextView)commentBox.findViewById(R.id.weibo_comments_item_name);
        TextView content = (TextView)commentBox.findViewById(R.id.weibo_comments_item_content);
        BootstrapButton delete = (BootstrapButton)commentBox.findViewById(R.id.weibo_comments_item_delete);

        name.setText(user_info.getString("uname"));

        // 去掉content中的a标签
        String rowContent = comment.getString("content");
        String realContent = rowContent.replaceAll("<a href[^>]*>", "");
        realContent = realContent.replaceAll("</a>", "");
        content.setText(FaceUtils.getExpressionString(context, realContent));

        bitmapUtils.display(avatar, user_info.getString("avatar_small"));

        // 删除评论
        if (user_info.getString("uid").equals(uid)){
            deleteComment(context, comment.getString("comment_id"), delete, commentBox, weibo_comments);
        }

        // 点击回复评论
        setReplyCommentListener(context, comment, feed_id, commentBox);

        return commentBox;
    }
}
