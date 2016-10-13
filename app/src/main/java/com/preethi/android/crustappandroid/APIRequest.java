package com.preethi.android.crustappandroid;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public abstract class APIRequest extends AsyncTask<Void, Void, String> {
    public static String ERROR = "error";

    protected abstract String getUrl();
    protected abstract APIResponseHandler getResponseHandler();

    protected void onPreExecute() {
    }

    protected String doInBackground(Void... urls) {
        try {
            URL url = new URL(getUrl());
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            try {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }
                bufferedReader.close();
                return stringBuilder.toString();
            }
            finally{
                urlConnection.disconnect();
            }
        }
        catch(Exception e) {
            Log.e("ERROR", e.getMessage(), e);
            return null;
        }
    }

    protected void onPostExecute(String response) {
        if (response == null) {
            response = ERROR;
        }
        getResponseHandler().handleResponse(response);
    }

    public interface APIResponseHandler {
        public void handleResponse(String response);
    }
}
