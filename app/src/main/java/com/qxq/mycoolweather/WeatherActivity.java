package com.qxq.mycoolweather;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.qxq.mycoolweather.gson.ForeCast;
import com.qxq.mycoolweather.gson.Weather;
import com.qxq.mycoolweather.util.HttpUtil;
import com.qxq.mycoolweather.util.Utility;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {

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
        if(Build.VERSION.SDK_INT>=21){
            View decorView=getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_weather);
        ButterKnife.bind(this);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String bingPic=prefs.getString("bing_pic",null);
        if(bingPic!=null){
            Glide.with(this).load(bingPic).into(KnifeBingPicImg);
        }
        else {
            LoadBingPic();
        }
        String weatherString = prefs.getString("weather", null);

        String weatherId = getIntent().getStringExtra("weather_id");

        if (weatherString != null) {
            //有缓存时直接解析天气数据
            Weather weather = Utility.handleWeatherResponse(weatherString);

            if(weather.basic.weatherId.equals(weatherId)){
                shouWeatherInfo(weather);
            }else {
                KnifeWeatherLayout.setVisibility(View.INVISIBLE);
                requestWeather(weatherId);
            }

        } else {
            KnifeWeatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(weatherId);
        }



    }

    /**
     * 加载必应图片
     */
    private void LoadBingPic() {
        String requestBingPic="http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkhttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
            final String bingpic=response.body().string();
                SharedPreferences.Editor editor=PreferenceManager.
                        getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("bing_pic",bingpic);
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
    private void requestWeather(final String weatherId) {
        String weatherurl = "http://guolin.tech/api/weather/?cityid=" +
                weatherId + "&key=6d950e1e88e347b59c609e727bfe859f ";
        HttpUtil.sendOkhttpRequest(weatherurl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
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

                        } else {
                            Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();

                        }

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
           // KnifeInfoText.setText(forecast.more.info);

            //KnifeDataText.setText(forecast.date);
            TextView maxText=(TextView)view.findViewById(R.id.max_text);
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

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_BACK){
            this.finish();
        }
        return super.onKeyDown(keyCode, event);
    }
}
