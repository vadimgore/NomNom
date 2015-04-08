package com.vadimoe.nomnom;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

class HttpPoster extends AsyncTask<String, String, String> {

    final static String TAG = "HttpPoster";
    Context mContext;
    String mProgressMsg;
    ProgressDialog mProgressDialog;

    HttpPoster(Context aContext, String progDialogMsg) {
        mContext = aContext;
        mProgressMsg = progDialogMsg;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (mContext != null) {
            mProgressDialog = new ProgressDialog(mContext);
            mProgressDialog.setTitle("Nom Nom");
            mProgressDialog.setMessage(mProgressMsg);
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.show();
        }
    }

    @Override
    protected String doInBackground(String... params) {
        String urlString = params[0]; // URL to call

        StringBuilder result = new StringBuilder();
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(urlString);
        HttpResponse response;
        try {
            // Add your data
            List<NameValuePair> nameValuePairs = new ArrayList<>((params.length-1)/2);
            for (int i = 1; i < params.length; i+=2) {
                nameValuePairs.add(new BasicNameValuePair(params[i], params[i+1]));
            }

            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));
            // Execute HTTP Post Request
            response = httpclient.execute(httppost);
            StatusLine statusLine = response.getStatusLine();
            int statusCode = statusLine.getStatusCode();
            if (statusCode == 200) {
                HttpEntity entity = response.getEntity();
                InputStream content = entity.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(content));
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                Log.i(TAG, "Your data: " + result.toString()); //response data
            } else {
                Log.e(TAG, "Failed with error: " + statusLine.getReasonPhrase());
                result.append(statusLine.getReasonPhrase());
                //Toast.makeText(mContext, TAG + ":error: " + statusLine.getReasonPhrase(), Toast.LENGTH_LONG).show();
            }
        } catch (ClientProtocolException e) {
            System.out.println(e.getMessage());
            return e.getMessage();
        } catch (IOException e) {
            System.out.println(e.getMessage());
            return e.getMessage();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return e.getMessage();
        }

        return result.toString();
    }

    protected void onPostExecute(String result) {
        Log.i(TAG, "onPostExecute: " + result);
        if (mProgressDialog != null)
            mProgressDialog.dismiss();
        Toast.makeText(mContext, result, Toast.LENGTH_LONG).show();
    }
}