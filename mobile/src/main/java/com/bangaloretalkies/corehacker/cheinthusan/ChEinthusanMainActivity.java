package com.bangaloretalkies.corehacker.cheinthusan;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.bangaloretalkies.corehacker.cheinthusan.mediaplayer.LocalPlayerActivity;
import com.google.android.gms.cast.CastMediaControlIntent;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.CastState;
import com.google.android.gms.cast.framework.CastStateListener;
import com.google.android.gms.cast.framework.IntroductoryOverlay;
import com.google.android.gms.cast.framework.SessionManagerListener;


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

public class ChEinthusanMainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private CastContext mCastContext;
    private CastStateListener mCastStateListener;
    private IntroductoryOverlay mIntroductoryOverlay;
    private MenuItem mediaRouteMenuItem;
    private MenuItem mQueueMenuItem;
    private CastSession mCastSession;
    private final SessionManagerListener<CastSession> mSessionManagerListener =
            new MySessionManagerListener();
    List<String> latestList = new ArrayList<String>();
    Set<String> latestSet = new LinkedHashSet<>();
    Map<String, ChEinthusanMovieInfo> latestMap = new HashMap<>();
    private ListView listViewLatestMovies;
    ArrayAdapter<String> adapter;
    ArrayAdapter<String> emptyAdapter;
    private RadioGroup radioGroup;

    private class MySessionManagerListener implements SessionManagerListener<CastSession> {

        @Override
        public void onSessionEnded(CastSession session, int error) {
            if (session == mCastSession) {
                mCastSession = null;
            }
            invalidateOptionsMenu();
        }

        @Override
        public void onSessionResumed(CastSession session, boolean wasSuspended) {
            mCastSession = session;
            invalidateOptionsMenu();
        }

        @Override
        public void onSessionStarted(CastSession session, String sessionId) {
            mCastSession = session;
            invalidateOptionsMenu();
        }

        @Override
        public void onSessionStarting(CastSession session) {
        }

        @Override
        public void onSessionStartFailed(CastSession session, int error) {
        }

        @Override
        public void onSessionEnding(CastSession session) {
        }

        @Override
        public void onSessionResuming(CastSession session, String sessionId) {
        }

        @Override
        public void onSessionResumeFailed(CastSession session, int error) {
        }

        @Override
        public void onSessionSuspended(CastSession session, int reason) {
        }
    }

    public void onRadioButtonClicked(View view) {
        Log.d("ChEinthusan", "Radio button checked");

        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        EinthusanFetchTask latestTask = new EinthusanFetchTask("latest");


        switch(view.getId()) {
            case R.id.radio_lang_hindi:
                if (checked)
                    latestTask.setLang("hindi");
                    break;
            case R.id.radio_lang_kannada:
                if (checked)
                    latestTask.setLang("kannada");
                    break;
            case R.id.radio_lang_tamil:
                if (checked)
                    latestTask.setLang("tamil");
                    break;
            case R.id.radio_lang_telugu:
                if (checked)
                    latestTask.setLang("telugu");
                    break;
        }
        radioGroup.check(view.getId());

        latestTask.execute();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ch_einthusan_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        // CastMediaControlIntent.DEFAULT_MEDIA_RECEIVER_APPLICATION_ID = "CC1AD845";

        Log.d("ChEinthusan", "cast receiver id: " + CastMediaControlIntent.DEFAULT_MEDIA_RECEIVER_APPLICATION_ID);
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mCastStateListener = new CastStateListener() {
            @Override
            public void onCastStateChanged(int newState) {
                if (newState != CastState.NO_DEVICES_AVAILABLE) {
                    showIntroductoryOverlay();
                }
            }
        };

        radioGroup = (RadioGroup) findViewById(R.id.radio_group_lang_select);
        radioGroup.check(R.id.radio_lang_hindi);
        radioGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("ChEinthusan", "Radio button checked");
                radioGroup.check(view.getId());
            }
        });

        listViewLatestMovies = (ListView) findViewById(R.id.listViewLatestMovies);
        listViewLatestMovies.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                int itemPosition     = position;
                String  itemValue    = (String) latestList.get(position);

                Toast.makeText(getApplicationContext(),
                        "ListItem: " + itemValue , Toast.LENGTH_SHORT)
                        .show();

                ChEinthusanMovieInfo selectedMovie = latestMap.get(itemValue);

                EinthusanFetchTask getUrlTask = new EinthusanFetchTask("geturl");
                getUrlTask.setPlaybackUrlId(selectedMovie.getId());
                getUrlTask.setMovieTitle(selectedMovie.getName());
                getUrlTask.execute();
            }

        });

        mCastContext = CastContext.getSharedInstance(this);
        mCastContext.registerLifecycleCallbacksBeforeIceCreamSandwich(this, savedInstanceState);

        EinthusanFetchTask latestTask = new EinthusanFetchTask("latest");
        latestTask.setLang("hindi");
        latestTask.execute();

    }

    @Override
    protected void onResume() {
        mCastContext.addCastStateListener(mCastStateListener);
        mCastContext.getSessionManager().addSessionManagerListener(
                mSessionManagerListener, CastSession.class);
        if (mCastSession == null) {
            mCastSession = CastContext.getSharedInstance(this).getSessionManager()
                    .getCurrentCastSession();
        }
        if (mQueueMenuItem != null) {
            mQueueMenuItem.setVisible(
                    (mCastSession != null) && mCastSession.isConnected());
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        mCastContext.removeCastStateListener(mCastStateListener);
        mCastContext.getSessionManager().removeSessionManagerListener(
                mSessionManagerListener, CastSession.class);
        super.onPause();
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

    /*
    https://monsoonmania.com/einthusan/api/geturl?id=2872
     */

    protected void getLatestMovies(String url)
    {
        String res = httpGet(url);
        JSONArray results = null;
        try {
            JSONObject resObj = new JSONObject(res);
            results = resObj.getJSONArray("results");
        } catch (JSONException e) {
            // Oops
        }

        if (null != results) {
            latestList.clear();
            latestSet.clear();
            for (int i=0; i < results.length(); i++) {
                try {
                    JSONObject entry = results.getJSONObject(i);
                    // Pulling items from the array
                    String title = entry.getString("title");
                    String id = entry.getString("id");
                    String cover = entry.getString("cover");
                    String movieUrl = entry.getString("url");
                    String lang = entry.getString("lang");
                    Log.d("ChEinthusan", "id : " + id + ", title: " + title + ", lang: " + lang + ", cover: " + cover + ", url: " + movieUrl);

                    if (!latestSet.contains(title)) {
                        latestList.add(title);
                        latestSet.add(title);
                        latestMap.put(title, new ChEinthusanMovieInfo(movieUrl, id, title, lang));
                    }
                } catch (JSONException e) {
                    // Oops
                }
            }
            adapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_list_item_1, android.R.id.text1, latestList);
            updateLatestMovies ();
        }
    }

    protected void getPlaybackUrl(String url, String title) {
        String res = httpGet(url);
        Log.d("ChEinthusan", "response: " + res);
        JSONArray results = null;

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

            ChEinthusanMovieInfo movie = latestMap.get(title);

            Intent intent = new Intent(getApplicationContext(), LocalPlayerActivity.class);
            intent.putExtra("media", item);
            intent.putExtra("shouldStart", false);
            intent.putExtra("movie", movie);
            ActivityCompat.startActivity(ChEinthusanMainActivity.this, intent, null);
        }
    }

    public class EinthusanFetchTask extends AsyncTask<Boolean, Integer, Boolean> {
        private String op;
        private String id;
        private String title;
        private String lang;

        EinthusanFetchTask(String op) {
            this.op = op;
        }

        public void setPlaybackUrlId (String id) {
            this.id = id;
        }

        public void setMovieTitle (String title) {
            this.title = title;
        }

        public void setLang(String lang) {
            this.lang = lang;
        }

        @Override
        protected Boolean doInBackground(Boolean... isChecked) {
            Log.v("ChEinthusan", "Operation: " + op);
            switch (op) {
                case "latest": {
                    getLatestMovies("https://monsoonmania.com/einthusan/api/latest?lang=" + this.lang);
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

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.ch_einthusan_main, menu);

        mediaRouteMenuItem = CastButtonFactory.setUpMediaRouteButton(getApplicationContext(), menu,
                R.id.media_route_menu_item);
        mQueueMenuItem = menu.findItem(R.id.action_show_queue);
        showIntroductoryOverlay();

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.action_show_queue).setVisible(
                (mCastSession != null) && mCastSession.isConnected());
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean dispatchKeyEvent(@NonNull KeyEvent event) {
        return mCastContext.onDispatchVolumeKeyEventBeforeJellyBean(event)
                || super.dispatchKeyEvent(event);
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_search) {
            // Handle the camera action
            Intent intent = new Intent(this, ChEinthusanSearchActivity.class);
            this.startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showIntroductoryOverlay() {
        if (mIntroductoryOverlay != null) {
            mIntroductoryOverlay.remove();
        }
        if ((mediaRouteMenuItem != null) && mediaRouteMenuItem.isVisible()) {
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    mIntroductoryOverlay = new IntroductoryOverlay.Builder(
                            ChEinthusanMainActivity.this, mediaRouteMenuItem)
                            .setTitleText(getString(R.string.introducing_cast))
                            .setOverlayColor(R.color.primary)
                            .setSingleTime()
                            .setOnOverlayDismissedListener(
                                    new IntroductoryOverlay.OnOverlayDismissedListener() {
                                        @Override
                                        public void onOverlayDismissed() {
                                            mIntroductoryOverlay = null;
                                        }
                                    })
                            .build();
                    mIntroductoryOverlay.show();
                }
            });
        }
    }

    public void updateLatestMovies ()
    {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // This code will always run on the UI thread, therefore is safe to modify UI elements.
                // Assign adapter to ListView
                listViewLatestMovies.setAdapter(emptyAdapter);
                listViewLatestMovies.setAdapter(adapter);
            }
        });
    }
}
