package com.WindHunter;



import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.*;
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
        ActionBar actionBar = getSupportActionBar();

        actionBar.setIcon(R.drawable.personal_info_menu);
        actionBar.setHomeButtonEnabled(true);

        actionBar.setTitle("关注微博");

        menu.add("全部微博");
        menu.add("关注微博");

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home){
            resideMenu.openMenu();
        }else if (item.getTitle().equals("全部微博")){
            getSupportActionBar().setTitle("全部微博");
            weiboList.setType("public_timeline").fresh();
        }else if (item.getTitle().equals("关注微博")){
            getSupportActionBar().setTitle("关注微博");
            weiboList.setType("friends_timeline").fresh();
        }

        return super.onOptionsItemSelected(item);
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

        String titles[]={" 主     页"," 收     藏"," 聊     天"," 微     吧"};
        int icon[]={R.drawable.main_menu_home,
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
