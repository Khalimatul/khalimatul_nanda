package com.example.user.weatherku.weather;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.user.weatherku.PerJam;
import com.example.user.weatherku.R;
import com.example.user.weatherku.Utils;
import com.example.user.weatherku.model.CityWeather;
import com.example.user.weatherku.model.CurrentByCity;
import com.example.user.weatherku.model.Forecast;
import com.example.user.weatherku.model.ForecastList;
import com.example.user.weatherku.model.Weather;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;


/**
 * Created by user on 12/12/17.
 */
public class WeatherFragmentJam extends Fragment {
    private static final String TAG = WeatherFragmentJam.class.toString();

    private ScrollView mScrollView;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private TextView mCityText;

    private ListView mListView;
    private Forecast mForecast;
    private List<ForecastList> mForecastList;

    private WeatherManager3 nWeatherManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        nWeatherManager = new WeatherManager3((PerJam)getActivity());

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_weather_jam, container, false);
//        Button button = (Button) view.findViewById(R.id.button1);
//        button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent inte = new Intent(getActivity(), MainActivity.class);
//                startActivity(inte);
//                Fragment fragment = new WeatherFragmentNow();
//                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
//                fragmentManager.beginTransaction().replace(R.id.container_weather, fragment).commit();
//                Fragment fragment = new WeatherFragmentNow();
//                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
//                transaction.replace(R.id.container_weather, fragment);
//                transaction.addToBackStack(null);
//                transaction.commit();
//            }
//        });
        return view;
    }
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mScrollView = (ScrollView) view.findViewById(R.id.scrollview_weather);
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_weather);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.orange50);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                nWeatherManager.fetchLocalWeatherData();
            }
        });


        mCityText = (TextView) view.findViewById(R.id.city_text_weather);

        mListView = (ListView) view.findViewById(R.id.list_weather);
        mListView.setDivider(null);

    }

    private void setWeatherIcon(ImageView icon, String description) {
        if (description.contains("clear")) {
            icon.setImageResource(R.drawable.clear);
        } else if (description.contains("rain")) {
            icon.setImageResource(R.drawable.rain);
        } else if (description.contains("cloud")) {
            icon.setImageResource(R.drawable.cloud);
        } else if (description.contains("snow")) {
            icon.setImageResource(R.drawable.snow);
        } else if (description.contains("mist") || description.contains("haze")) {
            icon.setImageResource(R.drawable.mist);
        }
    }

    private void updateWeather(CityWeather cityWeather) {
        Log.d(TAG, "updateWeather");
        Log.d(TAG, "updateWeather: " + cityWeather.main.temp);
        Log.d(TAG, "description: " + cityWeather.weathers[0].description);

        String stateString = cityWeather.name + ", " + mForecast.city.country;
        mCityText.setText(stateString);

        scrollViewToTop();
    }

    private void updateForecastList() {
        ForecastWeatherAdapter mAdapter = new ForecastWeatherAdapter();
        mListView.setAdapter(mAdapter);
        scrollViewToTop();
    }

    private void updateWeatherByCity(CurrentByCity currentByCity) {
        Log.d(TAG, "updateWeatherByCity");
        Log.d(TAG, "updateWeatherByCity: " + currentByCity.main.temp);
        Log.d(TAG, "description: " + currentByCity.weathers[0].description);

        String stateString = currentByCity.name + ", " + currentByCity.system.country;
        mCityText.setText(stateString);

        scrollViewToTop();
    }

    public void setCityWeather(CityWeather cityWeather) {
        Log.d(TAG, "setCityWeather");
        updateWeather(cityWeather);
    }

    public void setForecast(Forecast forecast) {
        Log.d(TAG, "setForecast");
        mForecast = forecast;
        mForecastList = new ArrayList<>();
        mForecastList.addAll(forecast.list);
        mForecastList.remove(0);
        updateForecastList();
    }

    public void setCurrentByCity(CurrentByCity currentByCity) {
        Log.d(TAG, "setCurrentByCity");
        updateWeatherByCity(currentByCity);
    }

    public void setRefreshing(boolean refreshing) {
        mSwipeRefreshLayout.setRefreshing(refreshing);
    }

    private void scrollViewToTop() {
        mScrollView.postDelayed(new Runnable() {
            @Override
            public void run() {
                mScrollView.fullScroll(ScrollView.FOCUS_UP);
            }
        }, 20);
    }

    private String getWeekdayFromString(String date) {
        long time = Long.parseLong(date) * 1000;
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        return calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.ENGLISH);
    }

    private class ForecastWeatherAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mForecastList.size();
        }

        @Override
        public Object getItem(int position) {
            return mForecastList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ForecastList forecastList = mForecastList.get(position);
            if (convertView == null) {
                convertView = View.inflate(parent.getContext(), R.layout.weather_list_item_jam, null);
                ViewHolder tag = new ViewHolder();
                  tag.weekday = (TextView) convertView.findViewById(R.id.weekday_list_item);
//                tag.icon = (ImageView) convertView.findViewById(R.id.icon_list_item);
//                tag.description = (TextView) convertView.findViewById(R.id.description_list_item);
//                tag.maxTemp = (TextView) convertView.findViewById(R.id.max_temp_list_item);
                tag.dayTemp = (TextView) convertView.findViewById(R.id.day_temp);
                tag.eveTemp = (TextView) convertView.findViewById(R.id.eve_temp);
                tag.nightTemp = (TextView) convertView.findViewById(R.id.night_temp);
                tag.mornTemp = (TextView) convertView.findViewById(R.id.morn_temp);
                tag.icon = (ImageView) convertView.findViewById(R.id.day_icon);
                tag.icon2 = (ImageView) convertView.findViewById(R.id.morning_icon);
                tag.icon3 = (ImageView) convertView.findViewById(R.id.evening_icon);
                tag.icon4 = (ImageView) convertView.findViewById(R.id.night_icon);
                convertView.setTag(tag);
            }

            ViewHolder tag = (ViewHolder) convertView.getTag();
            Weather[] weathers = forecastList.weathers;

            if (forecastList != null) {
//                String stateString = cityWeather.name + ", " + mForecast.city.country;
//                mCityText.setText(stateString);
                tag.weekday.setText(getWeekdayFromString(forecastList.timestamp));
//                setWeatherIcon(tag.icon, weathers[0].description);
//                tag.description.setText(weathers[0].description);
//                tag.maxTemp.setText(Utils.temperatureFormat(forecastList.temp.max));
                String stateString = "Day : "+Utils.temperatureFormat(forecastList.temp.day);
                tag.dayTemp.setText(stateString);
                String stateString2 = "Evening : "+Utils.temperatureFormat(forecastList.temp.eve);
                tag.eveTemp.setText(stateString2);
                String stateString3 = "Night : "+Utils.temperatureFormat(forecastList.temp.night);
                tag.nightTemp.setText(stateString3);
                String stateString4 = "Morning : "+Utils.temperatureFormat(forecastList.temp.morn);
                tag.mornTemp.setText(stateString4);
            }
            return convertView;
        }
    }

    private static class ViewHolder {
        ImageView icon;
        ImageView icon2;
        ImageView icon3;
        ImageView icon4;
        TextView weekday;
        TextView dayTemp;
        TextView eveTemp;
        TextView nightTemp;
        TextView mornTemp;
    }
}
