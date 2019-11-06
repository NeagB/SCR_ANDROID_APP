package edu.utcluj.gpsm.RestClient.retrofit.client;

import edu.utcluj.gpsm.RestClient.ResPosition;
import edu.utcluj.gpsm.RestClient.ResUser;
import edu.utcluj.gpsm.RestClient.dto.PositionDTO;
import edu.utcluj.gpsm.RestClient.dto.UserLoginDTO;
import edu.utcluj.gpsm.RestClient.dto.UserRegisterDTO;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface UserService {

    @POST("user/login")
    Call<ResUser> login(@Body UserLoginDTO userLoginDTO, @Header("Authorization") String credentials);

    @POST("user/register")
    Call<ResUser> register(@Body UserRegisterDTO userRegisterDTO);

    @POST("position/addPos")
    Call<ResPosition> addPostion(@Body PositionDTO positionDTO, @Header("Authorization") String credentials);


}
