package com.example.registrolineascampo;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class Registros extends AppCompatActivity {

    private Button btnNavRegistro, btnNavConfig, btnNavReportes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pantalla_inicial);
       //Inicializando Componentes
        btnNavConfig=findViewById(R.id.btnNavConfig);
        btnNavRegistro=findViewById(R.id.btnNavRegistro);
        btnNavReportes=findViewById(R.id.btnNavReportes);
       //Termina Inicializacion

       //Metodos Click
        btnNavConfig.setOnClickListener(v -> NavConfig());
        btnNavReportes.setOnClickListener(View ->NavReportes());

    }

    private void NavConfig(){
       if (btnNavConfig.isClickable()){
           Intent inten = new Intent(Registros.this,Configuracion.class);
           startActivity(inten);
           finish();
       }
    }
    private void NavReportes(){
        if(btnNavReportes.isClickable()){
            Intent inten = new Intent(Registros.this,Reportes.class);
            startActivity(inten);
            finish();
        }
    }
}