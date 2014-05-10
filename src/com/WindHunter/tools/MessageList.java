package com.WindHunter.tools;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.WH.xListView.XListView;
import com.WindHunter.MessageDetailActivity;
import com.WindHunter.R;
import com.beardedhen.androidbootstrap.FontAwesomeText;
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

public class MessageList {
    // 每页 评论数
    private int count;

    // 页码
    private int page;

    private WHActivity context;

    private XListView messageList;

    private MessageAdapter messageAdapter;

    public MessageList(WHActivity context, XListView messageList){
        this.context = context;
        this.messageList = messageList;

        messageAdapter = new MessageAdapter(context, R.layout.message_list_item);
        messageList.setAdapter(messageAdapter);

        this.count = 10;
    }

    public MessageList setCount(int count){
        this.count = count;
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
        String messageApi = "http://" + context.host + "index.php?app=api&mod=Message&act=get_message_list";
        RequestParams requestParams = new RequestParams();
        requestParams.addQueryStringParameter("count", count + "");
        requestParams.addQueryStringParameter("page", page + "");
        requestParams.addQueryStringParameter("oauth_token", context.oauth_token);
        requestParams.addQueryStringParameter("oauth_token_secret", context.oauth_token_secret);


        // 请求绘制ListView界面
        context.httpUtils.send(HttpRequest.HttpMethod.GET,
                messageApi,
                requestParams,
                new RequestCallBack<String>(){

                    @Override
                    public void onSuccess(ResponseInfo<String> stringResponseInfo) {
                        try {
                            if (!stringResponseInfo.result.equals("null")) {
                                JSONArray jsonArray = new JSONArray(stringResponseInfo.result);
                                messageAdapter.addAll(getMessageDataArray(jsonArray));
                                messageAdapter.notifyDataSetChanged();

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

    private void initXListView(){

        // 启用上拉更多
        messageList.setPullLoadEnable(true);

        // 上下拉刷新
        messageList.setXListViewListener(new XListView.IXListViewListener() {
            @Override
            public void onRefresh() {
                // 下拉刷新

                // 初始化为第一页
                page = 1;

                // 组装微博API
                String messageApi = "http://" + context.host + "index.php?app=api&mod=Message&act=get_message_list";
                RequestParams requestParams = new RequestParams();
                requestParams.addQueryStringParameter("count", count + "");
                requestParams.addQueryStringParameter("page", page + "");
                requestParams.addQueryStringParameter("oauth_token", context.oauth_token);
                requestParams.addQueryStringParameter("oauth_token_secret", context.oauth_token_secret);


                // 请求绘制ListView界面
                context.httpUtils.send(HttpRequest.HttpMethod.GET,
                        messageApi,
                        requestParams,
                        new RequestCallBack<String>() {

                            @Override
                            public void onSuccess(ResponseInfo<String> stringResponseInfo) {
                                try {
                                    if (stringResponseInfo.result.equals("null")){
                                        Toast.makeText(context, "没有对话", Toast.LENGTH_SHORT).show();
                                    }else{
                                        JSONArray jsonArray = new JSONArray(stringResponseInfo.result);
                                        messageAdapter.clear();

                                        messageAdapter.addAll(getMessageDataArray(jsonArray));
                                        messageAdapter.notifyDataSetChanged();

                                        // 页码加 1
                                        page += 1;
                                    }
                                } catch (JSONException e) {
                                    Toast.makeText(context, "网络出错", Toast.LENGTH_SHORT).show();
                                }

                                messageList.stopRefresh();
                                messageList.setRefreshTime(new SimpleDateFormat().format(Calendar.getInstance().getTime()));
                            }

                            @Override
                            public void onFailure(HttpException e, String s) {
                                Toast.makeText(context, "网络出错", Toast.LENGTH_SHORT).show();
                                messageList.stopRefresh();
                                messageList.setRefreshTime(new SimpleDateFormat().format(Calendar.getInstance().getTime()));
                            }
                        });
            }

            @Override
            public void onLoadMore() {
                // 上拉更多


                // 组装API
                String messageApi = "http://" + context.host + "index.php?app=api&mod=Message&act=get_message_list";
                RequestParams requestParams = new RequestParams();
                requestParams.addQueryStringParameter("count", count + "");
                requestParams.addQueryStringParameter("page", page + "");
                requestParams.addQueryStringParameter("oauth_token", context.oauth_token);
                requestParams.addQueryStringParameter("oauth_token_secret", context.oauth_token_secret);

                context.httpUtils.send(HttpRequest.HttpMethod.GET,
                        messageApi,
                        requestParams,
                        new RequestCallBack<String>() {

                            @Override
                            public void onSuccess(ResponseInfo<String> stringResponseInfo) {
                                try {
                                    if (stringResponseInfo.result.equals("null")) {
                                        //  没有更多
                                        Toast.makeText(context, "没有更多", Toast.LENGTH_SHORT).show();
                                        messageList.stopLoadMore();
                                    } else {
                                        JSONArray jsonArray = new JSONArray(stringResponseInfo.result);
                                        messageAdapter.addAll(getMessageDataArray(jsonArray));
                                        messageAdapter.notifyDataSetChanged();

                                        messageList.stopLoadMore();

                                        // 页码增加
                                        page += 1;
                                    }
                                } catch (JSONException e) {
                                    Toast.makeText(context, "网络错误", Toast.LENGTH_SHORT).show();
                                    Log.e("json", e.toString());
                                    messageList.stopLoadMore();
                                }
                            }

                            @Override
                            public void onFailure(HttpException e, String s) {
                                Toast.makeText(context, "网络错误", Toast.LENGTH_SHORT).show();
                                messageList.stopLoadMore();
                            }
                        });
            }
        });

        // 点击监听事件
        messageList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MessageData messageData = (MessageData) parent.getItemAtPosition(position);
                Intent intent = new Intent(context, MessageDetailActivity.class);
                intent.putExtra("list_id", messageData.list_id);

                context.startActivity(intent);
            }
        });
    }

    private class MessageAdapter extends ArrayAdapter<MessageData> {

        private int resource;
        private final LayoutInflater inflater;

        public MessageAdapter(Context context, int resource) {
            super(context, resource);
            this.resource = resource;
            inflater = LayoutInflater.from(context);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null){
                convertView = inflater.inflate(resource, parent, false);
            }
            final MessageData messageData = getItem(position);

            ImageView from_avatar = (ImageView)convertView.findViewById(R.id.message_list_item_from_avatar);
            TextView from_name = (TextView)convertView.findViewById(R.id.message_list_item_from_name);
            TextView last_message = (TextView)convertView.findViewById(R.id.message_list_item_last_message);
            TextView ctime = (TextView)convertView.findViewById(R.id.message_list_item_ctime);
            TextView num = (TextView)convertView.findViewById(R.id.message_list_item_num);
            FontAwesomeText delete = (FontAwesomeText)convertView.findViewById(R.id.message_list_item_delete);


            context.bitmapUtils.display(from_avatar, messageData.from_avatar);
            from_name.setText( ( (context.uid.equals(messageData.from_uid) ) ?
                    "我" :
                    messageData.from_name ) + "说: ");
            last_message.setText(messageData.last_message);
            ctime.setText(messageData.ctime);
            num.setText(messageData.num);
            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("是否删除?");
                    builder.setPositiveButton("是", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // 组装 API
                            String deleteMessageApi = "http://" + context.host + "index.php?app=api&mod=Message&act=destroy";
                            RequestParams requestParams = new RequestParams();
                            requestParams.addQueryStringParameter("list_id", messageData.list_id);
                            requestParams.addQueryStringParameter("oauth_token", context.oauth_token);
                            requestParams.addQueryStringParameter("oauth_token_secret", context.oauth_token_secret);

                            context.httpUtils.send(HttpRequest.HttpMethod.GET,
                                    deleteMessageApi,
                                    requestParams,
                                    new RequestCallBack<String>() {
                                        @Override
                                        public void onSuccess(ResponseInfo<String> responseInfo) {
                                            String state = responseInfo.result;

                                            if (state.equals("1")){
                                                Toast.makeText(context, "删除成功", Toast.LENGTH_SHORT).show();

                                                messageAdapter.remove(messageData);
                                                messageAdapter.notifyDataSetChanged();
                                            }else{
                                                Toast.makeText(context, "删除失败", Toast.LENGTH_SHORT).show();
                                            }
                                        }

                                        @Override
                                        public void onFailure(HttpException e, String s) {
                                            Toast.makeText(context, "网络出错", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    });
                    builder.setNegativeButton("不,我再想想", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                        }
                    });

                    builder.create().show();
                }
            });


            return convertView;
        }
    }

    private List<MessageData> getMessageDataArray(JSONArray jsonArray) throws JSONException {
        List<MessageData> items = new ArrayList<MessageData>();
        JSONObject jsonItem;

        for (int i = 0; i < jsonArray.length(); i++){
            jsonItem = jsonArray.getJSONObject(i);
            MessageData messageData = new MessageData();

            messageData.from_avatar = jsonItem.getString("from_face");
            messageData.from_name = jsonItem.getString("from_uname");
            messageData.last_message = jsonItem.getString("content");
            messageData.ctime = jsonItem.getString("ctime");
            messageData.num = jsonItem.getString("message_num");
            messageData.from_uid = jsonItem.getString("from_uid");
            messageData.list_id = jsonItem.getString("list_id");



            items.add(messageData);
        }

        return items;
    }

    private class MessageData {
        private String from_avatar;
        private String from_uid;
        private String from_name;
        private String last_message;
        private String ctime;
        private String num;
        private String list_id;
    }
}
