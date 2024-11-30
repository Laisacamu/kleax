package com.example.kleax;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class agregar_tarjeta extends AppCompatActivity {

    private PocketBaseApi apiService;
    private EditText etNumeroTarjeta, etFechaVencimiento, etCVV, etTitular;
    private Button btnGuardarTarjeta;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agregar_tarjeta);

        // Inicializar el servicio API
        apiService = ApiClient.getClient().create(PocketBaseApi.class);

        etNumeroTarjeta = findViewById(R.id.numero_tarjeta);
        etFechaVencimiento = findViewById(R.id.fecha_expiracion);
        etCVV = findViewById(R.id.cvc);
        etTitular = findViewById(R.id.titular);
        btnGuardarTarjeta = findViewById(R.id.btn_guardar_tarjeta);

        btnGuardarTarjeta.setOnClickListener(v -> {
            Log.d("Button", "Botón presionado");
            guardarTarjeta();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Cargar método de pago al reanudar la actividad
        SharedPreferences preferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String metodoPago = preferences.getString("metodo_pago", "Credito");
        Log.d("MetodoPago", "Método de pago recuperado al reanudar la actividad: " + metodoPago);
    }

    private void guardarTarjeta() {
        Log.d("GuardarTarjeta", "Iniciando validación de campos");

        // Obtener los datos de la tarjeta
        String numeroTarjeta = etNumeroTarjeta.getText().toString().trim();
        String fechaVencimiento = etFechaVencimiento.getText().toString().trim();
        String cvv = etCVV.getText().toString().trim();
        String titular = etTitular.getText().toString().trim();
        Log.d("FechaVencimiento", "Fecha antes de guardar: " + fechaVencimiento);

        // Validar si la fecha tiene el formato MM/yyyy
        if (!fechaVencimiento.matches("^(0[1-9]|1[0-2])/\\d{4}$")) {
            Log.e("Validación", "Formato de fecha incorrecto");
            Toast.makeText(this, "Formato de fecha inválido. Usa MM/yyyy", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validar los campos
        if (numeroTarjeta.isEmpty() || fechaVencimiento.isEmpty() || cvv.isEmpty() || titular.isEmpty()) {
            Log.e("Validación", "Faltan campos por completar");
            Toast.makeText(this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validar el número de tarjeta
        if (!numeroTarjeta.matches("\\d{16}")) {
            Log.e("Validación", "Número de tarjeta inválido");
            Toast.makeText(this, "El número de tarjeta debe tener 16 dígitos", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validar el CVV
        if (!cvv.matches("\\d{3}")) {
            Log.e("Validación", "CVV inválido");
            Toast.makeText(this, "El CVV debe tener 3 dígitos", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validar la fecha de vencimiento
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/yyyy", Locale.getDefault());
        try {
            Date parsedDate = dateFormat.parse(fechaVencimiento);
            if (parsedDate == null) {
                Log.e("Validación", "La fecha de vencimiento es inválida");
                Toast.makeText(this, "La tarjeta tiene una fecha de vencimiento incorrecta", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validar si la fecha de vencimiento es en el pasado
            Calendar currentDate = Calendar.getInstance();
            Calendar expiryDate = Calendar.getInstance();
            expiryDate.setTime(parsedDate);

            if (expiryDate.before(currentDate)) {
                Log.e("Validación", "La tarjeta ya está vencida");
                Toast.makeText(this, "La tarjeta ya está vencida", Toast.LENGTH_SHORT).show();
                return;
            }

        } catch (ParseException e) {
            Log.e("Validación", "Formato de fecha inválido", e);
            Toast.makeText(this, "Formato de fecha inválido. Usa MM/yyyy", Toast.LENGTH_SHORT).show();
        }

        Log.d("GuardarTarjeta", "Campos validados correctamente");
        verificarYRegistrarTarjeta(numeroTarjeta, fechaVencimiento, cvv, titular);
    }



    private void verificarYRegistrarTarjeta(String numeroTarjeta, String fechaVencimiento, String cvv, String titular) {
        SharedPreferences preferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String metodoPago = preferences.getString("metodo_pago", "Credito"); // Default: "Credito"
        String token = preferences.getString("auth_token", null);

        if (token == null) {
            Log.e("GuardarTarjeta", "No se encontró el token de autenticación");
            Toast.makeText(this, "No se encontró el token de autenticación", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d("MetodoPago", "Método de pago seleccionado: " + metodoPago);
        Log.d("GuardarTarjeta", "Token encontrado: " + token);

        apiService.getUserData("Bearer " + token).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Map<String, Object>> items = (List<Map<String, Object>>) response.body().get("items");
                    if (items != null && !items.isEmpty()) {
                        String userId = (String) items.get(0).get("id");
                        verificarSiTarjetaExiste(apiService, token, numeroTarjeta, fechaVencimiento, cvv, titular, userId, metodoPago);
                    } else {
                        Log.e("APIResponse", "No se encontraron datos del usuario");
                        Toast.makeText(agregar_tarjeta.this, "No se encontraron datos del usuario", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e("APIResponse", "Error al obtener datos del usuario: " + response.message());
                    Toast.makeText(agregar_tarjeta.this, "Error al obtener datos del usuario", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Log.e("APIError", "Error en la conexión: " + t.getMessage());
                Toast.makeText(agregar_tarjeta.this, "Fallo en la conexión", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void verificarSiTarjetaExiste(PocketBaseApi apiService, String token, String numeroTarjeta, String fechaVencimiento, String cvv, String titular, String userId, String metodoPago) {
        apiService.getTarjetas("Bearer " + token).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Map<String, Object>> tarjetas = (List<Map<String, Object>>) response.body().get("items");

                    boolean tarjetaExiste = false;
                    if (tarjetas != null) {
                        for (Map<String, Object> tarjeta : tarjetas) {
                            String tarjetaExistente = (String) tarjeta.get("numeroTarjeta");
                            if (tarjetaExistente != null && tarjetaExistente.equals(numeroTarjeta)) {
                                tarjetaExiste = true;
                                break;
                            }
                        }
                    }

                    if (tarjetaExiste) {
                        Toast.makeText(agregar_tarjeta.this, "La tarjeta ya está registrada", Toast.LENGTH_SHORT).show();
                    } else {
                        registrarTarjeta(numeroTarjeta, fechaVencimiento, cvv, titular, userId, metodoPago);
                    }
                } else {
                    registrarTarjeta(numeroTarjeta, fechaVencimiento, cvv, titular, userId, metodoPago);
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Toast.makeText(agregar_tarjeta.this, "Error en la conexión al verificar tarjeta", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void registrarTarjeta(String numeroTarjeta, String fechaVencimiento, String cvv, String titular, String userId, String metodoPago) {
        Map<String, Object> tarjetaData = new HashMap<>();
        tarjetaData.put("numeroTarjeta", numeroTarjeta);
        tarjetaData.put("fechaVencimiento", fechaVencimiento);
        tarjetaData.put("cvv", cvv);
        tarjetaData.put("titular", titular);
        tarjetaData.put("user_id", userId);
        tarjetaData.put("metodoPago", metodoPago);

        apiService.registrarTarjeta(tarjetaData).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Suponiendo que el id de la tarjeta está en la respuesta
                    String tarjetaId = (String) response.body().get("id");  // Aquí deberías ajustar esto a la respuesta de tu backend

                    if (tarjetaId != null) {
                        // Actualizar las preferencias compartidas con los nuevos datos de la tarjeta
                        SharedPreferences preferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString("tarjeta_id", tarjetaId);  // Guardar el tarjetaId
                        editor.putString("metodo_pago", metodoPago);  // Guardar el método de pago
                        editor.putString("numero_tarjeta", numeroTarjeta);  // Guardar el número de tarjeta
                        editor.putString("fecha_vencimiento", fechaVencimiento);  // Guardar la fecha de vencimiento
                        editor.putString("cvv", cvv);  // Guardar el cvv
                        editor.putString("titular", titular);  // Guardar el titular de la tarjeta
                        editor.apply();

                        // Navegar a la actividad Finalizando_compra
                        Toast.makeText(agregar_tarjeta.this, "Tarjeta registrada exitosamente", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(agregar_tarjeta.this, Finalizando_compra.class));
                        finish();
                    } else {
                        Toast.makeText(agregar_tarjeta.this, "No se pudo obtener el ID de la tarjeta", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(agregar_tarjeta.this, "Error al registrar la tarjeta", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Toast.makeText(agregar_tarjeta.this, "Error en la conexión al registrar tarjeta", Toast.LENGTH_SHORT).show();
            }
        });
    }


}
