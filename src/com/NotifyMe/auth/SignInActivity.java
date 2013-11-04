package com.NotifyMe.auth;

/*
 * Copyright 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import com.NotifyMe.MainActivity;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import com.NotifyMe.R;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.GooglePlayServicesUtil;
import org.json.JSONException;
import org.json.JSONObject;

public class SignInActivity extends Activity {
    private static final String TAG = "SignInActivity";
    private static final String SCOPE = "oauth2:https://www.googleapis.com/auth/userinfo.profile https://www.googleapis.com/auth/userinfo.email";

    private AccountManager mAccountManager;

    private TextView mOut;

    private Spinner mAccountTypesSpinner;

    static final int REQUEST_CODE_RECOVER_FROM_AUTH_ERROR = 1001;
    static final int REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR = 1002;

    private String[] mNamesArray;
    private String mEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_in);

        mOut = (TextView) findViewById(R.id.message);
        mNamesArray = getAccountNames();
        mAccountTypesSpinner = initializeSpinner(
                R.id.sign_in_account_types_spinner, mNamesArray);

        initializeFetchButton();

        // Keep this if we want to start task from previous activity with intent extras
//        Bundle extras = getIntent().getExtras();
//        if (extras.containsKey(EXTRA_ACCOUNTNAME)) {
//            mEmail = extras.getString(EXTRA_ACCOUNTNAME);
//            mAccountTypesSpinner.setSelection(getIndex(mNamesArray, mEmail));
//            getTask(SignInActivity.this, mEmail, SCOPE, REQUEST_CODE_RECOVER_FROM_AUTH_ERROR)
//                    .execute();
//        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_RECOVER_FROM_AUTH_ERROR) {
            handleAuthorizeResult(resultCode, data);
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * This method is a hook for background threads and async tasks that need to update the UI.
     * It does this by launching a runnable under the UI thread.
     */
    public void show(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mOut.setText(message);
            }
        });
    }

    /**
     * This method is a hook for background threads and async tasks that need to launch a dialog.
     * It does this by launching a runnable under the UI thread.
     */
    public void showErrorDialog(final int code) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Dialog d = GooglePlayServicesUtil.getErrorDialog(
                        code,
                        SignInActivity.this,
                        REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR);
                d.show();
            }
        });
    }

    private void handleAuthorizeResult(int resultCode, Intent data) {
        if (data == null) {
            show("Unknown error, click the button again");
            return;
        }
        if (resultCode == RESULT_OK) {
            Log.i(TAG, "Retrying");
            getTask(this, mEmail, SCOPE, REQUEST_CODE_RECOVER_FROM_AUTH_ERROR).execute();
            return;
        }
        if (resultCode == RESULT_CANCELED) {
            show("User rejected authorization.");
            return;
        }
        show("Unknown error, click the button again");
    }

    private String[] getAccountNames() {
        mAccountManager = AccountManager.get(this);
        Account[] accounts = mAccountManager.getAccountsByType(GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);
        String[] names = new String[accounts.length];
        for (int i = 0; i < names.length; i++) {
            names[i] = accounts[i].name;
        }
        return names;
    }

    private Spinner initializeSpinner(int id, String[] values) {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(SignInActivity.this,
                android.R.layout.simple_spinner_item, values);
        Spinner spinner = (Spinner) findViewById(id);
        spinner.setAdapter(adapter);
        return spinner;
    }

    private void initializeFetchButton() {
        Button getToken = (Button) findViewById(R.id.sign_in_btn);
        getToken.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int accountIndex = mAccountTypesSpinner.getSelectedItemPosition();
                if (accountIndex < 0) {
                    // this happens when the sample is run in an emulator which has no google account
                    // added yet.
                    show("No account available. Please add an account to the phone first.");
                    return;
                }
                mEmail = mNamesArray[accountIndex];
                getTask(SignInActivity.this, mEmail, SCOPE, REQUEST_CODE_RECOVER_FROM_AUTH_ERROR).execute();
            }
        });
    }

    private AbstractGetInfoTask getTask(SignInActivity activity, String email, String scope, int requestCode) {
        return new GetInfoInForeground(activity, email, scope, requestCode);
    }

    /**
     * @return Application's {@code SharedPreferences}.
     */
    private SharedPreferences getGCMPreferences() {
        return getSharedPreferences(MainActivity.class.getSimpleName(), Context.MODE_PRIVATE);
    }

    public void saveProfile(JSONObject profile) throws JSONException {
        final SharedPreferences prefs = getGCMPreferences();

        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(MainActivity.PROPERTY_EMAIL, profile.getString("email"));
        editor.putString(MainActivity.PROPERTY_FULLNAME, profile.getString("name"));
        editor.putString(MainActivity.PROPERTY_GIVEN_NAME, profile.getString("given_name"));
        editor.putString(MainActivity.PROPERTY_FAMILY_NAME, profile.getString("family_name"));
        editor.putString(MainActivity.PROPERTY_GOOGLE_UID, profile.getString("id"));
        editor.commit();
    }

    public void finishActivity() {
        setResult(MainActivity.VALID_SIGN_IN_RESULT);
        finish();
    }
}