package com.ojsb.vectordrawablescaletest;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;
import android.os.Build;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by brady on 2017/6/16.
 */

public class MySurfaceView extends SurfaceView implements SurfaceHolder.Callback {

    static boolean dumpPNG = true;
    static Handler handler = new Handler();

    public MySurfaceView(Context context) {
        super(context);
        getHolder().addCallback(this);
    }
    public MySurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        getHolder().addCallback(this);
    }
    public MySurfaceView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        getHolder().addCallback(this);
    }

    private void updateStatus(final String str) {
        handler.post(new Runnable(){
            public void run(){
                Toast.makeText(MySurfaceView.this.getContext(), str, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private Bitmap getBitmapFromDrawable(VectorDrawable vectorDrawable) {
        Bitmap bm = Bitmap.createBitmap(64, 64, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bm);
        canvas.drawColor(Color.WHITE);
        vectorDrawable.setBounds(0, 0, 64, 64);
        vectorDrawable.draw(canvas);
        return bm;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Drawable drawable = ContextCompat.getDrawable(getContext(), R.drawable.vector_icon_create);
        Bitmap icon = getBitmapFromDrawable((VectorDrawable)drawable);
        canvas.drawBitmap(icon, 0, 0, new Paint());
    }

    private Bitmap screenShot(SurfaceView surfaceView) {
        SyncPixelCopy copy = new SyncPixelCopy();
        Bitmap dstBitmap = Bitmap.createBitmap(64, 64, Bitmap.Config.ARGB_8888);

        int copyResult = copy.request(surfaceView, dstBitmap);
        Log.d("PixelCopySurfaceView", String.valueOf(copyResult));
        return dstBitmap;
    }

    private void selfie() {

        final SurfaceView surfaceView = (SurfaceView) findViewById(R.id.surfaceView);

        if (android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.N_MR1) {
            try {
                Thread.sleep(500);
            } catch (Exception e) {
                Log.e("Sleep", "Cannot Sleep");
            }
            //final Bitmap bitmap = screenShot(surfaceView);
        }



        new Thread() {
            public void run() {
                try {
                    // sleep a bit to wait for rendering
                    if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.N_MR1) {
                        Thread.sleep(1000);

                        updateStatus("Selfie");

                        Thread.sleep(3000); // wait for 3 sec for the toast dismissing
                        Log.e("Selfie", "Capturing");
                    }

                    Bitmap bitmap = screenShot(surfaceView);
                    
                    if (dumpPNG) {
                        String outputFolder = MySurfaceView.this.getContext().getExternalFilesDir(null).getAbsolutePath();
                        saveVectorDrawableIntoPNG(bitmap, outputFolder, "SurfaceView");
                    }

                } catch (Exception e) {
                    Log.e("Thread", "Cannot get rect info in another thread");
                }
            }
        }.start();

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Canvas canvas = null;
        try {
            canvas = holder.lockCanvas(null);
            synchronized (holder) {
                onDraw(canvas);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (canvas != null) {
                holder.unlockCanvasAndPost(canvas);
                selfie();
            }
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    private void saveVectorDrawableIntoPNG(Bitmap bitmap, String outputFolder, String filename) throws IOException {
        FileOutputStream out = null;
        try {
            File folder = new File (outputFolder);
            if (!folder.exists()) folder.mkdir();
            String outputFilename = outputFolder + "/" + filename + ".png";

            Log.d("PNG", "Save to "  + outputFilename);

            File outputFile = new File(outputFilename);
            if (!outputFile.exists()) outputFile.createNewFile();
            else outputFile.delete();

            out = new FileOutputStream(outputFile, false);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            Log.d("PNG", "Save to "+ outputFilename + " successfully");
        } catch (Exception e) {
            Log.e("PNG", "exception arised while saving vector to png");
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }
}
