package com.WindHunter;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.*;
import android.net.Uri;
import android.os.Bundle;
import android.view.*;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.WindHunter.tools.WHActivity;
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

    @ViewInject(R.id.user_follow_state)
    TextView user_follow_state;

    @ViewInject(R.id.user_follower_avatar)
    LinearLayout user_follower_avatar;

    @ViewInject(R.id.user_following_avatar)
    LinearLayout user_following_avatar;

    @ViewInject(R.id.user_following_text)
    TextView user_following_text;

    @ViewInject(R.id.user_follower_text)
    TextView user_follower_text;

    @ViewInject(R.id.user_post_message)
    BootstrapButton user_post_message;

    // 用户id
    private String user_id;
    private String post_name;

    //关注状态
    private boolean follow_state;

    private final int REQUEST_IMAGE_CONTENT = 100;
    private final int REQUEST_CAPTURE = 101;

    // 用于存储照相后的照片
    private Uri photoUri = null;


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        if (uid.equals(user_id))
            title.setText("个人主页");
        else
            title.setText("他人主页");

        return true;
    }

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

        addAvatarToLayout(this, "following", user_following_avatar, 5);

        addAvatarToLayout(this, "followers", user_follower_avatar, 5);

    }


    private void makeUserUI(final Context context){

        if (uid.equals(user_id)){
            user_following_text.setText("我的关注");
            user_follower_text.setText("我的粉丝");
            user_post_message.setVisibility(View.GONE);
        }else{
            user_following_text.setText("他的关注");
            user_follower_text.setText("他的粉丝");
            user_post_message.setVisibility(View.VISIBLE);
        }


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
                            post_name = user.getString("uname");
                            user_sex.setText(user.getString("sex"));
                            user_location.setText(user.getString("location"));
                            user_weibo_count.setText(user.getJSONObject("count_info").getInt("weibo_count") + "");
                            user_following_count.setText(user.getJSONObject("count_info").getInt("following_count") + "");
                            user_follower_count.setText(user.getJSONObject("count_info").getInt("follower_count") + "");
                            if (user.isNull("intro"))
                                user_intro.setText("这个人很懒，什么都没写");
                            else
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
            user_follow_state.setVisibility(View.GONE);
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
            user_follow_state.setVisibility(View.VISIBLE);
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
                                    user_follow_state.setText("未关注");
                                    follow_state = false;
                                }else{
                                    user_follow.setEnabled(true);
                                    user_follow.setBootstrapType("danger");
                                    user_follow.setText("取消关注");
                                    user_follow.setLeftIcon("fa-minus");
                                    follow_state = true;
                                    if (jsonObject.getJSONObject("follow_state").getInt("follower") == 0){
                                        user_follow_state.setText("已关注");
                                    }else{
                                        user_follow_state.setText("互相关注");
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

    @OnClick(R.id.user_follow)
    public void followClick(View view){
        // 组装取消关注API 请求参数
        String followApi = "http://" + host + "index.php?app=api&mod=User&act=follow_";
        RequestParams requestParams = new RequestParams();
        requestParams.addQueryStringParameter("user_id", user_id);
        requestParams.addQueryStringParameter("oauth_token", oauth_token);
        requestParams.addQueryStringParameter("oauth_token_secret", oauth_token_secret);
        if(follow_state){
            //取消关注
            httpUtils.send(HttpRequest.HttpMethod.GET,
                    followApi + "destroy",
                    requestParams,
                    new RequestCallBack<String>() {
                        @Override
                        public void onSuccess(ResponseInfo<String> responseInfo) {
                            try {
                                JSONObject jsonObject = new JSONObject(responseInfo.result);
                                if(jsonObject.getInt("following") == 0){
                                    Toast.makeText(UserActivity.this, "取消关注成功", Toast.LENGTH_SHORT).show();
                                    user_follow.setBootstrapType("primary");
                                    user_follow.setText("加关注");
                                    user_follow.setLeftIcon("fa-plus");
                                    follow_state = false;
                                    user_follow_state.setText("未关注");
                                }
                            } catch (JSONException e) {
                                Toast.makeText(UserActivity.this, "取消关注失败", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(HttpException e, String s) {
                            Toast.makeText(UserActivity.this, "网络出错", Toast.LENGTH_SHORT).show();
                        }
                    });
        }else {
            //添加关注
            httpUtils.send(HttpRequest.HttpMethod.GET,
                    followApi+"create",
                    requestParams,
                    new RequestCallBack<String>() {
                        @Override
                        public void onSuccess(ResponseInfo<String> responseInfo) {
                            try {
                                JSONObject jsonObject = new JSONObject(responseInfo.result);
                                if(jsonObject.getInt("following") == 1){
                                    Toast.makeText(UserActivity.this, "关注成功", Toast.LENGTH_SHORT).show();
                                    user_follow.setBootstrapType("danger");
                                    user_follow.setText("取消关注");
                                    user_follow.setLeftIcon("fa-minus");
                                    follow_state = true;
                                    if (jsonObject.getInt("follower") == 0){
                                        user_follow_state.setText("已关注");
                                    }else{
                                        user_follow_state.setText("互相关注");
                                    }
                                }
                            } catch (JSONException e) {
                                Toast.makeText(UserActivity.this, "关注失败", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(HttpException e, String s) {
                            Toast.makeText(UserActivity.this, "网络出错", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }


    // 用图像填充布局
    private void addAvatarToLayout(final Context context, String type, final LinearLayout layout, final int max){
        // 组装关注列表API 请求参数
        String followApi = "http://" + host + "index.php?app=api&mod=User&act=user_" + type;
        RequestParams requestParams = new RequestParams();
        requestParams.addQueryStringParameter("user_id", user_id);
        requestParams.addQueryStringParameter("oauth_token", oauth_token);
        requestParams.addQueryStringParameter("oauth_token_secret", oauth_token_secret);

        httpUtils.send(HttpRequest.HttpMethod.GET,
                followApi,
                requestParams,
                new RequestCallBack<String>() {
                    @Override
                    public void onSuccess(ResponseInfo<String> responseInfo) {
                        try {
                            JSONArray jsonArray = new JSONArray(responseInfo.result);
                            if (jsonArray.length() == 0){
                                TextView textView = new TextView(context);
                                layout.addView(textView);
                                textView.setText("无");
                            }else{
                                String avatarUrl;
                                String uname;
                                ImageView imageView;
                                TextView textView;
                                LinearLayout imageBox;

                                for (int i = 0; i < jsonArray.length(); i++){
                                    // 获取数据
                                    avatarUrl = jsonArray.getJSONObject(i).getString("avatar_small");
                                    uname = jsonArray.getJSONObject(i).getString("uname");

                                    // 动态生成View
                                    imageView = new ImageView(context);
                                    imageBox = new LinearLayout(context);
                                    textView = new TextView(context);

                                    LinearLayout.LayoutParams textLayout = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                    textLayout.gravity = Gravity.CENTER;

                                    LinearLayout.LayoutParams imgBoxLayout = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                    imgBoxLayout.leftMargin = 10;

                                    imageBox.setOrientation(LinearLayout.VERTICAL);
                                    imageBox.addView(imageView, new LinearLayout.LayoutParams(60, 60));
                                    imageBox.addView(textView, textLayout);

                                    layout.addView(imageBox, imgBoxLayout);

                                    // 绘制View
                                    textView.setText(uname);
                                    bitmapUtils.display(imageView, avatarUrl);

                                    if (i > max - 2){
                                        break;
                                    }
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

    @OnClick(R.id.user_weibo_show)
    public void weiboShowClick(View view){
        Intent intent = new Intent(this, UserWeiboActivity.class);
        intent.putExtra("user_id", user_id);
        startActivity(intent);
    }

    @OnClick(R.id.user_following_show)
    public void followingShowClick(View view){
        Intent intent = new Intent(this, UserFollowActivity.class);
        intent.putExtra("user_id", user_id);
        intent.putExtra("type", "user_following");
        startActivity(intent);
    }

    @OnClick(R.id.user_follower_show)
    public void followerShowClick(View view){
        Intent intent = new Intent(this, UserFollowActivity.class);
        intent.putExtra("user_id", user_id);
        intent.putExtra("type", "user_followers");
        startActivity(intent);
    }


    // TODO: 上传头像
    @OnClick(R.id.user_avatar)
    public void changeAvatar(View view){


        if (uid.equals(user_id)){

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("更换头像");
            String[] options = {"从图库", "从相机"};
            builder.setItems(options, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    switch (i){
                        case 0:
//                            Intent intent = new Intent();
//                            intent.setType("image/*");
//                            intent.setAction(Intent.ACTION_GET_CONTENT);
//                            startActivityForResult(intent, REQUEST_IMAGE_CONTENT);
                            break;
                        case 1:
//                            Intent capture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                            ContentValues values = new ContentValues();
//                            photoUri = UserActivity.this.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
//                            capture.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, photoUri);
//                            startActivityForResult(capture, REQUEST_CAPTURE);
                            break;
                    }
                }
            });

            builder.create().show();
        }
    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//
//        if (resultCode == RESULT_OK){
//
//            String imgPath;
//
//            if (requestCode == REQUEST_IMAGE_CONTENT && null != data){
//                // 图库
//                Uri imgUri = data.getData();
//                ContentResolver cr = this.getContentResolver();
//                Cursor cursor = null;
//                if (imgUri != null) {
//                    cursor = cr.query(imgUri, null, null, null, null);
//                }
//                if (cursor != null) {
//                    cursor.moveToFirst();
//                    imgPath = cursor.getString(1);
//                    cursor.close();
//                }
//            }else if (requestCode == REQUEST_CAPTURE){
//                // 相机
//                ContentResolver cr = this.getContentResolver();
//                Cursor cursor = cr.query(photoUri, null, null, null, null);
//                if (cursor != null) {
//                    cursor.moveToFirst();
//                    imgPath = cursor.getString(1);
//                    cursor.close();
//                }
//            }
//
//
//
//        }
//
//
//        super.onActivityResult(requestCode, resultCode, data);
//    }


    @OnClick(R.id.user_post_message)
    public void postMessage(View view){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("给 " + post_name + "发");

        View postView =  LayoutInflater.from(this).inflate(R.layout.post_message, null);

        builder.setView(postView);


        final BootstrapEditText contentView = (BootstrapEditText)postView.findViewById(R.id.post_message_content);
        BootstrapButton submit = (BootstrapButton)postView.findViewById(R.id.post_message_submit);

        final AlertDialog dialog = builder.create();
        dialog.show();

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String content = contentView.getText().toString();

                if (content.isEmpty()){
                    Toast.makeText(UserActivity.this, "还没有输入内容", Toast.LENGTH_SHORT).show();
                }else{
                    // 组装API 请求参数
                    String postMessageApi = "http://" + host + "index.php?app=api&mod=Message&act=create";
                    RequestParams requestParams = new RequestParams();
                    requestParams.addQueryStringParameter("to_uid", user_id);
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

                                    if (state.equals("false")){
                                        Toast.makeText(UserActivity.this, "发送失败", Toast.LENGTH_SHORT).show();
                                    }else{
                                        Toast.makeText(UserActivity.this, "发送成功", Toast.LENGTH_SHORT).show();
                                        dialog.dismiss();
                                    }
                                }

                                @Override
                                public void onFailure(HttpException e, String s) {
                                    Toast.makeText(UserActivity.this, "网络出错", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }
        });
    }
}
