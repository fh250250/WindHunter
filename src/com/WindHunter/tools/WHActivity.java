package com.WindHunter.tools;


import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.*;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.TextView;
import com.WindHunter.*;
import com.lidroid.xutils.BitmapUtils;
import com.lidroid.xutils.HttpUtils;


public abstract class WHActivity extends ActionBarActivity {

    protected String host, oauth_token, oauth_token_secret, uid;

    protected TextView title;

    protected BitmapUtils bitmapUtils;
    protected HttpUtils httpUtils;


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // 发微博
        menu.add("Post")
                .setIcon(R.drawable.action_bar_post)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setIcon(R.drawable.main_menu_home);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);

        title = new TextView(this);
        ActionBar.LayoutParams layoutParams = new ActionBar.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams.gravity = Gravity.LEFT;
        title.setTextSize(15);
        title.setTextColor(Color.WHITE);
        title.setGravity(Gravity.CENTER);
        actionBar.setCustomView(title, layoutParams);
        actionBar.setDisplayShowCustomEnabled(true);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getTitle().equals("Post")){
            // 跳转到 发微博
            startActivity(new Intent(this, PostActivity.class));
        }else if (item.getItemId() == android.R.id.home){
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }



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
        httpUtils.configDefaultHttpCacheExpiry(1000 * 5);
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
