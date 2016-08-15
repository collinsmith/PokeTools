package com.poketools;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.AccountPicker;

import android.accounts.AccountManager;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.pokegoapi.api.PokemonGo;
import com.pokegoapi.api.player.PlayerProfile;
import com.pokegoapi.auth.CredentialProvider;
import com.pokegoapi.auth.GoogleUserCredentialProvider;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;

import java.io.IOException;
import java.util.Arrays;

import POGOProtos.Data.PlayerDataOuterClass;
import okhttp3.OkHttpClient;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private static class Keys {
        static final String refreshToken = "refreshToken";

        private Keys() {
            //...
        }
    }

    private static final String TAG = LoginActivity.class.getSimpleName();

    private Button btnLoginWithGoogle;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        btnLoginWithGoogle = (Button) findViewById(R.id.btnLoginWithGoogle);
        btnLoginWithGoogle.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        try {
            switch (v.getId()) {
                case R.id.btnLoginWithGoogle:
                    selectAccount();
                    break;
                default:
                    return;
            }

        } catch (Exception e) {
            //...
        }
    }

    private void selectAccount() {
        Intent intent = AccountPicker.newChooseAccountIntent(
                null, null, new String[]{ "com.google" }, false, null, null, null, null);
        startActivityForResult(intent, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode) {
            case RESULT_OK:
                Log.d(TAG, Arrays.toString(data.getExtras().keySet().toArray()));
                String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                Log.d(TAG, "accountName=" + accountName);
                fetchAuth(accountName);
                break;
            case RESULT_CANCELED:
                Toast.makeText(this, R.string.pick_account, Toast.LENGTH_LONG).show();
                break;
            default:
                throw new IllegalStateException();
        }
    }

    private void fetchAuth(String accountName) {
        new FetchTokenAsyncTask().execute(accountName);
    }

    private void onAuthReceived(String token) {
        /*OkHttpClient client = new OkHttpClient();
        GoogleUserCredentialProvider provider;
        try {
            provider = new GoogleUserCredentialProvider(client, token);
        } catch (LoginFailedException e) {
            Log.e(TAG, e.getLocalizedMessage(), e);
            Toast.makeText(this, R.string.login_failed, Toast.LENGTH_LONG).show();
            return;
        } catch (RemoteServerException e) {
            Log.e(TAG, e.getLocalizedMessage(), e);
            Toast.makeText(this, R.string.login_failed, Toast.LENGTH_LONG).show();
            return;
        }*/

        login(token);
    }

    private void login(String token) {
        new LoginAsyncTask().execute(token);
    }

    private void onLogin(PokemonGo pogo) {
        //Intent intent = new Intent(this, MainActivity.class);
        //startActivity(intent);
    }

    private void onLoginFailed(Throwable t) {
        Log.e(TAG, t.getLocalizedMessage(), t);
    }

    private class FetchTokenAsyncTask extends AsyncTask<String, Void, String> {
        private static final String SCOPE
                = "oauth2:https://www.googleapis.com/auth/userinfo.email";

        @Override
        protected String doInBackground(String... params) {
            if (params == null) {
                throw new IllegalArgumentException("params cannot be null");
            } else if (params.length != 1) {
                throw new IllegalArgumentException(
                        "params should only contain 1 item (accountName)");
            }

            return fetchToken(params[0]);
        }

        private String fetchToken(String accountName) {
            try {
                String token = GoogleAuthUtil.getToken(LoginActivity.this, accountName, SCOPE);
                Log.d(TAG, "token=" + token);
                return token;
            } catch (IOException e) {
                Log.e(TAG, e.getLocalizedMessage(), e);
            } catch (UserRecoverableAuthException e) {
                Log.e(TAG, e.getLocalizedMessage(), e);
                startActivityForResult(e.getIntent(), 0);
            } catch (GoogleAuthException e) {
                Log.e(TAG, e.getLocalizedMessage(), e);
            }

            return null;
        }

        @Override
        protected void onPostExecute(String token) {
            super.onPostExecute(token);
            LoginActivity.this.onAuthReceived(token);
        }

    }

    private class LoginAsyncTask extends AsyncTask<String, Void, PokemonGo> {
        @Override
        protected PokemonGo doInBackground(String... params) {
            if (params == null) {
                throw new IllegalArgumentException("params cannot be null");
            } else if (params.length != 1) {
                throw new IllegalArgumentException(
                        "params should only contain 1 item (token)");
            }

            return attemptLogin(params[0]);
        }

        private PokemonGo attemptLogin(final String token) {
            try {
                OkHttpClient client = new OkHttpClient();
                CredentialProvider credentialProvider
                        = new GoogleUserCredentialProvider(client);
                PokemonGo pogo = new PokemonGo(credentialProvider, client);

                PlayerProfile profile = pogo.getPlayerProfile();
                PlayerDataOuterClass.PlayerData player = profile.getPlayerData();
                Log.d(TAG, "username=" + player.getUsername());

                return pogo;
            } catch (LoginFailedException e) {
                onLoginFailed(e);
                return null;
            } catch (RemoteServerException e) {
                onLoginFailed(e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(PokemonGo pogo) {
            super.onPostExecute(pogo);
            LoginActivity.this.onLogin(pogo);
        }

    }

}
