package com.example.kleax;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;
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

        // Validación de campos vacíos
        if (nombre.isEmpty() || usuario.isEmpty() || email.isEmpty() || password.isEmpty() || confirmarPassword.isEmpty()) {
            Toast.makeText(this, "Por favor, llena todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validar si se aceptaron las condiciones
        if (!aceptarCondiciones.isChecked()) {
            Toast.makeText(this, "Debes aceptar las condiciones", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validación de seguridad de la contraseña
        if (!esPasswordSegura(password)) {
            Toast.makeText(this, "La contraseña debe tener al menos 8 caracteres, una mayúscula, un número y un carácter especial", Toast.LENGTH_LONG).show();
            return;
        }

        // Validar que las contraseñas coincidan
        if (!password.equals(confirmarPassword)) {
            Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
            return;
        }

        // Crear cuerpo de la solicitud
        Map<String, Object> body = new HashMap<>();
        body.put("email", email);
        body.put("username", usuario);
        body.put("name", nombre);
        body.put("password", password);
        body.put("passwordConfirm", password);

        // Llamar al endpoint de PocketBase para registrar al usuario
        PocketBaseApi api = ApiClient.getClient().create(PocketBaseApi.class);
        api.registerUser(body).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful()) {
                    // Imprimir la respuesta completa para ver qué contiene
                    Map<String, Object> responseBody = response.body();
                    Log.d("ResponseBody", "Respuesta del servidor: " + responseBody);

                    // Si el registro es exitoso, hacer login automáticamente con las credenciales recién registradas
                    if (responseBody != null && responseBody.containsKey("token")) {
                        String token = (String) responseBody.get("token");
                        guardarToken(token);
                        Intent intent = new Intent(registrarse.this, home.class);
                        startActivity(intent);
                        finish();
                    } else {
                        // Si no hay token en la respuesta, proceder con el inicio de sesión manual
                        iniciarSesionAutomatico(email, password);
                    }
                } else {
                    // Si la respuesta no fue exitosa, mostrar detalles del error
                    mostrarError(response);
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                // En caso de error en la conexión, mostrar mensaje
                Toast.makeText(registrarse.this, "Error de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Método para iniciar sesión automáticamente con las credenciales recién registradas
    private void iniciarSesionAutomatico(String email, String password) {
        // Cambiar el tipo de Map a Map<String, String>
        Map<String, String> loginBody = new HashMap<>();
        loginBody.put("email", email);
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


    private boolean esPasswordSegura(String password) {
        Log.d("PasswordValidation", "Contraseña: " + password);  // Ver la contraseña antes de validarla
        String regex = "^(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=.]).{8,}$";
        boolean isValid = Pattern.matches(regex, password);
        Log.d("PasswordValidation", "Es válida: " + isValid);  // Ver si la contraseña pasa la validación
        return isValid;
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

    private void mostrarError(Response<Map<String, Object>> response) {
        try {
            String errorResponse = response.errorBody().string();
            Log.e("PocketBase", "Error de registro: " + response.code() + " - " + errorResponse);
            // Aquí puedes hacer algo más con el error si es necesario
        } catch (Exception e) {
            Log.e("PocketBase", "Error al obtener el cuerpo de error: " + e.getMessage());
        }
        Toast.makeText(this, "Error en el registro, intenta de nuevo.", Toast.LENGTH_SHORT).show();
    }

}
