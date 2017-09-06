/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.ojsb.leanbacktest;

import android.content.res.Resources.Theme;
import android.os.Bundle;
import android.os.Handler;
import android.support.v17.leanback.app.BackgroundManager;
import android.support.v17.leanback.app.BrowseFragment;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.HeaderItem;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.PresenterSelector;
import android.util.DisplayMetrics;
import android.view.View;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;



class ScrollPatternGenerator {
    private int currentRow;
    private int topRow;
    private int bottomRow;
    private enum ScrollDirection {DOWN, UP};
    private ScrollDirection direction;

    public ScrollPatternGenerator (int topRow, int bottomRow) {
        this.topRow = topRow;
        this.bottomRow = bottomRow;
        direction = ScrollDirection.DOWN;
        currentRow = topRow;
    }

    public int next() {
        if (currentRow == bottomRow) {
            direction = ScrollDirection.UP;
        } else if (currentRow == topRow) {
            direction = ScrollDirection.DOWN;
        }

        if (direction == ScrollDirection.DOWN) {
            currentRow++;
        } else {
            currentRow--;
        }
        return currentRow;
    }
}


public class MainFragment extends BrowseFragment {
    private static final String TAG = "MainFragment";

    private static final int NUM_ROWS = 20;
    private static final int START_ROW = 4;

    private static final int mInitialDelay = 0;
    private static final int mScrollInterval = 200;
    private static final int mScrollCount = 100;
    private static final boolean mShadow = true;

    private final Handler mHandler = new Handler();
    private ArrayObjectAdapter mRowsAdapter;
    private DisplayMetrics mMetrics;
    private BackgroundManager mBackgroundManager;

    private Timer mAutoScrollTimer;
    private int mAutoScrollCount;
    private ScrollPatternGenerator mScrollPatternGenerator;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        buildRowAdapterItems(MovieList.buildMedia(NUM_ROWS));

        prepareBackgroundManager();

        setupUIElements();

        setupEventListeners();

        startAutoScrollTimer(mInitialDelay, mScrollInterval, mScrollCount);
        mScrollPatternGenerator = new ScrollPatternGenerator(START_ROW, NUM_ROWS - 1 - START_ROW);
    }

    @Override
    public void onDestroy() {
        if (null != mAutoScrollTimer) {
            mAutoScrollTimer.cancel();
            mAutoScrollTimer = null;
        }
        super.onDestroy();
    }

    @Override
    public void onStop() {
        mBackgroundManager.release();
        super.onStop();
    }

    public void buildRowAdapterItems(HashMap<String, List<Movie>> data) {

        ListRowPresenter rowPresenter = new ListRowPresenter();
        rowPresenter.setShadowEnabled(mShadow);

        mRowsAdapter = new ArrayObjectAdapter(rowPresenter);
        CardPresenter cardPresenter = new CardPresenter();

        int i = 0;

        for (Map.Entry<String, List<Movie>> entry: data.entrySet()) {
            ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(cardPresenter);
            List<Movie> list = entry.getValue();

            for (int j = 0; j < list.size(); j++) {
                listRowAdapter.add(list.get(j));
            }
            HeaderItem header = new HeaderItem(i, entry.getKey());
            i++;
            mRowsAdapter.add(new ListRow(header, listRowAdapter));
        }
        setAdapter(mRowsAdapter);
    }

    private void prepareBackgroundManager() {

        mBackgroundManager = BackgroundManager.getInstance(getActivity());
        mBackgroundManager.attach(getActivity().getWindow());
        mBackgroundManager.setDrawable(getActivity().getResources().getDrawable(R.drawable.default_background, null/*getContext().getTheme()*/));
        mMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(mMetrics);
    }

    private void setupUIElements() {
        setBadgeDrawable(getActivity().getResources().getDrawable(
             R.drawable.videos_by_google_banner, null /*getContext().getTheme()*/));
        setTitle("Leanback Debug"); // Badge, when set, takes precedent over title
        setHeadersState(HEADERS_ENABLED);
        setHeadersTransitionOnBackEnabled(true);

        Theme theme = getContext().getTheme();

        // set fastLane (or headers) background color
        setBrandColor(getResources().getColor(R.color.fastlane_background, null/*theme*/));
        // set search icon color
        setSearchAffordanceColor(getResources().getColor(R.color.search_opaque, null/*theme*/));

        setHeaderPresenterSelector(new PresenterSelector() {
            @Override
            public Presenter getPresenter(Object item) {
                return new IconHeaderItemPresenter();
            }
        });

    }

    private void setupEventListeners() {
        setOnSearchClickedListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                // nothing happens here
            }
        });
    }

    private class UpdateAutoScrollTask extends TimerTask {
        // TODO
        @Override
        public void run() {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mAutoScrollCount == 0) {
                        mAutoScrollTimer.cancel();
                        return;
                    }
                    setSelectedPosition(mScrollPatternGenerator.next());
                    mAutoScrollCount --;
                }
            });
        }
    }
/*
    private class OldUpdateAutoScrollTask extends TimerTask {
        @Override
        public void run() {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mAutoScrollCount == 0) {
                        mAutoScrollTimer.cancel();
                        return;
                    }
                    if (mAutoScrollCount % 2 == 0) {
                        setSelectedPosition(NUM_ROWS - 1);
                    }else {
                        setSelectedPosition(0);
                    }
                    mAutoScrollCount --;
                }
            });
        }
    }
*/

    private void startAutoScrollTimer(int initialDelay, int interval, int count) {
        if (mAutoScrollTimer != null) {
            mAutoScrollTimer.cancel();
        }
        mAutoScrollCount = count;
        mAutoScrollTimer = new Timer();
        mAutoScrollTimer.schedule(new UpdateAutoScrollTask(), initialDelay, interval);
    }

    public void selectRow(int row) {
        setSelectedPosition(row);
    }

}
