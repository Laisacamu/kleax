package com.example.kleax;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.content.SharedPreferences;


import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class registrarse extends AppCompatActivity {

    private EditText nombreField;
    private EditText usuarioField;
    private EditText emailField;
    private EditText passwordField;
    private EditText confirmarPasswordField;
    private CheckBox aceptarCondiciones;
    private Button botonCrearCuenta;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrarse);

        // Inicializar elementos
        nombreField = findViewById(R.id.ingresa_Nombre);
        usuarioField = findViewById(R.id.ingresa_Usuario);
        emailField = findViewById(R.id.ingresa_Email);
        passwordField = findViewById(R.id.ingresa_Contraseña);
        confirmarPasswordField = findViewById(R.id.ingresa_ConfirmarContraseña);
        aceptarCondiciones = findViewById(R.id.acepto_Condiciones);
        botonCrearCuenta = findViewById(R.id.button_crear_cuenta);

        botonCrearCuenta.setOnClickListener(v -> registrarUsuario());

        TextView textIniciarSesion = findViewById(R.id.iniciar_sesion);
        textIniciarSesion.setOnClickListener(v -> {
            Intent intent = new Intent(registrarse.this, iniciar_sesion.class);
            startActivity(intent);
        });
    }

    private void registrarUsuario() {
        String nombre = nombreField.getText().toString().trim();
        String usuario = usuarioField.getText().toString().trim();
        String email = emailField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();
        String confirmarPassword = confirmarPasswordField.getText().toString().trim();

        // Validaciones de los campos
        if (nombre.isEmpty() || usuario.isEmpty() || email.isEmpty() || password.isEmpty() || confirmarPassword.isEmpty()) {
            Toast.makeText(this, "Por favor, llena todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!aceptarCondiciones.isChecked()) {
            Toast.makeText(this, "Debes aceptar las condiciones", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!esPasswordSegura(password)) {
            Toast.makeText(this, "La contraseña debe tener al menos 8 caracteres, una mayúscula, un número y un carácter especial", Toast.LENGTH_LONG).show();
            return;
        }

        if (!esUsernameValido(usuario)) {
            Toast.makeText(this, "El nombre de usuario debe tener entre 3 y 20 caracteres, y solo puede contener letras, números, puntos o guiones bajos.", Toast.LENGTH_LONG).show();
            return;
        }

        if (!password.equals(confirmarPassword)) {
            Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Por favor ingresa un correo electrónico válido", Toast.LENGTH_SHORT).show();
            return;
        }

        // Verificar si el correo ya está registrado
        verificarCorreoExistente(email);
    }

    private void verificarCorreoExistente(String email) {
        // Asegurarnos de que el filtro esté correctamente formateado
        String filtro = "email=\"" + email + "\""; // Usamos comillas para envolver el valor del correo

        PocketBaseApi api = ApiClient.getClient().create(PocketBaseApi.class);
        api.checkEmailExists(filtro).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful()) {
                    Map<String, Object> responseBody = response.body();
                    Log.d("Registro", "Respuesta del servidor: " + responseBody.toString());
                    if (responseBody != null && responseBody.containsKey("records") && !((List<?>) responseBody.get("records")).isEmpty()) {
                        // El correo ya está registrado
                        Toast.makeText(registrarse.this, "Este correo electrónico ya está registrado", Toast.LENGTH_SHORT).show();
                    } else {
                        // El correo no está registrado, proceder al registro del usuario
                        registrarUsuarioEnBaseDeDatos();
                    }
                } else {
                    // Aquí tratamos el error correctamente
                    Log.e("Registro", "Error en la respuesta: " + response.message());
                    Toast.makeText(registrarse.this, "Error al verificar el correo", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                // Muestra el error de conexión con más detalles para depurar
                Toast.makeText(registrarse.this, "Error de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("Registro", "Error de conexión: " + t.getMessage());
            }

        });
    }


    // Método para registrar el usuario
    private void registrarUsuarioEnBaseDeDatos() {
        String nombre = nombreField.getText().toString().trim();
        String usuario = usuarioField.getText().toString().trim();
        String email = emailField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();

        // Crear cuerpo de la solicitud con los campos requeridos
        Map<String, Object> body = new HashMap<>();
        body.put("email", email);
        body.put("username", usuario);
        body.put("name", nombre);
        body.put("password", password);
        body.put("passwordConfirm", password);

        Log.d("Registro", "Cuerpo de la solicitud: " + body.toString());

        // Llamada al endpoint para registrar al usuario
        PocketBaseApi api = ApiClient.getClient().create(PocketBaseApi.class);
        api.registerUser(body).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful()) {
                    // El registro fue exitoso, proceder al inicio de sesión
                    iniciarSesionAutomatico(email, password);
                } else {
                    mostrarError(response);
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Toast.makeText(registrarse.this, "Error de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Método para iniciar sesión automáticamente después del registro
    private void iniciarSesionAutomatico(String email, String password) {
        Map<String, String> loginBody = new HashMap<>();
        loginBody.put("identity", email);
        loginBody.put("password", password);

        PocketBaseApi api = ApiClient.getClient().create(PocketBaseApi.class);
        api.loginUser(loginBody).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful()) {
                    Map<String, Object> responseBody = response.body();
                    if (responseBody != null && responseBody.containsKey("token")) {
                        String token = (String) responseBody.get("token");
                        guardarToken(token);
                        Intent intent = new Intent(registrarse.this, home.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(registrarse.this, "Error al obtener el token", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    mostrarError(response);
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Toast.makeText(registrarse.this, "Error de conexión al iniciar sesión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Validación de formato de correo
    private boolean esUsernameValido(String username) {
        String regex = "^[a-zA-Z0-9._]{3,20}$";
        return Pattern.matches(regex, username);
    }

    // Validación de la contraseña
    private boolean esPasswordSegura(String password) {
        String regex = "^(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=.]).{8,}$";
        return Pattern.matches(regex, password);
    }

    // Método para guardar el token de autenticación
    private void guardarToken(String token) {
        SharedPreferences preferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        preferences.edit().putString("auth_token", token).apply();
        Log.d("PocketBase", "Token guardado exitosamente.");
    }

    private void mostrarError(Response<Map<String, Object>> response) {
        try {
            String errorResponse = response.errorBody().string();
            Log.e("PocketBase", "Error de registro: " + response.code() + " - " + errorResponse);

            if (response.code() == 400) {
                // Comprobamos si el error está relacionado con el username
                if (errorResponse.contains("username")) {
                    Toast.makeText(registrarse.this, "El nombre de usuario es inválido o ya está en uso", Toast.LENGTH_SHORT).show();
                } else if (errorResponse.contains("email")) {
                    Toast.makeText(registrarse.this, "Correo electrónico ya registrado", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(registrarse.this, "Error al registrar al usuario", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(registrarse.this, "Error al registrar al usuario", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("PocketBase", "Error al obtener el mensaje de error", e);
        }
    }

}
