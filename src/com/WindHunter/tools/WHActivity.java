package com.WindHunter.tools;


import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.MotionEvent;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import com.WindHunter.R;
import com.lidroid.xutils.BitmapUtils;
import com.lidroid.xutils.HttpUtils;
import com.special.ResideMenu.ResideMenu;

public class WHActivity extends ActionBarActivity {

    protected BitmapUtils bitmapUtils;
    protected HttpUtils httpUtils;

    protected ResideMenu resideMenu;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 配置BitmapUtils
        initBitmapUtils();

        // 初始化HttpUtils
        httpUtils = new HttpUtils();

        // 加载slideMenu
        initSlideMenu();
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
        // TODO: 需添加占位图片和加载失败图片
        bitmapUtils.configDefaultLoadingImage(R.drawable.icon);
        bitmapUtils.configDefaultLoadFailedImage(R.drawable.icon);
        bitmapUtils.configDefaultBitmapConfig(Bitmap.Config.RGB_565);
    }

    private void initSlideMenu(){
        resideMenu = new ResideMenu(this);
        resideMenu.setBackground(R.drawable.menu_background);
        resideMenu.attachToActivity(this);
    }
}
