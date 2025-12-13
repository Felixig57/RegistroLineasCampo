package com.example.registrolineascampo;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class Reportes extends AppCompatActivity {
    private Button btnNavConfig, btnNavReportes, btnNavRegistro;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reportes);
        //Inicializar Componentes
        btnNavConfig = findViewById(R.id.btnNavConfig);
        btnNavReportes = findViewById(R.id.btnNavReportes);
        btnNavRegistro = findViewById(R.id.btnNavRegistro);
        //Termina la inicializacion
        //Inicializar Metodos OnClick
        btnNavConfig.setOnClickListener(View -> NavConfig());
        btnNavRegistro.setOnClickListener(View -> NavRegistro());
    }
    private void NavConfig(){
        if(btnNavConfig.isClickable()){
            Intent PantallaConfig = new Intent(Reportes.this,Configuracion.class);
            startActivity(PantallaConfig);
            finish();
        }
    }
    private void NavRegistro(){
        if(btnNavRegistro.isClickable()){
            Intent PantallaRegistro = new Intent(Reportes.this, Registros.class);
            startActivity(PantallaRegistro);
            finish();
        }
    }
}