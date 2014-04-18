package com.WindHunter;


import android.os.Bundle;
import com.WH.xListView.XListView;
import com.WindHunter.tools.WHActivity;
import com.WindHunter.tools.WeiboList;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;

public class AtActivity extends WHActivity {

    @ViewInject(R.id.at_weibo_list)
    XListView at_weibo_list;

    private WeiboList weiboList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.at);

        ViewUtils.inject(this);


        weiboList = new WeiboList(this, at_weibo_list);
        weiboList.setCount(10).setType("mentions_feed").run();
    }
}
