package com.example.micontacto;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class DetalleActivity extends AppCompatActivity {

    private Contacto contacto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // datos que mando MainActivity con putExtra
        contacto = Contacto.desdeIntent(getIntent());
        if (contacto == null) {
            Toast.makeText(this, "No llegaron los datos del contacto", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        ImageView ivFoto = findViewById(R.id.ivFotoDetalle);
        TextView tvNombre = findViewById(R.id.tvNombreDetalle);
        TextView tvTelefono = findViewById(R.id.tvTelefonoDetalle);
        TextView tvCorreo = findViewById(R.id.tvCorreoDetalle);
        TextView tvDireccion = findViewById(R.id.tvDireccionDetalle);
        TextView tvSitioWeb = findViewById(R.id.tvSitioWebDetalle);
        Button btnLlamar = findViewById(R.id.btnLlamar);
        Button btnCorreo = findViewById(R.id.btnCorreo);
        Button btnMapa = findViewById(R.id.btnMapa);
        Button btnWeb = findViewById(R.id.btnWeb);

        tvNombre.setText(contacto.getNombre());
        tvTelefono.setText(contacto.getTelefono());
        tvCorreo.setText(contacto.getCorreo());
        tvDireccion.setText(contacto.getDireccion());
        if (contacto.getSitioWeb().isEmpty()) {
            tvSitioWeb.setText(R.string.sin_sitio_web);
        } else {
            tvSitioWeb.setText(contacto.getSitioWeb());
        }
        ImagenUtils.mostrarFoto(this, contacto.getFotoUri(), ivFoto);

        btnLlamar.setOnClickListener(v -> llamar());
        btnCorreo.setOnClickListener(v -> enviarCorreo());
        btnMapa.setOnClickListener(v -> verEnMapa());
        btnWeb.setOnClickListener(v -> abrirSitioWeb());
    }

    // implicito 1: abre el marcador con el numero (ACTION_DIAL no pide permiso, no llama solo)
    private void llamar() {
        Intent intent = new Intent(Intent.ACTION_DIAL,
            Uri.parse("tel:" + contacto.getTelefono()));
        abrirOtraApp(intent, "No hay una app de teléfono");
    }

    // implicito 2: abre la app de correo con el destinatario, asunto y mensaje
    private void enviarCorreo() {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:")); // asi solo salen apps de correo
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{contacto.getCorreo()});
        intent.putExtra(Intent.EXTRA_SUBJECT, "Hola " + contacto.getNombre());
        intent.putExtra(Intent.EXTRA_TEXT, "Hola " + contacto.getNombre() + ", te escribo desde MiContacto.");
        abrirOtraApp(intent, "No hay una app de correo instalada");
    }

    // implicito 3: busca la direccion en el mapa
    private void verEnMapa() {
        Uri ubicacion = Uri.parse("geo:0,0?q=" + Uri.encode(contacto.getDireccion()));
        Intent intent = new Intent(Intent.ACTION_VIEW, ubicacion);
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            // si no esta google maps la abro en el navegador
            Uri web = Uri.parse("https://www.google.com/maps/search/?api=1&query=" + Uri.encode(contacto.getDireccion()));
            abrirOtraApp(new Intent(Intent.ACTION_VIEW, web), "No hay app de mapas ni navegador");
        }
    }

    // implicito 4: abre el sitio web en el navegador
    private void abrirSitioWeb() {
        String sitio = contacto.getSitioWeb();
        if (sitio.isEmpty()) {
            Toast.makeText(this, "Este contacto no tiene sitio web", Toast.LENGTH_SHORT).show();
            return;
        }
        // si lo escribieron sin http le agrego https://
        if (!sitio.startsWith("http://") && !sitio.startsWith("https://")) {
            sitio = "https://" + sitio;
        }
        abrirOtraApp(new Intent(Intent.ACTION_VIEW, Uri.parse(sitio)), "No hay un navegador");
    }

    // si no hay ninguna app que pueda abrir el intent, sale un mensaje y la app no se cae
    private void abrirOtraApp(Intent intent, String mensajeError) {
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, mensajeError, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
