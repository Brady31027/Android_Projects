package com.ojsb.layerclear;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.PixelCopy;
import android.view.PixelCopy.OnPixelCopyFinishedListener;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.Window;

/**
 * Created by brady on 2017/6/21.
 */

public class SyncCopy implements OnPixelCopyFinishedListener {

    private static Handler sHandler;
    static {
        HandlerThread thread = new HandlerThread("PixelCopyHelper");
        thread.start();
        sHandler = new Handler(thread.getLooper());
    }
    private int mStatus = -1;

    public int request(Surface source, Bitmap dest) {
        synchronized (this) {
            PixelCopy.request(source, dest, this, sHandler);
            return getResultLocked();
        }
    }

    public int request(SurfaceView source, Bitmap dest) {
        synchronized (this) {
            PixelCopy.request(source, dest, this, sHandler);
            return getResultLocked();
        }
    }

    public int request(Window source, Bitmap dest) {
        synchronized (this) {
            PixelCopy.request(source, dest, this, sHandler);
            return getResultLocked();
        }
    }

    public int request(Window source, Rect rect, Bitmap dest) {
        synchronized (this) {
            PixelCopy.request(source, rect, dest, this, sHandler);
            return getResultLocked();
        }
    }

    private int getResultLocked() {
        try {
            this.wait(1000);
        } catch (InterruptedException e) {
            Log.e("Copy","PixelCopy request didn't complete within 1s");
        }
        Log.v("AnotherThread", String.valueOf(mStatus));
        return mStatus;
    }

    @Override
    public void onPixelCopyFinished(int copyResult) {
        synchronized (this) {
            mStatus = copyResult;
            this.notify();
        }
    }
}
