package com.duosl.coolweather.coolweather;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.duosl.coolweather.coolweather.db.City;
import com.duosl.coolweather.coolweather.db.County;
import com.duosl.coolweather.coolweather.db.Province;
import com.duosl.coolweather.coolweather.util.HttpUtil;
import com.duosl.coolweather.coolweather.util.JSONUtil;
import com.duosl.coolweather.coolweather.util.ObjectStringUtil;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by Duosl on 2018/9/13.
 */

public class ChooseAreaFragment extends Fragment {

    private static final String TAG = "ChooseAreaFragment";

    //级别
    public static final int LEVEL_PROVINCE=0;
    public static final int LEVEL_CITY=1;
    public static final int LEVEL_COUNTY=2;

    //控件
    private ProgressDialog progressDialog;
    private TextView titleText;
    private Button backButton;
    private ListView listView;

    private ArrayAdapter<String> adapter;
    private List<String> dataList=new ArrayList<>();

    private List<Province> provinceList;//省列表
    private List<City> cityList;    //市列表
    private List<County> countyList;    //县列表

    public Province selectedProvince;  //选中的省份
    public City selectedCity;  //选中的城市
    public int currentLevel;  //当前选中的级别

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.choose_area,container,false);
        titleText=view.findViewById(R.id.title_text);
        backButton=view.findViewById(R.id.back_button);
        listView=view.findViewById(R.id.list_view);
        adapter=new ArrayAdapter<String>(getContext(),android.R.layout.simple_list_item_1,dataList);
        listView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                SharedPreferences.Editor editor= PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
                if (currentLevel==LEVEL_PROVINCE){
                    selectedProvince=provinceList.get(position);
                    //将选中的省缓存
                    editor.putString("selectedProvince", ObjectStringUtil.objToStr(selectedProvince));
                    editor.apply();
                    queryCities();
                }else if(currentLevel==LEVEL_CITY){
                    selectedCity=cityList.get(position);
                    //将选中的市缓存
                    editor.putString("selectedCity", ObjectStringUtil.objToStr(selectedCity));
                    editor.apply();
                    queryCounties();
                }else if(currentLevel==LEVEL_COUNTY){
                    String weatherId=countyList.get(position).getWeatherId();
                    //在选中区县时将当前区县所在市的区县存在缓存中
                    if (getActivity() instanceof MainActivity){
                        Intent intent=new Intent(getActivity(),WeatherActivity.class);
                        intent.putExtra("weather_id",weatherId);
                        startActivity(intent);
                        getActivity().finish();
                    }else if(getActivity() instanceof WeatherActivity){
                        WeatherActivity weatherActivity= (WeatherActivity) getActivity();
                        weatherActivity.drawerLayout.closeDrawers();//关闭左侧滑动菜单
                        weatherActivity.swipeRefresh.setRefreshing(true);   //设置swipeRefresh控件可刷新
                        weatherActivity.requestWeather(weatherId);
                    }
                }
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentLevel==LEVEL_COUNTY){
                    queryCities();
                }else if (currentLevel==LEVEL_CITY){
                    queryProvinces();
                }
            }
        });

        //从缓存中获取当前区县所在市的区县信息
        SharedPreferences sp=PreferenceManager.getDefaultSharedPreferences(getContext());
        String currentProvinceStr = sp.getString("selectedProvince",null);
        String currentCityStr = sp.getString("selectedCity",null);
        try {
            selectedProvince= (Province) ObjectStringUtil.StringToObj(currentProvinceStr);
            selectedCity= (City) ObjectStringUtil.StringToObj(currentCityStr);
            queryCounties();
        } catch (Exception e) {
            e.printStackTrace();
            queryProvinces();
        }

    }

    /**
     * 查询全国的省，优先从数据库中查询，如果没有查询到再去服务器上查询
     */
    private void queryProvinces() {
        titleText.setText("请选择省");
        backButton.setVisibility(View.GONE);
        provinceList=DataSupport.findAll(Province.class);

        if(provinceList.size()>0){
            dataList.clear();
            for (Province province:provinceList){
                dataList.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();//更新listview的显示
            listView.setSelection(0);
            currentLevel=LEVEL_PROVINCE;
        }else{
            String url="http://guolin.tech/api/china";
            queryFormServer(url,"province");
        }
    }

    /**
     * 查询选中的省内的所有的市，优先从数据库中查询，如果没有查询到再去服务器上查询
     */
    private void queryCities() {
        titleText.setText(selectedProvince.getProvinceName());
        backButton.setVisibility(View.VISIBLE);

        cityList= DataSupport.where("provinceId=?",String.valueOf(selectedProvince.getId())).find(City.class);
        if (cityList.size()>0){
            //从数据库中查询
            dataList.clear();
            for (City city:cityList){
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel=LEVEL_CITY;
        }else{
            //从服务器端查询
            int provinceCode=selectedProvince.getProvinceCode();
            String url="http://guolin.tech/api/china/"+provinceCode;
            queryFormServer(url,"city");
        }
    }

    /**
     * 查询选中的市内的所有的区县 ，优先从数据库中查询，如果没有查询到再去服务器上查询
     */
    private void queryCounties() {
        titleText.setText(selectedCity.getCityName());
        backButton.setVisibility(View.VISIBLE);

        countyList= DataSupport.where("cityId=?",String.valueOf(selectedCity.getId())).find(County.class);
        if (countyList.size()>0){
            //从数据库中查询
            dataList.clear();
            for (County county:countyList){
                dataList.add(county.getCountryName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel=LEVEL_COUNTY;
        }else{
            //从服务器端查询
            int provinceCode=selectedProvince.getProvinceCode();
            int cityCode=selectedCity.getCityCode();
            String url="http://guolin.tech/api/china/"+provinceCode+"/"+cityCode;
            queryFormServer(url,"county");
        }
    }

    /**
     * 根据传入的地址和类型从服务器上查询省市县数据
     * @param url
     * @param type
     */
    private void queryFormServer(String url, final String type) {
        showProgressDialog();
        HttpUtil.sendOkHttpRequest(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                //通过runOnUiThread() 方法回到主线程处理逻辑
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(getContext(), "加载失败，请检查网络连接！", Toast.LENGTH_SHORT).show();;
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText=response.body().string();
                boolean result=false;
                if("province".equals(type)){
                    result= JSONUtil.handleProvinceResponse(responseText);
                }else if("city".equals(type)){
                    result=JSONUtil.handleCityResponse(responseText,selectedProvince.getId());
                }else if ("county".equals(type)){
                    result=JSONUtil.handleCountyResponse(responseText,selectedCity.getId());
                }
                if (result){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if("province".equals(type)){
                               queryProvinces();
                            }else if("city".equals(type)){
                                queryCities();
                            }else if ("county".equals(type)){
                               queryCounties();
                            }
                        }
                    });
                }
            }
        });
    }

    /**
     * 显示进度对话框
     */
    private void showProgressDialog() {
        if(progressDialog==null){
            progressDialog=new ProgressDialog(getActivity());
            progressDialog.setMessage("正在加载...");
            progressDialog.setCancelable(false);
        }
        progressDialog.show();
    }

    /**
     * 关闭进度对话框
     */
    private void closeProgressDialog() {
        if(progressDialog!=null){
            progressDialog.dismiss();
        }
    }
}
