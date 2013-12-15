package com.NotifyMe.Utilities;

import android.app.Activity;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.NotifyMe.R;
import org.apache.commons.lang3.text.WordUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

public class NotificationsList extends ArrayAdapter<String> {

    private final Activity context;
    private final String[] notifications;

    public NotificationsList(Activity context, String[] notifications) {
        super(context, R.layout.notification_row, notifications);
        this.context = context;
        this.notifications = notifications;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.notification_row, null, true);

        TextView text = (TextView) rowView.findViewById(R.id.txt);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.img);
        try {
            JSONObject notification = new JSONObject(notifications[position].trim());
            Iterator<?> keys = notification.keys();

            while( keys.hasNext() ){
                String key = (String)keys.next();
                if (key.equals("_id") || key.equals("uid"))
                    continue;
                String value = notification.getString(key);
                if(key.equals("service")) {
                    text.append(Html.fromHtml("<b>" + WordUtils.capitalize(value) + "</b><br />"));
                    // This should be in a hashmap
                    if(value.equals("reddit")) {
                        imageView.setImageResource(R.drawable.reddit);
                    } else if(value.equals("weather")) {
                        imageView.setImageResource(R.drawable.weather);
                    } else if(value.equals("github")) {
                        imageView.setImageResource(R.drawable.github);
                    } else if(value.equals("poly")) {
                        imageView.setImageResource(R.drawable.poly);
                    }
                } else {
                    text.append(WordUtils.capitalize(key) + " : " + value + "\n");
                }
            }
        } catch (JSONException e) {

        }

        return rowView;
    }
}