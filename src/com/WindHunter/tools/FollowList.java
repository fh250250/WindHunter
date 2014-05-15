package com.WindHunter.tools;


import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.WH.xListView.XListView;
import com.WindHunter.R;
import com.WindHunter.UserActivity;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class FollowList {
    // 每页关注数
    private int count;

    // 页码
    private int page;

    // 关注类型
    private String type;

    private String user_id;
    private String key;
    private String mod;

    private WHActivity context;

    private XListView followList;

    // 关注列表适配器
    private FollowAdapter followAdapter;

    public FollowList(WHActivity context, XListView followList){
        this.context = context;
        this.followList = followList;

        // 设置好适配器
        followAdapter = new FollowAdapter(context, R.layout.follow_list_item);
        followList.setAdapter(followAdapter);

        // 默认情况下的值
        this.count = 10;
        this.mod = "User";
    }

    public FollowList setCount(int count){
        this.count = count;
        return this;
    }

    public FollowList setType(String type){
        this.type = type;
        return this;
    }

    public FollowList setUserID(String user_id){
        this.user_id = user_id;
        return this;
    }

    public FollowList setKey(String key){
        this.key = key;
        return this;
    }
    public FollowList setMod(String mod){
        this.mod = mod;
        return this;
    }

    public void run(){
        initXListView();
        fillListView();
    }

    private void initXListView(){

        // 启用上拉更多 关闭下拉刷新
        followList.setPullLoadEnable(true);
        followList.setPullRefreshEnable(false);

        followList.setXListViewListener(new XListView.IXListViewListener() {
            @Override
            public void onRefresh() {

            }

            @Override
            public void onLoadMore() {
                // 组装API
                String followApi = "http://" + context.host + "index.php?app=api&mod=" + mod + "&act=" + type;
                RequestParams requestParams = new RequestParams();

                if (mod.equals("User")){
                    requestParams.addQueryStringParameter("user_id", user_id);
                }else if (mod.equals("WeiboStatuses")){
                    requestParams.addQueryStringParameter("key", key);
                }
                requestParams.addQueryStringParameter("count", count + "");
                requestParams.addQueryStringParameter("page", page + "");
                requestParams.addQueryStringParameter("oauth_token", context.oauth_token);
                requestParams.addQueryStringParameter("oauth_token_secret", context.oauth_token_secret);


                context.httpUtils.send(HttpRequest.HttpMethod.GET,
                        followApi,
                        requestParams,
                        new RequestCallBack<String>(){

                            @Override
                            public void onSuccess(ResponseInfo<String> stringResponseInfo) {
                                try {
                                    JSONArray jsonArray = new JSONArray(stringResponseInfo.result);
                                    if (jsonArray.length() == 0){
                                        //  没有更多
                                        Toast.makeText(context, "没有更多", Toast.LENGTH_SHORT).show();
                                        followList.stopLoadMore();
                                    }else{
                                        followAdapter.addAll(getFollowDataArray(jsonArray));
                                        followAdapter.notifyDataSetChanged();

                                        followList.stopLoadMore();

                                        // 页码增加
                                        page += 1;
                                    }
                                } catch (JSONException e) {
                                    Toast.makeText(context, "网络错误", Toast.LENGTH_SHORT).show();
                                    followList.stopLoadMore();
                                }
                            }

                            @Override
                            public void onFailure(HttpException e, String s) {
                                Toast.makeText(context, "网络错误", Toast.LENGTH_SHORT).show();
                                followList.stopLoadMore();
                            }
                        });
            }
        });

        followList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                FollowData followData = (FollowData)adapterView.getItemAtPosition(i);

                Intent intent = new Intent(context, UserActivity.class);
                intent.putExtra("user_id", followData.user_id);
                context.startActivity(intent);
            }
        });

    }


    private void fillListView(){
        // 初始化为第一页
        page = 1;

        // 组装关注用户API
        String followApi = "http://" + context.host + "index.php?app=api&mod=" + mod + "&act=" + type;
        RequestParams requestParams = new RequestParams();

        if (mod.equals("User")){
            requestParams.addQueryStringParameter("user_id", user_id);
        }else if (mod.equals("WeiboStatuses")){
            requestParams.addQueryStringParameter("key", key);
        }
        requestParams.addQueryStringParameter("count", count + "");
        requestParams.addQueryStringParameter("page", page + "");
        requestParams.addQueryStringParameter("oauth_token", context.oauth_token);
        requestParams.addQueryStringParameter("oauth_token_secret", context.oauth_token_secret);


        // 请求绘制ListView界面
        context.httpUtils.send(HttpRequest.HttpMethod.GET,
                followApi,
                requestParams,
                new RequestCallBack<String>(){

                    @Override
                    public void onSuccess(ResponseInfo<String> stringResponseInfo) {
                        try {
                            JSONArray jsonArray = new JSONArray(stringResponseInfo.result);
                            if( jsonArray.length() == 0 ){
                                Toast.makeText(context, "没有", Toast.LENGTH_SHORT).show();
                            }else{
                                followAdapter.addAll(getFollowDataArray(jsonArray));
                                followAdapter.notifyDataSetChanged();

                                // 页码加 1
                                page += 1;
                            }
                        } catch (JSONException e) {
                            Toast.makeText(context, "网络出错", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(HttpException e, String s) {
                        Toast.makeText(context, "网络出错", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private List<FollowData> getFollowDataArray(JSONArray jsonArray) throws JSONException {
        List<FollowData> items = new ArrayList<FollowData>();
        JSONObject jsonItem;

        for (int i = 0; i < jsonArray.length(); i++){
            jsonItem = jsonArray.getJSONObject(i);
            FollowData followData = new FollowData();

            followData.name = jsonItem.getString("uname");
            followData.avatar = jsonItem.getString("avatar_middle");
            followData.user_id = jsonItem.getString("uid");

            if(jsonItem.isNull("intro")){
                followData.intro = "";
            }else {
                followData.intro = jsonItem.getString("intro");
            }

            if (jsonItem.getString("sex").equals("1")){
                followData.sex = "男";
            }else {
                followData.sex = "女";
            }


            items.add(followData);
        }

        return items;
    }

    private class FollowAdapter extends ArrayAdapter<FollowData> {

        private int resource;
        private final LayoutInflater inflater;

        public FollowAdapter(Context context, int resource) {
            super(context, resource);
            this.resource = resource;
            inflater = LayoutInflater.from(context);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null){
                convertView = inflater.inflate(resource, parent, false);
            }

            FollowData followData = getItem(position);

            ImageView avatar = (ImageView)convertView.findViewById(R.id.follow_list_item_avatar);
            TextView name = (TextView)convertView.findViewById(R.id.follow_list_item_name);
            TextView intro = (TextView)convertView.findViewById(R.id.follow_list_item_intro);
            TextView sex = (TextView)convertView.findViewById(R.id.follow_list_item_sex);

            context.bitmapUtils.display(avatar, followData.avatar);
            name.setText(followData.name);
            intro.setText("个人简介:"+followData.intro);
            sex.setText(followData.sex);

            return convertView;
        }
    }

    private class FollowData {
        private String name;
        private String avatar;
        private String intro;
        private String sex;
        private String user_id;
    }
}
