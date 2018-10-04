package com.duosl.coolweather.coolweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Duosl on 2018/9/13.
 */

public class Now {

    @SerializedName("tmp")
    public String temperature;

    @SerializedName("cond_txt")
    public String info;

}
