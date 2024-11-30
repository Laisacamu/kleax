package com.example.kleax;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RestablecerContraseña extends AppCompatActivity {

    // Casilla de texto para el correo
    private EditText emailField;
    private Button botonRestablecer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restablecer_contrasena);

        // Inicializar elementos
        emailField = findViewById(R.id.caja_1);
        botonRestablecer = findViewById(R.id.button_restablecer);

        // Configurar el evento del botón para restablecer la contraseña
        botonRestablecer.setOnClickListener(v -> restablecerContraseña());
    }

    private void restablecerContraseña() {
        String email = emailField.getText().toString();

        // Validación de campo vacío
        if (email.isEmpty()) {
            Toast.makeText(this, "Por favor, ingresa tu correo", Toast.LENGTH_SHORT).show();
            return;
        }

        // Crear cuerpo de la solicitud
        Map<String, String> body = new HashMap<>();
        body.put("email", email);  // El campo que usa PocketBase para la identidad (correo)

        PocketBaseApi api = ApiClient.getClient().create(PocketBaseApi.class);
        api.requestPasswordReset(body).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(RestablecerContraseña.this, "Te hemos enviado un correo con instrucciones.", Toast.LENGTH_SHORT).show();
                } else {
                    Log.e("PocketBase", "Error en la solicitud de restablecimiento: " + response.code() + " - " + response.message());
                    Toast.makeText(RestablecerContraseña.this, "Hubo un problema al enviar el correo. Inténtalo de nuevo.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Log.e("PocketBase", "Error de conexión: " + t.getMessage());
                t.printStackTrace();
                Toast.makeText(RestablecerContraseña.this, "Error de conexión. Inténtalo más tarde.", Toast.LENGTH_SHORT).show();
            }
        });

    }

}
