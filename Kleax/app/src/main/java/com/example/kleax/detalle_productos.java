package com.example.kleax;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class detalle_productos extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_detalle_productos);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        View botonhome = findViewById(R.id.home_page);
        botonhome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(detalle_productos.this, home.class);
                startActivity(intent);
            }
        });
        View botonbuscar = findViewById(R.id.search);
        botonbuscar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(detalle_productos.this, buscar.class);
                startActivity(intent);
            }
        });
        View botoncompra = findViewById(R.id.shopping_ca);
        botoncompra.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(detalle_productos.this, agregar_carrito.class);
                startActivity(intent);
            }
        });
        View botonperfil = findViewById(R.id.perfil);
        botonperfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(detalle_productos.this, Perfil.class);
                startActivity(intent);
            }
        });
        View botoncomprar = findViewById(R.id.rectangle_5);
        botoncomprar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(detalle_productos.this, agregar_carrito.class);
                startActivity(intent);
            }
        });
    }
}