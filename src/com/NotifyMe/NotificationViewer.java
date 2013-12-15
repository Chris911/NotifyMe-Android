package com.NotifyMe;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import com.NotifyMe.Utilities.*;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class NotificationViewer extends Activity {
    ListView list;
    String[] notifications;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.notifications_viewer);

        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                notifications = getNotifications(getIntent().getExtras().getString("UID"));
                return  null;
            }

            @Override
            protected void onPostExecute(String msg) {
                NotificationsList adapter = new NotificationsList(NotificationViewer.this, notifications);
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

    private String[] getNotifications(String uid) {
        List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
        params.add(new BasicNameValuePair("uid", uid));
        APIResponse response = ServerUtilities.post(MainActivity.NOTIFYME_API_URL + MainActivity.NOTIF_LIST_ENDPOINT, params);
        if(response.APISuccess) {
            try {
                JSONArray notifications = response.jsonResponse.getJSONArray("notifications");
                ArrayList<String> notifications_list = JSONUtilities.jsonArrayToList(notifications);
                return notifications_list.toArray(new String[notifications_list.size()]);
            } catch (JSONException e) {
                Toast.makeText(this.getBaseContext(), "Error loading notifications", Toast.LENGTH_LONG);
            }
        } else {
            Toast.makeText(this.getBaseContext(), "Error loading notifications", Toast.LENGTH_LONG);
        }
        return null;
    }
}