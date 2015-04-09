package com.vadimoe.nomnom;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import java.io.InputStream;

class ImageLoader extends AsyncTask<String, Void, Bitmap> {

    final static String TAG ="Image Loader";
    ImageView mImage;

    public ImageLoader(ImageView image) {
        this.mImage = image;
    }

    protected Bitmap doInBackground(String... urls) {
        String urldisplay = urls[0];
        Bitmap mIcon11 = null;
        try {
            InputStream in = new java.net.URL(urldisplay).openStream();
            mIcon11 = BitmapFactory.decodeStream(in);
        } catch (Exception e) {
            Log.i(TAG, "Exception: " + e.getMessage());
        }
        return mIcon11;
    }

    protected void onPostExecute(Bitmap result) {
        mImage.setImageBitmap(result);
    }
}