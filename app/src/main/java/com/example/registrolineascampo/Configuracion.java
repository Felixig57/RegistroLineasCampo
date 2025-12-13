package com.example.registrolineascampo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
public class Configuracion extends AppCompatActivity {
    private Button btnNavConfig, btnNavRegistro,btnNavReportes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuracion);
      //Inicializar Componentes
        btnNavConfig = findViewById(R.id.btnNavConfig);
        btnNavRegistro = findViewById(R.id.btnNavRegistro);
        btnNavReportes = findViewById(R.id.btnNavReportes);
        //Termina inicializacion
        //Inicializar Metodos
        btnNavReportes.setOnClickListener(View -> NavReportes());
        btnNavRegistro.setOnClickListener(View -> NavRegistro());
    }
    private void NavRegistro(){
        if (btnNavRegistro.isClickable()){
            Intent PantallaRegistro= new Intent(Configuracion.this, Registros.class);
            startActivity(PantallaRegistro);
            finish();
        }
    }
    private void NavReportes(){
        if(btnNavReportes.isClickable()){
            Intent PantallaReportes = new Intent(Configuracion.this, Reportes.class);
            startActivity(PantallaReportes);
            finish();
        }
    }
}