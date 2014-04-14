package com.WindHunter;



import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import com.WH.xListView.XListView;
import com.WindHunter.tools.WHActivity;
import com.beardedhen.androidbootstrap.BootstrapButton;
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


public class MainActivity extends WHActivity {

    // 每页10条
    private final String COUNT = "10";
    // 页码
    private int page;

    private WeiboAdapter weiboAdapter;

    // ListView
    @ViewInject(R.id.weibo_list)
    XListView weiboList;


    // 绘制ActionBar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // 组装个人信息API 请求参数
        String userShowApi = "http://" + host + "index.php?app=api&mod=User&act=show";
        RequestParams requestParams = new RequestParams();
        requestParams.addQueryStringParameter("user_id", uid);
        requestParams.addQueryStringParameter("oauth_token", oauth_token);
        requestParams.addQueryStringParameter("oauth_token_secret", oauth_token_secret);

        // 完成请求并填充title
        httpUtils.send(HttpRequest.HttpMethod.GET,
                userShowApi,
                requestParams,
                new RequestCallBack<String>(){

                    @Override
                    public void onSuccess(ResponseInfo<String> stringResponseInfo) {
                        try {
                            JSONObject jsonObject = new JSONObject(stringResponseInfo.result);

                            getSupportActionBar().setTitle(jsonObject.getString("uname"));
                        } catch (JSONException e) {
                            Toast.makeText(MainActivity.this, "网络出错", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(HttpException e, String s) {
                        Toast.makeText(MainActivity.this, "网络出错", Toast.LENGTH_SHORT).show();
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


        // 初始化列表数据适配器
        weiboAdapter = new WeiboAdapter(MainActivity.this, R.layout.weibo_list_item);
        weiboList.setAdapter(weiboAdapter);


        // 加载XListView
        initXListView();


        // 填充列表
        firstFillListView();

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

                // 组装关注用户最新微博信息API
                String friendsTimeLineApi = "http://" + host + "index.php?app=api&mod=WeiboStatuses&act=friends_timeline";
                RequestParams requestParams = new RequestParams();
                requestParams.addQueryStringParameter("mid", uid);
                requestParams.addQueryStringParameter("count", COUNT);
                requestParams.addQueryStringParameter("page", page + "");
                requestParams.addQueryStringParameter("oauth_token", oauth_token);
                requestParams.addQueryStringParameter("oauth_token_secret", oauth_token_secret);


                // 请求绘制ListView界面
                httpUtils.send(HttpRequest.HttpMethod.GET,
                        friendsTimeLineApi,
                        requestParams,
                        new RequestCallBack<String>(){

                            @Override
                            public void onSuccess(ResponseInfo<String> stringResponseInfo) {
                                try {
                                    JSONArray jsonArray = new JSONArray(stringResponseInfo.result);
                                    if( jsonArray.length() == 0 ){
                                        Toast.makeText(MainActivity.this, "没有内容", Toast.LENGTH_SHORT).show();
                                    }else{
                                        weiboAdapter.clear();

                                        weiboAdapter.addAll(getWeiboDataArray(jsonArray));
                                        weiboAdapter.notifyDataSetChanged();

                                        // 页码加 1
                                        page += 1;
                                    }
                                } catch (JSONException e) {
                                    Toast.makeText(MainActivity.this, "网络出错", Toast.LENGTH_SHORT).show();
                                }

                                weiboList.stopRefresh();
                                // TODO: 时间有错
                                weiboList.setRefreshTime(new SimpleDateFormat("HH:MM:SS").format(new Date()));
                            }

                            @Override
                            public void onFailure(HttpException e, String s) {
                                Toast.makeText(MainActivity.this, "网络出错", Toast.LENGTH_SHORT).show();
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
                String friendsTimeLineApi = "http://" + host + "index.php?app=api&mod=WeiboStatuses&act=friends_timeline";
                RequestParams requestParams = new RequestParams();
                requestParams.addQueryStringParameter("mid", uid);
                requestParams.addQueryStringParameter("count", COUNT);
                requestParams.addQueryStringParameter("page", page + "");
                requestParams.addQueryStringParameter("oauth_token", oauth_token);
                requestParams.addQueryStringParameter("oauth_token_secret", oauth_token_secret);

                httpUtils.send(HttpRequest.HttpMethod.GET,
                        friendsTimeLineApi,
                        requestParams,
                        new RequestCallBack<String>(){

                            @Override
                            public void onSuccess(ResponseInfo<String> stringResponseInfo) {
                                try {
                                    JSONArray jsonArray = new JSONArray(stringResponseInfo.result);
                                    if (jsonArray.length() == 0){
                                        //  没有更多
                                        Toast.makeText(MainActivity.this, "没有更多", Toast.LENGTH_SHORT).show();
                                        weiboList.stopLoadMore();
                                    }else{
                                        weiboAdapter.addAll(getWeiboDataArray(jsonArray));
                                        weiboAdapter.notifyDataSetChanged();

                                        weiboList.stopLoadMore();

                                        // 页码增加
                                        page += 1;
                                    }
                                } catch (JSONException e) {
                                    Toast.makeText(MainActivity.this, "网络错误", Toast.LENGTH_SHORT).show();
                                    weiboList.stopLoadMore();
                                }
                            }

                            @Override
                            public void onFailure(HttpException e, String s) {
                                Toast.makeText(MainActivity.this, "网络错误", Toast.LENGTH_SHORT).show();
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
                Intent intent = new Intent(MainActivity.this, WeiboActivity.class);
                intent.putExtra("feed_id", weiboData.feed_id);

                MainActivity.this.startActivity(intent);
            }
        });
    }


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
    }

    private List<WeiboData> getWeiboDataArray(JSONArray jsonArray) throws JSONException {

        List<WeiboData> items = new ArrayList<WeiboData>();
        JSONObject jsonItem;

        for (int i = 0; i < jsonArray.length(); i++){
            jsonItem = jsonArray.getJSONObject(i);
            WeiboData weiboData = new WeiboData();

            weiboData.uname =  jsonItem.getString("uname");
            weiboData.avatar = jsonItem.getString("avatar_middle");
            weiboData.content = jsonItem.getString("feed_content");
            weiboData.feed_id = jsonItem.getString("feed_id");
            weiboData.ctime = jsonItem.getString("ctime");
            weiboData.from = jsonItem.getString("from");
            weiboData.digg_count = jsonItem.getString("digg_count");
            weiboData.comment_count = jsonItem.getString("comment_count");
            weiboData.repost_count = jsonItem.getString("repost_count");
            weiboData.attachUrls = getAttachArray(jsonItem);

            items.add(weiboData);
        }

        return  items;
    }

    // 自定义 ListView 数据适配器
    private class WeiboAdapter extends ArrayAdapter<WeiboData>{

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

            bitmapUtils.display(holder.weibo_item_avatar, weiboData.avatar);
            holder.weibo_item_uname.setText(weiboData.uname);
            holder.weibo_item_content.setText(weiboData.content);
            holder.weibo_item_ctime.setText(weiboData.ctime);
            holder.weibo_item_from.setText(switchFromCode(weiboData.from));
            holder.weibo_item_num.setText("赞(" + weiboData.digg_count + ") | 转发(" + weiboData.repost_count + ") | 评论(" + weiboData.comment_count
                    + ")");
            addImageToLayout(weiboData.attachUrls, holder.weibo_item_img);


            return row;
        }
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
    }

    // 首次填充ListView
    private void firstFillListView(){
        // 初始化为第一页
        page = 1;

        // 组装关注用户最新微博信息API
        String friendsTimeLineApi = "http://" + host + "index.php?app=api&mod=WeiboStatuses&act=friends_timeline";
        RequestParams requestParams = new RequestParams();
        requestParams.addQueryStringParameter("mid", uid);
        requestParams.addQueryStringParameter("count", COUNT);
        requestParams.addQueryStringParameter("page", page + "");
        requestParams.addQueryStringParameter("oauth_token", oauth_token);
        requestParams.addQueryStringParameter("oauth_token_secret", oauth_token_secret);


        // 请求绘制ListView界面
        httpUtils.send(HttpRequest.HttpMethod.GET,
                friendsTimeLineApi,
                requestParams,
                new RequestCallBack<String>(){

                    @Override
                    public void onSuccess(ResponseInfo<String> stringResponseInfo) {
                        try {
                            JSONArray jsonArray = new JSONArray(stringResponseInfo.result);
                            if( jsonArray.length() == 0 ){
                                Toast.makeText(MainActivity.this, "没有内容", Toast.LENGTH_SHORT).show();
                            }else{
                                weiboAdapter.addAll(getWeiboDataArray(jsonArray));
                                weiboAdapter.notifyDataSetChanged();

                                // 页码加 1
                                page += 1;
                            }
                        } catch (JSONException e) {
                            Toast.makeText(MainActivity.this, "网络出错", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(HttpException e, String s) {
                        Toast.makeText(MainActivity.this, "网络出错", Toast.LENGTH_SHORT).show();
                    }
                });
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
                    TextView textView = new TextView(this);
                    layout.addView(textView);
                    textView.setText("点击查看更多");
                    break;
                }
                imageView = new ImageView(this);

                // TODO: 图片间需要间隙
                layout.addView(imageView, new LinearLayout.LayoutParams(100, 100));
                bitmapUtils.display(imageView, url);
                counter++;
            }
        }

    }
}
