package com.example.registrolineascampo.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.registrolineascampo.data.entity.Trabajador;

import java.util.List;

@Dao
public interface TrabajadorDao {
    @Insert
    long insert(Trabajador t);

    @Update
    void update(Trabajador t);

    @Query("SELECT * FROM trabajadores")
    List<Trabajador> getAll();

    @Query("SELECT * FROM trabajadores WHERE id = :id LIMIT 1")
    Trabajador getById(int id);

    // Ranking del dia: mayor a menor cantidad de lineas
    @Query("SELECT * FROM trabajadores WHERE fecha = :fecha ORDER BY totalLineas DESC, nombre ASC")
    List<Trabajador> getRankingPorFecha(String fecha);

    // Para "Limpiar historial"
    @Query("DELETE FROM trabajadores")
    void deleteAll();
}