package com.ojsb.webviewtest;

import android.util.Log;
import android.view.ViewTreeObserver;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.concurrent.CountDownLatch;

/**
 * Created by brady on 2018/9/13.
 */

public final class WebviewHelper {
    private static final int FRAME_COUNT = 1;
    private int mDrawCount = 0;
    private final CountDownLatch mLatch;
    private final WebView mWebView;

    public WebviewHelper(WebView webView, CountDownLatch latch) {
        mLatch = latch;
        mWebView = webView;
        mWebView.setWebViewClient(mClient);
    }

    public void loadData(String data) {
        mWebView.loadData(data, null, null);
    }

    private WebViewClient mClient = new WebViewClient() {
        public void onPageFinished(WebView view, String url) {
            Log.v("[MTK-GPU]", "onPageFinished is called");
            mWebView.postVisualStateCallback(0, mVisualStateCallback);
        }
    };

    private WebView.VisualStateCallback mVisualStateCallback = new WebView.VisualStateCallback() {
        @Override
        public void onComplete(long l) {
            mWebView.getViewTreeObserver().addOnDrawListener(mOnDrawListener);
            mWebView.invalidate();
        }
    };

    private ViewTreeObserver.OnDrawListener mOnDrawListener = new ViewTreeObserver.OnDrawListener() {
        @Override
        public void onDraw() {
            if (++mDrawCount <= FRAME_COUNT) {
                Log.v("[MTK-GPU]", "webview frame "+String.valueOf(mDrawCount)+" is drawn");
                mWebView.postInvalidate();
                return;
            }

            mWebView.post(new Runnable() {
                @Override
                public void run() {
                    mWebView.getViewTreeObserver().removeOnDrawListener(mOnDrawListener);
                    mLatch.countDown();
                }
            });
        }
    };

}
