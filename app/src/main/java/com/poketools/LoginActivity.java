package com.poketools;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.pokegoapi.api.PokemonGo;
import com.pokegoapi.auth.GoogleUserCredentialProvider;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;

import org.apache.commons.lang3.StringUtils;

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
        WebViewClient webViewClient = new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                String title = view.getTitle();
                if (!title.startsWith("Success ")) {
                    return;
                }

                String content = title.substring(8);
                Log.d(TAG, "response=" + content);
                Uri uri = Uri.parse("http://localhost?" + content);
                String authCode = uri.getQueryParameter("code");
                if (authCode == null) {
                    return;
                }

                Log.d(TAG, "got " + authCode);
                onAuthReceived(authCode);
            }
        };

        Uri uri = Uri.parse(PokeTools.Auth.OAUTH2_AUTH_ENDPOINT).buildUpon()
                .appendQueryParameter("client_id", PokeTools.Auth.OAUTH2_CLIENT_ID)
                .appendQueryParameter("redirect_uri", PokeTools.Auth.OAUTH2_REDIRECT_URI)
                .appendQueryParameter("response_type", PokeTools.Auth.OAUTH2_RESPONSE_TYPE)
                .appendQueryParameter("scope", StringUtils.join(PokeTools.Auth.OAUTH2_SCOPE, " "))
                .build();

        WebView webView = (WebView) findViewById(R.id.webview);
        webView.loadUrl(uri.toString());
        webView.setWebViewClient(webViewClient);
        webView.setVisibility(View.VISIBLE);
        webView.bringToFront();

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        //new OAuth2AsyncTask().execute();
    }

    private void onAuthReceived(String authCode) {
        login(authCode);
    }

    private void login(String authCode) {
        new LoginAsyncTask().execute(authCode);
    }

    private void onLogin(PokemonGo pogo) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
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
                Log.d(TAG, "attempting login with " + authCode + " ...");
                OkHttpClient client = new OkHttpClient();
                GoogleUserCredentialProvider credentialProvider
                        = new GoogleUserCredentialProvider(client);
                credentialProvider.login(authCode);

                /*
                Log.d(TAG, "authenticating with pogo servers...");
                PokemonGo pogo = new PokemonGo(credentialProvider, client);

                PlayerProfile profile = pogo.getPlayerProfile();
                PlayerDataOuterClass.PlayerData player = profile.getPlayerData();
                Log.d(TAG, "logged in as " + player.getUsername());

                return pogo;
                */
                return null;
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
