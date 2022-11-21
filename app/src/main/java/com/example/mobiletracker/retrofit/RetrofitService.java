package com.example.mobiletracker.retrofit;

import com.google.gson.Gson;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

// Retrofit configuration class

public class RetrofitService {

    private Retrofit retrofit;

    public RetrofitService() {
        initRetrofit();
    }

    // Current wifi IP address to get the URL of the server
    // Need JSON so will be using the JSON library
    private void initRetrofit() {
        retrofit = new Retrofit.Builder()
                .baseUrl("http://10.122.5.90:8080/")
//                .baseUrl("http://10.241.232.138:8080/")
                .addConverterFactory(GsonConverterFactory.create(new Gson()))
                .build();
    }

    // Get Retrofit service
    public Retrofit getRetrofit() {
        return retrofit;
    }
}
