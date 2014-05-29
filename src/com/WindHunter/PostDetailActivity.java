package com.WindHunter;


import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.*;
import android.widget.*;
import com.WindHunter.tools.FaceUtils;
import com.WindHunter.tools.WeibaBaseActivity;
import com.beardedhen.androidbootstrap.BootstrapButton;
import com.beardedhen.androidbootstrap.BootstrapEditText;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class PostDetailActivity extends WeibaBaseActivity {

    private String post_id;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setTitle("帖子详情");

        menu.add("comment").setIcon(R.drawable.face).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        menu.add("favorite").setIcon(R.drawable.face).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getTitle().equals("comment")){
            // 评论帖子
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("评论帖子");
            View commentView =  LayoutInflater.from(this).inflate(R.layout.post_message, null);

            builder.setView(commentView);

            final BootstrapEditText contentView = (BootstrapEditText)commentView.findViewById(R.id.post_message_content);
            BootstrapButton submit = (BootstrapButton)commentView.findViewById(R.id.post_message_submit);
            ImageView face = (ImageView)commentView.findViewById(R.id.post_message_face);

            final AlertDialog dialog = builder.create();
            dialog.show();

            face.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    FaceUtils.getFaceToEdit(PostDetailActivity.this, contentView);
                }
            });

            submit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String content = contentView.getText().toString();

                    if (content.isEmpty()){
                        Toast.makeText(PostDetailActivity.this, "还没有输入内容", Toast.LENGTH_SHORT).show();
                    }else{
                        // 组装API 请求参数
                        String postMessageApi = "http://" + host + "index.php?app=api&mod=Weiba&act=comment_post";
                        RequestParams requestParams = new RequestParams();
                        requestParams.addQueryStringParameter("id", post_id);
                        requestParams.addQueryStringParameter("user_id", uid);
                        requestParams.addBodyParameter("content", content);
                        requestParams.addQueryStringParameter("oauth_token", oauth_token);
                        requestParams.addQueryStringParameter("oauth_token_secret", oauth_token_secret);

                        httpUtils.send(HttpRequest.HttpMethod.POST,
                                postMessageApi,
                                requestParams,
                                new RequestCallBack<String>() {
                                    @Override
                                    public void onSuccess(ResponseInfo<String> responseInfo) {
                                        String state = responseInfo.result;

                                        if (state.equals("0")){
                                            Toast.makeText(PostDetailActivity.this, "评论失败", Toast.LENGTH_SHORT).show();
                                        }else{
                                            Toast.makeText(PostDetailActivity.this, "评论成功", Toast.LENGTH_SHORT).show();
                                            dialog.dismiss();

                                            Intent intent = new Intent(PostDetailActivity.this, PostDetailActivity.class);
                                            intent.putExtra("post_id", post_id);
                                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                            startActivity(intent);
                                        }
                                    }

                                    @Override
                                    public void onFailure(HttpException e, String s) {
                                        Toast.makeText(PostDetailActivity.this, "网络出错", Toast.LENGTH_SHORT).show();
                                        Log.e("net error", e.toString() + s);
                                    }
                                });
                    }
                }
            });
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.post_detail);

        post_id = getIntent().getStringExtra("post_id");

        initDetail(this);

        initComment(this);
    }

    private void initDetail(final WeibaBaseActivity context){
        String api = "http://" + host + "index.php?app=api&mod=Weiba&act=post_detail";
        RequestParams requestParams = new RequestParams();
        requestParams.addQueryStringParameter("id", post_id);
        requestParams.addQueryStringParameter("user_id", uid);
        requestParams.addQueryStringParameter("oauth_token", oauth_token);
        requestParams.addQueryStringParameter("oauth_token_secret", oauth_token_secret);

        httpUtils.send(HttpRequest.HttpMethod.GET,
                api,
                requestParams,
                new RequestCallBack<String>() {
                    @Override
                    public void onSuccess(ResponseInfo<String> responseInfo) {
                        try {
                            JSONObject jsonObject = new JSONObject(responseInfo.result);
                            drawDetail(context, jsonObject);
                        } catch (JSONException e) {
                            Toast.makeText(context, "网络异常", Toast.LENGTH_SHORT).show();
                            Log.e("json error", e.toString());
                        }
                    }

                    @Override
                    public void onFailure(HttpException e, String s) {
                        Toast.makeText(context, "网络异常", Toast.LENGTH_SHORT).show();
                        Log.e("net error", e.toString() + s);
                    }
                });

    }

    private void drawDetail(WeibaBaseActivity context, JSONObject jsonObject) throws JSONException {
        TextView titleView = (TextView)findViewById(R.id.post_detail_title);
        TextView countView = (TextView)findViewById(R.id.post_detail_count);
        TextView timeView = (TextView)findViewById(R.id.post_detail_time);
        TextView contentView = (TextView)findViewById(R.id.post_detail_content);

        String title = jsonObject.getString("title");
        String reply_count = jsonObject.getString("reply_count");
        String read_count = jsonObject.getString("read_count");
        String time = jsonObject.getString("post_time");
        String content = jsonObject.getString("content");

        titleView.setText(title);
        countView.setText("浏览数: " + read_count + " | 评论数: " + reply_count);
        timeView.setText(context.getTimeFromPHP(time));

        content = content.replaceAll("<img.+?src=.+?/>", "");
        contentView.setText(Html.fromHtml(content));
    }

    private void initComment(final WeibaBaseActivity context){

        final int count = 10;
        final int[] page = {1};

        String api = "http://" + host + "index.php?app=api&mod=Weiba&act=comment_list";
        RequestParams requestParams = new RequestParams();
        requestParams.addQueryStringParameter("id", post_id);
        requestParams.addQueryStringParameter("user_id", uid);
        requestParams.addQueryStringParameter("count", count + "");
        requestParams.addQueryStringParameter("page", page[0] + "");
        requestParams.addQueryStringParameter("oauth_token", oauth_token);
        requestParams.addQueryStringParameter("oauth_token_secret", oauth_token_secret);

        httpUtils.send(HttpRequest.HttpMethod.GET,
                api,
                requestParams,
                new RequestCallBack<String>() {
                    @Override
                    public void onSuccess(ResponseInfo<String> responseInfo) {
                        try {
                            JSONArray jsonArray = new JSONArray(responseInfo.result);
                            LinearLayout layout = (LinearLayout)findViewById(R.id.post_detail_comment);

                            if (jsonArray.length() == 0){
                                TextView textView = new TextView(context);
                                textView.setText("没有评论");
                                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                layoutParams.gravity = Gravity.CENTER;
                                layout.addView(textView, layoutParams);
                            }else {
                                for (int i = 0; i < jsonArray.length(); i++){
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                    layoutParams.setMargins(0,0,0,8);
                                    layout.addView(buildComment(context, jsonObject), layoutParams);
                                }
                            }
                        } catch (JSONException e) {
                            Toast.makeText(context, "网络异常", Toast.LENGTH_SHORT).show();
                            Log.e("net error", e.toString());
                        }
                    }

                    @Override
                    public void onFailure(HttpException e, String s) {
                        Toast.makeText(context, "网络异常", Toast.LENGTH_SHORT).show();
                        Log.e("net error", e.toString() + s);
                    }
                });

        final ScrollView scrollView = (ScrollView)findViewById(R.id.post_detail_scroll);
        scrollView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        break;
                    case MotionEvent.ACTION_UP:
                        if (scrollView.getChildAt(0).getMeasuredHeight() <= view.getHeight() + view.getScrollY()) {
                            Log.d("scroll view", "bottom");
                            Log.d("scroll view", page[0] + "");

                            page[0]++;

                            // 组装 微博评论API 请求参数
                            String weiboCommentsApi = "http://" + host + "index.php?app=api&mod=Weiba&act=comment_list";
                            RequestParams requestParams = new RequestParams();
                            requestParams.addQueryStringParameter("id", post_id);
                            requestParams.addQueryStringParameter("user_id", uid);
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
                                                    LinearLayout layout = (LinearLayout)findViewById(R.id.post_detail_comment);

                                                    for (int i = 0; i < jsonArray.length(); i++){
                                                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                                                        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                                        layoutParams.setMargins(0,0,0,8);
                                                        layout.addView(buildComment(context, jsonObject), layoutParams);
                                                    }
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

    private View buildComment(WeibaBaseActivity context, JSONObject jsonObject) throws JSONException {
        View view = LayoutInflater.from(context).inflate(R.layout.weibo_comments_item, null);

        ImageView avatarView = (ImageView)view.findViewById(R.id.weibo_comments_item_avatar);
        TextView nameView = (TextView)view.findViewById(R.id.weibo_comments_item_name);
        TextView timeView = (TextView)view.findViewById(R.id.weibo_comments_item_time);
        TextView contentView = (TextView)view.findViewById(R.id.weibo_comments_item_content);

        String avatar = jsonObject.getJSONObject("author_info").getString("avatar_middle");
        String name = jsonObject.getJSONObject("author_info").getString("uname");
        String time = jsonObject.getString("ctime");
        String content = jsonObject.getString("content");

        bitmapUtils.display(avatarView, avatar);
        nameView.setText(name);
        timeView.setText(WeibaBaseActivity.getTimeFromPHP(time));
        contentView.setText(FaceUtils.getExpressionString(context, content));

        return view;
    }
}
