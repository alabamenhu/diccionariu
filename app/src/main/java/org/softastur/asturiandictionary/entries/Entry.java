package org.softastur.asturiandictionary.entries;

import android.app.DatePickerDialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.SharedPreferences;
import java.util.Calendar;

import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import org.softastur.asturiandictionary.MultipleMatchAdapter;
import org.softastur.asturiandictionary.R;
import org.softastur.asturiandictionary.RemoteFile;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;


/**
 * A {@link Fragment} subclass that is used to
 * Activities that contain this fragment must implement the
 * {@link Entry.DictionaryViewCallbacks} interface
 * to handle interaction events.
 * Use the {@link Entry#newInstance} factory method to
 * create an instance of this fragment.
 * Holds the entry information obtained from the server (presently in a JSON) object.
 * Therefore, other fragments must obtain their data directly from this fragment (but they do
 * their own formatting).
 */
public class Entry extends Fragment implements View.OnClickListener, ListView.OnItemClickListener,
        RemoteFile.OnReceiveDataListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    protected static final String ARG_ACTION = "action";
    protected static final String ARG_ID = "id";
    protected static final String ARG_DATA = "data";

    // these are the requests made, and should be converted as necessary to a status
    public static final int NO_ACTION = 0;
    public static final int SHOW_ENTRY = 1;
    public static final int SHOW_RANDOM = 2; // so far not used
    public static final int RELOAD_ENTRY = 3;
    public static final int DAILY_WORD = 4; // deprecated, placed into a separate subclass


    // The status is used by subfragments to determine how they should display
    public int status = AWAITING_QUERY;
    public static final int AWAITING_QUERY = 0;
    public static final int NEEDS_TO_LOAD_ENTRY = 1;
    public static final int LOADING_ENTRY = 2;
    public static final int ENTRY_LOADED = 3;

    public static final int STRING_SEARCH = 0;
    public static final int DOWNLOADTASK_ENTRY = 1;

    protected static final int DISPLAY_INTRODUCTION = 0;
    protected static final int DISPLAY_DICTIONARY = 1;

    private static final String entryQueryURL = "http://asturianu.elahorcado.net/view_entry_json_new.php?id=";

    private boolean favorite = false;
    private SharedPreferences.Editor editor;
    private SharedPreferences preferences;

    private String[] partsOfSpeech;

    // TODO: Rename and change types of parameters
    protected int initAction;
    protected int wordID;
    private String lemma;
    private int partOfSpeech;
    private JSONObject data;
    private boolean dailyMode = false;


    private DictionaryViewCallbacks mListener;

    private ListView multipleMatchList;
    private MultipleMatchAdapter multipleMatchAdapter;
    private boolean ignoreCallbacks = false;

    // Direct view access
    private EntryPagerAdapter tabAdapter;
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private View introText;
    private View entryHeader;

    private static final int TAB_COUNT = 3;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param action the action to take when loading the fragment.
     * @param id the id of the word, if any.
     * @return A new instance of fragment Entry.
     */
    // TODO: Rename and change types and number of parameters
    public static Entry newInstance(int action, int id) {
        Entry fragment = new Entry();
        Bundle args = new Bundle();
        args.putInt(ARG_ACTION, action);
        args.putInt(ARG_ID, id);
        fragment.setArguments(args);
        return fragment;
    }

    public Entry() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        System.out.println("asturianu - Entry->onCreate() begins");
        super.onCreate(savedInstanceState);
        tabAdapter = new EntryPagerAdapter(getChildFragmentManager(),this);
        boolean reset = false;
        // so first we see if we have a saved instance state, that overrides everything else
        if(savedInstanceState != null) {
            System.out.println("asturianu - there is a saved instance state");
            if(savedInstanceState.getInt("RELOAD",0) == 1) {
                initAction = RELOAD_ENTRY;
                try {
                    wordID = savedInstanceState.getInt("WORD_ID", 0);
                    lemma = savedInstanceState.getString("LEDE", "");
                    partOfSpeech = savedInstanceState.getInt("PART_OF_SPEECH", 0);
                    data = new JSONObject(savedInstanceState.getString("DATA"));
                }catch (Exception e){
                    // this catch all is not elegant, I know, but battling the
                    // number of different ways we can be created, it's the simplest
                    reset = true;
                }

            }else{
                // this would be weird, but could happen ...
                reset = true;
            }
        }else if (getArguments() != null) {
            initAction = getArguments().getInt(ARG_ACTION);
            wordID = getArguments().getInt(ARG_ID);

            if(!(initAction == NO_ACTION || initAction == SHOW_ENTRY) || wordID == 0) {
                reset = true;
            }
        }else{
            reset = true;
        }

        if(reset) {
            initAction = NO_ACTION;
            wordID = 0;
        }

        System.out.println("asturianu - initial action is: " + initAction + ", wordID = " + wordID);

        if(initAction == NO_ACTION) {
            status = AWAITING_QUERY;
        }else if(initAction == SHOW_ENTRY) {
            status = NEEDS_TO_LOAD_ENTRY;
        }else if(initAction == RELOAD_ENTRY) {
            status = ENTRY_LOADED;
        }
        System.out.println("asturianu - initial action is now " + initAction + ", status = " + status + ", wordID = " + wordID);

        preferences = getActivity().getPreferences(Context.MODE_PRIVATE);
        editor = preferences.edit();
        partsOfSpeech = getResources().getStringArray(R.array.parts_of_speech);
        System.out.println("asturianu - Entry->onCreate() ends");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        System.out.println("asturianu - Entry.onCreateView()");
