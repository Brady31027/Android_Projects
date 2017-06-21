package com.ojsb.copypixel;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private ImageView srcImageView1;
    private ImageView srcImageView2;
    private ImageView srcImageView3;
    private ImageView dstImageView1;
    private ImageView dstImageView2;
    private ImageView dstImageView3;
    private Rect srcRect1;
    private Rect srcRect2;
    private Rect srcRect3;
    private Bitmap bitmap1;
    private Bitmap bitmap2;
    private Bitmap bitmap3;
    private String outputFolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initAndSetupView();

        // Do 2 things
        // 1. create a new thread to get the srcRect
        // 2. display images and save images to sdcard
        copyPixelAndSaveAsPNG();
    }

    private void initAndSetupView() {
        srcImageView1 = (ImageView) findViewById(R.id.srcImageView1);
        srcImageView2 = (ImageView) findViewById(R.id.srcImageView2);
        srcImageView3 = (ImageView) findViewById(R.id.srcImageView3);
        dstImageView1 = (ImageView) findViewById(R.id.dstImageView1);
        dstImageView2 = (ImageView) findViewById(R.id.dstImageView2);
        dstImageView3 = (ImageView) findViewById(R.id.dstImageView3);
        srcRect1 = new Rect();
        srcRect2 = new Rect();
        srcRect3 = new Rect();
        outputFolder = getExternalFilesDir(null).getAbsolutePath();
    }

    private Bitmap selfie(Rect srcRect) {
        SyncPixelCopy copy = new SyncPixelCopy();
        Bitmap dest = Bitmap.createBitmap(srcRect.width(), srcRect.height(), Bitmap.Config.ARGB_8888);
        int copyResult = copy.request(getWindow(), srcRect, dest);
        Log.d("Selfie", String.valueOf(copyResult));
        return dest;
    }


    private void copyPixelAndSaveAsPNG() {
        new Thread() {
            public void run() {
                try {
                    Thread.sleep(1000);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            boolean srcRectResult1 = srcImageView1.getGlobalVisibleRect(srcRect1);
                            boolean srcRectResult2 = srcImageView2.getGlobalVisibleRect(srcRect2);
                            boolean srcRectResult3 = srcImageView3.getGlobalVisibleRect(srcRect3);
                            Log.d("Rect", "getGlobalVisibleRect return " + String.valueOf(srcRectResult1) + " : " +
                                    String.valueOf(srcRect1.left)+ ", "+ String.valueOf(srcRect1.right)+  ", "+
                                    String.valueOf(srcRect1.top) + ", "+ String.valueOf(srcRect1.bottom));
                            Log.d("Rect", "getGlobalVisibleRect return " + String.valueOf(srcRectResult2) + " : " +
                                    String.valueOf(srcRect2.left)+ ", "+ String.valueOf(srcRect2.right)+  ", "+
                                    String.valueOf(srcRect2.top) + ", "+ String.valueOf(srcRect2.bottom));
                            Log.d("Rect", "getGlobalVisibleRect return " + String.valueOf(srcRectResult3) + " : " +
                                    String.valueOf(srcRect3.left)+ ", "+ String.valueOf(srcRect3.right)+  ", "+
                                    String.valueOf(srcRect3.top) + ", "+ String.valueOf(srcRect3.bottom));
                        }
                    });
                } catch (InterruptedException e) {
                    Log.e("Thread", "Cannot get rect info in another thread");
                }

                bitmap1 = selfie(srcRect1);
                bitmap2 = selfie(srcRect2);
                bitmap3 = selfie(srcRect3);

                try {
                    Thread.sleep(1000);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                dstImageView1.setImageBitmap(bitmap1);
                                dstImageView2.setImageBitmap(bitmap2);
                                dstImageView3.setImageBitmap(bitmap3);

                                saveBitmapIntoPNG(bitmap1, outputFolder, "img1");
                                saveBitmapIntoPNG(bitmap2, outputFolder, "img2");
                                saveBitmapIntoPNG(bitmap3, outputFolder, "img3");
                            } catch (IOException e) {
                                Log.e("Thread", "Cannot save as PNG files");
                            }
                        }
                    });
                } catch (InterruptedException e) {
                    Log.e("Thread", "Cannot execute uithread runable");
                }

            }
        }.start();
    }

    private void saveBitmapIntoPNG(Bitmap bitmap, String outputFolder, String filename) throws IOException {
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
