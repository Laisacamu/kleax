package com.example.kleax;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class recibiendo_pago extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_recibiendo_pago);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Crear un retraso de 2 segundos antes de redirigir a la pantalla de inicio
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Aquí, cambiamos a la actividad principal (Home)
                Intent intent = new Intent(recibiendo_pago.this, home.class); // Reemplaza 'HomeActivity.class' por tu actividad de inicio
                startActivity(intent);
                finish(); // Termina la actividad actual para que no vuelva cuando el usuario presione atrás
            }
        }, 2000); // 2000 milisegundos = 2 segundos
    }
}
