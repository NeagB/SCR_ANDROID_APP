package edu.utcluj.gpsm.RestClient.retrofit.client;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static UserService apiService;

    public RetrofitClient(String url){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(UserService.class);
    }

    public  UserService getUserService(){
        return apiService;
    }
}
