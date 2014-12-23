package com.bengalbot.android.drawer;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bengalbot.android.R;


/**
 * Fragment that holds the view for the Navigation Drawer.
 *
 * Copyright (c) 2014 Pandora Media, Inc.
 */
public class NavigationDrawerFragment extends Fragment implements AdapterView.OnItemClickListener {

    private NavigationDrawerListener mDrawerListener;
    private DrawerLayout mDrawerLayout;
    private View mContentView;
    private ListView mDrawerListView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.navigation_drawer_fragment, container, false);

        RelativeLayout userContainer = (RelativeLayout) view.findViewById(R.id.user_container);

        TextView userText = (TextView) view.findViewById(R.id.user_text);
        userText.setText("Jenny Mendez");

        // Setup Nav Drawer List
        mDrawerListView = (ListView) view.findViewById(R.id.navigation_list);

        String[] values = new String[] { "Connect to BLE Devices", "My Presets",};


        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                R.layout.navigation_drawer_list_item, R.id.drawer_item_text, values);

        mDrawerListView.setAdapter(adapter);
        mDrawerListView.setOnItemClickListener(this);


        return view;
    }

    public void setup(int fragmentId, DrawerLayout drawerLayout, Toolbar toolbar) {
        mContentView = getActivity().findViewById(fragmentId);

        mDrawerLayout = drawerLayout;
        mDrawerListener = new NavigationDrawerListener();

    }

    public void openDrawer() {
        mDrawerLayout.openDrawer(mContentView);
    }

    public void closeDrawer() {
        mDrawerLayout.closeDrawer(mContentView);
    }

    public boolean isDrawerOpen() {
        return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(mContentView);
    }

    public void toggleDrawer() {
        if (isDrawerOpen()) {
            closeDrawer();
        } else {
            openDrawer();
        }
    }



    public void unlockDrawer() {
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        ((NavigationDrawerCallbacks) getActivity()).onNavigationDrawerItemSelected(position);
    }

    private class NavigationDrawerListener implements DrawerLayout.DrawerListener {

        @Override
        public void onDrawerSlide(View drawerView, float slideOffset) {
        }

        @Override
        public void onDrawerOpened(View drawerView) {
            if (!isAdded()) return;
        }

        @Override
        public void onDrawerClosed(View drawerView) {
            if (!isAdded()) return;

        }

        @Override
        public void onDrawerStateChanged(int newState) {

        }
    }
}
