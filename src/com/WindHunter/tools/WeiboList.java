package com.WindHunter.tools;


import android.content.Context;
import android.content.Intent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.WH.xListView.XListView;
import com.WindHunter.R;
import com.WindHunter.WeiboActivity;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.lidroid.xutils.view.annotation.ViewInject;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class WeiboList {

    // 每页微博数
    private int count;

    // 页码
    private int page;

    // 微博类型
    private String type;

    // 当微博类型为user_timeline时的user_id
    private String user_id;

    private WHActivity context;

    private XListView weiboList;

    // 微博列表适配器
    private WeiboAdapter weiboAdapter;

    public WeiboList(WHActivity context, XListView weiboList){
        this.context = context;
        this.weiboList = weiboList;

        // 设置好适配器
        weiboAdapter = new WeiboAdapter(context, R.layout.weibo_list_item);
        weiboList.setAdapter(weiboAdapter);

        // 默认情况下的值
        this.count = 10;
        this.type = "friends_timeline";
        this.user_id = context.uid;
    }

    public WeiboList setCount(int count){
        this.count = count;
        return this;
    }

    public WeiboList setType(String type){
        this.type = type;
        return this;
    }

    public WeiboList setUser_id(String user_id){
        this.user_id = user_id;
        return this;
    }

    public void run(){
        initXListView();
        fillListView();
    }

    public void fresh(){
        weiboAdapter.clear();
        fillListView();
    }

    private void initXListView(){

        // 启用上拉更多
        weiboList.setPullLoadEnable(true);

        // 上下拉刷新
        weiboList.setXListViewListener(new XListView.IXListViewListener() {
            @Override
            public void onRefresh() {
                // 下拉刷新

                // 初始化为第一页
                page = 1;

                // 组装微博API
                String weiboApi = "http://" + context.host + "index.php?app=api&mod=WeiboStatuses&act=" + type;
                RequestParams requestParams = new RequestParams();

                // 当为user_timeline时多加一个参数
                if (type.equals("user_timeline"))
                    requestParams.addQueryStringParameter("user_id", user_id);

                requestParams.addQueryStringParameter("count", count + "");
                requestParams.addQueryStringParameter("page", page + "");
                requestParams.addQueryStringParameter("oauth_token", context.oauth_token);
                requestParams.addQueryStringParameter("oauth_token_secret", context.oauth_token_secret);


                // 请求绘制ListView界面
                context.httpUtils.send(HttpRequest.HttpMethod.GET,
                        weiboApi,
                        requestParams,
                        new RequestCallBack<String>(){

                            @Override
                            public void onSuccess(ResponseInfo<String> stringResponseInfo) {
                                try {
                                    JSONArray jsonArray = new JSONArray(stringResponseInfo.result);
                                    if( jsonArray.length() == 0 ){
                                        Toast.makeText(context, "没有内容", Toast.LENGTH_SHORT).show();
                                    }else{
                                        weiboAdapter.clear();

                                        weiboAdapter.addAll(getWeiboDataArray(jsonArray));
                                        weiboAdapter.notifyDataSetChanged();

                                        // 页码加 1
                                        page += 1;
                                    }
                                } catch (JSONException e) {
                                    Toast.makeText(context, "网络出错", Toast.LENGTH_SHORT).show();
                                }

                                weiboList.stopRefresh();
                                // TODO: 时间有错
                                weiboList.setRefreshTime(new SimpleDateFormat("HH:MM:SS").format(new Date()));
                            }

                            @Override
                            public void onFailure(HttpException e, String s) {
                                Toast.makeText(context, "网络出错", Toast.LENGTH_SHORT).show();
                                weiboList.stopRefresh();
                                // TODO：时间有错
                                weiboList.setRefreshTime(new SimpleDateFormat("HH:MM:SS").format(new Date()));
                            }
                        });
            }

            @Override
            public void onLoadMore() {
                // 上拉更多


                // 组装API
                String weiboApi = "http://" + context.host + "index.php?app=api&mod=WeiboStatuses&act=" + type;
                RequestParams requestParams = new RequestParams();

                // 当为user_timeline时多加一个参数
                if (type.equals("user_timeline"))
                    requestParams.addQueryStringParameter("user_id", user_id);

                requestParams.addQueryStringParameter("count", count + "");
                requestParams.addQueryStringParameter("page", page + "");
                requestParams.addQueryStringParameter("oauth_token", context.oauth_token);
                requestParams.addQueryStringParameter("oauth_token_secret", context.oauth_token_secret);

                context.httpUtils.send(HttpRequest.HttpMethod.GET,
                        weiboApi,
                        requestParams,
                        new RequestCallBack<String>(){

                            @Override
                            public void onSuccess(ResponseInfo<String> stringResponseInfo) {
                                try {
                                    JSONArray jsonArray = new JSONArray(stringResponseInfo.result);
                                    if (jsonArray.length() == 0){
                                        //  没有更多
                                        Toast.makeText(context, "没有更多", Toast.LENGTH_SHORT).show();
                                        weiboList.stopLoadMore();
                                    }else{
                                        weiboAdapter.addAll(getWeiboDataArray(jsonArray));
                                        weiboAdapter.notifyDataSetChanged();

                                        weiboList.stopLoadMore();

                                        // 页码增加
                                        page += 1;
                                    }
                                } catch (JSONException e) {
                                    Toast.makeText(context, "网络错误", Toast.LENGTH_SHORT).show();
                                    weiboList.stopLoadMore();
                                }
                            }

                            @Override
                            public void onFailure(HttpException e, String s) {
                                Toast.makeText(context, "网络错误", Toast.LENGTH_SHORT).show();
                                weiboList.stopLoadMore();
                            }
                        });
            }
        });

        // 点击监听事件
        weiboList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                WeiboData weiboData = (WeiboData)parent.getItemAtPosition(position);
                Intent intent = new Intent(context, WeiboActivity.class);
                intent.putExtra("feed_id", weiboData.feed_id);

                context.startActivity(intent);
            }
        });
    }

    private void fillListView(){
        // 初始化为第一页
        page = 1;

        // 组装关注用户最新微博信息API
        String weiboApi = "http://" + context.host + "index.php?app=api&mod=WeiboStatuses&act=" + type;
        RequestParams requestParams = new RequestParams();

        if (type.equals("user_timeline"))
            requestParams.addQueryStringParameter("user_id", user_id);

        requestParams.addQueryStringParameter("count", count + "");
        requestParams.addQueryStringParameter("page", page + "");
        requestParams.addQueryStringParameter("oauth_token", context.oauth_token);
        requestParams.addQueryStringParameter("oauth_token_secret", context.oauth_token_secret);


        // 请求绘制ListView界面
        context.httpUtils.send(HttpRequest.HttpMethod.GET,
                weiboApi,
                requestParams,
                new RequestCallBack<String>(){

                    @Override
                    public void onSuccess(ResponseInfo<String> stringResponseInfo) {
                        try {
                            JSONArray jsonArray = new JSONArray(stringResponseInfo.result);
                            if( jsonArray.length() == 0 ){
                                Toast.makeText(context, "没有内容", Toast.LENGTH_SHORT).show();
                            }else{
                                weiboAdapter.addAll(getWeiboDataArray(jsonArray));
                                weiboAdapter.notifyDataSetChanged();

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

    // 自定义 ListView 数据适配器
    private class WeiboAdapter extends ArrayAdapter<WeiboData> {

        private int resource;
        private final LayoutInflater inflater;

        public WeiboAdapter(Context context, int resource) {
            super(context, resource);
            this.resource = resource;
            inflater = LayoutInflater.from(context);
        }

        @Override
        public View getView(int position, View row, ViewGroup parent) {

            // 用于加速列表的
            WeiboItemHolder holder = null;
            if (row == null){
                row = inflater.inflate(resource, parent, false);
                holder = new WeiboItemHolder();
                ViewUtils.inject(holder, row);
                row.setTag(holder);
            }else{
                holder = (WeiboItemHolder)row.getTag();
            }

            // 绘制每一行
            WeiboData weiboData = this.getItem(position);

            context.bitmapUtils.display(holder.weibo_item_avatar, weiboData.avatar);
            holder.weibo_item_uname.setText(weiboData.uname);
            holder.weibo_item_content.setText(weiboData.content);
            holder.weibo_item_ctime.setText(weiboData.ctime);
            holder.weibo_item_from.setText(switchFromCode(weiboData.from));
            holder.weibo_item_num.setText("赞(" + weiboData.digg_count + ") | 转发(" + weiboData.repost_count + ") | 评论(" + weiboData.comment_count
                    + ")");
            addImageToLayout(weiboData.attachUrls, holder.weibo_item_img);
            addRepostToLayout(weiboData.repostWeiboData, holder.weibo_item_repost, inflater);


            return row;
        }
    }

    private List<WeiboData> getWeiboDataArray(JSONArray jsonArray) throws JSONException {

        List<WeiboData> items = new ArrayList<WeiboData>();
        JSONObject jsonItem;

        for (int i = 0; i < jsonArray.length(); i++){
            jsonItem = jsonArray.getJSONObject(i);
            WeiboData weiboData = new WeiboData();

            weiboData.uname =  jsonItem.getString("uname");
            weiboData.avatar = jsonItem.getString("avatar_middle");

            // 空内容
            if (jsonItem.getString("feed_content").equals("null"))
                weiboData.content = "";
            else
                weiboData.content = jsonItem.getString("feed_content");

            weiboData.feed_id = jsonItem.getString("feed_id");
            weiboData.ctime = jsonItem.getString("ctime");
            weiboData.from = jsonItem.getString("from");
            weiboData.digg_count = jsonItem.getString("digg_count");
            weiboData.comment_count = jsonItem.getString("comment_count");
            weiboData.repost_count = jsonItem.getString("repost_count");
            weiboData.attachUrls = getAttachArray(jsonItem);
            weiboData.repostWeiboData = getRepostWeibo(jsonItem);

            items.add(weiboData);
        }

        return  items;
    }


    // 转发微博的数据结构
    private class RepostWeiboData {
        private String uname;
        private String ctime;
        private String content;
        private List<String> attachUrls;
        private String from;
        private String comment_count;
        private String repost_count;
    }


    // 微博的数据结构
    private class WeiboData {
        private String uname;               // 微博发布者
        private String avatar;              // 发布者头像
        private String content;             // 微博内容
        private String feed_id;             // 微博id
        private String ctime;               // 发布时间
        private String from;                // 来自什么平台
        private String digg_count;          // 赞   数量
        private String comment_count;       // 评论 数量
        private String repost_count;        // 转发 数量
        private List<String> attachUrls;    // 图片URL
        private RepostWeiboData repostWeiboData; // 转发的微博
    }

    // 持有者模式 用于加速列表
    private class WeiboItemHolder {
        @ViewInject(R.id.weibo_item_avatar)
        private ImageView weibo_item_avatar;

        @ViewInject(R.id.weibo_item_uname)
        private TextView weibo_item_uname;

        @ViewInject(R.id.weibo_item_content)
        private TextView weibo_item_content;

        @ViewInject(R.id.weibo_item_ctime)
        private TextView weibo_item_ctime;

        @ViewInject(R.id.weibo_item_from)
        private TextView weibo_item_from;

        @ViewInject(R.id.weibo_item_num)
        private TextView weibo_item_num;

        @ViewInject(R.id.weibo_item_img)
        private LinearLayout weibo_item_img;

        @ViewInject(R.id.weibo_item_repost)
        private LinearLayout weibo_item_repost;
    }

    private String switchFromCode(String from){
        if (from.equals("0"))
            return "来自网站";
        else if (from.equals("1"))
            return "来自手机网页版";
        else if (from.equals("2"))
            return "来自Android客户端";
        else if (from.equals("3"))
            return "来自iPhone客户端";
        else
            return "未知平台";
    }

    // 返回图片URL列表
    private List<String> getAttachArray(JSONObject jsonObject) throws JSONException {
        List<String> urls = new ArrayList<String>();
        if (jsonObject.getString("feedType").equals("postimage")){
            JSONArray jsonArray = jsonObject.getJSONArray("attach");

            for (int i = 0; i < jsonArray.length(); i++){
                urls.add(jsonArray.getJSONObject(i).getString("attach_middle"));
            }
        }

        return urls;
    }

    // 动态加载图片到布局
    private void addImageToLayout(List<String> attachUrls, LinearLayout layout){
        layout.removeAllViews();

        if (!attachUrls.isEmpty()){
            ImageView imageView;
            int counter = 0;
            for (String url : attachUrls){
                if (counter > 1){

                    //TODO:调整textview的位置
                    TextView textView = new TextView(context);
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    layoutParams.gravity = Gravity.BOTTOM;
                    layoutParams.bottomMargin = 5;
                    layout.addView(textView,layoutParams);
                    textView.setText("更多图片...");
                    break;
                }
                imageView = new ImageView(context);

                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(100,100);
                layoutParams.leftMargin = 5;
                layout.addView(imageView, layoutParams);
                context.bitmapUtils.display(imageView, url);
                counter++;
            }
        }

    }

    // 返回转发微博数据
    private RepostWeiboData getRepostWeibo(JSONObject jsonObject) throws JSONException {

        if (jsonObject.getString("feedType").equals("repost")){

            JSONObject repostWeibo = jsonObject.getJSONObject("transpond_data");
            RepostWeiboData repostWeiboData = new RepostWeiboData();

            repostWeiboData.uname = repostWeibo.getString("uname");
            repostWeiboData.ctime = repostWeibo.getString("ctime");

            // 空内容
            if (repostWeibo.getString("feed_content").equals("null"))
                repostWeiboData.content = "";
            else
                repostWeiboData.content = repostWeibo.getString("feed_content");

            repostWeiboData.from = repostWeibo.getString("from");
            repostWeiboData.repost_count = repostWeibo.getString("repost_count");
            repostWeiboData.comment_count = repostWeibo.getString("comment_count");
            repostWeiboData.attachUrls = getAttachArray(repostWeibo);

            return repostWeiboData;
        }else{
            return null;
        }
    }

    // 动态加载转发微博
    private void addRepostToLayout(RepostWeiboData repostWeiboData, LinearLayout layout, LayoutInflater inflater){
        layout.removeAllViews();

        if (repostWeiboData != null){
            View repostView = inflater.inflate(R.layout.weibo_list_item_repost, null);
            layout.addView(repostView);

            ((TextView)repostView.findViewById(R.id.weibo_item_repost_uname))
                    .setText("@" + repostWeiboData.uname);

            ((TextView)repostView.findViewById(R.id.weibo_item_repost_ctime))
                    .setText(repostWeiboData.ctime);

            ((TextView)repostView.findViewById(R.id.weibo_item_repost_content))
                    .setText(repostWeiboData.content);

            ((TextView)repostView.findViewById(R.id.weibo_item_repost_from))
                    .setText(switchFromCode(repostWeiboData.from));

            ((TextView)repostView.findViewById(R.id.weibo_item_repost_num))
                    .setText("转发(" + repostWeiboData.repost_count + ") | 评论(" + repostWeiboData.comment_count + ")");

            LinearLayout img = (LinearLayout)repostView.findViewById(R.id.weibo_item_repost_img);
            addImageToLayout(repostWeiboData.attachUrls, img);
        }
    }
}
