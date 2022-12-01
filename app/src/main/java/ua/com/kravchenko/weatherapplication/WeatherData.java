package ua.com.kravchenko.weatherapplication;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class WeatherData {

    private Date mSunrise;
    private Date mSunset;
    private String mTemperature;
    private String mTempMin;
    private String mTempMax;
    private String mWind;
    private String mPressure;
    private String mHumidity;
    private String mLocation;
    private String mWeatherIcon;

    public static WeatherData parseWeatherData(JSONObject jsonObject) {
        WeatherData weatherData = new WeatherData();
        try {
            JSONObject weather = jsonObject.getJSONArray("weather").getJSONObject(0);
            JSONObject main = jsonObject.getJSONObject("main");
            JSONObject wind = jsonObject.getJSONObject("wind");
            JSONObject sys = jsonObject.getJSONObject("sys");

            int temp = main.getInt("temp");
            int tempMin = main.getInt("temp_min");
            int tempMax = main.getInt("temp_max");
            long sunrise = sys.getLong("sunrise") * 1000 + 7200000;
            long sunset = sys.getLong("sunset") * 1000 + 7200000;

            weatherData.mSunrise = new Date(sunrise);
            weatherData.mSunset = new Date(sunset);
            weatherData.mTemperature = temp + "°C";
            weatherData.mTempMin = tempMin + "°C";
            weatherData.mTempMax = tempMax + "°C";
            weatherData.mPressure = main.getString("pressure");
            weatherData.mHumidity = main.getString("humidity") + "%";
            weatherData.mWind = wind.getString("speed");
            weatherData.mLocation = jsonObject.getString("name") + ", " + sys.getString("country");
            weatherData.mWeatherIcon = updateWeatherIcon(weather.getInt("id"));

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return weatherData;
    }

    private static String updateWeatherIcon(int condition){
        if (condition == 800){
            return "day_clear";
        } else {
            switch (condition / 100){
                case 2: return "thunderstorm";
                case 3: return "drizzle";
                case 5: return "rain";
                case 6: return "snow";
                case 7: return "fog";
                case 8: return "cloudy";
            }
        }
        return "default";
    }

    public Date getSunrise() { return mSunrise; }

    public Date getSunset() { return mSunset; }

    public String getTemperature() { return mTemperature; }

    public String getTempMin() { return mTempMin; }

    public String getTempMax() { return mTempMax; }

    public String getWind() { return mWind; }

    public String getPressure() { return mPressure; }

    public String getHumidity() { return mHumidity; }

    public String getLocation() { return mLocation; }

    public String getIcon() { return mWeatherIcon; }

}
