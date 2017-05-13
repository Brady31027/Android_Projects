/**
 *
 * CobaltFragment
 * Cobalt
 *
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Cobaltians
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 */

package org.cobaltians.cobalt.fragments;

import org.cobaltians.cobalt.Cobalt;
import org.cobaltians.cobalt.R;
import org.cobaltians.cobalt.activities.CobaltActivity;
import org.cobaltians.cobalt.customviews.CobaltSwipeRefreshLayout;
import org.cobaltians.cobalt.customviews.IScrollListener;
import org.cobaltians.cobalt.customviews.OverScrollingWebView;
import org.cobaltians.cobalt.database.LocalStorageJavaScriptInterface;
import org.cobaltians.cobalt.plugin.CobaltPluginManager;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.*;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * {@link Fragment} allowing interactions between native and Web
 * 
 * @author Diane Moebs
 */
public abstract class CobaltFragment extends Fragment implements IScrollListener, SwipeRefreshLayout.OnRefreshListener {

    // TAG
    protected final static String TAG = CobaltFragment.class.getSimpleName();
	
	/*********************************************************
	 * MEMBERS
	 ********************************************************/

	protected Context mContext;

    protected ViewGroup mWebViewContainer;

	protected OverScrollingWebView mWebView;
    protected CobaltSwipeRefreshLayout mSwipeRefreshLayout;

	protected Handler mHandler = new Handler();
	private ArrayList<JSONObject> mWaitingJavaScriptCallsQueue = new ArrayList<JSONObject>();
	
	private boolean mPreloadOnCreate = true;
	private boolean mCobaltIsReady = false;

	private boolean mIsInfiniteScrollRefreshing = false;

	private CobaltPluginManager mPluginManager;

    private boolean mAllowCommit;

