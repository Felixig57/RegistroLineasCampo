package com.example.registrolineascampo;
import androidx.room.Database;
import androidx.room.RoomDatabase;

//clase abstract que hace conexion con los usuarios
@Database(entities = {Usuario.class},version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract UsuarioDao usuarioDao();
}