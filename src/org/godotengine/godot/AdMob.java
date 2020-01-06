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
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import com.godot.game.R;
import com.google.android.gms.ads.*;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdCallback;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.firebase.FirebaseApp;
import org.godotengine.godot.Dictionary;
import org.godotengine.godot.Utils;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
//import java.util.Dictionary;

public class AdMob {

    private static Activity activity = null;
    private static AdMob mInstance = null;
    private static Dictionary rewarded_meta_data = null;
    FrameLayout layout = null;
    private boolean mAdRewardLoaded = false;
    private boolean mAdViewLoaded = false;
    private HashMap<String, RewardedAd> reward_ads = null;
    private AdView mAdView = null;
    private InterstitialAd mInterstitialAd = null;
    private Dictionary mAdSize = null;
    private FirebaseApp mFirebaseApp = null;
    private JSONObject AdMobConfig = null;

    public AdMob(Activity p_activity) {
        activity = p_activity;
    }

    public static AdMob getInstance(Activity p_activity) {
        if (mInstance == null) {
            mInstance = new AdMob(p_activity);
        }

        return mInstance;
    }

    public void init(FirebaseApp firebaseApp, FrameLayout layout) {

        this.layout = layout;

        mFirebaseApp = firebaseApp;

        AdMobConfig = Firebase.getConfig().optJSONObject("Ads");
        MobileAds.initialize(activity, AdMobConfig.optString("AppId"));

        if (AdMobConfig.optBoolean("BannerAd", false)) {
            createBanner();
        }
        if (AdMobConfig.optBoolean("InterstitialAd", false)) {
            createInterstitial();
        }
        if (AdMobConfig.optBoolean("RewardedVideoAd", false)) {
            String ad_unit_id = AdMobConfig.optString("RewardedVideoAdId", "");
            List<String> ad_units = new ArrayList<String>();

            if (ad_unit_id.length() <= 0 || AdMobConfig.optBoolean("TestAds", false)) {
                Utils.d("GodotFirebase", "AdMob:RewardedVideo:UnitId:NotProvidedOrTestAds:AddingTestAd");
                ad_unit_id += "," + activity.getString(R.string.test_rewarded_video_ad_unit_id);
            }

            reward_ads = new HashMap<String, RewardedAd>();
            ad_units = Arrays.asList(ad_unit_id.split(","));
            for (String unit_id : ad_units) {
                Utils.d("GodotFirebase", "rewards_ads_put: " + unit_id);
                reward_ads.put(unit_id, requestNewRewardedVideo(unit_id));
            }
        }

        mAdSize = new Dictionary();
        mAdSize.put("width", 0);
        mAdSize.put("height", 0);

        onStart();
    }

    public Dictionary getBannerSize() {
        if ((int) mAdSize.get("width") == 0 || (int) mAdSize.get("height") == 0) {
            Utils.d("GodotFirebase", "AdView::Not::Loaded::Yet");
        }

        return mAdSize;
    }

    public void setBannerUnitId(final String id) {
        createBanner(id);
    }

    public void createBanner() {
        if (AdMobConfig == null) {
            return;
        }

        String ad_unit_id = AdMobConfig.optString("BannerAdId", "");

        if (ad_unit_id.length() <= 0 || AdMobConfig.optBoolean("TestAds", false)) {
            Utils.d("GodotFirebase", "AdMob:Banner:UnitId:NotProvidedOrTestAds:AddingTestAd");
            ad_unit_id = activity.getString(R.string.test_banner_ad_unit_id);
        }

        createBanner(ad_unit_id);
    }

    public void createBanner(final String ad_unit_id) {
        mAdViewLoaded = false;

        // see https://github.com/godotengine/godot/issues/32827 for changes
        // FrameLayout layout = ((Godot)activity).layout; // Getting Godots framelayout

        FrameLayout.LayoutParams AdParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT);

        if (mAdView != null) {
            layout.removeView(mAdView);
        }

        if (AdMobConfig.optString("BannerGravity", "BOTTOM").equals("BOTTOM")) {
            AdParams.gravity = Gravity.BOTTOM;
        } else {
            AdParams.gravity = Gravity.TOP;
        }

        AdRequest.Builder adRequestB = new AdRequest.Builder();
        adRequestB.tagForChildDirectedTreatment(true);

        // Covered with the test ad ID
		/*
		if (BuildConfig.DEBUG || AdMobConfig.optBoolean("TestAds", false)) {
			adRequestB.addTestDevice(AdRequest.DEVICE_ID_EMULATOR);
			adRequestB.addTestDevice(AdMobConfig.optString("TestDevice", Utils.getDeviceId(activity)));
		}
		*/

        AdRequest adRequest = adRequestB.build();

        mAdView = new AdView(activity);
        mAdView.setBackgroundColor(Color.TRANSPARENT);
        mAdView.setAdUnitId(ad_unit_id);
        mAdView.setAdSize(AdSize.SMART_BANNER);

        mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                Utils.d("GodotFirebase", "AdMob:Banner:OnAdLoaded");
                AdSize adSize = mAdView.getAdSize();
                mAdViewLoaded = true;

                mAdSize.put("width", adSize.getWidthInPixels(activity));
                mAdSize.put("height", adSize.getHeightInPixels(activity));

                Utils.callScriptFunc("AdMob", "AdMob_Banner", "loaded");
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                Utils.w("GodotFirebase", "AdMob:Banner:onAdFailedToLoad:ErrorCode:" + errorCode);
                Utils.callScriptFunc("AdMob", "AdMob_Banner", "load_failed");
            }
        });

        mAdView.setVisibility(View.INVISIBLE);
        mAdView.loadAd(adRequest);

        layout.addView(mAdView, AdParams);
    }

    public void createInterstitial() {
        if (AdMobConfig == null) {
            return;
        }

        String ad_unit_id = AdMobConfig.optString("InterstitialAdId", "");

        if (ad_unit_id.length() <= 0 || AdMobConfig.optBoolean("TestAds", false)) {
            Utils.d("GodotFirebase", "AdMob:Interstitial:UnitId:NotProvidedOrTestAds:AddingTestAd");
            ad_unit_id = activity.getString(R.string.test_interstitial_ad_unit_id);
        }

        mInterstitialAd = new InterstitialAd(activity);
        mInterstitialAd.setAdUnitId(ad_unit_id);
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                Utils.d("GodotFirebase", "AdMob:Interstitial:OnAdLoaded");
                Utils.callScriptFunc("AdMob", "AdMob_Interstitial", "loaded");
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                Utils.w("GodotFirebase", "AdMob:Interstitial:onAdFailedToLoad:" + errorCode);
                Utils.callScriptFunc("AdMob", "AdMob_Interstitial", "load_failed");
            }

            @Override
            public void onAdClosed() {
                Utils.w("GodotFirebase", "AdMob:Interstitial:onAdClosed");
                Utils.callScriptFunc("AdMob", "AdMob_Interstitial", "closed");
                requestNewInterstitial();
            }
        });

        requestNewInterstitial();
    }

    public void emitRewardedVideoStatus() {
        for (String unit_id : reward_ads.keySet()) {

            Utils.callScriptFunc("AdMob", "AdMob_Video",
                    buildStatus(unit_id, reward_ads.get(unit_id).isLoaded() ? "loaded" : "not_loaded"));
        }
    }

    public Dictionary buildStatus(String unitid, String status) {
        Dictionary dict = new Dictionary();
        dict.put("unit_id", unitid);
        dict.put("status", status);

        return dict;
    }

    public boolean isBannerLoaded() {
        return mAdViewLoaded;
    }

    public boolean isInterstitialLoaded() {
        if (mInterstitialAd != null) {
            return mInterstitialAd.isLoaded();
        }

        Utils.d("GodotFirebase", "Interstitial:NotInitialized");
        return false;
    }

    public boolean isRewardedAdLoaded(final String unit_id) {
        if (!isInitialized() || reward_ads == null) {
            return false;
        }

        return reward_ads.get(unit_id).isLoaded();
    }

    public void requestRewardedVideoStatus() {
        emitRewardedVideoStatus();
    }

    public void show_rewarded_video() {
        if (!isInitialized() || reward_ads == null) {
            Utils.d("GodotFirebase", "AdMob:RewardedVideo:NotConfigured[ reward_ad instance is null ]");
            return;
        }

        show_rewarded_video((String) reward_ads.keySet().toArray()[0]);
    }

    public void show_rewarded_video(final String unit_id) {
        if (!isInitialized() || reward_ads == null) {
            Utils.d("GodotFirebase", "AdMob:RewardedVideo:NotConfigured[ reward_ad instance is null ]");
            return;
        }

        RewardedAdCallback adCallback = new RewardedAdCallback() {
            @Override
            public void onUserEarnedReward(@NonNull RewardItem reward) {
                Utils.d("GodotFirebase", "AdMob:Rewarded:Success");

                Dictionary ret = new Dictionary();
                ret.put("RewardType", reward.getType());
                ret.put("RewardAmount", reward.getAmount());
                ret.put("unit_id", unit_id);

                Utils.callScriptFunc("AdMob", "AdMobReward", ret);
            }

            @Override
            public void onRewardedAdClosed() {
                Utils.d("GodotFirebase", "AdMob:VideoAd:Closed");
                Utils.callScriptFunc("AdMob", "AdMob_Video", buildStatus(unit_id, "closed"));
                reloadRewardedVideo(AdMobConfig.optBoolean("TestAds", false) ?
                        activity.getString(R.string.test_rewarded_video_ad_unit_id) : unit_id);
            }

            @Override
            public void onRewardedAdOpened() {
                Utils.d("GodotFirebase", "AdMob:VideoAd:Opended");
            }

            @Override
            public void onRewardedAdFailedToShow(int errorCode) {
                Utils.d("GodotFirebase", "Reward:VideoAd:FailedToShow");
            }
        };

        RewardedAd reward_ad;

        //If it is a test, call a test ads, but pass the actual ad id called to the callback.
        if (AdMobConfig.optBoolean("TestAds", false)) {
            String test_unit_id = activity.getString(R.string.test_rewarded_video_ad_unit_id);
            rewarded_meta_data.put("unit_id", test_unit_id);
            reward_ad = reward_ads.get(test_unit_id);
        } else {
            rewarded_meta_data.put("unit_id", unit_id);
            reward_ad = reward_ads.get(unit_id);
        }

        if (reward_ad.isLoaded()) {
            reward_ad.show(activity, adCallback);
        } else {
            Utils.d("GodotFirebase", "AdMob:RewardedVideo:NotLoaded");
        }
    }

    public void show_banner_ad(final boolean show) {
        if (!isInitialized() || mAdView == null) {
            return;
        }

        // Show Ad Banner here

        if (show) {
            if (mAdView.isEnabled()) {
                mAdView.setEnabled(true);
            }
            if (mAdView.getVisibility() == View.INVISIBLE) {
                Utils.d("GodotFirebase", "AdMob:Visiblity:On");
                mAdView.setVisibility(View.VISIBLE);
            }
        } else {
            if (mAdView.isEnabled()) {
                mAdView.setEnabled(false);
            }
            if (mAdView.getVisibility() != View.INVISIBLE) {
                Utils.d("GodotFirebase", "AdMob:Visiblity:Off");
                mAdView.setVisibility(View.INVISIBLE);
            }
        }
    }

    public void show_interstitial_ad() {
        if (!isInitialized() || mInterstitialAd == null) {
            return;
        }

        // Show interstitial ad
        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        } else {
            Utils.d("GodotFirebase", "AdMob:Interstitial:NotLoaded");
        }
    }

    public void reloadRewardedVideo(final String unitid) {
        if (reward_ads == null) {
            return;
        }

        Utils.d("GodotFirebase", "AdMob:RewardedVideo:Reloading_RewardedVideo_Request");
        reward_ads.put(unitid, requestNewRewardedVideo(unitid));
    }

    private RewardedAd requestNewRewardedVideo(final String unitid) {
        Utils.d("GodotFirebase", "AdMob:Loading:RewardedAd:For: " + unitid);
        mAdRewardLoaded = false;
        RewardedAd rewardedAd = new RewardedAd(activity, unitid);
        AdRequest.Builder adRB = new AdRequest.Builder();

        // Covered with the test ad ID
        if (BuildConfig.DEBUG || AdMobConfig.optBoolean("TestAds", false)) {
            adRB.addTestDevice(AdRequest.DEVICE_ID_EMULATOR);
            adRB.addTestDevice(AdMobConfig.optString("TestDevice", Utils.getDeviceId(activity)));
        }

        rewardedAd.loadAd(adRB.build(), new RewardedAdLoadCallback() {
            @Override
            public void onRewardedAdLoaded() {
                Utils.d("GodotFirebase", "AdMob:Video:Loaded");

                mAdRewardLoaded = true;
                Utils.callScriptFunc("AdMob", "AdMob_Video", buildStatus(unitid, "loaded"));
            }

            @Override
            public void onRewardedAdFailedToLoad(int errorCode) {
                Utils.d("GodotFirebase", "AdMob:VideoLoad:Failed" + errorCode);
                Utils.callScriptFunc("AdMob", "AdMob_Video", buildStatus(unitid, "load_failed"));
                //reloadRewardedVideo(unitid);
            }
        });
        return rewardedAd;
    }

    private void requestNewInterstitial() {
        AdRequest.Builder adRB = new AdRequest.Builder();

        // Covered with the test ad ID
        if (BuildConfig.DEBUG || AdMobConfig.optBoolean("TestAds", false)) {
            adRB.addTestDevice(AdRequest.DEVICE_ID_EMULATOR);
            adRB.addTestDevice(AdMobConfig.optString("TestDevice", Utils.getDeviceId(activity)));
        }

        AdRequest adRequest = adRB.build();

        mInterstitialAd.loadAd(adRequest);
    }

    private boolean isInitialized() {
        if (mFirebaseApp == null) {
            Utils.d("GodotFirebase", "AdMob:NotInitialized.");
            return false;
        } else {
            return true;
        }
    }

    public void onStart() {
        rewarded_meta_data = new Dictionary();
    }

    public void onPause() {
        if (mAdView != null) {
            mAdView.pause();
        }
        //if (reward_ad != null) { reward_ad.pause(activity); }
    }

    public void onResume() {
        if (mAdView != null) {
            mAdView.resume();
        }
        //if (reward_ad != null) { reward_ad.resume(activity); }
    }

    public void onStop() {
        if (mAdView != null) {
            mAdView.destroy();
        }
        //if (reward_ad != null) { reward_ad.destroy(activity); }
    }
}
