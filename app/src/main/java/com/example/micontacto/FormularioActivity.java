package com.example.micontacto;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class FormularioActivity extends AppCompatActivity {

    private EditText etNombre, etTelefono, etCorreo, etDireccion, etSitioWeb;
    private ImageView ivFoto;

    private Uri uriFoto; // archivo donde la camara deja la foto
    private String fotoGuardada = "";

    private ActivityResultLauncher<String> permisoCamaraLauncher;
    private ActivityResultLauncher<Uri> camaraLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_formulario);

        // flecha para volver
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        ivFoto = findViewById(R.id.ivFotoForm);
        etNombre = findViewById(R.id.etNombre);
        etTelefono = findViewById(R.id.etTelefono);
        etCorreo = findViewById(R.id.etCorreo);
        etDireccion = findViewById(R.id.etDireccion);
        etSitioWeb = findViewById(R.id.etSitioWeb);
        Button btnTomarFoto = findViewById(R.id.btnTomarFoto);
        Button btnGuardar = findViewById(R.id.btnGuardar);

        // respuesta cuando se pide el permiso de camara
        permisoCamaraLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), aceptado -> {
            if (aceptado) {
                abrirCamara();
            } else {
                Toast.makeText(this, "Sin permiso de cámara no se puede tomar la foto", Toast.LENGTH_LONG).show();
            }
        });

        // implicito 5: abre la camara (TakePicture usa ACTION_IMAGE_CAPTURE)
        camaraLauncher = registerForActivityResult(new ActivityResultContracts.TakePicture(), seTomo -> {
            if (seTomo && uriFoto != null) {
                fotoGuardada = uriFoto.toString();
                ImagenUtils.mostrarFoto(this, fotoGuardada, ivFoto);
                Toast.makeText(this, "Foto guardada en la galería", Toast.LENGTH_SHORT).show();
            } else if (uriFoto != null) {
                // se cancelo la foto, borro el archivo que quedo vacio en la galeria
                getContentResolver().delete(uriFoto, null, null);
            }
        });

        if (savedInstanceState != null) {
            // se giro la pantalla, recupero la foto
            fotoGuardada = savedInstanceState.getString("foto", "");
        } else {
            // si viene un contacto es porque se esta editando, relleno los campos
            Contacto contacto = Contacto.desdeIntent(getIntent());
            if (contacto != null) {
                etNombre.setText(contacto.getNombre());
                etTelefono.setText(contacto.getTelefono());
                etCorreo.setText(contacto.getCorreo());
                etDireccion.setText(contacto.getDireccion());
                etSitioWeb.setText(contacto.getSitioWeb());
                fotoGuardada = contacto.getFotoUri();
            }
        }
        ImagenUtils.mostrarFoto(this, fotoGuardada, ivFoto);

        btnTomarFoto.setOnClickListener(v -> tomarFoto());
        btnGuardar.setOnClickListener(v -> guardar());
    }

    private void tomarFoto() {
        // validacion: que el celular tenga camara
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            Toast.makeText(this, "Este celular no tiene cámara", Toast.LENGTH_SHORT).show();
            return;
        }

        // validacion: permiso de camara, si no esta se pide
        String camara = Manifest.permission.CAMERA;
        if (ContextCompat.checkSelfPermission(this, camara)
                == PackageManager.PERMISSION_GRANTED) {
            abrirCamara();
        } else {
            permisoCamaraLauncher.launch(camara);
        }
    }

    private void abrirCamara() {
        // creo el archivo en la galeria (Pictures/MiContacto) donde se va a guardar la foto
        ContentValues valores = new ContentValues();
        valores.put(MediaStore.Images.Media.DISPLAY_NAME, "contacto_" + System.currentTimeMillis() + ".jpg");
        valores.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        valores.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/MiContacto");
        uriFoto = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, valores);

        if (uriFoto == null) {
            Toast.makeText(this, "No se pudo crear la foto", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            camaraLauncher.launch(uriFoto);
        } catch (ActivityNotFoundException e) {
            getContentResolver().delete(uriFoto, null, null);
            Toast.makeText(this, "No hay una app de cámara", Toast.LENGTH_SHORT).show();
        }
    }

    private void guardar() {
        String nombre = etNombre.getText().toString().trim();
        String telefono = etTelefono.getText().toString().trim().replace(" ", "");
        String correo = etCorreo.getText().toString().trim();
        String direccion = etDireccion.getText().toString().trim();
        String web = etSitioWeb.getText().toString().trim();

        boolean valido = true;

        if (nombre.isEmpty()) {
            etNombre.setError("Ingresa el nombre");
            valido = false;
        } else if (nombre.length() < 3) {
            etNombre.setError("El nombre es muy corto");
            valido = false;
        }

        // el telefono puede empezar con + y tener entre 8 y 12 numeros
        if (!telefono.matches("\\+?[0-9]{8,12}")) {
            etTelefono.setError("Teléfono no válido, ej: +56912345678");
            valido = false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            etCorreo.setError("Correo no válido");
            valido = false;
        }

        if (direccion.isEmpty()) {
            etDireccion.setError("Ingresa una dirección");
            valido = false;
        }

        // la web es opcional, solo se revisa si escribieron algo
        if (!web.isEmpty() && !Patterns.WEB_URL.matcher(web).matches()) {
            etSitioWeb.setError("Sitio web no válido");
            valido = false;
        }

        if (!valido) {
            return;
        }

        // todo ok, le devuelvo el contacto a MainActivity
        Contacto contacto = new Contacto(nombre, telefono, correo, direccion, web, fotoGuardada);
        Intent resultado = new Intent();
        contacto.agregarAIntent(resultado);
        setResult(RESULT_OK, resultado);
        finish();
    }

    // para no perder la foto si se gira la pantalla
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("foto", fotoGuardada);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
