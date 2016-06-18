package com.example.abhishekjpr.newmovieproject;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class CustomAdapter extends BaseAdapter {
    private ArrayList<HoldMovieReviews> listData;
    private LayoutInflater layoutInflater;
    TextView otherDay, otherStatus, otherHigh, otherLow;
    ImageView otherImageView;

    public CustomAdapter(Context aContext, List<HoldMovieReviews> listData) {
        this.listData = (ArrayList) listData;
        layoutInflater = LayoutInflater.from(aContext);
    }

    @Override
    public int getCount() {
        return listData.size();
    }

    @Override
    public Object getItem(int position) {
        return listData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.review_textview, null);
            holder = new ViewHolder();
            holder.reviewAuthor = (TextView) convertView.findViewById(R.id.reviewListAuthorTextView);
            holder.reviewURL = (TextView) convertView.findViewById(R.id.reviewListReviewURLTextView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        try {
            holder.reviewAuthor.setText(listData.get(position).getAuthor());
            holder.reviewURL.setText("Click :: "+listData.get(position).getUrl());

        } catch (Exception e) {
        }
        return convertView;
    }

    static class ViewHolder {
        TextView reviewAuthor;
        TextView reviewURL;
    }
}