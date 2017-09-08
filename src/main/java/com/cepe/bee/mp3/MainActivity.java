package com.cepe.bee.mp3;

import java.io.File;
import java.util.Locale;

import android.content.Context;
import android.os.Environment;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.Tab;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import com.startapp.android.publish.StartAppAd;
import com.startapp.android.publish.StartAppSDK;
import com.startapp.android.publish.splash.SplashConfig;

import mp3download.musicman.com.simplemp3download.R;

public class MainActivity extends AppCompatActivity implements ActionBar.OnNavigationListener, ActionBar.TabListener {

    SectionsPagerAdapter mSectionsPagerAdapter;
    ViewPager mViewPager;

    Fragment appWallFragment;
    Fragment searchFragment;
    Fragment downloadFragment;
    FragmentManager manager;
    FragmentTransaction transaction;

    private StartAppAd startAppAd = new StartAppAd(this);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        StartAppSDK.init(this, "211183952", true);
        StartAppAd.showSplash(this, savedInstanceState,
                new SplashConfig().setTheme(SplashConfig.Theme.GLOOMY).setLogo(R.drawable.ic_launcher).setAppName(getString(R.string.app_name))
        );
        setContentView(R.layout.activity_main);

        appWallFragment = new AppWallFragment();
        searchFragment = new SearchFragment();
        downloadFragment = new DownloadFragment();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.container, searchFragment)
                .add(R.id.container, downloadFragment)
                .hide(downloadFragment)
                .commit();

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        final ActionBar actionBar = getSupportActionBar();

        //Prepare Pre-Requisites
        String path= Environment.getExternalStorageDirectory()+"/music/"+getString(R.string.app_name);
        boolean exists = (new File(path)).exists();
        if (!exists){
            new File(path).mkdirs();
        }
		
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        Tab tab = actionBar.newTab();
        tab.setText(R.string.fragment_one);
        tab.setTabListener(this);

        Tab tab2 = actionBar.newTab();
        tab2.setText(R.string.fragment_two);
        tab2.setTabListener(this);


        actionBar.addTab(tab);
        actionBar.addTab(tab2);
    }

    @Override
    protected void onSaveInstanceState (Bundle outState){
        super.onSaveInstanceState(outState);
        startAppAd.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        startAppAd.onBackPressed();
        super.onBackPressed();
    }

    @Override
    public void onPause() {
        super.onPause();
        startAppAd.onPause();
    }

    @Override
    public void onResume(){
        super.onResume();
        startAppAd.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(int i, long l) {
        Fragment fragment = null;
        android.app.FragmentManager fragmentManager = getFragmentManager();

        switch(i){
            case 0:
                fragment = new AppWallFragment();
                break;
            case 1:
                fragment = new DownloadFragment();
                break;
        }

        //getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment).commit();

        return true;
    }

    @Override
    public void onTabSelected(Tab tab, FragmentTransaction ft) {


        switch (tab.getPosition()){
            case 0:
                transaction = getSupportFragmentManager().beginTransaction();
                transaction.setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right);
                transaction.show(searchFragment).hide(downloadFragment).commit();
                break;

            case 1:
                transaction = getSupportFragmentManager().beginTransaction();
                transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left);
                transaction.show(downloadFragment).hide(searchFragment).commit();
                View view = this.getCurrentFocus();
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
                break;
        }

    }

    @Override
    public void onTabUnselected(Tab tab, FragmentTransaction ft) {

    }

    @Override
    public void onTabReselected(Tab tab, FragmentTransaction ft) {

    }


    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.title_section1).toUpperCase(l);
                case 1:
                    return getString(R.string.title_section2).toUpperCase(l);
                case 2:
                    return getString(R.string.title_section3).toUpperCase(l);
            }
            return null;
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_search, container, false);
            return rootView;
        }
    }

}
