package com.WindHunter.tools;


import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import com.WindHunter.R;
import com.lidroid.xutils.BitmapUtils;
import com.lidroid.xutils.HttpUtils;

public abstract class WeibaBaseActivity extends Activity {

    protected String host, oauth_token, oauth_token_secret, uid;
    protected BitmapUtils bitmapUtils;
    protected HttpUtils httpUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 配置HttpUtils
        initHttpUtils();

        // 配置BitmapUtils
        initBitmapUtils();

        // 从全局对象中获取认证数据
        SharedPreferences settings = getSharedPreferences("settings", MODE_PRIVATE);
        host = settings.getString("Host", "demo.thinksns.com/t3/");
        oauth_token = settings.getString("oauth_token", "");
        oauth_token_secret = settings.getString("oauth_token_secret", "");
        uid = settings.getString("uid", "");
    }


    private void initHttpUtils(){
        httpUtils = new HttpUtils();
        httpUtils.configDefaultHttpCacheExpiry(1000 * 2);
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
}
