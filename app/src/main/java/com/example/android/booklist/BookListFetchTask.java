package com.example.android.booklist;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by lokesh on 28/6/16.
 */
public class BookListFetchTask extends AsyncTask<String, Void, String[]> {

    private final String LOG_TAG = BookListFetchTask.class.getSimpleName();

    private ArrayAdapter<String> mbookAdapter;

    private final int maxResults = 10;

    private String mtopic = "android";



  public  BookListFetchTask(ArrayAdapter<String> bookAdapter) {
      mbookAdapter = bookAdapter;


  }


    @Override
    protected String[] doInBackground(String... params) {

// These two need to be declared outside the try/catch
// so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

// Will contain the raw JSON response as a string.
        String bookListJsonStr = null;


        try {
            // Construct the URL for the OpenWeatherMap query
            // Possible parameters are available at OWM's forecast API page, at
            // http://openweathermap.org/API#forecast
            final String BOOKLIST_BASE_URL = " https://www.googleapis.com/books/v1/volumes?";
            final String QUERY_PARAM = "q";
            final String MAXRES_PARAM = "maxResults";

            Uri builtUri = Uri.parse(BOOKLIST_BASE_URL).buildUpon()
                    .appendQueryParameter(QUERY_PARAM, "android")
                    .appendQueryParameter(MAXRES_PARAM, Integer.toString(maxResults))
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
                bookListJsonStr = null;
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
                bookListJsonStr = null;
            }
            bookListJsonStr = buffer.toString();
            Log.v(LOG_TAG, "Forecast string: " + bookListJsonStr);
        } catch (IOException e) {
            Log.e("PlaceholderFragment", "Error ", e);
            // If the code didn't successfully get the weather data, there's no point in attempting
            // to parse it.
            bookListJsonStr = null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e("PlaceholderFragment", "Error closing stream", e);
                }
            }
        }


        try {
            return getBookListFromJson(bookListJsonStr);
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
        return null;

    }

    @Override
    protected void onPostExecute(String[] result) {
        if (result != null) {
            mbookAdapter.clear();
            for (String book : result) {
                mbookAdapter.add(book);
            }
        }
    }


    private String[] getBookListFromJson(String bookListJsonStr) throws JSONException {
        final String BL_ITEMS = "items";
        final  String BL_VOLUME = "volumeInfo";

        JSONObject booklistJson = new JSONObject(bookListJsonStr);
        JSONArray items = booklistJson.getJSONArray(BL_ITEMS);

        String[] resultStrs = new String[maxResults];
        for (int i = 0; i < items.length(); i++) {
            JSONObject oneBook = items.getJSONObject(i);
            JSONObject volumeObject = oneBook.getJSONObject(BL_VOLUME);
            String title = volumeObject.getString("title");
            String author = volumeObject.getJSONArray("authors").getString(0);

            resultStrs[i] = title + " " + author;
        }


        for (String s : resultStrs) {
            Log.v(LOG_TAG, "Forecast entry: " + s);
        }
        return resultStrs;


    }
}


