package com.WindHunter;



import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.*;
import android.widget.*;
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

        actionBar.setTitle("微博列表");

        // 加入 微博转换按钮
        Switch switchBar = new Switch(this);
        switchBar.setTextOff("关注微博");
        switchBar.setTextOn("全部微博");
        actionBar.setCustomView(switchBar, new ActionBar.LayoutParams(Gravity.CENTER));
        actionBar.setDisplayShowCustomEnabled(true);

        switchBar.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b){
                    weiboList.setType("public_timeline").fresh();
                }else{
                    weiboList.setType("friends_timeline").fresh();
                }
            }
        });

        return super.onCreateOptionsMenu(menu);
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
