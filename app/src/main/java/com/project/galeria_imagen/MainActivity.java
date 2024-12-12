package com.project.galeria_imagen;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private Uri photoURI;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ActivityResultLauncher<Intent> cameraFullSizeLauncher;
    private ActivityResultLauncher<Intent> cameraThumbnailLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageView imageView = findViewById(R.id.img);

        // Configurar lanzadores de actividad
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        imageView.setImageURI(uri);
                    }
                });

        cameraFullSizeLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            ImageView imageView = findViewById(R.id.img);
                            if (photoURI != null) {
                                imageView.setImageURI(photoURI);
                            } else {
                                Log.e("ERROR", "No photoURI available");
                            }
                        }
                    }
                });

        cameraThumbnailLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Bitmap thumbnail = (Bitmap) result.getData().getExtras().get("data");
                        imageView.setImageBitmap(thumbnail);
                    }
                });

        // Configurar botones
        Button selectImageButton = findViewById(R.id.buttonGallery);
        selectImageButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            imagePickerLauncher.launch(intent);
        });

        Button photoButton = findViewById(R.id.buttonFullSize);
        photoButton.setOnClickListener(view -> {
            try {
                File photoFile = createImageFile();
                if (photoFile != null) {
                    photoURI = FileProvider.getUriForFile(MainActivity.this,
                            "com.project.galeria_imagen.fileprovider",  // Cambiar esta línea
                            photoFile);

                    Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, photoURI);
                    cameraFullSizeLauncher.launch(intent);
                }
            } catch (IOException e) {
                Log.e("ERROR", "Error creating file", e);
            }
        });

        Button thumbnailPhotoButton = findViewById(R.id.buttonThumbnail);
        thumbnailPhotoButton.setOnClickListener(v -> {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            cameraThumbnailLauncher.launch(intent);
        });
    }

    private File createImageFile() throws IOException {
        // Crear un nombre único para el archivo de imagen
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }
}

