
package com.example.registrolineascampo.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.registrolineascampo.data.entity.LaborCampo;

import java.util.List;

@Dao
public interface LaborDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    void insert(LaborCampo labor);

    @Query("SELECT * FROM labor_campo WHERE fecha = :fecha")
    List<LaborCampo> getLaboresPorFecha(String fecha);

    @Query("SELECT * FROM labor_campo")
    List<LaborCampo> getAll();

}
