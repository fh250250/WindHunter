package com.WindHunter;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.*;
import android.view.animation.AlphaAnimation;
import android.widget.*;
import com.lidroid.xutils.BitmapUtils;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.lidroid.xutils.view.annotation.ViewInject;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;


public class GetFollowForPostActivity extends ActionBarActivity {

    private String oauth_token;
    private String oauth_token_secret;
    private String uid;
    private String host;
    private HttpUtils httpUtils;
    private BitmapUtils bitmapUtils;
    private AtListAdapter followListAdapter;
    private AtListAdapter searchListAdapter;
    private HashMap<String, ImageView> previews;
    private int RESPONSE_CODE = 102;

    @ViewInject(R.id.get_follow_list)
    ListView get_follow_list;

    @ViewInject(R.id.get_follow_preview)
    LinearLayout get_follow_preview;


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayShowHomeEnabled(false);

        android.support.v7.widget.SearchView searchView = new SearchView(this);
        searchView.setQueryHint("搜索");
        searchView.setMaxWidth(400);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String key) {

                if (key.isEmpty()){
                    return true;
                }


                // 组装 搜索用户API
                String searchApi = "http://" + host + "index.php?app=api&mod=WeiboStatuses&act=weibo_search_user";
                RequestParams requestParams = new RequestParams();
                requestParams.addQueryStringParameter("count", "100");
                requestParams.addQueryStringParameter("key", key);
                requestParams.addQueryStringParameter("oauth_token", oauth_token);
                requestParams.addQueryStringParameter("oauth_token_secret", oauth_token_secret);

                httpUtils.send(HttpRequest.HttpMethod.GET,
                        searchApi,
                        requestParams,
                        new RequestCallBack<String>() {
                            @Override
                            public void onSuccess(ResponseInfo<String> responseInfo) {
                                try {
                                    JSONArray jsonArray = new JSONArray(responseInfo.result);

                                    if (jsonArray.length() == 0){
                                        Toast.makeText(GetFollowForPostActivity.this,
                                                "无搜索结果", Toast.LENGTH_SHORT).show();
                                        return;
                                    }

                                    ArrayList<UserData> items = new ArrayList<UserData>();
                                    for (int i = 0; i < jsonArray.length(); i++){
                                        UserData userData = new UserData();
                                        userData.name = jsonArray.getJSONObject(i).getString("uname");
                                        userData.avatar = jsonArray.getJSONObject(i).getString("avatar_middle");
                                        items.add(userData);
                                    }

                                    get_follow_list.setAdapter(searchListAdapter);
                                    searchListAdapter.clear();
                                    searchListAdapter.addAll(items);
                                    searchListAdapter.notifyDataSetChanged();

                                } catch (JSONException e) {
                                    Toast.makeText(GetFollowForPostActivity.this,
                                            "网络出错", Toast.LENGTH_SHORT).show();
                                    Log.e("jsonParseError", e.toString());
                                }
                            }

                            @Override
                            public void onFailure(HttpException e, String s) {
                                Toast.makeText(GetFollowForPostActivity.this,
                                        "网络出错", Toast.LENGTH_SHORT).show();
                                Log.e("netError", e.toString());
                            }
                        });

                return true;
            }
        });
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                get_follow_list.setAdapter(followListAdapter);

                return false;
            }
        });

        menu.add("Search").setActionView(searchView)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);


        Button complete = new Button(this);
        complete.setText("完成");
        complete.setTextSize(12);
        ActionBar.LayoutParams layoutParams = new ActionBar.LayoutParams(120, 70);
        layoutParams.gravity = Gravity.LEFT | Gravity.CENTER_VERTICAL;
        actionBar.setCustomView(complete, layoutParams);
        actionBar.setDisplayShowCustomEnabled(true);

        complete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                String[] names = previews.keySet().toArray(new String[previews.size()]);
                intent.putExtra("names", names);

                setResult(RESPONSE_CODE, intent);
                finish();
            }
        });

        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.get_follow_for_post);

        ViewUtils.inject(this);
        httpUtils = new HttpUtils();
        httpUtils.configDefaultHttpCacheExpiry(5 * 1000);

        bitmapUtils = new BitmapUtils(this);
        // 配置位图显示动画
        AlphaAnimation animation = new AlphaAnimation(0.0f, 1.0f);
        animation.setDuration(1000);
        bitmapUtils.configDefaultImageLoadAnimation(animation);

        // 占位图片
        // TODO: 需添加载失败图片
        bitmapUtils.configDefaultLoadingImage(R.drawable.loadingimg);
        bitmapUtils.configDefaultLoadFailedImage(R.drawable.icon);
        bitmapUtils.configDefaultBitmapConfig(Bitmap.Config.RGB_565);

        oauth_token = getIntent().getStringExtra("oauth_token");
        oauth_token_secret = getIntent().getStringExtra("oauth_token_secret");
        uid = getIntent().getStringExtra("uid");
        host = getIntent().getStringExtra("host");


        followListAdapter = new AtListAdapter(this, R.layout.get_follow_for_post_item);
        searchListAdapter = new AtListAdapter(this, R.layout.get_follow_for_post_item);



        previews = new HashMap<String, ImageView>();
        // 列表每一项的点击事件
        get_follow_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                CheckBox checkBox = (CheckBox)view.findViewById(R.id.get_follow_item_check);
                UserData userData = (UserData)adapterView.getItemAtPosition(i);
                if (checkBox.isChecked()){
                    checkBox.setChecked(false);
                    get_follow_preview.removeView(previews.get(userData.name));
                    previews.remove(userData.name);
                }else{
                    checkBox.setChecked(true);

                    if (previews.containsKey(userData.name))
                        return;

                    ImageView avatar = new ImageView(GetFollowForPostActivity.this);
                    bitmapUtils.display(avatar, userData.avatar);
                    previews.put(userData.name, avatar);

                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(60, ViewGroup.LayoutParams.MATCH_PARENT);
                    layoutParams.gravity = Gravity.CENTER_VERTICAL;
                    layoutParams.setMargins(10,2,0,2);

                    get_follow_preview.addView(avatar, 0, layoutParams);
                }
            }
        });


        // 组装 关注用户API
        String followApi = "http://" + host + "index.php?app=api&mod=User&act=user_following";
        RequestParams requestParams = new RequestParams();
        requestParams.addQueryStringParameter("count", "100");
        requestParams.addQueryStringParameter("user_id", uid);
        requestParams.addQueryStringParameter("oauth_token", oauth_token);
        requestParams.addQueryStringParameter("oauth_token_secret", oauth_token_secret);


        httpUtils.send(HttpRequest.HttpMethod.GET,
                followApi,
                requestParams,
                new RequestCallBack<String>() {
                    @Override
                    public void onSuccess(ResponseInfo<String> responseInfo) {
                        try {
                            JSONArray jsonArray = new JSONArray(responseInfo.result);

                            if (jsonArray.length() == 0){
                                Toast.makeText(GetFollowForPostActivity.this,
                                        "还没有关注任何人", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            ArrayList<UserData> items = new ArrayList<UserData>();
                            for (int i = 0; i < jsonArray.length(); i++){
                                UserData userData = new UserData();
                                userData.name = jsonArray.getJSONObject(i).getString("uname");
                                userData.avatar = jsonArray.getJSONObject(i).getString("avatar_middle");
                                items.add(userData);
                            }

                            get_follow_list.setAdapter(followListAdapter);
                            followListAdapter.addAll(items);
                            followListAdapter.notifyDataSetChanged();

                        } catch (JSONException e) {
                            Toast.makeText(GetFollowForPostActivity.this,
                                    "网络出错", Toast.LENGTH_SHORT).show();
                            Log.e("jsonParseError", e.toString());
                        }
                    }

                    @Override
                    public void onFailure(HttpException e, String s) {
                        Toast.makeText(GetFollowForPostActivity.this,
                                "网络出错", Toast.LENGTH_SHORT).show();
                        Log.e("netError", e.toString() + s);
                    }
                });
    }


    private class AtListAdapter extends ArrayAdapter<UserData>{
        private LayoutInflater inflater;
        private int resource;

        public AtListAdapter(Context context, int resource) {
            super(context, resource);
            inflater = LayoutInflater.from(context);
            this.resource = resource;
        }

        @Override
        public View getView(int position, View row, ViewGroup parent) {

            if (row == null)
                row = inflater.inflate(resource, parent, false);

            ImageView avatar = (ImageView)row.findViewById(R.id.get_follow_item_avatar);
            TextView name = (TextView)row.findViewById(R.id.get_follow_item_name);

            UserData userData = getItem(position);

            name.setText(userData.name);
            bitmapUtils.display(avatar, userData.avatar);


            return row;
        }
    }

    private class UserData {
        private String name;
        private String avatar;
    }

}
