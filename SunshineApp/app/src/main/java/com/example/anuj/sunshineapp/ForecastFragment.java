package com.example.anuj.sunshineapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment {

    ArrayAdapter<String> stringArrayAdapter;
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
            FetchWeatherTask fetchWeatherTask = new FetchWeatherTask();

            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String location = sharedPreferences.getString(getString(R.string.pref_location_key),
                    getString(R.string.pref_location_default));
            
            String postelCode = "411045";
           fetchWeatherTask.execute(location);
            return true;
        }

        return  super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //hasOptionsMenu();
        View rootView = inflater.inflate(R.layout.forecast_fragment, container, false);
        String[] strings = CreateArrayStringList();
        List<String> stringArrayList = new ArrayList<String>(Arrays.asList(strings));

        stringArrayAdapter = new ArrayAdapter<String>(getActivity(), R.layout.list_item_forecast,
                R.id.list_item_forecast_textview, stringArrayList);
        ListView listView = (ListView) rootView.findViewById(R.id.listView_forecast);
        listView.setAdapter(stringArrayAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView textView = (TextView) view;
                if(textView == null)
                    return;
//                Context context = getActivity().getApplicationContext();
//                CharSequence text = textView.getText();
//                int duration = Toast.LENGTH_SHORT;
//
//                Toast toast = Toast.makeText(context, text, duration);
//                toast.show();

                Intent intent = new Intent(getActivity(),DetailActivity.class)
                        .putExtra(Intent.EXTRA_TEXT, textView.getText());
                startActivity(intent);

            }
        });
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


    public class FetchWeatherTask extends AsyncTask<String, Void, String[]>
    {
        private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();

        @Override
        protected String[] doInBackground(String... params) {
            if(params.length == 0)
                return null;
            String postalCode = params[0];
            String[] forecastJsonStrArr = null;
            return httpRestApiCall(postalCode);

        }

        @Override
        protected void onPostExecute(String[] strings) {
            if(strings != null)
            {
                stringArrayAdapter.clear();
                for(String dayForecastStr: strings)
                {
                    stringArrayAdapter.add(dayForecastStr);
                }
            }
        }

        private String[] httpRestApiCall(String postalCode) {
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            String forecastJsonStr = null;
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            Uri.Builder uriBuilder = new Uri.Builder();
            uriBuilder.scheme("http")
                    .authority("api.openweathermap.org")
                    .appendPath("data")
                    .appendPath("2.5")
                    .appendPath("forecast")
                    .appendPath("daily")
                    .appendQueryParameter("q",postalCode)
                    .appendQueryParameter("mode", "json")
                    .appendQueryParameter("units", "metric")
                    .appendQueryParameter("cnt", "7")
                    .appendQueryParameter("APPID","cb6080db92d88726d09b48aab743f818");

            //String urlApi = "http://api.openweathermap.org/data/2.5/forecast/daily?q=94043&mode=json&units=metric&cnt=7&APPID=cb6080db92d88726d09b48aab743f818";
            String urlApi = uriBuilder.build().toString();
            // Will contain the raw JSON response as a string.
            forecastJsonStr = null;

            try {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are avaiable at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast
                //URL url = new URL(urlApi);
                URL url = buildUrl(postalCode);

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
                Log.i(LOG_TAG, forecastJsonStr);

                try {
                    return WeatherDataParser.getWeatherDataFromJson(forecastJsonStr, 7);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
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
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }
            return null;
        }

        private URL buildUrl(String postalCode)
        {
            URL url = null;
            String format = "json";
            String units = "metric";
            int numDays = 7;
            String appId = "cb6080db92d88726d09b48aab743f818";

            final String FORECAST_BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/daily?";
            final String QUERY_PARAM = "q";
            final String FORMAT_PARAM = "mode";
            final String UNITS_PARAM = "units";
            final String DAYS_PARAM = "cnt";
            final String APPID = "APPID";

            Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                    .appendQueryParameter(QUERY_PARAM, postalCode)
                    .appendQueryParameter(FORECAST_BASE_URL, format)
                    .appendQueryParameter(UNITS_PARAM, units)
                    .appendQueryParameter(DAYS_PARAM, Integer.toString(numDays))
                    .appendQueryParameter(APPID, appId)
                    .build();
            try {
                url = new URL(builtUri.toString());
                Log.i(LOG_TAG, "Built URI" + builtUri.toString());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

            return url;

        }
    }

}