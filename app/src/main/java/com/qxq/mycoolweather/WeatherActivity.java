package com.qxq.mycoolweather;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.qxq.mycoolweather.gson.ForeCast;
import com.qxq.mycoolweather.gson.Weather;
import com.qxq.mycoolweather.service.AutoUpdateService;
import com.qxq.mycoolweather.util.HttpUtil;
import com.qxq.mycoolweather.util.Utility;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {
    public SwipeRefreshLayout swipeRefreshLayout;

    private String mWeatherId;
    private Button navButton;
    public DrawerLayout drawerLayout;

    @BindView(R.id.title_city)
    TextView KnifeTitleCity;
    @BindView(R.id.title_update_time)
    TextView KnifeTitleUpdateTime;
    @BindView(R.id.degree_text)
    TextView KnifeDegreeText;
    @BindView(R.id.weather_info_text)
    TextView KnifeWeatherInfoText;
    @BindView(R.id.forecast_layout)
    LinearLayout KnifeForecastLayout;
    @BindView(R.id.aqi_text)
    TextView KnifeAqiText;
    @BindView(R.id.pm25_text)
    TextView Knife25Text;
    @BindView(R.id.comfort_text)
    TextView KnifeComfortText;
    @BindView(R.id.car_wash_text)
    TextView KnifeCarWashText;
    @BindView(R.id.sport_text)
    TextView KnifeSportText;
    @BindView(R.id.weather_layout)
    ScrollView KnifeWeatherLayout;
    @BindView(R.id.bing_pic_img)
    ImageView KnifeBingPicImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //进行状态栏的空间加融合进布局，并设置状态栏的背景透明
        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_weather);
        ButterKnife.bind(this);
        swipeRefreshLayout= (SwipeRefreshLayout) findViewById(R.id.swip_refresh);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);

        drawerLayout= (DrawerLayout) findViewById(R.id.drawer_layout);
        navButton= (Button) findViewById(R.id.nav_button);
        navButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

       final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String bingPic = prefs.getString("bing_pic", null);
        if (bingPic != null) {
            Glide.with(this).load(bingPic).into(KnifeBingPicImg);
        } else {
            LoadBingPic();
        }
      String weatherString = prefs.getString("weather", null);

        String weatherId = getIntent().getStringExtra("weather_id");

        if (weatherString != null) {
            //有缓存时直接解析天气数据
            Weather weather = Utility.handleWeatherResponse(weatherString);

            mWeatherId = weather.basic.weatherId;

            if (mWeatherId.equals(weatherId)) {
                shouWeatherInfo(weather);

            } else {
                KnifeWeatherLayout.setVisibility(View.INVISIBLE);
                requestWeather(weatherId);

            }

        } else {
            //无缓存时去服务器查询天气
            KnifeWeatherLayout.setVisibility(View.INVISIBLE);
//            mWeatherId=weatherId;
            requestWeather(weatherId);
        }

       /*  此种做法，使得保存的永远是软件安装后第一次展示的城市的weatherString,
       导致一直显示那一个城市的天气信息
       if(weatherString!=null){
            Weather weather = Utility.handleWeatherResponse(weatherString);
            shouWeatherInfo(weather);
         }
        else {
            //无缓存时去服务器查询天气
            KnifeWeatherLayout.setVisibility(View.INVISIBLE);
            String weatherId = getIntent().getStringExtra("weather_id");
            requestWeather(weatherId);
        }*/

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                String weatherStirngs=prefs.getString("weather",null);
                Weather weather = Utility.handleWeatherResponse(weatherStirngs);
                requestWeather(weather.basic.weatherId);
            }
        });
    }


    /**
     * 加载必应图片
     */
    private void LoadBingPic() {
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkhttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingpic = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager.
                        getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("bing_pic", bingpic);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(bingpic).into(KnifeBingPicImg);

                    }
                });
            }
        });
    }

    /**
     * 根据天气id请求城市天气
     *
     * @param weatherId
     */
    public void requestWeather(final String weatherId) {
        String weatherurl = "http://guolin.tech/api/weather/?cityid=" +
                weatherId + "&key=6d950e1e88e347b59c609e727bfe859f ";
        HttpUtil.sendOkhttpRequest(weatherurl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this, "获取天气信息失败",
                                Toast.LENGTH_SHORT).show();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                final Weather weather = Utility.handleWeatherResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (weather != null && "ok".equals(weather.status)) {
                            SharedPreferences.Editor editor =
                                    PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weather", responseText);
                            editor.apply();
                            shouWeatherInfo(weather);
                            Toast.makeText(WeatherActivity.this, "获取天气信息成功",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(WeatherActivity.this, "获取天气信息失败",
                                    Toast.LENGTH_SHORT).show();
                        }
                        swipeRefreshLayout.setRefreshing(false);

                    }
                });
            }
        });
    }

    /**
     * 处理并展示Weather实体类中的数据
     */
    private void shouWeatherInfo(final Weather weather) {
        String cityName = weather.basic.cityName;
        String updateTime = weather.basic.update.updateTime.split(" ")[1];
        String degree = weather.now.temperature + "℃";
        String weatherInfo = weather.now.more.info;
        KnifeTitleCity.setText(cityName);
        KnifeTitleUpdateTime.setText(updateTime);
        KnifeDegreeText.setText(degree);
        KnifeWeatherInfoText.setText(weatherInfo);
        KnifeForecastLayout.removeAllViews();
        for (ForeCast forecast : weather.foreCastList) {
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item, KnifeForecastLayout, false);
            TextView dataText = (TextView) view.findViewById(R.id.data_text);
            dataText.setText(forecast.date);
            TextView infoText = (TextView) view.findViewById(R.id.info_text);
            infoText.setText(forecast.more.info);

            TextView maxText = (TextView) view.findViewById(R.id.max_text);
            maxText.setText(forecast.temperature.max);
            // KnifeMaxText.setText(forecast.temperature.max);
            TextView minText = (TextView) view.findViewById(R.id.min_text);
            minText.setText(forecast.temperature.min);
            //  KnifeMinText.setText(forecast.temperature.min);

            KnifeForecastLayout.addView(view);
        }
        if (weather.aqi != null) {
            KnifeAqiText.setText(weather.aqi.city.aqi);
            Knife25Text.setText(weather.aqi.city.pm25);
        }
        String comFort = "舒适度：" + weather.suggestion.comfort.info;
        String carWash = "洗车洗漱：" + weather.suggestion.carWash.info;
        String sport = "运动健康" + weather.suggestion.sport.info;
        KnifeComfortText.setText(comFort);
        KnifeCarWashText.setText(carWash);
        KnifeSportText.setText(sport);
        KnifeWeatherLayout.setVisibility(View.VISIBLE);
        Intent intent=new Intent(this, AutoUpdateService.class);
        startService(intent);

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {

           backListener();
           // exit();
            return false;

        }
        return super.onKeyDown(keyCode, event);
    }
    private void backListener(){

            if(System.currentTimeMillis()-exitTime>2000){
                Toast.makeText(getApplicationContext(),"再次点击退出",Toast.LENGTH_SHORT).show();
                exitTime=System.currentTimeMillis();

            }else {
                finish();
                System.exit(0);
            }


    }
    private long exitTime=0;
    // 定义一个变量，来标识是否退出
    private static boolean isExit = false;

    Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            isExit = false;
        }
    };
    private void exit() {
        if (!isExit) {
            isExit = true;
            Toast.makeText(getApplicationContext(), "再按一次退出程序",
                    Toast.LENGTH_SHORT).show();
            // 利用handler延迟发送更改状态信息
            mHandler.sendEmptyMessageDelayed(0, 2000);
        } else {
            finish();
            System.exit(0);
        }
    }





}
