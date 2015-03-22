package com.anhembimorumbi.adsgexvo.fotastica;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import android.app.Activity;
import android.hardware.Camera.CameraInfo;
import android.util.Log;

public class MainActivity extends ActionBarActivity {
    static{ System.loadLibrary("opencv_java"); }

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private Bitmap imageBitmap;

    public Bitmap getBitmap(){
        return imageBitmap;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        ImageButton button = (ImageButton) findViewById(R.id.btnCamera);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Captura a imagem da camera


                /*Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                }*/

                imageBitmap = ResizeBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.bmpref),300);
                AbreTratamentoImagem();
            }
        });

        return true;
    }

    private Bitmap ResizeBitmap(Bitmap image, int MaxWidth){
        float factor = (float)MaxWidth / (float)image.getWidth();

        int h = (int)(image.getHeight()*factor);
        int w =(int)(image.getWidth()*factor);

        return Bitmap.createScaledBitmap( image, w, h, true);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();

            //Ajusta o tamanho da imagem, proporcionalmente para uma largura de 300 pixels
            imageBitmap = ResizeBitmap((Bitmap) extras.get("data"),300);
            AbreTratamentoImagem();
        }
    }

    void AbreTratamentoImagem(){
        //Abre a activity para tratamento da imagem e finalização

        Intent intent = new Intent(this, PhotoActivity.class);
        intent.putExtra("bitmap",imageBitmap);
        startActivity(intent);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_about) {
            //Abre a activity para mostrar o About
            Intent intent = new Intent(this, AboutActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
