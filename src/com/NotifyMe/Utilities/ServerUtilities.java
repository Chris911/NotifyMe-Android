package com.NotifyMe.Utilities;

import android.util.Log;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Helper class used to communicate with the server.
 */
public final class ServerUtilities {

    static final String TAG = "Server Utilities";

    public static APIResponse post(String url, List<BasicNameValuePair> params) {
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(url);
        try {
            httppost.setEntity(new UrlEncodedFormEntity(params));
            // Execute HTTP Post Request
            Log.i(TAG, "POST Request: " + url);
            return new APIResponse(httpclient.execute(httppost));
        } catch (ClientProtocolException e) {
        } catch (IOException e) {
        }
        return null;
    }
}