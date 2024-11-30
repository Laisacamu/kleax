package com.example.kleax;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;
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

    // Endpoint para registrar un pago
    @POST("api/collections/pagos/records")
    Call<Map<String, Object>> registrarPago(@Body Map<String, Object> pagoData);

    // Endpoint para registrar una tarjeta
    @POST("/api/collections/tarjetas/records")
    Call<Map<String, Object>> registrarTarjeta(@Body Map<String, Object> tarjetaData);

    // Endpoint para obtener datos del usuario
    @GET("/api/collections/users/records")
    Call<Map<String, Object>> getUserData(@Header("Authorization") String token);

    @GET("/api/collections/users/records")
    Call<Map<String, Object>> getUserDatas(
            @Header("Authorization") String token,
            @Query("filter") String filter // Filtro agregado para buscar por user_id
    );


    // Nuevo endpoint para obtener tarjetas
    @GET("/api/collections/tarjetas/records")
    Call<Map<String, Object>> getTarjetas(@Header("Authorization") String token);

    @GET("/api/collections/carrito_productos/records")
    Call<Map<String, Object>> getCarritoProductos(
            @Header("Authorization") String token,
            @Query("filter") String filter // Cambiado para recibir el filtro completo
    );

    @GET("/api/collections/carrito/records")
    Call<Map<String, Object>>  getCarrito(
            @Header("Authorization") String token,
            @Query("filter") String filter);


    // Método para obtener los detalles de un producto por su ID
    @GET("/api/collections/productos/records/{id}")
    Call<Map<String, Object>> getProductoById(
            @Header("Authorization") String token,
            @Path("id") String productoId);


    // Endpoint para restablecer la contraseña
    @POST("/api/collections/users/request-password-reset")
    Call<Map<String, Object>> requestPasswordReset(@Body Map<String, String> body);
}
