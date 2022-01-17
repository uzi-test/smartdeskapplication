package com.smartdesk.services;

import com.smartdesk.constants.FirebaseConstants;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RestClient {

    private static Service service;

    static {
        setupRestClient();
    }

    private RestClient() {
    }

    public static Service get() {
        return service;
    }

    public static void setupRestClient() {
        System.out.println("SERVICEAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
        if (service == null) {
            System.out.println("SERVICE         NULLLLLLLLLLLLLLLLLL");
            Gson gson = new GsonBuilder()
                    .setLenient()
                    .create();
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(FirebaseConstants.Firebase_BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
            service = retrofit.create(Service.class);
        }
    }
}

