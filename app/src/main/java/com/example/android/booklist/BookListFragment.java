package com.example.android.booklist;


import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

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
 * A simple {@link Fragment} subclass.
 */
public class BookListFragment extends Fragment {

    private ArrayAdapter<String> mbookAdapter;

    private int maxResults = 10;

    private String mtopic;


    public BookListFragment() {

        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        ArrayList<String> bookList = new ArrayList<String>();
        View rootView = inflater.inflate(R.layout.book_list, container, false);
       final EditText topicText = (EditText) rootView.findViewById(R.id.book);



        Button button = (Button) rootView.findViewById(R.id.search);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mtopic = topicText.getText().toString();

                if (mtopic.trim().length() != 0) {
                    BookListFetchTask bookListFetchTask = new BookListFetchTask();
                    bookListFetchTask.execute(mtopic);

                }
            }
        });


        ListView listView = (ListView) rootView.findViewById(R.id.book_list_view);

        mbookAdapter = new ArrayAdapter<String>(getActivity(), R.layout.list_items, R.id.book_title, bookList);

        listView.setAdapter(mbookAdapter);




        return rootView;


    }


    public class BookListFetchTask extends AsyncTask<String, Void, String[]> {

        private final String LOG_TAG = BookListFetchTask.class.getSimpleName();


        @Override
        protected String[] doInBackground(String... params) {

// These two need to be declared outside the try/catch
// so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

// Will contain the raw JSON response as a string.
            String bookListJsonStr = null;


            try {
                //Constructing url for book Api by adding parameters

                final String BOOKLIST_BASE_URL = " https://www.googleapis.com/books/v1/volumes?";
                final String QUERY_PARAM = "q";
                final String MAXRES_PARAM = "maxResults";

                Uri builtUri = Uri.parse(BOOKLIST_BASE_URL).buildUpon()
                        .appendQueryParameter(QUERY_PARAM, mtopic)
                        .appendQueryParameter(MAXRES_PARAM, Integer.toString(maxResults))
                        .build();

                URL url = new URL(builtUri.toString());

                // Create the request to google books api  and open the connection

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

                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    bookListJsonStr = null;
                }
                bookListJsonStr = buffer.toString();
                Log.v(LOG_TAG, "Forecast string: " + bookListJsonStr);
            } catch (IOException e) {
                Log.e("Booklist Fragment", "Error ", e);
                // If the code didn't successfully books fata, there's no point in attempting
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
                        Log.e("BookListFragment", "Error closing stream", e);
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
            final String OWM_LIST = "items";

            JSONObject booklistJson = new JSONObject(bookListJsonStr);

            JSONArray items = booklistJson.getJSONArray(OWM_LIST);

            String[] resultStrs = new String[maxResults];

            for (int i = 0; i < items.length(); i++) {
                JSONObject oneBook = items.getJSONObject(i); //Gets each item i.e is book
                //gets the volume Info Object from Api which contains book title and authors name
                JSONObject volumeObject = oneBook.getJSONObject("volumeInfo");
                String title = volumeObject.getString("title");
                String author = volumeObject.getJSONArray("authors").getString(0);

                resultStrs[i] = title + " \n" + author;
            }


            for (String s : resultStrs) {
                Log.v(LOG_TAG, "Forecast entry: " + s);
            }
            return resultStrs;


        }
    }

}