//        View view = inflater.inflate(R.layout.fragment_dictionary_view, container, false);
        return inflater.inflate(R.layout.fragment_entry_main, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        System.out.println("asturianu - onviewcreeated action is now " + initAction + ", status = " + status + ", wordID = " + wordID);

        viewPager = (ViewPager) view.findViewById(R.id.pager);
        viewPager.setAdapter(tabAdapter);

        tabLayout = (TabLayout) view.findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        introText = view.findViewById(R.id.intro);
        entryHeader = view.findViewById(R.id.entry);

        view.findViewById(R.id.favorite_button).setOnClickListener(this);

        if(status == NEEDS_TO_LOAD_ENTRY) {
            RemoteFile.accessFile(entryQueryURL+wordID,this,1,8000);
            status = LOADING_ENTRY;
        }else if(status == ENTRY_LOADED) {

        }

        // there should be a different action if we get caught in the middle of downloading
        // something, as the downloader is def external to the activity.
        //if (status == ENTRY_LOADED) {
        //    tabAdapter.update();
        //}
        updateView();
//            view.findViewById(R.id.favorite_button).setOnClickListener(this);
    }

    @Override
    public void onViewStateRestored (Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        ignoreCallbacks = false;
        /*System.out.println("asturianu - Entry.onViewStateRestored()");
        if(savedInstanceState != null) {
            if(savedInstanceState.getInt("RELOAD",0) == 1) {
                getView().findViewById(R.id.loading).setVisibility(View.GONE);
                getView().findViewById(R.id.entry).setVisibility(View.VISIBLE);
                getView().findViewById(R.id.info).setVisibility(View.GONE);
                ((TextView)getView().findViewById(R.id.definition)).setText(savedInstanceState.getCharSequence("DEFINITION"));
                ((TextView)getView().findViewById(R.id.entry_lede)).setText(savedInstanceState.getString("LEDE"));
                tabAdapter.update();
            }
        }*/
    }


    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onDictionaryEntryInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);
        try {
            mListener = (DictionaryViewCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement DictionaryViewCallbacks");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface DictionaryViewCallbacks {
        // TODO: Update argument type and name
        public void onDictionaryEntryInteraction(Uri uri);
    }

    public void onClick(View view) {
        switch (view.getId()) { // at the moment there are no other buttons, but this may change
            case R.id.favorite_button:
                onClickedFavorite();
                break;
            default:
        }
    }

    protected void onClickedFavorite() {
        favorite = !favorite;
        updateFavoriteList();
        setFavoriteImage();
    }

    public void onTimeout(int tag, long time) {
        Toast toast = Toast.makeText(getContext(),"Timeout on retrieving data",Toast.LENGTH_SHORT);
        toast.show();
    }
    public void onRetrieveData(String string, int tag, long time) {
        //System.out.println("asturianu " + string);
        System.out.println("asturianu - Entry.onRetrieveData() - Got data");
        if (tag == STRING_SEARCH) { // 0 = search for word
            // deprecated
        } else if (tag == DOWNLOADTASK_ENTRY) {
            if(string.equals("")) {
                System.out.println("asturianu - DOWNLOAD TIMED OUT OR OTHER ERROR");
            }
            try {
                data = new JSONObject(string);
            } catch (JSONException e) {
                data = new JSONObject();
            }

            lemma = data.optString("lede"," ");
            partOfSpeech = data.optInt("pos",0);
            favorite = isFavorite();
            status = ENTRY_LOADED;

            updateView();
        }

    }

    protected void updateView() {
        TextView lemmaText = (TextView) getView().findViewById(R.id.leme);
        TextView posText = (TextView) getView().findViewById(R.id.info);

        switch(status) {
            case ENTRY_LOADED:
            case LOADING_ENTRY: // todo surprisingly, right now both of these are identical
                lemmaText.setText(lemma);
                posText.setText(partsOfSpeech[partOfSpeech]);
                setFavoriteImage();
                setDisplay(DISPLAY_DICTIONARY);
                tabAdapter.update();
                break;
            case AWAITING_QUERY:
            default:
                setDisplay(DISPLAY_INTRODUCTION);
        }
    }

    public void lazyLoad(int id, String word, int pos) {
        wordID = id;
        lemma = word;
        partOfSpeech = pos;
        RemoteFile.accessFile(entryQueryURL + wordID,this,1,8000);
        status = LOADING_ENTRY;
        updateView();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        System.out.println("asturianu - onItemCLick(…,…," + position + ","+id+")");
        System.out.println("asturianu - Entry->Status = LOADING_ENTRY");

        JSONObject data = (JSONObject) multipleMatchAdapter.getItem(position);
        int entryID = data.optInt("id", 0);
        wordID = data.optInt("id", 0);
        lemma = data.optString("lede", "");

        RemoteFile.accessFile(entryQueryURL + entryID,this,0,8000);

        status = LOADING_ENTRY;
        updateView();
        System.out.println("asturianu - XXXXXXXXXXXXXXXXXXXXX");

    }

    private boolean isFavorite() {
        Set<String> favoritesList = preferences.getStringSet("favorites", new HashSet<String>());

        for(String i : favoritesList) {
            int id = Integer.parseInt(i.substring(0, i.indexOf(",")));
            if(wordID == id) {
                return true;
            }
        }
        return false;
    }

    private void setFavoriteImage() {
        ImageButton ib = (ImageButton)getView().findViewById(R.id.favorite_button);
        ib.setImageResource(favorite ? R.drawable.favorite_set : R.drawable.favorite_unset);
    }

    private void updateFavoriteList() {
        Set<String> favoritesList = new HashSet<String>(preferences.getStringSet("favorites", new HashSet<String>()));
        if(favorite) {
            // make sure it's added to the list
            for(String i : favoritesList) {
                int id = Integer.parseInt(i.substring(0, i.indexOf(",")));
                if(wordID == id) {
                    // it was already in the list so bail
                    return;
                }
            }
            // 'twasn't in the list, so add and commit
            favoritesList.add(Integer.toString(wordID) + "," + Integer.toString(partOfSpeech) + "," + lemma);
        }else{
            // make sure it's deleted
            String toRemove = null;
            for(String i : favoritesList) {
                int id = Integer.parseInt(i.substring(0, i.indexOf(",")));

                if(wordID == id) {
                    toRemove = i;
                }
            }
            if(toRemove != null) favoritesList.remove(toRemove);
        }
        editor.putStringSet("favorites",favoritesList);
        editor.commit();
    }

    public void onSaveInstanceState(Bundle savedInstanceState) {
        System.out.println("asturianu - Entry.onSaveInstanceState();");
        savedInstanceState.putInt("RELOAD",1);
        savedInstanceState.putInt("WORD_ID", wordID);
        savedInstanceState.putInt("PART_OF_SPEECH", partOfSpeech);
        savedInstanceState.putString("LEMMA", lemma);
        // on reload, if the data is "", we will reload based on the wordID.  TODO: implement a
        // service which will allow the data to be done across a reload.
        savedInstanceState.putString(
                "DATA",data == null
                    ? ""
                    : status == ENTRY_LOADED
                        ? data.toString()
                        : ""
        );
        ignoreCallbacks = true;
        super.onSaveInstanceState(savedInstanceState);
    }

    public int getStatus() {
        return status;
    }

    /////////////////////
    // Private classes //
    /////////////////////
    private class EntryPagerAdapter extends FragmentPagerAdapter{
        private Entry entry;
        ArrayList<EntryReceiver> loadedFragments = new ArrayList<>();

        public EntryPagerAdapter(FragmentManager fragmentManager, Entry entry) {
            super(fragmentManager);
            System.out.println("asturianu - new EntryPagerAdapter()");
            this.entry = entry;
        }
        @Override
        public int getCount() {
            return TAB_COUNT; // todo for conjunctions, etc, may be better to display only two
                              // or alternative disable based on data.
        }
        @Override
        public Fragment getItem(int position) {
            switch(position) {
                case 0: return new Definitions();
                case 1: return new Phrases();
                case 2: return new Forms();
                default: return null; // should never happen
            }
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            EntryReceiver createdFragment = (EntryReceiver) super.instantiateItem(container, position);

            int id = (int) (Math.random() * 9999); // for debuging, probably can delete once
                                                   // view pager is 100% working
            System.out.println("asturianu - Entry.instantiateItem(...," + position + "); [id: " + id + "]");
            // save the appropriate reference depending on position
            createdFragment.update();
            createdFragment.setID(id);
            loadedFragments.add(createdFragment);
            return createdFragment;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            System.out.println("asturianu - EntryPagerAdapter.destroyItem(..., "+position+",obj)");
            loadedFragments.remove(object);
            super.destroyItem(container,position,object);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            String title = null;
            if (position == 0) {
                title = getResources().getString(R.string.entry_definitions);
            } else if (position == 1) {
                title = getResources().getString(R.string.entry_phrases);
            } else if (position == 2) {
                title = getResources().getString(R.string.entry_forms);
            }
            return title;
        }

        public void update() {
            System.out.println("asturianu - EntryPagerAdapter.update();");
            for (EntryReceiver receiver : loadedFragments) {
                receiver.update();
            }
        }
    }

    /////////////////////////////////////
    // Interfaces and external getters //
    /////////////////////////////////////

    public interface EntryReceiver {
        void update();
        void setID(int id);
    }
    public JSONObject getData() {
        return data;
    }

    ///////////////////////////
    // Purely visual methods //
    ///////////////////////////

    protected void setDisplay(int type) {
        switch (type) {
            case DISPLAY_INTRODUCTION:
                introText.setVisibility(View.VISIBLE);
                entryHeader.setVisibility(View.GONE);
                tabLayout.setVisibility(View.GONE);
                viewPager.setVisibility(View.GONE);
                break;
            case DISPLAY_DICTIONARY:
                introText.setVisibility(View.GONE);
                entryHeader.setVisibility(View.VISIBLE);
                tabLayout.setVisibility(View.VISIBLE);
                viewPager.setVisibility(View.VISIBLE);
                break;
            default:
        }
    }

    private void toast(String s) {
        Toast toast = Toast.makeText(getContext(), s, Toast.LENGTH_SHORT);
        toast.show();
    }

}

