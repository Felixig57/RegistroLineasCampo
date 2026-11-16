package com.example.registrolineascampo.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.registrolineascampo.data.entity.ConfiguracionTrabajo;

@Dao
public interface ConfiguracionDao {
    @Insert
    long insert(ConfiguracionTrabajo c);

    // Ultima configuracion guardada (la mas reciente por id)
    @Query("SELECT * FROM configuracion_trabajo ORDER BY id DESC LIMIT 1")
    ConfiguracionTrabajo getUltimaConfig();

    // Configuracion del dia: la ultima guardada con la fecha indicada
    @Query("SELECT * FROM configuracion_trabajo WHERE fecha = :fecha ORDER BY id DESC LIMIT 1")
    ConfiguracionTrabajo getConfigPorFecha(String fecha);

    // Para "Limpiar historial"
    @Query("DELETE FROM configuracion_trabajo")
    void deleteAll();
}