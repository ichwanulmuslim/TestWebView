package com.app.webdroid.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.MailTo;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.app.webdroid.BuildConfig;
import com.app.webdroid.Config;
import com.app.webdroid.R;
import com.app.webdroid.databases.prefs.SharedPref;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Utils {

    private static final String TAG = "utils";
    private Uri mCapturedImageUri;
    private static final int LOCATION_SETTINGS_PROMPT_DURATION = 10000;
    public static final int REQUEST_PERMISSION_READ_EXTERNAL_STORAGE_AND_CAMERA = 101;
    public static final int REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE = 102;
    public static final int REQUEST_PERMISSION_ACCESS_LOCATION = 103;
    public static final String[] DOWNLOAD_FILE_TYPES = {".*zip$", ".*rar$", ".*pdf$", ".*doc$", ".*xls$", ".*mp3$", ".*wma$", ".*ogg$", ".*m4a$", ".*wav$", ".*avi$", ".*mov$", ".*mp4$", ".*mpg$", ".*3gp$", ".*drive.google.com.*download.*", ".*dropbox.com/s/.*"};
    public static final String[] LINKS_OPENED_IN_EXTERNAL_BROWSER = {"target=blank", "target=external", "play.google.com/store", "youtube.com/watch", "facebook.com/sharer", "twitter.com/share", "plus.google.com/share"};
    public static final String[] LINKS_OPENED_IN_INTERNAL_WEBVIEW = {"target=webview", "target=internal"};

    public Utils() {

    }

    public static void getTheme(Context context) {
        SharedPref sharedPref = new SharedPref(context);
        if (sharedPref.getIsDarkTheme()) {
            context.setTheme(R.style.AppDarkTheme);
        } else {
            context.setTheme(R.style.AppTheme);
        }
    }

    public static void setupToolbar(AppCompatActivity activity, Toolbar toolbar, String title, boolean backButton) {
        SharedPref sharedPref = new SharedPref(activity);
        activity.setSupportActionBar(toolbar);
        if (sharedPref.getIsDarkTheme()) {
            toolbar.setBackgroundColor(activity.getResources().getColor(R.color.colorToolbarDark));
        } else {
            toolbar.setBackgroundColor(activity.getResources().getColor(R.color.colorPrimary));
        }
        final ActionBar actionBar = activity.getSupportActionBar();
        if (actionBar != null) {
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(backButton);
            activity.getSupportActionBar().setHomeButtonEnabled(backButton);
            activity.getSupportActionBar().setTitle(title);
        }
    }

    public static void setNavigation(Activity activity) {
        SharedPref sharedPref = new SharedPref(activity);
        if (sharedPref.getIsDarkTheme()) {
            Utils.darkNavigation(activity);
        } else {
            Utils.lightNavigation(activity);
        }
        setLayoutDirection(activity);
    }

    public static void setLayoutDirection(Activity activity) {
        if (Config.ENABLE_RTL_MODE) {
            activity.getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        }
    }

    public static void darkNavigation(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            activity.getWindow().setNavigationBarColor(ContextCompat.getColor(activity, R.color.colorStatusBarDark));
            activity.getWindow().setStatusBarColor(ContextCompat.getColor(activity, R.color.colorStatusBarDark));
            activity.getWindow().getDecorView().setSystemUiVisibility(0);
        }
    }

    public static void lightNavigation(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            activity.getWindow().setNavigationBarColor(ContextCompat.getColor(activity, R.color.colorWhite));
            activity.getWindow().setStatusBarColor(ContextCompat.getColor(activity, R.color.colorPrimaryDark));
            activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        }
    }

    public static boolean isDownloadableFile(String url) {
        url = url.toLowerCase();
        for (String type : DOWNLOAD_FILE_TYPES) {
            if (url.matches(type)) return true;
        }
        return false;
    }

    public static String getFileName(String url) {
        int idx = url.indexOf("?");
        if (idx > -1) {
            url = url.substring(0, idx);
        }
        url = url.toLowerCase();
        idx = url.lastIndexOf("/");
        if (idx > -1) {
            return url.substring(idx + 1);
        } else {
            return Long.toString(System.currentTimeMillis());
        }
    }

    public static void downloadFile(@NonNull Context context, @NonNull String url, @NonNull String fileName) {
        try {
            DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            Uri downloadUri = Uri.parse(url);
            DownloadManager.Request request = new DownloadManager.Request(downloadUri);
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE)
                    .setAllowedOverRoaming(false)
                    .setTitle(fileName)
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
                    .allowScanningByMediaScanner();
            downloadManager.enqueue(request);
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        }
    }

    public void setupChooserIntent(Intent chooserIntent) {
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Parcelable[]{createImageCaptureIntent()});
    }

    public Uri getCapturedImageUri() {
        Uri uri = null;
        if (mCapturedImageUri != null) {
            uri = mCapturedImageUri;
            mCapturedImageUri = null;
        }
        return uri;
    }

    public Uri[] getCapturedImageUris() {
        Uri[] uris = null;
        if (mCapturedImageUri != null) {
            uris = new Uri[]{mCapturedImageUri};
            mCapturedImageUri = null;
        }
        return uris;
    }

    private Intent createImageCaptureIntent() {
        mCapturedImageUri = getImageUri();
        Intent imageCaptureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        imageCaptureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mCapturedImageUri);
        return imageCaptureIntent;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private Uri getImageUri() {
        File externalDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File cameraDir = new File(externalDir.getAbsolutePath() + File.separator + "WebViewExample");
        cameraDir.mkdirs();
        String filePath = cameraDir.getAbsolutePath() + File.separator + System.currentTimeMillis() + ".jpg";
        return Uri.fromFile(new File(filePath));
    }

    public static boolean startIntentActivity(Context context, String url) {
        if (url != null && url.startsWith("mailto:")) {
            MailTo mailTo = MailTo.parse(url);
            Utils.startEmailActivity(context, mailTo.getTo(), mailTo.getSubject(), mailTo.getBody());
            return true;
        } else if (url != null && url.startsWith("tel:")) {
            Utils.startCallActivity(context, url);
            return true;
        } else if (url != null && url.startsWith("sms:")) {
            Utils.startSmsActivity(context, url);
            return true;
        } else if (url != null && url.startsWith("geo:")) {
            Utils.startMapSearchActivity(context, url);
            return true;
        } else if (url != null && url.startsWith("fb://")) {
            Utils.startWebActivity(context, url);
            return true;
        } else if (url != null && url.startsWith("twitter://")) {
            Utils.startWebActivity(context, url);
            return true;
        } else if (url != null && url.startsWith("whatsapp://")) {
            Utils.startWebActivity(context, url);
            return true;
        } else {
            return false;
        }
    }

    public static void startWebActivity(Context context, String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("StringBufferReplaceableByString")
    public static void startEmailActivity(Context context, String email, String subject, String text) {
        try {
            StringBuilder builder = new StringBuilder();
            builder.append("mailto:");
            builder.append(email);
            Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse(builder.toString()));
            intent.putExtra(Intent.EXTRA_SUBJECT, subject);
            intent.putExtra(Intent.EXTRA_TEXT, text);
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void startCallActivity(Context context, String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse(url));
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void startSmsActivity(Context context, String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse(url));
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void startMapSearchActivity(Context context, String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static boolean isLocationEnabled(Context context) {
        int locationMode;
        try {
            locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
            return false;
        }
        return locationMode != Settings.Secure.LOCATION_MODE_OFF;
    }

    @SuppressLint("WrongConstant")
    public static void showLocationSettingsPrompt(final View view) {
        Snackbar.make(view, "Device location is disabled", LOCATION_SETTINGS_PROMPT_DURATION)
                .setAction("Settings", v -> {
                    final Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    view.getContext().startActivity(intent);
                })
                .show();
    }

    public static boolean isOnline(@NonNull Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isAvailable() && networkInfo.isConnected());
    }

    public static int getType(@NonNull Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null) {
            return networkInfo.getType();
        } else {
            return -1;
        }
    }

    public static String getTypeName(@NonNull Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if (networkInfo != null) {
            return networkInfo.getTypeName();
        } else {
            return null;
        }
    }

    public static boolean checkPermissionReadExternalStorageAndCamera(final Fragment fragment) {
        return checkPermissions(fragment, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA},
                new int[]{R.string.permission_read_external_storage, R.string.permission_camera},
                REQUEST_PERMISSION_READ_EXTERNAL_STORAGE_AND_CAMERA);
    }

    public static boolean checkPermissionWriteExternalStorage(final Fragment fragment) {
        return checkPermissions(fragment, R.string.permission_write_external_storage);
    }

    @SuppressWarnings("UnusedReturnValue")
    public static boolean checkPermissionAccessLocation(final Fragment fragment) {
        return checkPermissions(fragment, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                new int[]{R.string.permission_access_location, R.string.permission_access_location},
                REQUEST_PERMISSION_ACCESS_LOCATION);
    }

    private static boolean checkPermissions(final Fragment fragment, final int explanation) {
        final int result = ContextCompat.checkSelfPermission(Objects.requireNonNull(fragment.getActivity()), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (result != PackageManager.PERMISSION_GRANTED) {
            if (fragment.shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                Snackbar.make(Objects.requireNonNull(fragment.getView()), explanation, Snackbar.LENGTH_INDEFINITE)
                        .setAction(android.R.string.ok, v -> fragment.requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                Utils.REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE)).show();
            } else {
                fragment.requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, Utils.REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE);
            }
        }
        return result == PackageManager.PERMISSION_GRANTED;
    }

    @SuppressWarnings("ToArrayCallWithZeroLengthArrayArgument")
    private static boolean checkPermissions(final Fragment fragment, final String[] permissions, final int[] explanations, final int requestCode) {
        final int[] results = new int[permissions.length];
        for (int i = 0; i < permissions.length; i++) {
            results[i] = ContextCompat.checkSelfPermission(Objects.requireNonNull(fragment.getActivity()), permissions[i]);
        }
        final List<String> deniedPermissions = new ArrayList<>();
        for (int i = 0; i < results.length; i++) {
            if (results[i] != PackageManager.PERMISSION_GRANTED) {
                deniedPermissions.add(permissions[i]);
            }
        }
        if (!deniedPermissions.isEmpty()) {
            final String[] params = deniedPermissions.toArray(new String[deniedPermissions.size()]);
            boolean isShown = false;
            for (int i = 0; i < permissions.length; i++) {
                if (fragment.shouldShowRequestPermissionRationale(permissions[i])) {
                    Snackbar.make(Objects.requireNonNull(fragment.getView()), explanations[i], Snackbar.LENGTH_INDEFINITE)
                            .setAction(android.R.string.ok, v -> fragment.requestPermissions(params, requestCode)).show();
                    isShown = true;
                    break;
                }
            }
            if (!isShown) {
                fragment.requestPermissions(params, requestCode);
            }
        }
        return deniedPermissions.isEmpty();
    }

    public static void shareApp(Activity activity, String title) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, title + "\n\n" + "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID);
        sendIntent.setType("text/plain");
        activity.startActivity(sendIntent);
    }

    public static void shareContent(Activity activity, String title, String message) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, title + "\n\n" + message + "\n\n" + "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID);
        sendIntent.setType("text/plain");
        activity.startActivity(sendIntent);
    }

    public static void rateApp(Activity activity) {
        activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID)));
    }

    public static void moreApps(Activity activity, String moreAppsUrl) {
        activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(moreAppsUrl)));
    }

    public static void showAboutDialog(Activity activity) {
        LayoutInflater layoutInflater = LayoutInflater.from(activity);
        View view = layoutInflater.inflate(R.layout.dialog_about, null);
        TextView txtAppVersion = view.findViewById(R.id.txt_app_version);
        txtAppVersion.setText(activity.getString(R.string.msg_about_version) + " " + BuildConfig.VERSION_CODE + " (" + BuildConfig.VERSION_NAME + ")");
        final AlertDialog.Builder alert = new AlertDialog.Builder(activity);
        alert.setView(view);
        alert.setPositiveButton(R.string.dialog_option_ok, (dialog, which) -> dialog.dismiss());
        alert.show();
    }

    public static void showPrivacyPolicyDialog(Activity activity) {

        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        LayoutInflater layoutInflater = LayoutInflater.from(activity);
        View view = layoutInflater.inflate(R.layout.dialog_privacy, null);

        SharedPref sharedPref = new SharedPref(activity);

        ProgressBar progressBar = view.findViewById(R.id.progressBar);
        ImageButton btnClose = view.findViewById(R.id.btn_close);

        WebView webView = view.findViewById(R.id.webView);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                new Handler().postDelayed(() -> progressBar.setVisibility(View.INVISIBLE), 500);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url != null && (url.startsWith("http") || url.startsWith("https"))) {
                    activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                }
                return true;
            }
        });
        webView.loadUrl(sharedPref.getPrivacyPolicyUrl());
        builder.setCancelable(false);
        builder.setView(view);

        final AlertDialog dialog = builder.create();
        btnClose.setOnClickListener(v -> new Handler().postDelayed(dialog::dismiss, 250));

        dialog.show();
    }

}
