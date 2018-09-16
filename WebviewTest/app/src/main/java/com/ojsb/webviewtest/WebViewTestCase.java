package com.ojsb.webviewtest;

import java.util.concurrent.CountDownLatch;

/**
 * Created by brady on 2018/9/14.
 */

public class WebViewTestCase {

    public int layoutID;
    public boolean useHardware;
    public CanvasClient canvasClient;
    public CountDownLatch readyFence;

    public WebViewTestCase(int layoutId, boolean useHardware, CountDownLatch fence) {
        this.layoutID = layoutId;
        this.useHardware = useHardware;
        this.readyFence = fence;
    }
    public WebViewTestCase(CanvasClient client, boolean useHardware) {
        this.canvasClient = client;
        this.useHardware = useHardware;
    }

}


