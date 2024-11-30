package com.example.kleax;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class metodo_pago extends AppCompatActivity {

    Button btnAgregarTarjeta;
    TextView crDito, deDito, tarjetaRegistrada;
    private PocketBaseApi apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_metodo_pago);

        btnAgregarTarjeta = findViewById(R.id.btn_agregar_tarjeta);
        crDito = findViewById(R.id.cr_dito);
        deDito = findViewById(R.id.de_dito);
        tarjetaRegistrada = findViewById(R.id.tarjeta_registrada);

        // Inicializa el servicio de la API
        apiService = ApiClient.getClient().create(PocketBaseApi.class);

        SharedPreferences preferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String authToken = preferences.getString("auth_token", null);
        String userId = preferences.getString("user_id", null); // Recupera el user_id

        if (authToken == null) {
            Log.e("AuthError", "No se encontró el token de autenticación. Redirigiendo a inicio de sesión.");
            Toast.makeText(this, "Inicia sesión nuevamente", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(metodo_pago.this, iniciar_sesion.class));
            finish(); // Evita que el usuario continúe
            return;
        }

        if (userId == null) {
            Log.e("UserIdError", "No se encontró el user_id. Redirigiendo a inicio de sesión.");
            Toast.makeText(this, "Error al obtener el ID de usuario", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(metodo_pago.this, iniciar_sesion.class));
            finish(); // Evita que el usuario continúe si no hay user_id
            return;
        }


