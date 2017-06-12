package org.softastur.asturiandictionary;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static org.softastur.asturiandictionary.FragmentDictionary.NO_ACTION;
import static org.softastur.asturiandictionary.FragmentDictionary.RELOAD_ENTRY;
import static org.softastur.asturiandictionary.R.xml.preferences;

/**
 * Created by guifa on 5/29/17.
 */

public class FragmentEntryMain extends Fragment {

    CharSequence formattedEntry;
    static String ARG_PARAM1 = "PARAM1";
    static String ARG_PARAM2 = "PARAM2";

    JSONObject json;
    int id;
    String lede;
    int pos;
    int action;
    SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private FragmentDictionary.DictionaryViewCallbacks listener;

    // TODO: Rename and change types and number of parameters
    public static FragmentEntryMain newInstance(String param1, String param2, JSONObject json) {
        FragmentEntryMain fragment = new FragmentEntryMain();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        args.putString("json", json.toString());
        fragment.setArguments(args);
        return fragment;
    }

    public FragmentEntryMain() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(savedInstanceState != null) {
            if(savedInstanceState.getInt("RELOAD",0) == 1) {
                id = savedInstanceState.getInt("WORD_ID",0);
                lede = savedInstanceState.getString("LEDE","");
                pos = savedInstanceState.getInt("PART_OF_SPEECH",0);
                action = RELOAD_ENTRY;
            }
        }else if (getArguments() != null) {
            action = getArguments().getInt(ARG_PARAM1);
            id = getArguments().getInt(ARG_PARAM2);
            try {
                json = new JSONObject(getArguments().getString("json"));
            } catch (JSONException e) {

            }
        }else{
            action = NO_ACTION;
            id = 0;
        }
        preferences = getActivity().getPreferences(Context.MODE_PRIVATE);
        editor = preferences.edit();
    }


    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    public void onSaveInstanceState(Bundle savedInstanceState) {
        System.out.println("asturianu - SHOULD BE SAVING VIEW DATA");
        savedInstanceState.putInt("RELOAD",1);
        savedInstanceState.putInt("WORD_ID",id);
        savedInstanceState.putInt("PART_OF_SPEECH",pos);
        savedInstanceState.putString("LEDE",lede);
        savedInstanceState.putCharSequence("DEFINITION",formattedEntry);
        //ignoreCallbacks = true;
        super.onSaveInstanceState(savedInstanceState);
    }

}
