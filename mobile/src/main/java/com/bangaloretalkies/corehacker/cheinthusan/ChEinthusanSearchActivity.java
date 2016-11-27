package com.bangaloretalkies.corehacker.cheinthusan;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.bangaloretalkies.corehacker.cheinthusan.mediaplayer.LocalPlayerActivity;
import com.google.android.gms.cast.MediaInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ChEinthusanSearchActivity extends AppCompatActivity {

    EditText editTextSearchText;
    Button buttonSearch;
    ListView listViewSearchResults;
    String searchText;

    List<String> searchList = new ArrayList<String>();
    Set<String> searchSet = new LinkedHashSet<>();
    ArrayAdapter<String> adapter;
    ArrayAdapter<String> emptyAdapter;
    Map <String, ChEinthusanMovieInfo> searchMap = new HashMap<>();
    private String lang;
    private RadioGroup radioGroup;

    public void onRadioButtonClicked(View view) {
        Log.d("ChEinthusan", "Radio button checked");

        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        EinthusanFetchTask latestTask = new EinthusanFetchTask("latest");


        switch(view.getId()) {
            case R.id.radio_lang_hindi:
                if (checked)
                    lang = "hindi";
                break;
            case R.id.radio_lang_kannada:
                if (checked)
                    lang = "kannada";
                break;
            case R.id.radio_lang_tamil:
                if (checked)
                    lang = "tamil";
                break;
            case R.id.radio_lang_telugu:
                if (checked)
                    lang = "telugu";
                break;
        }
        radioGroup.check(view.getId());

        latestTask.execute();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ch_einthusan_search);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        editTextSearchText = (EditText) findViewById(R.id.editTextSearchText);
        buttonSearch = (Button) findViewById(R.id.buttonSearch);
        listViewSearchResults = (ListView) findViewById(R.id.listViewSearchResults);

        radioGroup = (RadioGroup) findViewById(R.id.radio_group_lang_select);
        radioGroup.check(R.id.radio_lang_hindi);
        lang = "hindi";

        buttonSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchText = editTextSearchText.getText().toString();
                Log.v("ChEinthusanSearch", "Search Text = " + searchText);

                EinthusanFetchTask searchTask = new EinthusanFetchTask("search");
                searchTask.setSearch(searchText);
                searchTask.setLang(lang);
                searchTask.execute();
            }
        });

        listViewSearchResults.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // ListView Clicked item index
                int itemPosition     = position;

                // ListView Clicked item value
                String  itemValue    = (String) searchList.get(position);

                // Show Alert
                Toast.makeText(getApplicationContext(),
                        "Position: " + itemPosition + "  ListItem: " + itemValue , Toast.LENGTH_SHORT)
                        .show();

                ChEinthusanMovieInfo selectedMovie = searchMap.get(itemValue);
                EinthusanFetchTask getUrlTask = new EinthusanFetchTask("geturl");
                getUrlTask.setPlaybackUrlId(selectedMovie.getId());
                getUrlTask.setMovieTitle(selectedMovie.getName());
                getUrlTask.execute();
            }

        });
    }

    public void updateSearchList ()
    {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // This code will always run on the UI thread, therefore is safe to modify UI elements.
                // Assign adapter to ListView
                listViewSearchResults.setAdapter(emptyAdapter);
                listViewSearchResults.setAdapter(adapter);
            }
        });
    }

    protected String httpGet (String url) {
        HttpURLConnection con = null;
        URL obj = null;
        try {
            obj = new URL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        try {
            con = (HttpURLConnection) obj.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            con.setRequestMethod("GET");
        } catch (ProtocolException e) {
            e.printStackTrace();
        }

        int responseCode = 0;
        try {
            responseCode = con.getResponseCode();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.d("ChEinthusan", "\nSending 'GET' request to URL : " + url);
        Log.d("ChEinthusan", "Response Code : " + responseCode);

        BufferedReader in = null;
        try {
            in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        String inputLine;
        StringBuffer response = new StringBuffer();

        try {
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String res = response.toString();
        Log.d("ChEinthusan", "response: " + res);
        return res;
    }

    protected void searchMovies(String url) {
        String res = httpGet(url);

        JSONArray results = null;
        try {
            JSONObject resObj = new JSONObject(res);
            results = resObj.getJSONArray("results");
        } catch (JSONException e) {
            // Oops
        }

        if (null != results) {
            searchList.clear();
            searchSet.clear();
            for (int i=0; i < results.length(); i++) {
                try {
                    JSONObject entry = results.getJSONObject(i);
                    // Pulling items from the array
                    String title = entry.getString("title");
                    String id = entry.getString("id");
                    // String cover = entry.getString("cover");
                    String movieUrl = entry.getString("url");
                    String lang = entry.getString("lang");
                    Log.d("ChEinthusan", "id : " + id + ", title: " + title + ", lang: " + lang + ", url: " + movieUrl);

                    if (!searchSet.contains(title)) {
                        searchList.add(title);
                        searchSet.add(title);
                        searchMap.put(title, new ChEinthusanMovieInfo(movieUrl, id, title, lang));
                    }
                } catch (JSONException e) {
                    // Oops
                }
            }
            adapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_list_item_1, android.R.id.text1, searchList);
            updateSearchList ();
        }
    }

    protected void getPlaybackUrl(String url, String title) {
        String res = httpGet(url);

        if (res.startsWith("http") && res.contains("m3u8")) {
            String mimeType = "application/vnd.apple.mpegurl";
            int duration = 333;

            MediaInfo item = new MediaInfo.Builder(res)
                    .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                    .setContentType(mimeType)
                    //.setMetadata(movieMetadata)
                    //.setMediaTracks(tracks)
                    //.setStreamDuration(duration * 1000)
                    //.setCustomData(jsonObj)
                    .build();

            ChEinthusanMovieInfo movie = searchMap.get(title);

            Intent intent = new Intent(getApplicationContext(), LocalPlayerActivity.class);
            intent.putExtra("media", item);
            intent.putExtra("shouldStart", false);
            intent.putExtra("movie", movie);
            ActivityCompat.startActivity(ChEinthusanSearchActivity.this, intent, null);
        }
    }

    public class EinthusanFetchTask extends AsyncTask<Boolean, Integer, Boolean> {
        private String op;
        private String id;
        private String lang;
        private String search;
        private String title;

        EinthusanFetchTask(String op) {
            this.op = op;
        }

        public void setSearch(String search) {
            this.search = search;
        }

        public void setLang(String lang) {
            this.lang = lang;
        }

        public void setPlaybackUrlId (String id) {
            this.id = id;
        }

        public void setMovieTitle (String title) {
            this.title = title;
        }

        @Override
        protected Boolean doInBackground(Boolean... isChecked) {
            Log.v("ChEinthusan", "Operation: " + op);
            switch (op) {
                case "search": {
                    searchMovies("https://monsoonmania.com/einthusan/api/search?lang=" + this.lang + "&search_query=" + this.search);
                    break;
                }
                case "geturl": {
                    getPlaybackUrl ("https://monsoonmania.com/einthusan/api/geturl?id=" + this.id, this.title);
                    break;
                }
            }
            return null;
        }
    }
}
