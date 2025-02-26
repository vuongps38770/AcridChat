package com.example.acrid.Helper;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
public class NotificationAPI {

    private Retrofit retrofit;
    private final String BASE_URL= "https://serveradmin-e7vg.onrender.com/";


    public NotificationAPI() {
        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }
    public <T>  T createRetrofitClass(Class<T> service){
        return retrofit.create(service);
    }

}
