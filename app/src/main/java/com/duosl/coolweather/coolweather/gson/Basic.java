package com.duosl.coolweather.coolweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Duosl on 2018/9/13.
 */

public class Basic {

    @SerializedName("city")
    public String cityName;

    @SerializedName("cid")
    public String weatherId;

    public String parent_city;

}
