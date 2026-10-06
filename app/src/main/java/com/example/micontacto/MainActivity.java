package com.example.micontacto;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private ImageView ivFoto;
    private TextView tvNombre, tvTelefono, tvCorreo;
    private Button btnRegistrar;

    private Contacto contacto; // queda en null si todavia no hay contacto
    private ActivityResultLauncher<Intent> formularioLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ivFoto = findViewById(R.id.ivFotoMain);
        tvNombre = findViewById(R.id.tvNombreMain);
        tvTelefono = findViewById(R.id.tvTelefonoMain);
        tvCorreo = findViewById(R.id.tvCorreoMain);
        btnRegistrar = findViewById(R.id.btnRegistrar);
        Button btnVerDetalle = findViewById(R.id.btnVerDetalle);
        Button btnAyuda = findViewById(R.id.btnAyuda);

        // aca llega lo que devuelve el formulario
        formularioLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                resultado -> {
                    Intent datos = resultado.getData();
                    if (resultado.getResultCode() == RESULT_OK
                            && datos != null) {
                        Contacto nuevo = Contacto.desdeIntent(datos);
                        if (nuevo != null) {
                            contacto = nuevo;
                            guardarContacto();
                            mostrarContacto();
                        }
                    }
                });

        // explicito 1: abre el formulario y espera que vuelva con los datos
        btnRegistrar.setOnClickListener(v -> {
            Intent intent = new Intent(this, FormularioActivity.class);
            // si ya hay un contacto se lo mando al formulario para editarlo
            if (contacto != null) {
                contacto.agregarAIntent(intent);
            }
            formularioLauncher.launch(intent);
        });

        // explicito 2: abre el detalle y le manda los datos con putExtra
        btnVerDetalle.setOnClickListener(v -> {
            if (contacto == null) {
                Toast.makeText(this, "Primero registra un contacto", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(this, DetalleActivity.class);
            contacto.agregarAIntent(intent);
            startActivity(intent);
        });

        // explicito 3: abre la pantalla de ayuda
        btnAyuda.setOnClickListener(v -> startActivity(new Intent(this, AyudaActivity.class)));

        cargarContacto();
        mostrarContacto();
    }

    private void mostrarContacto() {
        if (contacto == null) {
            tvNombre.setText(R.string.sin_contacto);
            tvTelefono.setVisibility(View.GONE);
            tvCorreo.setVisibility(View.GONE);
            btnRegistrar.setText(R.string.btn_registrar);
            return;
        }

        tvNombre.setText(contacto.getNombre());
        tvTelefono.setText(contacto.getTelefono());
        tvCorreo.setText(contacto.getCorreo());
        tvTelefono.setVisibility(View.VISIBLE);
        tvCorreo.setVisibility(View.VISIBLE);
        btnRegistrar.setText(R.string.btn_editar);
        ImagenUtils.mostrarFoto(this, contacto.getFotoUri(), ivFoto);
    }

    // guardo el contacto para que no se pierda al cerrar la app
    private void guardarContacto() {
        SharedPreferences prefs = getSharedPreferences("datos_contacto", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(Contacto.EXTRA_NOMBRE, contacto.getNombre());
        editor.putString(Contacto.EXTRA_TELEFONO, contacto.getTelefono());
        editor.putString(Contacto.EXTRA_CORREO, contacto.getCorreo());
        editor.putString(Contacto.EXTRA_DIRECCION, contacto.getDireccion());
        editor.putString(Contacto.EXTRA_SITIO_WEB, contacto.getSitioWeb());
        editor.putString(Contacto.EXTRA_FOTO, contacto.getFotoUri());
        editor.apply();
    }

    private void cargarContacto() {
        SharedPreferences prefs = getSharedPreferences("datos_contacto", MODE_PRIVATE);
        String nombre = prefs.getString(Contacto.EXTRA_NOMBRE, null);
        if (nombre != null) {
            contacto = new Contacto(nombre,
                    prefs.getString(Contacto.EXTRA_TELEFONO, ""),
                    prefs.getString(Contacto.EXTRA_CORREO, ""),
                    prefs.getString(Contacto.EXTRA_DIRECCION, ""),
                    prefs.getString(Contacto.EXTRA_SITIO_WEB, ""),
                    prefs.getString(Contacto.EXTRA_FOTO, ""));
        }
    }
}
