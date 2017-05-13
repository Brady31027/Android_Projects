package org.cobaltians.cobalt.customviews;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import org.cobaltians.cobalt.Cobalt;
import org.cobaltians.cobalt.R;
import org.cobaltians.cobalt.activities.CobaltActivity;
import org.cobaltians.cobalt.font.CobaltFontManager;

import java.lang.ref.WeakReference;

/**
 * Created by sebastienfamel on 10/12/15.
 */
public class ActionViewMenuItem extends RelativeLayout {

    final static String TAG = ActionViewMenuItem.class.getSimpleName();

    protected String mName;
    protected JSONObject mAction;
    protected String mBarsColor;
    protected ActionViewMenuItemListener mListener;
    protected Context mContext;

    protected TextView mBadgeTv;
    protected ImageButton mImageButton;
    protected Button mButton;

    protected LayoutInflater mInflater;

    public ActionViewMenuItem(Context context) {
        super(context);
        mContext = context;
        mInflater = LayoutInflater.from(mContext);
        init();
    }

    public ActionViewMenuItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        mInflater = LayoutInflater.from(mContext);
        init();
    }

    public ActionViewMenuItem(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        mInflater = LayoutInflater.from(mContext);
        init();
    }

    public ActionViewMenuItem(Context context, JSONObject action, String barsColor) {
        super(context);
        mContext = context;
        mInflater = LayoutInflater.from(mContext);

        this.mAction = action;
        this.mBarsColor = barsColor;

        init();
    }

    public void setActionViewMenuItemListener(WeakReference<CobaltActivity> weakActivity) {
        mListener = weakActivity.get();
    }

    protected void init() {
        mInflater.inflate(R.layout.custom_action_view_menu_item, this, true);
        try {
            String title = mAction.getString(Cobalt.kActionTitle);
            String icon = mAction.optString(Cobalt.kActionIcon, null);               // must be "fontKey character"
            String androidIcon = mAction.optString(Cobalt.kActionAndroidIcon, null);
            String color = mAction.optString(Cobalt.kActionColor, mBarsColor);             // default: same as bar color
            boolean visible = mAction.optBoolean(Cobalt.kActionVisible, true);
            boolean enabled = mAction.optBoolean(Cobalt.kActionEnabled, true);
            String badge = mAction.optString(Cobalt.kActionBadge, null);             // if "", hide it

            mImageButton = (ImageButton) findViewById(R.id.image_button_item);
            mButton = (Button) findViewById(R.id.button_item);
            mBadgeTv = (TextView) findViewById(R.id.badge_item);

            if (androidIcon != null || icon != null) {

                int idResource;
                if (androidIcon != null) idResource = getResource(androidIcon);
                else idResource = getResource(icon);

                if (idResource != 0) {
                    try {
                        mImageButton.setColorFilter(Cobalt.parseColor(color));
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            mImageButton.setImageDrawable(mContext.getDrawable(idResource));
                        } else
                            mImageButton.setImageDrawable(mContext.getResources().getDrawable(idResource));
                    }
                    catch(Resources.NotFoundException exception) {
                        exception.printStackTrace();
                    }
                }
                else {
                    int iconColor = CobaltFontManager.DEFAULT_COLOR;

                    try {
                        iconColor = Cobalt.parseColor(color);
                    }
                    catch (IllegalArgumentException exception) {
                        if (Cobalt.DEBUG) {
                            Log.w(Cobalt.TAG, TAG + " - init setImageDrawable : color " + color + " format not supported, use (#)RGB or (#)RRGGBB(AA).");
                        }

                        exception.printStackTrace();
                    }

                    mImageButton.setImageDrawable(CobaltFontManager.getCobaltFontDrawable(mContext, icon, iconColor));
                }

                mImageButton.setEnabled(enabled);

                mImageButton.setVisibility(visible ? VISIBLE : GONE);

                if (badge != null && badge.length()>0) {
                    mBadgeTv.setText(badge);
                    mBadgeTv.setVisibility(VISIBLE);
                    /*
                    //TODO see how to create and update gradientDrawable
                        GradientDrawable drawableBadge = new GradientDrawable();
                        drawableBadge.setShape(GradientDrawable.RECTANGLE);
                        drawableBadge.setCornerRadius(20);
                        drawableBadge.setColor(getHexaColor("#F00"));
                        drawableBadge.setStroke(1, Cobalt.parseColor(color));
                     */
                }
                mImageButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            String name  = mAction.getString(Cobalt.kActionName);
                            ((CobaltActivity)mContext).onPressed(name);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
                // TODO: add toast tooltip OnLongClickListener with title anchored on MenuItem
            }
            else {
                mButton.setText(title);
                if (color != null) {
                    int textColor = 0;
                    try {
                        textColor = Cobalt.parseColor(color);
                    }
                    catch (IllegalArgumentException exception) {
                        if (Cobalt.DEBUG) {
                            Log.w(Cobalt.TAG, TAG + " - init setTextColor : color " + color + " format not supported, use (#)RGB or (#)RRGGBB(AA).");
                        }

                        exception.printStackTrace();
                    }
                    // TODO: @sebf default color to black?
                    mButton.setTextColor(textColor);
                }
                mButton.setEnabled(enabled);
                mButton.setVisibility(visible ? VISIBLE : GONE);
                mButton.setBackgroundColor(Color.TRANSPARENT);
                mButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            String name  = mAction.getString(Cobalt.kActionName);
                            ((CobaltActivity) mContext).onPressed(name);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
                // TODO: apply color for items in overflow popup
            }

            // TODO: find best background to mimic default behavior
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                int[] attrs = {android.R.attr.selectableItemBackgroundBorderless};
                TypedArray styledAttrs = mContext.getTheme().obtainStyledAttributes(attrs);
                Drawable backgroundDrawable = styledAttrs.getDrawable(0);
                setBackground(backgroundDrawable);
            }
            else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                int[] attrs = {android.R.attr.selectableItemBackground};
                TypedArray styledAttrs = mContext.getTheme().obtainStyledAttributes(attrs);
                int backgroundResId = styledAttrs.getResourceId(0, 0);
                setBackgroundResource(backgroundResId);
            }
            else {
                setBackgroundResource(android.R.drawable.menuitem_background);
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // TODO: @sebf You'd better use the CobaltActivity.getResourceIdentifier method or move it into Cobalt class
    protected int getResource(String imageLink) {
        if (!imageLink.contains(":")) {
            return this.getResources().getIdentifier(imageLink,"drawable", mContext.getPackageName());
        }
        else {
            try {
                String [] splitLink = imageLink.split(":");
                String packName = splitLink[0];
                String drawableName = splitLink[1];
                PackageManager manager = mContext.getPackageManager();
                Resources apkResource = manager.getResourcesForApplication(packName);
                return apkResource.getIdentifier(drawableName, "drawable", packName);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    public String getName(){ return mName;}

    public void setActionBadge(String text) {
        // TODO: @sebf it's the same thing!
        if (text.length()>0 && !text.equals("")) {
            mBadgeTv.setText(text);
            mBadgeTv.setVisibility(View.VISIBLE);
        }
        else {
            mBadgeTv.setText("");
            mBadgeTv.setVisibility(View.GONE);
        }
    }

    public void setActionContent(JSONObject content) {
        // TODO: @sebf You're not forced to track the action content because,
        // compared to iOS, where bars are the navigationController ones, so need to be reset at push then re-init at pop,
        // on Android, they're activity ones.
		mAction = content;
		
        String androidIcon = mAction.optString(Cobalt.kActionAndroidIcon, null);
        String title = mAction.optString(Cobalt.kActionTitle, null);
        String icon = mAction.optString(Cobalt.kActionIcon, null);
        String color = mAction.optString(Cobalt.kActionColor, mBarsColor);

        if ((androidIcon != null || icon != null) && mImageButton != null) {
            int idResource;
            if (androidIcon != null)  idResource = getResource(androidIcon);
            else idResource = getResource(icon);
            if (idResource != 0) {
                try {
                    //TODO: @sebf where is the color applied?
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        mImageButton.setImageDrawable(mContext.getDrawable(idResource));
                    }
                    else mImageButton.setImageDrawable(mContext.getResources().getDrawable(idResource));
                }
                catch(Resources.NotFoundException exception) {
                    exception.printStackTrace();
                }
            }
            else {
                int iconColor = 0;
                try {
                    iconColor = Cobalt.parseColor(color);
                }
                catch (IllegalArgumentException exception) {
                    if (Cobalt.DEBUG) {
                        Log.w(Cobalt.TAG, TAG + " - init setIconColor : color " + color + " format not supported, use (#)RGB or (#)RRGGBB(AA).");
                    }

                    exception.printStackTrace();
                }

                mImageButton.setImageDrawable(CobaltFontManager.getCobaltFontDrawable(mContext, icon, iconColor));
            }
        }
        else if (title != null && mButton != null) {
            mButton.setText(title);

            // TODO: @sebf cannot happen
            if (color != null) {
                int textColor = 0;
                try {
                    textColor = Cobalt.parseColor(color);
                }
                catch (IllegalArgumentException exception) {
                    if (Cobalt.DEBUG) {
                        Log.w(Cobalt.TAG, TAG + " - init setTextColor : color " + color + " format not supported, use (#)RGB or (#)RRGGBB(AA).");
                    }

                    exception.printStackTrace();
                }

                mButton.setTextColor(textColor);
            }
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (mImageButton != null) {
            mImageButton.setEnabled(enabled);
        }
        /*if (enabled) {
            // TODO: @sebf Why check if visible or not before set?
            if (!mImageButton.isEnabled()) {
                mImageButton.setEnabled(true);
            }
        }
        else if (mImageButton.isEnabled()) {
            mImageButton.setEnabled(false);
        }*/
    }
}
