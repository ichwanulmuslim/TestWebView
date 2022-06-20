package com.app.webdroid.listener;

import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebView;

import java.lang.ref.WeakReference;

public class WebViewOnKeyListener implements View.OnKeyListener {

    private final WeakReference<DrawerStateListener> mDrawerStateListener;

    public WebViewOnKeyListener(DrawerStateListener drawerStateListener) {
        mDrawerStateListener = new WeakReference<>(drawerStateListener);
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            WebView webView = (WebView) v;
            DrawerStateListener drawerStateListener = mDrawerStateListener.get();
            if (drawerStateListener != null && keyCode == KeyEvent.KEYCODE_BACK) {
                if (webView.canGoBack() && !drawerStateListener.isDrawerOpen()) {
                    webView.goBack();
                } else {
                    drawerStateListener.onBackButtonPressed();
                }
                return true;
            }
        }
        return false;
    }
}
