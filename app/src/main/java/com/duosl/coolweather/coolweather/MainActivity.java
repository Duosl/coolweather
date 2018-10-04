package com.duosl.coolweather.coolweather;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.ImageView;
import android.widget.Toast;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.bumptech.glide.Glide;
import com.duosl.coolweather.coolweather.gson.Weather;
import com.duosl.coolweather.coolweather.util.JSONUtil;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        Boolean isLoc=getIntent().getBooleanExtra("location",false);
        SharedPreferences sp= PreferenceManager.getDefaultSharedPreferences(this);
        String weatherStr=sp.getString("weather",null);
        if(weatherStr!=null && isLoc==false){
            Intent intent=new Intent(this,WeatherActivity.class);
            startActivity(intent);
            finish();
        }else{
            //获取当前的网络状态
            NetworkInfo networkInfo=((ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
            if(networkInfo!=null && networkInfo.isAvailable()) {//网络可用时
                List<String> permissionList=new ArrayList<>();
                if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
                    permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
                }
                if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_PHONE_STATE)!= PackageManager.PERMISSION_GRANTED){
                    permissionList.add(Manifest.permission.READ_PHONE_STATE);
                }
                if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                    permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                }
                if (!permissionList.isEmpty()){
                    String [] permissions=permissionList.toArray(new String[permissionList.size()]);
                    ActivityCompat.requestPermissions(MainActivity.this,permissions,1);
                }else{
                    Intent intent=new Intent(this,WeatherActivity.class);
                    startActivity(intent);
                    finish();
                }
            }else {

            }


        }

    }

}
