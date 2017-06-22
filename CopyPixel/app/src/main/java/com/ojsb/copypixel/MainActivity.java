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
    private ImageView dstImageView1;
    private Rect srcRect1;
    private Bitmap bitmap1;
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
        dstImageView1 = (ImageView) findViewById(R.id.dstImageView1);
        srcRect1 = new Rect();
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
                            Log.d("Rect", "getGlobalVisibleRect return " + String.valueOf(srcRectResult1) + " : " +
                                    String.valueOf(srcRect1.left)+ ", "+ String.valueOf(srcRect1.right)+  ", "+
                                    String.valueOf(srcRect1.top) + ", "+ String.valueOf(srcRect1.bottom));

                        }
                    });
                } catch (InterruptedException e) {
                    Log.e("Thread", "Cannot get rect info in another thread");
                }

                bitmap1 = selfie(srcRect1);

                try {
                    Thread.sleep(1000);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                dstImageView1.setImageBitmap(bitmap1);
                                saveBitmapIntoPNG(bitmap1, outputFolder, "img1");
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
