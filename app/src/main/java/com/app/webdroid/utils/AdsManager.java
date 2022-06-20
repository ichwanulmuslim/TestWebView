package com.app.webdroid.utils;

import static com.app.webdroid.Config.LEGACY_GDPR;

import android.app.Activity;
import android.view.View;

import com.app.webdroid.BuildConfig;
import com.app.webdroid.databases.prefs.AdsPref;
import com.app.webdroid.databases.prefs.SharedPref;
import com.app.webdroid.models.Ads;
import com.app.webdroid.models.App;
import com.solodroid.ads.sdk.format.AdNetwork;
import com.solodroid.ads.sdk.format.BannerAd;
import com.solodroid.ads.sdk.format.InterstitialAd;
import com.solodroid.ads.sdk.format.NativeAd;
import com.solodroid.ads.sdk.format.NativeAdFragment;
import com.solodroid.ads.sdk.gdpr.GDPR;
import com.solodroid.ads.sdk.gdpr.LegacyGDPR;

public class AdsManager {

    Activity activity;
    AdNetwork.Initialize adNetwork;
    BannerAd.Builder bannerAd;
    InterstitialAd.Builder interstitialAd;
    NativeAd.Builder nativeAd;
    NativeAdFragment.Builder nativeAdView;
    SharedPref sharedPref;
    AdsPref adsPref;
    LegacyGDPR legacyGDPR;
    GDPR gdpr;

    public AdsManager(Activity activity) {
        this.activity = activity;
        this.sharedPref = new SharedPref(activity);
        this.adsPref = new AdsPref(activity);
        this.legacyGDPR = new LegacyGDPR(activity);
        this.gdpr = new GDPR(activity);
        adNetwork = new AdNetwork.Initialize(activity);
        bannerAd = new BannerAd.Builder(activity);
        interstitialAd = new InterstitialAd.Builder(activity);
        nativeAd = new NativeAd.Builder(activity);
        nativeAdView = new NativeAdFragment.Builder(activity);
    }

    public void initializeAd() {
        adNetwork.setAdStatus(adsPref.getAdStatus())
                .setAdNetwork(adsPref.getAdType())
                .setBackupAdNetwork(adsPref.getBackupAds())
                .setAdMobAppId(null)
                .setStartappAppId(adsPref.getStartappAppId())
                .setUnityGameId(adsPref.getUnityGameId())
                .setAppLovinSdkKey(null)
                .setMopubBannerId(adsPref.getMopubBannerAdUnitId())
                .setDebug(BuildConfig.DEBUG)
                .build();
    }

    public void loadBannerAd(int placement) {
        bannerAd.setAdStatus(adsPref.getAdStatus())
                .setAdNetwork(adsPref.getAdType())
                .setBackupAdNetwork(adsPref.getBackupAds())
                .setAdMobBannerId(adsPref.getAdMobBannerId())
                .setUnityBannerId(adsPref.getUnityBannerPlacementId())
                .setAppLovinBannerId(adsPref.getAppLovinBannerAdUnitId())
                .setMopubBannerId(adsPref.getMopubBannerAdUnitId())
                .setDarkTheme(sharedPref.getIsDarkTheme())
                .setPlacementStatus(placement)
                .setLegacyGDPR(LEGACY_GDPR)
                .build();
    }

    public void loadInterstitialAd(int placement, int interval) {
        interstitialAd.setAdStatus(adsPref.getAdStatus())
                .setAdNetwork(adsPref.getAdType())
                .setBackupAdNetwork(adsPref.getBackupAds())
                .setAdMobInterstitialId(adsPref.getAdMobInterstitialId())
                .setUnityInterstitialId(adsPref.getUnityInterstitialPlacementId())
                .setAppLovinInterstitialId(adsPref.getAppLovinInterstitialAdUnitId())
                .setMopubInterstitialId(adsPref.getMopubInterstitialAdUnitId())
                .setInterval(interval)
                .setPlacementStatus(placement)
                .setLegacyGDPR(LEGACY_GDPR)
                .build();
    }

    public void loadNativeAd(int placement) {
        nativeAd.setAdStatus(adsPref.getAdStatus())
                .setAdNetwork(adsPref.getAdType())
                .setBackupAdNetwork(adsPref.getBackupAds())
                .setAdMobNativeId(adsPref.getAdMobNativeId())
                .setPlacementStatus(placement)
                .setDarkTheme(sharedPref.getIsDarkTheme())
                .setLegacyGDPR(LEGACY_GDPR)
                .build();
    }

    public void loadNativeAdView(View view, int placement) {
        nativeAdView.setAdStatus(adsPref.getAdStatus())
                .setAdNetwork(adsPref.getAdType())
                .setBackupAdNetwork(adsPref.getBackupAds())
                .setAdMobNativeId(adsPref.getAdMobNativeId())
                .setPlacementStatus(placement)
                .setDarkTheme(sharedPref.getIsDarkTheme())
                .setLegacyGDPR(LEGACY_GDPR)
                .setView(view)
                .build();
    }

    public void showInterstitialAd() {
        interstitialAd.show();
    }

    public void updateConsentStatus() {
        if (LEGACY_GDPR) {
            legacyGDPR.updateLegacyGDPRConsentStatus(adsPref.getAdMobPublisherId(), sharedPref.getPrivacyPolicyUrl());
        } else {
            gdpr.updateGDPRConsentStatus();
        }
    }

    public void saveAds(AdsPref adsPref, Ads ads) {
        adsPref.saveAds(
                ads.ad_status,
                ads.ad_type,
                ads.backup_ads,
                ads.admob_publisher_id,
                ads.admob_app_id,
                ads.admob_banner_unit_id,
                ads.admob_interstitial_unit_id,
                ads.admob_native_unit_id,
                ads.admob_app_open_ad_unit_id,
                ads.startapp_app_id,
                ads.unity_game_id,
                ads.unity_banner_placement_id,
                ads.unity_interstitial_placement_id,
                ads.applovin_banner_ad_unit_id,
                ads.applovin_interstitial_ad_unit_id,
                ads.applovin_native_ad_manual_unit_id,
                ads.applovin_banner_zone_id,
                ads.applovin_interstitial_zone_id,
                ads.interstitial_ad_interval
        );
    }

    public void saveConfig(SharedPref sharedPref, App app) {
        sharedPref.saveConfig(
                app.toolbar,
                app.navigation_drawer,
                app.geolocation,
                app.cache,
                app.open_link_in_external_browser,
                app.privacy_policy_url,
                app.more_apps_url,
                app.redirect_url
        );
    }

}
