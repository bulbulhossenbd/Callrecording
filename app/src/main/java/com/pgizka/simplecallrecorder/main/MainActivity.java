package com.pgizka.simplecallrecorder.main;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.support.v4.widget.DrawerLayout;

import com.pgizka.simplecallrecorder.contacts.ContactsFragment;
import com.pgizka.simplecallrecorder.options.OptionsFragment;
import com.pgizka.simplecallrecorder.R;
import com.pgizka.simplecallrecorder.recordings.RecordingsFragment;
import com.pgizka.simplecallrecorder.util.SettingsActivity;
import com.pgizka.simplecallrecorder.util.UpdateUserData;


public class MainActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {
    static final String TAG = MainActivity.class.getSimpleName();
    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    RecordingsFragment recordingsFragment;
    OptionsFragment optionsFragment;
    ContactsFragment contactsFragment;

    int currentFragment = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "on Create");

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);

        //onNavigationDrawerItemSelected(position);

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
        new UpdateUserData(this).execute();
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        Log.d(TAG, "on navigation drawer item selected");
        if (position == 6) {
            startActivity(new Intent(this, SettingsActivity.class));
        } else if(position >= 3 && position <= 5){
            // TODO launch last visited record or contact
        } else {
            currentFragment = position;
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.container, getProperFragment(position))
                    .commit();
        }
    }

    public Fragment getProperFragment(int position){
        switch (position) {
            case 0:
                if(recordingsFragment == null){
                    recordingsFragment = new RecordingsFragment();
                }
                return recordingsFragment;
                //return new RecordingsFragment();
            case 1:
                if(optionsFragment == null){
                    optionsFragment = new OptionsFragment();
                }
                return optionsFragment;
                //return new OptionsFragment();
            case 2:
                if(contactsFragment == null){
                    contactsFragment = new ContactsFragment();
                }
                return contactsFragment;
                //return new ContactsFragment();
            default:
                throw new UnsupportedOperationException("There is no fragment");
        }
    }

    public String getCurrentItemTitle() {
        switch (currentFragment) {
            case 0:
                return getString(R.string.title_section_recordings);
            case 1:
                return getString(R.string.title_section_options);
            case 2:
                return getString(R.string.title_section_contacts);
            default:
                return getTitle().toString();
        }
    }

    public void setUpActionBar(String title) {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(title);
        Log.d(TAG, "on restore action bar " + title);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            //getMenuInflater().inflate(R.menu.main, menu);
            setUpActionBar(getCurrentItemTitle());
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }
}
