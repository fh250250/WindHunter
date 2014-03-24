package com.WindHunter.tools;


import android.os.AsyncTask;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

public abstract class NetWorker {


    // 传入api地址，异步执行后台操作
    public void execute(String api){
        MyTask task = new MyTask();
        task.execute(api);
    }

    // 下载后的界面处理
    protected abstract void onPostExecute(String result);
    // 下载前的界面处理
    protected abstract void onPreExecute();
    // 解析下载的数据
    protected abstract void parser(String jsonStr);
    // 网路出错时的UI处理
    protected abstract void alert();

    // 实现内部的异步下载类
    protected class MyTask extends AsyncTask<String, Integer, String>{

        @Override // 后台操作
        protected String doInBackground(String... api) {
            try{
                HttpClient httpClient = new DefaultHttpClient();
                HttpGet httpGet = new HttpGet(api[0]);
                HttpResponse httpResponse = httpClient.execute(httpGet);
                // 返回请求数据
                return EntityUtils.toString(httpResponse.getEntity());
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            // 如果网络出错，调用错误处理UI
            // 否则，解析数据
            if (result != null){
                NetWorker.this.parser(result);
            }else{
                NetWorker.this.alert();
            }
            NetWorker.this.onPostExecute(result);
        }

        @Override
        protected void onPreExecute() {
            NetWorker.this.onPreExecute();
        }
    }
}
