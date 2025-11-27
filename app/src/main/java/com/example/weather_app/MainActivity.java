package com.example.weather_app;

import androidx.appcompat.app.AppCompatActivity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    EditText cityName;
    Button search;
    TextView show;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cityName = findViewById(R.id.cityName);
        search = findViewById(R.id.search);
        show = findViewById(R.id.weather);

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String city = cityName.getText().toString().trim();
                if (city.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Enter a city name", Toast.LENGTH_SHORT).show();
                } else {
                
                    String url = "https://api.openweathermap.org/data/2.5/weather?q=" + city + "&appid=6738f67bf0bf4478825e3bea30425fae";
                    new GetWeatherTask().execute(url);
                }
            }
        });
    }

    private class GetWeatherTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            StringBuilder result = new StringBuilder();
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                InputStream inputStream = connection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                return result.toString();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if (result == null) {
                show.setText("Unable to fetch weather data");
                return;
            }

            try {
                JSONObject jsonObject = new JSONObject(result);

                // Location
                String city = jsonObject.getString("name");
                String country = jsonObject.getJSONObject("sys").getString("country");

                // Main weather info
                JSONObject main = jsonObject.getJSONObject("main");
                double temp = main.getDouble("temp") - 273.15; // Kelvin to Celsius
                double feelsLike = main.getDouble("feels_like") - 273.15;
                int humidity = main.getInt("humidity");
                double pressure = main.getDouble("pressure");

                // Wind
                JSONObject wind = jsonObject.getJSONObject("wind");
                double windSpeed = wind.getDouble("speed");

                // Condition
                String condition = jsonObject.getJSONArray("weather")
                        .getJSONObject(0)
                        .getString("description");

                // Build display string
                String weatherInfo = "Location: " + city + ", " + country + "\n" +
                        "Temperature: " + String.format("%.2f", temp) + "°C\n" +
                        "Feels Like: " + String.format("%.2f", feelsLike) + "°C\n" +
                        "Condition: " + condition + "\n" +
                        "Humidity: " + humidity + "%\n" +
                        "Pressure: " + pressure + " hPa\n" +
                        "Wind Speed: " + windSpeed + " m/s\n";

                show.setText(weatherInfo);

            } catch (Exception e) {
                e.printStackTrace();
                show.setText("Error parsing weather data");
            }
        }
    }
}
