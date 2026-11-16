package com.example.registrolineascampo.data.local;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.registrolineascampo.data.dao.ConfiguracionDao;
import com.example.registrolineascampo.data.dao.LaborDao;
import com.example.registrolineascampo.data.dao.TrabajadorDao;
import com.example.registrolineascampo.data.dao.UsuarioDao;
import com.example.registrolineascampo.data.entity.ConfiguracionTrabajo;
import com.example.registrolineascampo.data.entity.LaborCampo;
import com.example.registrolineascampo.data.entity.Trabajador;
import com.example.registrolineascampo.data.entity.Usuario;

// Clase abstracta que hace la conexion con la base de datos local (Singleton)
@Database(entities = {Usuario.class, ConfiguracionTrabajo.class, Trabajador.class, LaborCampo.class}, version = 5, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract UsuarioDao usuarioDao();
    public abstract ConfiguracionDao configuracionDao();
    public abstract TrabajadorDao trabajadorDao();
    public abstract LaborDao laborDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getAppDatabase(Context ctx) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(ctx.getApplicationContext(),
                                    AppDatabase.class, "db_campo")
                            .fallbackToDestructiveMigration() // version 2 -> 3 -> 4 -> 5 (en desarrollo)
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}