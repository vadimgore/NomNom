package com.vadimoe.nomnom;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

class HttpGetter extends AsyncTask<String, String, String> {

    Context mContext;
    String mProgressMsg;
    AsyncTaskListener mListener;
    ProgressDialog mProgressDialog;

    public HttpGetter() {
        mContext = null;
        mProgressDialog = null;
    }

    public HttpGetter(Context context, AsyncTaskListener listener, String progressMsg) {
        mContext = context;
        mListener = listener;
        mProgressMsg = progressMsg;
        mProgressDialog = new ProgressDialog(mContext);
        mProgressDialog.setTitle("Nom Nom");
        mProgressDialog.setMessage(mProgressMsg);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mProgressDialog.show();
    }

    @Override
    protected String doInBackground(String... params) {
        // TODO Auto-generated method stub
        StringBuilder result = new StringBuilder();
        HttpClient client = new DefaultHttpClient();

        // Build URL string
        StringBuilder urlString = new StringBuilder();
        urlString.append(params[0]);
        for (int i=1; i<params.length; i++) {
            if (i==1) urlString.append("?");
            else urlString.append("&");

            urlString.append(params[i]);
        }

        HttpGet httpGet = new HttpGet(urlString.toString());

        try {
            HttpResponse response = client.execute(httpGet);
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
                Log.i("Getter", "Your data: " + result.toString()); //response data
            } else {
                Log.e("Getter", "Failed with error: " + statusLine.getReasonPhrase());
                result.append(statusLine.getReasonPhrase());
            }
        } catch (ClientProtocolException e) {
            result.append(e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            result.append(e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            result.append(e.getMessage());
            e.printStackTrace();
        }

        return result.toString();
    }

    protected void onPostExecute(String result) {
        super.onPreExecute();
        Log.i("onPostExecute: ", result);

        mListener.onTaskComplete(result);

        if (mProgressDialog != null && mProgressDialog.isShowing())
            mProgressDialog.dismiss();
    }
}

