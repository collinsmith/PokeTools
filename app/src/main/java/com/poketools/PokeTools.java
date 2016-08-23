package com.poketools;

import android.net.Uri;

import com.pokegoapi.api.PokemonGo;
import com.pokegoapi.auth.CredentialProvider;
import com.pokegoapi.auth.GoogleUserCredentialProvider;

import net.openid.appauth.AuthorizationServiceConfiguration;

import okhttp3.OkHttpClient;

class PokeTools {

    static final String OAUTH2_AUTH_ENDPOINT = "https://accounts.google.com/o/oauth2/auth";
    static final String OAUTH2_TOKEN_ENDPOINT = "https://www.googleapis.com/oauth2/v4/token";
    static final String OAUTH2_CLIENT_ID = GoogleUserCredentialProvider.CLIENT_ID;
    static final String OAUTH2_RESPONSE_TYPE = "code";
    static final String OAUTH2_REDIRECT_URI = "com.poketools:/localhost";
    //static final String OAUTH2_REDIRECT_URI = "urn:ietf:wg:oauth:2.0:oob:auto";
    static final String OAUTH2_SCOPE = "openid email https://www.googleapis.com/auth/userinfo.email";

    static final AuthorizationServiceConfiguration OAUTH2_SERVICE_CONFIG = new AuthorizationServiceConfiguration(
            Uri.parse(OAUTH2_AUTH_ENDPOINT),
            Uri.parse(OAUTH2_TOKEN_ENDPOINT),
            null);

    static final OkHttpClient client = new OkHttpClient();
    static CredentialProvider credentialProvider;
    static PokemonGo pogo;

    private PokeTools() {
    }

    public static <T extends CredentialProvider> T getCredentialProvider() {
        return (T) credentialProvider;
    }

}
