package com.app.webdroid.activities;

import static com.solodroid.ads.sdk.util.Constant.ADMOB;
import static com.solodroid.ads.sdk.util.Constant.AD_STATUS_ON;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;
import androidx.multidex.MultiDex;

import com.app.webdroid.Config;
import com.app.webdroid.callbacks.CallbackConfig;
import com.app.webdroid.databases.prefs.AdsPref;
import com.app.webdroid.databases.prefs.SharedPref;
import com.app.webdroid.models.Notification;
import com.app.webdroid.rests.RestAdapter;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.messaging.FirebaseMessaging;
import com.onesignal.OneSignal;
import com.solodroid.ads.sdk.format.AppOpenAdManager;
import com.solodroid.ads.sdk.util.OnShowAdCompleteListener;
import com.solodroid.ads.sdk.util.Tools;

import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyApplication extends Application implements Application.ActivityLifecycleCallbacks, LifecycleObserver {

    private static final String TAG = "MyApplication";
    private AppOpenAdManager appOpenAdManager;
    Call<CallbackConfig> callbackConfigCall = null;
    Activity currentActivity;
    FirebaseAnalytics mFirebaseAnalytics;
    SharedPref sharedPref;
    AdsPref adsPref;
    String message = "";
    String bigPicture = "";
    String title = "";
    String link = "";
    String postId = "";
    String uniqueId = "";
    Notification notification;

    @Override
    public void onCreate() {
        super.onCreate();
        this.registerActivityLifecycleCallbacks(this);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        sharedPref = new SharedPref(this);
        adsPref = new AdsPref(this);

        MobileAds.initialize(this, initializationStatus -> {
        });
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
        appOpenAdManager = new AppOpenAdManager();

        initNotification();
    }

    public void initNotification() {
        OneSignal.disablePush(false);
        Log.d(TAG, "OneSignal Notification is enabled");

        // Enable verbose OneSignal logging to debug issues if needed.
        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE);
        OneSignal.initWithContext(this);
        requestConfig();

        OneSignal.setNotificationOpenedHandler(
                result -> {
                    title = result.getNotification().getTitle();
                    message = result.getNotification().getBody();
                    bigPicture = result.getNotification().getBigPicture();
                    Log.d(TAG, title + ", " + message + ", " + bigPicture);
                    try {
                        uniqueId = result.getNotification().getAdditionalData().getString("unique_id");
                        postId = result.getNotification().getAdditionalData().getString("post_id");
                        link = result.getNotification().getAdditionalData().getString("link");
                        Log.d(TAG, postId + ", " + uniqueId);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra("unique_id", uniqueId);
                    intent.putExtra("post_id", postId);
                    intent.putExtra("title", title);
                    intent.putExtra("link", link);
                    startActivity(intent);
                });

        OneSignal.unsubscribeWhenNotificationsAreDisabled(true);
    }

    private void requestConfig() {
        String data = Tools.decode(Config.ACCESS_KEY);
        String[] results = data.split("_applicationId_");
        String remoteUrl = results[0];
        String applicationId = results[1];
        requestAPI(remoteUrl);
    }

    private void requestAPI(String remoteUrl) {
        if (remoteUrl.startsWith("http://") || remoteUrl.startsWith("https://")) {
            if (remoteUrl.contains("https://drive.google.com")) {
                String driveUrl = remoteUrl.replace("https://", "").replace("http://", "");
                List<String> data = Arrays.asList(driveUrl.split("/"));
                String googleDriveFileId = data.get(3);
                callbackConfigCall = RestAdapter.createApi().getDriveJsonFileId(googleDriveFileId);
            } else {
                callbackConfigCall = RestAdapter.createApi().getJsonUrl(remoteUrl);
            }
        } else {
            callbackConfigCall = RestAdapter.createApi().getDriveJsonFileId(remoteUrl);
        }
        callbackConfigCall.enqueue(new Callback<CallbackConfig>() {
            public void onResponse(@NonNull Call<CallbackConfig> call, @NonNull Response<CallbackConfig> response) {
                CallbackConfig resp = response.body();
                if (resp != null) {
                    notification = resp.notification;
                    FirebaseMessaging.getInstance().subscribeToTopic(notification.fcm_notification_topic);
                    OneSignal.setAppId(notification.onesignal_app_id);
                    Log.d(TAG, "FCM Subscribe topic : " + notification.fcm_notification_topic);
                    Log.d(TAG, "OneSignal App ID : " + notification.onesignal_app_id);
                }
            }

            public void onFailure(@NonNull Call<CallbackConfig> call, @NonNull Throwable th) {
                Log.e(TAG, "initialize failed : " + th.getMessage());
            }
        });
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    protected void onMoveToForeground() {
        // Show the ad (if available) when the app moves to foreground.
        if (adsPref.getAdType().equals(ADMOB) && adsPref.getAdStatus().equals(AD_STATUS_ON)) {
            if (!adsPref.getAdMobAppOpenAdId().equals("0")) {
                if (!currentActivity.getIntent().hasExtra("unique_id")) {
                    appOpenAdManager.showAdIfAvailable(currentActivity, adsPref.getAdMobAppOpenAdId());
                }
            }
        }
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        if (adsPref.getAdType().equals(ADMOB) && adsPref.getAdStatus().equals(AD_STATUS_ON)) {
            if (!adsPref.getAdMobAppOpenAdId().equals("0")) {
                if (!appOpenAdManager.isShowingAd) {
                    currentActivity = activity;
                }
            }
        }
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
    }

    public void showAdIfAvailable(@NonNull Activity activity, @NonNull OnShowAdCompleteListener onShowAdCompleteListener) {
        // We wrap the showAdIfAvailable to enforce that other classes only interact with MyApplication class
        if (adsPref.getAdType().equals(ADMOB) && adsPref.getAdStatus().equals(AD_STATUS_ON)) {
            if (!adsPref.getAdMobAppOpenAdId().equals("0")) {
                appOpenAdManager.showAdIfAvailable(activity, adsPref.getAdMobAppOpenAdId(), onShowAdCompleteListener);
            }
        }
    }

}