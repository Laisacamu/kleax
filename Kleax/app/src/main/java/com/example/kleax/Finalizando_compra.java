    package com.example.kleax;

    import android.content.Intent;
    import android.content.SharedPreferences;
    import android.os.Bundle;
    import android.util.Log;
    import android.view.Gravity;
    import android.widget.Button;
    import android.widget.ImageView;
    import android.widget.LinearLayout;
    import android.widget.TextView;
    import android.widget.Toast;

    import androidx.appcompat.app.AppCompatActivity;

    import com.bumptech.glide.Glide;

    import java.io.IOException;
    import java.util.HashMap;
    import java.util.ArrayList;
    import java.util.List;
    import java.util.Map;

    import retrofit2.Call;
    import retrofit2.Callback;
    import retrofit2.Response;

    import java.text.SimpleDateFormat;
    import java.util.Date;
    import java.util.Locale;

    public class Finalizando_compra extends AppCompatActivity {

        private PocketBaseApi apiService;
        private TextView tvMetodoPago, tvNumeroTarjeta, tvProductos, tvSubtotal, tvIVA, tvDescuento, tvTotal;
        private double subtotalGlobal = 0.0;
        private int productosProcesados = 0;
        private List<Map<String, Object>> carritoProductos = new ArrayList<>();
        // Definir como variable global
        private int totalProductosCarrito = 0;


        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_finalizar_compra);

            // Inicializar el servicio API
            apiService = ApiClient.getClient().create(PocketBaseApi.class);

            // Asociar las vistas con los elementos del layout
            tvMetodoPago = findViewById(R.id.tvMetodoPago);
            tvNumeroTarjeta = findViewById(R.id.tvNumeroTarjeta);
            tvProductos = findViewById(R.id.tvProductos);
            tvSubtotal = findViewById(R.id.tvSubtotal);
            tvIVA = findViewById(R.id.tvIVA);
            tvDescuento = findViewById(R.id.tvDescuento);
            tvTotal = findViewById(R.id.tvTotal);
            Button btnFinalizarCompra = findViewById(R.id.btnFinalizarCompra);

            // Recuperar el auth_token y el user_id de SharedPreferences
            SharedPreferences preferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
            String authToken = preferences.getString("auth_token", null);
            String userId = preferences.getString("user_id", null);

            // Verificar si el auth_token y user_id están disponibles
            if (authToken == null || userId == null) {
                Log.e("AuthError", "No se encontró el token de autenticación o el user_id. Redirigiendo a inicio de sesión.");
                Toast.makeText(this, "Por favor, inicia sesión nuevamente", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(Finalizando_compra.this, iniciar_sesion.class));
                finish(); // Evitar que el usuario continúe sin estar autenticado
                return;
            }

            // Si los valores existen, puedes proceder con las acciones necesarias
            Log.d("FinalizandoCompra", "user_id: " + userId + " y auth_token recuperados exitosamente.");


            // Cargar datos de la compra
            cargarDatosCompra();

            btnFinalizarCompra.setOnClickListener(v -> finalizarCompra());
        }

        private void cargarDatosCompra() {
            SharedPreferences preferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
            String userIdFromSharedPreferences = preferences.getString("user_id", null);  // Recupera el userId
            Log.d("FinalizandoCompra", "UserId de SharedPreferences: " + userIdFromSharedPreferences);
            String token = preferences.getString("auth_token", null);

            if (token == null) {
                Log.e("FinalizandoCompra", "No se encontró el token de autenticación");
                Toast.makeText(Finalizando_compra.this, "No se encontró el token de autenticación", Toast.LENGTH_SHORT).show();
                return; // Sal de este método si no hay token
            }

            Log.d("FinalizandoCompra", "Token de autenticación: " + token);


            // Cargar primero los datos del método de pago desde SharedPreferences
            String metodoPago = preferences.getString("metodo_pago", "Credito");
            tvMetodoPago.setText(metodoPago); // Muestra el método de pago seleccionado

            // Obtener el userId de SharedPreferences
            String userId = preferences.getString("user_id", null);
            if (userId == null) {
                Log.e("FinalizandoCompra", "No se encontró el user_id en SharedPreferences");
                Toast.makeText(Finalizando_compra.this, "No se encontró el ID de usuario", Toast.LENGTH_SHORT).show();
                return; // Salir si el user_id no está disponible
            }

            Log.d("FinalizandoCompra", "user_id recuperado: " + userId);

            String filter = "id='" + userId + "'";  // Filtro para buscar el usuario específico
            // Realizar la solicitud para obtener los datos del usuario
            apiService.getUserDatas("Bearer " + token, filter).enqueue(new Callback<Map<String, Object>>() {
                @Override
                public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        List<Map<String, Object>> items = (List<Map<String, Object>>) response.body().get("items");

                        if (items != null && !items.isEmpty()) {
                            String userIdFromSharedPreferences = preferences.getString("user_id", null);
                            boolean usuarioEncontrado = false;

                            for (Map<String, Object> user : items) {
                                String userIdFromResponse = (String) user.get("id");

                                if (userIdFromResponse != null && userIdFromResponse.equals(userIdFromSharedPreferences)) {
                                    usuarioEncontrado = true;
                                    Log.d("FinalizandoCompra", "Usuario encontrado: " + userIdFromResponse);
                                    obtenerCarrito(userIdFromResponse);
                                    obtenerMetodoPago(userIdFromResponse);
                                    break;  // Salimos del bucle al encontrar el usuario correcto
                                }
                            }

                            if (!usuarioEncontrado) {
                                Log.e("FinalizandoCompra", "El userId recuperado no coincide con el guardado.");
                                Toast.makeText(Finalizando_compra.this, "ID de usuario no coincide", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                    else {
                            Log.e("FinalizandoCompra", "Items no disponibles en la respuesta");
                            Toast.makeText(Finalizando_compra.this, "Error: usuario no encontrado", Toast.LENGTH_SHORT).show();
                        }
                    }


                @Override
                public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                    Log.e("FinalizandoCompra", "Error de conexión: " + t.getMessage());
                    Toast.makeText(Finalizando_compra.this, "Error de conexión", Toast.LENGTH_SHORT).show();
                }
            });
        }


        private void obtenerMetodoPago(String userId) {
            SharedPreferences preferences = getSharedPreferences("user_prefs", MODE_PRIVATE);

            // Obtener el método de pago y número de tarjeta desde SharedPreferences
            String metodoPago = preferences.getString("metodo_pago", "Credito");
            String numeroTarjeta = preferences.getString("numero_tarjeta", "**** **** **** ****");

            // Agregar logs para depuración
            Log.d("FinalizandoCompra", "Método de pago obtenido de SharedPreferences: " + metodoPago);
            Log.d("FinalizandoCompra", "Número de tarjeta obtenido de SharedPreferences: " + numeroTarjeta);

            // Establecer el método de pago en la interfaz
            tvMetodoPago.setText(metodoPago);

            // Mostrar solo los últimos 4 dígitos de la tarjeta
            if (numeroTarjeta != null && numeroTarjeta.length() > 4) {
                tvNumeroTarjeta.setText("**** **** **** " + numeroTarjeta.substring(numeroTarjeta.length() - 4));
                Log.d("FinalizandoCompra", "Número de tarjeta mostrado: **** **** **** " + numeroTarjeta.substring(numeroTarjeta.length() - 4));
            } else {
                tvNumeroTarjeta.setText("Tarjeta no disponible");
                Log.d("FinalizandoCompra", "Número de tarjeta no disponible");
            }
        }


        private void obtenerCarrito(String userId) {
            SharedPreferences preferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
            String token = preferences.getString("auth_token", null);

            if (token == null && userId != null) {
                Toast.makeText(Finalizando_compra.this, "Error: Token no encontrado", Toast.LENGTH_SHORT).show();
                return;
            }

            // 1. Obtener carrito asociado al usuario
            String filtro = "usuarios_id='" + userId + "'";
            Log.d("FinalizandoCompra", "Filtro para obtener carrito: " + filtro);

            apiService.getCarrito("Bearer " + token, filtro).enqueue(new Callback<Map<String, Object>>() {
                @Override
                public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Log.d("FinalizandoCompra", "Respuesta completa del carrito: " + response.body().toString());

                        // 2. Parsear la respuesta para obtener los items
                        Map<String, Object> responseBody = response.body();
                        List<Map<String, Object>> items = (List<Map<String, Object>>) responseBody.get("items");

                        if (items != null && !items.isEmpty()) {
                            Map<String, Object> carrito = items.get(0); // Obtener el primer carrito (si hay varios)
                            String carritoId = (String) carrito.get("id"); // Obtener el id del carrito

                            Log.d("FinalizandoCompra", "Carrito encontrado con ID: " + carritoId);

                            // 3. Validar que el carrito tenga un ID
                            if (carritoId != null && !carritoId.isEmpty()) {
                                // 4. Obtener productos del carrito usando carrito_id
                                obtenerProductosCarrito(carritoId, token);
                            } else {
                                Log.e("FinalizandoCompra", "Carrito no encontrado o ID vacío.");
                                tvProductos.setText("No se encontró un carrito.");
                            }
                        } else {
                            Log.e("FinalizandoCompra", "Lista de items vacía o nula.");
                            tvProductos.setText("Tu carrito está vacío. Agrega productos para continuar.");
                        }
                    } else {
                        Log.e("FinalizandoCompra", "Error en la respuesta HTTP: " + response.code() + " - " + response.message());
                        tvProductos.setText("Tu carrito está vacío.");
                    }
                }

                @Override
                public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                    Log.e("FinalizandoCompra", "Error al obtener el carrito: " + t.getMessage());
                    Toast.makeText(Finalizando_compra.this, "Error al obtener el carrito", Toast.LENGTH_SHORT).show();
                }
            });
        }


        private void obtenerProductosCarrito(String carritoId, String token) {
            if (token == null) {
                Toast.makeText(Finalizando_compra.this, "Error: Token no encontrado", Toast.LENGTH_SHORT).show();
                return;
            }

            // 1. Obtener productos en el carrito usando carrito_id desde la relación en carrito_productos
            String filtro = "carrito_id='" + carritoId + "'";
            apiService.getCarritoProductos("Bearer " + token, filtro).enqueue(new Callback<Map<String, Object>>() {
                @Override
                public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                    if (response.isSuccessful()) {
                        Map<String, Object> responseBody = response.body();
                        carritoProductos = (List<Map<String, Object>>) responseBody.get("items");

                        if (carritoProductos != null && !carritoProductos.isEmpty()) {


                            totalProductosCarrito = 0;
                            for (Map<String, Object> item : carritoProductos) {
                                List<String> productosIds = (List<String>) item.get("productos_id");
                                if (productosIds != null) {
                                    totalProductosCarrito += productosIds.size(); // Sumar la cantidad de productos en cada item
                                }
                            }

                            Log.d("FinalizandoCompra", "Total de productos en el carrito: " + totalProductosCarrito);


                            subtotalGlobal = 0.0;  // Reseteamos el subtotal global antes de recalcular
                            productosProcesados = 0;

                            for (Map<String, Object> item : carritoProductos) {
                                // Asegúrate de que "productos_id" sea una lista, no un solo String
                                List<String> productosIds = (List<String>) item.get("productos_id");

                                if (productosIds != null) {
                                    // Iterar sobre cada producto ID en la lista
                                    for (String productoId : productosIds) {
                                        int cantidad = (item.get("cantidad") != null) ? Integer.parseInt(item.get("cantidad").toString()) : 1;
                                        obtenerProductoDetalles(productoId, token, cantidad);  // Aquí sigue siendo asíncron
                                    }
                                } else {
                                    Log.e("FinalizandoCompra", "No se encontraron productos_id en el carrito.");
                                }
                            }
                        } else {
                            Log.e("FinalizandoCompra", "No se encontraron productos en el carrito.");
                            tvProductos.setText("Tu carrito está vacío.");
                        }
                    } else {
                        Log.e("FinalizandoCompra", "Error en la respuesta: " + response.errorBody());
                        tvProductos.setText("Error al obtener productos del carrito.");
                    }
                }

                @Override
                public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                    Log.e("FinalizandoCompra", "Error al obtener productos del carrito: " + t.getMessage());
                    Toast.makeText(Finalizando_compra.this, "Error al obtener productos del carrito", Toast.LENGTH_SHORT).show();
                }
            });
        }


        private void obtenerProductoDetalles(String productoId, String token, int cantidad) {
            // Validar que el token no sea nulo antes de continuar
            if (token == null) {
                Toast.makeText(Finalizando_compra.this, "Error: Token no encontrado", Toast.LENGTH_SHORT).show();
                return;
            }

            // Verificar si el carrito tiene productos
            if (carritoProductos == null || carritoProductos.isEmpty()) {
                Log.e("FinalizandoCompra", "El carrito está vacío, no hay productos para procesar.");
                return;  // Salir si el carrito está vacío
            }

            // Verificar si el carrito tiene productos
            Log.d("FinalizandoCompra", "Productos en el carrito: " + carritoProductos);

            apiService.getProductoById("Bearer " + token, productoId).enqueue(new Callback<Map<String, Object>>() {
                @Override
                public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                    if (response.isSuccessful()) {
                        Map<String, Object> producto = response.body();
                        if (producto != null) {
                            double precio = 0.0;

                            // Obtener precio del producto
                            String precioTexto = (String) producto.get("precio");
                            if (precioTexto != null) {
                                try {
                                    precio = Double.parseDouble(precioTexto);
                                } catch (NumberFormatException e) {
                                    Log.e("FinalizandoCompra", "Error al convertir el precio: " + e.getMessage());
                                }
                            }

                            // Calcular subtotal para este producto
                            double subtotal = precio * cantidad;

                            // Acumular el subtotal global
                            subtotalGlobal += subtotal;

                            // Log de detalles del producto
                            Log.d("FinalizandoCompra", "Producto ID: " + productoId + ", Cantidad: " + cantidad + ", Precio: " + precio + ", Subtotal: " + subtotal);

                            // Mostrar los detalles del producto en la UI
                            String nombre = (String) producto.get("nombre");
                            String descripcion = (String) producto.get("descripcion");
                            String imagenUrl = (String) producto.get("imagen_url");
                            String categoria = (String) producto.get("categoria");

                            // Asegurarse de que los valores no sean nulos
                            if (nombre == null) nombre = "Nombre no disponible";
                            if (descripcion == null) descripcion = "Descripción no disponible";
                            if (categoria == null) categoria = "Categoría no disponible";
                            if (imagenUrl == null) imagenUrl = "";

                            // Log para verificar los detalles
                            Log.d("FinalizandoCompra", "Producto: " + nombre + " - " + descripcion + " - $" + precio + ", Subtotal: $" + subtotal);

                            // Mostrar detalles del producto en la UI
                            StringBuilder productoDetails = new StringBuilder();
                            productoDetails.append("\n")  // Salto de línea antes de agregar los productos
                                    .append(nombre)
                                    .append(" - $").append(precio).append("\n")
                                    .append("Descripción: ").append(descripcion).append("\n")
                                    .append("Categoría: ").append(categoria).append("\n")
                                    .append("Subtotal: $").append(subtotal).append("\n\n");  // Salto de línea al final

                            tvProductos.append(productoDetails.toString());

                            // Llamar al método para agregar la imagen y el nombre al layout
                            if (imagenUrl != null && !imagenUrl.isEmpty()) {
                                agregarImagenAlLayout(imagenUrl, productoDetails.toString()); // Aquí se pasa el mismo objeto productoDetails
                            }

                            // Contabilizamos cuántos productos hemos procesado
                            productosProcesados++;

                            // Ahora calculamos el total cuando todos los productos han sido procesados
                            Log.d("FinalizandoCompra", "Productos procesados: " + productosProcesados);
                            Log.d("FinalizandoCompra", "Cantidad total de productos en el carrito: " + totalProductosCarrito);


                            // Ahora calculamos el total cuando todos los productos han sido procesados
                            if (productosProcesados == carritoProductos.size()) {
                                double iva = subtotalGlobal * 0.16;
                                double descuento = calcularDescuento(subtotalGlobal);
                                double total = subtotalGlobal + iva - descuento;

                                // Logs para verificar los cálculos antes de actualizar la UI
                                Log.d("FinalizandoCompra", "Subtotal global calculado: " + subtotalGlobal);
                                Log.d("FinalizandoCompra", "IVA calculado (16%): " + iva);
                                Log.d("FinalizandoCompra", "Descuento calculado: " + descuento);
                                Log.d("FinalizandoCompra", "Total calculado (Subtotal + IVA - Descuento): " + total);

                                tvSubtotal.setText(String.format("Subtotal: $%.2f", subtotalGlobal));
                                tvIVA.setText(String.format("IVA: $%.2f", iva));
                                tvDescuento.setText(String.format("Descuento: $%.2f", descuento));
                                tvTotal.setText(String.format("Total: $%.2f", total));
                                // Log de confirmación después de actualizar la UI
                                Log.d("FinalizandoCompra", "Total calculado y UI actualizada correctamente.");


                            } else {
                                // Log si los productos no han sido procesados completamente
                                Log.d("FinalizandoCompra", "Esperando más productos: " + (totalProductosCarrito - productosProcesados) + " productos restantes.");

                            }

                        } else {
                            Log.e("FinalizandoCompra", "Producto no encontrado en la base de datos.");
                        }
                    } else {
                        try {
                            // Intentar leer el cuerpo del error
                            String errorBody = response.errorBody() != null ? response.errorBody().string() : "No se obtuvo cuerpo de error";
                            Log.e("FinalizandoCompra", "Error al obtener detalles del producto: " + errorBody);
                        } catch (IOException e) {
                            Log.e("FinalizandoCompra", "Error al leer el cuerpo de error: " + e.getMessage());
                        }
                    }
                }

                @Override
                public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                    Log.e("FinalizandoCompra", "Error al obtener detalles del producto: " + t.getMessage());
                }
            });
        }



        private double calcularDescuento(double subtotal) {
            double descuento = 0.0;
            // Regla de ejemplo: 10% de descuento si el subtotal es mayor a 100
            if (subtotal > 100) {
                descuento = subtotal * 0.10;
            }
            return descuento;
        }


        private void agregarImagenAlLayout(String imagenUrl, String detallesProducto) {
            if (imagenUrl == null || imagenUrl.isEmpty()) {
                Log.e("FinalizandoCompra", "URL de la imagen no válida.");
                return;
            }

            LinearLayout layoutProductos = findViewById(R.id.layoutProductos);

            // Crear un LinearLayout vertical para cada producto
            LinearLayout productoLayout = new LinearLayout(Finalizando_compra.this);
            productoLayout.setOrientation(LinearLayout.VERTICAL);
            productoLayout.setPadding(10, 10, 10, 10);  // Ajustar el espaciado si es necesario

            // Crear un TextView para los detalles del producto
            TextView detallesProductoTextView = new TextView(Finalizando_compra.this);
            detallesProductoTextView.setText(detallesProducto);
            detallesProductoTextView.setTextSize(14);  // Ajusta el tamaño según lo necesites
            detallesProductoTextView.setGravity(Gravity.START);
            detallesProductoTextView.setTextAppearance(R.style.desc_productos);

            // Crear una nueva ImageView para la imagen del producto
            ImageView imageView = new ImageView(Finalizando_compra.this);

            // Establecer un tamaño fijo para la imagen (ejemplo: 150dp de ancho y 150dp de alto)
            int imageWidth = (int) (getResources().getDisplayMetrics().density * 150);  // 150dp de ancho
            int imageHeight = (int) (getResources().getDisplayMetrics().density * 150); // 150dp de alto

            // Establecer las dimensiones y propiedades de la imagen
            imageView.setLayoutParams(new LinearLayout.LayoutParams(imageWidth, imageHeight));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

            // Cargar la imagen desde la URL usando Glide
            Glide.with(Finalizando_compra.this)
                    .load(imagenUrl)
                    .placeholder(R.drawable.placeholder) // Imagen de carga
                    .error(R.drawable.error_image) // Imagen de error
                    .into(imageView);

            // Agregar el TextView y la ImageView al LinearLayout vertical
            productoLayout.addView(detallesProductoTextView);
            productoLayout.addView(imageView);

            // Agregar el LinearLayout del producto al layout de productos
            layoutProductos.addView(productoLayout);
        }



            private void finalizarCompra() {
                SharedPreferences preferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
                String token = preferences.getString("auth_token", null);
                String userId = preferences.getString("user_id", null);

                if (token == null || userId == null) {
                    Toast.makeText(this, "Error de usuario no encontrado", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Verificar si existe un ID de tarjeta válido
                String tarjetaId = preferences.getString("tarjeta_id", null);
                if (tarjetaId == null || tarjetaId.isEmpty()) {
                    Log.e("FinalizandoCompra", "ID de tarjeta no encontrado.");
                    Toast.makeText(this, "ID de tarjeta no disponible", Toast.LENGTH_SHORT).show();
                    return;
                }

                Log.d("FinalizandoCompra", "Tarjeta ID almacenado: " + tarjetaId);  // Imprimir el tarjetaId

                // Solicitar tarjetas del backend para verificar si existe la tarjeta
                apiService.getTarjetas("Bearer " + token).enqueue(new Callback<Map<String, Object>>() {
                    @Override
                    public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                        if (response.isSuccessful()) {
                            // Verifica si 'items' existe y no es nulo
                            Map<String, Object> responseBody = response.body();
                            List<Map<String, Object>> items = (List<Map<String, Object>>) responseBody.get("items");

                            if (items != null && !items.isEmpty()) {
                                Log.d("FinalizandoCompra", "Respuesta de tarjetas: " + items.toString());

                                boolean tarjetaEncontrada = false;

                                // Buscar la tarjeta en la lista de 'items'
                                for (Map<String, Object> tarjeta : items) {
                                    String idTarjeta = (String) tarjeta.get("id");
                                    Log.d("FinalizandoCompra", "Comparando tarjeta ID desde respuesta: " + idTarjeta);
                                    Log.d("FinalizandoCompra", "Comparando con tarjeta ID almacenado: " + tarjetaId);

                                    if (idTarjeta.equals(tarjetaId)) {
                                        tarjetaEncontrada = true;
                                        break;
                                    }
                                }

                                if (tarjetaEncontrada) {
                                    Log.d("FinalizandoCompra", "Tarjeta válida encontrada.");
                                    // Ahora que hemos verificado que la tarjeta existe, podemos continuar con el pago
                                    registrarPago(tarjetaId, userId);
                                } else {
                                    Log.e("FinalizandoCompra", "Tarjeta no encontrada.");
                                    Toast.makeText(Finalizando_compra.this, "Tarjeta no válida", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Log.e("FinalizandoCompra", "No se encontró el campo 'items' o está vacío.");
                                Toast.makeText(Finalizando_compra.this, "Error al verificar tarjetas", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Log.e("FinalizandoCompra", "Error al verificar tarjeta: " + response.code() + " - " + response.message());
                            Toast.makeText(Finalizando_compra.this, "Error al verificar tarjeta", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                        Log.e("FinalizandoCompra", "Error de conexión: " + t.getMessage());
                        Toast.makeText(Finalizando_compra.this, "Error de conexión", Toast.LENGTH_SHORT).show();
                    }
                });
            }


            private void registrarPago(String tarjetaId, String userId) {
                // Validar monto
                String montoStr = tvTotal.getText().toString().replace("Total:", "").replace("$", "").trim();
                if (montoStr.isEmpty()) {
                    Toast.makeText(this, "Monto inválido", Toast.LENGTH_SHORT).show();
                    return;
                }

                double monto;
                try {
                    monto = Double.parseDouble(montoStr);
                } catch (NumberFormatException e) {
                    Log.e("FinalizandoCompra", "Error al convertir el monto: " + e.getMessage());
                    Toast.makeText(this, "Monto inválido", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Obtener la fecha actual en el formato esperado (yyyy-MM-dd HH:mm:ss.SSSZ)
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ", Locale.getDefault());
                String fechaPago = sdf.format(new Date());  // Convertir la fecha actual a string en el formato deseado

                // Crear los datos del pago
                Map<String, Object> pagoData = new HashMap<>();
                pagoData.put("userId", userId);
                pagoData.put("monto", monto);
                pagoData.put("metodoPago", tvMetodoPago.getText().toString());
                pagoData.put("fechaPago", fechaPago);  // Usamos la fecha formateada
                pagoData.put("tarjetaId", tarjetaId);  // Aquí ya pasamos el tarjetaId validado

                // Registrar el pago
                apiService.registrarPago(pagoData).enqueue(new Callback<Map<String, Object>>() {
                    @Override
                    public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                        if (response.isSuccessful()) {
                            // Solo procesamos la respuesta sin necesidad de mostrar la fecha de pago
                            Toast.makeText(Finalizando_compra.this, "Compra Finalizada", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(Finalizando_compra.this, recibiendo_pago.class));
                        } else {
                            Log.e("FinalizandoCompra", "Error al registrar pago: " + response.code() + " - " + response.message());
                            Toast.makeText(Finalizando_compra.this, "Error al procesar el pago", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                        Log.e("FinalizandoCompra", "Error de conexión: " + t.getMessage());
                        Toast.makeText(Finalizando_compra.this, "Error de conexión", Toast.LENGTH_SHORT).show();
                    }
                });
            }


    }
