package org.softastur.asturiandictionary;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FragmentDictionary.DictionaryViewCallbacks} interface
 * to handle interaction events.
 * Use the {@link FragmentDictionary#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentDictionary extends Fragment implements View.OnClickListener, ListView.OnItemClickListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "action";
    private static final String ARG_PARAM2 = "id";

    public static final int NO_ACTION = 0;
    public static final int SHOW_ENTRY = 1;
    public static final int SHOW_RANDOM = 2;
    public static final int RELOAD_ENTRY = 3;

    private static final String entryQueryURL = "http://asturianu.elahorcado.net/view_entry_json.php?id=";

    private boolean favorite = false;
    private SharedPreferences.Editor editor;
    private SharedPreferences preferences;

    // TODO: Rename and change types of parameters
    private int mAction;
    private int mId;
    private String mLede;
    private int mPOS;
    private CharSequence mDefinitionFormatted;

    private DictionaryViewCallbacks mListener;

    private ListView multipleMatchList;
    private MultipleMatchAdapter multipleMatchAdapter;
    private View loadingWarning;
    private View entryView;
    private boolean ignoreCallbacks = false;
    // These are the formatting options
    private DictionaryViewStyles styler = new DictionaryViewStyles();
    private View mainView;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param action Parameter 1.
     * @param id Parameter 2.
     * @return A new instance of fragment FragmentDictionary.
     */
    // TODO: Rename and change types and number of parameters
    public static FragmentDictionary newInstance(int action, int id) {
        FragmentDictionary fragment = new FragmentDictionary();
        Bundle args = new Bundle();
        args.putInt(ARG_PARAM1, action);
        args.putInt(ARG_PARAM2, id);
        fragment.setArguments(args);
        return fragment;
    }

    public FragmentDictionary() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(savedInstanceState != null) {
            if(savedInstanceState.getInt("RELOAD",0) == 1) {
                mId = savedInstanceState.getInt("WORD_ID",0);
                mLede = savedInstanceState.getString("LEDE","");
                mPOS = savedInstanceState.getInt("PART_OF_SPEECH",0);
                mAction = RELOAD_ENTRY;
            }
        }else if (getArguments() != null) {
            mAction = getArguments().getInt(ARG_PARAM1);
            mId = getArguments().getInt(ARG_PARAM2);
        }else{
                mAction = NO_ACTION;
                mId = 0;
        }
        preferences = getActivity().getPreferences(Context.MODE_PRIVATE);
        editor = preferences.edit();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        System.out.println("asturianu - SHOULD BE CREATING VIEW");
        View view = inflater.inflate(R.layout.fragment_dictionary_view, container, false);
        loadingWarning = view.findViewById(R.id.loading);
        entryView = view.findViewById(R.id.entry);

        if (mAction == SHOW_ENTRY) {
            System.out.println("Ast - SHOULD BE INITIATING A DOWNLOAD");
            view.findViewById(R.id.conjugate_button).setOnClickListener(this);
            view.findViewById(R.id.favorite_button).setOnClickListener(this);
            DownloadTask task = new DownloadTask();
            task.setTag(1);
            task.execute(entryQueryURL + mId);
        } else if (mAction == NO_ACTION) {
            System.out.println("Ast - SHOULD BE TAKING NO ACTION");
            view.findViewById(R.id.loading).setVisibility(View.GONE);
            view.findViewById(R.id.info).setVisibility(View.VISIBLE);
        } else if (mAction == RELOAD_ENTRY) {
            System.out.println("Ast - SHOULD BE RELOADING ENTRY");
            view.findViewById(R.id.conjugate_button).setOnClickListener(this);
            view.findViewById(R.id.favorite_button).setOnClickListener(this);
            view.findViewById(R.id.loading).setVisibility(View.GONE);
            view.findViewById(R.id.entry).setVisibility(View.VISIBLE);
            view.findViewById(R.id.info).setVisibility(View.GONE);
            mDefinitionFormatted = savedInstanceState.getCharSequence("DEFINITION");
            mLede = savedInstanceState.getString("LEDE");
            mId = savedInstanceState.getInt("WORD_ID");
            mPOS = savedInstanceState.getInt("PART_OF_SPEECH");
            ((TextView)view.findViewById(R.id.definition)).setText(mDefinitionFormatted);
            ((TextView)view.findViewById(R.id.entry_lede)).setText(mLede);
            view.findViewById(R.id.conjugate_button).setVisibility(mPOS == 2 ? View.VISIBLE : View.GONE);
        }
        mainView = view;
        return view;
    }

    @Override
    public void onViewStateRestored (Bundle savedInstanceState) {
        System.out.println("SHOULD BE RESTORING A VIEW STATE");
        if(savedInstanceState != null) {
            if(savedInstanceState.getInt("RELOAD",0) == 1) {
                getView().findViewById(R.id.loading).setVisibility(View.GONE);
                getView().findViewById(R.id.entry).setVisibility(View.VISIBLE);
                getView().findViewById(R.id.info).setVisibility(View.GONE);
                ((TextView)getView().findViewById(R.id.definition)).setText(savedInstanceState.getCharSequence("DEFINITION"));
                ((TextView)getView().findViewById(R.id.entry_lede)).setText(savedInstanceState.getString("LEDE"));
            }
        }
        super.onViewStateRestored(savedInstanceState);
    }


    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onDictionaryEntryInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
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
        public void onShowConjugations(int id);
    }

    public void onClick(View view) {
        if(view.getId() == R.id.conjugate_button) {
            mListener.onShowConjugations(mId);

        }else if(view.getId() == R.id.favorite_button) {
            toggleFavorite();
            System.out.println("ASTv should be setting favorite");
        }
    }

    public void onRetrieveData(String string, int tag) {
        JSONObject data;
        System.out.println("asturianu " + string);
        if (tag == 0) { // 0 = search for word
            try {
                data = new JSONObject(string);
                multipleMatchAdapter.resetWithJson(data);
            } catch (JSONException e) {
                return;
            }
            // warning get view can be null when unpausing/restarting!! TODO
            if(loadingWarning != null) {
                entryView.setVisibility(View.GONE);
                loadingWarning.setVisibility(View.GONE);
            }
        } else if (tag == 1) { // 1 = call up entry
            if(loadingWarning != null) {
                loadingWarning.setVisibility(View.VISIBLE);
                getView().findViewById(R.id.info).setVisibility(View.GONE);

                JSONObject json;
                try {
                    json = new JSONObject(string);

                } catch (JSONException e) {
                    json = new JSONObject();
                }

                mDefinitionFormatted = formatEntry(json);
                ((TextView) getView().findViewById(R.id.definition)).setText(mDefinitionFormatted);

                int part_of_speech = json.optInt("pos", 0);
                switch (part_of_speech) {
                    case 2:
                        getView().findViewById(R.id.conjugate_button).setVisibility(View.VISIBLE);
                        break;
                    default:
                        getView().findViewById(R.id.conjugate_button).setVisibility(View.GONE);

                }

                favorite = isFavorite();
                setFavoriteImage();

                getView().findViewById(R.id.loading).setVisibility(View.GONE);
                getView().findViewById(R.id.entry).setVisibility(View.VISIBLE);
            }
        }

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        System.out.println("asturianu clicked on item " + position);
        JSONObject data = (JSONObject) multipleMatchAdapter.getItem(position);
        int entryID = data.optInt("id", 0);
        DownloadTask task = new DownloadTask();
        task.setTag(1);
        task.execute(entryQueryURL + entryID);
    }

    private boolean isFavorite() {
        Set<String> favoritesList = preferences.getStringSet("favorites", new HashSet<String>());

        for(String i : favoritesList) {
            int id = Integer.parseInt(i.substring(0, i.indexOf(",")));
            if(mId == id) {
                return true;
            }
        }
        return false;
    }

    private void toggleFavorite() {
        if(favorite) {
            favorite = false;
        }else{
            favorite = true;
        }
        updateFavoriteList();
        setFavoriteImage();
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
                if(mId == id) {
                    // it was already in the list so bail
                    return;
                }
            }
            // 'twasn't in the list, so add and commit
            favoritesList.add(Integer.toString(mId) + "," + Integer.toString(mPOS) + "," + mLede);
        }else{
            // make sure it's deleted
            String toRemove = null;
            for(String i : favoritesList) {
                int id = Integer.parseInt(i.substring(0, i.indexOf(",")));

                if(mId == id) {
                    toRemove = i;
                }
            }
            if(toRemove != null) favoritesList.remove(toRemove);
        }
        editor.putStringSet("favorites",favoritesList);
        editor.commit();
    }

    private class DownloadTask extends AsyncTask<String, Void, String> {

        int tag;
        public void setTag(int i) {tag = i;}

        @Override
        protected String doInBackground(String... urls) {
            URL url;
            HttpURLConnection urlConnection;
            String result;

            try {
                url = new URL(urls[0]);
                urlConnection = (HttpURLConnection) url.openConnection();

            }catch(Exception e) {
                return "";
            }

            try {
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                result = inputStreamToString(in, "UTF-8");
            }catch(IOException e) {
                return "";
            }finally {
                urlConnection.disconnect();
            }

            return result;
        }

        @Override
        protected void onPostExecute(String result){
            if(!ignoreCallbacks) onRetrieveData(result,tag);
        }

        private static final int BUFFER_SIZE = 4 * 1024;

        public String inputStreamToString(InputStream inputStream, String charsetName)
                throws IOException {
            StringBuilder builder = new StringBuilder();
            InputStreamReader reader = new InputStreamReader(inputStream, charsetName);
            char[] buffer = new char[BUFFER_SIZE];
            int length;
            while ((length = reader.read(buffer)) != -1) {
                builder.append(buffer, 0, length);
            }
            return builder.toString();
        }
    }

    private CharSequence formatEntry(JSONObject data) {
        // This should eventually be broken into two parts -- one to process the JSON the other to format for display
        if(data == null) return "";
        FormattedStringBuilder builder = new FormattedStringBuilder();
        try {
            mLede = data.optString("lede"," ");
            mPOS = data.optInt("pos",0);
            TextView lede = (TextView) getView().findViewById(R.id.entry_lede);
            lede.setText(mLede);

            JSONArray definitions = data.getJSONArray("definitions");

            //
            // Step 1: Format the definitions (if any)
            // Each definition is contained in a JSONObject with the following fields:
            //    - tags (JSONArray of Strings)
            //    - expl (0 = normal definition, 1 = explicative definition that describes instead of defines)
            //    - def  (the definition itself. Minor content found in {{ }}
            //    - examples (JSONArray of Strings)

            if(definitions != null) {
                for(int i = 0; i < definitions.length(); i++) {

                    // new line for new definition
                    if(i!=0) {builder.append("\n");}

                    // Add the definition number
                    builder.append(Integer.toString(i+1)+".", styler.definitionNumber());

                    // space between number and definition
                    builder.append(" ");

                    // definition
                    String definition = definitions.getJSONObject(i).optString("def","");
                    int offset = 0;
                    int newOffset = 0;
                    boolean explicative = (definitions.getJSONObject(i).optInt("expl",0) == 1);

                    if(explicative) { builder.append("*",styler.explicativeStar());}

                    while(definition.indexOf("{{",offset) != -1) {
                        newOffset = definition.indexOf("{{",offset);
                        builder.append(
                                definition.substring(offset,newOffset),
                                (explicative ? styler.explicativeDefinition() : styler.normalDefinition())
                        );

                        offset = newOffset+2;
                        newOffset = definition.indexOf("}}",offset);
                        if(newOffset == -1) {
                            builder.append(
                                    "[" + definition.substring(offset) + "]",
                                    (explicative ? styler.secondaryExplicativeDefinition() : styler.secondaryDefinition())
                            );
                            offset = definition.length();
                        }else{
                            builder.append(
                                    "[" + definition.substring(offset,newOffset) + "]",
                                    (explicative ? styler.secondaryExplicativeDefinition() : styler.secondaryDefinition())
                            );
                            offset = newOffset + 2;
                        }
                    }
                    builder.append(
                            definition.substring(offset),
                            (explicative ? styler.explicativeDefinition() : styler.normalDefinition())
                    );

                    // definition examples
                    JSONArray definitionExamples = definitions.getJSONObject(i).optJSONArray("examples");
                    if(definitionExamples != null) {
                        builder.append("\n");
                        for(int j = 0; j < definitionExamples.length(); j++) {
                            builder.append(
                                    definitionExamples.optString(j,"") + "\n",
                                    styler.definitionExample()
                            );
                        }
                    } // end definition examples
                }
            }

            // phrases
            JSONArray phrases = data.optJSONArray("phrases");
            if(phrases != null) {
                for(int j = 0; j < phrases.length(); j++) {
                    JSONObject phrase = phrases.getJSONObject(j);

                    // lede
                    builder.append(phrase.optString("lede","") + "\n", styler.phraseLede());

                    JSONArray phraseDefinitions = phrase.optJSONArray("def");
                    if(phraseDefinitions != null) {
                        for(int k = 0; k < phraseDefinitions.length(); k++) {
                            builder.append(
                                    Integer.toString(k+1) + ". ",
                                    styler.phraseDefinition()
                            );

                            String phraseDefinition = phraseDefinitions.getJSONObject(k).optString("def","");
                            int offset = 0;
                            int newOffset = 0;

                            while(phraseDefinition.indexOf("{{",offset) != -1) {
                                newOffset = phraseDefinition.indexOf("{{",offset);
                                builder.append(
                                        phraseDefinition.substring(offset,newOffset),
                                        new Object[]{new ForegroundColorSpan(Color.BLACK)}/*,
                                                new LeadingMarginSpan.Standard(75,100)*/
                                );

                                offset = newOffset+2;
                                newOffset = phraseDefinition.indexOf("}}", offset);
                                if(newOffset == -1) {
                                    builder.append(
                                            "[" + phraseDefinition.substring(offset) + "]",
                                            new Object[]{new ForegroundColorSpan(Color.GRAY)}/*,
                                                    new LeadingMarginSpan.Standard(75,100)*/
                                    );
                                    offset = phraseDefinition.length();
                                }else{
                                    builder.append(
                                            "[" + phraseDefinition.substring(offset,newOffset) + "]",
                                            new Object[]{new ForegroundColorSpan(Color.GRAY)}/*,
                                                    new LeadingMarginSpan.Standard(75,100)*/
                                    );
                                    offset = newOffset + 2;
                                }
                            }
                            builder.append(
                                    phraseDefinition.substring(offset) + "\n",
                                    new Object[]{new ForegroundColorSpan(Color.BLACK)}
                            );

                            JSONArray phraseDefinitionExamples = phraseDefinitions.optJSONArray(k);
                            if(phraseDefinitionExamples != null) {
                                for(int l = 0; l < phraseDefinitionExamples.length(); l++) {
                                    builder.append(
                                            phraseDefinitionExamples.optString(l,"") + "\n",
                                            styler.phraseDefinitionExample()
                                    );
                                }
                            }
                        }
                    }


                }
            }


            return builder.build();
        } catch (JSONException e) {
        }
        return "";
    }

    public void onSaveInstanceState(Bundle savedInstanceState) {
        System.out.println("asturianu - SHOULD BE SAVING VIEW DATA");
        savedInstanceState.putInt("RELOAD",1);
        savedInstanceState.putInt("WORD_ID",mId);
        savedInstanceState.putInt("PART_OF_SPEECH",mPOS);
        savedInstanceState.putString("LEDE",mLede);
        savedInstanceState.putCharSequence("DEFINITION",mDefinitionFormatted);
        ignoreCallbacks = true;
        super.onSaveInstanceState(savedInstanceState);
    }

}

