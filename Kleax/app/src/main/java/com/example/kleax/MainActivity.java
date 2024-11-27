package com.example.kleax;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Encontrar el botón con el ID "boton_re"
        View botonRegistrarse = findViewById(R.id.boton_re);

        // Configurar el OnClickListener para abrir ActivityRegistrarse
        botonRegistrarse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,registrarse.class);
                startActivity(intent);
            }
        });
        TextView iniciarSesionText = findViewById(R.id.iniciar_ses);
        iniciarSesionText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, iniciar_sesion.class);
                startActivity(intent);
            }
        });
    }
}