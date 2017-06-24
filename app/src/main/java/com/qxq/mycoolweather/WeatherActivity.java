package com.qxq.mycoolweather;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.qxq.mycoolweather.gson.Weather;
import com.qxq.mycoolweather.util.Utility;

import butterknife.BindView;
import butterknife.ButterKnife;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
        ButterKnife.bind(this);
        SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString=prefs.getString("weather",null);
        if(weatherString!=null){
            //有缓存时直接解析天气数据
            Weather weather= Utility.handleWeatherResponse(weatherString);
          shouWeatherInfo(weather);
        }else {
            //无缓存时去服务器查询天气
            String weatherId=getIntent().getStringExtra("weather_id");
            KnifeWeatherLayout.setVisibility(View.INVISIBLE) ;
            requestWeather(weatherId);
        }
    }

    private void requestWeather(final String weatherId) {

    }

    private void shouWeatherInfo(final Weather weather) {
    }
}
