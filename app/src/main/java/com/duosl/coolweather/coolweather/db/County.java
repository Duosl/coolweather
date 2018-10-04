package com.duosl.coolweather.coolweather.db;

import org.litepal.crud.DataSupport;

import java.io.Serializable;

/**
 * 县、街道 -- 实体类
 * Created by Duosl on 2018/9/13.
 */

public class County extends DataSupport implements Serializable {
    private int id;//县键
    private String countryName;//县名
    private int countryCode;//县代号
    private int cityId;//所属市id
    private String weatherId;//天气id

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    public int getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(int countryCode) {
        this.countryCode = countryCode;
    }

    public int getCityId() {
        return cityId;
    }

    public void setCityId(int cityId) {
        this.cityId = cityId;
    }

    public String getWeatherId() {
        return weatherId;
    }

    public void setWeatherId(String weatherId) {
        this.weatherId = weatherId;
    }
}
