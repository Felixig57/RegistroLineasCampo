package com.example.registrolineascampo.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "configuracion_trabajo")
public class ConfiguracionTrabajo {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String tipoLabor;
    public String numeroCuadro;
    public String variedadCuadro;
    public double precioLinea;
    public String fecha;

    public ConfiguracionTrabajo() {
    }
}