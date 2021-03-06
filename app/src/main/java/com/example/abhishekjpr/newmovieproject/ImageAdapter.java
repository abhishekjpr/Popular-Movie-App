package com.example.abhishekjpr.newmovieproject;


import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;


public class ImageAdapter extends BaseAdapter {
    private ArrayList<Bitmap> listData;
    Context mContext;

    // Constructor
    public ImageAdapter(Context aContext, List<Bitmap> listData) {
        this.listData = (ArrayList)listData;
        mContext = aContext;
    }

    public int getCount() {
        return listData.size();
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            imageView = new ImageView(mContext);
            imageView.setLayoutParams(new GridView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }
        else
        {
            imageView = (ImageView) convertView;
        }

        imageView.setImageBitmap(listData.get(position));
        return imageView;
    }
}