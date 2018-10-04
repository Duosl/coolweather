package com.duosl.coolweather.coolweather.db;

import org.litepal.crud.DataSupport;

import java.io.Serializable;

/**
 * 省 --实体类
 * Created by Duosl on 2018/9/13.
 */

public class Province extends DataSupport implements Serializable{
    private int id; //主键
    private String provinceName;//省名
    private int provinceCode;//省代号

    public int getId() {
        return id;
    }

    public String getProvinceName() {
        return provinceName;
    }

    public int getProvinceCode() {
        return provinceCode;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setProvinceName(String provinceName) {
        this.provinceName = provinceName;
    }

    public void setProvinceCode(int provinceCode) {
        this.provinceCode = provinceCode;
    }
}
