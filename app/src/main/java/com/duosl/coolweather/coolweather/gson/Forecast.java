package com.duosl.coolweather.coolweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Duosl on 2018/9/13.
 */

public class Forecast {

    public String date;

    @SerializedName("tmp")
    public Temperature temperature;
    
    @SerializedName("cond")
    public More more;


    public class Temperature{

        public String max;

        public String min;
    }

    public class More{

        @SerializedName("txt_d")
        public String info;
    }

    @Override
    public String toString() {
        return "Forecast{" +
                "date='" + date + '\'' +
                ", temperature=" + temperature +
                ", more=" + more +
                '}';
    }
}
