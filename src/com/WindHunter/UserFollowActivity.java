package com.WindHunter;


import android.os.Bundle;
import android.view.Menu;
import com.WH.xListView.XListView;
import com.WindHunter.tools.FollowList;
import com.WindHunter.tools.WHActivity;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;

public class UserFollowActivity extends WHActivity {

    private String user_id;
    private String type;

    @ViewInject(R.id.user_following_list)
    XListView following_list;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        if (uid.equals(user_id)){
            if (type.equals("user_following")){
                title.setText("我的关注");
            }else{
                title.setText("我的粉丝");
            }
        }else{
            if (type.equals("user_following")){
                title.setText("他的关注");
            }else{
                title.setText("他的粉丝");
            }
        }

        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_follow);

        ViewUtils.inject(this);

        user_id = getIntent().getStringExtra("user_id");
        type = getIntent().getStringExtra("type");

        FollowList followList = new FollowList(this, following_list);
        followList.setCount(20).setType(type).setUserID(user_id).run();
    }
}
