/**
 *
 * CobaltFontManager
 * Cobalt
 *
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Cobaltians
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

package org.cobaltians.cobalt.font;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;

import org.cobaltians.cobalt.Cobalt;

/**
 * Singleton allowing to
 */
public class CobaltFontManager {

    /**
     * the fonts key in cobalt.conf
     */
    private static final String kFonts = "fonts";
    private static final  String kAndroid = "android";
    private static final  String CONF_FILE = "cobalt.conf";
    private static Context mContext;
    // TAG
    public static final String TAG = CobaltFontManager.class.getSimpleName();
    public static final int DEFAULT_COLOR = Color.BLACK;

    /**
     * Initializes and returns a font drawable with a font icon identifier, color, text size and padding
     * @param context the activity context
     * @param identifier the font icon identifier as "font-key font-icon" (i.e.: fa fa-mobile)
     * @param color the text color as a color-int
     * @return a Drawable or null
     */
    //* @param textSize the text size in sp
    //* @param padding the padding in dp
    public static CobaltAbstractFontDrawable getCobaltFontDrawable(Context context, String identifier, int color) {
        mContext = context;
        if (identifier != null) {
            if (identifier.contains(" ")) {
                String[] splitIdentifier = identifier.split(" ");
                String fontName = splitIdentifier[0];
                Class<? extends CobaltAbstractFontDrawable> fontClass = getFonts().get(fontName);
                if (fontClass != null) {
                    try {
                        Class[] argsClass = new Class[] {Context.class, String.class, int.class};
                        Object[] arrayArgs = new Object[] {context, splitIdentifier[1], color };
                        Constructor fontConstructor = fontClass.getDeclaredConstructor(argsClass);
                        try {
                            return (CobaltAbstractFontDrawable) fontConstructor.newInstance(arrayArgs);
                        } catch (InstantiationException e) {
                            if (Cobalt.DEBUG) Log.e(TAG, "- getCobaltFontDrawable: exception in Instantiation of fontClass");
                            e.printStackTrace();
                        } catch (IllegalAccessException e) {
                            if (Cobalt.DEBUG) Log.e(TAG, "- getCobaltFontDrawable");
                            e.printStackTrace();
                        } catch (InvocationTargetException e) {
                            if (Cobalt.DEBUG) Log.e(TAG, "- getCobaltFontDrawable");
                            e.printStackTrace();
                        }
                    } catch (NoSuchMethodException e) {
                        e.printStackTrace();
                    }
                }
                else if (Cobalt.DEBUG) Log.e(TAG, "- getCobaltFontDrawable: no font class found for name " + fontName + ".");
            }
            else if (Cobalt.DEBUG) Log.e(TAG, TAG + " - getCobaltFontDrawable : no space separate in identifier");
        }
        else if (Cobalt.DEBUG) Log.e(TAG, TAG + " - getCobaltFontDrawable: identifier for icon is null");

        return null;
    }

    /**
     * Returns font key:class tuples as HashMap
     * @return font key:class tuples as HashMap
     */
    private static HashMap<String, Class<? extends CobaltAbstractFontDrawable>> getFonts() {
        HashMap<String, Class<? extends CobaltAbstractFontDrawable>> fontMap = new HashMap<>();

        try {
            // TODO: make the Cobalt method public and use it instead of reimplement it
            JSONObject configuration = getConfiguration();
            JSONObject fonts = configuration.getJSONObject(kFonts);
            Iterator<String> fontsIterator = fonts.keys();

            while(fontsIterator.hasNext()) {
                String fontName = fontsIterator.next();
                try{
                    JSONObject font = fonts.getJSONObject(fontName);
                    String fontClassName = font.getString(kAndroid);
                    try {
                        Class<?> fontClass = Class.forName(fontClassName);
                        if (CobaltAbstractFontDrawable.class.isAssignableFrom(fontClass)) {
                            fontMap.put(fontName, (Class<? extends CobaltAbstractFontDrawable>) fontClass);
                        }
                        else if (Cobalt.DEBUG) Log.e(TAG, TAG + " - getFonts: " + fontClass + " does not inherit from CobaltAbstractFontDrawable!\n" + fontName + " font message will not be processed.");

                    }
                    catch (ClassNotFoundException e) {
                        if (Cobalt.DEBUG) {
                            Log.e(TAG, TAG + " - getFonts: " + fontClassName + " class not found!\n" + fontName + " font message will not be processed.");
                            e.printStackTrace();
                        }
                    }
                }
                catch (JSONException e) {
                    if (Cobalt.DEBUG) {
                        Log.e(TAG, TAG + " - getFonts: " + fontName + " field is not a JSONObject or does not contain an android field or is not a String.\n" + fontName + " font message will not be processed.");
                        e.printStackTrace();
                    }
                }
            }
        }
        catch (JSONException e) {
            if (Cobalt.DEBUG) {
                Log.w(TAG, TAG + " - getFonts: fonts field of cobalt.conf not found or not a JSONObject.");
                e.printStackTrace();
            }
        }
        return fontMap;
    }

    /**********************************************************************************************
     * HELPER METHODS
     **********************************************************************************************/

    // TODO: remove
    private static JSONObject getConfiguration() {
        String mResourcePath = Cobalt.getInstance(mContext).getResourcePathFromAsset();
        String configuration = readFileFromAssets( mResourcePath + CONF_FILE);

        try {
            return new JSONObject(configuration);
        }
        catch (JSONException exception) {
            if (Cobalt.DEBUG) Log.e(TAG, TAG + " - getConfiguration: check cobalt.conf. File is missing or not at " + mResourcePath + CONF_FILE);
            exception.printStackTrace();
        }

        return new JSONObject();
    }

    // TODO: remove
    private static String readFileFromAssets(String file) {
        try {
            AssetManager assetManager = mContext.getAssets();
            InputStream inputStream = assetManager.open(file);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder fileContent = new StringBuilder();
            int character;

            while ((character = bufferedReader.read()) != -1) {
                fileContent.append((char) character);
            }

            return fileContent.toString();
        }
        catch (FileNotFoundException exception) {
            if (Cobalt.DEBUG) Log.e(TAG, TAG + " - readFileFromAssets: " + file + "not found.");
        }
        catch (IOException exception) {
            if (Cobalt.DEBUG) Log.e(TAG, TAG + " - readFileFromAssets: IOException");
            exception.printStackTrace();
        }

        return "";
    }

}
