package com.app.webdroid.databases.prefs;

import android.content.Context;
import android.content.SharedPreferences;

public class AdsPref {

    private Context context;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public AdsPref(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences("ads_setting", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public void saveAds(String adStatus, String adType, String backupAds, String adMobPublisherId, String adMobAppId, String adMobBannerId, String adMobInterstitialId, String adMobNativeId, String adMobAppOpenId, String startAppId, String unityGameId, String unityBannerId, String unityInterstitialId, String appLovinBannerId, String appLovinInterstitialId, String applovinNativeAdManualUnitId, String applovinBannerZoneId, String applovinInterstitialZoneId, int interstitialAdInterval) {
        editor.putString("ad_status", adStatus);
        editor.putString("ad_type", adType);
        editor.putString("backup_ads", backupAds);
        editor.putString("admob_publisher_id", adMobPublisherId);
        editor.putString("admob_app_id", adMobAppId);
        editor.putString("admob_banner_unit_id", adMobBannerId);
        editor.putString("admob_interstitial_unit_id", adMobInterstitialId);
        editor.putString("admob_native_unit_id", adMobNativeId);
        editor.putString("admob_app_open_ad_unit_id", adMobAppOpenId);
        editor.putString("startapp_app_id", startAppId);
        editor.putString("unity_game_id", unityGameId);
        editor.putString("unity_banner_placement_id", unityBannerId);
        editor.putString("unity_interstitial_placement_id", unityInterstitialId);
        editor.putString("applovin_banner_ad_unit_id", appLovinBannerId);
        editor.putString("applovin_interstitial_ad_unit_id", appLovinInterstitialId);
        editor.putString("applovin_native_ad_manual_unit_id", applovinNativeAdManualUnitId);
        editor.putString("applovin_banner_zone_id", applovinBannerZoneId);
        editor.putString("applovin_interstitial_zone_id", applovinInterstitialZoneId);
        editor.putInt("interstitial_ad_interval", interstitialAdInterval);
        editor.apply();
    }

    public String getAdStatus() {
        return sharedPreferences.getString("ad_status", "0");
    }

    public String getAdType() {
        return sharedPreferences.getString("ad_type", "0");
    }

    public String getBackupAds() {
        return sharedPreferences.getString("backup_ads", "none");
    }

    public String getAdMobPublisherId() {
        return sharedPreferences.getString("admob_publisher_id", "0");
    }

    public String getAdMobAppId() {
        return sharedPreferences.getString("admob_app_id", "0");
    }

    public String getAdMobBannerId() {
        return sharedPreferences.getString("admob_banner_unit_id", "0");
    }

    public String getAdMobInterstitialId() {
        return sharedPreferences.getString("admob_interstitial_unit_id", "0");
    }

    public String getAdMobNativeId() {
        return sharedPreferences.getString("admob_native_unit_id", "0");
    }

    public String getAdMobAppOpenAdId() {
        return sharedPreferences.getString("admob_app_open_ad_unit_id", "0");
    }

    public String getStartappAppId() {
        return sharedPreferences.getString("startapp_app_id", "0");
    }

    public String getUnityGameId() {
        return sharedPreferences.getString("unity_game_id", "0");
    }

    public String getUnityBannerPlacementId() {
        return sharedPreferences.getString("unity_banner_placement_id", "banner");
    }

    public String getUnityInterstitialPlacementId() {
        return sharedPreferences.getString("unity_interstitial_placement_id", "video");
    }

    public String getAppLovinBannerAdUnitId() {
        return sharedPreferences.getString("applovin_banner_ad_unit_id", "0");
    }

    public String getAppLovinInterstitialAdUnitId() {
        return sharedPreferences.getString("applovin_interstitial_ad_unit_id", "0");
    }

    public String getMopubBannerAdUnitId() {
        return sharedPreferences.getString("mopub_banner_ad_unit_id", "0");
    }

    public String getMopubInterstitialAdUnitId() {
        return sharedPreferences.getString("mopub_interstitial_ad_unit_id", "0");
    }

    public int getInterstitialAdInterval() {
        return sharedPreferences.getInt("interstitial_ad_interval", 0);
    }

}
