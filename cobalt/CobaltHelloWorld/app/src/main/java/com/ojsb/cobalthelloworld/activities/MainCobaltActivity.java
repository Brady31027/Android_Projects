package com.ojsb.cobalthelloworld.activities;

import com.ojsb.cobalthelloworld.fragments.MainCobaltFragment;

import org.cobaltians.cobalt.Cobalt;
import org.cobaltians.cobalt.activities.CobaltActivity;
import org.cobaltians.cobalt.fragments.CobaltFragment;

public class MainCobaltActivity extends CobaltActivity {

    protected CobaltFragment getFragment() {
        return Cobalt.getInstance(this).getFragmentForController(MainCobaltFragment.class, "default", "index.html");
    }
}