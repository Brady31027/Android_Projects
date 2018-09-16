package com.ojsb.webviewtest;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * Created by brady on 2018/9/13.
 */

public class CanvasClientView extends View {

    private CanvasClient mCanvasClient;

    public CanvasClientView(Context context) {
        super(context);
    }

    public CanvasClientView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CanvasClientView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setCanvasClient(CanvasClient canvasClient) {
        mCanvasClient = canvasClient;
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (mCanvasClient == null) {
            Log.e("[MTK-GPU]", "mCanvasClient is null");
        }

        int saveCount = canvas.save();

        canvas.clipRect(0, 0, 90, 90);

        mCanvasClient.draw(canvas, 90, 90);

        canvas.restoreToCount(saveCount);
    }

}
