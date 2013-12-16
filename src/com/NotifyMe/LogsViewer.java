package com.NotifyMe;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import com.NotifyMe.Lists.LogsList;
import com.NotifyMe.Utilities.APIResponse;
import com.NotifyMe.Utilities.JSONUtilities;
import com.NotifyMe.Lists.NotificationsList;
import com.NotifyMe.Utilities.ServerUtilities;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class LogsViewer extends Activity {
    ListView list;
    Context context;
    String[] logs;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getApplicationContext();

        setContentView(R.layout.notifications_viewer);

        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                logs = getLogs(getIntent().getExtras().getString("UID"));
                return  null;
            }

            @Override
            protected void onPostExecute(String msg) {
                if(logs == null)
                    return;
                
                LogsList adapter = new LogsList(LogsViewer.this, logs);
                list = (ListView)findViewById(R.id.list);
                list.setAdapter(adapter);
                list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                Intent intent = new Intent(getApplicationContext(), WebViewer.class);
//                intent.setData(Uri.parse(servicesUrl[position]));
//                startActivity(intent);
                    }
                });
            }
        }.execute(null, null, null);
    }

    private String[] getLogs(String uid) {
        List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
        params.add(new BasicNameValuePair("uid", uid));
        APIResponse response = ServerUtilities.post(MainActivity.NOTIFYME_API_URL + MainActivity.LOG_LIST_ENDPOINT, params);
        if(response.APISuccess) {
            try {
                JSONArray logs = response.jsonResponse.getJSONArray("logs");
                ArrayList<String> logs_list = JSONUtilities.jsonArrayToList(logs);
                return logs_list.toArray(new String[logs_list.size()]);
            } catch (JSONException e) {
                showErrorNotif();
            }
        } else {
            showErrorNotif();
        }
        return null;
    }

    private void showErrorNotif() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, "Error loading logs", Toast.LENGTH_LONG).show();
            }
        });
    }
}