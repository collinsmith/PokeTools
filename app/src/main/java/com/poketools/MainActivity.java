package com.poketools;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import net.openid.appauth.AuthorizationException;
import net.openid.appauth.AuthorizationResponse;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AuthorizationResponse authorizationResponse = AuthorizationResponse.fromIntent(getIntent());
        AuthorizationException exception = AuthorizationException.fromIntent(getIntent());
        if (authorizationResponse != null) {
            // authorization succeeded
            Log.d(TAG, "authorizationCode=" + authorizationResponse.authorizationCode);
        } else {
            // authorization failed, check ex for more details
            Log.e(TAG, exception.getLocalizedMessage(), exception);
        }

        setContentView(R.layout.main);
    }

}
