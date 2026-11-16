package com.example.registrolineascampo.ui.Settings;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.registrolineascampo.R;
import com.example.registrolineascampo.data.entity.ConfiguracionTrabajo;
import com.example.registrolineascampo.data.entity.LaborCampo;
import com.example.registrolineascampo.data.local.AppDatabase;
import com.example.registrolineascampo.ui.Records.Registros;
import com.example.registrolineascampo.ui.Reports.Reportes;
import com.google.android.material.textfield.TextInputEditText;

public class Configuracion extends AppCompatActivity {

    // Cambia esta lista por los tipos de labor que ya usas
    private static final String[] TIPOS_TRABAJO = {"Poda", "Deshije", "Amarre", "Cosecha"};

    private Spinner spinnerTipoTrabajo;
    private TextInputEditText txtConfigCuadro, txtConfigVariedad, txtConfigPrecio;
    private Button btnGuardarConfig, btnLimpiarHistorial, btnCerrarSesion;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setStatusBarColor(android.graphics.Color.parseColor("#4CAF50"));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuracion); // ajusta si tu layout se llama distinto
        com.example.registrolineascampo.ui.InsetsUtil.aplicar(
                findViewById(R.id.main),
                findViewById(R.id.linearLayoutHeader),
                findViewById(R.id.bottomNavLayout));
        db = AppDatabase.getAppDatabase(getApplicationContext());

        spinnerTipoTrabajo = findViewById(R.id.spinnerTipoTrabajo);
        txtConfigCuadro = findViewById(R.id.txtConfigCuadro);
        txtConfigVariedad = findViewById(R.id.txtConfigVariedad);
        txtConfigPrecio = findViewById(R.id.txtConfigPrecio);
        btnGuardarConfig = findViewById(R.id.btnGuardarConfig);
        btnLimpiarHistorial = findViewById(R.id.btnLimpiarHistorial);
        btnCerrarSesion = findViewById(R.id.btnCerrarSesion);

        spinnerTipoTrabajo.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, TIPOS_TRABAJO));

        cargarConfiguracionDelDia();

        btnGuardarConfig.setOnClickListener(v -> guardarConfiguracion());
        btnLimpiarHistorial.setOnClickListener(v -> confirmarLimpiarHistorial());
        btnCerrarSesion.setOnClickListener(v -> cerrarSesion());
    }

    // Si ya hay configuracion de hoy, la muestra en los campos
    private void cargarConfiguracionDelDia() {
        new Thread(() -> {
            ConfiguracionTrabajo c = db.configuracionDao().getConfigPorFecha(LaborCampo.fechaHoy());
            runOnUiThread(() -> {
                if (c == null) return;
                txtConfigCuadro.setText(c.numeroCuadro);
                txtConfigVariedad.setText(c.variedadCuadro);
                if (c.precioLinea > 0) txtConfigPrecio.setText(String.valueOf(c.precioLinea));
                for (int i = 0; i < TIPOS_TRABAJO.length; i++) {
                    if (TIPOS_TRABAJO[i].equals(c.tipoLabor)) {
                        spinnerTipoTrabajo.setSelection(i);
                        break;
                    }
                }
            });
        }).start();
    }

    private String texto(TextInputEditText campo) {
        return campo.getText() != null ? campo.getText().toString().trim() : "";
    }

    private void guardarConfiguracion() {
        String cuadro = texto(txtConfigCuadro);
        String variedad = texto(txtConfigVariedad);
        String textoPrecio = texto(txtConfigPrecio);

        if (cuadro.isEmpty() || variedad.isEmpty()) {
            Toast.makeText(this, "Ingresa el cuadro y la variedad", Toast.LENGTH_SHORT).show();
            return;
        }

        double precio = 0;
        try {
            if (!textoPrecio.isEmpty()) precio = Double.parseDouble(textoPrecio);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Precio no válido", Toast.LENGTH_SHORT).show();
            return;
        }

        ConfiguracionTrabajo config = new ConfiguracionTrabajo();
        config.tipoLabor = spinnerTipoTrabajo.getSelectedItem().toString();
        config.numeroCuadro = cuadro;
        config.variedadCuadro = variedad;
        config.precioLinea = precio;
        config.fecha = LaborCampo.fechaHoy();

        new Thread(() -> {
            db.configuracionDao().insert(config);
            runOnUiThread(() ->
                    Toast.makeText(this, "Configuración guardada", Toast.LENGTH_SHORT).show());
        }).start();
    }

    private void confirmarLimpiarHistorial() {
        new AlertDialog.Builder(this)
                .setTitle("Limpiar historial")
                .setMessage("Se borrarán todos los trabajadores y configuraciones guardadas. ¿Continuar?")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Borrar", (d, w) -> new Thread(() -> {
                    db.trabajadorDao().deleteAll();
                    db.configuracionDao().deleteAll();
                    runOnUiThread(() ->
                            Toast.makeText(this, "Historial borrado", Toast.LENGTH_SHORT).show());
                }).start())
                .show();
    }

    private void cerrarSesion() {
        // Reemplaza por tu pantalla de login, por ejemplo:
        // Intent i = new Intent(this, Login.class);
        // i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        // startActivity(i);
        finishAffinity();
    }

    // ---------- Navegación (tu XML usa android:onClick) ----------

    public void NavRegistro(View v) {
        startActivity(new Intent(this, Registros.class));
        finish();
    }

    public void NavReportes(View v) {
        startActivity(new Intent(this, Reportes.class));
        finish();
    }

    public void NavConfig(View v) {
        // Ya estás en Config
    }
}