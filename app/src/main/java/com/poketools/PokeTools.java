package com.poketools;

import com.pokegoapi.api.PokemonGo;
import com.pokegoapi.auth.CredentialProvider;
import com.pokegoapi.auth.GoogleUserCredentialProvider;

import okhttp3.OkHttpClient;

class PokeTools {

    static class Auth {

        static final String OAUTH2_AUTH_ENDPOINT = "https://accounts.google.com/o/oauth2/auth";
        static final String OAUTH2_CLIENT_ID = GoogleUserCredentialProvider.CLIENT_ID;
        static final String OAUTH2_REDIRECT_URI = "urn:ietf:wg:oauth:2.0:oob:auto";
        static final String OAUTH2_RESPONSE_TYPE = "code";
        static final String[] OAUTH2_SCOPE = new String[] {
                "openid",
                "email",
                "https://www.googleapis.com/auth/userinfo.email"
        };

        static final String OAUTH2_TOKEN_ENDPOINT = "https://www.googleapis.com/oauth2/v4/token";

        private Auth() {
        }
    }

    static final OkHttpClient client = new OkHttpClient();
    static CredentialProvider credentialProvider;
    static PokemonGo pogo;

    private PokeTools() {
    }

    public static <T extends CredentialProvider> T getCredentialProvider() {
        return (T) credentialProvider;
    }

}
