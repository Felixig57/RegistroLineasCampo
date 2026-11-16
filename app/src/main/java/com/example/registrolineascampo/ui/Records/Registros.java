package com.example.registrolineascampo.ui.Records;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.registrolineascampo.R;
import com.example.registrolineascampo.data.entity.ConfiguracionTrabajo;
import com.example.registrolineascampo.data.entity.LaborCampo;
import com.example.registrolineascampo.data.entity.Trabajador;
import com.example.registrolineascampo.data.local.AppDatabase;
import com.example.registrolineascampo.ui.Reports.Reportes;
import com.example.registrolineascampo.ui.Settings.Configuracion;

import java.util.List;

public class Registros extends AppCompatActivity {

    private Button btnNavRegistro, btnNavConfig, btnNavReportes;
    private TextView txtValTipo, txtValCuadro, txtValVariedad, textViewNoEmpleados;
    private LinearLayout btnAgregarTrabajador;
    private RecyclerView recyclerTrabajadores;
    private TrabajadorAdapter adapter;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setStatusBarColor(android.graphics.Color.parseColor("#4CAF50"));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registros);
        com.example.registrolineascampo.ui.InsetsUtil.aplicar(
                findViewById(R.id.main),
                findViewById(R.id.linearLayoutHeader),
                findViewById(R.id.bottomNavLayout));
        // Inicializando Componentes
        btnNavConfig = findViewById(R.id.btnNavConfig);
        btnNavRegistro = findViewById(R.id.btnNavRegistro);
        btnNavReportes = findViewById(R.id.btnNavReportes);
        txtValTipo = findViewById(R.id.txtValTipo);
        txtValCuadro = findViewById(R.id.txtValCuadro);
        txtValVariedad = findViewById(R.id.txtValVariedad);
        btnAgregarTrabajador = findViewById(R.id.btnAgregarTrabajador);
        recyclerTrabajadores = findViewById(R.id.recyclerTrabajadores);
        textViewNoEmpleados = findViewById(R.id.textViewNoEmpleados);

        db = AppDatabase.getAppDatabase(getApplicationContext());

        // Configurar RecyclerView
        recyclerTrabajadores.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TrabajadorAdapter(new TrabajadorAdapter.OnLineaClickListener() {
            @Override
            public void onSumarClick(Trabajador trabajador) {
                Intent intent = new Intent(Registros.this, AgregarLinea.class);
                intent.putExtra("trabajador_id", trabajador.id);
                startActivity(intent);
            }

            @Override
            public void onRestarClick(Trabajador trabajador) {
                if (trabajador.totalLineas <= 0) return;

                new Thread(() -> {
                    if (trabajador.lineasTexto != null && trabajador.lineasTexto.contains(", ")) {
                        int ultimaComa = trabajador.lineasTexto.lastIndexOf(", ");
                        trabajador.lineasTexto = trabajador.lineasTexto.substring(0, ultimaComa);
                    } else {
                        trabajador.lineasTexto = "Sin líneas";
                    }
                    trabajador.totalLineas -= 1;
                    db.trabajadorDao().update(trabajador);

                    runOnUiThread(() -> cargarTrabajadores());
                }).start();
            }
        });
        recyclerTrabajadores.setAdapter(adapter);

        // Métodos Click
        btnNavConfig.setOnClickListener(v -> NavConfig());
        btnNavReportes.setOnClickListener(v -> NavReportes());

        btnAgregarTrabajador.setOnClickListener(v -> {
            Intent intent = new Intent(Registros.this, AgregarTrabajador.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarConfiguracionDelDia();
        cargarTrabajadores();
    }

    private void cargarTrabajadores() {
        new Thread(() -> {
            List<Trabajador> lista = db.trabajadorDao().getAll();
            runOnUiThread(() -> {
                if (lista != null && !lista.isEmpty()) {
                    adapter.setLista(lista);
                    recyclerTrabajadores.setVisibility(View.VISIBLE);
                    textViewNoEmpleados.setVisibility(View.GONE);
                } else {
                    recyclerTrabajadores.setVisibility(View.GONE);
                    textViewNoEmpleados.setVisibility(View.VISIBLE);
                }
            });
        }).start();
    }

    private void cargarConfiguracionDelDia() {
        new Thread(() -> {
            ConfiguracionTrabajo c = db.configuracionDao().getConfigPorFecha(LaborCampo.fechaHoy());
            runOnUiThread(() -> {
                if (c != null) {
                    txtValTipo.setText(c.tipoLabor);
                    txtValCuadro.setText(c.numeroCuadro);
                    txtValVariedad.setText(c.variedadCuadro);
                } else {
                    txtValTipo.setText("---");
                    txtValCuadro.setText("---");
                    txtValVariedad.setText("---");
                }
            });
        }).start();
    }

    private void NavConfig() {
        if (btnNavConfig.isClickable()) {
            Intent inten = new Intent(Registros.this, Configuracion.class);
            startActivity(inten);
            finish();
        }
    }

    private void NavReportes() {
        if (btnNavReportes.isClickable()) {
            Intent inten = new Intent(Registros.this, Reportes.class);
            startActivity(inten);
            finish();
        }
    }
}