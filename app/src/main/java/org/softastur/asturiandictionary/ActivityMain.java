package org.softastur.asturiandictionary;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Locale;

/**
 *  This code implements a number of different features in Android.  For reference:
 *
 *  Text processing (for definitions, inline translation)
 *  https://medium.com/google-developers/custom-text-selection-actions-with-action-process-text-191f792d2999#.7rt2g9npw
 *
 */


public class ActivityMain extends AppCompatActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks,
        FragmentDictionary.DictionaryViewCallbacks,
        SearchView.OnQueryTextListener,
        View.OnClickListener,
        MenuItemCompat.OnActionExpandListener,
        AdapterView.OnItemClickListener,
        FragmentSettings.OnSettingsFragmentListener,
        FragmentFavorites.OnLoadEntry
         {

    MenuItem searchMenuItem;
    private static final String TAG = "ASTMain";


    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;
    private Toolbar mToolbar;
    private SearchResultAdapter searchAdapter;

    public final int ACTION_NONE        = 0;
    public final int ACTION_LOOKUP_WORD = 1;
    public final int ACTION_TRANSLATE   = 2;

    private int currentSection = -1;
    private static final String STATE_SECTION = "SECTION";
    private static final int SECTION_HOME = 0;
    private static final int SECTION_DICTIONARY = 1;
    private static final int SECTION_TRANSLATE = 2;
    private static final int SECTION_SETTINGS = 3;
    private static final int SECTION_SPELLCHECK = 4;
    private static final int SECTION_ABOUT = 5;
    private static final int SECTION_FAVORITES = 6;
    private static final int SECTION_ENTRY = 7;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // MUST BE FIRST -- changes language to Asturian if needed
        checkInterfaceLocale();

        // UNIVERSAL SET UP
        // Here we load the views, toolbar, and drawer.  This should be done *always*
        // and is needed in order to properly set up
        setContentView(R.layout.activity_main);
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        searchAdapter = new SearchResultAdapter(this,0, new ArrayList());
        ListView lv = ((ListView)findViewById(R.id.search_suggestions));
        lv.setAdapter(searchAdapter);
        lv.setOnItemClickListener(this);


        // RESTORE THE SAVED STATE
        // All of this code could probably be later moved to another block or subroutine.
        // Basically, check the section, and load it.  Each section may have certain special
        // variables that need to be checked.
        if(savedInstanceState != null) {
            // we probably had a screen rotation or other configuration change
            int section = savedInstanceState.getInt(STATE_SECTION,SECTION_HOME);

            // determine the screen we were on and load it.
            loadSection(section);

            // retrieve old search suggestions if they were there

        }else if(getIntent().getIntExtra("action",ACTION_NONE) == ACTION_LOOKUP_WORD) {
            // User wanted to look up a word via the extra.
            // Note that currently the server does not currently attempt to do
            // any spell checking on the word, which grreatly reduces its utility.
            loadSection(SECTION_ENTRY);
            // TODO FINISH
        }else if(getIntent().getIntExtra("action",ACTION_NONE) == ACTION_TRANSLATE) {
            // TODO do translation code based on below sample
            //Intent intent = new Intent();
            //intent.putExtra(Intent.EXTRA_PROCESS_TEXT, replacementText);
            //setResult(RESULT_OK, intent);
        }else{
            // fresh launch of the application
            loadSection(SECTION_HOME);
        }


        // old code, may be of use, but should stay commented
        // out unless needed for some specific reason
        //PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        //mToolbar = new Toolbar(getApplicationContext());


        mTitle = getTitle();



        // Set up the drawer.
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        int section;
        switch(position) {
            // case 0 is the icon and cannot be clicked
            case 1: section = SECTION_HOME; break;
            case 2: section = SECTION_FAVORITES; break;
            case 3: section = SECTION_SPELLCHECK; break;
            case 4: section = SECTION_TRANSLATE; break;
            case 5: section = SECTION_SETTINGS; break;
            case 6: section = SECTION_ABOUT; break;
            default:
                mTitle = getString(R.string.title_section1);
                mTitle = getString(R.string.title_section6);
                mTitle = getString(R.string.title_section2);
                mTitle = getString(R.string.title_section3);
                mTitle = getString(R.string.title_section4);
                mTitle = getString(R.string.title_section5);
                section = SECTION_HOME;
                break;
        }
        loadSection(section);
    }

    private void loadSection(int section) {

        // bail out if we've already loaded it
        if(section == currentSection) return;

        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment;

        // first we attempt to find it, because if it exists, we switch to it.
        // otherwise, we have to create it and then add it, which is a slightly
        // different process
        switch(section) {
            case SECTION_ABOUT:
                fragment = fragmentManager.findFragmentByTag("about"); // TODO static magic numbers
                break;
            case SECTION_SETTINGS:
                fragment = fragmentManager.findFragmentByTag("settings"); // TODO static magic numbers
                break;
            case SECTION_SPELLCHECK:
                fragment = fragmentManager.findFragmentByTag("spellcheck"); // TODO static magic numbers
                break;
            case SECTION_TRANSLATE:
                fragment = fragmentManager.findFragmentByTag("translate"); // TODO static magic numbers
                break;
            case SECTION_FAVORITES:
                fragment = fragmentManager.findFragmentByTag("favorites"); // TODO static magic numbers
                break;
            case SECTION_HOME:
            case SECTION_DICTIONARY:
            default:
                fragment = fragmentManager.findFragmentByTag("dictionary"); // TODO static magic numbers
                //fragment = PlaceholderFragment.newInstance(position + 1);
                break;
        }

        if(fragment != null) {
            fragmentManager.beginTransaction().replace(R.id.container, fragment).addToBackStack(null).commit();
        }else{
            // in this case, we need to make it first
            String tag;
            switch(section) {
                case SECTION_ABOUT: fragment = FragmentAbout.newInstance("",""); tag = "about"; break;
                case SECTION_SETTINGS: fragment = FragmentSettings.newInstance("",""); tag = "settings"; break;
                case SECTION_SPELLCHECK: fragment = FragmentSpelling.newInstance("",""); tag = "spellcheck"; break;
                case SECTION_TRANSLATE: fragment = FragmentTranslation.newInstance("",""); tag = "translate"; break;
                case SECTION_FAVORITES: fragment = FragmentFavorites.newInstance("",""); tag = "favorites"; break;
                case SECTION_DICTIONARY:
                case SECTION_HOME:default: fragment = FragmentDictionary.newInstance(FragmentDictionary.NO_ACTION,0); tag = "dictionary"; break;
            }

            fragmentManager.beginTransaction().replace(R.id.container,fragment, tag).addToBackStack(null).commit();
        }

        currentSection = section;
        //fragmentManager.beginTransaction()
        //        .replace(R.id.container, fragment)
        //        .commit();
        // TODO searchAdapter.doQuery(string);

    }

    public void onSectionAttached(int number) {
        updateTitleBar();
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        //actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        updateTitleBar();
    }

             private void updateTitleBar() {
                 String newTitle;
                 switch (currentSection) {
                     case SECTION_DICTIONARY:
                         SECTION_HOME:
                         newTitle = getString(R.string.title_section1);
                         break;
                     case SECTION_FAVORITES:
                         newTitle = getString(R.string.title_section6);
                         break;
                     case SECTION_SPELLCHECK:
                         newTitle = getString(R.string.title_section2);
                         break;
                     case SECTION_TRANSLATE:
                         newTitle = getString(R.string.title_section3);
                         break;
                     case SECTION_SETTINGS:
                         newTitle = getString(R.string.title_section4);
                         break;
                     case SECTION_ABOUT:
                         newTitle = getString(R.string.title_section4);
                         break;
                     default:
                         newTitle = getString(R.string.title_section1);
                         break;
                 }

                 ActionBar actionBar = getSupportActionBar();
                 actionBar.setTitle(newTitle);
             }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);

            //SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
            MenuItem searchItem = menu.findItem(R.id.action_search);

            SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
            searchView.setOnQueryTextListener(this);
            searchView.setOnClickListener(this);

            searchMenuItem = menu.findItem(R.id.action_search);
            MenuItemCompat.setOnActionExpandListener(searchMenuItem, this);


            //searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
            //searchView.setIconifiedByDefault(false);


            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }else if(id == R.id.action_search) {
            System.out.println("asturianu - initiated search request");
            View suggestions = findViewById(R.id.search_suggestions);
            suggestions.setVisibility(View.VISIBLE);
            //onSearchRequested();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public void onDictionaryEntryInteraction(Uri uri) {


    }


    public boolean onQueryTextChange(String string) {
        System.out.println("asturianu test search " + string);
        // Only return results if we have more 3 letters or more
        if(string.length() > 0) {
            searchAdapter.doQuery(string);
        }
        return true; // tells the SearchView to do nothing
    }

    public boolean onQueryTextSubmit(String string) {
        System.out.println("asturianu test search submit " + string);
        return true; // tells the SearchView to do nothing
    }

    public void onClick(View view) {
        if(view.getId() == R.id.action_search) {

        }
        System.out.println("asturianu - got a click in main activity");

    }

    //@Override
    public boolean onMenuItemActionExpand(MenuItem item) {
        if(item.getItemId() == R.id.action_search) {
            // show the search results
            findViewById(R.id.search_suggestions).setVisibility(View.VISIBLE);
        }
        return true;
    }

    //@Override
    public boolean onMenuItemActionCollapse(MenuItem item) {
        //do what you want to when close the sesarchview
        //remember to return true;
        if(item.getItemId() == R.id.action_search) {
            // hide the search results
            findViewById(R.id.search_suggestions).setVisibility(View.GONE);
        }
        return true;
    }

    public void onItemClick(AdapterView adapterView, View view, int position, long id) {
        onLoadWord((int) id);
    }

    public void onLoadWord(int id) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment;
        fragment = FragmentDictionary.newInstance(FragmentDictionary.SHOW_ENTRY,id);
        fragmentManager.beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
        findViewById(R.id.search_suggestions).setVisibility(View.GONE);
        if(searchMenuItem != null) searchMenuItem.collapseActionView();
        mTitle = getString(R.string.title_section1);
    }

    public void onShowConjugations(int id) {
        Intent intent = new Intent(this, DictionaryConjugationView.class);
        intent.putExtra("id", id);
        startActivity(intent);
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
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);

            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((ActivityMain) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }

    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putInt(STATE_SECTION,currentSection);

        super.onSaveInstanceState(savedInstanceState);
    }

    private void checkInterfaceLocale() {
        boolean overrideLanguage =
                getPreferences(Context.MODE_PRIVATE)
                    .getBoolean(FragmentSettings.KEY_PREF_OVERRIDE_LANGUAGE,false);

        if(overrideLanguage) {
            Locale locale = new Locale("ast");
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getBaseContext().getResources().updateConfiguration(
                    config,
                    getBaseContext().getResources().getDisplayMetrics()
            );
        }
    }

    public void onNeedsRestart() {
        restart(this,1);
    }

    public void restart(Context context, int delay) {
        if (delay < 1) {
            delay = 1;
        }
        Intent restartIntent = context.getPackageManager()
                .getLaunchIntentForPackage(context.getPackageName());
        PendingIntent intent = PendingIntent.getActivity(
                context,
                0,
                restartIntent, PendingIntent.FLAG_ONE_SHOT
        );
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        manager.set(AlarmManager.RTC, System.currentTimeMillis() + delay, intent);
        finish();
        System.exit(0);
    }

    public void onLoadEntry(int i) {
        onLoadWord(i);
    }
 }
