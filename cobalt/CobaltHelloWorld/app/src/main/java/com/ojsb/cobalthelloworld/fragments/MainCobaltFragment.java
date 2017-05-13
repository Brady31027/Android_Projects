package com.ojsb.cobalthelloworld.fragments;

import org.cobaltians.cobalt.fragments.CobaltFragment;
import org.json.JSONObject;

/**
 * Created by brady on 2017/5/12.
 */

public class MainCobaltFragment extends CobaltFragment {
    @Override
    protected boolean onUnhandledCallback(String callback, JSONObject data) {
        return false;
    }

    @Override
    protected boolean onUnhandledEvent(String event, JSONObject data, String callback) {
        return false;
    }

    @Override
    protected void onUnhandledMessage(JSONObject message) {

    }
}
