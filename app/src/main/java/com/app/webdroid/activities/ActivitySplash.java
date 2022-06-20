package com.app.webdroid.activities;

import static com.app.webdroid.utils.Constant.DELAY_SPLASH;
import static com.solodroid.ads.sdk.util.Constant.ADMOB;
import static com.solodroid.ads.sdk.util.Constant.AD_STATUS_ON;

import android.app.Application;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.app.webdroid.BuildConfig;
import com.app.webdroid.Config;
import com.app.webdroid.R;
import com.app.webdroid.callbacks.CallbackConfig;
import com.app.webdroid.databases.prefs.AdsPref;
import com.app.webdroid.databases.prefs.SharedPref;
import com.app.webdroid.databases.sqlite.DbNavigation;
import com.app.webdroid.models.Ads;
import com.app.webdroid.models.App;
import com.app.webdroid.models.Navigation;
import com.app.webdroid.rests.RestAdapter;
import com.app.webdroid.utils.AdsManager;
import com.app.webdroid.utils.Utils;
import com.solodroid.ads.sdk.util.Tools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivitySplash extends AppCompatActivity {

    public static final String TAG = "SplashActivity";
    Call<CallbackConfig> callbackConfigCall = null;
    ProgressBar progressBar;
    AdsManager adsManager;
    SharedPref sharedPref;
    AdsPref adsPref;
    App app;
    Ads ads;
    List<Navigation> navigationList = new ArrayList<>();
    DbNavigation dbNavigation;
    ImageView imgSplash;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.getTheme(this);
        setContentView(R.layout.activity_splash);
        Utils.setNavigation(this);

        dbNavigation = new DbNavigation(this);
        adsManager = new AdsManager(this);
        sharedPref = new SharedPref(this);
        adsPref = new AdsPref(this);

        imgSplash = findViewById(R.id.imgSplash);
        if (sharedPref.getIsDarkTheme()) {
            imgSplash.setImageResource(R.drawable.splash_dark);
        } else {
            imgSplash.setImageResource(R.drawable.splash_default);
        }

        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);

        if (adsPref.getAdType().equals(ADMOB) && adsPref.getAdStatus().equals(AD_STATUS_ON)) {
            if (!adsPref.getAdMobAppOpenAdId().equals("0")) {
                Application application = getApplication();
                ((MyApplication) application).showAdIfAvailable(ActivitySplash.this, this::requestConfig);
            } else {
                requestConfig();
            }
        } else {
            requestConfig();
        }

    }

    private void requestConfig() {
        String data = Tools.decode(Config.ACCESS_KEY);
        String[] results = data.split("_applicationId_");
        String remoteUrl = results[0];
        String applicationId = results[1];

        if (applicationId.equals(BuildConfig.APPLICATION_ID)) {
            requestAPI(remoteUrl);
        } else {
            new AlertDialog.Builder(this)
                    .setTitle("Error")
                    .setMessage("Whoops! invalid access key or applicationId, please check your configuration")
                    .setPositiveButton(getString(R.string.dialog_option_ok), (dialog, which) -> finish())
                    .setCancelable(false)
                    .show();
        }
        Log.d(TAG, "Start request config");
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
                displayApiResults(resp);
            }

            public void onFailure(@NonNull Call<CallbackConfig> call, @NonNull Throwable th) {
                Log.e(TAG, "initialize failed");
                startMainActivity();
            }
        });
    }

    private void displayApiResults(CallbackConfig resp) {

        if (resp != null) {
            app = resp.app;
            ads = resp.ads;
            navigationList = resp.menus;

            if (app.status.equals("1")) {
                adsManager.saveConfig(sharedPref, app);
                adsManager.saveAds(adsPref, ads);

                dbNavigation.truncateTableMenu(DbNavigation.TABLE_MENU);

                new Handler().postDelayed(()-> {
                    dbNavigation.addListCategory(navigationList, DbNavigation.TABLE_MENU);
                    startMainActivity();
                }, 100);

                Log.d(TAG, "App status is live");
            } else {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(app.redirect_url)));
                finish();
                Log.d(TAG, "App status is suspended");
            }
            Log.d(TAG, "initialize success");
        } else {
            Log.d(TAG, "initialize failed");
            startMainActivity();
        }

    }

    private void startMainActivity() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        new Handler(Looper.getMainLooper()).postDelayed(this::finish, DELAY_SPLASH);
    }

}
