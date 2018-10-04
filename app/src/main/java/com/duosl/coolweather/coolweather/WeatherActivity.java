package com.duosl.coolweather.coolweather;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapView;
import com.bumptech.glide.Glide;
import com.duosl.coolweather.coolweather.gson.Forecast;
import com.duosl.coolweather.coolweather.gson.Weather;
import com.duosl.coolweather.coolweather.util.AppSetting;
import com.duosl.coolweather.coolweather.util.HttpUtil;
import com.duosl.coolweather.coolweather.util.JSONUtil;

import org.w3c.dom.Text;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends BaseActivity {

    private long firstTime=0;   //两秒内按返回键两次退出程序
    //百度地图
    public static LocationClient locationClient;
    private String currentCity;

    public DrawerLayout drawerLayout;   //左侧滑动菜单--选择省市县
    private TextView titleSetting;  //设置菜单按钮
    private TextView change_city; //切换城市
    private TextView locationFun;   //定位按钮
    public SwipeRefreshLayout swipeRefresh;//下拉刷新控件
    private String mWeatherId;  //用于记录城市的天气id
    private ScrollView weatherLayout;   //天气显示控件
    private TextView titleCity; //显示要查询的城市
    private TextView weatherUpdateTime;   //显示本次天气的更新时间
    private TextView degreeText;    //当前气温
    private TextView weatherInfoText;   //天气基本信息
    private LinearLayout forecastLayout;    //七日天气
    private TextView aqiText;   //aqi 指数
    private TextView pm25Text;  //PM2.5 指数
    private TextView comfortText;   //舒适度建议
    private TextView carWashText;   //洗车建议
    private TextView sportText;     //运动建议

    private ImageView bingPicImg;   //背景图片

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //实现背景图和状态栏的融合
        if(Build.VERSION.SDK_INT>=21) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

        locationClient=new LocationClient(getApplicationContext());
        locationClient.registerLocationListener(new WeatherActivity.MyLocationListener());


        setContentView(R.layout.activity_weather);

        //初始化各控件
        weatherLayout=findViewById(R.id.weather_layout);
        titleCity=findViewById(R.id.title_city);
        titleSetting=findViewById(R.id.title_setting);

        titleSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(getApplicationContext(),SettingActivity.class);
                startActivity(intent);
            }
        });
        weatherUpdateTime=findViewById(R.id.weather_update_time);
        degreeText=findViewById(R.id.degree_text);
        weatherInfoText=findViewById(R.id.weather_info_text);
        forecastLayout=findViewById(R.id.forecast_layout);
        aqiText=findViewById(R.id.aqi_text);
        pm25Text=findViewById(R.id.pm25_text);
        comfortText=findViewById(R.id.comfort_text);
        carWashText=findViewById(R.id.car_wash_text);
        sportText=findViewById(R.id.sport_text);
        bingPicImg=findViewById(R.id.bing_pic_img);

        swipeRefresh=findViewById(R.id.swipe_refresh);
        swipeRefresh.setColorSchemeResources(R.color.colorAccent);
        swipeRefresh.setRefreshing(true);

        drawerLayout=findViewById(R.id.drawer_layout);

        change_city=findViewById(R.id.change_city);
        change_city.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
       locationFun=findViewById(R.id.location_fun);
       locationFun.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               NetworkInfo networkInfo=((ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
               if(networkInfo!=null && networkInfo.isAvailable()) {//网络可用时
                   requestLocation();
               }else{
                   Toast.makeText(WeatherActivity.this, "请检查网络连接！", Toast.LENGTH_SHORT).show();
               }
           }
       });
        titleCity=findViewById(R.id.title_city);
        titleCity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(mWeatherId);
            }
        });

        loadPage();
    }

    public void getCityIdByName(final String cityName) {
        String url="https://free-api.heweather.com/s6/weather/now?location="+cityName+"&key="+AppSetting.MY_HFKEY;
        HttpUtil.sendOkHttpRequest(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this, "获取天气信息失败，请检查网络连接！", Toast.LENGTH_SHORT).show();
                        drawerLayout.openDrawer(GravityCompat.START);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText=response.body().string();
                final Weather weather=JSONUtil.handleWeatherResopnse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (weather!=null && "ok".equals(weather.status)){
                            requestWeather(weather.basic.weatherId);
                            swipeRefresh.setRefreshing(true);
                        }else{
                            Toast.makeText(WeatherActivity.this, "获取天气信息失败，请检查网络连接！", Toast.LENGTH_SHORT).show();
                            drawerLayout.openDrawer(GravityCompat.START);
                        }
                    }
                });
            }
        });
    }

    /**
     * 根据城市id去请求城市的天气信息
     * @param weatherId
     */
    public void requestWeather(final String weatherId) {
        String url="http://guolin.tech/api/weather?cityid="+weatherId+"&key="+ AppSetting.MY_HFKEY;

        HttpUtil.sendOkHttpRequest(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this, "获取天气信息失败，请检查网络连接！", Toast.LENGTH_SHORT).show();
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText=response.body().string();
                final Weather weather=JSONUtil.handleWeatherResopnse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (weather!=null && "ok".equals(weather.status)){
                            SharedPreferences.Editor editor=PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weather",responseText);
                            editor.apply();
                            showWeatherInfo(weather);
                            mWeatherId=weather.basic.weatherId;
                        }else{
                            Toast.makeText(WeatherActivity.this, "获取天气信息失败，请检查网络连接！", Toast.LENGTH_SHORT).show();
                        }
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }
        });

        loadBingPic();
    }


    private void loadBingPic() {
        String requestBingPic="http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingPic=response.body().string();
                SharedPreferences.Editor editor=PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("bing_pic",bingPic);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(bingPic).into(bingPicImg);
                    }
                });
            }
        });
    }

    /**
     * 处理并解析Weather实体类中的数据
     * @param weather
     */
    private void showWeatherInfo(Weather weather) {
        //将天气信息显示在相关控件上
        String cityName=weather.basic.cityName;
        String updateTime=weather.update.updateTime.split(" ")[1];
        String degree=weather.now.temperature+"℃";
        String weatherInfo=weather.now.info;
        titleCity.setText(cityName);
        weatherUpdateTime.setText("更新时间："+updateTime);
        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);
        //初始化（清空）显示容器
        forecastLayout.removeAllViews();

        //遍历7日天气信息
        if(!(weather.forecastList==null || weather.forecastList.size()==0)){
            for (Forecast forecast: weather.forecastList)
            {
                View view= LayoutInflater.from(this).inflate(R.layout.forecast_item,forecastLayout,false);
                TextView dateText=view.findViewById(R.id.date_text);
                TextView infoText=view.findViewById(R.id.info_text);
                TextView maxText=view.findViewById(R.id.max_text);
                TextView minText=view.findViewById(R.id.min_text);
                dateText.setText(forecast.date);
                infoText.setText(forecast.more.info);
                maxText.setText(forecast.temperature.max+"℃");
                minText.setText(forecast.temperature.min+"℃");
                forecastLayout.addView(view);
            }
        }

        if(weather.aqi!=null){
            aqiText.setText(weather.aqi.city.aqi);
            pm25Text.setText(weather.aqi.city.pm25);
        }
        if(weather.suggestion!=null){
            String comfort="舒适度："+weather.suggestion.comfort.info;
            String carWash="洗车指数："+weather.suggestion.carWash.info;
            String sport="运动建议："+weather.suggestion.sport.info;
            comfortText.setText(comfort);
            carWashText.setText(carWash);
            sportText.setText(sport);
        }
        weatherLayout.setVisibility(View.VISIBLE);

    }

    @Override
    public void onBackPressed() {
        long secondTime = System.currentTimeMillis();//以毫米为单位
        if(secondTime-firstTime>2000){
            Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
            firstTime=secondTime;
        }else {
            finish();
            System.exit(0);
        }
    }
    class MyLocationListener extends BDAbstractLocationListener {
        @Override
        public void onReceiveLocation(final BDLocation location) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(currentCity!=location.getDistrict()){
                        currentCity=location.getDistrict();
                        loadPage();
                    }
                }
            });
        }

        @Override
        public void onConnectHotSpotMessage(String s, int i) {

        }
    }
    private void requestLocation() {
        initLocation();
        locationClient.start();
        loadPage();
        drawerLayout.closeDrawer(GravityCompat.START);
    }

    private void loadPage() {
        SharedPreferences sp= PreferenceManager.getDefaultSharedPreferences(this);
        if(currentCity!=null){
            getCityIdByName(currentCity);
        }else{
            String weatherStr=sp.getString("weather",null);
            if(weatherStr!=null){
                //有缓存时直接解析天气资源
                Weather weather= JSONUtil.handleWeatherResopnse(weatherStr);
                mWeatherId=weather.basic.weatherId;
                NetworkInfo networkInfo=((ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
                if(networkInfo!=null && networkInfo.isAvailable()) {//网络可用时
                    //从服务器获取最新的天气信息
                    requestWeather(mWeatherId);
                }else {
                    //显示缓存的天气信息
                    showWeatherInfo(weather);
                    swipeRefresh.setRefreshing(false);
                }
            }else{//无缓存时去服务器查询天气信息
                mWeatherId=getIntent().getStringExtra("weather_id");
                weatherLayout.setVisibility(View.INVISIBLE);
                requestWeather(mWeatherId);
            }
        }

        String bingPic=sp.getString("bing_pic",null);
        if(bingPic!=null){
            Glide.with(this).load(bingPic).into(bingPicImg);
        }else{
            loadBingPic();
        }
    }

    private void initLocation() {
        LocationClientOption option=new LocationClientOption();
        option.setIsNeedAddress(true);
        //option.setScanSpan(5000);//每隔5s更新当前位置
        option.setCoorType("bd09ll");
        //使用高精度定位，GPS打开则使用GPS定位，否则使用网络定位
        //option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        locationClient.setLocOption(option);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        locationClient.stop();
    }
}
