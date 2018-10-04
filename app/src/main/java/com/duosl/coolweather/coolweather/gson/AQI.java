package com.duosl.coolweather.coolweather.gson;

/**
 * Created by Duosl on 2018/9/13.
 */

public class AQI {

    public AQICity city;

    public class AQICity{
        public String aqi;
        public String pm25;
    }
}
