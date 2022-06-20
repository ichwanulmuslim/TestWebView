package com.app.webdroid.databases.prefs;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPref {

    Context context;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public SharedPref(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences("blog_setting", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    //blog credentials
    public void saveBlogCredentials(String bloggerId, String apiKey) {
        editor.putString("blogger_id", bloggerId);
        editor.putString("api_key", apiKey);
        editor.apply();
    }

    public Boolean getIsDarkTheme() {
        return sharedPreferences.getBoolean("theme", false);
    }

    public void setIsDarkTheme(Boolean isDarkTheme) {
        editor.putBoolean("theme", isDarkTheme);
        editor.apply();
    }

    public Integer getLastItemPosition() {
        return sharedPreferences.getInt("item_position", 0);
    }

    public void setLastItemPosition(int position) {
        editor.putInt("item_position", position);
        editor.apply();
    }

    public Boolean getIsNotificationOn() {
        return sharedPreferences.getBoolean("notification", true);
    }

    public void setIsNotificationOn(Boolean isNotificationOn) {
        editor.putBoolean("notification", isNotificationOn);
        editor.apply();
    }

    public void saveConfig(String toolbar, String navigationDrawer, String geolocation, String cache, String openLinkInExternalBrowser, String privacyPolicyUrl, String moreAppsUrl, String redirectUrl) {
        editor.putString("toolbar", toolbar);
        editor.putString("navigation_drawer", navigationDrawer);
        editor.putString("geolocation", geolocation);
        editor.putString("cache", cache);
        editor.putString("open_link_in_external_browser", openLinkInExternalBrowser);
        editor.putString("privacy_policy_url", privacyPolicyUrl);
        editor.putString("more_apps_url", moreAppsUrl);
        editor.putString("redirect_url", redirectUrl);
        editor.apply();
    }

    public String getToolbar() {
        return sharedPreferences.getString("toolbar", "true");
    }

    public String getNavigationDrawer() {
        return sharedPreferences.getString("navigation_drawer", "true");
    }

    public String getGeolocation() {
        return sharedPreferences.getString("geolocation", "true");
    }

    public String getCache() {
        return sharedPreferences.getString("cache", "true");
    }

    public String getOpenLinkInExternalBrowser() {
        return sharedPreferences.getString("open_link_in_external_browser", "true");
    }

    public String getPrivacyPolicyUrl() {
        return sharedPreferences.getString("privacy_policy_url", "");
    }

    public String getMoreAppsUrl() {
        return sharedPreferences.getString("more_apps_url", "");
    }

    public String getRedirectUrl() {
        return sharedPreferences.getString("redirect_url", "");
    }

    public void resetPageToken() {
        sharedPreferences.edit().remove("page_token").apply();
    }

    public Integer getFontSize() {
        return sharedPreferences.getInt("font_size", 2);
    }

    public void updateFontSize(int font_size) {
        editor.putInt("font_size", font_size);
        editor.apply();
    }

    public Integer getInAppReviewToken() {
        return sharedPreferences.getInt("in_app_review_token", 0);
    }

    public void updateInAppReviewToken(int value) {
        editor.putInt("in_app_review_token", value);
        editor.apply();
    }

}
