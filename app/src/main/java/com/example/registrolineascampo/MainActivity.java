package com.example.registrolineascampo;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.room.Room;

public class MainActivity extends AppCompatActivity {
    private EditText txtUsuario, txtContrasena;
    private Button btnLogin;
    private AppDatabase db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
     //inicializar componentes
      txtUsuario = findViewById(R.id.txtUsuario);
      txtContrasena = findViewById(R.id.txtContrasena);
      btnLogin = findViewById(R.id.btnLogin);
      //inicializar BD
        db = Room.databaseBuilder(getApplicationContext(),AppDatabase.class,"db_campo").build();
        MisUsuarios();

      //on click del boton
      btnLogin.setOnClickListener(v -> iniciar());
    }
    private void iniciar(){
        String Usuario = txtUsuario.getText().toString().trim();
        String Contrasena = txtContrasena.getText().toString().trim();

        //validar campos vacios


        if(Usuario.isEmpty() || Contrasena.isEmpty()){

            Toast.makeText(this,"No dejar Campos Vacios",Toast.LENGTH_SHORT).show();

            if(Usuario.isEmpty()) txtUsuario.setError("Requerido");
            if(Contrasena.isEmpty()) txtContrasena.setError("Requerido");

        }else{
            new Thread(() -> {
                // 1. Consultamos la base de datos en el hilo secundario
                Usuario usuarioEncontrado = db.usuarioDao().login(Usuario, Contrasena);

                // 2. Volvemos al Hilo Principal (UI) para mostrar el resultado y navegar
                runOnUiThread(() -> {
                    if (usuarioEncontrado != null) {
                        Toast.makeText(getApplicationContext(), "¡Bienvenido " + usuarioEncontrado.nombreUsuario + "!", Toast.LENGTH_LONG).show();
                        // Navegación exitosa
                        Intent intent = new Intent(MainActivity.this, PantallaInicial.class);
                        startActivity(intent);
                        finish();

                    } else {
                        Toast.makeText(getApplicationContext(), "Usuario o Contraseña incorrectos", Toast.LENGTH_SHORT).show();
                    }
                });

            }).start();
        }

    }//termina metodo iniciar
    private void MisUsuarios(){
        new Thread(() -> {
            if(db.usuarioDao().ContarUsuarios()== 0){
                Usuario Apuntador1 = new Usuario("Apuntador1_2025","Poda_2025_1");
                Usuario Apuntador2 = new Usuario("Apuntador2_2025","Poda_2025_2");
                Usuario Admin = new Usuario("Admin123","12345");

                db.usuarioDao().registrar(Apuntador1);
                db.usuarioDao().registrar(Apuntador2);
                db.usuarioDao().registrar(Admin);
            }
        }).start();
    }

    }//cierra clase principal
