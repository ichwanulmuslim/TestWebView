package com.app.webdroid.callbacks;

import com.app.webdroid.models.Ads;
import com.app.webdroid.models.App;
import com.app.webdroid.models.Navigation;
import com.app.webdroid.models.Notification;

import java.util.ArrayList;
import java.util.List;

public class CallbackConfig {

    public App app = null;
    public List<Navigation> menus = new ArrayList<>();
    public Notification notification = null;
    public Ads ads = null;

}