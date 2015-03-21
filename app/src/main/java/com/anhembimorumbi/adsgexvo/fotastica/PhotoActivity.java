package com.anhembimorumbi.adsgexvo.fotastica;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

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

public class PhotoActivity extends ActionBarActivity {
    private Bitmap imageBitmap;

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

    private Bitmap toHistogram(Bitmap bmpOriginal){
        Mat img = new Mat();
        Utils.bitmapToMat(bmpOriginal, img);

        List<Mat> imagesList=new ArrayList<>();
        imagesList.add(img);

        int histSizeArray[]={256,256,256};
        int channelArray[]={0,1,2};
        MatOfInt channels=new MatOfInt(channelArray);

        Mat hist=new Mat();
        MatOfInt histSize=new MatOfInt(256, 256, 256);

        float hrangesArray[]={0.0f,255.0f};
        MatOfFloat ranges=new MatOfFloat(0.0f,255.0f, 0.0f, 255.0f, 0.0f, 255.0f);

        Mat histImage = Mat.zeros( 100, (int)histSize.get(0, 0)[0], CvType.CV_8UC1);

        calcHist(imagesList, channels,new Mat(), hist, histSize, ranges);
        Core.normalize(hist,hist);

        for( int i = 0; i < (int)histSize.get(0, 0)[0]; i++ )
        {
            Core.line(
                    histImage,
                    new org.opencv.core.Point( i, histImage.rows() ),
                    new org.opencv.core.Point( i, histImage.rows()-Math.round(hist.get(i,0)[0] )) ,
                    new Scalar( 255, 255, 255),
                    1, 8, 0 );
        }
//(histogram, histogram, 1, histImage.rows() , Core.NORM_MINMAX, -1, new Mat() );


        Bitmap destImage;
        destImage=Bitmap.createBitmap(bmpOriginal);

        Utils.matToBitmap(histImage,destImage);

        return destImage;
    }

}
