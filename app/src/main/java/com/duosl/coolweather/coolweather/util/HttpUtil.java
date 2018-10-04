package com.duosl.coolweather.coolweather.util;

import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Created by Duosl on 2018/9/13.
 */

public class HttpUtil {

    /**
     * 发送一条http请求
     * @param url   请求地址
     * @param callback  回调函数--用于处理服务器响应
     */
    public static void sendOkHttpRequest(String url, okhttp3.Callback callback){
        OkHttpClient client=new OkHttpClient();
        Request request=new Request.Builder().url(url).build();
        client.newCall(request).enqueue(callback);
    }
}
