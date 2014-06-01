package com.WindHunter.tools;


import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.WH.xListView.XListView;
import com.WindHunter.R;
import com.WindHunter.WeiboActivity;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CommentList {
    // 每页 评论数
    private int count;

    // 页码
    private int page;

    // 评论类型
    private String type;

    private WHActivity context;

    private XListView commentList;

    private CommentAdapter commentAdapter;

    public CommentList(WHActivity context, XListView commentList){
        this.context = context;
        this.commentList = commentList;

        commentAdapter = new CommentAdapter(context, R.layout.comment_list_item);
        commentList.setAdapter(commentAdapter);

        this.count = 10;
    }

    public CommentList setCount(int count){
        this.count = count;
        return this;
    }

    public CommentList setType(String type){
        this.type = type;
        return this;
    }

    public void run(){
        initXListView();
        fillListView();
    }

    private void fillListView(){
        // 初始化为第一页
        page = 1;

        // 组装关注用户最新微博信息API
        String commentApi = "http://" + context.host + "index.php?app=api&mod=WeiboStatuses&act=" + type;
        RequestParams requestParams = new RequestParams();
        requestParams.addQueryStringParameter("count", count + "");
        requestParams.addQueryStringParameter("page", page + "");
        requestParams.addQueryStringParameter("oauth_token", context.oauth_token);
        requestParams.addQueryStringParameter("oauth_token_secret", context.oauth_token_secret);


        // 请求绘制ListView界面
        context.httpUtils.send(HttpRequest.HttpMethod.GET,
                commentApi,
                requestParams,
                new RequestCallBack<String>(){

                    @Override
                    public void onSuccess(ResponseInfo<String> stringResponseInfo) {
                        try {
                            JSONArray jsonArray = new JSONArray(stringResponseInfo.result);
                            if (jsonArray.length() != 0) {
                                commentAdapter.addAll(getCommentDataArray(jsonArray));
                                commentAdapter.notifyDataSetChanged();

                                // 页码加 1
                                page += 1;
                            }
                        } catch (JSONException e) {
                            Toast.makeText(context, "网络出错", Toast.LENGTH_SHORT).show();
                            Log.e("json error", e.toString());
                        }
                    }

                    @Override
                    public void onFailure(HttpException e, String s) {
                        Toast.makeText(context, "网络出错", Toast.LENGTH_SHORT).show();
                        Log.e("net error", e.toString() + s);
                    }
                });
    }

    private void initXListView(){

        // 启用上拉更多
        commentList.setPullLoadEnable(true);

        // 上下拉刷新
        commentList.setXListViewListener(new XListView.IXListViewListener() {
            @Override
            public void onRefresh() {
                // 下拉刷新

                // 初始化为第一页
                page = 1;

                // 组装微博API
                String commentApi = "http://" + context.host + "index.php?app=api&mod=WeiboStatuses&act=" + type;
                RequestParams requestParams = new RequestParams();
                requestParams.addQueryStringParameter("count", count + "");
                requestParams.addQueryStringParameter("page", page + "");
                requestParams.addQueryStringParameter("oauth_token", context.oauth_token);
                requestParams.addQueryStringParameter("oauth_token_secret", context.oauth_token_secret);


                // 请求绘制ListView界面
                context.httpUtils.send(HttpRequest.HttpMethod.GET,
                        commentApi,
                        requestParams,
                        new RequestCallBack<String>(){

                            @Override
                            public void onSuccess(ResponseInfo<String> stringResponseInfo) {
                                try {
                                    JSONArray jsonArray = new JSONArray(stringResponseInfo.result);
                                    if( jsonArray.length() == 0 ){
                                        Toast.makeText(context, "没有评论", Toast.LENGTH_SHORT).show();
                                    }else{
                                        commentAdapter.clear();

                                        commentAdapter.addAll(getCommentDataArray(jsonArray));
                                        commentAdapter.notifyDataSetChanged();

                                        // 页码加 1
                                        page += 1;
                                    }
                                } catch (JSONException e) {
                                    Toast.makeText(context, "网络出错", Toast.LENGTH_SHORT).show();
                                    Log.e("json error", e.toString());
                                }

                                commentList.stopRefresh();
                                commentList.setRefreshTime(new SimpleDateFormat().format(Calendar.getInstance().getTime()));
                            }

                            @Override
                            public void onFailure(HttpException e, String s) {
                                Toast.makeText(context, "网络出错", Toast.LENGTH_SHORT).show();
                                commentList.stopRefresh();
                                commentList.setRefreshTime(new SimpleDateFormat().format(Calendar.getInstance().getTime()));
                            }
                        });
            }

            @Override
            public void onLoadMore() {
                // 上拉更多


                // 组装API
                String commentApi = "http://" + context.host + "index.php?app=api&mod=WeiboStatuses&act=" + type;
                RequestParams requestParams = new RequestParams();
                requestParams.addQueryStringParameter("count", count + "");
                requestParams.addQueryStringParameter("page", page + "");
                requestParams.addQueryStringParameter("oauth_token", context.oauth_token);
                requestParams.addQueryStringParameter("oauth_token_secret", context.oauth_token_secret);

                context.httpUtils.send(HttpRequest.HttpMethod.GET,
                        commentApi,
                        requestParams,
                        new RequestCallBack<String>(){

                            @Override
                            public void onSuccess(ResponseInfo<String> stringResponseInfo) {
                                try {
                                    JSONArray jsonArray = new JSONArray(stringResponseInfo.result);
                                    if (jsonArray.length() == 0){
                                        //  没有更多
                                        Toast.makeText(context, "没有更多", Toast.LENGTH_SHORT).show();
                                        commentList.stopLoadMore();
                                    }else{
                                        commentAdapter.addAll(getCommentDataArray(jsonArray));
                                        commentAdapter.notifyDataSetChanged();

                                        commentList.stopLoadMore();

                                        // 页码增加
                                        page += 1;
                                    }
                                } catch (JSONException e) {
                                    Toast.makeText(context, "网络错误", Toast.LENGTH_SHORT).show();
                                    Log.e("json error", e.toString());
                                    commentList.stopLoadMore();
                                }
                            }

                            @Override
                            public void onFailure(HttpException e, String s) {
                                Toast.makeText(context, "网络错误", Toast.LENGTH_SHORT).show();
                                commentList.stopLoadMore();
                            }
                        });
            }
        });

        // 点击监听事件
        commentList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CommentData commentData = (CommentData)parent.getItemAtPosition(position);
                Intent intent = new Intent(context, WeiboActivity.class);
                intent.putExtra("feed_id", commentData.feed_id);

                context.startActivity(intent);
            }
        });
    }

    private class CommentAdapter extends ArrayAdapter<CommentData> {

        private int resource;
        private final LayoutInflater inflater;

        public CommentAdapter(Context context, int resource) {
            super(context, resource);
            this.resource = resource;
            inflater = LayoutInflater.from(context);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null){
                convertView = inflater.inflate(resource, parent, false);
            }
            CommentData commentData = getItem(position);

            ImageView avatar = (ImageView)convertView.findViewById(R.id.comment_list_item_avatar);
            TextView name = (TextView)convertView.findViewById(R.id.comment_list_item_name);
            TextView comment_content = (TextView)convertView.findViewById(R.id.comment_list_item_comment_content);
            TextView ctime = (TextView)convertView.findViewById(R.id.comment_list_item_ctime);


            context.bitmapUtils.display(avatar, commentData.avatar);
            name.setText(commentData.name);
            comment_content.setText(FaceUtils.getExpressionString(context, commentData.comment_content));
            ctime.setText(commentData.ctime);

            return convertView;
        }
    }

    private List<CommentData> getCommentDataArray(JSONArray jsonArray) throws JSONException {
        List<CommentData> items = new ArrayList<CommentData>();
        JSONObject jsonItem;

        for (int i = 0; i < jsonArray.length(); i++){
            jsonItem = jsonArray.getJSONObject(i);
            CommentData commentData = new CommentData();

            commentData.avatar = jsonItem.getJSONObject("user_info").getString("avatar_middle");
            commentData.name = jsonItem.getJSONObject("user_info").getString("uname");

            // 去掉content中的a标签
            String rowContent = jsonItem.getString("content");
            String realContent = rowContent.replaceAll("<a href[^>]*>", "");
            realContent = realContent.replaceAll("</a>", "");
            commentData.comment_content = realContent;

            commentData.feed_id = jsonItem.getJSONObject("sourceInfo").getString("source_id");
            commentData.ctime = jsonItem.getString("ctime");


            items.add(commentData);
        }

        return items;
    }

    private class CommentData {
        private String avatar;
        private String name;
        private String comment_content;
        private String feed_id;
        private String ctime;
    }

}
