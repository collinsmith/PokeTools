package com.poketools;

import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.pokegoapi.api.PokemonGo;
import com.pokegoapi.api.player.PlayerProfile;
import com.pokegoapi.auth.GoogleUserCredentialProvider;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;

import net.openid.appauth.AuthorizationRequest;
import net.openid.appauth.AuthorizationService;

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
                    requestAuth();
                    break;
                default:
                    return;
            }

        } catch (Exception e) {
            //...
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        final EditText input = new EditText(LoginActivity.this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);

        new AlertDialog.Builder(this)
                .setView(input)
                .setTitle("Enter Provided Authorization Code:")
                .setPositiveButton("Done", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        login(input.getText().toString());
                    }
                })
                .show();
    }

    private void requestAuth() {
        new OAuth2AsyncTask().execute();
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

        //login(token);
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

    private class OAuth2AsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            return fetchToken();
        }

        private String fetchToken() {
            //Intent intent = new Intent(Intent.ACTION_VIEW);
            //intent.setData(Uri.parse(GoogleUserCredentialProvider.LOGIN_URL));
            //startActivityForResult(intent, 0);

            AuthorizationRequest authorizationRequest = new AuthorizationRequest.Builder(
                        PokeTools.OAUTH2_SERVICE_CONFIG,
                        PokeTools.OAUTH2_CLIENT_ID,
                        PokeTools.OAUTH2_RESPONSE_TYPE,
                        Uri.parse(PokeTools.OAUTH2_REDIRECT_URI))
                    .setScope(PokeTools.OAUTH2_SCOPE)
                    .build();

            Context context = LoginActivity.this;
            AuthorizationService service = new AuthorizationService(context);
            Intent postAuthIntent = new Intent(context, MainActivity.class);
            service.performAuthorizationRequest(
                    authorizationRequest,
                    PendingIntent.getActivity(
                            context, authorizationRequest.hashCode(), postAuthIntent, 0));

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

        private PokemonGo attemptLogin(final String authCode) {
            try {
                OkHttpClient client = new OkHttpClient();
                GoogleUserCredentialProvider credentialProvider
                        = new GoogleUserCredentialProvider(client);
                credentialProvider.login(authCode);
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
