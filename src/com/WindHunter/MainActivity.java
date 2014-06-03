package com.WindHunter;



import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.*;
import android.widget.TextView;
import android.widget.Toast;
import com.WH.xListView.XListView;
import com.WindHunter.tools.WHActivity;
import com.WindHunter.tools.WeiboList;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.special.ResideMenu.ResideMenu;
import com.special.ResideMenu.ResideMenuItem;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends WHActivity {

    private ResideMenu resideMenu;

    // ListView
    @ViewInject(R.id.main_weibo_list)
    XListView main_weibo_list;

    private WeiboList weiboList;


    // 绘制ActionBar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // 发微博
        menu.add("Post")
                .setIcon(R.drawable.bar_post)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);


        ActionBar actionBar = getSupportActionBar();

        actionBar.setIcon(R.drawable.personal_info_menu);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);

        menu.add("全部微博");
        menu.add("关注微博");


        title = new TextView(this);
        ActionBar.LayoutParams layoutParams = new ActionBar.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams.gravity = Gravity.LEFT;
        title.setTextSize(15);
        title.setTextColor(Color.WHITE);
        title.setGravity(Gravity.CENTER);
        actionBar.setCustomView(title, layoutParams);
        actionBar.setDisplayShowCustomEnabled(true);
        title.setText("关注微博");

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home){
            resideMenu.openMenu();
        }else if (item.getTitle().equals("全部微博")){
            title.setText("全部微博");
            weiboList.setType("public_timeline").fresh();
        }else if (item.getTitle().equals("关注微博")){
            title.setText("关注微博");
            weiboList.setType("friends_timeline").fresh();
        }else if (item.getTitle().equals("Post")){
            // 跳转到 发微博
            startActivity(new Intent(this, PostActivity.class));
        }

        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);


        // 注入Activity
        ViewUtils.inject(this);

        initSlideMenu(this);

        weiboList = new WeiboList(this, main_weibo_list);
        weiboList.setCount(10).setType("friends_timeline").run();
    }

    private void initSlideMenu(final Context context){
        resideMenu = new ResideMenu(this);
        resideMenu.setBackground(R.drawable.menu_background);
        resideMenu.attachToActivity(this);

        String titles[]={"主   页",
                         "与我相关",
                         "收   藏",
                         "微   吧",
                         "搜   索"};
        int icon[]={R.drawable.main_menu_profile,
                    R.drawable.main_menu_about_me,
                    R.drawable.main_menu_collect,
                    R.drawable.main_menu_app,
                    R.drawable.main_menu_search};

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
            }
        });

        // 跳转到与我相关
        items.get(1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, AboutMeActivity.class);
                context.startActivity(intent);
            }
        });

        // 跳转到收藏
        items.get(2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, CollectionActivity.class);
                context.startActivity(intent);
            }
        });

        // 跳转到微吧
        items.get(3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, WeibaActivity.class);
                context.startActivity(intent);
            }
        });

        // 跳转到搜索
        items.get(4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, SearchActivity.class);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return resideMenu.onInterceptTouchEvent(ev) || super.dispatchTouchEvent(ev);
    }

    //按两次退出程序
    private long mExitTime;
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if ((System.currentTimeMillis() - mExitTime) > 2000) {
                Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
                mExitTime = System.currentTimeMillis();

            } else {
                finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
