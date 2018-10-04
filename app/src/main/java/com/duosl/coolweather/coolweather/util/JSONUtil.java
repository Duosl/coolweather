package com.duosl.coolweather.coolweather.util;

import android.text.TextUtils;
import android.util.Log;

import com.duosl.coolweather.coolweather.db.City;
import com.duosl.coolweather.coolweather.db.County;
import com.duosl.coolweather.coolweather.db.Province;
import com.duosl.coolweather.coolweather.gson.Weather;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Duosl on 2018/9/13.
 */

public class JSONUtil {
    /**
     * 解析和处理服务器返回的省级数据
     * @param response
     * @return
     */
    public static boolean handleProvinceResponse(String response){
        if(!TextUtils.isEmpty(response)){
            try {
                JSONArray allProvinces=new JSONArray(response);
                for (int i=0;i<allProvinces.length();i++){
                    JSONObject provinceObject=allProvinces.getJSONObject(i);
                    Province province=new Province();
                    province.setProvinceName(provinceObject.getString("name"));
                    province.setProvinceCode(provinceObject.getInt("id"));
                    province.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 解析和处理服务器返回的市级数据
     * @param response
     * @param provinceId
     * @return
     */
    public static boolean handleCityResponse(String response,int provinceId){
        if(!TextUtils.isEmpty(response)){
            try {
                JSONArray allCitys=new JSONArray(response);
                for (int i=0;i<allCitys.length();i++){
                    JSONObject cityObject=allCitys.getJSONObject(i);
                    City city=new City();
                    city.setCityName(cityObject.getString("name"));
                    city.setCityCode(cityObject.getInt("id"));
                    city.setProvinceId(provinceId);
                    city.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 解析和处理服务器返回的县级数据
     * @param response
     * @param cityId
     * @return
     */
    public static boolean handleCountyResponse(String response,int cityId){
        if(!TextUtils.isEmpty(response)){
            try {
                JSONArray allCounties=new JSONArray(response);
                for (int i=0;i<allCounties.length();i++){
                    JSONObject cityObject=allCounties.getJSONObject(i);
                    County county=new County();
                    county.setCountryName(cityObject.getString("name"));
                    county.setCountryCode(cityObject.getInt("id"));
                    county.setWeatherId(cityObject.getString("weather_id"));
                    county.setCityId(cityId);
                    county.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static Weather handleWeatherResopnse(String response){
        try {
            //获取返回的天气的json对象
            JSONObject jsonObject=new JSONObject(response);
            //获取name=HeWeather的对象，并将其转换为JSONArray对象
            JSONArray jsonArray=null;
            try {
                jsonArray = jsonObject.getJSONArray("HeWeather");
            } catch (JSONException e) {
                jsonArray = jsonObject.getJSONArray("HeWeather6");
            }

            String weatherContent=jsonArray.getJSONObject(0).toString();
            //将返回的json字符串转换为Weather对象
            Weather weather=new Gson().fromJson(weatherContent,Weather.class);
            return weather;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
