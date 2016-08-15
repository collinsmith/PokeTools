package com.poketools;

import com.pokegoapi.api.PokemonGo;
import com.pokegoapi.auth.CredentialProvider;

import okhttp3.OkHttpClient;

class PokeTools {

    static final OkHttpClient client = new OkHttpClient();
    static CredentialProvider credentialProvider;
    static PokemonGo pogo;

    private PokeTools() {
    }

    public static <T extends CredentialProvider> T getCredentialProvider() {
        return (T) credentialProvider;
    }

}
