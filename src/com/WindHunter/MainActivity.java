package com.WindHunter;


import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;
import com.lidroid.xutils.BitmapUtils;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.slidingmenu.lib.SlidingMenu;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends Activity {

    private String host, oauth_token, oauth_token_secret, uid;

    @ViewInject(R.id.avatar)
    ImageView avatar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // 注入Activity
        ViewUtils.inject(this);

        // 加载slideMenu
        initSlideMenu();

        // 从全局对象中获取认证数据
        SharedPreferences settings = getSharedPreferences("settings", MODE_PRIVATE);
        host = settings.getString("Host", "demo.thinksns.com/t3/");
        oauth_token = settings.getString("oauth_token", "");
        oauth_token_secret = settings.getString("oauth_token_secret", "");
        uid = settings.getString("uid", "");

        // 组装API 请求参数
        String userShowApi = "http://" + host + "index.php?app=api&mod=User&act=show";
        RequestParams requestParams = new RequestParams();
        requestParams.addQueryStringParameter("user_id", uid);
        requestParams.addQueryStringParameter("oauth_token", oauth_token);
        requestParams.addQueryStringParameter("oauth_token_secret", oauth_token_secret);

        // 完成请求并绘制界面
        HttpUtils http = new HttpUtils();
        http.send(HttpRequest.HttpMethod.GET,
                userShowApi,
                requestParams,
                new RequestCallBack<String>(){

                    @Override
                    public void onSuccess(ResponseInfo<String> stringResponseInfo) {
                        try {
                            JSONObject jsonObject = new JSONObject(stringResponseInfo.result);
                            String avatarUrl = jsonObject.getString("avatar_tiny");

                            BitmapUtils bitmapUtils = new BitmapUtils(MainActivity.this);
                            bitmapUtils.display(avatar, avatarUrl);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(HttpException e, String s) {
                        Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void initSlideMenu(){
        final SlidingMenu menu=new SlidingMenu(this);
        menu.setMode(SlidingMenu.LEFT);
        menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
        menu.setShadowWidthRes(R.dimen.shadow_width);
        menu.setShadowDrawable(R.drawable.shadow);
        menu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
        menu.setBehindWidth(200);
        menu.setFadeDegree(0.35f);
        menu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
        menu.setMenu(R.layout.menu);
    }
}
