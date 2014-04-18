package com.WindHunter;



import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.*;
import com.WH.xListView.XListView;
import com.WindHunter.tools.WHActivity;
import com.WindHunter.tools.WeiboList;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;


public class MainActivity extends WHActivity {

    // ListView
    @ViewInject(R.id.main_weibo_list)
    XListView main_weibo_list;

    private WeiboList weiboList;


    // 绘制ActionBar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        ActionBar actionBar = getSupportActionBar();

        actionBar.setTitle("关注微博");

        menu.add("全部微博");
        menu.add("关注微博");

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getTitle().equals("全部微博")){
            getSupportActionBar().setTitle("全部微博");
            weiboList.setType("public_timeline").fresh();
        }
        else if (item.getTitle().equals("关注微博")){
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

        weiboList = new WeiboList(this, main_weibo_list);
        weiboList.setCount(10).setType("friends_timeline").run();
    }



}
