package com.WindHunter;


import android.content.Context;
import android.os.Bundle;
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
}
