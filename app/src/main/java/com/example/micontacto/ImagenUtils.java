package com.example.micontacto;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.widget.ImageView;

public class ImagenUtils {

    // carga la foto en el ImageView con un hilo aparte (Thread)
    // las fotos de la camara pesan harto y si se cargan en el hilo principal la app se pega
    public static void mostrarFoto(Activity activity, String fotoUri, ImageView imageView) {
        if (fotoUri == null || fotoUri.isEmpty()) {
            imageView.setImageResource(R.drawable.ic_persona);
            return;
        }

        new Thread(() -> {
            Bitmap foto = null;
            try {
                ImageDecoder.Source source = ImageDecoder.createSource(activity.getContentResolver(), Uri.parse(fotoUri));
                // la achico a la mitad para que no ocupe tanta memoria
                foto = ImageDecoder.decodeBitmap(source, (decoder, info, src) -> decoder.setTargetSampleSize(2));
            } catch (Exception e) {
                // pasa si la foto ya no existe, por ejemplo si la borraron de la galeria
            }

            Bitmap resultado = foto;
            // la pantalla solo se puede cambiar desde el hilo principal
            activity.runOnUiThread(() -> {
                if (resultado != null) {
                    imageView.setImageBitmap(resultado);
                } else {
                    imageView.setImageResource(R.drawable.ic_persona);
                }
            });
        }).start();
    }
}
