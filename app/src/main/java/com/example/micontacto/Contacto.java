package com.example.micontacto;

import android.content.Intent;

public class Contacto {

    // claves para los putExtra y para SharedPreferences
    public static final String EXTRA_NOMBRE = "nombre";
    public static final String EXTRA_TELEFONO = "telefono";
    public static final String EXTRA_CORREO = "correo";
    public static final String EXTRA_DIRECCION = "direccion";
    public static final String EXTRA_SITIO_WEB = "sitio_web";
    public static final String EXTRA_FOTO = "foto";

    private String nombre;
    private String telefono;
    private String correo;
    private String direccion;
    private String sitioWeb;
    private String fotoUri;

    public Contacto(String nombre, String telefono, String correo, String direccion, String sitioWeb, String fotoUri) {
        this.nombre = nombre;
        this.telefono = telefono;
        this.correo = correo;
        this.direccion = direccion;
        // la web y la foto son opcionales, si vienen null las dejo vacias
        this.sitioWeb = sitioWeb != null ? sitioWeb : "";
        this.fotoUri = fotoUri != null ? fotoUri : "";
    }

    // mete los datos del contacto en el intent
    public void agregarAIntent(Intent intent) {
        intent.putExtra(EXTRA_NOMBRE, nombre);
        intent.putExtra(EXTRA_TELEFONO, telefono);
        intent.putExtra(EXTRA_CORREO, correo);
        intent.putExtra(EXTRA_DIRECCION, direccion);
        intent.putExtra(EXTRA_SITIO_WEB, sitioWeb);
        intent.putExtra(EXTRA_FOTO, fotoUri);
    }

    // arma el contacto con lo que trae el intent, si no viene el nombre devuelve null
    public static Contacto desdeIntent(Intent intent) {
        if (intent == null || intent.getStringExtra(EXTRA_NOMBRE) == null) {
            return null;
        }
        return new Contacto(
                intent.getStringExtra(EXTRA_NOMBRE),
                intent.getStringExtra(EXTRA_TELEFONO),
                intent.getStringExtra(EXTRA_CORREO),
                intent.getStringExtra(EXTRA_DIRECCION),
                intent.getStringExtra(EXTRA_SITIO_WEB),
                intent.getStringExtra(EXTRA_FOTO));
    }

    public String getNombre() {
        return nombre;
    }

    public String getTelefono() {
        return telefono;
    }

    public String getCorreo() {
        return correo;
    }

    public String getDireccion() {
        return direccion;
    }

    public String getSitioWeb() {
        return sitioWeb;
    }

    public String getFotoUri() {
        return fotoUri;
    }
}