    /**************************************************************************************************
	 * LIFECYCLE
	 **************************************************************************************************/

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPluginManager = CobaltPluginManager.getInstance(mContext);
        setRetainInstance(true);
        mAllowCommit = true;
    }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);

        try {
            View view = inflater.inflate(getLayoutToInflate(), container, false);

            setUpViews(view);
            setUpListeners();

            return view;
        }
        catch (InflateException e) {
            if (Cobalt.DEBUG) Log.e(Cobalt.TAG, TAG + " - onCreateView: InflateException");
            e.printStackTrace();
        }

		return null;
	}	

	/**
	 * Restores Web view state.
	 */
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		if (mWebView != null) {
			mWebView.restoreState(savedInstanceState);
		}
	}
	
	@Override
	public void onStart() {
		super.onStart();
		addWebView();
		preloadContent();
	}

    @Override
    public void onResume() {
        mAllowCommit = true;
        super.onResume();
        JSONObject data = ((CobaltActivity) mContext).getDataNavigation();
        sendEvent(Cobalt.JSEventOnPageShown, data, null);
        ((CobaltActivity) mContext).setDataNavigation(null);
    }
	
	@Override
	public void onStop() {
		super.onStop();
		
		// Fragment will rotate or be destroyed, so we don't preload content defined in fragment's arguments again
        mPreloadOnCreate = false;
		
		removeWebViewFromPlaceholder();
	}

    /**
     * Saves the Web view state.
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        mAllowCommit = false;
        super.onSaveInstanceState(outState);
        if (mWebView != null) {
            mWebView.saveState(outState);
        }
    }

    @Override
	public void onDestroy() {
		super.onDestroy();
		
		mPluginManager.onFragmentDestroyed(mContext, this);
	}
    
	/****************************************************************************************
	 * LIFECYCLE HELPERS
	 ***************************************************************************************/

	/**
	 * This method should be overridden in subclasses.
	 * @return Layout id inflated by this fragment
	 */
	protected int getLayoutToInflate() {
		if (isPullToRefreshActive()) return R.layout.fragment_refresh_cobalt;
        else return R.layout.fragment_cobalt;
	}

	/**
	 * Sets up the fragment's properties according to the inflated layout.
	 * This method should be overridden in subclasses. 
	 * @param rootView: parent view
	 */
	protected void setUpViews(View rootView) {
        mWebViewContainer = ((ViewGroup) rootView.findViewById(getWebViewContainerId()));
        if (isPullToRefreshActive()) {
            mSwipeRefreshLayout = ((CobaltSwipeRefreshLayout) rootView.findViewById(getSwipeRefreshContainerId()));
            if (mSwipeRefreshLayout != null) {
                mSwipeRefreshLayout.setColorSchemeResources(R.color.cobalt_blue_bright,
                        R.color.cobalt_blue_light,
                        R.color.cobalt_blue_dark,
                        R.color.cobalt_blue_light);
            }
            else if (Cobalt.DEBUG) Log.w(Cobalt.TAG, TAG + " - setUpViews: SwipeRefresh container not found!");
        }
        if (Cobalt.DEBUG && mWebViewContainer == null) Log.w(Cobalt.TAG, TAG + " - setUpViews: WebView container not found!");
	}

    protected int getWebViewContainerId() {
        return R.id.web_view_container;
    }

    protected int getSwipeRefreshContainerId() {
        return R.id.swipe_refresh_container;
    }

	/**c
	 * Sets up listeners for components inflated from the given layout and the parent view.
	 * This method should be overridden in subclasses.
	 */
	protected void setUpListeners() { }
	
	/**
	 * Called to add the Web view in the placeholder (and creates it if necessary).
	 * This method SHOULD NOT be overridden in subclasses.
	 */
	protected void addWebView() {
        if (mWebView == null) {
            mWebView = new OverScrollingWebView(mContext);
            setWebViewSettings(this);

            if (isPullToRefreshActive()
                    && mSwipeRefreshLayout != null) {
                mSwipeRefreshLayout.setOnRefreshListener(this);
                mSwipeRefreshLayout.setWebView(mWebView);
            }
		}

        if (mWebViewContainer != null) {
            mWebViewContainer.addView(mWebView);
        }
	}

    protected void setWebViewSettings(CobaltFragment javascriptInterface) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) mWebView.setLayerType(View.LAYER_TYPE_HARDWARE ,null);

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD) mWebView.setOverScrollMode(View.OVER_SCROLL_IF_CONTENT_SCROLLS);
        mWebView.setScrollListener(this);
        mWebView.setScrollBarStyle(WebView.SCROLLBARS_INSIDE_OVERLAY);

        // Enables JS
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        // Enables and setups JS local storage
        webSettings.setDomStorageEnabled(true);
        webSettings.setDatabaseEnabled(true);
        //@deprecated since API 19. But calling this method have simply no effect for API 19+
        webSettings.setDatabasePath(mContext.getFilesDir().getParentFile().getPath() + "/databases/");

        // Enables cross-domain calls for Ajax
        allowAjax();

        // Fix some focus issues on old devices like HTC Wildfire
        // keyboard was not properly showed on input touch.
        mWebView.requestFocus(View.FOCUS_DOWN);
        mWebView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_UP:
                        if (! view.hasFocus()) {
                            view.requestFocus();
                        }
                        break;
                    default:
                        break;
                }

                return false;
            }
        });
        
        //Enable Webview debugging from chrome desktop
        if (Cobalt.DEBUG && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }

        // Add JavaScript interface so JavaScript can call native functions.
        mWebView.addJavascriptInterface(javascriptInterface, "Android");
        mWebView.addJavascriptInterface(new LocalStorageJavaScriptInterface(mContext), "LocalStorage");

        WebViewClient webViewClient = new WebViewClient() {

            @Override
            public void onPageFinished(WebView view, String url) {
                executeWaitingCalls();
            }
        };

        mWebView.setWebViewClient(webViewClient);
    }

    @SuppressLint("NewApi")
	private void allowAjax() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            // TODO: see how to restrict only to local files
            mWebView.getSettings().setAllowUniversalAccessFromFileURLs(true);
        }
    }

	private void preloadContent() {
        String page = (getPage() != null) ? getPage() : "index.html";
		
		if (mPreloadOnCreate) {
			loadFileFromAssets(page);
		}
	}

    /**
     * Load the given file in the Web view
     * @param file: file name to load.
     * @warning All application HTML files should be found in the same subfolder in ressource path
     */
    private void loadFileFromAssets(String file) {
        mWebView.loadUrl(Cobalt.getInstance(mContext).getResourcePath() + file);
    }

	/**
	 * Called when fragment is about to rotate or be destroyed
	 * This method SHOULD NOT be overridden in subclasses.
	 */
	private void removeWebViewFromPlaceholder() {
		if (mWebViewContainer != null) {
            mWebViewContainer.removeView(mWebView);
		}
	}
	
	/****************************************************************************************
	 * SCRIPT EXECUTION
	 ***************************************************************************************/
	// TODO: find a way to keep in the queue not sent messages
	/**
	 * Sends script to be executed by JavaScript in Web view
	 * @param jsonObj: JSONObject containing script.
	 */
	private void executeScriptInWebView(final JSONObject jsonObj) {
        if (jsonObj != null) {
			if (mCobaltIsReady && isAdded()) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mWebView.post(new Runnable() {
                            @Override
                            public void run() {
                                // Line & paragraph separators are not JSON compliant but supported by JSONObject
                                String script = jsonObj.toString().replaceAll("[\u2028\u2029]", "");

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                                    // Since KitKat, messages are automatically urldecoded when received from the web. encoding them to fix this.
                                    script = script.replaceAll("%", "%25");
                                }

                                String url = "javascript:cobalt.execute(" + script + ");";
                                mWebView.loadUrl(url);
                            }
                        });
                    }
                });
			}
			else {
				if (Cobalt.DEBUG) Log.i(Cobalt.TAG, TAG + " - executeScriptInWebView: adding message to queue: " + jsonObj);
				mWaitingJavaScriptCallsQueue.add(jsonObj);
			}
		}
        else if (Cobalt.DEBUG) Log.e(Cobalt.TAG, TAG + " - executeScriptInWebView: jsonObj is null!");
	}

	private void executeWaitingCalls() {
		int waitingJavaScriptCallsQueueLength = mWaitingJavaScriptCallsQueue.size();

		for (int i = 0 ; i < waitingJavaScriptCallsQueueLength ; i++) {
			if (Cobalt.DEBUG) Log.i(Cobalt.TAG, TAG + " - executeWaitingCalls: execute " + mWaitingJavaScriptCallsQueue.get(i).toString());
			executeScriptInWebView(mWaitingJavaScriptCallsQueue.get(i));
		}
		
		mWaitingJavaScriptCallsQueue.clear();
	}

	/****************************************************************************************
	 * MESSAGE SENDING
	 ***************************************************************************************/

    /**
     * Calls the Web callback with an object containing response fields
     * @param callbackId: the Web callback.
     * @param data: the object containing response fields
     */
    public void sendCallback(final String callbackId, final JSONObject data) {
        if (callbackId != null
                && callbackId.length() > 0) {
            try {
                JSONObject jsonObj = new JSONObject();
                jsonObj.put(Cobalt.kJSType, Cobalt.JSTypeCallBack);
                jsonObj.put(Cobalt.kJSCallback, callbackId);
                jsonObj.put(Cobalt.kJSData, data);
                executeScriptInWebView(jsonObj);
            }
            catch (JSONException exception) {
                if (Cobalt.DEBUG) Log.e(Cobalt.TAG, TAG + " - sendCallback: JSONException");
                exception.printStackTrace();
            }
        }
        else if (Cobalt.DEBUG) Log.e(Cobalt.TAG, TAG + " - sendCallback: callbackId is null or empty!");
    }

    /**
     * Calls the Web callback with an object containing response fields
     * @param event: the name of the event.
     * @param data: the object containing response fields
     * @param callbackID: the Web callback.
     */
    public void sendEvent(final String event, final JSONObject data, final String callbackID) {
        if (event != null
                && event.length() > 0) {
            try {
                JSONObject jsonObj = new JSONObject();
                jsonObj.put(Cobalt.kJSType, Cobalt.JSTypeEvent);
                jsonObj.put(Cobalt.kJSEvent, event);
                jsonObj.put(Cobalt.kJSData, data);
                jsonObj.put(Cobalt.kJSCallback, callbackID);
                executeScriptInWebView(jsonObj);
            }
            catch (JSONException exception) {
                if (Cobalt.DEBUG) Log.e(Cobalt.TAG, TAG + " - sendEvent: JSONException");
                exception.printStackTrace();
            }
        }
        else if (Cobalt.DEBUG) Log.e(Cobalt.TAG, TAG + " - sendEvent: event is null or empty!");
    }

    /**
     * Calls the Web callback with an object containing response fields
     * @param plugin: the name of the plugin.
     * @param data: the object containing response fields
     * @param callbackID: the Web callback.
     */
    public void sendPlugin(final String plugin, final JSONObject data, final String callbackID) {
        if (plugin != null
            && plugin.length() > 0) {
            try {
                JSONObject jsonObj = new JSONObject();
                jsonObj.put(Cobalt.kJSType, Cobalt.JSTypePlugin);
                jsonObj.put(Cobalt.kJSPluginName, plugin);
                jsonObj.put(Cobalt.kJSData, data);
                jsonObj.put(Cobalt.kJSCallback, callbackID);
                executeScriptInWebView(jsonObj);
            }
            catch (JSONException exception) {
                if (Cobalt.DEBUG) Log.e(Cobalt.TAG, TAG + " - sendPlugin: JSONException");
                exception.printStackTrace();
            }
        }
        else if (Cobalt.DEBUG) Log.e(Cobalt.TAG, TAG + " - sendPlugin: plugin is null or empty!");
    }

    /**
     * Calls the Web callback with an object containing response fields
     * @param message: the object containing response fields
     */
    public void sendMessage(final JSONObject message) {
        if (message != null) {
            executeScriptInWebView(message);
        }
        else if (Cobalt.DEBUG) Log.e(Cobalt.TAG, TAG + " - sendMessage: message is null !");
    }

	/****************************************************************************************
	 * MESSAGE HANDLING
	 ***************************************************************************************/
	/**
	 * This method is called when the JavaScript sends a message to the native side.
	 * This method should be overridden in subclasses.
	 * @param message : the JSON-message sent by JavaScript.
	 * @return true if the message was handled by the native, false otherwise
	 * @details some basic operations are already implemented : navigation, logs, toasts, native alerts, web alerts
	 * @details this method may be called from a secondary thread.
	 */
	// This method must be public !!!
	@JavascriptInterface
	public boolean onCobaltMessage(String message) {
		try {
			final JSONObject jsonObj = new JSONObject(message);
			
			// TYPE
			if (jsonObj.has(Cobalt.kJSType)) {
				String type = jsonObj.getString(Cobalt.kJSType);

                final JSONObject data;
                String callback;
                String action;

                switch (type) {
                    // CALLBACK
                    case Cobalt.JSTypeCallBack:
                        String callbackID = jsonObj.getString(Cobalt.kJSCallback);
                        data = jsonObj.optJSONObject(Cobalt.kJSData);
                        return handleCallback(callbackID, data);
                    // COBALT IS READY
                    case Cobalt.JSTypeCobaltIsReady:
                        String versionWeb = jsonObj.optString(Cobalt.kJSVersion, null);
                        String versionNative = getResources().getString(R.string.version_name);
                        if (versionWeb != null && !versionWeb.equals(versionNative))
                            Log.e(TAG, "Warning : Cobalt version mismatch : Android Cobalt version is " + versionNative + " but Web Cobalt version is " + versionWeb + ". You should fix this. ");
                        onCobaltIsReady();
                        return true;
                    // EVENT
                    case Cobalt.JSTypeEvent:
                        String event = jsonObj.getString(Cobalt.kJSEvent);
                        data = jsonObj.optJSONObject(Cobalt.kJSData);
                        callback = jsonObj.optString(Cobalt.kJSCallback, null);
                        return handleEvent(event, data, callback);
                    // INTENT
                    case Cobalt.JSTypeIntent:
                        action = jsonObj.getString(Cobalt.kJSAction);

                        // OPEN EXTERNAL URL
                        if (action.equals(Cobalt.JSActionIntentOpenExternalUrl)) {
                            data = jsonObj.getJSONObject(Cobalt.kJSData);
                            String url = data.getString(Cobalt.kJSUrl);
                            openExternalUrl(url);

                            return true;
                        }
                        // UNHANDLED INTENT
                        else {
                            onUnhandledMessage(jsonObj);
                            break;
                        }
                    // LOG
                    case Cobalt.JSTypeLog:
                        String text = jsonObj.getString(Cobalt.kJSValue);
                        Log.d(Cobalt.TAG, "JS LOG: " + text);
                        return true;
                    // NAVIGATION
                    case Cobalt.JSTypeNavigation:
                        action = jsonObj.getString(Cobalt.kJSAction);

                        String page;
                        String controller;

                        switch (action) {
                            // PUSH
                            case Cobalt.JSActionNavigationPush:
                                data = jsonObj.getJSONObject(Cobalt.kJSData);
                                push(data);
                                return true;
                            // POP
                            case Cobalt.JSActionNavigationPop:
                                data = jsonObj.optJSONObject(Cobalt.kJSData);
                                if (data != null) {
                                    page = data.optString(Cobalt.kJSPage, null);
                                    controller = data.optString(Cobalt.kJSController, null);
                                    JSONObject dataToPop = data.optJSONObject(Cobalt.kJSData);
                                    if (page != null) pop(controller, page, dataToPop);
                                    else pop(dataToPop);
                                }
                                else pop();
                                return true;
                            // MODAL
                            case Cobalt.JSActionNavigationModal:
                                data = jsonObj.getJSONObject(Cobalt.kJSData);
                                String callbackId = jsonObj.optString(Cobalt.kJSCallback, null);
                                presentModal(data, callbackId);
                                return true;
                            // DISMISS
                            case Cobalt.JSActionNavigationDismiss:
                                // TODO: not present in iOS
                                data = jsonObj.getJSONObject(Cobalt.kJSData);
                                controller = data.getString(Cobalt.kJSController);
                                page = data.getString(Cobalt.kJSPage);
                                JSONObject dataForDissmiss = data.optJSONObject(Cobalt.kJSData);
                                dismissModal(controller, page, dataForDissmiss);
                                return true;
                            // REPLACE
                            case Cobalt.JSActionNavigationReplace:
                                data = jsonObj.getJSONObject(Cobalt.kJSData);
                                replace(data);
                                return true;
                            // UNHANDLED NAVIGATION
                            default:
                                onUnhandledMessage(jsonObj);
                                break;
                        }
                        break;
                    // PLUGIN
                    case Cobalt.JSTypePlugin:
                        mPluginManager.onMessage(mContext, this, jsonObj);
                        break;
                    // UI
                    case Cobalt.JSTypeUI:
                        String control = jsonObj.getString(Cobalt.kJSUIControl);
                        data = jsonObj.getJSONObject(Cobalt.kJSData);
                        callback = jsonObj.optString(Cobalt.kJSCallback, null);
                        return handleUi(control, data, callback);
                    // WEB LAYER
                    case Cobalt.JSTypeWebLayer:
                        action = jsonObj.getString(Cobalt.kJSAction);

                        // SHOW
                        if (action.equals(Cobalt.JSActionWebLayerShow)) {
                            data = jsonObj.getJSONObject(Cobalt.kJSData);

                            mHandler.post(new Runnable() {

                                @Override
                                public void run() {
                                    showWebLayer(data);
                                }
                            });

                            return true;
                        }
                        // UNHANDLED WEB LAYER
                        else {
                            onUnhandledMessage(jsonObj);
                            break;
                        }
                    // UNHANDLED TYPE
                    default:
                        onUnhandledMessage(jsonObj);
                        break;
                }
			}
			// UNHANDLED MESSAGE
			else {
				onUnhandledMessage(jsonObj);
			}
		} 
		catch (JSONException exception) {
            if (Cobalt.DEBUG) Log.e(Cobalt.TAG, TAG + " - onCobaltMessage: JSONException");
			exception.printStackTrace();
		}
        catch (NullPointerException exception) {
            if (Cobalt.DEBUG) Log.e(Cobalt.TAG, TAG + " - onCobaltMessage: NullPointerException");
            exception.printStackTrace();
        }
        
		return false;
	}
	
	private void onCobaltIsReady() {
		if (Cobalt.DEBUG) Log.i(Cobalt.TAG, TAG + " - onReady - version "+getResources().getString(R.string.version_name));

		mCobaltIsReady = true;
		executeWaitingCalls();
        onReady();
	}

    protected void onReady() { }

	private boolean handleCallback(String callback, JSONObject data) {
        switch(callback) {
            case Cobalt.JSCallbackOnBackButtonPressed:
                try {
                    onBackPressed(data.getBoolean(Cobalt.kJSValue));
                    return true;
                }
                catch (JSONException exception) {
                    if (Cobalt.DEBUG) Log.e(Cobalt.TAG, TAG + " - handleCallback: JSONException");
                    exception.printStackTrace();
                    return false;
                }
            case Cobalt.JSCallbackPullToRefreshDidRefresh:
                mWebView.post(new Runnable() {

                    @Override
                    public void run() {
                        onPullToRefreshDidRefresh();
                    }
                });
                return true;
            case Cobalt.JSCallbackInfiniteScrollDidRefresh:
                mWebView.post(new Runnable() {

                    @Override
                    public void run() {
                        onInfiniteScrollDidRefresh();
                    }
                });
                return true;
            default:
                return onUnhandledCallback(callback, data);
        }
	}
	
	protected abstract boolean onUnhandledCallback(String callback, JSONObject data);
	
	private boolean handleEvent(String event, JSONObject data, String callback) {
		return onUnhandledEvent(event, data, callback);
	}
	
	protected abstract boolean onUnhandledEvent(String event, JSONObject data, String callback);
	
	private boolean handleUi(String control, JSONObject data, String callback) {
		try {
            switch (control) {
                // PICKER
                case Cobalt.JSControlPicker:
                    String type = data.getString(Cobalt.kJSType);

                    // DATE
                    if (type.equals(Cobalt.JSPickerDate)) {
                        JSONObject date = data.optJSONObject(Cobalt.kJSDate);

                        Calendar calendar = Calendar.getInstance();
                        int year = calendar.get(Calendar.YEAR);
                        int month = calendar.get(Calendar.MONTH);
                        int day = calendar.get(Calendar.DAY_OF_MONTH);

                        if (date != null
                            && date.has(Cobalt.kJSYear)
                            && date.has(Cobalt.kJSMonth)
                            && date.has(Cobalt.kJSDay)) {
                            year = date.getInt(Cobalt.kJSYear);
                            month = date.getInt(Cobalt.kJSMonth) - 1;
                            day = date.getInt(Cobalt.kJSDay);
                        }

                        JSONObject texts = data.optJSONObject(Cobalt.kJSTexts);
                        String title = texts.optString(Cobalt.kJSTitle, null);
                        //String delete = texts.optString(Cobalt.kJSDelete, null);
                        String clear = texts.optString(Cobalt.kJSClear, null);
                        String cancel = texts.optString(Cobalt.kJSCancel, null);
                        String validate = texts.optString(Cobalt.kJSValidate, null);

                        showDatePickerDialog(year, month, day, title, clear, cancel, validate, callback);

                        return true;
                    }

                    break;
                // ALERT
                case Cobalt.JSControlAlert:
                    showAlertDialog(data, callback);
                    return true;
                // TOAST
                case Cobalt.JSControlToast:
                    String message = data.getString(Cobalt.kJSMessage);
                    Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
                    return true;
                // BARS
                case Cobalt.JSControlBars:
                    String action = data.getString(Cobalt.kJSAction);
                    switch(action) {
                        // SET BARS
                        case Cobalt.JSActionSetBars:
                            JSONObject bars = data.optJSONObject(Cobalt.kJSBars);
                            setBars(bars);
                            return true;
                        case Cobalt.JSActionSetActionBadge:
                            //TODO: @sebf prefer using get instead of opt for mandatory keys and output error message if an exception throws. Same for all following cases
                            try {
                                final String name = data.getString(Cobalt.kActionName);
                                final String badge = data.getString(Cobalt.kActionBadge);

                                //TODO: @sebf always check for nullability on mContext! Same for all following cases
                                final CobaltActivity activity = (CobaltActivity) mContext;
                                if (activity != null) {
                                    activity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            activity.setBadgeMenuItem(name, badge);
                                        }
                                    });
                                }
                            }
                            catch(JSONException exception) {
                                exception.printStackTrace();
                            }
                            return true;
                        case Cobalt.JSActionSetActionContent:
                            try {
                                final String nameContent = data.getString(Cobalt.kActionName);
                                final JSONObject content = data.getJSONObject(Cobalt.kContent);

                                final CobaltActivity activity = (CobaltActivity) mContext;
                                if (activity != null) {
                                    activity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            activity.setContentMenuItem(nameContent, content);
                                        }
                                    });
                                }
                            }
                            catch(JSONException exception) {
                                exception.printStackTrace();
                            }

                            return true;
                        case Cobalt.JSActionSetBarsVisible:
                            try {
                                final JSONObject visible = data.getJSONObject(Cobalt.kVisible);

                                final CobaltActivity activity = (CobaltActivity) mContext;
                                if (activity != null) {
                                    activity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            activity.setActionBarVisible(visible);
                                        }
                                    });
                                }
                            }
                            catch(JSONException exception) {
                                exception.printStackTrace();
                            }
                            return true;
                        case Cobalt.JSActionSetBarContent:
                            try {
                                final JSONObject barsContent = data.getJSONObject(Cobalt.kContent);

                                final CobaltActivity activity = (CobaltActivity) mContext;
                                if (activity != null) {
                                    activity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            activity.setBarContent(barsContent);
                                        }
                                    });
                                }
                            }
                            catch(JSONException exception) {
                                exception.printStackTrace();
                            }

                            return true;
                        case Cobalt.JSActionSetActionVisible:
                            try {
                                final String actionName = data.getString(Cobalt.kActionName);
                                final boolean actionVisible = data.getBoolean(Cobalt.kVisible);

                                final CobaltActivity activity = (CobaltActivity) mContext;
                                if (activity != null) {
                                    activity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            activity.setActionItemVisible(actionName, actionVisible);
                                        }
                                    });
                                }
                            }
                            catch(JSONException exception) {
                                exception.printStackTrace();
                            }

                            return true;
                        case Cobalt.JSActionSetActionEnabled:
                            try {
                                final String actionNameEnabled = data.getString(Cobalt.kActionName);
                                final boolean actionEnabled = data.getBoolean(Cobalt.kEnabled);

                                final CobaltActivity activity = (CobaltActivity) mContext;
                                if (activity != null) {
                                    activity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            activity.setActionItemEnabled(actionNameEnabled, actionEnabled);
                                        }
                                    });
                                }
                            }
                            catch(JSONException exception) {
                                exception.printStackTrace();
                            }

                            return true;
                        default:
                            break;
                    }
                    break;
                default:
                    break;
            }
		} 
		catch (JSONException exception) {
            if (Cobalt.DEBUG) Log.e(Cobalt.TAG, TAG + " - handleUi: JSONException");
			exception.printStackTrace();
		}
		
		// UNHANDLED UI
		try {
			JSONObject jsonObj = new JSONObject();
			jsonObj.put(Cobalt.kJSType, Cobalt.JSTypeUI);
			jsonObj.put(Cobalt.kJSUIControl, control);
			jsonObj.put(Cobalt.kJSData, data);
			jsonObj.put(Cobalt.kJSCallback, callback);
			onUnhandledMessage(jsonObj);
		}
		catch (JSONException exception) {
            if (Cobalt.DEBUG) Log.e(Cobalt.TAG, TAG + " - handleUi: JSONException");
			exception.printStackTrace();
		}
		
		return false;
	}

    protected void setBars(final JSONObject actionBar) {
        if (mContext != null) {
            Intent intent = ((CobaltActivity) mContext).getIntent();
            Bundle bundle = intent.getExtras();
            if (bundle == null) {
                bundle = new Bundle();
            }
            Bundle extras = bundle.getBundle(Cobalt.kExtras);
            if (extras == null) {
                extras = new Bundle();
                bundle.putBundle(Cobalt.kExtras, extras);
            }

            extras.putString(Cobalt.kBars, actionBar.toString());
            intent.putExtras(bundle);

            ((CobaltActivity) mContext).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ((CobaltActivity) mContext).setupBars(actionBar);
                    ((CobaltActivity) mContext).supportInvalidateOptionsMenu();
                }
            });
        }
    }
	protected abstract void onUnhandledMessage(JSONObject message);
	
	/*****************************************************************************************************************
	 * NAVIGATION
	 ****************************************************************************************************************/

	private void push(JSONObject data) {
        try {
            String page = data.getString(Cobalt.kJSPage);
            String controller = data.optString(Cobalt.kJSController, null);
            JSONObject bars = data.optJSONObject(Cobalt.kJSBars);
            JSONObject dataToPush = data.optJSONObject(Cobalt.kJSData);

            Intent intent = Cobalt.getInstance(mContext).getIntentForController(controller, page);
            if (intent != null) {
                if (bars != null) {
                    Bundle configuration = intent.getBundleExtra(Cobalt.kExtras);
                    configuration.putString(Cobalt.kBars, bars.toString());
                }
                if (dataToPush != null) {
                    intent.putExtra(Cobalt.kJSData, dataToPush.toString());
                }

                mContext.startActivity(intent);
            }
            else if (Cobalt.DEBUG) {
                Log.e(Cobalt.TAG, TAG + " - push: unable to push " + controller + " controller.");
            }
        }
        catch(JSONException exception) {
            if (Cobalt.DEBUG) {
                Log.e(Cobalt.TAG, TAG + " - push: missing mandatory page field.");
            }

            exception.printStackTrace();
        }
	}
	
	private void pop() {
        onBackPressed(true);
    }

    private void pop(JSONObject data) {
        ((CobaltActivity) mContext).dataForPop(data);
        pop();
    }

    private void pop(String controller, String page, JSONObject data) {
        ((CobaltActivity) mContext).popTo(controller, page, data);
    }
	
	private void presentModal(JSONObject data, String callBackID) {
        try {
            String page = data.getString(Cobalt.kJSPage);
            String controller = data.optString(Cobalt.kJSController, null);
            JSONObject bars = data.optJSONObject(Cobalt.kJSBars);
            JSONObject dataForModal = data.optJSONObject(Cobalt.kJSData);

            Intent intent = Cobalt.getInstance(mContext).getIntentForController(controller, page);

            if (intent != null) {
                intent.putExtra(Cobalt.kPushAsModal, true);
                if (bars != null) {
                    Bundle configuration = intent.getBundleExtra(Cobalt.kExtras);
                    configuration.putString(Cobalt.kBars, bars.toString());
                }
                if (dataForModal != null) {
                    intent.putExtra(Cobalt.kJSData, dataForModal.toString());
                }

                mContext.startActivity(intent);

                // Sends callback to store current activity & HTML page for dismiss
                try {
                    JSONObject callbackData = new JSONObject();
                    callbackData.put(Cobalt.kJSPage, getPage());
                    callbackData.put(Cobalt.kJSController, mContext.getClass().getName());
                    sendCallback(callBackID, callbackData);
                }
                catch (JSONException exception) {
                    exception.printStackTrace();
                }
            }
            else if (Cobalt.DEBUG) {
                Log.e(Cobalt.TAG,  TAG + " - presentModal: unable to present modal " + controller + " controller.");
            }
        }
        catch(JSONException exception) {
            if (Cobalt.DEBUG) {
                Log.e(Cobalt.TAG, TAG + " - presentModal: missing mandatory page field.");
            }

            exception.printStackTrace();
        }
	}

	private void dismissModal(String controller, String page, JSONObject dataForDissmiss) {
		try {
			Class<?> pClass = Class.forName(controller);

			// Instantiates intent only if class inherits from Activity
			if (Activity.class.isAssignableFrom(pClass)) {
				Bundle bundle = new Bundle();
				bundle.putString(Cobalt.kPage, page);

				Intent intent = new Intent(mContext, pClass);
				intent.putExtra(Cobalt.kExtras, bundle);
                intent.putExtra(Cobalt.kPopAsModal, true);
                if (dataForDissmiss != null) {
                    intent.putExtra(Cobalt.kJSData, dataForDissmiss.toString());
                }
				NavUtils.navigateUpTo((Activity) mContext, intent);
			}
			else if(Cobalt.DEBUG) Log.e(Cobalt.TAG,  TAG + " - dismissModal: unable to dismiss modal since " + controller + " does not inherit from Activity");
		} 
		catch (ClassNotFoundException exception) {
			if (Cobalt.DEBUG) Log.e(Cobalt.TAG,  TAG + " - dismissModal: " + controller + "not found");
			exception.printStackTrace();
		}
	}

    private void replace(JSONObject data) {
        try {
            String page = data.getString(Cobalt.kJSPage);
            String controller = data.optString(Cobalt.kJSController, null);
            JSONObject bars = data.optJSONObject(Cobalt.kJSBars);
            JSONObject dataForReplace = data.optJSONObject(Cobalt.kJSData);
            boolean animated = data.optBoolean(Cobalt.kJSAnimated);

			boolean clearHistory = data.optBoolean(Cobalt.kJSClearHistory, false);
			
            Intent intent = Cobalt.getInstance(mContext).getIntentForController(controller, page);
            if (intent != null) {
                intent.putExtra(Cobalt.kJSAnimated, animated);
                if (bars != null) {
                    Bundle configuration = intent.getBundleExtra(Cobalt.kExtras);
                    configuration.putString(Cobalt.kBars, bars.toString());
                }
                if (dataForReplace != null) {
                    intent.putExtra(Cobalt.kJSData, dataForReplace.toString());
                }

				if (clearHistory) {
					intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
				}
				
                mContext.startActivity(intent);
                ((Activity) mContext).finish();
            }
            else if (Cobalt.DEBUG) {
                Log.e(Cobalt.TAG,  TAG + " - replace: unable to replace " + controller + " controller.");
            }
        }
        catch(JSONException exception) {
            if (Cobalt.DEBUG) {
                Log.e(Cobalt.TAG, TAG + " - replace: missing mandatory page field.");
            }

            exception.printStackTrace();
        }
    }
	
	/**
	 * Called when onBackPressed event is fired. Asks the Web view for back permission.
	 * This method should NOT be overridden in subclasses.
	 */
	public void askWebViewForBackPermission() {
		sendEvent(Cobalt.JSEventOnBackButtonPressed, null, Cobalt.JSCallbackOnBackButtonPressed);
	}
	
	/**
	 * Called when the Web view allowed or not the onBackPressed event.
	 * @param allowedToBack:    true if the WebView allowed the onBackPressed event
     *                          false otherwise.
	 * @details if allowedToBack is true, the onBackPressed method of the activity will be called.
     * This method should not be overridden in subclasses.
	 */
	protected void onBackPressed(boolean allowedToBack) {
        if (allowedToBack) {
            if (Cobalt.DEBUG) Log.i(Cobalt.TAG, TAG + " - onBackPressed: onBackPressed event allowed by Web view");
            ((CobaltActivity) mContext).back();
        }
        else if (Cobalt.DEBUG) Log.i(Cobalt.TAG, TAG + " - onBackPressed: onBackPressed event denied by Web view");
	}
	
	/***********************************************************************************************************************************
	 * WEB LAYER
	 **********************************************************************************************************************************/
	private void showWebLayer(JSONObject data) {
        try {
            String page = data.getString(Cobalt.kJSPage);
            double fadeDuration = data.optDouble(Cobalt.kJSWebLayerFadeDuration, 0.3);

            Bundle bundle = new Bundle();
            bundle.putString(Cobalt.kPage, page);

            CobaltWebLayerFragment webLayerFragment = getWebLayerFragment();

            if (webLayerFragment != null) {
                webLayerFragment.setArguments(bundle);

                FragmentTransaction fragmentTransition = ((FragmentActivity) mContext).getSupportFragmentManager().beginTransaction();

                if (fadeDuration > 0) {
                    fragmentTransition.setCustomAnimations( android.R.anim.fade_in, android.R.anim.fade_out,
                                                            android.R.anim.fade_in, android.R.anim.fade_out);
                }
                else {
                    fragmentTransition.setTransition(FragmentTransaction.TRANSIT_NONE);
                }

                if (CobaltActivity.class.isAssignableFrom(mContext.getClass())) {
                    // Dismiss current Web layer if one is already shown
                    CobaltActivity activity = (CobaltActivity) mContext;
                    Fragment currentFragment = activity.getSupportFragmentManager().findFragmentById(activity.getFragmentContainerId());
                    if (currentFragment != null
                        && CobaltWebLayerFragment.class.isAssignableFrom(currentFragment.getClass())) {
                        ((CobaltWebLayerFragment) currentFragment).dismissWebLayer(null);
                    }

                    // Shows Web layer
                    if (activity.findViewById(activity.getFragmentContainerId()) != null) {
                        fragmentTransition.add(activity.getFragmentContainerId(), webLayerFragment);
                        if (allowFragmentCommit()) fragmentTransition.commit();
                    }
                    else if (Cobalt.DEBUG) Log.e(Cobalt.TAG, TAG + " - showWebLayer: fragment container not found");
                }
            }
            else if (Cobalt.DEBUG) Log.e(Cobalt.TAG, TAG + " - showWebLayer: getWebLayerFragment returned null!");
        }
        catch (JSONException exception) {
            if(Cobalt.DEBUG) Log.e(Cobalt.TAG, TAG + " - showWebLayer: JSONException");
            exception.printStackTrace();
        }
	}
	
	/**
	 * Returns new instance of a {@link CobaltWebLayerFragment}
	 * @return a new instance of a {@link CobaltWebLayerFragment}
	 * This method may be overridden in subclasses if the {@link CobaltWebLayerFragment} must implement customized stuff.
	 */
	protected CobaltWebLayerFragment getWebLayerFragment() {
		return new CobaltWebLayerFragment();
	}
	
	/**
	 * Called from the corresponding {@link CobaltWebLayerFragment} when dismissed.
	 * This method may be overridden in subclasses.
	 */
	public void onWebLayerDismiss(final String page, final JSONObject data) {
		mHandler.post(new Runnable() {

			@Override
			public void run() {
				try {
					JSONObject jsonObj = new JSONObject();
					jsonObj.put(Cobalt.kJSPage, page);
					jsonObj.put(Cobalt.kJSData, data);

					sendEvent(Cobalt.JSEventWebLayerOnDismiss, jsonObj, null);
				} 
				catch (JSONException exception) {
                    if(Cobalt.DEBUG) Log.e(Cobalt.TAG, TAG + " - onWebLayerDismiss: JSONException");
					exception.printStackTrace();
				}
			}
		});
	}

    public boolean allowFragmentCommit() {
        return mAllowCommit;
    }
	
	/******************************************************************************************************************
	 * ALERT DIALOG
	 *****************************************************************************************************************/

	private void showAlertDialog(JSONObject data, final String callback) {		
		try {
			String title = data.optString(Cobalt.kJSAlertTitle);
			String message = data.optString(Cobalt.kJSMessage);
			boolean cancelable = data.optBoolean(Cobalt.kJSAlertCancelable, false);
			JSONArray buttons = data.has(Cobalt.kJSAlertButtons) ? data.getJSONArray(Cobalt.kJSAlertButtons) : new JSONArray();

            AlertDialog alertDialog = new AlertDialog.Builder(mContext)
                                                        .setTitle(title)
                                                        .setMessage(message)
                                                        .create();
            alertDialog.setCancelable(cancelable);

			if (buttons.length() == 0) {
                alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "OK", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (callback != null) {
							try {
								JSONObject data = new JSONObject();
								data.put(Cobalt.kJSAlertButtonIndex, 0);
								sendCallback(callback, data);
							} 
							catch (JSONException exception) {
                                if(Cobalt.DEBUG) Log.e(Cobalt.TAG, TAG + ".AlertDialog - onClick: JSONException");
								exception.printStackTrace();
							}								
						}
					}
				});
			}
			else {
				int buttonsLength = Math.min(buttons.length(), 3);
				for (int i = 0 ; i < buttonsLength ; i++) {
                    int buttonId;

                    switch(i) {
                        case 0:
                        default:
                            buttonId = DialogInterface.BUTTON_NEGATIVE;
                            break;
                        case 1:
                            buttonId = DialogInterface.BUTTON_NEUTRAL;
                            break;
                        case 2:
                            buttonId = DialogInterface.BUTTON_POSITIVE;
                            break;
                    }

                    alertDialog.setButton(buttonId, buttons.getString(i), new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							if (callback != null) {
                                int buttonIndex;
                                switch(which) {
                                    case DialogInterface.BUTTON_NEGATIVE:
                                    default:
                                        buttonIndex = 0;
                                        break;
                                    case DialogInterface.BUTTON_NEUTRAL:
                                        buttonIndex = 1;
                                        break;
                                    case DialogInterface.BUTTON_POSITIVE:
                                        buttonIndex = 2;
                                        break;
                                }

								try {
									JSONObject data = new JSONObject();
									data.put(Cobalt.kJSAlertButtonIndex, buttonIndex);
									sendCallback(callback, data);
								} 
								catch (JSONException exception) {
                                    if (Cobalt.DEBUG) Log.e(Cobalt.TAG, TAG + ".AlertDialog - onClick: JSONException");
									exception.printStackTrace();
								}
							}
						}
					});
				}
			}

            alertDialog.show();
		} 
		catch (JSONException exception) {
            if (Cobalt.DEBUG) Log.e(Cobalt.TAG, TAG + " - showAlertDialog: JSONException");
			exception.printStackTrace();
		}
	}
	
	/*************************************************************************************
     * DATE PICKER
     ************************************************************************************/

    private void showDatePickerDialog(int year, int month, int day, String title, String delete, String cancel, String validate, String callbackID) {
    	Bundle args = new Bundle();
    	args.putInt(CobaltDatePickerFragment.ARG_YEAR, year);
    	args.putInt(CobaltDatePickerFragment.ARG_MONTH, month);
    	args.putInt(CobaltDatePickerFragment.ARG_DAY, day);
    	args.putString(CobaltDatePickerFragment.ARG_TITLE, title);
    	args.putString(CobaltDatePickerFragment.ARG_DELETE, delete);
    	args.putString(CobaltDatePickerFragment.ARG_CANCEL, cancel);
    	args.putString(CobaltDatePickerFragment.ARG_VALIDATE, validate);
    	args.putString(CobaltDatePickerFragment.ARG_CALLBACK_ID, callbackID);

        CobaltDatePickerFragment fragment = new CobaltDatePickerFragment();
        fragment.setArguments(args);
        fragment.setListener(this);

        fragment.show(((FragmentActivity) mContext).getSupportFragmentManager(), "datePicker");
    }
    
    protected void sendDate(int year, int month, int day, String callbackID) {
    	try {
            if (year != -1
                && month != -1
                && day != -1) {
                JSONObject date = new JSONObject();
                date.put(Cobalt.kJSYear, year);
                date.put(Cobalt.kJSMonth, ++month);
                date.put(Cobalt.kJSDay, day);

                sendCallback(callbackID, date);
    		}
            else {
                sendCallback(callbackID, null);
            }
		}
        catch (JSONException e) {
            if (Cobalt.DEBUG) Log.e(Cobalt.TAG, TAG + " - sendDate: JSONException");
			e.printStackTrace();
		}
    }

    /********************************************************
     * OPEN EXTERNAL URL
     ********************************************************/

    private void openExternalUrl(String url) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
    }

    /******************************************************************************************************************************
	 * PULL TO REFRESH
	 *****************************************************************************************************************************/
	
	/**
	 * Set the four colors used in the progress animation from color resources.
     * The first color will also be the color of the bar that grows in response to a user swipe gesture.
     * Must be called only after super.onStart().
	 * @param colorResource1 the first color resource
	 * @param colorResource2 the second color resource
	 * @param colorResource3 the third color resource
	 * @param colorResource4 the last color resource
	 */
	protected void setRefreshColorScheme(int colorResource1, int colorResource2, int colorResource3, int colorResource4) {
        if (mSwipeRefreshLayout != null) mSwipeRefreshLayout.setColorSchemeResources(colorResource1, colorResource2, colorResource3, colorResource4);
        else if (Cobalt.DEBUG) Log.e(Cobalt.TAG, TAG + " - setColorScheme: Pull-to-refresh must be active and method called after super.onStart()!");
	}

    @Override
    public void onRefresh() {
        refreshWebView();
    }

    private void refreshWebView() {
		mHandler.post(new Runnable() {

			@Override
			public void run() {
                sendEvent(Cobalt.JSEventPullToRefresh, null, Cobalt.JSCallbackPullToRefreshDidRefresh);
			}
		});
	}
	
	private void onPullToRefreshDidRefresh() {
        mSwipeRefreshLayout.setRefreshing(false);
		onPullToRefreshRefreshed();
	}

	/**
	 * This method may be overridden in subclasses.
	 */
	protected void onPullToRefreshRefreshed() { }
	
	/************************************************************************************
	 * INFINITE SCROLL
	 ***********************************************************************************/
	
	@Override
	public void onOverScrolled(int scrollX, int scrollY, int oldscrollX, int oldscrollY) {
        int height = mWebView.getHeight();
        long contentHeight = (long) Math.floor(mWebView.getContentHeight() * mContext.getResources().getDisplayMetrics().density);
        
		if (isInfiniteScrollActive()
            && ! mIsInfiniteScrollRefreshing
            && scrollY >= oldscrollY
            && scrollY + height >= contentHeight - height * getInfiniteScrollOffset() / 100) {
			infiniteScrollRefresh();
		}
	}

	private void infiniteScrollRefresh() {
        mHandler.post(new Runnable() {

            @Override
            public void run() {
                sendEvent(Cobalt.JSEventInfiniteScroll, null, Cobalt.JSCallbackInfiniteScrollDidRefresh);
                mIsInfiniteScrollRefreshing = true;
            }
        });
	}
	
	private void onInfiniteScrollDidRefresh() {
		mIsInfiniteScrollRefreshing = false;
		onInfiniteScrollRefreshed();
	}

	/**
	 * This method may be overridden in subclasses.
	 */
	protected void onInfiniteScrollRefreshed() { }
	
    /******************************************************
	 * CONFIGURATION
	 ******************************************************/
	
	private boolean isPullToRefreshActive() {
		Bundle args = getArguments();
        return args != null && args.getBoolean(Cobalt.kPullToRefresh);
	}
	
	private boolean isInfiniteScrollActive() {
		Bundle args = getArguments();
		return args != null && args.getBoolean(Cobalt.kInfiniteScroll);
	}

    private int getInfiniteScrollOffset() {
        Bundle args = getArguments();
        if (args != null) {
            return args.getInt(Cobalt.kInfiniteScrollOffset);
        }
        else {
            return Cobalt.INFINITE_SCROLL_OFFSET_DEFAULT_VALUE;
        }
    }

    protected String getPage() {
        Bundle args = getArguments();
        if (args != null) {
            return args.getString(Cobalt.kPage);
        }
        else {
            return null;
        }
    }
}
