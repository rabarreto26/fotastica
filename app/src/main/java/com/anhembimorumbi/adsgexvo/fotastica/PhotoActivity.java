package com.anhembimorumbi.adsgexvo.fotastica;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;

import org.opencv.*;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Vector;

import static org.opencv.core.Core.convertScaleAbs;
import static org.opencv.imgproc.Imgproc.GaussianBlur;
import static org.opencv.imgproc.Imgproc.Sobel;
import static org.opencv.imgproc.Imgproc.calcHist;

import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.FacebookError;

public class PhotoActivity extends ActionBarActivity {
    private Bitmap imageBitmap;
    final Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);

        imageBitmap=(Bitmap)getIntent().getExtras().get("bitmap");
        ShowBitmap(imageBitmap);
    }

    private void ShowBitmap(Bitmap imagem){
        ImageView img = (ImageView)findViewById(R.id.imgFoto);
        img.setImageBitmap(imagem);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_photo, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_gray) {
            ShowBitmap(toGrayscale(imageBitmap));
            return true;
        }
        if (id == R.id.action_laplace){
            ShowBitmap(toLaplace(imageBitmap));
            return true;
        }
        if (id == R.id.action_negative){
            ShowBitmap(toNegative(imageBitmap));
        }
        if (id == R.id.action_hedge){
            ShowBitmap(toHorizontalEdge(imageBitmap));
        }
        if (id == R.id.action_vedge){
            ShowBitmap(toVerticalEdge(imageBitmap));
        }
        if (id == R.id.action_histogram){
            ShowBitmap(toHistogram(imageBitmap));
        }
        if (id == R.id.action_contrast){
            toContrsat(imageBitmap);
        }

        return true;
    }

    private Bitmap toHorizontalEdge(Bitmap bmpOriginal){
        int l= CvType.CV_8UC1;
        Mat matImage = new Mat();
        Mat src_gray = new Mat();
        Mat grad_x = new Mat();
        Mat abs_grad_x = new Mat();

        int scale = 1;
        int delta = 0;
        int ddepth =  CvType.CV_16S;

        Utils.bitmapToMat(bmpOriginal, matImage);
        Size sz = new Size(3,3);
        GaussianBlur( matImage, matImage, sz, 0, 0, Imgproc.BORDER_DEFAULT);
        Imgproc.cvtColor(matImage, src_gray, Imgproc.COLOR_BGR2GRAY);

        Sobel( src_gray, grad_x, ddepth, 1, 0, 3, scale, delta, Imgproc.BORDER_DEFAULT );
        convertScaleAbs( grad_x, abs_grad_x );

        Bitmap destImage;
        destImage=Bitmap.createBitmap(bmpOriginal);
        Utils.matToBitmap(abs_grad_x,destImage);

        return destImage;
    }

    private Bitmap toVerticalEdge(Bitmap bmpOriginal){
        int l= CvType.CV_8UC1;
        Mat matImage = new Mat();
        Mat src_gray = new Mat();
        Mat grad_y = new Mat();
        Mat abs_grad_y = new Mat();

        int scale = 1;
        int delta = 0;
        int ddepth =  CvType.CV_16S;

        Utils.bitmapToMat(bmpOriginal, matImage);
        Size sz = new Size(3,3);
        GaussianBlur( matImage, matImage, sz, 0, 0, Imgproc.BORDER_DEFAULT);
        Imgproc.cvtColor(matImage, src_gray, Imgproc.COLOR_BGR2GRAY);

        Sobel( src_gray, grad_y, ddepth, 0, 1, 3, scale, delta, Imgproc.BORDER_DEFAULT );
        convertScaleAbs( grad_y, abs_grad_y );

        Bitmap destImage;
        destImage=Bitmap.createBitmap(bmpOriginal);
        Utils.matToBitmap(abs_grad_y,destImage);

        return destImage;
    }

    private Bitmap toLaplace(Bitmap bmpOriginal){
        int l= CvType.CV_8UC1;
        Mat matImage = new Mat();

        Utils.bitmapToMat(bmpOriginal, matImage);

        Mat matImageGrey = new Mat();
        Imgproc.cvtColor(matImage,matImageGrey, Imgproc.COLOR_BGR2GRAY);

        Bitmap destImage;
        destImage=Bitmap.createBitmap(bmpOriginal);

        Mat dst2=new Mat();
        Utils.bitmapToMat(destImage, dst2);

        Mat laplacianImage = new Mat();
        dst2.convertTo(laplacianImage, l);

        Imgproc.Laplacian(matImageGrey, laplacianImage, CvType.CV_8U);
        Utils.matToBitmap(laplacianImage,destImage);

        return destImage;
    }

    private Bitmap toGrayscale(Bitmap bmpOriginal)
    {
        int width, height;
        height = bmpOriginal.getHeight();
        width = bmpOriginal.getWidth();

        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmpOriginal, 0, 0, paint);
        return bmpGrayscale;
    }


    private Bitmap toNegative(Bitmap bmpOriginal)
    {
        int width, height;
        float[] colorMatrix_Negative = {
                -1.0f, 0, 0, 0, 255, //red
                0, -1.0f, 0, 0, 255, //green
                0, 0, -1.0f, 0, 255, //blue
                0, 0, 0, 1.0f, 0 //alpha
        };

        height = bmpOriginal.getHeight();
        width = bmpOriginal.getWidth();

        Bitmap bmpNegative = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmpNegative);
        Paint paint = new Paint();

        ColorFilter colorFilter_Negative = new ColorMatrixColorFilter(colorMatrix_Negative);
        paint.setColorFilter(colorFilter_Negative);
        c.drawBitmap(bmpOriginal, 0, 0, paint);
        return bmpNegative;
    }

    private void toContrsat(Bitmap bmpOriginal) {
        final Bitmap bmpContrast = Bitmap.createBitmap(bmpOriginal);

        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.layout_contrast);
        dialog.setTitle("Contraste e Brilho");

        Button dialogButton = (Button)dialog.findViewById(R.id.btnOk);
        // if button is clicked, close the custom dialog
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SeekBar sb_brilho = (SeekBar)dialog.findViewById(R.id.brilho);
                SeekBar sb_contraste = (SeekBar)dialog.findViewById(R.id.contraste);

                int brilho = sb_brilho.getProgress();
                int contraste = sb_contraste.getProgress();

                float real_contrast = 0;
                float real_bright = 0;

                //o ajuste de contraste na matriz de cores se dá entre os valores 0..10, sendo o valor default = 1
                //Então o valor de 0 a 50 retornado pelo diálogo será de 0..1 para a matriz de cores
                //valor de 50 a 100 retornado pelo diálogo será de 1..10 para a matriz de cores
                //Como os valores não são lineares, é necessário dividir a fórmula em duas partes
                if (contraste<=50){
                    real_contrast = (float)contraste / 50f;
                }else{
                    real_contrast = 1f + (((float)contraste-50f)/(100f-50f))*(10f-1f);
                }

                //o ajuste do brilho na matriz de cores se dá entre os valor -255...255, sendo o valor default = 0
                //Como a range de cores é linear, basta normalizar o retorno do diálogo e calcular o valor final

                real_bright=-255f + ((float)brilho/100f)*(255f-(-255f));

                ColorMatrix cm = new ColorMatrix(new float[]
                        {
                                real_contrast, 0, 0, 0, real_bright,
                                0, real_contrast, 0, 0, real_bright,
                                0, 0, real_contrast, 0, real_bright,
                                0, 0, 0, 1, 0
                        });

                Canvas canvas = new Canvas(bmpContrast);

                Paint paint = new Paint();
                paint.setColorFilter(new ColorMatrixColorFilter(cm));
                canvas.drawBitmap(bmpContrast, 0, 0, paint);

                ShowBitmap(bmpContrast);
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private Bitmap toHistogram(Bitmap bmpOriginal){
        Bitmap destImage = Bitmap.createBitmap(300,300, Bitmap.Config.ARGB_8888);

        float R[] = new float[256];
        float G[] = new float[256];
        float B[] = new float[256];

        float RN[] = new float[256];
        float GN[] = new float[256];
        float BN[] = new float[256];

        float Max = Float.MIN_VALUE;
        float Min = Float.MAX_VALUE;

        int pix[] = new int[bmpOriginal.getWidth()*bmpOriginal.getHeight()];
        bmpOriginal.getPixels(pix,0,bmpOriginal.getWidth(),0,0,bmpOriginal.getWidth(),bmpOriginal.getHeight());

        //Percorre toda a imagem a vai somando as ocorrências das cores vermelha, verde e azul no respectivo array
        for(int i=0;i<=pix.length-1;i++){
            int c=pix[i];
            R[Color.red(c)]++;
            G[Color.green(c)]++;
            B[Color.blue(c)]++;
        }

        /*for(int x=0;x<=bmpOriginal.getWidth()-1;x++){
            for(int y=0;y<=bmpOriginal.getHeight()-1;y++){
                int c=bmpOriginal.getPixel(x,y);
                R[Color.red(c)]++;
                G[Color.green(c)]++;
                B[Color.blue(c)]++;
            }
        }*/

        //Percorre o array e encontra os menores e maiores valores, que serão usados para normalizar as contagens e assim renderizar o histograma
        for(int i=0;i<=255;i++){
            if(R[i]>Max){Max=R[i];}
            if(G[i]>Max){Max=G[i];}
            if(B[i]>Max){Max=B[i];}

            if(R[i]<Min){Min=R[i];}
            if(G[i]<Min){Min=G[i];}
            if(B[i]<Min){Min=B[i];}
        }

        //Normaliza o array
        for(int i=0;i<=255;i++){
            RN[i]= (R[i] - Min) / (Max - Min);
            GN[i]= (G[i] - Min) / (Max - Min);
            BN[i]= (B[i] - Min) / (Max - Min);
        }

        Paint pr = new Paint();
        Paint pg = new Paint();
        Paint pb = new Paint();

        pr.setColor(Color.rgb(255,0,0));
        pr.setStrokeWidth(2);

        pg.setColor(Color.rgb(0,255,0));
        pb.setStrokeWidth(2);

        pb.setColor(Color.rgb(0,0,255));
        pb.setStrokeWidth(2);


        //Renderiza o histograma
        Canvas canvas = new Canvas(destImage);
        canvas.drawARGB(255,0,0,0);

        for(int i=0;i<=254;i++){
            float x1=(float)destImage.getWidth() * ((float)i / 254);
            float x2=(float)destImage.getWidth() * ((float)(i+1) / 254);

            float yr1=(float)destImage.getHeight() - ((float)destImage.getHeight() * RN[i]);
            float yr2=(float)destImage.getHeight() - ((float)destImage.getHeight() * RN[i+1]);

            float yg1=(float)destImage.getHeight() - ((float)destImage.getHeight() * GN[i]);
            float yg2=(float)destImage.getHeight() - ((float)destImage.getHeight() * GN[i+1]);

            float yb1=(float)destImage.getHeight() - ((float)destImage.getHeight() * BN[i]);
            float yb2=(float)destImage.getHeight() - ((float)destImage.getHeight() * BN[i+1]);

            canvas.drawLine(x1,yr1,x2,yr2,pr);
            canvas.drawLine(x1,yg1,x2,yg2,pg);
            canvas.drawLine(x1,yb1,x2,yb2,pb);
        }

        return destImage;
    }

    private void toFacebook(Bitmap bmpOriginal){
    }

}
