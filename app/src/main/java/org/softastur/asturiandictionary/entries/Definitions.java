package org.softastur.asturiandictionary.entries;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.softastur.asturiandictionary.ActivityMain;
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

public class Definitions extends android.support.v4.app.Fragment implements Entry.EntryReceiver {

    CharSequence formattedEntry;

    SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private Entry.DictionaryViewCallbacks listener;


    private JSONArray data;
    private static final String newLine = "\n";
    private DictionaryViewStyles styler;

    private View loadingDisplay;
    private TextView textDisplay;
    private Entry entry;

    // for debugging
    private boolean LOG = false;
    private int id = 0;


    public static Definitions newInstance() {
        return new Definitions();
        //Bundle args = new Bundle();
        //args.putString("data", data);
        //fragment.setArguments(args);

    }

    public Definitions() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        styler = new DictionaryViewStyles((ActivityMain)getActivity());
        entry = (Entry) getParentFragment();
        _log(".onAttach()");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        _log(".onDetach()");
        entry = null;
        //listener = null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _log(".onCreate()");

        preferences = getActivity().getPreferences(Context.MODE_PRIVATE);
        editor = preferences.edit();
        if(savedInstanceState != null) {
            String json = savedInstanceState.getString("JSONdata","");
            if(json != "") {
                try {
                    data = new JSONArray(json);
                }catch(JSONException e) {
                    data = new JSONArray();
                }
            }else{
                data = new JSONArray();
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) { // todo recode completely
        _log(".onSavedInstanceState()");
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putString("JSONdata",data.toString());

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        _log(".onCreatView();");
        return inflater.inflate(R.layout.fragment_entry_definitions, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view,savedInstanceState);
        _log(".onViewCreated();");
        textDisplay = (TextView) view.findViewById(R.id.text);
        loadingDisplay = view.findViewById(R.id.loading);
        textDisplay.setMovementMethod(LinkMovementMethod.getInstance());
        updateDisplay();
    }

    private void updateDisplay() {
        // This may get called occasionally before the view has been created, if so, bail
        if (textDisplay == null || loadingDisplay == null) {
            _log(".updateDisplay(); null views");
            return;
        }

        switch(entry.getStatus()) {
            case AWAITING_QUERY:
                _log("Definitions.updateDisplay()->AwaitingQuery");
                // viewpager that contains this fragment will be hidden,
                // so we don't need to do anything at all
                break;

            case LOADING_ENTRY:
                _log(".updateDisplay()->LoadingEntry");
                textDisplay.setVisibility(GONE);
                loadingDisplay.setVisibility(VISIBLE);
                break;

            case ENTRY_LOADED:
                _log(".updateDisplay()->EntryLoaded");
                data = entry.getData().optJSONArray("definitions");
                loadingDisplay.setVisibility(GONE);
                textDisplay.setVisibility(VISIBLE);
                textDisplay.setText(formatDefinitions()); // ,TextView.BufferType.SPANNABLE
                textDisplay.invalidate();
                break;
            default:
        }
    }

    private CharSequence formatDefinitions() {
        if(data == null) return "No data available in Entry object";

        FormattedStringBuilder builder = new FormattedStringBuilder(true);

        try {
            // perhaps just these definitions should be returned from the entry object.
            JSONArray definitions = data;

            // Each definition is contained in a JSONObject with the following fields:
            //    - tags (JSONArray of Strings)
            //    - expl (0 = normal definition, 1 = explicative definition that describes instead of defines)
            //    - def  (the definition itself. Minor content found in {{ }}
            //    - examples (JSONArray of Strings)

            for(int i = 0; i < definitions.length(); i++) {

                // new line for new definition, except the first one
                if(i!=0) {builder.append("\n");}

                // Add the definition number
                builder.beginFormat(styler.definitionNumber())
                        .append(Integer.toString(i+1)+".")
                        .endFormat();

                // space between number and definition
                builder.append(" ");

                // definition
                String definition = definitions.getJSONObject(i).optString("def","");
                int offset = 0;
                int newOffset = 0;
                boolean explicative = (definitions.getJSONObject(i).optInt("expl",0) == 1);
                if(explicative) {
                    builder.beginFormat(styler.explicativeStar())
                            .append("*")
                            .endFormat()
                            .beginFormat(styler.explicativeDefinition());
                }else{
                    builder.beginFormat(styler.normalDefinition());
                }

                styler.processMarkup(definition,builder);

                builder.endFormat();

                // definition examples
                JSONArray definitionExamples = definitions.getJSONObject(i).optJSONArray("examples");
                if(definitionExamples != null) {
                    builder.append("\n");
                    for(int j = 0; j < definitionExamples.length(); j++) {
                        builder.beginFormat(styler.definitionExample())
                                .append(definitionExamples.optString(j,"") + "\n")
                                .endFormat();
                    }
                } // end definition examples
            }
            return builder.build();
        }catch (JSONException e) {
            _log(".formatDefinitions() --> error processing definitions");
            return "Error processing entry data";
        }


    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        _log(".onDestroyView()");
    }

    public void update() {
        _log(".update()");
        updateDisplay();
    }
    public void setID(int i) {
        id = i;
    }
    private void _log(String s) {
        if (LOG) System.out.println("asturianu - [" + id + "] Definitions" + s);
    }

}
