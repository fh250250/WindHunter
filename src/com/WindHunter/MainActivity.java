package com.WindHunter;


import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
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
import com.slidingmenu.lib.SlidingMenu;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends Activity {

    private String host, oauth_token, oauth_token_secret, uid;
    private BitmapUtils bitmapUtils;

    // 个人头像
    @ViewInject(R.id.avatar)
    ImageView avatar;

    // ListView
    @ViewInject(R.id.weibo_list)
    ListView weiboList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // 注入Activity
        ViewUtils.inject(this);

        // 配置BitmapUtils
        initBitmapUtils();

        // 加载slideMenu
        initSlideMenu();

        // 从全局对象中获取认证数据
        SharedPreferences settings = getSharedPreferences("settings", MODE_PRIVATE);
        host = settings.getString("Host", "demo.thinksns.com/t3/");
        oauth_token = settings.getString("oauth_token", "");
        oauth_token_secret = settings.getString("oauth_token_secret", "");
        uid = settings.getString("uid", "");

        // 组装个人信息API 请求参数
        String userShowApi = "http://" + host + "index.php?app=api&mod=User&act=show";
        RequestParams requestParams = new RequestParams();
        requestParams.addQueryStringParameter("user_id", uid);
        requestParams.addQueryStringParameter("oauth_token", oauth_token);
        requestParams.addQueryStringParameter("oauth_token_secret", oauth_token_secret);

        // 完成请求并绘制个人信息界面
        HttpUtils http = new HttpUtils();
        http.send(HttpRequest.HttpMethod.GET,
                userShowApi,
                requestParams,
                new RequestCallBack<String>(){

                    @Override
                    public void onSuccess(ResponseInfo<String> stringResponseInfo) {
                        try {
                            JSONObject jsonObject = new JSONObject(stringResponseInfo.result);
                            String avatarUrl = jsonObject.getString("avatar_tiny");

                            bitmapUtils.display(avatar, avatarUrl);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(HttpException e, String s) {
                        Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_LONG).show();
                    }
                });


        // 组装关注用户最新微博信息API
        String friendsTimeLineApi = "http://" + host + "index.php?app=api&mod=WeiboStatuses&act=friends_timeline";
        RequestParams requestParams1 = new RequestParams();
        requestParams1.addQueryStringParameter("mid", uid);
        requestParams1.addQueryStringParameter("oauth_token", oauth_token);
        requestParams1.addQueryStringParameter("oauth_token_secret", oauth_token_secret);


        // 请求绘制ListView界面
        http.send(HttpRequest.HttpMethod.GET,
                friendsTimeLineApi,
                requestParams1,
                new RequestCallBack<String>(){

                    @Override
                    public void onSuccess(ResponseInfo<String> stringResponseInfo) {
                        try {
                            JSONArray jsonArray = new JSONArray(stringResponseInfo.result);
                            if( jsonArray.length() == 0 ){
                                // TODO: 没有微博
                            }else{
                                // 解析数据并填充
                                WeiboAdapter weiboAdapter = new WeiboAdapter(MainActivity.this,
                                                                             R.layout.weibo_list_item,
                                                                             getWeiboDataArray(jsonArray));
                                weiboList.setAdapter(weiboAdapter);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(HttpException e, String s) {

                    }
                });
    }

    private void initSlideMenu(){
        final SlidingMenu menu=new SlidingMenu(this);
        menu.setMode(SlidingMenu.LEFT);
        menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
        menu.setShadowWidthRes(R.dimen.shadow_width);
        menu.setShadowDrawable(R.drawable.shadow);
        menu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
        menu.setBehindWidth(200);
        menu.setFadeDegree(0.35f);
        menu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
        menu.setMenu(R.layout.menu);
    }

    private void initBitmapUtils(){

        // 生成BitmapUtils实体
        bitmapUtils = new BitmapUtils(this);

        // 配置位图显示动画
        ScaleAnimation animation = new ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setDuration(800);
        bitmapUtils.configDefaultImageLoadAnimation(animation);

        // 占位图片
        // TODO: 需添加占位图片和加载失败图片
        bitmapUtils.configDefaultLoadingImage(R.drawable.ic_launcher);
        bitmapUtils.configDefaultBitmapConfig(Bitmap.Config.RGB_565);
    }

    private class WeiboData {
        private String uname;
        private String avatar;
        private String content;
    }

    private List<WeiboData> getWeiboDataArray(JSONArray jsonArray) throws JSONException {

        List<WeiboData> items = new ArrayList<WeiboData>();
        JSONObject jsonItem;

        for (int i = 0; i < jsonArray.length(); i++){
            jsonItem = jsonArray.getJSONObject(i);
            WeiboData weiboData = new WeiboData();

            weiboData.uname =  jsonItem.getString("uname");
            weiboData.avatar = jsonItem.getString("avatar_small");
            weiboData.content = jsonItem.getString("content");

            items.add(weiboData);
        }

        return  items;
    }

    // 自定义 ListView 数据适配器
    private class WeiboAdapter extends ArrayAdapter<WeiboData>{

        private int resource;
        private List<WeiboData> items;
        private final LayoutInflater inflater;

        public WeiboAdapter(Context context, int resource, List<WeiboData> items) {
            super(context, resource, items);
            this.resource = resource;
            this.items = items;
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
            WeiboData weiboData = items.get(position);

            bitmapUtils.display(holder.weibo_item_avatar, weiboData.avatar);
            holder.weibo_item_uname.setText(weiboData.uname);
            holder.weibo_item_content.setText(weiboData.content);

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
    }
}
