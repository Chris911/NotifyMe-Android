package com.NotifyMe.Lists;

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

public class LogsList extends ArrayAdapter<String> {

    private final Activity context;
    private final String[] logs;

    public LogsList(Activity context, String[] logs) {
        super(context, R.layout.notification_row, logs);
        this.context = context;
        this.logs = logs;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.notification_row, null, true);

        TextView text = (TextView) rowView.findViewById(R.id.txt);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.img);
        try {
            JSONObject log = new JSONObject(logs[position].trim());
            String service = log.getString("service");
            String time = log.getString("time");
            String message = log.getString("message");

            if(service.equals("reddit")) {
                imageView.setImageResource(R.drawable.reddit);
            } else if(service.equals("weather")) {
                imageView.setImageResource(R.drawable.weather);
            } else if(service.equals("github")) {
                imageView.setImageResource(R.drawable.github);
            } else if(service.equals("poly")) {
                imageView.setImageResource(R.drawable.poly);
            }

            text.append(Html.fromHtml("<b>" + WordUtils.capitalize(service) + "</b>   [<i>" + time + "</i>]<br /><br />"));
            text.append(message);
        } catch (JSONException e) {
        }
        return rowView;
    }
}
