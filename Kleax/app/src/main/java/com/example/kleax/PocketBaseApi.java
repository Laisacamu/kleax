package com.example.kleax;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.POST;

public interface PocketBaseApi {

    // Endpoint para iniciar sesión
    @POST("/api/collections/users/auth-with-password")
    Call<Map<String, Object>> loginUser(@Body Map<String, String> body);

    // Endpoint para registrar un nuevo usuario
    @POST("/api/collections/users/records")
    Call<Map<String, Object>> registerUser(@Body Map<String, Object> body);

    // Endpoint para verificar si un correo ya está registrado
    @GET("/api/collections/users/records")
    Call<Map<String, Object>> checkEmailExists(@Query("filter") String filter);
}
