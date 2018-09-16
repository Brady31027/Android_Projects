package com.ojsb.webviewtest;

import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewStub;
import android.view.ViewTreeObserver;
import android.webkit.WebView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;


public class MainActivity extends AppCompatActivity {


    private final static long TIME_OUT_MS = 1000;
    private final int mTotalDrawCount = 1;
    private final Object mLock = new Object();

    private List<WebViewTestCase> mTestCases;
    private View mView;
    private View mViewWrapper;
    private CanvasClient mCanvasClient;
    private int mDrawCount = 0;
    private boolean mOnTv;
    private Handler mHandler;
    private WebView mWebview;
    private CountDownLatch mFence;

    private ViewTreeObserver.OnDrawListener mOnDrawListener = new ViewTreeObserver.OnDrawListener() {
        @Override
        public void onDraw() {
            if (++mDrawCount <= mTotalDrawCount) {
                Log.v("[MTK-GPU]", "frame "+String.valueOf(mDrawCount)+" is drawn");
                mView.postInvalidate();
                return;
            }

            mView.post(new Runnable() {
                @Override
                public void run() {
                    mView.getViewTreeObserver().removeOnDrawListener(mOnDrawListener);
                }
            });
        }
    };


    private void notifyOnDrawCompleted() {
        mView.getViewTreeObserver().addOnDrawListener(mOnDrawListener);
        mView.invalidate();
    }

    public class RenderSpecHandler extends Handler {
        public static final int MSG_LAYOUT = 1;
        public static final int MSG_CANVAS = 2;

        public void handleMessage(Message message) {
            Log.d("[MTK-GPU]", "[Handler] received message "+message.what);

            setContentView(R.layout.activity_main); // test container
            ViewStub stub = (ViewStub) findViewById(R.id.content_stub);
            mViewWrapper = findViewById(R.id.content_wrapper);

            switch (message.what) {
                case MSG_LAYOUT:
                    stub.setLayoutResource(message.arg1);
                    mView = stub.inflate();
                    mViewWrapper.setLayerType(message.arg2, null);
                    WebView webview = (WebView)findViewById(R.id.webview);
                    WebviewHelper helper = new WebviewHelper(webview, (CountDownLatch)message.obj);
                    helper.loadData("<body style=\"background-color:blue\">");
                    notifyOnDrawCompleted();
                    break;
                case MSG_CANVAS:
                    stub.setLayoutResource(R.layout.canvas_client_view);
                    mView = stub.inflate();
                    ((CanvasClientView) mView).setCanvasClient((CanvasClient) (message.obj));
                    mViewWrapper.setLayerType(message.arg1, null);
                    notifyOnDrawCompleted();
                    break;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toastDeviceType();
        setToFullScreen();
        mockFakeDate();
        setHandler();

        sendSignalToHandler();
    }

    private void toastDeviceType() {
        int uiMode = getResources().getConfiguration().uiMode;
        mOnTv = (uiMode & Configuration.UI_MODE_TYPE_MASK) == Configuration.UI_MODE_TYPE_TELEVISION;
        if (mOnTv) {
            Toast.makeText(this, "TV", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Mobile", Toast.LENGTH_SHORT).show();
        }
    }

    private void setToFullScreen() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    private void mockFakeDate() {
        mTestCases = new ArrayList<>();

        // prerequisite: create a canvasClient
        // in our case, we don't have to compare to the golden image,
        // thus we don't add canvas to taskqueue no matter it's rendered by CPU or GPU
        CanvasClient canvasClient = new CanvasClient() {
            @Override
            public void draw(Canvas canvas, int width, int height) {
                Paint paint = new Paint();
                paint.setAntiAlias(false);
                paint.setColor(Color.RED);
                canvas.drawOval(0, 0, width, height, paint);
            }
        };


        // hardware rendered layout
        CountDownLatch hwFence = new CountDownLatch(1);
        mTestCases.add(new WebViewTestCase(R.layout.circle_clipped_webview, true, hwFence));

        // software rendered layout
        // CountDownLatch swFence = new CountDownLatch(1);
        // mTestCases.add(new WebViewTestCase(R.layout.circle_clipped_webview, false, swFence));
    }

    private void setHandler() {
        mHandler = new RenderSpecHandler();
    }

    private void sendSignalToHandler() {
        for (WebViewTestCase testCase : mTestCases) {
            enqueueRenderSpecAndWait(testCase.layoutID, testCase.canvasClient,
                    testCase.useHardware, testCase.readyFence);
        }
    }


    private void enqueueRenderSpecAndWait(int layoutId, CanvasClient canvasClient, boolean useHardware, CountDownLatch readyFence) {

        int layerType = useHardware? View.LAYER_TYPE_NONE: View.LAYER_TYPE_SOFTWARE;

        synchronized (mLock) {
            if (canvasClient != null) {
                mHandler.obtainMessage(RenderSpecHandler.MSG_CANVAS, layerType,
                        0 /*dummy*/, canvasClient).sendToTarget();
            } else {
                mHandler.obtainMessage(RenderSpecHandler.MSG_LAYOUT, layoutId,
                        layerType, readyFence).sendToTarget();
            }
            try {
                mLock.wait(TIME_OUT_MS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
