package com.WindHunter.tools;


import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.*;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageButton;
import android.widget.Toast;
import com.WindHunter.R;
import com.lidroid.xutils.BitmapUtils;
import com.lidroid.xutils.HttpUtils;
import com.special.ResideMenu.ResideMenu;

public abstract class WHActivity extends ActionBarActivity {

    protected String host, oauth_token, oauth_token_secret, uid;

    protected BitmapUtils bitmapUtils;
    protected HttpUtils httpUtils;

    protected ResideMenu resideMenu;
    private PathView pathView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        // 配置BitmapUtils
        initBitmapUtils();

        // 初始化HttpUtils
        httpUtils = new HttpUtils();

        // 加载slideMenu
        initSlideMenu();


        // 从全局对象中获取认证数据
        SharedPreferences settings = getSharedPreferences("settings", MODE_PRIVATE);
        host = settings.getString("Host", "demo.thinksns.com/t3/");
        oauth_token = settings.getString("oauth_token", "");
        oauth_token_secret = settings.getString("oauth_token_secret", "");
        uid = settings.getString("uid", "");


        pathView = new PathView(this);
        setContentView(pathView);
    }


    @Override
    public void setContentView(int layoutResID) {
        View layout = LayoutInflater.from(this).inflate(layoutResID, null);
        pathView.addView(layout);

        initPathView(this);
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

    private void initPathView(final Context context){
        ImageButton startMenu = new ImageButton(this);
        startMenu.setBackgroundResource(R.drawable.start_menu_btn);
        pathView.setStartMenu(startMenu);

        int[] drawableIds = { R.drawable.start_menu_scan_normal,
                              R.drawable.start_menu_call_normal,
                              R.drawable.start_menu_sms_normal,
                              R.drawable.start_menu_chat_normal};
        View[] items = new View[drawableIds.length];
        for (int i = 0; i < drawableIds.length; i++) {
            ImageButton button = new ImageButton(this);
            button.setBackgroundResource(drawableIds[i]);
            items[i] = button;
        }
        pathView.setItems(items);

        pathView.setOnItemClickListener(new PathView.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Toast.makeText(context, "您单击了第"+position+"项", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
