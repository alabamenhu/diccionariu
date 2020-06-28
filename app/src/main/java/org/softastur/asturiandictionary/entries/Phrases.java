package org.softastur.asturiandictionary.entries;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.softastur.asturiandictionary.DictionaryViewStyles;
import org.softastur.asturiandictionary.FormattedStringBuilder;
import org.softastur.asturiandictionary.R;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static org.softastur.asturiandictionary.entries.Entry.AWAITING_QUERY;
import static org.softastur.asturiandictionary.entries.Entry.ENTRY_LOADED;
import static org.softastur.asturiandictionary.entries.Entry.LOADING_ENTRY;

/**
 * Created by guifa on 5/29/17.
 */
// chillylion139/myspectrum2g
public class Phrases extends android.support.v4.app.Fragment implements Entry.EntryReceiver {

    CharSequence formattedEntry;

    SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private Entry.DictionaryViewCallbacks listener;
    private Entry entry;

    private JSONArray data;
    private static final String newLine = "\n";
    private DictionaryViewStyles styler = new DictionaryViewStyles();
    TextView textDisplay;
    View loadingDisplay;

    public static Phrases newInstance() {
        Phrases fragment = new Phrases();
        return fragment;
    }

    public static Phrases newInstanceWithData(JSONObject data) {
        return newInstanceWithData(data.toString());
    }

    public static Phrases newInstanceWithData(String data) {
        Phrases fragment = new Phrases();
        Bundle args = new Bundle();
        args.putString("data", data);
        fragment.setArguments(args);
        return fragment;
    }

    public Phrases() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        entry = (Entry) getParentFragment();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState != null) {
            String json = savedInstanceState.getString("JSON_DATA","");
            if(json != "") {
                try {
                    data = new JSONArray(json);
                }catch(JSONException e) {

                }
            }
        }
        System.out.println("asturianu - [" + id + "] Phrases.onCreate()");
        preferences = getActivity().getPreferences(Context.MODE_PRIVATE);
        editor = preferences.edit();
    }


    public void onSaveInstanceState(Bundle savedInstanceState) { // todo recode completely
        System.out.println("asturianu - [" + id + "] Phrases.onSavedInstanceState()");
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putString("JSON_DATA",data.toString());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_entry_phrases, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        System.out.println("asturianu - [" + id + "] Phrases.onViewCreated();");
        textDisplay = (TextView) view.findViewById(R.id.text);
        loadingDisplay = view.findViewById(R.id.loading);
    }

    private void updateDisplay() {
        if (textDisplay == null || loadingDisplay == null) {
            System.out.println("asturianu - [" + id + "] Phrases.updateDisplay(); null views");
            return;
        }

        switch(entry.getStatus()) {
            case AWAITING_QUERY:
                System.out.println("asturianu - [" + id + "] Phrases.updateDisplay()->AwaitingQuery");

                break;
            case LOADING_ENTRY:
                System.out.println("asturianu - [" + id + "] Phrases.updateDisplay()->LoadingEntry");
                textDisplay.setVisibility(GONE);
                loadingDisplay.setVisibility(VISIBLE);
                break;

            case ENTRY_LOADED:
                System.out.println("asturianu - [" + id + "] Phrases.updateDisplay()->EntryLoaded");
                data = entry.getData().optJSONArray("phrases");
                loadingDisplay.setVisibility(GONE);
                textDisplay.setVisibility(VISIBLE);
                textDisplay.setText(formatPhrases());
                System.out.println("asturianu - [" + id + "] Stringified content:" + textDisplay.getText().toString());
                System.out.println("asturianu - [" + id + "] Length of content:" + textDisplay.getText().toString().length());
                textDisplay.invalidate();
                //if(getView() != null) getView().invalidate();
                //get.invalidate();
                break;
            default:
        }
    }

    private CharSequence formatPhrases() {
        if(data == null) return "";
        FormattedStringBuilder builder = new FormattedStringBuilder(true);
        try {
            // phrases
            JSONArray phrases = data;
            if(phrases != null) {
                for(int j = 0; j < phrases.length(); j++) {
                    JSONObject phrase = phrases.getJSONObject(j);

                    // lede
                    builder.beginFormat(styler.phraseLede())
                            .append(phrase.optString("lede","") + "\n")
                            .endFormat();

                    JSONArray phraseDefinitions = phrase.optJSONArray("def");
                    if(phraseDefinitions != null) {
                        for(int k = 0; k < phraseDefinitions.length(); k++) {
                            builder.beginFormat(styler.phraseDefinition())
                                    .append(Integer.toString(k+1) + ". ");
                            String phraseDefinition = phraseDefinitions.getJSONObject(k).optString("def","");
                            styler.processMarkup(phraseDefinition,builder);

                            builder.endFormat()
                                    .append("\n");

                            JSONArray phraseDefinitionExamples = phraseDefinitions.optJSONArray(k);
                            if(phraseDefinitionExamples != null) {
                                for(int l = 0; l < phraseDefinitionExamples.length(); l++) {
                                    builder.beginFormat(styler.phraseDefinitionExample());
                                    styler.processMarkup(phraseDefinitionExamples.optString(l,"") + "\n",builder);
                                    builder.endFormat();
                                }
                            }

                            //builder.endFormat();
                        }
                    }
                }
            }


            return builder.build();
        } catch (JSONException e) {
        }
        return "";


    }

    public void setEntry(Entry e) {
        entry = e;
    }
    public void update() {
        System.out.println("asturianu - [" + id + "] Phrases.update()");
        updateDisplay();
    }
    int id = 0;
    public void setID(int i) {
        id = i;
    }

}
