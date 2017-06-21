package com.ojsb.layerclear;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends Activity {

    private LinearLayout root;
    private FrameLayout parent;
    private View child;
    private Rect drawRect;
    private TextView debugMsg;

    static boolean dumpAsPNG = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initAndSetupView();
        drawViews();
        verify();
    }

    private void initAndSetupView() {
        root = (LinearLayout) findViewById(R.id.linear_layout);
        root.setBackgroundColor(Color.BLACK);
        parent = (FrameLayout) findViewById(R.id.frame_layout);
        parent.setBackgroundColor(Color.WHITE);
        parent.setDrawingCacheEnabled(true);
        debugMsg = (TextView) findViewById(R.id.debug_message);
        debugMsg.setBackgroundColor(Color.WHITE);
        drawRect = new Rect();
    }

    private void drawViews() {
        child = new View(getApplicationContext());
        child.setBackgroundColor(Color.BLUE);
        child.setTranslationX(10);
        child.setTranslationY(10);
        child.setLayoutParams( new FrameLayout.LayoutParams(50, 50));
        child.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        parent.addView(child);
    }

    private Bitmap selfieByPixelCopy(Rect srcRect) {
        SyncCopy copy = new SyncCopy();
        Bitmap dst = Bitmap.createBitmap(srcRect.width(), srcRect.height(), Bitmap.Config.ARGB_8888);
        int copyResult = copy.request(getWindow(), srcRect, dst);
        Log.d("Selfie", String.valueOf(copyResult));
        return dst;
    }

    private Bitmap selfieByDrawingCache(Rect srcRect){
        Bitmap dst = parent.getDrawingCache();
        return dst;
    }

    private void printMsg(String str) {
        debugMsg.append(str);
    }

    private void _verify(Bitmap bitmapPixelCopy, Bitmap bitmapDrawingCache, int width, int height, int pixelCnt) {
        int pixelCopyWhitePixelCnt = 0;
        int drawingCacheWhitePixelCnt = 0;
        for (int x = 0; x < width; x++)
            for (int y = 0; y < height; y++)
            {
                if (bitmapPixelCopy.getPixel(x, y) == Color.WHITE) {
                    pixelCopyWhitePixelCnt ++;
                }
                if (bitmapDrawingCache.getPixel(x, y) == Color.WHITE) {
                    drawingCacheWhitePixelCnt ++;
                }
            }
        printMsg("\n");
        printMsg("    [Golden] Expect WHITE pixel cnt: "+ String.valueOf(pixelCnt) + "\n");
        printMsg("    [PixelCopy] Actual WHITE pixel cnt: "+ String.valueOf(pixelCopyWhitePixelCnt) + "\n");
        printMsg("    [DrawingCache] Bypass PixelCopy WHITE pixel cnt: " + String.valueOf(drawingCacheWhitePixelCnt)+ "\n");

    }

    private void verify() {

        new Thread() {
            public void run() {
                try {
                    Thread.sleep(500);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            boolean srcRectResult1 = parent.getGlobalVisibleRect(drawRect);
                            Log.d("Rect", "getGlobalVisibleRect return " + String.valueOf(srcRectResult1) + " : " +
                                    String.valueOf(drawRect.left)+ ", "+ String.valueOf(drawRect.right)+  ", "+
                                    String.valueOf(drawRect.top) + ", "+ String.valueOf(drawRect.bottom));
                            Bitmap pixelCopyBitmap = selfieByPixelCopy(drawRect);
                            Bitmap drawingCacheBitmap = selfieByDrawingCache(drawRect);

                            // dump for debugging
                            if (dumpAsPNG) {
                                String outputFolder = getExternalFilesDir(null).getAbsolutePath();
                                try {
                                    saveAsPNG(pixelCopyBitmap, outputFolder, "pixelCopy");
                                    saveAsPNG(drawingCacheBitmap, outputFolder, "drawingCache");
                                } catch (IOException e) {
                                    Log.e("Save", "Cannot save as PNG file");
                                }
                            }

                            int parentWidth = drawRect.right - drawRect.left;
                            int parentHeight = drawRect.bottom - drawRect.top;
                            int childWidth = 50;
                            int childHeight = 50;
                            int goldenPixelCnt = parentWidth * parentHeight - childWidth * childHeight;
                            _verify(pixelCopyBitmap, drawingCacheBitmap, parentWidth, parentHeight, goldenPixelCnt);
                        }
                    });

                } catch (InterruptedException e) {
                    Log.e("Verify", "Cannot create verify thread");
                }
            }
        }.start();
    }

    private void saveAsPNG(Bitmap bitmap, String outputFolder, String filename) throws IOException {
        FileOutputStream out = null;
        try {
            File folder = new File (outputFolder);
            if (!folder.exists()) folder.mkdir();
            String outputFilename = outputFolder + "/" + filename + ".png";

            Log.d("PNG", "Save to "  + outputFilename);

            File outputFile = new File(outputFilename);
            if (!outputFile.exists()) outputFile.createNewFile();
            else {
                Log.d("PNG", "Remove existed png file");
                outputFile.delete();
            }

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
