package com.android.services.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.services.R;
import com.android.services.models.AppNameAndIcon;

import java.util.ArrayList;
import java.util.List;

public class AppIconAndNameSpinnerAdapter extends ArrayAdapter<AppNameAndIcon> {
    LayoutInflater inflater;
    List<AppNameAndIcon> objects;
    ViewHolder holder = null;

    public AppIconAndNameSpinnerAdapter(Context context, int textViewResourceId, List<AppNameAndIcon> objects) {
        super(context, textViewResourceId, objects);
        inflater = ((Activity) context).getLayoutInflater();
        this.objects = objects;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    public View getCustomView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = new ViewHolder();
        LayoutInflater inflater = ((Activity)getContext()).getLayoutInflater();
        View row = inflater.inflate(R.layout.app_icon_and_name_spinner_item, parent, false);
        holder.img = (ImageView)row.findViewById(R.id.spinner_img);
        holder.name = (TextView)row.findViewById(R.id.spinner_name);

        AppNameAndIcon item = objects.get(position);


        if(position == 0)
        {
            holder.img.setVisibility(View.GONE);
            holder.name.setText(item.getAppName());
        }
        else {
            holder.name.setText(item.getAppName());
            //holder.imgIcon.setImageResource(item.icon);
            //if (holder.imgIcon.getTag() == null ||  !holder.imgIcon.getTag().equals(item.icon)) {
            holder.img.setImageResource(item.getAppIcon());
            //}
        }
        return row;
    }


    static class ViewHolder {
        TextView name;
        ImageView img;
    }
}
