package com.WindHunter;


import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.*;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.*;
import com.WindHunter.tools.DES_MOBILE;
import com.WindHunter.tools.MD5;
import com.lidroid.xutils.BitmapUtils;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.lidroid.xutils.view.annotation.ViewInject;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class RecommendActivity extends ActionBarActivity {

    private String oauth_token;
    private String oauth_token_secret;
    private BitmapUtils bitmapUtils;
    private ArrayList<String> ids;

    @ViewInject(R.id.recommend_row_one)
    LinearLayout rowOne;

    @ViewInject(R.id.recommend_row_two)
    LinearLayout rowTwo;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setTitle("推荐关注");

        menu.add("跳过").setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        menu.add("完成").setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

        setProgressBarIndeterminateVisibility(false);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getTitle().equals("跳过")){
            Intent i = new Intent(this, MainActivity.class);
            startActivity(i);
            finish();
        }else if (item.getTitle().equals("完成")){

            SharedPreferences settings = getSharedPreferences("settings", MODE_PRIVATE);
            String host = settings.getString("Host", "demo.thinksns.com/t3/");
            HttpUtils httpUtils = new HttpUtils();
            httpUtils.configDefaultHttpCacheExpiry(2000);

            createFollows(host, httpUtils, ids);

            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recommend);

        ViewUtils.inject(this);

        initBitmapUtils();

        String email = getIntent().getStringExtra("email");
        String password = getIntent().getStringExtra("password");
        ids = new ArrayList<String>();

        login(email, password);
    }

    void login(final String email, final String password){
        final String requestKey = "THINKSNS";

        SharedPreferences settings = getSharedPreferences("settings", MODE_PRIVATE);
        final String host = settings.getString("Host", "demo.thinksns.com/t3/");

        // 加密账号密码
        String encodeUid = new DES_MOBILE().setKey(requestKey).encrypt(email);
        String encodePasswd = new DES_MOBILE().setKey(requestKey).encrypt(MD5.md5(password));

        // 认证api
        String authorizeApi = "http://" + host + "index.php?app=api&mod=Oauth&act=authorize";

        // 请求的参数
        RequestParams requestParams = new RequestParams();
        requestParams.addQueryStringParameter("uid", encodeUid);
        requestParams.addQueryStringParameter("passwd", encodePasswd);

        final HttpUtils http = new HttpUtils();
        http.configDefaultHttpCacheExpiry(1000);
        http.send(HttpRequest.HttpMethod.GET,
                authorizeApi,
                requestParams,
                new RequestCallBack<String>() {
                    @Override
                    public void onSuccess(ResponseInfo<String> responseInfo) {
                        try {
                            // 获取JSON对象
                            JSONObject jsonResult = new JSONObject(responseInfo.result);
                            // 先计算出是否有code 防止 '&&'后面的代码先执行而照成异常
                            boolean tmp = jsonResult.has("code");
                            if ( tmp && jsonResult.getString("code").equals("00001") ){
                                // 认证失败
                                Toast.makeText(RecommendActivity.this, R.string.login_alert_autherror, Toast.LENGTH_SHORT).show();
                            }else{
                                // 成功
                                SharedPreferences settings = getSharedPreferences("settings", MODE_PRIVATE);
                                SharedPreferences.Editor editor = settings.edit();
                                editor.putString("oauth_token", jsonResult.getString("oauth_token"));
                                editor.putString("oauth_token_secret", jsonResult.getString("oauth_token_secret"));
                                editor.putString("uid", jsonResult.getString("uid"));
                                editor.commit();

                                oauth_token = jsonResult.getString("oauth_token");
                                oauth_token_secret = jsonResult.getString("oauth_token_secret");

                                drawRecommendView(host, http);
                            }
                        } catch (JSONException e) {
                            Toast.makeText(RecommendActivity.this,R.string.login_alert_neterror,Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(HttpException e, String s) {
                        Toast.makeText(RecommendActivity.this,R.string.login_alert_neterror,Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void drawRecommendView(String host, HttpUtils httpUtils){
        // 组装 API 请求参数
        String recommendApi = "http://" + host + "index.php?app=api&mod=User&act=get_user_follower";
        RequestParams requestParams = new RequestParams();
        requestParams.addQueryStringParameter("limit", "6");
        requestParams.addQueryStringParameter("oauth_token", oauth_token);
        requestParams.addQueryStringParameter("oauth_token_secret", oauth_token_secret);

        httpUtils.send(HttpRequest.HttpMethod.GET,
                recommendApi,
                requestParams,
                new RequestCallBack<String>() {
                    @Override
                    public void onSuccess(ResponseInfo<String> responseInfo) {
                        try {
                            JSONArray jsonArray = new JSONArray(responseInfo.result);

                            if (jsonArray.length() == 0){
                                Toast.makeText(RecommendActivity.this, "没有推荐，你是第一个", Toast.LENGTH_SHORT).show();
                            }else{
                                for (int i = 0; i < jsonArray.length(); i++){
                                    JSONObject user = jsonArray.getJSONObject(i);
                                    final String id = user.getString("uid");

                                    View view = LayoutInflater.from(RecommendActivity.this).inflate(R.layout.recommend_item, null);
                                    ImageView avatarView = (ImageView)view.findViewById(R.id.recommend_item_avatar);
                                    TextView nameView = (TextView)view.findViewById(R.id.recommend_item_name);
                                    TextView numView = (TextView)view.findViewById(R.id.recommend_item_followers_num);
                                    final CheckBox checkBox = (CheckBox)view.findViewById(R.id.recommend_item_check);
                                    FrameLayout frameLayout = (FrameLayout)view.findViewById(R.id.recommend_item_frame);

                                    bitmapUtils.display(avatarView, user.getString("avatar_middle"));
                                    nameView.setText(user.getString("uname"));
                                    numView.setText("粉丝数: " + user.getJSONObject("user_data").getString("follower_count"));

                                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                    layoutParams.weight = 1;
                                    layoutParams.topMargin = 50;

                                    if (i < 3){
                                        rowOne.addView(view,layoutParams);
                                    }else {
                                        rowTwo.addView(view,layoutParams);
                                    }

                                    frameLayout.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            if (checkBox.isChecked()){
                                                checkBox.setChecked(false);
                                                ids.remove(id);
                                            }else{
                                                checkBox.setChecked(true);
                                                ids.add(id);
                                            }
                                        }
                                    });
                                }
                            }
                        } catch (JSONException e) {
                            Toast.makeText(RecommendActivity.this, "网络出错", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(HttpException e, String s) {
                        Toast.makeText(RecommendActivity.this, "网络出错", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void initBitmapUtils(){

        bitmapUtils = new BitmapUtils(this);

        // 配置位图显示动画
        ScaleAnimation animation = new ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setDuration(800);
        bitmapUtils.configDefaultImageLoadAnimation(animation);

        // 占位图片
        // TODO: 需添加载失败图片
        bitmapUtils.configDefaultLoadingImage(R.drawable.loadingimg);
        bitmapUtils.configDefaultLoadFailedImage(R.drawable.icon);
        bitmapUtils.configDefaultBitmapConfig(Bitmap.Config.RGB_565);
    }

    private void createFollows(final String host, final HttpUtils httpUtils, final ArrayList<String> ids){
        if (ids.size() != 0) {
            final String createFollowApi = "http://" + host + "index.php?app=api&mod=User&act=follow_create";
            RequestParams requestParams = new RequestParams();
            requestParams.addQueryStringParameter("user_id", ids.get(0));
            requestParams.addQueryStringParameter("oauth_token", oauth_token);
            requestParams.addQueryStringParameter("oauth_token_secret", oauth_token_secret);

            httpUtils.send(HttpRequest.HttpMethod.GET,
                    createFollowApi,
                    requestParams,
                    new RequestCallBack<String>() {
                        @Override
                        public void onSuccess(ResponseInfo<String> responseInfo) {
                            ids.remove(ids.get(0));
                            createFollows(host, httpUtils, ids);
                        }

                        @Override
                        public void onFailure(HttpException e, String s) {

                        }
                    });
        }
    }
}
