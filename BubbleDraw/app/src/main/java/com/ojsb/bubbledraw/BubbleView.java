package com.ojsb.bubbledraw;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by brady on 2017/4/16.
 */

@SuppressLint("AppCompatCustomView")
public class BubbleView extends ImageView {

    private List<Bubble> bubbleList;
    private final int DELAY = 16; // 60 fps
    private Paint myPaint = new Paint();
    private Handler handler;

    private class Bubble{
        private int x, y, size, xspeed, yspeed;
        private int color; // here is what Android different from Java

        public Bubble(int newX, int newY, int newSize) {
            x = newX;
            y = newY;
            size = newSize;
            color = Color.argb(
                    (int) (Math.random() * 256),
                    (int) (Math.random() * 256),
                    (int) (Math.random() * 256),
                    (int) (Math.random() * 256));
            xspeed = (int) (Math.random() * 20 + 5);
            yspeed = (int) (Math.random() * 20 + 5);
        }

        private void update() {
            x += xspeed;
            y += yspeed;

            if (x + size/2 < 0 || x + size/2 > getWidth()) {
                xspeed = -xspeed;
            }
            if (y + size/2 < 0 || y + size/2 > getHeight()) {
                yspeed = -yspeed;
            }
        }
    }

    public BubbleView (Context context, AttributeSet attrs){
        super(context, attrs);
        bubbleList = new ArrayList<Bubble>();
        myPaint.setColor(Color.WHITE);
        handler = new Handler();
        this.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent e) {
                int x = (int)e.getX();
                int y = (int)e.getY();
                int size = (int)(Math.random() * 50 + 50);
                Bubble b = new Bubble(x, y, size);
                bubbleList.add(b);
                return true;
            }
        });
    }

    private Runnable r = new Runnable() {
        @Override
        public void run(){
            for (Bubble b: bubbleList) {
                b.update();
            }
            invalidate();
        }
    };

    protected void onDraw(Canvas canvas){
        for (Bubble b : bubbleList) {
            myPaint.setColor(b.color);
            canvas.drawOval(b.x - b.size/2, b.y - b.size/2, b.x + b.size/2, b.y + b.size/2, myPaint);
        }
        handler.postDelayed(r, DELAY);
    }
}
