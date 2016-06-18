package com.example.abhishekjpr.newmovieproject;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;

public class DbBitmapUtility {

    // convert from bitmap to byte array
    public static byte[] getBytes(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream);
        return stream.toByteArray();
    }

    // convert from byte array to bitmap
    public static Bitmap getImage(byte[] image) {
        System.out.println("Image : "+image+" Length: "+image.length+"-->"+BitmapFactory.decodeByteArray(image, 0, image.length));
        return BitmapFactory.decodeByteArray(image, 0, image.length);
    }
}