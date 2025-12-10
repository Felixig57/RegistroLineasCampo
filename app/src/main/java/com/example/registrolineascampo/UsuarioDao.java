package com.example.registrolineascampo;
import androidx.room.Query;
import androidx.room.Dao;
import androidx.room.Insert;
@Dao
public interface UsuarioDao {
@Insert
    void registrar(Usuario usuario);
//query para seleccion de usuarios
@Query("SELECT * FROM usuarios WHERE nombreUsuario =:Usuario AND Contrasena =:pass LIMIT 1")
Usuario login(String Usuario, String pass);

@Query("SELECT COUNT(*) FROM usuarios")
    int ContarUsuarios();
}
