package com.WindHunter;


import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import com.WH.xListView.XListView;
import com.WindHunter.tools.WHActivity;
import com.WindHunter.tools.WeiboList;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;

public class UserWeiboActivity extends WHActivity {

    @ViewInject(R.id.user_weibo_list)
    XListView user_weibo_list;

    private WeiboList weiboList;

    private String user_id;


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        if (uid.equals(user_id)){
            title.setText("我的微博");
        }else {
            title.setText("他的微博");
        }

        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_weibo);

        ViewUtils.inject(this);

        user_id = getIntent().getStringExtra("user_id");
        weiboList = new WeiboList(this, user_weibo_list);
        weiboList.setCount(10).setType("user_timeline").setUser_id(user_id).run();
    }
}
