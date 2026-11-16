package com.example.registrolineascampo.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "trabajadores")
public class Trabajador {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String numeroTrabajador; // Por si buscas por número en el SearchView
    public String nombre;
    public String lineasTexto;      // Ejemplo: "2, 42, 89"
    public int totalLineas;         // Ejemplo: 3
    public String fecha;            // Para filtrar los registros del día

    public Trabajador() {
    }

    public Trabajador(String numeroTrabajador, String nombre, String lineasTexto, int totalLineas, String fecha) {
        this.numeroTrabajador = numeroTrabajador;
        this.nombre = nombre;
        this.lineasTexto = lineasTexto;
        this.totalLineas = totalLineas;
        this.fecha = fecha;
    }
}