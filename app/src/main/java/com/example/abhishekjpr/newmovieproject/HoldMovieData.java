package com.example.abhishekjpr.newmovieproject;


import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Abhishek on 06-Jun-16.
 */
public class HoldMovieData implements Parcelable {
    String pPath, id;

    public HoldMovieData(){

    }

    protected HoldMovieData(Parcel in) {
        pPath = in.readString();
        id = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(pPath);
        dest.writeString(id);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<HoldMovieData> CREATOR = new Creator<HoldMovieData>() {
        @Override
        public HoldMovieData createFromParcel(Parcel in) {
            return new HoldMovieData(in);
        }

        @Override
        public HoldMovieData[] newArray(int size) {
            return new HoldMovieData[size];
        }
    };

    public void setPosterPath(String path){
        pPath = path;
    }
    public String getPosterPath(){
        return pPath;
    }
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
}