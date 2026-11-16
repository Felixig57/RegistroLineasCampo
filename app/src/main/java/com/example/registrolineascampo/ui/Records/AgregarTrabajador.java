package com.example.registrolineascampo.ui.Records;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.registrolineascampo.R;
import com.example.registrolineascampo.data.entity.ConfiguracionTrabajo;
import com.example.registrolineascampo.data.entity.LaborCampo;
import com.example.registrolineascampo.data.entity.Trabajador;
import com.example.registrolineascampo.data.local.AppDatabase;
import com.example.registrolineascampo.ui.Reports.Reportes;
import com.example.registrolineascampo.ui.Settings.Configuracion;
import com.google.android.material.textfield.TextInputEditText;

public class AgregarTrabajador extends AppCompatActivity {

    private TextView txtValTipo, txtValCuadro, txtValVariedad;
    private TextInputEditText txtNombreEmpleado, txtNumeroEmpleado, txtCuadroEmpleado, txtVariedadEmpleado;
    private Button btnAgregarEmpleado, btnNavRegistro, btnNavReportes, btnNavConfig;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setStatusBarColor(android.graphics.Color.parseColor("#4CAF50"));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agregartrabajador);
        com.example.registrolineascampo.ui.InsetsUtil.aplicar(
                findViewById(R.id.main),
                findViewById(R.id.linearLayoutHeader),
                findViewById(R.id.bottomNavLayout));
        db = AppDatabase.getAppDatabase(getApplicationContext());

        // Inicializar vistas
        txtValTipo = findViewById(R.id.txtValTipo);
        txtValCuadro = findViewById(R.id.txtValCuadro);
        txtValVariedad = findViewById(R.id.txtValVariedad);

        txtNombreEmpleado = findViewById(R.id.txtNombreEmpleado);
        txtNumeroEmpleado = findViewById(R.id.txtNumeroEmpleado);
        txtCuadroEmpleado = findViewById(R.id.txtCuadroEmpleado);
        txtVariedadEmpleado = findViewById(R.id.txtVariedadEmpleado);

        btnAgregarEmpleado = findViewById(R.id.btnAgregarEmpleado);
        btnNavRegistro = findViewById(R.id.btnNavRegistro);
        btnNavReportes = findViewById(R.id.btnNavReportes);
        btnNavConfig = findViewById(R.id.btnNavConfig);

        cargarConfiguracionDelDia();

        // Guardar trabajador
        btnAgregarEmpleado.setOnClickListener(v -> guardarTrabajador());

        // Navegación inferior
        btnNavRegistro.setOnClickListener(v -> finish());
        btnNavReportes.setOnClickListener(v -> {
            startActivity(new Intent(AgregarTrabajador.this, Reportes.class));
            finish();
        });
        btnNavConfig.setOnClickListener(v -> {
            startActivity(new Intent(AgregarTrabajador.this, Configuracion.class));
            finish();
        });
    }

    private void cargarConfiguracionDelDia() {
        new Thread(() -> {
            ConfiguracionTrabajo c = db.configuracionDao().getConfigPorFecha(LaborCampo.fechaHoy());
            runOnUiThread(() -> {
                if (c != null) {
                    txtValTipo.setText(c.tipoLabor);
                    txtValCuadro.setText(c.numeroCuadro);
                    txtValVariedad.setText(c.variedadCuadro);

                    // Prellenar los inputs para ahorrar tiempo al apuntador
                    txtCuadroEmpleado.setText(c.numeroCuadro);
                    txtVariedadEmpleado.setText(c.variedadCuadro);
                }
            });
        }).start();
    }

    private void guardarTrabajador() {
        String nombre = txtNombreEmpleado.getText() != null ? txtNombreEmpleado.getText().toString().trim() : "";
        String numero = txtNumeroEmpleado.getText() != null ? txtNumeroEmpleado.getText().toString().trim() : "";
        String cuadro = txtCuadroEmpleado.getText() != null ? txtCuadroEmpleado.getText().toString().trim() : "";
        String variedad = txtVariedadEmpleado.getText() != null ? txtVariedadEmpleado.getText().toString().trim() : "";

        if (nombre.isEmpty() || numero.isEmpty()) {
            Toast.makeText(this, "Ingresa al menos el nombre y número de empleado", Toast.LENGTH_SHORT).show();
            return;
        }

        // Inicia con 0 líneas asignadas para que se vayan sumando con el botón (+) en Registros
        Trabajador nuevo = new Trabajador(numero, nombre, "Sin líneas", 0, LaborCampo.fechaHoy());

        new Thread(() -> {
            db.trabajadorDao().insert(nuevo);
            runOnUiThread(() -> {
                Toast.makeText(this, "Trabajador agregado", Toast.LENGTH_SHORT).show();
                finish(); // Regresa a Registros y dispara onResume() para actualizar la lista
            });
        }).start();
    }
}