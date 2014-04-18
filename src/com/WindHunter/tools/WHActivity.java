package com.WindHunter.tools;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.*;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import com.WindHunter.R;
import com.WindHunter.UserActivity;
import com.lidroid.xutils.BitmapUtils;
import com.lidroid.xutils.HttpUtils;
import com.special.ResideMenu.ResideMenu;
import com.special.ResideMenu.ResideMenuItem;

import java.util.ArrayList;
import java.util.List;

public abstract class WHActivity extends ActionBarActivity {

    protected String host, oauth_token, oauth_token_secret, uid;

    protected BitmapUtils bitmapUtils;
    protected HttpUtils httpUtils;

    protected ResideMenu resideMenu;


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setIcon(R.drawable.personal_info_menu);
        actionBar.setHomeButtonEnabled(true);

        // 发微博
        menu.add("Post")
                .setIcon(R.drawable.action_bar_post)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

        menu.add("@我的");
        menu.add("评论我的");
        menu.add("搜索");

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            resideMenu.openMenu();
        }else if (item.getTitle().equals("Post")){
            // 跳转到发微博
        }

        return super.onOptionsItemSelected(item);
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        // 配置BitmapUtils
        initBitmapUtils();

        // 初始化HttpUtils
        httpUtils = new HttpUtils();


        // 从全局对象中获取认证数据
        SharedPreferences settings = getSharedPreferences("settings", MODE_PRIVATE);
        host = settings.getString("Host", "demo.thinksns.com/t3/");
        oauth_token = settings.getString("oauth_token", "");
        oauth_token_secret = settings.getString("oauth_token_secret", "");
        uid = settings.getString("uid", "");

        // 加载slideMenu
        initSlideMenu(this);

    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return resideMenu.onInterceptTouchEvent(ev) || super.dispatchTouchEvent(ev);
    }

    private void initBitmapUtils(){

        // 生成BitmapUtils实体
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

    private void initSlideMenu(final Context context){
        resideMenu = new ResideMenu(this);
        resideMenu.setBackground(R.drawable.menu_background);
        resideMenu.attachToActivity(this);

        String titles[]={" 主     页"," 微     博"," 收     藏"," 聊     天"," 微     吧"};
        int icon[]={R.drawable.main_menu_home,
                    R.drawable.main_menu_weibo,
                    R.drawable.main_menu_collect,
                    R.drawable.main_menu_chat,
                    R.drawable.main_menu_app};

        List<ResideMenuItem> items = new ArrayList<ResideMenuItem>();

        for (int i = 0; i < titles.length; i++){
            ResideMenuItem item = new ResideMenuItem(this, icon[i], titles[i]);
            items.add(item);
        }

        resideMenu.setMenuItems(items);

        // 跳转到个人主页
        items.get(0).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, UserActivity.class);
                intent.putExtra("user_id", uid);
                context.startActivity(intent);
                resideMenu.closeMenu();
            }
        });
    }

}
