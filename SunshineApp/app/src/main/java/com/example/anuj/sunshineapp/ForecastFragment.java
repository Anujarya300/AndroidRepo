package com.example.anuj.sunshineapp;

import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment {

    public ForecastFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Add this line in order for this fragment to handle menu events.

        setHasOptionsMenu(true);;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forecastfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.action_refresh){
            return true;
        }

        return  super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        hasOptionsMenu();
        View rootView = inflater.inflate(R.layout.forecast_fragment, container, false);
        String[] strings = CreateArrayStringList();
        List<String> stringArrayList = new ArrayList<String>(Arrays.asList(strings));

        ArrayAdapter<String> stringArrayAdapter = new ArrayAdapter<String>(getActivity(), R.layout.list_item_forecast,
                R.id.list_item_forecast_textview, stringArrayList);
        ListView listView = (ListView) rootView.findViewById(R.id.listView_forecast);
        listView.setAdapter(stringArrayAdapter);


        return rootView;
    }

    private String[] CreateArrayStringList() {
        String[] strings = new String[15];
        strings[0] = "Today - Sunny - 88/63";
        strings[1] = "Tomorrow - Cloudy - 80/52";
        strings[2] = "Weds - Foggy - 82/61";
        strings[3] = "Thurs - Rainy - 78/60";
        strings[4] = "Fri - Foggy - 70/50";
        strings[5] = "Sat - Cloudy - 65/48";
        strings[6] = "Sun - Sunny - 72/63";
        strings[7] = "Today - Sunny - 88/63";
        strings[8] = "Tomorrow - Cloudy - 80/52, Tomorrow - Cloudy - 80/52, Tomorrow - Cloudy - 80/52";
        strings[9] = "Weds - Foggy - 82/61";
        strings[10] = "Thurs - Rainy - 78/60";
        strings[11] = "Fri - Foggy - 70/50";
        strings[12] = "Sat - Cloudy - 65/48";
        strings[13] = "Sun - Sunny - 72/63";
        strings[14] = "Sun - Sunny - 72/63";

        return strings;
    }


    public class FetchWeatherTask extends AsyncTask<String, Void, String>
    {
        private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();

        protected String doInBackground(String... params) {
            String forecastJsonStr = null;
            if(!httpRestApiCall(forecastJsonStr))
                return null;
            else return forecastJsonStr;
        }

        private boolean httpRestApiCall(String forecastJsonStr) {
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            forecastJsonStr = null;

            try {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are avaiable at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast
                URL url = new URL("http://api.openweathermap.org/data/2.5/forecast/daily?q=94043&mode=json&units=metric&cnt=7");

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return false;
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
                    return false;
                }
                forecastJsonStr = buffer.toString();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return false;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }
            return true;
        }
    }

}