package com.duosl.coolweather.coolweather.db;

import org.litepal.crud.DataSupport;

import java.io.Serializable;

/**
 * 市 实体类
 * Created by Duosl on 2018/9/13.
 */

public class City extends DataSupport  implements Serializable {

    private int id;//主键
    private String cityName;//市名
    private int cityCode;//市代号
    private int provinceId;//所属省id

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public int getCityCode() {
        return cityCode;
    }

    public void setCityCode(int cityCode) {
        this.cityCode = cityCode;
    }

    public int getProvinceId() {
        return provinceId;
    }

    public void setProvinceId(int provinceId) {
        this.provinceId = provinceId;
    }
}
