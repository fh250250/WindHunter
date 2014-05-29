package com.WindHunter.tools;


import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.WH.xListView.XListView;
import com.WindHunter.PostDetailActivity;
import com.WindHunter.R;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class PostsList {

    private WeibaBaseActivity context;
    private String weiba_id;
    private XListView xListView;
    private PostsAdapter adapter;
    private int count;
    private int page;

    public PostsList(WeibaBaseActivity context, XListView xListView){
        this.context = context;
        this.xListView = xListView;

        this.count = 10;
        this.page = 1;

        xListView.setPullLoadEnable(true);
        xListView.setPullRefreshEnable(true);
    }

    public PostsList setWeibaID(String id){
        this.weiba_id = id;
        return this;
    }

    public PostsList setCount(int count){
        this.count = count;
        return this;
    }

    public void run(){
        init();
        setListener();
    }

    private void setListener(){
        xListView.setXListViewListener(new XListView.IXListViewListener() {
            @Override
            public void onRefresh() {
                page = 1;

                String api = "http://" + context.host + "index.php?app=api&mod=Weiba&act=get_posts";
                RequestParams requestParams = new RequestParams();
                requestParams.addQueryStringParameter("id", weiba_id);
                requestParams.addQueryStringParameter("count", count + "");
                requestParams.addQueryStringParameter("page", page + "");
                requestParams.addQueryStringParameter("oauth_token", context.oauth_token);
                requestParams.addQueryStringParameter("oauth_token_secret", context.oauth_token_secret);

                context.httpUtils.send(HttpRequest.HttpMethod.GET,
                        api,
                        requestParams,
                        new RequestCallBack<String>() {
                            @Override
                            public void onSuccess(ResponseInfo<String> responseInfo) {
                                try {
                                    JSONArray jsonArray = new JSONArray(responseInfo.result);

                                    if (jsonArray.length() == 0){
                                        Toast.makeText(context, "没有内容", Toast.LENGTH_SHORT).show();
                                    }else{
                                        adapter.setJsonArray(jsonArray);
                                        adapter.notifyDataSetChanged();
                                    }
                                } catch (JSONException e) {
                                    Toast.makeText(context, "网络异常", Toast.LENGTH_SHORT).show();
                                    Log.e("json error", e.toString());
                                }
                                xListView.stopRefresh();
                                xListView.setRefreshTime(new SimpleDateFormat().format(Calendar.getInstance().getTime()));
                            }

                            @Override
                            public void onFailure(HttpException e, String s) {
                                xListView.stopRefresh();
                                xListView.setRefreshTime(new SimpleDateFormat().format(Calendar.getInstance().getTime()));
                                Toast.makeText(context, "网络异常", Toast.LENGTH_SHORT).show();
                                Log.e("net error", e.toString() + s);
                            }
                        });
            }

            @Override
            public void onLoadMore() {
                page ++;

                String api = "http://" + context.host + "index.php?app=api&mod=Weiba&act=get_posts";
                RequestParams requestParams = new RequestParams();
                requestParams.addQueryStringParameter("id", weiba_id);
                requestParams.addQueryStringParameter("count", count + "");
                requestParams.addQueryStringParameter("page", page + "");
                requestParams.addQueryStringParameter("oauth_token", context.oauth_token);
                requestParams.addQueryStringParameter("oauth_token_secret", context.oauth_token_secret);

                context.httpUtils.send(HttpRequest.HttpMethod.GET,
                        api,
                        requestParams,
                        new RequestCallBack<String>() {
                            @Override
                            public void onSuccess(ResponseInfo<String> responseInfo) {
                                try {
                                    JSONArray jsonArray = new JSONArray(responseInfo.result);

                                    if (jsonArray.length() == 0){
                                        Toast.makeText(context, "没有更多", Toast.LENGTH_SHORT).show();
                                        page --;
                                    }else{
                                        adapter.add(jsonArray);
                                        adapter.notifyDataSetChanged();
                                    }
                                    xListView.stopLoadMore();
                                } catch (JSONException e) {
                                    page --;
                                    xListView.stopLoadMore();
                                    Toast.makeText(context, "网络异常", Toast.LENGTH_SHORT).show();
                                    Log.e("json error", e.toString());
                                }
                            }

                            @Override
                            public void onFailure(HttpException e, String s) {
                                page --;
                                xListView.stopLoadMore();
                                Toast.makeText(context, "网络异常", Toast.LENGTH_SHORT).show();
                                Log.e("net error", e.toString() + s);
                            }
                        });
            }
        });

        xListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                JSONObject jsonObject = (JSONObject)adapterView.getItemAtPosition(i);
                try {
                    String post_id = jsonObject.getString("post_id");

                    Intent intent = new Intent(context, PostDetailActivity.class);
                    intent.putExtra("post_id", post_id);
                    context.startActivity(intent);
                } catch (JSONException e) {
                    Log.e("json error", e.toString());
                }
            }
        });
    }

    private void init(){
        String api = "http://" + context.host + "index.php?app=api&mod=Weiba&act=get_posts";
        RequestParams requestParams = new RequestParams();
        requestParams.addQueryStringParameter("id", weiba_id);
        requestParams.addQueryStringParameter("count", count + "");
        requestParams.addQueryStringParameter("page", page + "");
        requestParams.addQueryStringParameter("oauth_token", context.oauth_token);
        requestParams.addQueryStringParameter("oauth_token_secret", context.oauth_token_secret);

        context.httpUtils.send(HttpRequest.HttpMethod.GET,
                api,
                requestParams,
                new RequestCallBack<String>() {
                    @Override
                    public void onSuccess(ResponseInfo<String> responseInfo) {
                        try {
                            JSONArray jsonArray = new JSONArray(responseInfo.result);

                            if (jsonArray.length() == 0){
                                Toast.makeText(context, "没有帖子", Toast.LENGTH_SHORT).show();
                            }else{
                                adapter = new PostsAdapter(context, jsonArray);
                                xListView.setAdapter(adapter);
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


    private class PostsAdapter extends BaseAdapter {

        private WeibaBaseActivity context;
        private JSONArray jsonArray;

        public PostsAdapter(WeibaBaseActivity context, JSONArray jsonArray){
            this.context = context;
            this.jsonArray = jsonArray;
        }

        public void add(JSONArray array) throws JSONException {
            for (int i = 0; i < array.length(); i++){
                JSONObject object = array.getJSONObject(i);
                jsonArray.put(object);
            }
        }

        public void setJsonArray(JSONArray jsonArray){
            this.jsonArray = jsonArray;
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
                view = LayoutInflater.from(context).inflate(R.layout.weiba_posts_item, null);
            }

            JSONObject jsonObject;
            try {
                jsonObject = jsonArray.getJSONObject(i);

                ImageView avatarView = (ImageView)view.findViewById(R.id.water_fall_cell_avatar);
                TextView nameView = (TextView)view.findViewById(R.id.water_fall_cell_name);
                TextView titleView = (TextView)view.findViewById(R.id.water_fall_cell_title);
                TextView timeView = (TextView)view.findViewById(R.id.water_fall_cell_time);
                TextView countView = (TextView)view.findViewById(R.id.water_fall_cell_count);

                String avatar = jsonObject.getJSONObject("author_info").getString("avatar_middle");
                String name = jsonObject.getJSONObject("author_info").getString("uname");
                String title = jsonObject.getString("title");
                String time = jsonObject.getString("post_time");
                String replyCount = jsonObject.getString("reply_count");
                String readCount = jsonObject.getString("read_count");

                context.bitmapUtils.display(avatarView, avatar);
                nameView.setText(name);
                titleView.setText(title);
                timeView.setText(context.getTimeFromPHP(time));


                countView.setText("浏览数: " + readCount + " | 评论数: " + replyCount);

            } catch (JSONException e) {
                Log.e("json error", e.toString());
            }


            return view;
        }
    }

}
