package com.app.webdroid.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentSender;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.webdroid.BuildConfig;
import com.app.webdroid.Config;
import com.app.webdroid.R;
import com.app.webdroid.adapters.AdapterNavigation;
import com.app.webdroid.databases.prefs.AdsPref;
import com.app.webdroid.databases.prefs.SharedPref;
import com.app.webdroid.databases.sqlite.DbNavigation;
import com.app.webdroid.fragments.FragmentWebView;
import com.app.webdroid.listener.DrawerStateListener;
import com.app.webdroid.listener.LoadUrlListener;
import com.app.webdroid.models.Navigation;
import com.app.webdroid.utils.AdsManager;
import com.app.webdroid.utils.Utils;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;
import com.google.android.play.core.tasks.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements LoadUrlListener, DrawerStateListener {

    private long exitTime = 0;
    private final static String COLLAPSING_TOOLBAR_FRAGMENT_TAG = "collapsing_toolbar";
    private final static String SELECTED_TAG = "selected_index";
    private static int selectedIndex;
    private final static int COLLAPSING_TOOLBAR = 0;
    private DrawerLayout drawerLayout;
    private FragmentManager fragmentManager;
    ActionBarDrawerToggle actionBarDrawerToggle;
    NavigationView navigationView;
    RecyclerView recyclerView;
    DbNavigation dbNavigation;
    List<Navigation> items;
    AdsManager adsManager;
    SharedPref sharedPref;
    AdsPref adsPref;
    CoordinatorLayout parentView;
    public static final int IMMEDIATE_APP_UPDATE_REQ_CODE = 124;
    private AppUpdateManager appUpdateManager;
    private BottomSheetDialog mBottomSheetDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.getTheme(this);
        setContentView(R.layout.activity_main);
        Utils.setNavigation(this);

        fragmentManager = getSupportFragmentManager();

        sharedPref = new SharedPref(this);

        adsPref = new AdsPref(this);
        adsManager = new AdsManager(this);
        adsManager.initializeAd();
        adsManager.updateConsentStatus();
        adsManager.loadBannerAd(1);
        adsManager.loadInterstitialAd(1, adsPref.getInterstitialAdInterval());

        parentView = findViewById(R.id.parent_view);
        navigationView = findViewById(R.id.navigationView);

        if (sharedPref.getIsDarkTheme()) {
            navigationView.setBackgroundColor(getResources().getColor(R.color.colorBackgroundDark));
        } else {
            navigationView.setBackgroundColor(getResources().getColor(R.color.colorBackgroundLight));
        }

        drawerLayout = findViewById(R.id.drawer_layout);
        if (!sharedPref.getNavigationDrawer().equals("true")) {
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        }

        selectedIndex = COLLAPSING_TOOLBAR;
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, new FragmentWebView(), COLLAPSING_TOOLBAR_FRAGMENT_TAG).commit();

        dbNavigation = new DbNavigation(this);
        items = dbNavigation.getAllMenu(DbNavigation.TABLE_MENU);

        appUpdateManager = AppUpdateManagerFactory.create(getApplicationContext());
        if (!BuildConfig.DEBUG) {
            inAppUpdate();
            inAppReview();
        }

        notificationOpenHandler();

    }

    public void notificationOpenHandler() {
        String title = getIntent().getStringExtra("title");
        String link = getIntent().getStringExtra("link");
        if (getIntent().hasExtra("unique_id")) {
            if (link != null && !link.equals("")) {
                loadWebPage(title, "url", link);
                loadNavigationMenu(false);
                sharedPref.setLastItemPosition(-1);
            } else {
                loadWebPage(items.get(0).name, items.get(0).type, items.get(0).url);
                loadNavigationMenu(true);
                sharedPref.setLastItemPosition(0);
            }
        } else {
            loadWebPage(items.get(0).name, items.get(0).type, items.get(0).url);
            loadNavigationMenu(true);
            sharedPref.setLastItemPosition(0);
        }
    }

    private void loadNavigationMenu(boolean selectFirstItem) {

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        AdapterNavigation adapterNavigation = new AdapterNavigation(this, new ArrayList<>());
        adapterNavigation.setListData(items);

        recyclerView.setAdapter(adapterNavigation);

        if (selectFirstItem) {
            recyclerView.postDelayed(() -> Objects.requireNonNull(recyclerView.findViewHolderForAdapterPosition(0)).itemView.performClick(), 10);
        }

        // on item list clicked
        adapterNavigation.setOnItemClickListener((v, obj, position) -> drawerLayout.closeDrawer(GravityCompat.START));

    }

    public void loadWebPage(String name, String type, String url) {
        new Handler().postDelayed(() -> {
            FragmentWebView argumentFragment = new FragmentWebView();
            Bundle data = new Bundle();
            data.putString("name", name);
            data.putString("type", type);
            data.putString("url", url);
            argumentFragment.setArguments(data);
            fragmentManager.beginTransaction().replace(R.id.fragment_container, argumentFragment).commit();
        }, 250);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(SELECTED_TAG, selectedIndex);
    }

    @Override
    public void onLoadUrl(String url) {
        adsManager.showInterstitialAd();
    }

    public void setupNavigationDrawer(Toolbar toolbar) {
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, 0, 0) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }
        };
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            exitApp();
        }
    }

    @Override
    public boolean isDrawerOpen() {
        return drawerLayout.isDrawerOpen(GravityCompat.START);
    }

    @Override
    public void onBackButtonPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            exitApp();
        }
    }

    public void exitApp() {
        if (Config.SHOW_EXIT_DIALOG) {
            showBottomSheetExitDialog();
        } else {
            if ((System.currentTimeMillis() - exitTime) > 2000) {
                showSnackBar(getString(R.string.exit_msg));
                exitTime = System.currentTimeMillis();
            } else {
                finish();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public AssetManager getAssets() {
        return getResources().getAssets();
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_settings:
                startActivity(new Intent(getApplicationContext(), ActivitySettings.class));
                return true;

            case R.id.menu_share:
                Utils.shareApp(this, getString(R.string.share_text));
                return true;

            case R.id.menu_rate:
                Utils.rateApp(this);
                return true;

            case R.id.menu_more:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(sharedPref.getMoreAppsUrl())));
                return true;

            case R.id.menu_privacy:
                Utils.showPrivacyPolicyDialog(this);
                return true;

            case R.id.menu_about:
                Utils.showAboutDialog(this);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showBottomSheetExitDialog() {
        @SuppressLint("InflateParams") final View view = getLayoutInflater().inflate(R.layout.dialog_exit, null);
        FrameLayout lytBottomSheet = view.findViewById(R.id.bottom_sheet);
        if (sharedPref.getIsDarkTheme()) {
            lytBottomSheet.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_rounded_dark));
        } else {
            lytBottomSheet.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_rounded_light));
        }
        Button btnRate = view.findViewById(R.id.btn_rate);
        Button btnShare = view.findViewById(R.id.btn_share);
        Button btnExit = view.findViewById(R.id.btn_exit);

        adsManager.loadNativeAdView(view, 1);

        btnRate.setOnClickListener(v -> {
            Utils.rateApp(this);
            mBottomSheetDialog.dismiss();
        });

        btnShare.setOnClickListener(v -> {
            Utils.shareApp(this, getString(R.string.share_text));
            mBottomSheetDialog.dismiss();
        });

        btnExit.setOnClickListener(v -> {
            finish();
            mBottomSheetDialog.dismiss();
        });

        if (sharedPref.getIsDarkTheme()) {
            if (Config.ENABLE_RTL_MODE) {
                mBottomSheetDialog = new BottomSheetDialog(this, R.style.SheetDialogDarkRTL);
            } else {
                mBottomSheetDialog = new BottomSheetDialog(this, R.style.SheetDialogDark);
            }
        } else {
            if (Config.ENABLE_RTL_MODE) {
                mBottomSheetDialog = new BottomSheetDialog(this, R.style.SheetDialogLightRTL);
            } else {
                mBottomSheetDialog = new BottomSheetDialog(this, R.style.SheetDialogLight);
            }
        }

        mBottomSheetDialog.setContentView(view);
        mBottomSheetDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        @SuppressWarnings("rawtypes") BottomSheetBehavior bottomSheetBehavior = mBottomSheetDialog.getBehavior();
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });

        mBottomSheetDialog.show();
        mBottomSheetDialog.setOnDismissListener(dialog -> mBottomSheetDialog = null);

    }

    private void inAppReview() {
        if (sharedPref.getInAppReviewToken() <= 3) {
            sharedPref.updateInAppReviewToken(sharedPref.getInAppReviewToken() + 1);
        } else {
            ReviewManager manager = ReviewManagerFactory.create(this);
            Task<ReviewInfo> request = manager.requestReviewFlow();
            request.addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    ReviewInfo reviewInfo = task.getResult();
                    manager.launchReviewFlow(MainActivity.this, reviewInfo).addOnFailureListener(e -> {
                    }).addOnCompleteListener(complete -> {
                            }
                    ).addOnFailureListener(failure -> {
                    });
                }
            }).addOnFailureListener(failure -> Log.d("In-App Review", "In-App Request Failed " + failure));
        }
    }

    private void inAppUpdate() {
        Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();
        appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> {
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                startUpdateFlow(appUpdateInfo);
            } else if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                startUpdateFlow(appUpdateInfo);
            }
        });
    }

    private void startUpdateFlow(AppUpdateInfo appUpdateInfo) {
        try {
            appUpdateManager.startUpdateFlowForResult(appUpdateInfo, AppUpdateType.IMMEDIATE, this, IMMEDIATE_APP_UPDATE_REQ_CODE);
        } catch (IntentSender.SendIntentException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (fragment != null) {
            fragment.onActivityResult(requestCode, resultCode, intent);
        }
        if (requestCode == IMMEDIATE_APP_UPDATE_REQ_CODE) {
            if (resultCode == RESULT_CANCELED) {
                showSnackBar(getString(R.string.msg_cancel_update));
            } else if (resultCode == RESULT_OK) {
                showSnackBar(getString(R.string.msg_success_update));
            } else {
                showSnackBar(getString(R.string.msg_failed_update));
                inAppUpdate();
            }
        }
    }

    public void showSnackBar(String message) {
        Snackbar.make(parentView, message, Snackbar.LENGTH_SHORT).show();
    }

}
