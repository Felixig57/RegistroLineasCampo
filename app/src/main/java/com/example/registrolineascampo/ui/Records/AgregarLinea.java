
package com.example.registrolineascampo.ui.Records;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
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

import java.util.ArrayList;
import java.util.List;

public class AgregarLinea extends AppCompatActivity {

    private TextView txtValTipo;
    private TextView txtValCuadro;
    private TextView txtValVariedad;
    private TextView txtNombreTrabajadorLinea;

    private AutoCompleteTextView autoCompleteNumeroLinea;

    private TextInputEditText txtCuadroLinea;
    private TextInputEditText txtVariedadLinea;

    private Button btnGuardarLinea;
    private Button btnNavRegistro;
    private Button btnNavReportes;
    private Button btnNavConfig;

    private AppDatabase db;

    private int trabajadorId = -1;

    private Trabajador trabajadorActual;

    private String tipoLaborActual = "";

    private boolean datosCargados = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setStatusBarColor(Color.parseColor("#4CAF50"));

        setContentView(R.layout.activity_agregarlinea);
        com.example.registrolineascampo.ui.InsetsUtil.aplicar(
                findViewById(R.id.main),
                findViewById(R.id.linearLayoutHeader),
                findViewById(R.id.bottomNavLayout));

        db = AppDatabase.getAppDatabase(getApplicationContext());

        trabajadorId = getIntent().getIntExtra("trabajador_id", -1);

        inicializarVistas();

        configurarDropdownLineas();

        btnGuardarLinea.setEnabled(false);

        cargarDatosIniciales();

        btnGuardarLinea.setOnClickListener(v -> guardarLinea());

        btnNavRegistro.setOnClickListener(v -> finish());

        btnNavReportes.setOnClickListener(v -> {
            startActivity(new Intent(this, Reportes.class));
            finish();
        });

