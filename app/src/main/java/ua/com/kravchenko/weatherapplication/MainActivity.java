package ua.com.kravchenko.weatherapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity {

    ImageView weather;
    TextView sunrise;
    TextView sunset;
    TextView temperature;
    TextView tempMin;
    TextView tempMax;
    TextView wind;
    TextView pressure;
    TextView humidity;
    TextView location;
    TextView updateTime;

    private String locationProvider = LocationManager.GPS_PROVIDER;
    private LocationManager locationManager;
    private LocationListener locationListener;

    final long UPDATE_TIME = 5000;
    final float UPDATE_DISTANCE = 1000;
    final int REQUEST_CODE = 101;

    private final String API_KEY = "40f292c233e75229c5b82b736ad75d81";
    private final String UNITS = "metric";
    private final String WEATHER_URL = "https://api.openweathermap.org/data/2.5/weather";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        weather = findViewById(R.id.iv_weather);
        sunrise = findViewById(R.id.tv_sunrise);
        sunset = findViewById(R.id.tv_sunset);
        temperature = findViewById(R.id.tv_temperature);
        tempMax = findViewById(R.id.tv_max_temperature);
        tempMin = findViewById(R.id.tv_min_temperature);
        wind = findViewById(R.id.tv_wind);
        pressure = findViewById(R.id.tv_pressure);
        humidity = findViewById(R.id.tv_humidity);
        location = findViewById(R.id.tv_location);
        updateTime = findViewById(R.id.tv_update_tracker);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getCurrentLocation();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (locationManager != null) {
            locationManager.removeUpdates(locationListener);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(MainActivity.this,
                        "Location acquired",
                        Toast.LENGTH_SHORT).show();
                getCurrentLocation();
            }
        }
    }

    private void getCurrentLocation() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = location -> {
            String latitude = String.valueOf(location.getLatitude());
            String longitude = String.valueOf(location.getLongitude());

            RequestParams params = new RequestParams();
            params.put("lat", latitude);
            params.put("lon", longitude);
            params.put("appid", API_KEY);
            params.put("units", UNITS);
            connectToOpenWeather(params);
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_CODE);
            return;
        }

        locationManager.requestLocationUpdates(locationProvider, UPDATE_TIME, UPDATE_DISTANCE, locationListener);
    }

    private void connectToOpenWeather(RequestParams params) {

        AsyncHttpClient client = new AsyncHttpClient();
        client.get(WEATHER_URL, params, new JsonHttpResponseHandler() {

            @Override
            public void onStart() {
                findViewById(R.id.pb_progress_bar).setVisibility(View.VISIBLE);
                findViewById(R.id.rl_main_container).setVisibility(View.GONE);
                findViewById(R.id.tv_error_message).setVisibility(View.GONE);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Toast.makeText(MainActivity.this, "Data fetched successfully!", Toast.LENGTH_SHORT).show();
                WeatherData weatherData = WeatherData.parseWeatherData(response);
                updateUI(weatherData);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                findViewById(R.id.tv_error_message).setVisibility(View.VISIBLE);
                findViewById(R.id.pb_progress_bar).setVisibility(View.GONE);
            }
        });
    }

    private void updateUI(WeatherData weatherData) {
        findViewById(R.id.pb_progress_bar).setVisibility(View.GONE);
        findViewById(R.id.rl_main_container).setVisibility(View.VISIBLE);

        Format time = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
        Format date = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.ENGLISH);
        int resourceID = getResources().getIdentifier(weatherData.getIcon(), "drawable", getPackageName());

        sunrise.setText(time.format(weatherData.getSunrise()));
        sunset.setText(time.format(weatherData.getSunset()));
        temperature.setText(weatherData.getTemperature());
        tempMin.setText(weatherData.getTempMin());
        tempMax.setText(weatherData.getTempMax());
        pressure.setText(weatherData.getPressure());
        humidity.setText(weatherData.getHumidity());
        wind.setText(weatherData.getWind());
        location.setText(weatherData.getLocation());
        updateTime.setText(date.format(new Date()));
        weather.setImageResource(resourceID);
    }
}
