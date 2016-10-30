package com.huangsz.android.screentip.data.weather;

import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Async task to fetch weather data from openweather.
 */
class FetchWeatherTask extends AsyncTask<String, Void, WeatherData> {

    private final String TAG = "FetchWeatherTask";

    // http://openweathermap.org/current

    // Construct the URL for the OpenWeatherMap query
    // Possible parameters are avaiable at OWM's forecast API page, at
    // http://openweathermap.org/API#forecast
    private static final String FORECAST_BASE_URL =
            "http://api.openweathermap.org/data/2.5/weather?";
    private static final String APP_ID = "4e6560b5c2663e3b71a12f31e1a30099";
    private static final String UNITS_PARAM = "units";
    private static final String APP_ID_PARAM = "APPID";
    private static final String LATITUDE_PARAM = "lat";
    private static final String LONGITUDE_PARAM = "lon";

    private final PostExecuteCallback postExecuteHandler;

    FetchWeatherTask(PostExecuteCallback postExecuteHandler) {
        this.postExecuteHandler = postExecuteHandler;
    }

    @Override
    @Nullable
    protected WeatherData doInBackground(String... params) {
        checkArgument(params.length == 3);

        // api.openweathermap.org/data/2.5/weather?lat=35&lon=139&units=metric
        String latitude = params[0];
        String longitude = params[1];
        String units = params[2];

        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String forecastJsonStr = null;

        try {
            Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                    .appendQueryParameter(LATITUDE_PARAM, latitude)
                    .appendQueryParameter(LONGITUDE_PARAM, longitude)
                    .appendQueryParameter(UNITS_PARAM, units)
                    .appendQueryParameter(APP_ID_PARAM, APP_ID)
                    .build();

            URL url = new URL(builtUri.toString());

            // Create the request to OpenWeatherMap, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return null;
            }
            forecastJsonStr = buffer.toString();
        } catch (IOException e) {
            Log.e(TAG, "Error ", e);
            // If the code didn't successfully get the weather data, there's no point in attempting
            // to parse it.
            return null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(TAG, "Error closing stream", e);
                }
            }
        }
        try {
            return getWeatherDataFromJson(forecastJsonStr);
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage(), e);
        }
        // This will only happen if there was an error getting or parsing the forecast.
        return null;
    }

    @Override
    protected void onPostExecute(WeatherData weatherData) {
        postExecuteHandler.onWeatherDataFetched(weatherData);
    }

    @Nullable
    private WeatherData getWeatherDataFromJson(String forecastJsonStr)
            throws JSONException {
        // These are the names of the JSON objects that need to be extracted.
        final String KEY_MAIN = "main";
        final String KEY_TEMPERATURE = "temp";

        WeatherData weatherData = new WeatherData();

        try {
            JSONObject forecastJson = new JSONObject(forecastJsonStr);
            JSONObject mainJson = forecastJson.getJSONObject(KEY_MAIN);
            weatherData.setCurrentTemperature(mainJson.getDouble(KEY_TEMPERATURE));
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return weatherData;
    }

    /** Callback to handle weather data after it's fetched from the cloud. */
    interface PostExecuteCallback {
        void onWeatherDataFetched(WeatherData weatherData);
    }
}