        btnNavConfig.setOnClickListener(v -> {
            startActivity(new Intent(this, Configuracion.class));
            finish();
        });
    }

    private void inicializarVistas() {

        txtValTipo = findViewById(R.id.txtValTipo);
        txtValCuadro = findViewById(R.id.txtValCuadro);
        txtValVariedad = findViewById(R.id.txtValVariedad);

        txtNombreTrabajadorLinea =
                findViewById(R.id.txtNombreTrabajadorLinea);

        autoCompleteNumeroLinea =
                findViewById(R.id.autoCompleteNumeroLinea);

        txtCuadroLinea = findViewById(R.id.txtCuadroLinea);
        txtVariedadLinea = findViewById(R.id.txtVariedadLinea);

        btnGuardarLinea = findViewById(R.id.btnGuardarLinea);
        btnNavRegistro = findViewById(R.id.btnNavRegistro);
        btnNavReportes = findViewById(R.id.btnNavReportes);
        btnNavConfig = findViewById(R.id.btnNavConfig);
    }

    private void configurarDropdownLineas() {

        List<String> numeros = new ArrayList<>();

        for (int i = 1; i <= 200; i++) {
            numeros.add(String.valueOf(i));
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                numeros
        );

        autoCompleteNumeroLinea.setAdapter(adapter);

        autoCompleteNumeroLinea.setThreshold(0);

        autoCompleteNumeroLinea.setOnClickListener(v ->
                autoCompleteNumeroLinea.showDropDown()
        );
    }

    private void cargarDatosIniciales() {

        new Thread(() -> {

            try {

                ConfiguracionTrabajo config =
                        db.configuracionDao().getConfigPorFecha(
                                LaborCampo.fechaHoy()
                        );

                Trabajador trabajador = null;

                if (trabajadorId != -1) {
                    trabajador = db.trabajadorDao().getById(trabajadorId);
                }

                final Trabajador trabajadorEncontrado = trabajador;

                runOnUiThread(() -> {

                    if (isFinishing() || isDestroyed()) {
                        return;
                    }

                    trabajadorActual = trabajadorEncontrado;

                    if (config != null) {

                        tipoLaborActual = config.tipoLabor != null
                                ? config.tipoLabor : "";

                        txtValTipo.setText(tipoLaborActual);

                        txtValCuadro.setText(config.numeroCuadro);
                        txtValVariedad.setText(config.variedadCuadro);

                        txtCuadroLinea.setText(config.numeroCuadro);
                        txtVariedadLinea.setText(config.variedadCuadro);

                    } else {

                        txtValTipo.setText("---");
                        txtValCuadro.setText("---");
                        txtValVariedad.setText("---");

                        Toast.makeText(
                                this,
                                "No existe configuración para hoy",
                                Toast.LENGTH_SHORT
                        ).show();
                    }

                    if (trabajadorActual != null) {

                        txtNombreTrabajadorLinea.setText(
                                trabajadorActual.nombre
                        );

                        datosCargados = true;
                        btnGuardarLinea.setEnabled(true);

                    } else {

                        txtNombreTrabajadorLinea.setText(
                                "Trabajador no encontrado"
                        );

                        Toast.makeText(
                                this,
                                "No se encontró el trabajador",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });

            } catch (Exception e) {

                runOnUiThread(() -> {

                    Toast.makeText(
                            this,
                            "Error al cargar datos: " + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
            }

        }).start();
    }

    private void guardarLinea() {

        if (!datosCargados || trabajadorActual == null) {

            Toast.makeText(
                    this,
                    "Los datos del trabajador todavía no están disponibles",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        String numLinea = autoCompleteNumeroLinea
                .getText().toString().trim();

        String cuadro = txtCuadroLinea.getText() != null
                ? txtCuadroLinea.getText().toString().trim()
                : "";

        String variedad = txtVariedadLinea.getText() != null
                ? txtVariedadLinea.getText().toString().trim()
                : "";

        if (numLinea.isEmpty()) {

            autoCompleteNumeroLinea.setError(
                    "Selecciona un número de línea"
            );

            return;
        }

        int numero;

        try {

            numero = Integer.parseInt(numLinea);

        } catch (NumberFormatException e) {

            autoCompleteNumeroLinea.setError(
                    "Número de línea inválido"
            );

            return;
        }

        if (numero < 1 || numero > 200) {

            autoCompleteNumeroLinea.setError(
                    "El número debe estar entre 1 y 200"
            );

            return;
        }

        autoCompleteNumeroLinea.setError(null);

        btnGuardarLinea.setEnabled(false);

        // Copiar los datos necesarios antes de iniciar el hilo.
        final int idTrabajador = trabajadorId;
        final String numeroLinea = String.valueOf(numero);
        final String cuadroFinal = cuadro;
        final String variedadFinal = variedad;
        final String tipoFinal = tipoLaborActual;

        new Thread(() -> {

            try {

                // Ejecutar las dos operaciones en una transacción.
                db.runInTransaction(() -> {

                    Trabajador trabajador =
                            db.trabajadorDao().getById(idTrabajador);

                    if (trabajador == null) {
                        throw new IllegalStateException(
                                "El trabajador ya no existe"
                        );
                    }

                    // Comprobar si la línea ya está registrada.
                    String lineasActuales = trabajador.lineasTexto;

                    if (lineasActuales != null
                            && !lineasActuales.trim().isEmpty()
                            && !lineasActuales.equals("Sin líneas")) {

                        String[] lineas = lineasActuales.split(",");

                        for (String linea : lineas) {

                            if (linea.trim().equals(numeroLinea)) {

                                throw new IllegalStateException(
                                        "Esta línea ya está registrada para el trabajador"
                                );
                            }
                        }
                    }

                    // Actualizar información del trabajador.
                    if (trabajador.totalLineas == 0
                            || lineasActuales == null
                            || lineasActuales.trim().isEmpty()
                            || lineasActuales.equals("Sin líneas")) {

                        trabajador.lineasTexto = numeroLinea;

                    } else {

                        trabajador.lineasTexto =
                                lineasActuales + ", " + numeroLinea;
                    }

                    trabajador.totalLineas++;

                    db.trabajadorDao().update(trabajador);

                    // Crear registro para los reportes.
                    LaborCampo labor = new LaborCampo(
                            trabajador.numeroTrabajador,
                            trabajador.nombre,
                            tipoFinal,
                            cuadroFinal,
                            variedadFinal,
                            numeroLinea,
                            LaborCampo.fechaHoy()
                    );

                    db.laborDao().insert(labor);
                });

                runOnUiThread(() -> {

                    if (isFinishing() || isDestroyed()) {
                        return;
                    }

                    Toast.makeText(
                            this,
                            "Línea " + numeroLinea + " agregada correctamente",
                            Toast.LENGTH_SHORT
                    ).show();

                    finish();
                });

            } catch (Exception e) {

                runOnUiThread(() -> {

                    if (isFinishing() || isDestroyed()) {
                        return;
                    }

                    btnGuardarLinea.setEnabled(true);

                    Toast.makeText(
                            this,
                            "No se pudo guardar: " + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
            }

        }).start();
    }
}
