package com.bangaloretalkies.corehacker.cheinthusan;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
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
import android.widget.Toast;

import com.bangaloretalkies.corehacker.cheinthusan.mediaplayer.LocalPlayerActivity;
import com.google.android.gms.cast.MediaInfo;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
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

        buttonSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchText = editTextSearchText.getText().toString();
                Log.v("ChEinthusanSearch", "Search Text = " + searchText);

                new MyTask().execute(true);
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
                String url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/CastVideos/hls/DesigningForGoogleCast.m3u8";
                String mimeType = "application/x-mpegurl";
                int duration = 333;

                MediaInfo item = new MediaInfo.Builder(url)
                        .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                        .setContentType(mimeType)
                        //.setMetadata(movieMetadata)
                        //.setMediaTracks(tracks)
                        .setStreamDuration(duration * 1000)
                        //.setCustomData(jsonObj)
                        .build();

                Intent intent = new Intent(getApplicationContext(), LocalPlayerActivity.class);
                intent.putExtra("media", item);
                intent.putExtra("shouldStart", false);
                ActivityCompat.startActivity(ChEinthusanSearchActivity.this, intent, null);

            }

        });
    }

    protected void connect(String url)
    {
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

        Log.d("ChEinthusanSearch", "\nSending 'GET' request to URL : " + url);

        Log.d("ChEinthusanSearch", "Response Code : " + responseCode);


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

        //print result
        Log.d("ChEinthusan", response.toString());
        Log.d("ChEinthusanSearch", "Parsing html...");
        Document doc = Jsoup.parse(response.toString());
        Log.d("ChEinthusanSearch", "Parsing html... complete");
        Elements info = doc.select("div#non-realtime-search");
        info = info.select("div.search-category");
        info = info.select("ul:has(li)");
        Elements searchedLinks = info.select("a[href]");

        Log.d("ChEinthusanSearch", "Search Results Size: " + searchedLinks.size());

        searchList.clear();
        searchSet.clear();
        for (Element link : searchedLinks) {
            Log.d("ChEinthusanSearch", " * a: <" + link.attr("href") + ">  (" + link.text() + ")");


            if (!searchSet.contains(link.text())) {
                searchList.add(link.text());
                searchSet.add(link.text());
            }

        }

        Log.d("ChEinthusanSearch", "Search Results: " + info.html());

        adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, searchList);
        updateSearchList ();
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

    public class MyTask extends AsyncTask<Boolean, Integer, Boolean> {
        @Override
        protected Boolean doInBackground(Boolean... booleen) {
            Log.v("ChEinthusanSearch", "Search String = " + searchText);
            connect("http://www.einthusan.com/search?lang=hindi&search_query=" + searchText);
            return null;
        }
    }
}

/*
Intent intent = new Intent(getActivity(), LocalPlayerActivity.class);
            intent.putExtra("media", item);
            intent.putExtra("shouldStart", false);
            ActivityCompat.startActivity(getActivity(), intent, options.toBundle());
 */
