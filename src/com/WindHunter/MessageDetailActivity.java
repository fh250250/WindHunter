package com.WindHunter;


import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.*;
import com.WindHunter.tools.FaceUtils;
import com.WindHunter.tools.WHActivity;
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

import android.os.Handler;


public class MessageDetailActivity extends WHActivity {

    private final int RIGHT_SCROLL_DELAY = 300;
    private String list_id;
    private String myAvatar;

    @ViewInject(R.id.message_detail_list)
    LinearLayout message_detail_list;

    @ViewInject(R.id.message_detail_scroll)
    ScrollView message_detail_scroll;

    @ViewInject(R.id.message_detail_reply)
    BootstrapEditText replyView;

    private GestureDetector gestureDetector;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        title.setText("对话详情");

        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.message_detail);

        ViewUtils.inject(this);

        list_id = getIntent().getStringExtra("list_id");

        drawDetailView(this);


        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener(){
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if ((e2.getX() - e1.getX()) > RIGHT_SCROLL_DELAY)
                    finish();

                return true;
            }
        });
    }

    void drawDetailView(final Context context){

        // 组装API
        String messageDetailApi = "http://" + host + "index.php?app=api&mod=Message&act=get_message_detail";
        RequestParams requestParams = new RequestParams();
        requestParams.addQueryStringParameter("id", list_id);
        requestParams.addQueryStringParameter("oauth_token", oauth_token);
        requestParams.addQueryStringParameter("oauth_token_secret", oauth_token_secret);

        httpUtils.send(HttpRequest.HttpMethod.GET,
                messageDetailApi,
                requestParams,
                new RequestCallBack<String>() {
                    @Override
                    public void onSuccess(ResponseInfo<String> responseInfo) {
                        try {
                            JSONArray jsonArray = new JSONArray(responseInfo.result);

                            addDetailToLayout(context, jsonArray, message_detail_list);
                        } catch (JSONException e) {
                            Toast.makeText(context, "网络出错", Toast.LENGTH_SHORT).show();
                            Log.e("json", e.toString());
                        }
                    }

                    @Override
                    public void onFailure(HttpException e, String s) {
                        Toast.makeText(context, "网络出错", Toast.LENGTH_SHORT).show();
                        Log.e("net", e.toString() + s);
                    }
                });

    }

    private void addDetailToLayout(Context context, JSONArray jsonArray, LinearLayout layout) throws JSONException {

        for (int i = jsonArray.length() - 1; i >= 0; i--){
            JSONObject item = jsonArray.getJSONObject(i);

            String from_uid = item.getString("from_uid");
            String content = item.getString("content");
            String from_face = item.getString("from_face");
            String ctime = item.getString("ctime");

            View view;

            if (uid.equals(from_uid)){
                view  = LayoutInflater.from(context).inflate(R.layout.message_detail_item_right, null);
                myAvatar = from_face;
            }else{
                view  = LayoutInflater.from(context).inflate(R.layout.message_detail_item_left, null);
            }

            ImageView avatarView = (ImageView)view.findViewById(R.id.message_detail_item_avatar);
            TextView  contentView = (TextView)view.findViewById(R.id.message_detail_item_content);
            TextView ctimeView = (TextView)view.findViewById(R.id.message_detail_item_time);


            bitmapUtils.display(avatarView, from_face);
            contentView.setText(FaceUtils.getExpressionString(context, content));
            ctimeView.setText(ctime.substring(5, 16));


            layout.addView(view);
        }

        scrollToBottom(message_detail_scroll, message_detail_list);
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

    private void scrollToBottom(final ScrollView scroll, final View inner) {
        Handler mHandler = new Handler();

        mHandler.post(new Runnable() {
            public void run() {
                if (scroll == null || inner == null) {
                    return;
                }

                int offset = inner.getMeasuredHeight() - scroll.getHeight();
                if (offset < 0) {
                    offset = 0;
                }

                scroll.smoothScrollTo(0, offset);
            }
        });
    }

    @OnClick(R.id.message_detail_submit)
    public void submit(View view){
        final String content = replyView.getText().toString();

        if (content.isEmpty()){
            Toast.makeText(this, "还没有输入任何内容", Toast.LENGTH_SHORT).show();
        }else{
            // 组装API
            String messageReplyApi = "http://" + host + "index.php?app=api&mod=Message&act=reply";
            RequestParams requestParams = new RequestParams();
            requestParams.addQueryStringParameter("id", list_id);
            requestParams.addBodyParameter("content", content);
            requestParams.addQueryStringParameter("oauth_token", oauth_token);
            requestParams.addQueryStringParameter("oauth_token_secret", oauth_token_secret);

            httpUtils.send(HttpRequest.HttpMethod.POST,
                    messageReplyApi,
                    requestParams,
                    new RequestCallBack<String>() {
                        @Override
                        public void onSuccess(ResponseInfo<String> responseInfo) {
                            String state = responseInfo.result;

                            if (state.equals("false")){
                                Toast.makeText(MessageDetailActivity.this, "回复失败", Toast.LENGTH_SHORT).show();
                            }else{
                                Toast.makeText(MessageDetailActivity.this, "回复成功", Toast.LENGTH_SHORT).show();

                                View message = LayoutInflater.from(MessageDetailActivity.this).inflate(R.layout.message_detail_item_right, null);

                                ImageView avatar = (ImageView)message.findViewById(R.id.message_detail_item_avatar);
                                TextView contentView = (TextView)message.findViewById(R.id.message_detail_item_content);

                                bitmapUtils.display(avatar, myAvatar);
                                contentView.setText(FaceUtils.getExpressionString(MessageDetailActivity.this,content));

                                message_detail_list.addView(message);
                                scrollToBottom(message_detail_scroll, message_detail_list);
                                replyView.setText("");
                            }
                        }

                        @Override
                        public void onFailure(HttpException e, String s) {
                            Toast.makeText(MessageDetailActivity.this, "网络出错", Toast.LENGTH_SHORT).show();
                            Log.e("net", e.toString() + "\n" + s);
                        }
                    });
        }
    }

    @OnClick(R.id.message_detail_face)
    public void faceClick(View view){
        FaceUtils.getFaceToEdit(this, replyView);
    }
}
