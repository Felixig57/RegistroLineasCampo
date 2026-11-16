package com.example.registrolineascampo.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

@Entity(tableName = "labor_campo")
public class LaborCampo {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String numeroTrabajador;
    public String nombreTrabajador;
    public String tipoLabor;
    public String numeroCuadro;
    public String variedadCuadro;
    public String numeroLinea;
    public String fecha; // Formato yyyy-MM-dd

    // Constructor vacío requerido por Room
    public LaborCampo() {}

    // Constructor para registrar fácilmente
    public LaborCampo(String numeroTrabajador, String nombreTrabajador, String tipoLabor, String numeroCuadro, String variedadCuadro, String numeroLinea, String fecha) {
        this.numeroTrabajador = numeroTrabajador;
        this.nombreTrabajador = nombreTrabajador;
        this.tipoLabor = tipoLabor;
        this.numeroCuadro = numeroCuadro;
        this.variedadCuadro = variedadCuadro;
        this.numeroLinea = numeroLinea;
        this.fecha = fecha;
    }

    // Fecha de hoy en formato yyyy-MM-dd
    public static String fechaHoy() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    }
}