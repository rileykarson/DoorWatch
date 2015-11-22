package ca.queensu.doorwatch;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Debug;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.okhttp.*;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }
/*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
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
*/
    @Override
    protected void onStart() {
        super.onStart();
        doSomethingRepeatedly();
    }

    private void updateDoorStatus() {
        new AsyncTask<Void, Void, String>() {
            @Override protected String doInBackground(Void... params) {
                OkHttpClient client = new OkHttpClient();
                String url = "http://cindygao.me:8000";
                Request request = new Request.Builder()
                        .url(url)
                        .build();
                String str;
                try {
                    Response res = client.newCall(request).execute();
                    str = res.body().string();
                } catch (IOException e) {
                    str = "lol david smells";
                }
                return str;
            }
            @Override protected void onPostExecute(String result) {
                TextView door_status = (TextView)findViewById(R.id.door_status);
                TextView update_time = (TextView)findViewById(R.id.update_time);
                CiscResponse resp = getResultResponse(result);
                door_status.setText(resp.message);
                update_time.setText(resp.lastUpdate);
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, null);
    }

    private CiscResponse getResultResponse(String result) {
        CiscResponse resp = new CiscResponse();
        try {
            JSONObject res = new JSONObject(result);
            String status = res.getString("door");
            String date = res.getString("time");
            if (status == "1") {
                resp.message = "The door is open.";
            } else {
                resp.message = "The door is closed.";
            }
            resp.lastUpdate = date;
            return resp;
        } catch (JSONException ex) {
            resp.message = "Server Failed";
            resp.lastUpdate = "N/A";
            return resp;
        }
    }
    private class CiscResponse {
        String message;
        String lastUpdate;
    }

    //Every 10000 ms
    private void doSomethingRepeatedly() {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate( new TimerTask() {
            public void run() {

                try{
                    updateDoorStatus();

                }
                catch (Exception e) {
                    // TODO: handle exception
                }

            }
        }, 0, 10000);
    }
}
