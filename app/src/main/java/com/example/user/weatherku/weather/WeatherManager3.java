package com.example.user.weatherku.weather;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

import com.example.user.weatherku.Constant.Constant;
import com.example.user.weatherku.LocationService;
import com.example.user.weatherku.PerJam;
import com.example.user.weatherku.model.CityWeather;
import com.example.user.weatherku.model.Current;
import com.example.user.weatherku.model.CurrentByCity;
import com.example.user.weatherku.model.Forecast;
import com.example.user.weatherku.model.WeatherBase;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;

/**
 * Created by user on 12/12/17.
 */
public class WeatherManager3 {
    private static final String TAG = WeatherManager3.class.toString();
    private static final int maxResults = 1;
    private static final String UPDATED_SUCCESS = "Berhasil Memperbarui !!";
    private static final String UPDATED_FAILED = "Gagal Memperbarui, Aktifkan GPS dan koneksi jaringan anda !!";
    private PerJam nMainView;
    private LocationService mLocationService;
    private WeatherService mWeatherService;

    private CityWeather mCityWeather;
    private Location mLocation;
    private String mCityName;
    private CityWeather[] mWeatherList;

    private List<WeatherBase> mSearchWeatherData;

    public WeatherManager3(PerJam mainActivity) {
        nMainView = mainActivity;
        mLocationService = new LocationService(nMainView);
        mWeatherService = new WeatherService();
    }

    public void fetchSearchWeatherData(Observable<String> city) {
        city.flatMap(new Func1<String, Observable<List<WeatherBase>>>() {
            @Override
            public Observable<List<WeatherBase>> call(String s) {
                return Observable.zip(mWeatherService.getCurrentWeatherByCity(s),
                        mWeatherService.getForecastWeather(s),
                        new Func2<CurrentByCity, Forecast, List<WeatherBase>>() {
                            @Override
                            public List<WeatherBase> call(CurrentByCity currentByCity, Forecast forecast) {
                                mSearchWeatherData = new ArrayList<WeatherBase>();
                                mSearchWeatherData.add(currentByCity);
                                mSearchWeatherData.add(forecast);
                                return mSearchWeatherData;
                            }
                        });
            }
        })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<WeatherBase>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(List<WeatherBase> weatherBases) {
                        displayCurrentByCity((CurrentByCity) weatherBases.get(0));
                        displayForecast((Forecast) weatherBases.get(1));
                        nMainView.getWeatherFragmenttt().setRefreshing(false);
                        nMainView.showUpdateInfo(UPDATED_SUCCESS);
                    }
                });
    }

    public void fetchLocalWeatherData() {
        mLocationService.getLocation()
                .flatMap(new Func1<Location, Observable<Current>>() {
                    @Override
                    public Observable<Current> call(Location location) {
                        mLocation = location;
                        return mWeatherService.getCurrentWeather(String.valueOf(location.getLatitude()),
                                String.valueOf(location.getLongitude()));
                    }
                })
                .map(new Func1<Current, String>() {
                    @Override
                    public String call(Current current) {
                        String cityName = getCityFromLocation(mLocation);
                        if (cityName == null) {
                            mWeatherList = current.list;
                        } else {
                            for (CityWeather cityWeather: current.list) {
                                if (cityName.contains(cityWeather.name)) {
                                    Log.d(TAG, "find match: " + cityWeather.name);
                                    mCityWeather = cityWeather;
                                    mCityName = cityWeather.name;
                                    return mCityName;
                                }
                            }
                            //not find match, display one in the list
                            mCityWeather = current.list[1];
                        }
                        return cityName;
                    }
                })
                .flatMap(new Func1<String, Observable<Forecast>>() {
                    @Override
                    public Observable<Forecast> call(String s) {
                        Log.d(TAG, "s: " + s);
                        mCityName = s;
                        return mWeatherService.getForecastWeather(s);
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Forecast>() {
                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "onError: " + e.getMessage());
                        if (mCityName != null) {
                            nMainView.getWeatherFragmenttt().setRefreshing(false);
                            nMainView.showUpdateInfo(UPDATED_FAILED);
                        } else {
                            if (mWeatherList != null)
                                displayCityFragment(mWeatherList);
                        }
                    }

                    @Override
                    public void onNext(Forecast forecast) {
                        Log.d(TAG, "onNext");
                        displayForecast(forecast);
                        displayCurrent(mCityWeather);
                        nMainView.getWeatherFragmenttt().setRefreshing(false);
                        nMainView.showUpdateInfo(UPDATED_SUCCESS);
                        displayWeatherFragment();
                    }
                });

    }

    private String getCityFromLocation(Location location) {
        Log.d(TAG, "getCityFromLocation");
        Geocoder geocoder = new Geocoder(nMainView, Locale.ENGLISH);
        List<Address> addresses = null;
        /*
        try {
            addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), maxResults);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (addresses != null) {
            if (addresses.get(0) != null) {
                return addresses.get(0).getAdminArea();
            }
        }
        */
        return getStoredCityName();
    }

    public void checkLocationState() {
        SharedPreferences sharedPreferences = nMainView.getSharedPreferences(Constant.LAST_LOCATION, Context.MODE_PRIVATE);
        LocationManager locationManager = (LocationManager) nMainView.getSystemService(Context.LOCATION_SERVICE);
        if (sharedPreferences.getString(Constant.LAST_LOCATION_LAT, null) != null
                || locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            fetchLocalWeatherData();
        }
    }

    private void displayCityFragment(CityWeather[] cityWeathers) {
        nMainView.showCityList(cityWeathers);
        nMainView.hideLoadingBar();
        nMainView.hideWeatherFragment();
    }

    private void displayWeatherFragment() {
        nMainView.showWeatherFragment();
        nMainView.hideLoadingBar();
        nMainView.hideCityList();
    }

    public void displayCurrent(CityWeather cityWeather) {
        Log.d(TAG, "displayCurrent");
        nMainView.getWeatherFragmenttt().setCityWeather(cityWeather);
    }

    public void displayForecast(Forecast forecast) {
        Log.d(TAG, "displayForecast");
        nMainView.getWeatherFragmenttt().setForecast(forecast);
    }

    public void displayCurrentByCity(CurrentByCity currentByCity) {
        nMainView.getWeatherFragmenttt().setCurrentByCity(currentByCity);
    }

    public void storeCityName(String name) {
        Log.d(TAG, name);
        SharedPreferences sharedPreferences = nMainView.getSharedPreferences(Constant.LAST_LOCATION, Context.MODE_PRIVATE);
        sharedPreferences.edit().putString(Constant.LAST_LOCATION_CITY, name).apply();
        fetchLocalWeatherData();
    }

    private String getStoredCityName() {
        SharedPreferences sharedPreferences = nMainView.getSharedPreferences(Constant.LAST_LOCATION, Context.MODE_PRIVATE);
        return sharedPreferences.getString(Constant.LAST_LOCATION_CITY, null);
    }


}