// Logs para verificar que el token es el mismo
        Log.d("MetodoPago", "Auth Token recuperado: " + authToken);
        Log.d("MetodoPago", "UserId recuperado: " + userId);

        // Verificar si el usuario tiene tarjetas de crédito y débito registradas
        verificarTarjetasRegistradas(authToken);

        // Inicializar el botón como oculto
        btnAgregarTarjeta.setVisibility(View.GONE);

        crDito.setOnClickListener(v -> {
            Log.d("MetodoPago", "Método de pago seleccionado: Crédito");
            crDito.setBackgroundColor(getResources().getColor(R.color.selected_color));
            deDito.setBackgroundColor(getResources().getColor(android.R.color.transparent));

            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("metodo_pago", "Credito");
            editor.apply();

            // Mostrar el botón después de elegir el método de pago
            btnAgregarTarjeta.setVisibility(View.VISIBLE);
        });

        deDito.setOnClickListener(v -> {
            Log.d("MetodoPago", "Método de pago seleccionado: Débito");
            deDito.setBackgroundColor(getResources().getColor(R.color.selected_color));
            crDito.setBackgroundColor(getResources().getColor(android.R.color.transparent));

            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("metodo_pago", "Debito");
            editor.apply();

            // Mostrar el botón después de elegir el método de pago
            btnAgregarTarjeta.setVisibility(View.VISIBLE);
        });

        btnAgregarTarjeta.setOnClickListener(v -> {
            SharedPreferences preferences1 = getSharedPreferences("user_prefs", MODE_PRIVATE);
            String metodoPago = preferences1.getString("metodo_pago", null);
            String tarjetaDebito = preferences1.getString("tarjeta_debito", null); // Tarjeta débito
            String tarjetaCredito = preferences1.getString("tarjeta_credito", null); // Tarjeta crédito

            Log.d("MetodoPago", "Método de pago seleccionado: " + metodoPago);
            Log.d("MetodoPago", "Tarjeta débito registrada: " + tarjetaDebito);
            Log.d("MetodoPago", "Tarjeta crédito registrada: " + tarjetaCredito);

            // Verificar que se haya seleccionado un método de pago
            if (metodoPago == null) {
                Log.e("MetodoPago", "No se ha seleccionado un método de pago.");
                Toast.makeText(metodo_pago.this, "Por favor, seleccione un método de pago.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Verifica si la tarjeta de débito está registrada y si el método seleccionado es Débito
            if (metodoPago.equals("Debito") && tarjetaDebito != null) {
                Log.d("MetodoPago", "Tarjeta de débito registrada. Redirigiendo a finalización de compra.");
                Intent intent = new Intent(metodo_pago.this, Finalizando_compra.class);
                startActivity(intent);
            }
            // Verifica si la tarjeta de crédito está registrada y si el método seleccionado es Crédito
            else if (metodoPago.equals("Credito") && tarjetaCredito != null) {
                Log.d("MetodoPago", "Tarjeta de crédito registrada. Redirigiendo a finalización de compra.");
                Intent intent = new Intent(metodo_pago.this, Finalizando_compra.class);
                startActivity(intent);
            }
            // Si no hay tarjeta registrada para el método de pago seleccionado, ir a agregar tarjeta
            else {
                Log.d("MetodoPago", "No se ha registrado la tarjeta para el método de pago seleccionado. Redirigiendo a agregar tarjeta.");
                Intent intent = new Intent(metodo_pago.this, agregar_tarjeta.class);
                startActivity(intent);
            }
        });
    }

    private void verificarTarjetasRegistradas(String authToken) {
        Log.d("MetodoPago", "Verificando tarjetas registradas...");
        apiService.getTarjetas("Bearer " + authToken).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Map<String, Object>> tarjetas = (List<Map<String, Object>>) response.body().get("items");


                    String tarjetaDebito = null;
                    String tarjetaCredito = null;
                    String tarjetaId = null;  // Almacenar el tarjeta_id encontrado

                    if (tarjetas != null && !tarjetas.isEmpty()) {
                        Log.d("MetodoPago", "Tarjetas encontradas: " + tarjetas.size());
                        // Verifica si hay una tarjeta de débito y de crédito registrada
                        for (Map<String, Object> tarjeta : tarjetas) {
                            String tipoTarjeta = (String) tarjeta.get("metodoPago"); // Asumimos que el tipo se guarda así

                            if ("Debito".equals(tipoTarjeta)) {
                                tarjetaDebito = (String) tarjeta.get("numeroTarjeta");
                                tarjetaId = (String) tarjeta.get("id");  // Guardamos el ID de la tarjeta

                                Log.d("MetodoPago", "Tarjeta débito encontrada: " + tarjetaDebito);
                            } else if ("Credito".equals(tipoTarjeta)) {
                                tarjetaCredito = (String) tarjeta.get("numeroTarjeta");
                                tarjetaId = (String) tarjeta.get("id");  // Guardamos el ID de la tarjeta
                                Log.d("MetodoPago", "Tarjeta crédito encontrada: " + tarjetaCredito);
                            }
                        }

                        // Guardar las tarjetas en SharedPreferences
                        SharedPreferences preferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString("tarjeta_id", tarjetaId);  // Guardamos el tarjeta_id
                        editor.putString("tarjeta_debito", tarjetaDebito);
                        editor.putString("tarjeta_credito", tarjetaCredito);
                        editor.apply();

                        // Mostrar las tarjetas registradas en la interfaz
                        if (tarjetaDebito != null && tarjetaCredito != null) {
                            tarjetaRegistrada.setVisibility(View.VISIBLE);
                            tarjetaRegistrada.setText("Tarjetas débito y crédito registradas");
                            Log.d("MetodoPago", "Ambas tarjetas (débito y crédito) están registradas.");
                        } else if (tarjetaDebito != null) {
                            tarjetaRegistrada.setVisibility(View.VISIBLE);
                            tarjetaRegistrada.setText("Tarjeta débito registrada");
                            Log.d("MetodoPago", "Solo tarjeta débito registrada.");
                        } else if (tarjetaCredito != null) {
                            tarjetaRegistrada.setVisibility(View.VISIBLE);
                            tarjetaRegistrada.setText("Tarjeta crédito registrada");
                            Log.d("MetodoPago", "Solo tarjeta crédito registrada.");
                        } else {
                            tarjetaRegistrada.setVisibility(View.GONE);
                            Log.d("MetodoPago", "No se encontraron tarjetas registradas.");
                        }
                    } else {
                        tarjetaRegistrada.setVisibility(View.GONE);
                        Log.d("MetodoPago", "No hay tarjetas registradas.");
                    }
                } else {
                    tarjetaRegistrada.setVisibility(View.GONE);
                    Log.e("MetodoPago", "Error al obtener las tarjetas: " + response.message());
                    Toast.makeText(metodo_pago.this, "No se pudo obtener las tarjetas registradas", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                tarjetaRegistrada.setVisibility(View.GONE);
                Log.e("MetodoPago", "Error de conexión al obtener tarjetas: " + t.getMessage());
                Toast.makeText(metodo_pago.this, "Error de conexión al verificar tarjetas", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
