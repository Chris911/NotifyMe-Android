package com.NotifyMe;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import com.NotifyMe.Utilities.ServicesList;

public class ServicesViewer extends Activity {
    ListView list;
    String[] services = {
            "Reddit",
            "Weather",
            "GitHub",
            "Polytechnique"
    };

    String[] servicesUrl = {
            "http://notifyme.cloudapp.net/services/reddit",
            "http://notifyme.cloudapp.net/services/weather",
            "http://notifyme.cloudapp.net/services/github",
            "http://notifyme.cloudapp.net/services/poly"
    };

    Integer[] imageId = {
            R.drawable.reddit,
            R.drawable.weather,
            R.drawable.github,
            R.drawable.poly
    };

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.services_viewer);

        ServicesList adapter = new ServicesList(ServicesViewer.this, services, imageId);
        list = (ListView)findViewById(R.id.list);
        list.setAdapter(adapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getApplicationContext(), WebViewer.class);
                intent.setData(Uri.parse(servicesUrl[position]));
                startActivity(intent);
            }
        });
    }
}