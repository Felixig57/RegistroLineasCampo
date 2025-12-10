package com.example.registrolineascampo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
@Entity(tableName = "usuarios")
public class Usuario {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String nombreUsuario;
    public String contrasena;


    public Usuario(String nombreUsuario, String contrasena){
        this.nombreUsuario=nombreUsuario;
        this.contrasena=contrasena;
    }
    public Usuario(){
    }
}
