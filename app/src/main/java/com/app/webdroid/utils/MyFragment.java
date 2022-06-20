package com.app.webdroid.utils;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import java.util.LinkedList;
import java.util.List;

public class MyFragment extends Fragment {

    List<Runnable> lists = new LinkedList<>();
    private final Object object = new Object();
    private Boolean aBoolean = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        synchronized (object) {
            aBoolean = true;
            int i = lists.size();
            while (i-- > 0) {
                Runnable runnable = lists.remove(0);
                runNow(runnable);
            }
        }
    }

    private void addPending(Runnable runnable) {
        synchronized (object) {
            lists.add(runnable);
        }
    }

    protected void runTaskCallback(Runnable runnable) {
        if (aBoolean) runNow(runnable);
        else addPending(runnable);
    }

    private void runNow(Runnable runnable) {
        if (getActivity() != null)
        getActivity().runOnUiThread(runnable);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        synchronized (object) {
            aBoolean = false;
        }
    }

}
