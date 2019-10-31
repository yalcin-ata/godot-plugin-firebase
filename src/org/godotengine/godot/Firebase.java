/**
 * Copyright 2017 FrogSquare. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/

package org.godotengine.godot;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.FrameLayout;
import com.google.firebase.FirebaseApp;
import org.godotengine.godot.Dictionary;
import org.json.JSONException;
import org.json.JSONObject;

public class Firebase extends Godot.SingletonBase {

    protected static String currentScreen = "None";
    private static Context context = null;
    private static Activity activity = null;
    private static JSONObject firebaseConfig = new JSONObject();
    FrameLayout layout = null;
    private FirebaseApp mFirebaseApp = null;

    public Firebase(Activity p_activity) {
        registerClass("Firebase", new String[]{
                "init", "initWithFile", "alert", "set_debug",

                //Analytics++
                "setScreenName", "sendAchievement", "send_custom", "send_events", "join_group", "level_up",
                "post_score", "content_select", "earn_currency",
                "spend_currency", "tutorial_begin", "tutorial_complete",
                //Analytics--

                //AdMob++
                "show_banner_ad", "show_interstitial_ad", "show_rewarded_video",
                "request_rewarded_video_status", "set_banner_unitid", "show_rvideo",
                "get_banner_size", "is_banner_loaded",
                "is_interstitial_loaded", "is_rewarded_video_loaded",
                //AdMob--

        });

        activity = p_activity;
    }

    static public Godot.SingletonBase initialize(Activity p_activity) {
        return new Firebase(p_activity);
    }

    /** Main Funcs **/
    public static JSONObject getConfig() {
        return firebaseConfig;
    }

    private void initFirebase(final String data) {
        Utils.d("GodotFirebase", "Data From File: " + data);

        JSONObject config = null;
        mFirebaseApp = FirebaseApp.initializeApp(activity);

        if (data.length() <= 0) {
            Utils.d("GodotFirebase", "Firebase initialized.");
            return;
        }

        try {
            config = new JSONObject(data);
            firebaseConfig = config;
        } catch (JSONException e) {
            Utils.d("GodotFirebase", "JSON Parse error: " + e.toString());
        }

        //Analytics++
        if (config.optBoolean("Analytics", true)) {
            Utils.d("GodotFirebase", "Initializing Firebase Analytics.");
            Analytics.getInstance(activity).init(mFirebaseApp);
        }
        //Analytics--

        //AdMob++
        if (config.optBoolean("AdMob", false)) {
            Utils.d("GodotFirebase", "Initializing Firebase AdMob.");
            AdMob.getInstance(activity).init(mFirebaseApp, layout);
        }
        //AdMob--

        Utils.d("GodotFirebase", "FireBase initialized.");
    }

    public void set_debug(final boolean p_value) {
        Utils.set_debug("GodotFirebase", p_value);
    }

    public void alertMsg(String message) {
        alertMsg("Firebase", message);
    }

    public void alertMsg(String title, String message) {
        AlertDialog.Builder bld;

        bld = new AlertDialog.Builder(activity, AlertDialog.THEME_HOLO_LIGHT);
        bld.setIcon(com.godot.game.R.drawable.icon);
        bld.setTitle(title);
        bld.setMessage(message);
        bld.setNeutralButton("OK", null);
        bld.create().show();
    }

    public void init(final String data, final int script_id) {
        activity.runOnUiThread(new Runnable() {
            public void run() {
                Utils.setScriptInstance(script_id);
                initFirebase(data);
            }
        });
    }

    public void initWithFile(final String fileName, final int script_id) {
        activity.runOnUiThread(new Runnable() {
            public void run() {
                String data = Utils.readFromFile(fileName, activity);
                data = data.replaceAll("\\s+", "");

                Utils.setScriptInstance(script_id);
                initFirebase(data);
            }
        });
    }

    //Analytics++
    public void setScreenName(final String screen_name) {
        activity.runOnUiThread(new Runnable() {
            public void run() {
                if (screen_name.length() <= 0) {
                    Utils.d("GodotFirebase", "Screen name is empty defaults to main");
                    Analytics.getInstance(activity).set_screen_name("Main Screen");
                } else {
                    Analytics.getInstance(activity).set_screen_name(screen_name);
                }
            }
        });
    }

    public void sendAchievement(final String a_id) {
        if (a_id.length() <= 0) {
            Utils.d("GodotFirebase", "Achievement id not provided");
            return;
        }

        activity.runOnUiThread(new Runnable() {
            public void run() {
                Analytics.getInstance(activity).send_achievement(a_id);
            }
        });
    }

    public void join_group(final String id) {
        activity.runOnUiThread(new Runnable() {
            public void run() {
                Analytics.getInstance(activity).send_group(id);
            }
        });
    }

    public void level_up(final String character, final int level) {
        activity.runOnUiThread(new Runnable() {
            public void run() {
                Analytics.getInstance(activity).send_level_up(character, level);
            }
        });
    }

    public void post_score(final String character, final int level, final int score) {
        activity.runOnUiThread(new Runnable() {
            public void run() {
                Analytics.getInstance(activity).send_score(character, level, score);
            }
        });
    }

    public void content_select(final String content, final String item_id) {
        activity.runOnUiThread(new Runnable() {
            public void run() {
                Analytics.getInstance(activity).send_content(content, item_id);
            }
        });
    }
    //Analytics--

    public void earn_currency(final String currency_name, final int value) {
        activity.runOnUiThread(new Runnable() {
            public void run() {
                Analytics.getInstance(activity).earn_currency(currency_name, value);
            }
        });
    }

    public void spend_currency(final String item_name, final String currency, final int value) {
        activity.runOnUiThread(new Runnable() {
            public void run() {
                Analytics.getInstance(activity)
                        .spend_currency(item_name, currency, value);
            }
        });
    }

    public void tutorial_begin() {
        activity.runOnUiThread(new Runnable() {
            public void run() {
                Analytics.getInstance(activity).send_tutorial_begin();
            }
        });
    }

    public void tutorial_complete() {
        activity.runOnUiThread(new Runnable() {
            public void run() {
                Analytics.getInstance(activity).send_tutorial_complete();
            }
        });
    }

    public void send_events(final String key, final Dictionary data) {
        if (key.length() <= 0 || data.size() <= 0) {
            Utils.d("GodotFirebase", "Key or Data is null.");
            return;
        }

        activity.runOnUiThread(new Runnable() {
            public void run() {
                Analytics.getInstance(activity).send_events(key, data);
            }
        });
    }

    public void send_custom(final String key, final String value) {
        if (key.length() <= 0 || value.length() <= 0) {
            Utils.d("GodotFirebase", "Key or Value is null.");
            return;
        }

        activity.runOnUiThread(new Runnable() {
            public void run() {
                Analytics.getInstance(activity).send_custom(key, value);
            }
        });
    }

    //AdMob++
    public boolean is_banner_loaded() {
        return AdMob.getInstance(activity).isBannerLoaded();
    }

    public boolean is_interstitial_loaded() {
        return AdMob.getInstance(activity).isInterstitialLoaded();

    }

    public boolean is_rewarded_video_loaded(final String unit_id) {
        return AdMob.getInstance(activity).isRewardedAdLoaded(unit_id);
    }

    public void set_banner_unitid(final String unit_id) {
        activity.runOnUiThread(new Runnable() {
            public void run() {
                AdMob.getInstance(activity).setBannerUnitId(unit_id);
            }
        });
    }

    public void show_banner_ad(final boolean show) {
        activity.runOnUiThread(new Runnable() {
            public void run() {
                AdMob.getInstance(activity).show_banner_ad(show);
            }
        });
    }
    //AdMob--

    public Dictionary get_banner_size() {
        return AdMob.getInstance(activity).getBannerSize();
    }
    /** Extra **/

    public void show_interstitial_ad() {
        activity.runOnUiThread(new Runnable() {
            public void run() {
                AdMob.getInstance(activity).show_interstitial_ad();
            }
        });
    }

    public void reload_rewarded_video(final String unit_id) {
        activity.runOnUiThread(new Runnable() {
            public void run() {
                AdMob.getInstance(activity).reloadRewardedVideo(unit_id);
            }
        });
    }

    public void show_rvideo(final String unit_id) {
        activity.runOnUiThread(new Runnable() {
            public void run() {
                AdMob.getInstance(activity).show_rewarded_video(unit_id);
            }
        });
    }

    public void request_rewarded_video_status() {
        activity.runOnUiThread(new Runnable() {
            public void run() {
                AdMob.getInstance(activity).requestRewardedVideoStatus();
            }
        });
    }

    public void show_rewarded_video() {
        activity.runOnUiThread(new Runnable() {
            public void run() {
                AdMob.getInstance(activity).show_rewarded_video();
            }
        });
    }

    /** Extra **/
    public void alert(final String message) {
        if (message.length() <= 0) {
            Utils.d("GodotFirebase", "Message is empty.");
            return;
        }

        activity.runOnUiThread(new Runnable() {
            public void run() {
                alertMsg(message);
            }
        });
    }

    protected void onMainActivityResult(int requestCode, int resultCode, Intent data) {
    }

    protected void onMainPause() {
        //AdMob++
        AdMob.getInstance(activity).onPause();
        //AdMob--
    }

    protected void onMainResume() {
        //AdMob++
        AdMob.getInstance(activity).onResume();
        //AdMob--
    }

    protected void onMainDestroy() {
        //AdMob++
        AdMob.getInstance(activity).onStop();
        //AdMob--
    }

    @Override
    public View onMainCreateView(Activity activity) {
        layout = new FrameLayout(activity);
        return layout;
    }
}
