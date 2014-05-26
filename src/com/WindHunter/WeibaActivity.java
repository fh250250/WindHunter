package com.WindHunter;


import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.*;
import com.WindHunter.tools.WeibaBaseActivity;
import com.beardedhen.androidbootstrap.BootstrapButton;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class WeibaActivity extends WeibaBaseActivity {

    ActionBar actionBar;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        actionBar.setDisplayShowHomeEnabled(false);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weiba);

        actionBar = getActionBar();

        initTabHost(this);
    }

    private void initTabHost(final Context context){
        actionBar.setTitle("微吧首页");

        final TabHost tabHost = (TabHost)findViewById(R.id.weiba_tab_host);
        tabHost.setup();

        final ImageView tab1 = new ImageView(context);
        tab1.setImageResource(R.drawable.tabbar_home_highlighted);
        tabHost.addTab(tabHost.newTabSpec("home").setIndicator(tab1).setContent(R.id.weiba_tab_home));

        final ImageView tab2 = new ImageView(context);
        tab2.setImageResource(R.drawable.tabbar_profile);
        tabHost.addTab(tabHost.newTabSpec("profile").setIndicator(tab2).setContent(R.id.weiba_tab_me));

        final ImageView tab3 = new ImageView(context);
        tab3.setImageResource(R.drawable.tabbar_discover);
        tabHost.addTab(tabHost.newTabSpec("search").setIndicator(tab3).setContent(R.id.weiba_tab_search));

        tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String s) {

                if (s.equals("home")){
                    actionBar.setTitle("微吧首页");
                    tab1.setImageResource(R.drawable.tabbar_home_highlighted);
                    tab2.setImageResource(R.drawable.tabbar_profile);
                    tab3.setImageResource(R.drawable.tabbar_discover);
                }else if (s.equals("profile")){
                    actionBar.setTitle("我的微吧");
                    tab1.setImageResource(R.drawable.tabbar_home);
                    tab2.setImageResource(R.drawable.tabbar_profile_highlighted);
                    tab3.setImageResource(R.drawable.tabbar_discover);
                }else if (s.equals("search")){
                    actionBar.setTitle("搜索");
                    tab1.setImageResource(R.drawable.tabbar_home);
                    tab2.setImageResource(R.drawable.tabbar_profile);
                    tab3.setImageResource(R.drawable.tabbar_discover_highlighted);
                }
            }
        });

        LinearLayout homeView = (LinearLayout)findViewById(R.id.weiba_tab_home);
        LinearLayout meView = (LinearLayout)findViewById(R.id.weiba_tab_me);
        LinearLayout searchView = (LinearLayout)findViewById(R.id.weiba_tab_search);

        initHomeView(context, homeView);
        initMeView(context, meView);
        initSearchView(context, searchView);
    }

    private void initHomeView(final Context context, final LinearLayout layout){
        // 组装 API 请求参数
        String getWeibaApi = "http://" + host + "index.php?app=api&mod=Weiba&act=get_weibas";
        RequestParams requestParams = new RequestParams();
        requestParams.addQueryStringParameter("count", "100");
        requestParams.addQueryStringParameter("page", "1");
        requestParams.addQueryStringParameter("oauth_token", oauth_token);
        requestParams.addQueryStringParameter("oauth_token_secret", oauth_token_secret);

        httpUtils.send(HttpRequest.HttpMethod.GET,
                getWeibaApi,
                requestParams,
                new RequestCallBack<String>() {
                    @Override
                    public void onSuccess(ResponseInfo<String> responseInfo) {
                        try {
                            JSONArray jsonArray = new JSONArray(responseInfo.result);

                            if (jsonArray.length() == 0){
                                TextView textView = new TextView(context);
                                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                layoutParams.gravity = Gravity.CENTER;
                                textView.setText("没有微吧");
                                layout.addView(textView, layoutParams);
                            }else{
                                ListView listView = new ListView(context);
                                listView.setAdapter(new WeibaAdapter(context, jsonArray));

                                layout.addView(listView);

                                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                        JSONObject jsonObject = (JSONObject)adapterView.getItemAtPosition(i);
                                        try {
                                            String weiba_id = jsonObject.getString("weiba_id");
                                            Intent intent = new Intent(context, WeibaPostsActivity.class);
                                            intent.putExtra("weiba_id", weiba_id);
                                            startActivity(intent);
                                        } catch (JSONException e) {
                                            Log.e("json error", e.toString());
                                        }
                                    }
                                });
                            }
                        } catch (JSONException e) {
                            Toast.makeText(context, "网络异常", Toast.LENGTH_SHORT).show();
                            Log.e("json error", e.toString());
                        }
                    }

                    @Override
                    public void onFailure(HttpException e, String s) {
                        Toast.makeText(context, "网络异常", Toast.LENGTH_SHORT).show();
                        Log.e("net error", e.toString() + s);
                    }
                });
    }

    private void initMeView(Context context, LinearLayout layout){
        View view =  LayoutInflater.from(context).inflate(R.layout.my_weiba, null);
        layout.addView(view);
    }

    private void initSearchView(Context context, LinearLayout layout){

    }


    private class WeibaAdapter extends BaseAdapter {

        private Context context;
        private JSONArray jsonArray;
        private LayoutInflater inflater;

        public WeibaAdapter(Context context, JSONArray jsonArray){
            this.context = context;
            this.jsonArray = jsonArray;
            inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return jsonArray.length();
        }

        @Override
        public Object getItem(int i) {
            try {
                return jsonArray.getJSONObject(i);
            } catch (JSONException e) {
                Log.e("json error", e.toString());
                return null;
            }
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if (view == null){
                view = inflater.inflate(R.layout.weibas_item, null);
            }

            ImageView logoView = (ImageView)view.findViewById(R.id.weiba_item_logo);
            TextView nameView = (TextView)view.findViewById(R.id.weiba_item_name);
            TextView numView = (TextView)view.findViewById(R.id.weiba_item_num);
            TextView introView = (TextView)view.findViewById(R.id.weiba_item_intro);
            final BootstrapButton followBtn = (BootstrapButton)view.findViewById(R.id.weiba_item_follow);

            try {
                final JSONObject jsonObject = jsonArray.getJSONObject(i);

                String logo = jsonObject.getString("logo_url");
                String name = jsonObject.getString("weiba_name");
                String follower_count = jsonObject.getString("follower_count");
                String thread_count = jsonObject.getString("thread_count");
                String intro = jsonObject.getString("intro");
                final String[] follow_state = {jsonObject.getString("followstate")};
                final String weiba_id = jsonObject.getString("weiba_id");

                bitmapUtils.display(logoView, logo);
                nameView.setText(name);
                numView.setText("粉丝数: " + follower_count + " / 帖子数: " + thread_count);
                introView.setText(intro);
                if (follow_state[0].equals("1")){
                    followBtn.setText("取消");
                    followBtn.setBootstrapType("danger");
                }else{
                    followBtn.setText("关注");
                    followBtn.setBootstrapType("primary");
                }

                followBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (follow_state[0].equals("1")){
                            // 取消关注
                            String weibaDestroyApi = "http://" + host + "index.php?app=api&mod=Weiba&act=destroy";
                            RequestParams requestParams = new RequestParams();
                            requestParams.addQueryStringParameter("id", weiba_id);
                            requestParams.addQueryStringParameter("oauth_token", oauth_token);
                            requestParams.addQueryStringParameter("oauth_token_secret", oauth_token_secret);

                            httpUtils.send(HttpRequest.HttpMethod.GET,
                                    weibaDestroyApi,
                                    requestParams,
                                    new RequestCallBack<String>() {
                                        @Override
                                        public void onSuccess(ResponseInfo<String> responseInfo) {
                                            String state = responseInfo.result;

                                            if (state.equals("1")){
                                                Toast.makeText(context, "取消成功", Toast.LENGTH_SHORT).show();
                                                followBtn.setText("关注");
                                                followBtn.setBootstrapType("primary");

                                                follow_state[0] = "0";
                                            }else{
                                                Toast.makeText(context, "取消失败", Toast.LENGTH_SHORT).show();
                                            }
                                        }

                                        @Override
                                        public void onFailure(HttpException e, String s) {
                                            Toast.makeText(context, "网络异常", Toast.LENGTH_SHORT).show();
                                            Log.e("net error", e.toString() + s);
                                        }
                                    });
                        }else{
                            // 关注
                            String weibaCreateApi = "http://" + host + "index.php?app=api&mod=Weiba&act=create";
                            RequestParams requestParams = new RequestParams();
                            requestParams.addQueryStringParameter("id", weiba_id);
                            requestParams.addQueryStringParameter("oauth_token", oauth_token);
                            requestParams.addQueryStringParameter("oauth_token_secret", oauth_token_secret);

                            httpUtils.send(HttpRequest.HttpMethod.GET,
                                    weibaCreateApi,
                                    requestParams,
                                    new RequestCallBack<String>() {
                                        @Override
                                        public void onSuccess(ResponseInfo<String> responseInfo) {
                                            String state = responseInfo.result;

                                            if (state.equals("1")){
                                                Toast.makeText(context, "关注成功", Toast.LENGTH_SHORT).show();
                                                followBtn.setText("取消");
                                                followBtn.setBootstrapType("danger");

                                                follow_state[0] = "1";
                                            }else{
                                                Toast.makeText(context, "关注失败", Toast.LENGTH_SHORT).show();
                                            }
                                        }

                                        @Override
                                        public void onFailure(HttpException e, String s) {
                                            Toast.makeText(context, "网络异常", Toast.LENGTH_SHORT).show();
                                            Log.e("net error", e.toString() + s);
                                        }
                                    });
                        }
                    }
                });
            } catch (JSONException e) {
                Log.e("json error", e.toString());
            }

            return view;
        }
    }
}
