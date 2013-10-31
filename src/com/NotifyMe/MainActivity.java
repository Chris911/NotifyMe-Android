package com.NotifyMe;

import android.app.Dialog;
import android.content.Intent;
import android.widget.EditText;
import com.NotifyMe.auth.SignInActivity;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;


public class MainActivity extends Activity {
    public static final String PROPERTY_REG_ID = "registration_id";
    public static final String PROPERTY_APP_VERSION = "appVersion";
    public static final String PROPERTY_GOOGLE_UID = "google_uid";
    public static final String PROPERTY_FULLNAME = "fullname";
    public static final String PROPERTY_EMAIL = "email";
    public static final String PROPERTY_GIVEN_NAME = "given_name";
    public static final String PROPERTY_FAMILY_NAME = "family_name";

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final int REQUEST_CODE_RECOVER_FROM_AUTH_ERROR = 1001;
    private static final int REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR = 1002;

    private final static String NOTIFYME_API_URL = "http://notifyme.cloudapp.net/api/";
    private final static String DEVICE_ENDPOINT  = "device/register";

    /**
     * Google Project ID for API access to GCM
     */
    String SENDER_ID = "339572350083";

    /**
     * Tag used on log messages.
     */
    static final String TAG = "NotifyMe GCM";

    TextView mDisplay;
    GoogleCloudMessaging gcm;
    AtomicInteger msgId = new AtomicInteger();
    Context context;

    String regId;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);
        mDisplay = (TextView) findViewById(R.id.display);

        context = getApplicationContext();
    }

    private void registerDevice() {
        if(getUserGoogleUID().isEmpty()) {
            mDisplay.append("You need to sign in first\n");
        } else {
            // Check device for Play Services APK. If check succeeds, proceed with GCM registration.
            if (checkPlayServices()) {
                gcm = GoogleCloudMessaging.getInstance(this);
                regId = getRegistrationId(context);

                if (regId.isEmpty()) {
                    registerInBackground();
                }
            } else {
                Log.i(TAG, "No valid Google Play Services APK found.");
            }
        }
    }

    // You need to do the Play Services APK check here too.
    @Override
    protected void onResume() {
        super.onResume();
        checkPlayServices();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    /**
     * Registers the application with GCM servers asynchronously.
     * <p>
     * Stores the registration ID and the app versionCode in the application's
     * shared preferences.
     */
    private void registerInBackground() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }
                    regId = gcm.register(SENDER_ID);
                    msg = "Device registered, registration ID=" + regId;

                    // Send Reg ID to backend so we can use it later to send messages
                    sendRegistrationIdToBackend();

                    // Persist the regID - no need to register again.
                    storeRegistrationId(context, regId);
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                mDisplay.append(msg + "\n");
            }
        }.execute(null, null, null);
    }

    // Send an upstream message.
    public void onClick(final View view) {

        if (view == findViewById(R.id.sign_in_google)) {
            String fullname = getUserFullname();
            if(fullname.isEmpty()) {
                // Sign in with Google
                Intent intent = new Intent(this, SignInActivity.class);
                startActivity(intent);
            } else {
                mDisplay.append("Already signed in as " + fullname + "\n");
            }

        } else if (view == findViewById(R.id.register)) {
            if(getUserGoogleUID().isEmpty()) {
                mDisplay.append("You need to sign in first\n");
            } else {
                new AsyncTask<Void, Void, String>() {
                    @Override
                    protected String doInBackground(Void... params) {
                        registerDevice();
                        return "Registering device in background.";
                    }

                    @Override
                    protected void onPostExecute(String msg) {
                        mDisplay.append(msg + "\n");
                        Log.i(TAG, "New regId: " + regId);
                    }
                }.execute(null, null, null);
            }

        } else if (view == findViewById(R.id.clear)) {
            mDisplay.setText("");
        }
    }

    /**
     * Gets the current registration ID for application on GCM service.
     * <p>
     * If result is empty, the app needs to register.
     *
     * @return registration ID, or empty string if there is no existing
     *         registration ID.
     */
    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGCMPreferences();
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.i(TAG, "Registration not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i(TAG, "App version changed.");
            return "";
        }
        return registrationId;
    }

    private String getUserFullname() {
        final SharedPreferences prefs = getGCMPreferences();
        String userFullname = prefs.getString(PROPERTY_FULLNAME, "");
        if (userFullname.isEmpty()) {
            return "";
        }
        return userFullname;
    }

    private String getUserGoogleUID() {
        final SharedPreferences prefs = getGCMPreferences();
        String uid = prefs.getString(PROPERTY_GOOGLE_UID, "");
        if (uid.isEmpty()) {
            return "";
        }
        return uid;
    }

    /**
     * Stores the registration ID and app versionCode in the application's
     * {@code SharedPreferences}.
     *
     * @param context application's context.
     * @param regId registration ID
     */
    private void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getGCMPreferences();
        int appVersion = getAppVersion(context);
        Log.i(TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }

    /**
     * @return Application's {@code SharedPreferences}.
     */
    private SharedPreferences getGCMPreferences() {
        return getSharedPreferences(MainActivity.class.getSimpleName(),
                Context.MODE_PRIVATE);
    }

    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    /**
     * Sends the registration ID to your server over HTTP, so it can use GCM/HTTP
     * or CCS to send messages to your app. Not needed for this demo since the
     * device sends upstream messages to a server that echoes back the message
     * using the 'from' address in the message.
     */
    private void sendRegistrationIdToBackend() {
        Map<String, String> params = new HashMap<String, String>();
        params.put("regId", regId);
        params.put("uid", getUserGoogleUID());
        params.put("device_type", "android");
        try {
            ServerUtilities.post(NOTIFYME_API_URL+DEVICE_ENDPOINT, params);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
