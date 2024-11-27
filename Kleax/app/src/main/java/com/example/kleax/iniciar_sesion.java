package com.example.kleax;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class iniciar_sesion extends AppCompatActivity {

    // Casillas de texto
    private EditText emailField;
    private EditText passwordField;

    // Botón Iniciar Sesión
    private Button botonIniciarSesion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_iniciar_sesion);

        // Inicializar elementos
        emailField = findViewById(R.id.caja_1);
        passwordField = findViewById(R.id.caja_2); // ID correcto para la contraseña
        botonIniciarSesion = findViewById(R.id.button_registrarse3);

        botonIniciarSesion.setOnClickListener(v -> iniciarSesion());

        // Texto para ir a la pantalla de registro
        TextView registrese = findViewById(R.id.reg_strese);
        registrese.setOnClickListener(v -> {
            Intent intent = new Intent(iniciar_sesion.this, registrarse.class);
            startActivity(intent);
        });
    }

    private void iniciarSesion() {
        String email = emailField.getText().toString();
        String password = passwordField.getText().toString();

        // Validación de campos vacíos
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor, llena todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        // Crear cuerpo de la solicitud
        Map<String, String> body = new HashMap<>();
        body.put("identity", email);
        body.put("password", password);

        // Llamar al endpoint de PocketBase para iniciar sesión
        Log.d("PocketBase", "Iniciando sesión con email: " + email);
        PocketBaseApi api = ApiClient.getClient().create(PocketBaseApi.class);
        Log.d("ApiClient", "Retrofit creado correctamente");
        api.loginUser(body).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful()) {
                    Map<String, Object> responseBody = response.body();
                    if (responseBody != null && responseBody.containsKey("token")) {
                        String token = (String) responseBody.get("token");
                        guardarToken(token);
                        Intent intent = new Intent(iniciar_sesion.this, home.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(iniciar_sesion.this, "Error al obtener el token", Toast.LENGTH_SHORT).show();
                        Log.e("PocketBase", "Token no recibido en la respuesta.");
                    }
                } else {
                    // Log de la respuesta completa para ver detalles adicionales del error
                    try {
                        String errorResponse = response.errorBody().string();
                        Log.e("PocketBase", "Error de autenticación: " + response.code() + " - " + errorResponse);
                    } catch (Exception e) {
                        Log.e("PocketBase", "Error al obtener el cuerpo de error: " + e.getMessage());
                    }
                    Toast.makeText(iniciar_sesion.this, "Credenciales incorrectas", Toast.LENGTH_SHORT).show();
                }
            }


            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Log.e("PocketBase", "Error al intentar iniciar sesión: " + t.getMessage());
                t.printStackTrace();  // Esto imprimirá más detalles sobre el error.
                Toast.makeText(iniciar_sesion.this, "Error de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Método para almacenar el token en SharedPreferences (puedes usar otro método de almacenamiento)
    private void guardarToken(String token) {
        // Usa SharedPreferences o algún otro método seguro para almacenar el token
        getSharedPreferences("user_prefs", MODE_PRIVATE)
                .edit()
                .putString("auth_token", token)
                .apply();
        Log.d("PocketBase", "Token guardado exitosamente.");
    }
}
