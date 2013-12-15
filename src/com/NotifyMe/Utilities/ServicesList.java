package com.NotifyMe.Utilities;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.NotifyMe.R;

public class ServicesList extends ArrayAdapter<String>{

    private final Activity context;
    private final String[] services;
    private final Integer[] imageId;
    public ServicesList(Activity context, String[] services, Integer[] imageId) {
        super(context, R.layout.service_row, services);
        this.context = context;
        this.services = services;
        this.imageId = imageId;

    }
    @Override
    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView= inflater.inflate(R.layout.service_row, null, true);
        TextView txtTitle = (TextView) rowView.findViewById(R.id.txt);

        ImageView imageView = (ImageView) rowView.findViewById(R.id.img);
        txtTitle.setText(services[position]);

        imageView.setImageResource(imageId[position]);
        return rowView;
    }
}